package dungeonDigger.gameFlow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.logging.Logger;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Animation;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.util.ResourceLoader;

import dungeonDigger.Enums.AbilityDeliveryMethod;
import dungeonDigger.Enums.GameState;
import dungeonDigger.Tools.References;
import dungeonDigger.entities.Ability;
import dungeonDigger.entities.Mob;
import dungeonDigger.entities.NetworkPlayer;
import dungeonDigger.network.ConnectionState;

/**
 * Initial game state that loads most assets and maintains references to universal objects
 * @author Eric
 */
public class DungeonDigger extends StateBasedGame {
	
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
		
		References.STATE = ConnectionState.IDLE;
		this.enterState(GameState.MAIN_MENU.ordinal());
	}
	
	public static void importSettings() {
		prepDirectories();
		loadImages();
		loadCharacterFiles();
		loadAbilities();
		loadSettings();	
		loadMobs();
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
			References.IMAGES.put("dwarf1", new Image( "dwarf1.png", new Color(255, 0, 255)));
			References.IMAGES.put("engy", new Image( "engy.png", new Color(255, 0, 255)));
			References.IMAGES.put("roomWallImage", new Image( ResourceLoader.getResourceAsStream("dirt floor 100x120.png"), "dirt floor 100x120.png", false));
			References.IMAGES.put("dirtFloorImage",  new Image( ResourceLoader.getResourceAsStream("Dirt Block.png"), "Dirt Block.png", false));
			References.IMAGES.put("roomFloorImage",  new Image( ResourceLoader.getResourceAsStream("dirt floor 100x120.png"), "dirt floor 100x120.png", false));
			References.IMAGES.put("dirtWallImage", new Image( ResourceLoader.getResourceAsStream("Wall Block Tall.png"), "Wall Block Tall.png", false));
			References.IMAGES.put("entranceImage", new Image( ResourceLoader.getResourceAsStream("Stone Block.png"), "Stone Block.png", false));			
			References.IMAGES.put("magicReticle", new Image("magic_reticle.png", new Color(255, 0, 255)));			
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
							if( References.CHARACTERBANK.get(property) != null ) {
								Logger.getAnonymousLogger().info("Duplicant character found: " + loadee.getName());
								duplicant = true;
								break;
							}
						}
						if( property.toString().equalsIgnoreCase("XCOORD") ) { loadee.getPosition().x = Integer.valueOf(line.substring(line.indexOf("]")+1)); }
						if( property.toString().equalsIgnoreCase("YCOORD") ) { loadee.getPosition().y = Integer.valueOf(line.substring(line.indexOf("]")+1)); }
						if( property.toString().equalsIgnoreCase("MAXHITPOINTS") ) { loadee.setHitPoints( Integer.valueOf(line.substring(line.indexOf("]")+1))); }
						if( property.toString().equalsIgnoreCase("SPEED") ) { loadee.setSpeed( Integer.valueOf(line.substring(line.indexOf("]")+1))); }
						if( property.toString().equalsIgnoreCase("AVATAR") ) { loadee.setIconName( line.substring(line.indexOf("]")+1)); }
						property.setLength(0);
					}
					
					if( !duplicant ) {
						References.CHARACTERBANK.put(loadee.getName(), loadee);
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
		References.KEY_BINDINGS.put(Keyboard.KEY_W, 		"moveUp");
		References.KEY_BINDINGS.put(Keyboard.KEY_S, 		"moveDown");
		References.KEY_BINDINGS.put(Keyboard.KEY_A, 		"moveLeft");
		References.KEY_BINDINGS.put(Keyboard.KEY_D, 		"moveRight");
		References.KEY_BINDINGS.put(Keyboard.KEY_1, 		"slot1");
		References.KEY_BINDINGS.put(Keyboard.KEY_2, 		"slot2");
		References.KEY_BINDINGS.put(Keyboard.KEY_3, 		"slot3");
		References.KEY_BINDINGS.put(Keyboard.KEY_4, 		"slot4");
		References.KEY_BINDINGS.put(Keyboard.KEY_GRAVE, 	"slot5");
		References.KEY_BINDINGS.put(Keyboard.KEY_Q, 		"slot6");
		References.KEY_BINDINGS.put(Keyboard.KEY_E, 		"slot7");
		References.KEY_BINDINGS.put(Keyboard.KEY_R, 		"slot8");
		References.KEY_BINDINGS.put(Keyboard.KEY_F, 		"slot9");
		References.KEY_BINDINGS.put(Keyboard.KEY_Z, 		"slot10");
		References.KEY_BINDINGS.put(Keyboard.KEY_X, 		"slot11");
		References.KEY_BINDINGS.put(Keyboard.KEY_C, 		"slot12");
		References.KEY_BINDINGS.put(Keyboard.KEY_V, 		"slot13");
		References.KEY_BINDINGS.put(Keyboard.KEY_SPACE, 	"slot14");
		
		String str;
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("data/config.ini")));
			while( (str = br.readLine()) != null ) {
				int separator = str.indexOf("=");
				int key = Keyboard.getKeyIndex(str.substring(0, separator));
				String binding = str.substring(separator + 1);
				
				References.KEY_BINDINGS.put(key, binding);				
			}
		} catch( FileNotFoundException e ) {
			Logger.getAnonymousLogger().info("No config ini file found!  Using default settings.");
		} catch( IOException e ) {
			Logger.getAnonymousLogger().info("Problem with config ini file!  Using default settings.");
		}
		
		// Setup standard/empty hotbars
		References.SLOT_BINDINGS.put("slot1",	"fireball");
		References.SLOT_BINDINGS.put("slot2",	"empty");
		References.SLOT_BINDINGS.put("slot3",	"empty");
		References.SLOT_BINDINGS.put("slot4",	"empty");
		References.SLOT_BINDINGS.put("slot5",	"empty");
		References.SLOT_BINDINGS.put("slot6",	"empty");
		References.SLOT_BINDINGS.put("slot7",	"empty");
		References.SLOT_BINDINGS.put("slot8",	"empty");
		References.SLOT_BINDINGS.put("slot9",	"empty");
		References.SLOT_BINDINGS.put("slot10",	"empty");
		References.SLOT_BINDINGS.put("slot11",	"empty");
		References.SLOT_BINDINGS.put("slot12",	"empty");
		References.SLOT_BINDINGS.put("slot13",	"empty");
		References.SLOT_BINDINGS.put("slot14",	"empty");
	}
	
	public static void loadAbilities() {
		BufferedReader in;
		File file = new File("data/abilities");
		Ability templater = null;
		
		if( !file.isDirectory() ) { 
			file.mkdir();
			System.out.println("NO ABILITY FILES FOUND! PLEASE REINSTALL.");
			System.exit(-1);
		} else {
			FilenameFilter abilFilesOnly = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					if( name.endsWith(".adf") ) { return true; }
					return false;
				}				
			};
			// Try to load each ability file
			for( File f : file.listFiles( abilFilesOnly ) ){
				try {
					in = new BufferedReader(new FileReader(f));
					String line = in.readLine();
					String str1, str2, str3;
					float x, y;
					int x1=0, x2=0, y1=0, y2=0;
					StringBuffer property = new StringBuffer();
					boolean duplicant = false;
					
					if( !line.equalsIgnoreCase("[ABILITY]") ) {
						Logger.getAnonymousLogger().info("Ability file: " + f.getName() + " seems corrupt. Skipping.");
						continue;
					}
					
					// Setup ability object
					while( (line = in.readLine()) != null ) {
						property.append(line.substring(1, line.indexOf("]")));
						if( property.toString().equalsIgnoreCase("NAME") ) { 
							templater = new Ability(line.substring(line.indexOf("]")+1));
							if( References.ABILITY_TEMPLATES.get(templater.getName()) != null ) {
								Logger.getAnonymousLogger().info("Duplicant ability template found: " + templater.getName());
								duplicant = true;
								break;
							}
						}
						if( property.toString().equalsIgnoreCase("SPRITESHEET") ) { 
							str1 = line.substring(line.indexOf("]")+1);
							str2 = str1.substring( str1.lastIndexOf('_')+1, str1.lastIndexOf('x'));
							str3 = str1.substring(str1.lastIndexOf('x')+1, str1.lastIndexOf('.'));
							templater.setSpriteSheet( new SpriteSheet(new Image(str1, Color.magenta), Integer.parseInt(str2), Integer.parseInt(str3))); 
						}
						if( property.toString().equalsIgnoreCase("ANIMSTARTX") ) { x1 = Integer.valueOf(line.substring(line.indexOf(']')+1)); }
						if( property.toString().equalsIgnoreCase("ANIMSTARTY") ) { y1 = Integer.valueOf(line.substring(line.indexOf(']')+1)); }
						if( property.toString().equalsIgnoreCase("ANIMENDX") ) { x2 = Integer.valueOf(line.substring(line.indexOf(']')+1)); }
						if( property.toString().equalsIgnoreCase("ANIMENDY") ) { y2 = Integer.valueOf(line.substring(line.indexOf(']')+1)); }
						if( property.toString().equalsIgnoreCase("DAMAGING") ) { templater.setDamaging( Boolean.valueOf(line.substring(line.indexOf("]")+1))); }
						if( property.toString().equalsIgnoreCase("HITFRAMES") ) { 
							str1 = line.substring(line.indexOf("]")+1);
							str2 = str1.substring( str1.lastIndexOf('_')+1, str1.lastIndexOf('x'));
							str3 = str1.substring(str1.lastIndexOf('x')+1, str1.lastIndexOf('.'));
							templater.setHitFrames( new SpriteSheet(new Image(str1, Color.magenta), Integer.parseInt(str2), Integer.parseInt(str3))); 
						}
						if( property.toString().equalsIgnoreCase("SPEED") ) { templater.setSpeed( Integer.valueOf(line.substring(line.indexOf("]")+1))); }
						if( property.toString().equalsIgnoreCase("FRIENDLY") ) { templater.setFriendly( Boolean.valueOf(line.substring(line.indexOf("]")+1))); }
						if( property.toString().equalsIgnoreCase("MOUSE") ) { templater.setMouse( Boolean.valueOf(line.substring(line.indexOf("]")+1))); }
						if( property.toString().equalsIgnoreCase("START") ) { 
							if( !line.substring(line.indexOf(']')+1, line.indexOf(',')).equalsIgnoreCase("M") ) {
								x = Float.valueOf(line.substring(line.indexOf(']')+1, line.indexOf(',')));
								y = Float.valueOf(line.substring(line.indexOf(',')+1));
								templater.setStartPoint(x, y);
							}
						}
						if( property.toString().equalsIgnoreCase("MIDDLE") ) { 
							x = Float.valueOf(line.substring(line.indexOf(']')+1, line.indexOf(',')));
							y = Float.valueOf(line.substring(line.indexOf(',')+1));
							templater.setMiddlePoint(x, y); 
						}
						if( property.toString().equalsIgnoreCase("END") ) { 
							x = Float.valueOf(line.substring(line.indexOf(']')+1, line.indexOf(',')));
							y = Float.valueOf(line.substring(line.indexOf(',')+1));
							templater.setEndPoint(x, y); 
						}
						if( property.toString().equalsIgnoreCase("DELIVERY") ) { 
							String adm =line.substring(line.indexOf(']')+1);
							templater.setDeliveryMethod(AbilityDeliveryMethod.valueOf(adm));
						}
						property.setLength(0);
					}
					// Create animation from info
					templater.setAnimation(new Animation(templater.getSpriteSheet(), x1, y1, x2, y2, true, 100, false));
						
					
					if( !duplicant ) {
						References.ABILITY_TEMPLATES.put(templater.getName(), templater);
						Logger.getAnonymousLogger().info("Archived ability template: " + templater.getName());
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch( NumberFormatException e ) {
					e.printStackTrace();
				} catch( SlickException e ) {
					e.printStackTrace();
				}
			}
		}
		References.ABILITY_FACTORY.init();
	}
	
	public static void loadMobs() {
		BufferedReader in;
		File file = new File("data/mobs");
		Mob templater = null;
		
		if( !file.isDirectory() ) { 
			file.mkdir();
			System.out.println("NO MOB FILES FOUND! PLEASE REINSTALL.");
			System.exit(-1);
		} else {
			FilenameFilter mobFilesOnly = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					if( name.endsWith(".mdf") ) { return true; }
					return false;
				}				
			};
			// Try to load each mob file
			for( File f : file.listFiles( mobFilesOnly ) ){
				try {
					in = new BufferedReader(new FileReader(f));
					String line = in.readLine();
					String str1, str2, str3;
					int x1=0, x2=0, y1=0, y2=0;
					StringBuffer property = new StringBuffer();
					boolean duplicant = false;
					
					if( !line.equalsIgnoreCase("[MOB]") ) {
						Logger.getAnonymousLogger().info("Mob file: " + f.getName() + " seems corrupt. Skipping.");
						continue;
					}
					
					// Setup mob object
					while( (line = in.readLine()) != null ) {
						property.append(line.substring(1, line.indexOf("]")));
						if( property.toString().equalsIgnoreCase("NAME") ) { 
							templater = new Mob(line.substring(line.indexOf("]")+1));
							if( References.MOB_TEMPLATES.get(templater.getName()) != null ) {
								Logger.getAnonymousLogger().info("Duplicant Mob template found: " + templater.getName());
								duplicant = true;
								break;
							}
						}
						if( property.toString().equalsIgnoreCase("SPRITESHEET") ) { 
							str1 = line.substring(line.indexOf("]")+1);
							str2 = str1.substring( str1.lastIndexOf('_')+1, str1.lastIndexOf('x'));
							str3 = str1.substring(str1.lastIndexOf('x')+1, str1.lastIndexOf('.'));
							templater.setSprites( new SpriteSheet(new Image(str1, Color.magenta), Integer.parseInt(str2), Integer.parseInt(str3))); 
						}
						if( property.toString().equalsIgnoreCase("ANIMSTARTX") ) { x1 = Integer.valueOf(line.substring(line.indexOf(']')+1)); }
						if( property.toString().equalsIgnoreCase("ANIMSTARTY") ) { y1 = Integer.valueOf(line.substring(line.indexOf(']')+1)); }
						if( property.toString().equalsIgnoreCase("ANIMENDX") ) { x2 = Integer.valueOf(line.substring(line.indexOf(']')+1)); }
						if( property.toString().equalsIgnoreCase("ANIMENDY") ) { y2 = Integer.valueOf(line.substring(line.indexOf(']')+1)); }
						if( property.toString().equalsIgnoreCase("SPEED") ) { templater.setSpeed( Integer.valueOf(line.substring(line.indexOf("]")+1))); }
						if( property.toString().equalsIgnoreCase("FRIENDLY") ) { templater.setFriendly( Boolean.valueOf(line.substring(line.indexOf("]")+1))); }
						if( property.toString().equalsIgnoreCase("HITPOINTS") ) { 
							templater.setMaxHitPoints( Integer.valueOf(line.substring(line.indexOf("]")+1))); 
							templater.setCurrentHitPoints( Integer.valueOf(line.substring(line.indexOf("]")+1))); 
						}
						property.setLength(0);
					}
					// Create animation from info
					templater.setAnimation(new Animation(templater.getSprites(), x1, y1, x2, y2, true, 100, false));
						
					
					if( !duplicant ) {
						References.MOB_TEMPLATES.put(templater.getName(), templater);
						Logger.getAnonymousLogger().info("Archived mob template: " + templater.getName());
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch( NumberFormatException e ) {
					e.printStackTrace();
				} catch( SlickException e ) {
					e.printStackTrace();
				}
			}
		}
		References.MOB_FACTORY.init();
	}
}