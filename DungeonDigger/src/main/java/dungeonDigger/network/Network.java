package dungeonDigger.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.newdawn.slick.geom.Vector2f;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.EndPoint;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;

import dungeonDigger.Enums.GameState;
import dungeonDigger.Tools.References;
import dungeonDigger.contentGeneration.GameSquare;
import dungeonDigger.entities.Agent;
import dungeonDigger.entities.GameObject;
import dungeonDigger.entities.NetworkPlayer;
import dungeonDigger.gameFlow.MultiplayerDungeon;

public class Network {
	static public final int port = 54555;
	//private Logger logger = Logger.getLogger("DungeonDigger.Network");
	
	// This registers objects that are going to be sent over the network.
	static public void register(EndPoint endPoint) {
		Kryo kryo = endPoint.getKryo();
		// TODO: LOGGER SETTINGS
		Log.set(Log.LEVEL_DEBUG);
		kryo.register(TextPacket.class);
		kryo.register(ChatPacket.class);
		kryo.register(LoginRequest.class);
		kryo.register(LoginResponse.class);
		kryo.register(SignOff.class);
		kryo.register(GameStartPacket.class);
		kryo.register(GameJoinPacket.class);
		kryo.register(PlayerInfoPacket.class);
		kryo.register(PlayerListPacket.class);
		kryo.register(GameObject.class);
		kryo.register(Agent.class);
		kryo.register(NetworkPlayer.class);
		kryo.register(NetworkPlayer[].class);
		kryo.register(TilesResponse.class);
		kryo.register(TilesRequest.class);
		kryo.register(TileResponse.class);
		kryo.register(GameSquare.class);
		kryo.register(GameSquare[][].class);
		kryo.register(WholeMapPacket.class);
		kryo.register(Vector.class);
		kryo.register(PlayerMovementUpdate.class);
		kryo.register(PlayerMovementRequest.class);
		kryo.register(PlayerMovementResponse.class);
	}
	
	//////////////////////////////////
	// Listeners that the game uses //
	//////////////////////////////////
	static public class ServerListener extends Listener {
		Logger logger = Logger.getLogger("ServerListener");
		
		@Override
		public void received(Connection connection, Object object) {
			logger.setLevel(Level.OFF);
			// LoginRequests handling
			if( object instanceof LoginRequest ) {
				logger.info("Recieved login request");
				LoginRequest login = (LoginRequest)object;
				// if already in, decline request
				// TODO: kick previous?
				if( References.ACTIVESESSIONNAMES.contains(login.account) ) { 
					connection.sendTCP(new LoginResponse(false));
					return; 
				}
				// accept, add to active sessions, create playerinfo from account name
				connection.sendTCP(new LoginResponse(true));
				References.ACTIVESESSIONNAMES.add(login.account);
				PlayerInfoPacket info = new PlayerInfoPacket();
				
				// if we have a record, use that info, otherwise new account created
				if( References.CHARACTERBANK.get(login.account) != null ) {
					logger.info("Login was a pre-existing player: " + login.account);					
					info.player = References.CHARACTERBANK.get(login.account);
					connection.sendTCP(info);
				} else {
					logger.info("Login was a new player: " + login.account);
					NetworkPlayer player = new NetworkPlayer();
					player.setName(login.account);
					player.setHitPoints(20);
					player.setSpeed(3);
					player.setIconName("dwarf1.png");
					player.setPosition(0, 0);
					info.player = player;
					References.CHARACTERBANK.put(login.account, player);
					connection.sendTCP(info);
				}
				// we're a server now if we weren't before, setup lobby
				// TODO: alter to accomodate real-time joining
				References.STATE = ConnectionState.SERVING;						
				logger.info("Sent text packet");
				connection.sendTCP(new TextPacket("Dungeon Lobby", 75, 75, 0));
			}
			// Handle chats and echo back to clients (if server), 10 most recent
			// TODO: keep up to 100? allow client to change chat size
			if( object instanceof ChatPacket ) {
				logger.info("Recieved a chat packet.");
				References.CHATS.add((ChatPacket)object);
				logger.info("Sent ech of chat to all users.");
				if( connection.getEndPoint() instanceof Server ) {
					((Server)connection.getEndPoint()).sendToAllTCP(object);
				}
				if( References.CHATS.size() > 10 ) { References.CHATS.remove(); }
			}
			// Handle perma-texts to display on the screen(scores, global msgs, etc)
			if( object instanceof TextPacket ) {
				logger.info("Recieved a text packet.");
				References.TEXTS.add( (TextPacket)object );
			}
			// Signoff - remove them from active sessions
			if( object instanceof SignOff ) {
				logger.info("Recieved a sign off packet.");
				References.ACTIVESESSIONNAMES.remove( ((SignOff)object).account );
				logger.info("Closing connection");
				connection.close();
			}
			// Movement request - validate or invalidate to client
			if( object instanceof PlayerMovementRequest ) {
				PlayerMovementRequest packet = (PlayerMovementRequest)object;
				int x = (int)packet.x / References.CLIENT_VIEW.getRatioRow();
				int y = (int)packet.y / References.CLIENT_VIEW.getRatioCol();
				logger.info("Recieved a player movement request packet for " + packet.player + " to move to " + "X:" + packet.x + " Y:" + packet.y);
				boolean passable = References.CLIENT_VIEW.dungeon[y][x].isPassable();
				logger.info("Position was passable: " + passable);
				if( passable ) {					
					for( NetworkPlayer player : References.PLAYER_LIST) {
						if( player.getName().equalsIgnoreCase(packet.player)) {
							player.setPosition(packet.x, packet.y);
						}
					}
					connection.sendTCP(new PlayerMovementResponse(true));
					References.SERVER.sendToAllExceptTCP(connection.getID(), new PlayerMovementUpdate(packet.player, packet.x, packet.y));
				} else {
					connection.sendTCP(new PlayerMovementResponse(false));
				}
			}
		}
	}
	static public class ClientListener extends Listener {
		Logger logger = Logger.getLogger("ClientListener");		
		
