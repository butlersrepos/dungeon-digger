package dungeonDigger.gameFlow;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import dungeonDigger.Tools.References;

/** This class facilitates the standard of updating the myCharacter object's logic and
 * rendering the dungeon.<br/>
 * All child objects should call their <b>super.update()</b> and <b>super.render()</b> before
 * doing their specific tasks.
 * @author Eric */
public abstract class DungeonDiggerState extends BasicGameState {
	public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
		switch(References.STATE) {
			case JOININGGAME: 
				break;
			case LAUNCHINGGAME: 
				break;			
			case INGAME:	
			case HOSTINGGAME:
			case SINGLEPLAYER:
				if( !References.PAUSED ) {
					References.myCharacter.update(container, delta);	
					References.ABILITY_FACTORY.update(container, game, delta);
					References.MOB_FACTORY.update(container, game, delta);	
				}
				break;
		}
	}
	
	public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
		switch(References.STATE) {
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
				g.translate(-References.myCharacter.getPosition().x+container.getWidth()/2, -References.myCharacter.getPosition().y+container.getHeight()/2);
				
				// The Dungeon handles rendering of the dungeon, players, mobs, abilities because it requires knowledge of all of them to render correctly
				References.CLIENT_VIEW.renderDungeon(container, g);
				// TODO: render HUD
				
				// Undo translation to render moving components (player, HUD)
				g.translate(References.myCharacter.getPosition().x-container.getWidth()/2, References.myCharacter.getPosition().y-container.getHeight()/2);
				break;
		}
	}
}
