package dungeonDigger.gameFlow;

import java.awt.Font;
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

import dungeonDigger.Enums.GameState;
import dungeonDigger.Tools.Constants;
import dungeonDigger.Tools.References;
import dungeonDigger.entities.NetworkPlayer;
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

	public void init(GameContainer container, StateBasedGame game) throws SlickException {}

	@Override
	public void enter(GameContainer container, StateBasedGame game){		
		if( References.STATE == ConnectionState.LISTENING ) {
			isServer = true;
			Network.register(References.SERVER);
			// Load our character up
			if( References.CHARACTERBANK.get(References.ACCOUNT_NAME) == null ) {
				NetworkPlayer player = new NetworkPlayer();
				player.setName(References.ACCOUNT_NAME);
				player.setHitPoints(20);
				player.setSpeed(3);
				player.setIconName("dwarf1.png");
				player.setPosition(0, 0);
				References.CHARACTERBANK.put(References.ACCOUNT_NAME, player);
			}
			References.ACTIVESESSIONNAMES.add(References.ACCOUNT_NAME);
			References.myCharacter = References.CHARACTERBANK.get(References.ACCOUNT_NAME);
			startAsServer();
		}
		if( References.STATE == ConnectionState.CONNECTING ) {
			Network.register(References.CLIENT);
			startAsClient(References.IP_CONNECT);
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
			public void componentActivated(AbstractComponent source) {
				System.out.println("ACTIVATED!");
			}
		});
		// Setup textfields		
		inputBox.addListener(new ComponentListener() {				
			public void componentActivated(AbstractComponent source) {
				String str = inputBox.getText().trim();
				// Ignore empty chats
				if( str.length() == 0 ) { return; }
				// Truncate the message to the max chars
				if( str.length() > Constants.MAX_MESSAGE_LENGTH ) { str = str.substring(0, Constants.MAX_MESSAGE_LENGTH); }
				// Send packet
				if( isServer ) { 
					// Prepend our name
					str = References.ACCOUNT_NAME + "(GameHost): " + str;
					ChatPacket msg = new ChatPacket(str);
					
					References.CHATS.add(msg);
					if( References.CHATS.size() > 10 ) { References.CHATS.remove(); }
					
					References.SERVER.sendToAllTCP( msg );
				}
				else { 
					// Prepend our name
					str = References.ACCOUNT_NAME + ": " + str;
					References.CLIENT.sendTCP( new ChatPacket(str) ); 
				}
				inputBox.setText("");
			}
		});
	}
	
	public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
		switch(References.STATE) {
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

	public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
		Iterator<TextPacket> it = References.TEXTS.iterator();
		while(it.hasNext()) {			
			TextPacket t = it.next();
			t.passedTime += delta;
			if( t.durationInMilliseconds != 0 && t.passedTime >= t.durationInMilliseconds ) {
				it.remove();
			}
		}		
		
		if( !isServer && References.STATE == ConnectionState.DISCONNECTED ) {
			SignOff s = new SignOff();
			s.account = References.ACCOUNT_NAME;
			References.CLIENT.sendTCP(s);
			game.enterState( GameState.MAIN_MENU.ordinal() );
		}
		
		if( References.STATE == ConnectionState.LAUNCHINGGAME || References.STATE == ConnectionState.JOININGGAME) {
			logger.info("Entering into MultiplayerDungeon state.");
			game.enterState( References.CHOSEN_GAME_STATE.ordinal() );
		}
	}
	
	public void renderTexts(GameContainer c, Graphics g) {
		for( TextPacket t: References.TEXTS ) {
			g.setColor( Color.white );
			g.drawString( t.text, t.x, t.y);
		}
	}
	
	public void renderChats(GameContainer c, Graphics g) {
		for(int i = 0; i < References.CHATS.size(); i++) {
			ChatPacket chat = References.CHATS.get(i);
			g.setColor( Color.white );
			g.drawString( chat.text, 85, 95+(i*20));
		}
	}
	
	public void startAsServer() {
		try { 
			References.SERVER.start();
			References.SERVER.bind(4444);
			
			// Listener log setup
			References.SERVER.addListener( new Network.ServerListener() );
		} catch( IOException e ) { e.printStackTrace(); }
	}
	
	public void startAsClient(String ip) {
		try {
			References.CLIENT.start();
			References.CLIENT.connect(5000, ip, 4444);
									
			References.CLIENT.addListener(new Network.ClientListener());
			
			LoginRequest request = new LoginRequest();
			request.account = References.ACCOUNT_NAME;
			logger.info("Sent a login request");
			References.CLIENT.sendTCP(request);
		} catch ( IOException e ) {e.printStackTrace();}
	}
	
	// Capture clicks and see if they clicked the start button
	@Override
	public void mouseClicked(int button, int x, int y, int clickCount){
		if( x >= 375 && x <= 500 && y >= 400 && y <= 435 && isServer ) {
			References.CHOSEN_GAME_STATE = GameState.MULTIPLAYERDUNGEON;
			References.STATE = ConnectionState.LAUNCHINGGAME;
			References.SERVER.sendToAllTCP(new GameJoinPacket( References.CHOSEN_GAME_STATE.ordinal() ));
		}
	}
}