		@Override
		public void received(Connection connection, Object object) {
			logger.setLevel(Level.OFF);
			if (object instanceof ChatPacket) {
				logger.info("Recieved a chat packet");
				References.CHATS.add((ChatPacket) object);
				if (References.CHATS.size() > 10) {
					References.CHATS.remove();
				}
			}
			if (object instanceof TextPacket) {
				logger.info("Recieved a text packet");
				References.TEXTS.add((TextPacket) object);
			}
			if (object instanceof LoginResponse) {
				logger.info("Recieved a login request. " + ((LoginResponse) object).response);
				if (((LoginResponse) object).response) {
					References.STATE = ConnectionState.CONNECTED;
				} else {
					References.STATE = ConnectionState.DISCONNECTED;
				}
			}
			if (object instanceof GameJoinPacket) {
				logger.info("Recieved a game join packet");
				References.STATE = ConnectionState.JOININGGAME;
				References.CHOSEN_GAME_STATE = GameState.values()[((GameJoinPacket) object).gameStateId];
				logger.info("Process game join packet.");
			}
			if (object instanceof GameStartPacket) {
				logger.info("Recieved a game start packet.");
				References.STATE = ConnectionState.INGAME;
				References.myCharacter.setPosition(((GameStartPacket)object).x,((GameStartPacket)object).y);
			}
			if (object instanceof PlayerInfoPacket) {
				logger.info("Recieved a player info packet");
				PlayerInfoPacket packet = (PlayerInfoPacket) object;
				References.myCharacter = packet.player;
			}
			if( object instanceof WholeMapPacket ) {
				logger.info("Recieved a map info packet");
				References.CLIENT_VIEW.setMap(((WholeMapPacket)object).dungeon);		
				References.PLAYER_LIST = ((WholeMapPacket)object).players;
			}
			if( object instanceof TileResponse ) {
				TileResponse tilePacket = (TileResponse)object;
				References.CLIENT_VIEW.dungeon[tilePacket.row][tilePacket.col].setTileLetter(tilePacket.tile.getTileLetter());
			}
			if( object instanceof PlayerMovementUpdate ) {				
				PlayerMovementUpdate packet = (PlayerMovementUpdate)object;
				logger.info("Recieved a player movement update packet " + packet.player + " moving to X:" + packet.x + ", Y:" + packet.y);
				for( NetworkPlayer player : References.PLAYER_LIST) {
					if( player.getName().equalsIgnoreCase(packet.player)) {
						player.setPosition(packet.x, packet.y);
					}
				}
			}
			if( object instanceof PlayerMovementResponse ) {
				PlayerMovementResponse packet = (PlayerMovementResponse)object;
				logger.info("Received a player movement response packet " + packet.response);
				if( packet.response ) {					
					if( References.myCharacter.getMovementList().size() > 1 ) {
						References.myCharacter.getMovementList().remove();
					} 
					References.myCharacter.setPosition( References.myCharacter.getProposedPlayerX(), References.myCharacter.getProposedPlayerY() );
					logger.info("Moved us to X:" + References.myCharacter.getPosition().x + " Y:" + References.myCharacter.getPosition().y);
				} else {					
					if( References.myCharacter.getMovementList().size() > 1 ) {
						Vector2f lastKnownGood = References.myCharacter.getMovementList().get(0);
						References.myCharacter.setPosition(lastKnownGood.x, lastKnownGood.y);
					} 
					int x = (int)References.myCharacter.getProposedPlayerX() / References.CLIENT_VIEW.getRatioRow();
					int y = (int)References.myCharacter.getProposedPlayerY() / References.CLIENT_VIEW.getRatioCol();
					logger.info("Sending tile request for " + x + ", " + y);
				}
				logger.info("Resetting pendingValidation");
				References.myCharacter.setPendingValidation( false );
			}
			if( object instanceof PlayerListPacket ) {
				logger.info("Recieved a player list packet.");
				PlayerListPacket packet = (PlayerListPacket)object;
				for( NetworkPlayer player : packet.players ) {
					if( player.getName().equalsIgnoreCase(References.ACCOUNT_NAME)) { continue; }
					References.PLAYER_LIST.add(player);
				}
			}
		}
	}
	
