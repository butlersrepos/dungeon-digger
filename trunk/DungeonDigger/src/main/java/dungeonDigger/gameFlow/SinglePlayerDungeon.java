package dungeonDigger.gameFlow;

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
	private boolean gen1Toggled, gen2Toggled, zToggled, tToggled;
	private double[] hallsDensity = new double[]{1d, 0.95d};
	private NetworkPlayer myPlayer;

	@Override
	public int getID() { return 1; }

	public void init(GameContainer container, StateBasedGame game) throws SlickException {
		References.CLIENT_VIEW = new DungeonGenerator();
	}
	
	@Override
	public void enter(GameContainer container, StateBasedGame game) {
		myPlayer = References.myCharacter;
		myPlayer.setInput(container.getInput());
		
		References.PLAYER_LIST.add(myPlayer);
		References.CLIENT_VIEW.generateDungeon1(99, 99, 0.25, hallsDensity);
		myPlayer.setPosition( (int)References.CLIENT_VIEW.getEntranceCoords().x, (int)References.CLIENT_VIEW.getEntranceCoords().y );
		System.out.println(System.currentTimeMillis() + " - Initiating the Quad Collision Manifold!");
		QuadCollisionEngine.initiateNodeZero(References.CLIENT_VIEW.dungeon);
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
		if( inputs.isKeyDown(Keyboard.KEY_9) ) {
			if( !gen1Toggled ) {
				References.CLIENT_VIEW.generateDungeon1(99, 99, 0.25, hallsDensity);
				myPlayer.setPosition( (int)References.CLIENT_VIEW.getEntranceCoords().x, (int)References.CLIENT_VIEW.getEntranceCoords().y );
				gen1Toggled = true;
			}
		} else { gen1Toggled = false; }
		
		if( inputs.isKeyDown(Keyboard.KEY_0) ) {
			if( !gen2Toggled ) {
				References.CLIENT_VIEW.generateDungeon2(99, 99, 50000, 4, 0.45, true);
				gen2Toggled = true;
			}
		} else { gen2Toggled = false; }
		
		if( inputs.isKeyDown(Keyboard.KEY_Z) && !zToggled ) {
			References.MOB_FACTORY.spawn("zombie", myPlayer.getPosition());
			zToggled = true;
		} else {
			zToggled = false;
		}
		
		if( inputs.isKeyDown(Keyboard.KEY_T) && !tToggled ) {
			QuadCollisionEngine.outputTreeToFile();
			tToggled = true;
		} else {
			tToggled = false;
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
