package dungeonDigger.gameFlow;

import java.util.logging.Logger;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;

import dungeonDigger.network.Network;
import dungeonDigger.network.Network.PlayerMovementUpdate;

public class NetworkPlayer {
	/* Actual stored fields of the Player */
	private String name;
	// Actual pixel measurement
	private int playerXCoord = 500, playerYCoord = 500;				
	// Image/avatar our player uses
	private String iconName = "dwarf1";						
	// If 0: die
	private int hitPoints = 20;								
	// How many pixels our character can move per step
	private int speed = 3;		
	
	// Used for local rendering while we query server to validate movement
	transient private int proposedPlayerX, proposedPlayerY;	
	transient private double reload = 500;
	// Tracks time passes until we can fire
	transient private double reloadTimer = 0;				
	// Tells if we're facing left
	transient private boolean flippedLeft, pendingValidation;					
	transient private Image icon;
	transient Logger logger = Logger.getLogger("NetworkPlayer");
	
	public NetworkPlayer() {
		if( iconName != null ) {		
			this.setIcon( DungeonDigger.IMAGES.get(iconName) );
		}
	}
	
	public void update(GameContainer container, int delta) {
		Input inputs = container.getInput();
		// TODO: get delta from server?
		addReloadTimer(delta);
		
		// Entirely different logic and flow if we're server or client
		// Client - must send packets and validate movements
		// Server - simply moves and updates all players
		switch( DungeonDigger.STATE ) {
			case HOSTINGGAME:
				serverSidePlaying(container, delta, inputs);				
				break;
			case INGAME:	
				if( !this.isPendingValidation() ) {
					clientSidePlaying(container, delta, inputs);					
				}
				break;
		}		
		
	}
	
	public void serverSidePlaying(GameContainer container, int delta, Input inputs) {
		int movement;
		if (inputs.isKeyDown(Keyboard.KEY_UP) && 
				(movement  = MultiplayerDungeon.CLIENT_VIEW.canMove(Direction.NORTH, playerYCoord, playerXCoord, speed))  > 0) {
			this.setPlayerYCoord( this.getPlayerYCoord() - movement );		
			pendingValidation = true;
		} 

		if (inputs.isKeyDown(Keyboard.KEY_DOWN) &&
				(movement  = MultiplayerDungeon.CLIENT_VIEW.canMove(Direction.SOUTH, playerYCoord, playerXCoord, speed))  > 0) { 
			this.setPlayerYCoord( this.getPlayerYCoord() + movement );
			pendingValidation = true;
		} 

		if (inputs.isKeyDown(Keyboard.KEY_LEFT) &&
				(movement  = MultiplayerDungeon.CLIENT_VIEW.canMove(Direction.WEST, playerYCoord, playerXCoord, speed))  > 0) { 
			setFlippedLeft(true);	
			this.setPlayerXCoord( this.getPlayerXCoord() - movement );
			pendingValidation = true;
		} 

		if (inputs.isKeyDown(Keyboard.KEY_RIGHT) &&
				(movement  = MultiplayerDungeon.CLIENT_VIEW.canMove(Direction.EAST, playerYCoord, playerXCoord, speed))  > 0) {
			setFlippedLeft(false);
			this.setPlayerXCoord( this.getPlayerXCoord() + movement );
			pendingValidation = true;
		} 
		// Inform all clients of the move
		if( pendingValidation ) {
			PlayerMovementUpdate packet = new Network.PlayerMovementUpdate(name, playerXCoord, playerYCoord);
			DungeonDigger.SERVER.sendToAllTCP(packet);
			pendingValidation = false;
		}
	}
	
	public void clientSidePlaying(GameContainer container, int delta, Input inputs) {
		int movement;
		if (inputs.isKeyDown(Keyboard.KEY_UP) && 
				(movement  = MultiplayerDungeon.CLIENT_VIEW.canMove(Direction.NORTH, playerYCoord, playerXCoord, speed))  > 0) {
			this.setProposedPlayerY( this.getPlayerYCoord() - movement );	
			this.setPendingValidation(true);
		} 

		if (inputs.isKeyDown(Keyboard.KEY_DOWN) &&
				(movement  = MultiplayerDungeon.CLIENT_VIEW.canMove(Direction.SOUTH, playerYCoord, playerXCoord, speed))  > 0) { 
			this.setProposedPlayerY( this.getPlayerYCoord() + movement );
			this.setPendingValidation(true);
		} 

		if (inputs.isKeyDown(Keyboard.KEY_LEFT) &&
				(movement  = MultiplayerDungeon.CLIENT_VIEW.canMove(Direction.WEST, playerYCoord, playerXCoord, speed))  > 0) { 
			setFlippedLeft(true);	
			this.setProposedPlayerX( this.getPlayerXCoord() - movement );
			this.setPendingValidation(true);
		} 

		if (inputs.isKeyDown(Keyboard.KEY_RIGHT) &&
				(movement  = MultiplayerDungeon.CLIENT_VIEW.canMove(Direction.EAST, playerYCoord, playerXCoord, speed))  > 0) {
			setFlippedLeft(false);
			this.setProposedPlayerX( this.getPlayerXCoord() + movement );
			this.setPendingValidation(true);
		} 
		if( pendingValidation ) {
			DungeonDigger.CLIENT.sendTCP(new Network.PlayerMovementRequest(name, proposedPlayerX, proposedPlayerY));
		}		
	}
	/***********************
	 * GETTERS AND SETTERS *
	 ***********************/

	public int getPlayerXCoord() {
		return playerXCoord;
	}
	public void setPlayerXCoord(int playerXCoord) {
		this.playerXCoord = playerXCoord;
	}
	public int getPlayerYCoord() {
		return playerYCoord;
	}
	public void setPlayerYCoord(int playerYCoord) {
		this.playerYCoord = playerYCoord;
	}
	public int getHitPoints() {
		return hitPoints;
	}
	public void setHitPoints(int hitPoints) {
		this.hitPoints = hitPoints;
	}
	public int getSpeed() {
		return speed;
	}
	public void setSpeed(int speed) {
		this.speed = speed;
	}
	public double getReload() {
		return reload;
	}
	public void setReload(double reload) {
		this.reload = reload;
	}
	public void setReloadTimer(double reloadTimer) {
		this.reloadTimer = reloadTimer;
	}
	public void addReloadTimer(double reloadTimer) {
		this.reloadTimer += reloadTimer;
		if( reloadTimer > reload ) { reloadTimer = reload; }
	}
	public double getReloadTimer() {
		return reloadTimer;
	}
	public void setIcon(Image icon) {
		this.icon = icon;
	}
	public Image getIcon() {
		return icon;
	}

	public void setFlippedLeft(boolean flippedLeft) {
		this.flippedLeft = flippedLeft;
	}

	public boolean isFlippedLeft() {
		return flippedLeft;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getIconName() {
		return iconName;
	}

	public void setIconName(String iconName) {
		this.iconName = iconName;
	}

	public void setProposedPlayerX(int proposedPlayerX) {
		this.proposedPlayerX = proposedPlayerX;
	}

	public int getProposedPlayerX() {
		return proposedPlayerX;
	}

	public void setProposedPlayerY(int proposedPlayerY) {
		this.proposedPlayerY = proposedPlayerY;
	}

	public int getProposedPlayerY() {
		return proposedPlayerY;
	}

	public void setPendingValidation(boolean pendingValidation) {
		this.pendingValidation = pendingValidation;
	}

	public boolean isPendingValidation() {
		return pendingValidation;
	}
}
