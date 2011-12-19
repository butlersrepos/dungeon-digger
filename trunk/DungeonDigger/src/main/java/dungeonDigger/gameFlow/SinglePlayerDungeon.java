package dungeonDigger.gameFlow;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.KeyListener;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import dungeonDigger.contentGeneration.DungeonGenerator;
import dungeonDigger.entities.NetworkPlayer;

public class SinglePlayerDungeon extends DungeonDiggerState implements KeyListener {
	private boolean gen1Toggled, gen2Toggled;
	private double[] hallsDensity = new double[]{1d, 0.95d};
	private NetworkPlayer myPlayer;

	@Override
	public int getID() { return 1; }

	public void init(GameContainer container, StateBasedGame game) throws SlickException {
		MultiplayerDungeon.CLIENT_VIEW = new DungeonGenerator();
	}
	
	@Override
	public void enter(GameContainer container, StateBasedGame game) {
		myPlayer = DungeonDigger.myCharacter;
		myPlayer.setInput(container.getInput());
		
		MultiplayerDungeon.CLIENT_VIEW.getPlayerList().add(myPlayer);
	}
	
	@Override
	public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
		super.update(container, game, delta);
		Input inputs = container.getInput();
		
		// 1 & 2 generate a layout
		if( inputs.isKeyDown(Keyboard.KEY_9) ) {
			if( !gen1Toggled ) {
				MultiplayerDungeon.CLIENT_VIEW.generateDungeon1(99, 99, 0.25, hallsDensity);
				myPlayer.setPlayerXCoord( (int)MultiplayerDungeon.CLIENT_VIEW.getEntranceCoords().x );
				myPlayer.setPlayerYCoord( (int)MultiplayerDungeon.CLIENT_VIEW.getEntranceCoords().y );
				gen1Toggled = true;
			}
		} else { gen1Toggled = false; }
		if( inputs.isKeyDown(Keyboard.KEY_0) ) {
			if( !gen2Toggled ) {
				MultiplayerDungeon.CLIENT_VIEW.generateDungeon2(99, 99, 50000, 4, 0.45, true);
				gen2Toggled = true;
			}
		} else { gen2Toggled = false; }
	}
	
	@Override
	public void keyPressed(int key, char c) {
		myPlayer.keyPressed(key, c);
	}
	@Override
	public void keyReleased(int key, char c) {
		myPlayer.keyReleased(key, c);
	}
}
