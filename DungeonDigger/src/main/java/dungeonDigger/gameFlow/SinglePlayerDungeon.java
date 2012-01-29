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
	private boolean gen1Toggled, gen2Toggled, zToggled;
	private double[] hallsDensity = new double[]{1d, 0.95d};
	private NetworkPlayer myPlayer;

	@Override
	public int getID() { return 1; }

	public void init(GameContainer container, StateBasedGame game) throws SlickException {
		MultiplayerDungeon.CLIENT_VIEW = new DungeonGenerator();
	}
	
	@Override
	public void enter(GameContainer container, StateBasedGame game) {
		myPlayer = References.myCharacter;
		myPlayer.setInput(container.getInput());
		
		References.PLAYER_LIST.add(myPlayer);
		MultiplayerDungeon.CLIENT_VIEW.generateDungeon1(99, 99, 0.25, hallsDensity);
		myPlayer.setPosition( (int)MultiplayerDungeon.CLIENT_VIEW.getEntranceCoords().x, (int)MultiplayerDungeon.CLIENT_VIEW.getEntranceCoords().y );
		System.out.println(System.currentTimeMillis() + " - Initiating the Quad Collision Manifold!");
		References.QUAD_COLLISION_MANIFOLD = QuadCollisionEngine.initiateNodeZero(MultiplayerDungeon.CLIENT_VIEW.dungeon);
		System.out.println(System.currentTimeMillis() + " - Quad Collision Manifold successfully initiated!");
	}
	
	@Override
	public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
		super.update(container, game, delta);
		Input inputs = container.getInput();
		
		// 9 & 0 generate a layout
		// Z spawns a zombie
		if( inputs.isKeyDown(Keyboard.KEY_9) ) {
			if( !gen1Toggled ) {
				MultiplayerDungeon.CLIENT_VIEW.generateDungeon1(99, 99, 0.25, hallsDensity);
				myPlayer.setPosition( (int)MultiplayerDungeon.CLIENT_VIEW.getEntranceCoords().x, (int)MultiplayerDungeon.CLIENT_VIEW.getEntranceCoords().y );
				gen1Toggled = true;
			}
		} else { gen1Toggled = false; }
		if( inputs.isKeyDown(Keyboard.KEY_0) ) {
			if( !gen2Toggled ) {
				MultiplayerDungeon.CLIENT_VIEW.generateDungeon2(99, 99, 50000, 4, 0.45, true);
				gen2Toggled = true;
			}
		} else { gen2Toggled = false; }
		if( inputs.isKeyDown(Keyboard.KEY_Z) && !zToggled ) {
			References.MOB_FACTORY.spawn("zombie", myPlayer.getPosition());
			zToggled = true;
		} else {
			zToggled = false;
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
