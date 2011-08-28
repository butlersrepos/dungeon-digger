package dungeonDigger.gameFlow;

import java.util.logging.Logger;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import com.esotericsoftware.kryonet.Connection;

import dungeonDigger.contentGeneration.DungeonGenerator;
import dungeonDigger.network.ConnectionState;
import dungeonDigger.network.Network.GameStartPacket;

public class MultiplayerDungeon extends BasicGameState {
	private boolean isServer;
	private Vector2f startPos;
	// Used for both, "CLIENT" = viewable interface
	public static DungeonGenerator CLIENT_VIEW;
	private Logger logger = Logger.getLogger("DungeonDigger.MultiplayerDungeon");
	
	@Override
	public int getID() { return 2; }

	@Override
	public void init(GameContainer container, StateBasedGame game) throws SlickException {
	}

	@Override
	public void enter(GameContainer container, StateBasedGame game) throws SlickException {		
		logger.info("Entering MultiplayerDungeon...");
		if( DungeonDigger.STATE == ConnectionState.LAUNCHINGGAME ) {
			logger.info("We're a server.");
			isServer = true;
			
			// Get a new dungeon
			logger.info("Making dungeon");
			CLIENT_VIEW = new DungeonGenerator();
			CLIENT_VIEW.generateDungeon1(100, 100, 0.25 , new double[]{1.0, 0.75});
			
			// Add all our active players info to the dungeon
			logger.info("Adding players to it");
			for( String name: DungeonDigger.ACTIVESESSIONNAMES ) {
				CLIENT_VIEW.getPlayerList().add( DungeonDigger.CHARACTERBANK.get(name) );
			}
			
			// Calculate the top left most open spot
			startPos = CLIENT_VIEW.getEntranceCoords();
			// Set me there
			DungeonDigger.myCharacter.setPlayerXCoord((int)startPos.x);
			DungeonDigger.myCharacter.setPlayerYCoord((int)startPos.y);
			
			// Send the map file, then tell the players where to start, and to start
			logger.info("Sending tile packets.");
			for( Connection c : DungeonDigger.SERVER.getConnections() ) {
				c.setTimeout(0);
			}
			CLIENT_VIEW.serverSendMap();
			
			// Update state
			logger.info("Change State to hosting.");
			DungeonDigger.STATE = ConnectionState.HOSTINGGAME;
		} else {		
			logger.info("We're a client.");
			isServer = false;
			
			CLIENT_VIEW = new DungeonGenerator();
			CLIENT_VIEW.initializeDungeon(100, 100);
		}
	}
	
	@Override
	public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
		switch(DungeonDigger.STATE) {
			case JOININGGAME: break;
			case LAUNCHINGGAME: break;			
			case INGAME:	
			case HOSTINGGAME:
				DungeonDigger.myCharacter.update(container, delta);				
				break;
		}
	}
	
	@Override
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
				// Translate graphics to preserve coordinates of map elements and follow the player
				g.translate(-DungeonDigger.myCharacter.getPlayerXCoord()+container.getWidth()/2, -DungeonDigger.myCharacter.getPlayerYCoord()+container.getHeight()/2);
				
				CLIENT_VIEW.renderDungeon(container, g);
				
				// Undo translation to render moving components (player, HUD)
				g.translate(DungeonDigger.myCharacter.getPlayerXCoord()-container.getWidth()/2, DungeonDigger.myCharacter.getPlayerYCoord()-container.getHeight()/2);
				break;
		}
	}
}
