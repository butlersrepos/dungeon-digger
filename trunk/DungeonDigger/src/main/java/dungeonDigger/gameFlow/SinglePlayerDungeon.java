package dungeonDigger.gameFlow;

import java.util.HashMap;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Input;
import org.newdawn.slick.KeyListener;
import org.newdawn.slick.MouseListener;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import dungeonDigger.Tools.References;
import dungeonDigger.collisions.QuadCollisionEngine;
import dungeonDigger.contentGeneration.DungeonGenerator;
import dungeonDigger.entities.NetworkPlayer;

public class SinglePlayerDungeon extends DungeonDiggerState implements KeyListener, MouseListener {
	private double[] hallsDensity = new double[]{1d, 0.95d};
	private NetworkPlayer myPlayer;
	private HashMap<Integer, Boolean> keyToggled = new HashMap<Integer, Boolean>();

	@Override
	public int getID() { return 1; }

	public void init(GameContainer container, StateBasedGame game) throws SlickException {
		References.CLIENT_VIEW = new DungeonGenerator();
		keyToggled.put(Keyboard.KEY_GRAVE, false);
		keyToggled.put(Keyboard.KEY_T, false);
		keyToggled.put(Keyboard.KEY_Z, false);
		keyToggled.put(Keyboard.KEY_9, false);
		keyToggled.put(Keyboard.KEY_0, false);
	}
	
	@Override
	public void enter(GameContainer container, StateBasedGame game) {
		myPlayer = References.myCharacter;
		myPlayer.setInput(container.getInput());
		
		References.PLAYER_LIST.add(myPlayer);
		References.CLIENT_VIEW.generateDungeon1(99, 99, 0.25, hallsDensity);
		myPlayer.setPosition( (int)References.CLIENT_VIEW.getEntranceCoords().x, (int)References.CLIENT_VIEW.getEntranceCoords().y );
		System.out.println(System.currentTimeMillis() + " - Initiating the Quad Collision Manifold!");
		References.QUAD_COLLISION_MANIFOLD = QuadCollisionEngine.initiateNodeZero(References.CLIENT_VIEW.dungeon);
		System.out.println(System.currentTimeMillis() + " - Quad Collision Manifold successfully initiated!");
		References.CLIENT_VIEW.populateWithZombies();
		System.out.println(System.currentTimeMillis() + " - Zombies populated!");
	}
	
	@Override
	public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
		super.update(container, game, delta);
		Input inputs = container.getInput();
		
		// 9 & 0 generate a layout
		// Z spawns a zombie
		if( inputs.isKeyDown(Keyboard.KEY_GRAVE) && !keyToggled.get(Keyboard.KEY_GRAVE) ) {
			References.DEBUG_MODE = !References.DEBUG_MODE;
			keyToggled.put(Keyboard.KEY_GRAVE, true);
		} else if( !inputs.isKeyDown(Keyboard.KEY_9) && keyToggled.get(Keyboard.KEY_GRAVE) ) { 
			keyToggled.put(Keyboard.KEY_GRAVE, false);
		}
		
		if( inputs.isKeyDown(Keyboard.KEY_9) && !keyToggled.get(Keyboard.KEY_9) ) {
			References.CLIENT_VIEW.generateDungeon1(99, 99, 0.25, hallsDensity);
			myPlayer.setPosition( (int)References.CLIENT_VIEW.getEntranceCoords().x, (int)References.CLIENT_VIEW.getEntranceCoords().y );
			keyToggled.put(Keyboard.KEY_9, true);
		} else if( !inputs.isKeyDown(Keyboard.KEY_9) && keyToggled.get(Keyboard.KEY_9) ) { 
			keyToggled.put(Keyboard.KEY_9, false);
		}
		
		if( inputs.isKeyDown(Keyboard.KEY_0) && !keyToggled.get(Keyboard.KEY_0) ) {
			References.CLIENT_VIEW.generateDungeon2(99, 99, 50000, 4, 0.45, true);
			keyToggled.put(Keyboard.KEY_0, true);
		} else if( !inputs.isKeyDown(Keyboard.KEY_0) && keyToggled.get(Keyboard.KEY_0) ) { 
			keyToggled.put(Keyboard.KEY_0, false);
		}
		
		if( inputs.isKeyDown(Keyboard.KEY_Z) && !keyToggled.get(Keyboard.KEY_Z) ) {
			References.MOB_FACTORY.spawn("zombie", myPlayer.getPosition());
			keyToggled.put(Keyboard.KEY_Z, true);
		} else if( !inputs.isKeyDown(Keyboard.KEY_Z) && keyToggled.get(Keyboard.KEY_Z) ) {
			keyToggled.put(Keyboard.KEY_Z, false);
		}
		
		if( inputs.isKeyDown(Keyboard.KEY_T) && !keyToggled.get(Keyboard.KEY_T) ) {
			QuadCollisionEngine.outputTreeToFile();
			keyToggled.put(Keyboard.KEY_T, true);
		} else if( !inputs.isKeyDown(Keyboard.KEY_T) && keyToggled.get(Keyboard.KEY_T) ) {
			keyToggled.put(Keyboard.KEY_T, false);
		}
	}
	
	@Override
	public void keyPressed(int key, char c) {
		myPlayer.keyPressed(key, c);
	}
	@Override
	public void keyReleased(int key, char c) {
		myPlayer.keyReleased(key, c);
	}
	@Override
	public void mouseClicked(int button, int x, int y, int clickCount) {
		myPlayer.mouseClicked(button, x, y, clickCount);
	}
}