	//////////////////////////////////////
	// Traffic, packets, responses, etc //
	//////////////////////////////////////
	static abstract class Response {
		public boolean response;
	}
	static public class ChatPacket {
		public String text;

		public ChatPacket() {}
		public ChatPacket(String text) {
			if (text.length() > 50) { text = text.substring(0, 50); }
			this.text = text;
		}
	}
	static public class GameJoinPacket {
		public int gameStateId;

		public GameJoinPacket() {}
		public GameJoinPacket(int gameStateId) { this.gameStateId = gameStateId; }
	}
	static public class GameStartPacket {
		public int x, y;

		public GameStartPacket() {}
		public GameStartPacket(int startX, int startY) {
			x = startX;
			y = startY;
		}
	}
	static public class LoginRequest {
		public String ipAddress, account, password;

		public LoginRequest() { }
		public LoginRequest(String account, String password, String ip) {
			this.account = account;
			this.ipAddress = ip;
			this.password = password;
		}
	}
	static public class LoginResponse extends Response{
		public LoginResponse() { }
		public LoginResponse(boolean val) { this.response = val; }
	}
	static public class SignOff {
		public String ipAddress, account, password;

		public SignOff() {
		}

		public SignOff(String account, String password, String ip) {
			this.account = account;
			this.ipAddress = ip;
			this.password = password;
		}
	}
	static public class TextPacket {
		public String text;
		// Duration, 0 = indefinite
		public int x, y, durationInMilliseconds, passedTime = 0;

		public TextPacket() { }
		public TextPacket(String text, int xPos, int yPos, int durationInMilliseconds) {
			if (text.length() > 50) { text = text.substring(0, 50); }
			this.text = text;
			this.x = xPos;
			this.y = yPos;
			this.durationInMilliseconds = durationInMilliseconds;
		}
	}
	static public class PlayerInfoPacket {
		public NetworkPlayer player;
	}
	static public class PlayerListPacket {
		public ArrayList<NetworkPlayer> players;
		public PlayerListPacket(){ }
		public PlayerListPacket(ArrayList<NetworkPlayer> players){
			this.players = players;
		}
		
	}
	static public class PlayerMovementUpdate {
		public String player;
		public float x, y; 
		public PlayerMovementUpdate() {}
		public PlayerMovementUpdate(String name, float newX, float newY) {
			this.player = name;
			this.x = newX;
			this.y = newY;
		}
	}
	static public class PlayerMovementRequest {
		public String player;
		public float x, y; 
		public PlayerMovementRequest() {}
		public PlayerMovementRequest(String name, float newX, float newY) {
			this.player = name;
			this.x = newX;
			this.y = newY;
		}
	}
	static public class PlayerMovementResponse extends Response {
		public PlayerMovementResponse() {}
		public PlayerMovementResponse(boolean success) { 
			this.response = success;
		}
	}
	static public class TilesResponse {
		public List<GameSquare> tiles;
	}
	static public class TileResponse {
		public GameSquare tile;
		public int row, col;
		public TileResponse(){}
		public TileResponse(int row, int col, GameSquare tile){
			this.row = row;
			this.col = col;
			this.tile = tile;
		}
	}
	static public class TilesRequest {
		public int[] list;

		public TilesRequest(int... coords) {
			list = coords;
		}
	}
	static public class WholeMapPacket {
		public GameSquare[][] dungeon;
		public ArrayList<NetworkPlayer> players;
	}
}
