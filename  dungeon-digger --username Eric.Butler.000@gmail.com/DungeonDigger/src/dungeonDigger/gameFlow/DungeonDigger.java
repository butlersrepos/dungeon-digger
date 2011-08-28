package dungeonDigger.gameFlow;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Server;

import dungeonDigger.network.ConnectionState;
import dungeonDigger.network.Network.ChatPacket;
import dungeonDigger.network.Network.TextPacket;

public class DungeonDigger extends StateBasedGame {
	public static NetworkPlayer myCharacter;
	public static Server SERVER = new Server();
	public static Client CLIENT = new Client();	
	public static int CHOSEN_GAME_STATE;
	public static final int MAINMENU = 0;
	public static final int LOBBY = 3;
	public static final int SINGLEPLAYERDUNGEON = 1;
	public static final int MULTIPLAYERDUNGEON = 2;
	public static String ACCOUNT_NAME;
	public static int MAX_MESSAGE_LENGTH = 50;
	public static ConnectionState STATE;
	public static HashMap<String, Image> IMAGES = new HashMap<String, Image>();
	public static ArrayList<TextPacket> TEXTS = new ArrayList<TextPacket>();
	public static LinkedList<ChatPacket> CHATS = new LinkedList<ChatPacket>();
	public static HashMap<String, NetworkPlayer> CHARACTERBANK = new HashMap<String, NetworkPlayer>();
	public static HashSet<String> ACTIVESESSIONNAMES = new HashSet<String>();
	
	public DungeonDigger(String title) {
		super(title);
	}

	// Start game
	public static void main(String[] args) {
		try {
			AppGameContainer app = new AppGameContainer(new DungeonDigger("Dungeon Digger"));
			app.setDisplayMode(640, 640, false);
			app.setTargetFrameRate(40);
			app.setUpdateOnlyWhenVisible(false);
			app.setAlwaysRender(true);
			app.start();
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void initStatesList(GameContainer container) throws SlickException {
		this.addState(new MainMenu());
		this.addState(new SinglePlayerDungeon());
		this.addState(new MultiplayerDungeon());
		this.addState(new Lobby());
		
		STATE = ConnectionState.IDLE;
		this.enterState(DungeonDigger.MAINMENU);
	}
}