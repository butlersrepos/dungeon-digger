package dungeonDigger.gameFlow;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.MouseListener;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.gui.AbstractComponent;
import org.newdawn.slick.gui.ComponentListener;
import org.newdawn.slick.gui.TextField;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import dungeonDigger.network.ConnectionState;
import dungeonDigger.network.Network;
import dungeonDigger.network.Network.ChatPacket;
import dungeonDigger.network.Network.GameJoinPacket;
import dungeonDigger.network.Network.LoginRequest;
import dungeonDigger.network.Network.SignOff;
import dungeonDigger.network.Network.TextPacket;

public class Lobby extends BasicGameState implements MouseListener{		
	private boolean isServer;
	private Logger logger = Logger.getLogger("DungeonDigger.Lobby");
		
	private TextField inputBox, startButton;
	
	@Override
	public int getID() { return 3; }

	@Override
	public void init(GameContainer container, StateBasedGame game) throws SlickException {}

	@Override
	public void enter(GameContainer container, StateBasedGame game){		
		if( DungeonDigger.STATE == ConnectionState.LISTENING ) {
			isServer = true;
			Network.register(DungeonDigger.SERVER);
			loadCharacterFiles();
			// Load our character up
			if( DungeonDigger.CHARACTERBANK.get(DungeonDigger.ACCOUNT_NAME) == null ) {
				NetworkPlayer player = new NetworkPlayer();
				player.setName(DungeonDigger.ACCOUNT_NAME);
				player.setHitPoints(20);
				player.setSpeed(3);
				player.setIconName("dwarf1.png");
				player.setPlayerXCoord(0);
				player.setPlayerYCoord(0);
				DungeonDigger.CHARACTERBANK.put(DungeonDigger.ACCOUNT_NAME, player);
			}
			DungeonDigger.myCharacter = DungeonDigger.CHARACTERBANK.get(DungeonDigger.ACCOUNT_NAME);
			startAsServer();
		}
		if( DungeonDigger.STATE == ConnectionState.CONNECTING ) {
			Network.register(DungeonDigger.CLIENT);
			startAsClient();
		}
		
		// Create our chat box
		Font awtFont = new Font("Times New Roman", 0, 24);			
		TrueTypeFont font = new TrueTypeFont(awtFont, false);
		inputBox = new TextField(container, font, 75, 350, 300, 25);
		startButton = new TextField(container, font, 375, 400, 125, 35);
		startButton.setBackgroundColor(Color.gray);
		startButton.setBorderColor(Color.white);
		startButton.setTextColor(Color.blue);
		startButton.setText("Start Game");
		startButton.setAcceptingInput(false);
		
		startButton.addListener(new ComponentListener() {
			@Override
			public void componentActivated(AbstractComponent source) {
				System.out.println("ACTIVATED!");
			}
		});
		// Setup textfields		
		inputBox.addListener(new ComponentListener() {				
			@Override
			public void componentActivated(AbstractComponent source) {
				String str = inputBox.getText().trim();
				// Ignore empty chats
				if( str.length() == 0 ) { return; }
				// Truncate the message to the max chars
				if( str.length() > DungeonDigger.MAX_MESSAGE_LENGTH ) { str = str.substring(0, DungeonDigger.MAX_MESSAGE_LENGTH); }
				// Send packet
				if( isServer ) { 
					// Prepend our name
					str = DungeonDigger.ACCOUNT_NAME + "(GameHost): " + str;
					ChatPacket msg = new ChatPacket(str);
					
					DungeonDigger.CHATS.add(msg);
					if( DungeonDigger.CHATS.size() > 10 ) { DungeonDigger.CHATS.pop(); }
					
					DungeonDigger.SERVER.sendToAllTCP( msg );
				}
				else { 
					// Prepend our name
					str = DungeonDigger.ACCOUNT_NAME + ": " + str;
					DungeonDigger.CLIENT.sendTCP( new ChatPacket(str) ); 
				}
				inputBox.setText("");
			}
		});
	}
	
	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
		switch(DungeonDigger.STATE) {
			case LISTENING:
				g.setColor(Color.white);
				g.drawString("Waiting for a connection...", 75, 75);
				break;
			case SERVING:
				g.setColor(Color.blue);
				g.drawString("Dungeon Lobby", 75, 75);
				renderTexts(container, g);
				renderChats(container, g);
				g.setColor(Color.white);
				inputBox.render(container, g);
				startButton.render(container, g);
				inputBox.setFocus(true);
				break;
			case CONNECTING:
				g.setColor(Color.white);
				g.drawString("Connecting...", 75, 75);
				break;
			case CONNECTED:
				renderTexts(container, g);
				renderChats(container, g);
				g.setColor(Color.white);
				inputBox.render(container, g);
				inputBox.setFocus(true);
				break;				
		}
	}

	@Override
	public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
		Iterator<TextPacket> it = DungeonDigger.TEXTS.iterator();
		while(it.hasNext()) {			
			TextPacket t = it.next();
			t.passedTime += delta;
			if( t.durationInMilliseconds != 0 && t.passedTime >= t.durationInMilliseconds ) {
				it.remove();
			}
		}		
		
		if( !isServer && DungeonDigger.STATE == ConnectionState.DISCONNECTED ) {
			SignOff s = new SignOff();
			s.account = DungeonDigger.ACCOUNT_NAME;
			DungeonDigger.CLIENT.sendTCP(s);
			game.enterState(DungeonDigger.MAINMENU);
		}
		
		if( DungeonDigger.STATE == ConnectionState.LAUNCHINGGAME || DungeonDigger.STATE == ConnectionState.JOININGGAME) {
			logger.info("Entering into MultiplayerDungeon state.");
			game.enterState( DungeonDigger.CHOSEN_GAME_STATE );
		}
	}
	
	public void renderTexts(GameContainer c, Graphics g) {
		for( TextPacket t: DungeonDigger.TEXTS ) {
			g.setColor( Color.white );
			g.drawString( t.text, t.x, t.y);
		}
	}
	
	public void renderChats(GameContainer c, Graphics g) {
		for(int i = 0; i < DungeonDigger.CHATS.size(); i++) {
			ChatPacket chat = DungeonDigger.CHATS.get(i);
			g.setColor( Color.white );
			g.drawString( chat.text, 85, 95+(i*20));
		}
	}
	
	public void startAsServer() {
		try { 
			DungeonDigger.SERVER.start();
			DungeonDigger.SERVER.bind(4444);
			// Listener log setup
			DungeonDigger.SERVER.addListener( new Network.ServerListener() );
		} catch( IOException e ) { e.printStackTrace(); }
	}
	
	public void startAsClient() {
		try {
			DungeonDigger.CLIENT.start();
			DungeonDigger.CLIENT.connect(5000, "127.0.0.1", 4444);
									
			DungeonDigger.CLIENT.addListener(new Network.ClientListener());
			
			LoginRequest request = new LoginRequest();
			request.account = DungeonDigger.ACCOUNT_NAME;
			logger.info("Sent a login request");
			DungeonDigger.CLIENT.sendTCP(request);
		} catch ( IOException e ) {e.printStackTrace();}
	}
	
	/**
	 * Load all .csf files into memory for players' characters
	 */
	public void loadCharacterFiles() {
		BufferedReader in;
		File file = new File("characters");
		if( !file.isDirectory() ) { file.mkdir(); }
		else {
			// Create filter to ignore all but csf files
			FilenameFilter charFilesOnly = new FilenameFilter() {
				@Override
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
						logger.info("Character file: " + f.getName() + " seems corrupt. Skipping.");
						continue;
					}
					
					// Setup player object
					StringBuffer property = new StringBuffer();
					while( (line = in.readLine()) != null ) {
						property.append(line.substring(1, line.indexOf("]")));
						if( property.toString().equalsIgnoreCase("NAME") ) { 
							loadee.setName( line.substring(line.indexOf("]")+1));
							if( DungeonDigger.CHARACTERBANK.get(property) != null ) {
								logger.info("Duplicant character found: " + loadee.getName());
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
						logger.info("Loaded character: " + loadee.getName());
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	// Capture clicks and see if they clicked the start button
	@Override
	public void mouseClicked(int button, int x, int y, int clickCount){
		if( x >= 375 && x <= 500 && y >= 400 && y <= 435 && isServer ) {
			DungeonDigger.CHOSEN_GAME_STATE = DungeonDigger.MULTIPLAYERDUNGEON;
			DungeonDigger.STATE = ConnectionState.LAUNCHINGGAME;
			DungeonDigger.SERVER.sendToAllTCP(new GameJoinPacket( DungeonDigger.CHOSEN_GAME_STATE ));
		}
	}
}
