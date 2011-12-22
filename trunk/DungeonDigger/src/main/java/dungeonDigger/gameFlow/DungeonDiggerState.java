package dungeonDigger.gameFlow;

import java.util.Iterator;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import dungeonDigger.entities.Ability;
import dungeonDigger.gameFlow.DungeonDigger;
import dungeonDigger.gameFlow.MultiplayerDungeon;

/**
 * This class facilitates the standard of updating the myCharacter object's logic and
 * rendering the dungeon.<br/>
 * All child objects should call their <b>super.update()</b> and <b>super.render()</b> before
 * doing their specific tasks.
 * @author Eric
 *
 */
public abstract class DungeonDiggerState extends BasicGameState {
	public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
		switch(DungeonDigger.STATE) {
			case JOININGGAME: 
				break;
			case LAUNCHINGGAME: 
				break;			
			case INGAME:	
			case HOSTINGGAME:
			case SINGLEPLAYER:
				DungeonDigger.myCharacter.update(container, delta);		
				Iterator<Ability> it = DungeonDigger.ACTIVE_ABILITIES.iterator();
				while( it.hasNext() ) {
					Ability a = it.next();
					a.update(container, game, delta);
					if( !a.isActive() ) { it.remove(); }
				}
				break;
		}
	}
	
	public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
		switch(DungeonDigger.STATE) {
			case JOININGGAME:
				g.setColor(Color.yellow);
				g.drawString("Loading map information...", 75, 75);
				break;			
			case LAUNCHINGGAME:
				g.setColor(Color.blue);
				g.drawString("Generating map and synching players...", 75, 75);
				break;
			case INGAME:
			case HOSTINGGAME:	
			case SINGLEPLAYER:
				// Translate graphics to preserve coordinates of map elements and follow the player
				g.translate(-DungeonDigger.myCharacter.getPlayerXCoord()+container.getWidth()/2, -DungeonDigger.myCharacter.getPlayerYCoord()+container.getHeight()/2);
				
				MultiplayerDungeon.CLIENT_VIEW.renderDungeon(container, g);
				
				// Undo translation to render moving components (player, HUD)
				g.translate(DungeonDigger.myCharacter.getPlayerXCoord()-container.getWidth()/2, DungeonDigger.myCharacter.getPlayerYCoord()-container.getHeight()/2);
				break;
		}
	}
}
