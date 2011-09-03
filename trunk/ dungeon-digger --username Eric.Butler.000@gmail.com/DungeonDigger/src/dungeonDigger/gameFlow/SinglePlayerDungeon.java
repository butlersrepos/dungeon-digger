package dungeonDigger.gameFlow;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import dungeonDigger.contentGeneration.DungeonGenerator;

public class SinglePlayerDungeon extends BasicGameState {
	private boolean gen1Toggled, gen2Toggled;
	private double[] hallsDensity = new double[]{1d, 0.95d};
	private NetworkPlayer myPlayer;

	@Override
	public int getID() { return 1; }

	@Override
	public void init(GameContainer container, StateBasedGame game) throws SlickException {
		MultiplayerDungeon.CLIENT_VIEW = new DungeonGenerator();
	}
	
	@Override
	public void enter(GameContainer container, StateBasedGame game) {
		myPlayer = DungeonDigger.myCharacter;
		MultiplayerDungeon.CLIENT_VIEW.getPlayerList().add(myPlayer);
	}
	
	@Override
	public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
		Input inputs = container.getInput();

		for( NetworkPlayer p : MultiplayerDungeon.CLIENT_VIEW.getPlayerList() ) {
			p.update(container, delta);
		}
		
		// 1 & 2 generate a layout
		if( inputs.isKeyDown(Keyboard.KEY_1) ) {
			if( !gen1Toggled ) {
				MultiplayerDungeon.CLIENT_VIEW.generateDungeon1(99, 99, 0.25, hallsDensity);
				myPlayer.setPlayerXCoord( (int)MultiplayerDungeon.CLIENT_VIEW.getEntranceCoords().x );
				myPlayer.setPlayerYCoord( (int)MultiplayerDungeon.CLIENT_VIEW.getEntranceCoords().y );
				gen1Toggled = true;
			}
		} else { gen1Toggled = false; }
		if( inputs.isKeyDown(Keyboard.KEY_2) ) {
			if( !gen2Toggled ) {
				MultiplayerDungeon.CLIENT_VIEW.generateDungeon2(99, 99, 50000, 4, 0.45, true);
				gen2Toggled = true;
			}
		} else { gen2Toggled = false; }
	}
	
	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
		// Translate graphics to preserve coordinates of map elements and follow the player
		g.translate(-myPlayer.getPlayerXCoord()+container.getWidth()/2, -myPlayer.getPlayerYCoord()+container.getHeight()/2);
		
		MultiplayerDungeon.CLIENT_VIEW.renderDungeon(container, g);
		
		// Undo translation to render moving components (player, HUD)
		g.translate(myPlayer.getPlayerXCoord()-container.getWidth()/2, myPlayer.getPlayerYCoord()-container.getHeight()/2);
	}

}
