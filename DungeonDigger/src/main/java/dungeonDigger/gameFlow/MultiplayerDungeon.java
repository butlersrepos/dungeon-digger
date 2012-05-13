package dungeonDigger.gameFlow;

import java.util.logging.Logger;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.state.StateBasedGame;

import com.esotericsoftware.kryonet.Connection;

import dungeonDigger.Tools.References;
import dungeonDigger.contentGeneration.DungeonGenerator;
import dungeonDigger.network.ConnectionState;
import dungeonDigger.network.Network;

public class MultiplayerDungeon extends DungeonDiggerState {
	private Vector2f startPos;
	// Used for dungeon data of both, aside: "CLIENT" = viewable interface
	private Logger logger = Logger.getLogger("DungeonDigger.MultiplayerDungeon");
	
	@Override
	public int getID() { return 2; }

	public void init(GameContainer container, StateBasedGame game) throws SlickException {}

	@Override
	public void enter(GameContainer container, StateBasedGame game) throws SlickException {		
		logger.info("Entering MultiplayerDungeon...");
		if( References.STATE == ConnectionState.LAUNCHINGGAME ) {
			logger.info("We're a server.");
			
			// Get a new dungeon
			logger.info("Making dungeon");
			References.CLIENT_VIEW = new DungeonGenerator();
			References.CLIENT_VIEW.generateDungeon1(100, 100, 0.25 , new double[]{1.0, 0.75});
			
			// Add all our active players info to the dungeon
			logger.info("Adding players to it");
			for( String name: References.ACTIVESESSIONNAMES ) {
				References.PLAYER_LIST.add( References.CHARACTERBANK.get(name) );
			}
			
			// Calculate the top left most open spot
			startPos = References.CLIENT_VIEW.getEntranceCoords();
			// Set me there
			References.myCharacter.setPosition((int)startPos.x, (int)startPos.y);
			
			// Send the map file, then tell the players where to start, and to start
			logger.info("Sending tile packets.");
			for( Connection c : References.SERVER.getConnections() ) {
				c.setTimeout(0);
			}
			References.CLIENT_VIEW.serverSendMap();
			References.SERVER.sendToAllTCP(new Network.PlayerListPacket(References.PLAYER_LIST));
			// Update state
			logger.info("Change State to hosting.");
			References.STATE = ConnectionState.HOSTINGGAME;
		} else {		
			logger.info("We're a client.");
			References.CLIENT.setTimeout(0);
			References.CLIENT_VIEW = new DungeonGenerator();
			References.CLIENT_VIEW.initializeDungeon(100, 100);
		}
	}
}
