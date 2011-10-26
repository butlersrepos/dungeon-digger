package dungeonDigger.network;

import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.newdawn.slick.geom.Point;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.EndPoint;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;

import dungeonDigger.contentGeneration.GameSquare;
import dungeonDigger.gameFlow.DungeonDigger;
import dungeonDigger.gameFlow.MultiplayerDungeon;
import dungeonDigger.gameFlow.NetworkPlayer;

public class Network {
	static public final int port = 54555;
	//private Logger logger = Logger.getLogger("DungeonDigger.Network");
	
	// This registers objects that are going to be sent over the network.
	static public void register(EndPoint endPoint) {
		Kryo kryo = endPoint.getKryo();
		// TODO: LOGGER SETTINGS
		Log.set(Log.LEVEL_TRACE);
		kryo.register(TextPacket.class);
		kryo.register(ChatPacket.class);
		kryo.register(LoginRequest.class);
		kryo.register(LoginResponse.class);
		kryo.register(SignOff.class);
		kryo.register(GameStartPacket.class);
		kryo.register(GameJoinPacket.class);
		kryo.register(PlayerInfoPacket.class);
		kryo.register(PlayerListPacket.class);
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
			if( object instanceof LoginRequest ) {
				logger.info("Recieved login request");
				LoginRequest login = (LoginRequest)object;
				if( DungeonDigger.ACTIVESESSIONNAMES.contains(login.account) ) { 
					connection.sendTCP(new LoginResponse(false));
					return; 
				}
				
				connection.sendTCP(new LoginResponse(true));
				DungeonDigger.ACTIVESESSIONNAMES.add(login.account);
				PlayerInfoPacket info = new PlayerInfoPacket();
				if( DungeonDigger.CHARACTERBANK.get(login.account) != null ) {
					logger.info("Login was a pre-existing player: " + login.account);					
					info.player = DungeonDigger.CHARACTERBANK.get(login.account);
					connection.sendTCP(info);
				} else {
					logger.info("Login was a new player: " + login.account);
					NetworkPlayer player = new NetworkPlayer();
					player.setName(login.account);
					player.setHitPoints(20);
					player.setSpeed(3);
					player.setIconName("dwarf1.png");
					player.setPlayerXCoord(0);
					player.setPlayerYCoord(0);
					info.player = player;
					DungeonDigger.CHARACTERBANK.put(login.account, player);
					connection.sendTCP(info);
				}
				DungeonDigger.STATE = ConnectionState.SERVING;						
				logger.info("Sent text packet");
				connection.sendTCP(new TextPacket("Dungeon Lobby", 75, 75, 0));
			}
			if( object instanceof ChatPacket ) {
				logger.info("Recieved a chat packet.");
				DungeonDigger.CHATS.add((ChatPacket)object);
				logger.info("Sent ech of chat to all users.");
				if( connection.getEndPoint() instanceof Server ) {
					((Server)connection.getEndPoint()).sendToAllTCP(object);
				}
				if( DungeonDigger.CHATS.size() > 10 ) { DungeonDigger.CHATS.pop(); }
			}
			if( object instanceof TextPacket ) {
				logger.info("Recieved a text packet.");
				DungeonDigger.TEXTS.add( (TextPacket)object );
			}
			if( object instanceof SignOff ) {
				logger.info("Recieved a sign off packet.");
				DungeonDigger.ACTIVESESSIONNAMES.remove( ((SignOff)object).account );
				logger.info("Closing connection");
				connection.close();
			}
			if( object instanceof PlayerMovementRequest ) {
				PlayerMovementRequest packet = (PlayerMovementRequest)object;
				int x = packet.x / MultiplayerDungeon.CLIENT_VIEW.getRatioX();
				int y = packet.y / MultiplayerDungeon.CLIENT_VIEW.getRatioY();
				logger.info("Recieved a player movement request packet for " + packet.player + " to move to " + "X:" + packet.x + " Y:" + packet.y);
				boolean passable = MultiplayerDungeon.CLIENT_VIEW.dungeon[y][x].isPassable();
				logger.info("Position was passable: " + passable);
				if( passable ) {					
					for( NetworkPlayer player : MultiplayerDungeon.CLIENT_VIEW.playerList) {
						if( player.getName().equalsIgnoreCase(packet.player)) {
							player.setPlayerXCoord(packet.x);
							player.setPlayerYCoord(packet.y);
						}
					}
					connection.sendTCP(new PlayerMovementResponse(true));
					DungeonDigger.SERVER.sendToAllExceptTCP(connection.getID(), new PlayerMovementUpdate(packet.player, packet.x, packet.y));
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
				DungeonDigger.CHATS.add((ChatPacket) object);
				if (DungeonDigger.CHATS.size() > 10) {
					DungeonDigger.CHATS.pop();
				}
			}
			if (object instanceof TextPacket) {
				logger.info("Recieved a text packet");
				DungeonDigger.TEXTS.add((TextPacket) object);
			}
			if (object instanceof LoginResponse) {
				logger.info("Recieved a login request. " + ((LoginResponse) object).response);
				if (((LoginResponse) object).response) {
					DungeonDigger.STATE = ConnectionState.CONNECTED;
				} else {
					DungeonDigger.STATE = ConnectionState.DISCONNECTED;
				}
			}
			if (object instanceof GameJoinPacket) {
				logger.info("Recieved a game join packet");
				DungeonDigger.STATE = ConnectionState.JOININGGAME;
				DungeonDigger.CHOSEN_GAME_STATE = ((GameJoinPacket) object).gameStateId;
				logger.info("Process game join packet.");
			}
			if (object instanceof GameStartPacket) {
				logger.info("Recieved a game start packet.");
				DungeonDigger.STATE = ConnectionState.INGAME;
				DungeonDigger.myCharacter.setPlayerXCoord( ((GameStartPacket)object).x );
				DungeonDigger.myCharacter.setPlayerYCoord( ((GameStartPacket)object).y );
			}
			if (object instanceof PlayerInfoPacket) {
				logger.info("Recieved a player info packet");
				PlayerInfoPacket packet = (PlayerInfoPacket) object;
				DungeonDigger.myCharacter = packet.player;
			}
			if( object instanceof WholeMapPacket ) {
				logger.info("Recieved a map info packet");
				MultiplayerDungeon.CLIENT_VIEW.setMap(((WholeMapPacket)object).dungeon);		
				MultiplayerDungeon.CLIENT_VIEW.playerList = ((WholeMapPacket)object).players;
			}
			if( object instanceof TileResponse ) {
				TileResponse tilePacket = (TileResponse)object;
				MultiplayerDungeon.CLIENT_VIEW.dungeon[tilePacket.row][tilePacket.col].setTileLetter(tilePacket.tile.getTileLetter());
			}
			if( object instanceof PlayerMovementUpdate ) {				
				PlayerMovementUpdate packet = (PlayerMovementUpdate)object;
				logger.info("Recieved a player movement update packet " + packet.player + " moving to X:" + packet.x + ", Y:" + packet.y);
				for( NetworkPlayer player : MultiplayerDungeon.CLIENT_VIEW.playerList) {
					if( player.getName().equalsIgnoreCase(packet.player)) {
						player.setPlayerXCoord(packet.x);
						player.setPlayerYCoord(packet.y);
					}
				}
			}
			if( object instanceof PlayerMovementResponse ) {
				PlayerMovementResponse packet = (PlayerMovementResponse)object;
				logger.info("Received a player movement response packet " + packet.response);
				if( packet.response ) {					
					if( DungeonDigger.myCharacter.getMovementList().size() > 1 ) {
						DungeonDigger.myCharacter.getMovementList().remove();
					} 
					DungeonDigger.myCharacter.setPlayerXCoord( DungeonDigger.myCharacter.getProposedPlayerX() );
					DungeonDigger.myCharacter.setPlayerYCoord( DungeonDigger.myCharacter.getProposedPlayerY() );
					logger.info("Moved us to X:" + DungeonDigger.myCharacter.getPlayerXCoord() + " Y:" + DungeonDigger.myCharacter.getPlayerYCoord());
				} else {					
					if( DungeonDigger.myCharacter.getMovementList().size() > 1 ) {
						Point lastKnownGood = DungeonDigger.myCharacter.getMovementList().get(0);
						DungeonDigger.myCharacter.setPlayerXCoord((int)lastKnownGood.getX());
						DungeonDigger.myCharacter.setPlayerYCoord((int)lastKnownGood.getY());
					} 
					int x = DungeonDigger.myCharacter.getProposedPlayerX() / MultiplayerDungeon.CLIENT_VIEW.getRatioX();
					int y = DungeonDigger.myCharacter.getProposedPlayerY() / MultiplayerDungeon.CLIENT_VIEW.getRatioY();
					logger.info("Sending tile request for " + x + ", " + y);
				}
				logger.info("Resetting pendingValidation");
				DungeonDigger.myCharacter.setPendingValidation( false );
			}
			if( object instanceof PlayerListPacket ) {
				logger.info("Recieved a player list packet.");
				PlayerListPacket packet = (PlayerListPacket)object;
				for( NetworkPlayer player : packet.players ) {
					if( player.getName().equalsIgnoreCase(DungeonDigger.ACCOUNT_NAME)) { continue; }
					MultiplayerDungeon.CLIENT_VIEW.playerList.add(player);
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
		public Vector<NetworkPlayer> players;
		public PlayerListPacket(){ }
		public PlayerListPacket(Vector<NetworkPlayer> players){
			this.players = players;
		}
		
	}
	static public class PlayerMovementUpdate {
		public String player;
		public int x, y; 
		public PlayerMovementUpdate() {}
		public PlayerMovementUpdate(String name, int newX, int newY) {
			this.player = name;
			this.x = newX;
			this.y = newY;
		}
	}
	static public class PlayerMovementRequest {
		public String player;
		public int x, y; 
		public PlayerMovementRequest() {}
		public PlayerMovementRequest(String name, int newX, int newY) {
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
		public Vector<NetworkPlayer> players;
	}
}
