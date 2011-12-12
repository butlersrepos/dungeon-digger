package dungeonDigger.gameFlow;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.logging.Logger;

import org.lwjgl.LWJGLUtil;
import org.lwjgl.input.Keyboard;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.util.ResourceLoader;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Server;

import dungeonDigger.entities.NetworkPlayer;
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
	public static String IP_CONNECT;
	public static int MAX_MESSAGE_LENGTH = 50;
	public static ConnectionState STATE;
	public static HashMap<String, Image> IMAGES = new HashMap<String, Image>();
	public static ArrayList<TextPacket> TEXTS = new ArrayList<TextPacket>();
	public static LinkedList<ChatPacket> CHATS = new LinkedList<ChatPacket>();
	public static HashMap<String, NetworkPlayer> CHARACTERBANK = new HashMap<String, NetworkPlayer>();
	public static HashSet<String> ACTIVESESSIONNAMES = new HashSet<String>();
	public static HashMap<String, Integer> KEY_BINDINGS = new HashMap<String, Integer>();
	
	public DungeonDigger(String title) {
		super(title);
	}

	// Start game
	public static void main(String[] args) {
		try {
			System.setProperty("org.lwjgl.librarypath", new File(System.getProperty("user.dir"), "natives").getAbsolutePath());
			System.setProperty("net.java.games.input.librarypath", System.getProperty("org.lwjgl.librarypath"));
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
		importSettings();
		
		this.addState(new MainMenu());
		this.addState(new SinglePlayerDungeon());
		this.addState(new MultiplayerDungeon());
		this.addState(new Lobby());
		
		STATE = ConnectionState.IDLE;
		this.enterState(DungeonDigger.MAINMENU);
	}
	
	public static void importSettings() {
		prepDirectories();
		loadImages();
		loadCharacterFiles();
		loadSettings();		
	}
	
	public static void prepDirectories() {
		File file = new File("data");		
		if( !file.isDirectory() ) { file.mkdir(); }
		
		file = new File("data/characters");		
		if( !file.isDirectory() ) { file.mkdir(); }
		
		file = new File("data/maps");		
		if( !file.isDirectory() ) { file.mkdir(); }
	}
	
	public static void loadImages() {
		try {
			DungeonDigger.IMAGES.put("dwarf1", new Image( "dwarf1.png", new Color(255, 0, 255)));
			DungeonDigger.IMAGES.put("roomWallImage", new Image( ResourceLoader.getResourceAsStream("dirt floor 100x120.png"), "dirt floor 100x120.png", false));
			DungeonDigger.IMAGES.put("dirtFloorImage",  new Image( ResourceLoader.getResourceAsStream("Dirt Block.png"), "Dirt Block.png", false));
			DungeonDigger.IMAGES.put("roomFloorImage",  new Image( ResourceLoader.getResourceAsStream("dirt floor 100x120.png"), "dirt floor 100x120.png", false));
			DungeonDigger.IMAGES.put("dirtWallImage", new Image( ResourceLoader.getResourceAsStream("Wall Block Tall.png"), "Wall Block Tall.png", false));
			DungeonDigger.IMAGES.put("entranceImage", new Image( ResourceLoader.getResourceAsStream("Stone Block.png"), "Stone Block.png", false));			
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}
	
	/*Load all .csf files into memory for players' characters */
	public static void loadCharacterFiles() {
		BufferedReader in;
		File file = new File("data/characters");
		
		if( !file.isDirectory() ) { file.mkdir(); }
		else {
			// Create filter to ignore all but csf files
			FilenameFilter charFilesOnly = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					if( name.endsWith(".csf") ) { return true; }
					return false;
				}				
			};
			// Try to load each player file
			for( File f : file.listFiles( charFilesOnly ) ){
				try {
					in = new BufferedReader(new FileReader(f));
					NetworkPlayer loadee = new NetworkPlayer();
					String line = in.readLine();
					boolean duplicant = false;
					
					if( !line.equalsIgnoreCase("[CHARACTER]") ) {
						Logger.getAnonymousLogger().info("Character file: " + f.getName() + " seems corrupt. Skipping.");
						continue;
					}
					
					// Setup player object
					StringBuffer property = new StringBuffer();
					while( (line = in.readLine()) != null ) {
						property.append(line.substring(1, line.indexOf("]")));
						if( property.toString().equalsIgnoreCase("NAME") ) { 
							loadee.setName( line.substring(line.indexOf("]")+1));
							if( DungeonDigger.CHARACTERBANK.get(property) != null ) {
								Logger.getAnonymousLogger().info("Duplicant character found: " + loadee.getName());
								duplicant = true;
								break;
							}
						}
						if( property.toString().equalsIgnoreCase("XCOORD") ) { loadee.setPlayerXCoord( Integer.valueOf(line.substring(line.indexOf("]")+1))); }
						if( property.toString().equalsIgnoreCase("YCOORD") ) { loadee.setPlayerYCoord( Integer.valueOf(line.substring(line.indexOf("]")+1))); }
						if( property.toString().equalsIgnoreCase("MAXHITPOINTS") ) { loadee.setHitPoints( Integer.valueOf(line.substring(line.indexOf("]")+1))); }
						if( property.toString().equalsIgnoreCase("SPEED") ) { loadee.setSpeed( Integer.valueOf(line.substring(line.indexOf("]")+1))); }
						if( property.toString().equalsIgnoreCase("AVATAR") ) { loadee.setIconName( line.substring(line.indexOf("]")+1)); }
						property.setLength(0);
					}
					
					if( !duplicant ) {
						DungeonDigger.CHARACTERBANK.put(loadee.getName(), loadee);
						Logger.getAnonymousLogger().info("Archived character: " + loadee.getName());
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void loadSettings() {
		// Setup standard keyBindings
		KEY_BINDINGS.put("moveUp", Keyboard.KEY_W);
		KEY_BINDINGS.put("moveDown", Keyboard.KEY_S);
		KEY_BINDINGS.put("moveLeft", Keyboard.KEY_A);
		KEY_BINDINGS.put("moveRight", Keyboard.KEY_D);
		
		String str;
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("data/config.ini")));
			while( (str = br.readLine()) != null ) {
				int separator = str.indexOf("=");
				String binding = str.substring(0, separator);
				int key = Keyboard.getKeyIndex(str.substring(separator + 1));
				
				KEY_BINDINGS.put(binding, key);				
			}
		} catch( FileNotFoundException e ) {
			Logger.getAnonymousLogger().info("No config ini file found!  Using default settings.");
		} catch( IOException e ) {
			Logger.getAnonymousLogger().info("Problem with config ini file!  Using default settings.");
		}		
	}
}