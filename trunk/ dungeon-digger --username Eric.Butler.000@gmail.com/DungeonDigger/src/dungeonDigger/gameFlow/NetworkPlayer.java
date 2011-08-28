package dungeonDigger.gameFlow;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;

import dungeonDigger.network.Network;
import dungeonDigger.network.Network.PlayerMovementUpdatePacket;

public class NetworkPlayer {
	private String name;
	private int playerXCoord = 500, playerYCoord = 500;		// Actual pixel measurement
	private String iconName = "dwarf1";						// Image/avatar our player uses
	private int hitPoints = 20;								// If 0: die
	private int speed = 3;									// Lower = faster, used as a reload time for moving
	transient private double reload = 500;
	transient private double reloadTimer = 0;				// Tracks time passes until we can fire
	transient private boolean flippedLeft;					// Tells if we're facing left
	transient private Image icon;

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
				// Inform all clients of the move
				PlayerMovementUpdatePacket packet = new Network.PlayerMovementUpdatePacket(this.name, this.getPlayerXCoord(), this.getPlayerYCoord(), this.isFlippedLeft());
				DungeonDigger.SERVER.sendToAllTCP(packet);
				break;
			case INGAME:
				clientSidePlaying(container, delta, inputs);				
				break;
		}		
		
	}
	
	public void serverSidePlaying(GameContainer container, int delta, Input inputs) {
		int movement;
		if (inputs.isKeyDown(Keyboard.KEY_UP) && 
				(movement  = MultiplayerDungeon.CLIENT_VIEW.canMove(Direction.NORTH, playerYCoord, playerXCoord, speed))  > 0) {
			this.setPlayerYCoord( this.getPlayerYCoord() - movement );			
		} 

		if (inputs.isKeyDown(Keyboard.KEY_DOWN) &&
				(movement  = MultiplayerDungeon.CLIENT_VIEW.canMove(Direction.SOUTH, playerYCoord, playerXCoord, speed))  > 0) { 
			this.setPlayerYCoord( this.getPlayerYCoord() + movement );
		} 

		if (inputs.isKeyDown(Keyboard.KEY_LEFT) &&
				(movement  = MultiplayerDungeon.CLIENT_VIEW.canMove(Direction.WEST, playerYCoord, playerXCoord, speed))  > 0) { 
			setFlippedLeft(true);	
			this.setPlayerXCoord( this.getPlayerXCoord() - movement );
		} 

		if (inputs.isKeyDown(Keyboard.KEY_RIGHT) &&
				(movement  = MultiplayerDungeon.CLIENT_VIEW.canMove(Direction.NORTH, playerYCoord, playerXCoord, speed))  > 0) {
			setFlippedLeft(false);
			this.setPlayerXCoord( this.getPlayerXCoord() + movement );
		} 
	}
	
	public void clientSidePlaying(GameContainer container, int delta, Input inputs) {
		int movement;
		if (inputs.isKeyDown(Keyboard.KEY_UP) && 
				(movement  = MultiplayerDungeon.CLIENT_VIEW.canMove(Direction.NORTH, playerYCoord, playerXCoord, speed))  > 0) {
			this.setPlayerYCoord( this.getPlayerYCoord() - movement );			
		} 

		if (inputs.isKeyDown(Keyboard.KEY_DOWN) &&
				(movement  = MultiplayerDungeon.CLIENT_VIEW.canMove(Direction.SOUTH, playerYCoord, playerXCoord, speed))  > 0) { 
			this.setPlayerYCoord( this.getPlayerYCoord() + movement );
		} 

		if (inputs.isKeyDown(Keyboard.KEY_LEFT) &&
				(movement  = MultiplayerDungeon.CLIENT_VIEW.canMove(Direction.WEST, playerYCoord, playerXCoord, speed))  > 0) { 
			setFlippedLeft(true);	
			this.setPlayerXCoord( this.getPlayerXCoord() - movement );
		} 

		if (inputs.isKeyDown(Keyboard.KEY_RIGHT) &&
				(movement  = MultiplayerDungeon.CLIENT_VIEW.canMove(Direction.NORTH, playerYCoord, playerXCoord, speed))  > 0) {
			setFlippedLeft(false);
			this.setPlayerXCoord( this.getPlayerXCoord() + movement );
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
}
