package dungeonDigger.gameFlow;

import java.awt.Font;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.gui.AbstractComponent;
import org.newdawn.slick.gui.ComponentListener;
import org.newdawn.slick.gui.TextField;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.util.ResourceLoader;

import dungeonDigger.network.ConnectionState;

public class MainMenu extends BasicGameState {
	TextField tf;
	boolean listening, connecting, triggered, go;
	String accountName;
	
	@Override
	public int getID() { return 0; }

	@Override
	public void init(GameContainer container, StateBasedGame game) throws SlickException {
		loadImages();
		Font awtFont = new Font("Times New Roman", 0, 24);			
		TrueTypeFont font = new TrueTypeFont(awtFont, false);
		
		tf = new TextField(container, font, 315, 75, 150, 25);
		tf.setTextColor(Color.black);
		tf.setBorderColor(Color.red);
		tf.setCursorVisible(true);
		tf.setBackgroundColor(Color.white);
		tf.addListener(new ComponentListener() {				
			@Override
			public void componentActivated(AbstractComponent source) {
				accountName = tf.getText().trim().toLowerCase();
				if( accountName.length() > 0 ) {
					DungeonDigger.ACCOUNT_NAME = accountName;
					switch(DungeonDigger.STATE) {
						case LOGGINGON:
							DungeonDigger.STATE = ConnectionState.CONNECTING; 
							break;
						case SETTINGUP:
							DungeonDigger.STATE = ConnectionState.LISTENING; 
							break;
					}
				}
			}
		});
	}

	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
		switch(DungeonDigger.STATE) {
			case IDLE:
				g.setColor(Color.white);
				g.drawString("1. SINGLE PLAYER", 280, 180);
				g.drawString("3. CONNECT TO SERVER", 280, 225);
				g.drawString("4. HOST SERVER", 280, 270);
				break;
			case LOGGINGON:
			case SETTINGUP:
				g.setColor(Color.white);
				g.drawString("Enter your account name: ", 75, 75);
				tf.render(container, g);
				break;		
		}

		g.resetTransform();
	}

	@Override
	public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
		Input inputs = container.getInput();
		
		switch(DungeonDigger.STATE) {
			case SINGLEPLAYER:
				DungeonDigger.myCharacter = new NetworkPlayer();
				game.enterState(DungeonDigger.SINGLEPLAYERDUNGEON);
				break;
			case LISTENING:
			case CONNECTING:
				game.enterState(DungeonDigger.LOBBY);
				break;
			case IDLE:				
				if( inputs.isKeyDown(Keyboard.KEY_1) ) { DungeonDigger.STATE = ConnectionState.SINGLEPLAYER; }
				if( inputs.isKeyDown(Keyboard.KEY_3) ) { DungeonDigger.STATE = ConnectionState.LOGGINGON; }
				if( inputs.isKeyDown(Keyboard.KEY_4) ) { DungeonDigger.STATE = ConnectionState.SETTINGUP; }
				if( inputs.isKeyDown(Keyboard.KEY_ESCAPE) ) { System.exit(0); }
				break;
		}
	}
	
	public void loadImages() {
		try {
			DungeonDigger.IMAGES.put("dwarf1", new Image( "dwarf1.png", new Color(255, 0, 255)));
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}
}
