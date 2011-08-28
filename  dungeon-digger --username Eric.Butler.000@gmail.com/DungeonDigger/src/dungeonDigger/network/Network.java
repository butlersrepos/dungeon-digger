package dungeonDigger.network;

import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.EndPoint;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import dungeonDigger.contentGeneration.GameSquare;
import dungeonDigger.gameFlow.DungeonDigger;
import dungeonDigger.gameFlow.Lobby;
import dungeonDigger.gameFlow.MultiplayerDungeon;
import dungeonDigger.gameFlow.NetworkPlayer;
import dungeonDigger.gameFlow.Player;

public class Network {
	static public final int port = 54555;
	private Logger logger = Logger.getLogger("DungeonDigger.Network");

	// This registers objects that are going to be sent over the network.
	static public void register(EndPoint endPoint) {
		Kryo kryo = endPoint.getKryo();

		kryo.register(TextPacket.class);
		kryo.register(ChatPacket.class);
		kryo.register(LoginRequest.class);
		kryo.register(LoginResponse.class);
		kryo.register(SignOff.class);
		kryo.register(GameStartPacket.class);
		kryo.register(GameJoinPacket.class);
		kryo.register(PlayerInfoPacket.class);
		kryo.register(NetworkPlayer.class);
		kryo.register(TilesResponse.class);
		kryo.register(TilesRequest.class);
		kryo.register(GameSquare.class);
		kryo.register(WholeMapPacket.class);
		kryo.register(Vector.class);
		kryo.register(PlayerMovementUpdatePacket.class);
	}

	static public class ChatPacket {
		public String text;

		public ChatPacket() {
		}

		public ChatPacket(String text) {
			if (text.length() > 50) {
				text = text.substring(0, 50);
			}
			this.text = text;
		}
	}
	static public class GameJoinPacket {
		public int gameStateId;

		public GameJoinPacket() {
		}

		public GameJoinPacket(int gameStateId) {
			this.gameStateId = gameStateId;
		}
	}
	static public class GameStartPacket {
		public int x, y;

		public GameStartPacket() {
		}

		public GameStartPacket(int startX, int startY) {
			x = startX;
			y = startY;
		}
	}
	static public class LoginRequest {
		public String ipAddress, account, password;

		public LoginRequest() {
		}

		public LoginRequest(String account, String password, String ip) {
			this.account = account;
			this.ipAddress = ip;
			this.password = password;
		}
	}
	static public class LoginResponse {
		public boolean success;

		public LoginResponse() {
		}

		public LoginResponse(boolean val) {
			this.success = val;
		}
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

		public TextPacket() {
		}

		public TextPacket(String text, int xPos, int yPos,
				int durationInMilliseconds) {
			if (text.length() > 50) {
				text = text.substring(0, 50);
			}
			this.text = text;
			this.x = xPos;
			this.y = yPos;
			this.durationInMilliseconds = durationInMilliseconds;
		}
	}
	static public class PlayerInfoPacket {
		public NetworkPlayer player;
	}
	static public class PlayerMovementUpdatePacket {
		public String player;
		public int x, y; 
		public boolean left;
		public PlayerMovementUpdatePacket() {}
		public PlayerMovementUpdatePacket(String name, int newX, int newY, boolean facingLeft) {
			this.player = name;
			this.x = newX;
			this.y = newY;
			this.left = facingLeft;
		}
	}
	static public class TilesResponse {
		public List<GameSquare> tiles;

	}
	static public class TilesRequest {
		public String list;

		public TilesRequest(int... coords) {
			for (int x : coords) {
				list += String.valueOf(x) + "-";
			}
		}
	}
	static public class WholeMapPacket {
		public GameSquare[][] dungeon;
		public Vector<NetworkPlayer> players;
	}
	//////////////////////////////////
	// Listeners that the game uses //
	//////////////////////////////////
	static public class ServerListener extends Listener {
		Logger logger = Logger.getLogger("ServerListener");
		
		@Override
		public void received(Connection connection, Object object) {
			if( object instanceof LoginRequest ) {
				logger.info("Recieved login request");
				LoginRequest login = (LoginRequest)object;
				if( DungeonDigger.ACTIVESESSIONNAMES.contains(login.account) ) { 
					connection.sendTCP(new LoginResponse(false));
					return; 
				}
				
				connection.sendTCP(new LoginResponse(true));
				DungeonDigger.ACTIVESESSIONNAMES.add(login.account);
				if( DungeonDigger.CHARACTERBANK.get(login.account) != null ) {
					logger.info("Login was a pre-existing player: " + login.account);
					PlayerInfoPacket info = new PlayerInfoPacket();
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
					DungeonDigger.CHARACTERBANK.put(login.account, player);
					connection.sendTCP(player);
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
					((Server)connection.getEndPoint()).sendToAllTCP((ChatPacket)object);
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
				connection.close();
			}
			if( object instanceof PlayerMovementUpdatePacket ) {
				PlayerMovementUpdatePacket packet = (PlayerMovementUpdatePacket)object;
				for( NetworkPlayer player : MultiplayerDungeon.CLIENT_VIEW.playerList) {
					if( player.getName().equalsIgnoreCase(packet.player)) {
						player.setFlippedLeft(packet.left);
						player.setPlayerXCoord(packet.x);
						player.setPlayerYCoord(packet.y);
					}
				}
			}
		}
	}
	static public class ClientListener extends Listener {
		Logger logger = Logger.getLogger("ClientListener");
		
		@Override
		public void received(Connection connection, Object object) {
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
				logger.info("Recieved a login request.");
				if (((LoginResponse) object).success) {
					DungeonDigger.STATE = ConnectionState.CONNECTED;
				} else {
					DungeonDigger.STATE = ConnectionState.DISCONNECTED;
				}
			}
			if (object instanceof GameJoinPacket) {
				logger.info("Recieved a game join packet");
				DungeonDigger.STATE = ConnectionState.JOININGGAME;
				DungeonDigger.CHOSEN_GAME_STATE = ((GameJoinPacket) object).gameStateId;
			}
			if (object instanceof GameStartPacket) {
				logger.info("Recieved a game start packet.");
				DungeonDigger.STATE = ConnectionState.INGAME;
			}
			if (object instanceof PlayerInfoPacket) {
				logger.info("Recieved a player info packet");
				PlayerInfoPacket packet = (PlayerInfoPacket) object;
				DungeonDigger.myCharacter = packet.player;
			}
			if( object instanceof WholeMapPacket ) {
				MultiplayerDungeon.CLIENT_VIEW.setMap(((WholeMapPacket)object).dungeon);		
				MultiplayerDungeon.CLIENT_VIEW.playerList = ((WholeMapPacket)object).players;
			}
			if( object instanceof PlayerMovementUpdatePacket ) {
				PlayerMovementUpdatePacket packet = (PlayerMovementUpdatePacket)object;
				for( NetworkPlayer player : MultiplayerDungeon.CLIENT_VIEW.playerList) {
					if( player.getName().equalsIgnoreCase(packet.player)) {
						player.setFlippedLeft(packet.left);
						player.setPlayerXCoord(packet.x);
						player.setPlayerYCoord(packet.y);
					}
				}
			}
		}
	}
}
