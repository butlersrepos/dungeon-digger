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
import dungeonDigger.network.Network;

public class MultiplayerDungeon extends DungeonDiggerState {
	private Vector2f startPos;
	// Used for dungeon data of both, aside: "CLIENT" = viewable interface
	public static DungeonGenerator CLIENT_VIEW;
	private Logger logger = Logger.getLogger("DungeonDigger.MultiplayerDungeon");
	
	@Override
	public int getID() { return 2; }

	public void init(GameContainer container, StateBasedGame game) throws SlickException {}

	@Override
	public void enter(GameContainer container, StateBasedGame game) throws SlickException {		
		logger.info("Entering MultiplayerDungeon...");
		if( DungeonDigger.STATE == ConnectionState.LAUNCHINGGAME ) {
			logger.info("We're a server.");
			
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
			DungeonDigger.SERVER.sendToAllTCP(new Network.PlayerListPacket(CLIENT_VIEW.getPlayerList()));
			// Update state
			logger.info("Change State to hosting.");
			DungeonDigger.STATE = ConnectionState.HOSTINGGAME;
		} else {		
			logger.info("We're a client.");
			DungeonDigger.CLIENT.setTimeout(0);
			CLIENT_VIEW = new DungeonGenerator();
			CLIENT_VIEW.initializeDungeon(100, 100);
		}
	}
}
