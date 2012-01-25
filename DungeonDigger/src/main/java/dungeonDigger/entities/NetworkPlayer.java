package dungeonDigger.entities;

import java.awt.Point;
import java.util.LinkedList;
import java.util.logging.Logger;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.KeyListener;
import org.newdawn.slick.MouseListener;

import org.newdawn.slick.geom.Rectangle;

import dungeonDigger.Enums.Direction;
import dungeonDigger.gameFlow.DungeonDigger;
import dungeonDigger.gameFlow.MultiplayerDungeon;
import dungeonDigger.network.Network;
import dungeonDigger.network.Network.PlayerMovementUpdate;

public class NetworkPlayer extends Agent implements KeyListener, MouseListener {
	/* Actual stored fields of the Player */
	/** Actual pixel measurement **/
	private int playerXCoord = 500, playerYCoord = 500;				
	/** Image/avatar short filename our player uses **/
	private String iconName = "engy";						
	/** If 0: die **/
	private int hitPoints = 20;								
	/** How many pixels our character can move per step **/
	private int speed = 3;		
	
	/** Used for local rendering while we query server to validate movement **/
	transient private int proposedPlayerX, proposedPlayerY;	
	transient private double reload = 500;
	/** Tracks time passes until we can fire **/
	transient private double reloadTimer = 0;				
	/** Tells if we're facing left **/
	transient private boolean flippedLeft;
	transient private boolean pendingValidation;					
	transient private Image icon;
	transient Logger logger = Logger.getLogger("NetworkPlayer");
	transient LinkedList<Point> movementList = new LinkedList<Point>();
	transient boolean movingUp, movingDown, movingLeft, movingRight;
	transient Input inputs = null;
	transient Point currentClick;
	
	public NetworkPlayer() {
		if( iconName != null ) {		
			this.setIcon( DungeonDigger.IMAGES.get(iconName) );
		}
	}
	
	@Override
	public void update(GameContainer container, int delta) {
		// TODO: get delta from server?
		addReloadTimer(delta);
		
		// Entirely different logic and flow if we're server or client
		// Client - must send packets and validate movements
		// Server - simply moves and updates all players
		handleMovement(container, delta);		
	}
	
	@Override
	public void render(GameContainer c, Graphics g) {}
	
	public void handleMovement(GameContainer container, int delta) {
		int movement;
		// Reset our proposed coords so that one doesn't lag behind the other
		this.setProposedPlayerX( this.getPlayerXCoord() );	
		this.setProposedPlayerY( this.getPlayerYCoord() );
		
		/* PRESSED UP */
		if( movingUp && (movement  = MultiplayerDungeon.CLIENT_VIEW.canMove(Direction.NORTH, this.getTerrainCollisionBox(), speed))  > 0) {
			this.setProposedPlayerY( this.getPlayerYCoord() - movement );	
		} 
		/* PRESSED DOWN */
		if( movingDown && (movement  = MultiplayerDungeon.CLIENT_VIEW.canMove(Direction.SOUTH, this.getTerrainCollisionBox(), speed))  > 0) { 
			this.setProposedPlayerY( this.getPlayerYCoord() + movement );
		} 
		/* PRESSED LEFT */
		if( movingLeft && (movement  = MultiplayerDungeon.CLIENT_VIEW.canMove(Direction.WEST, this.getTerrainCollisionBox(), speed))  > 0) { 
			this.setProposedPlayerX( this.getPlayerXCoord() - movement );
			setFlippedLeft(true);
		} 
		/* PRESSED RIGHT */
		if( movingRight && (movement  = MultiplayerDungeon.CLIENT_VIEW.canMove(Direction.EAST, this.getTerrainCollisionBox(), speed))  > 0) {
			this.setProposedPlayerX( this.getPlayerXCoord() + movement );
			setFlippedLeft(false);
		}	
		// If we move then handle it based on the server scenario we're in
		switch(DungeonDigger.STATE) {
			case INGAME:
				if( this.movementList.size() == 0 ) {
					this.movementList.add( new Point( this.getPlayerXCoord(), this.getPlayerYCoord() ) );
				}
				this.movementList.add( new Point( this.getProposedPlayerX(), this.getProposedPlayerY() ) );
				DungeonDigger.CLIENT.sendTCP(new Network.PlayerMovementRequest(name, proposedPlayerX, proposedPlayerY));
				this.setPlayerXCoord( this.getProposedPlayerX() );
				this.setPlayerYCoord( this.getProposedPlayerY() );
				break;
			case HOSTINGGAME:
				PlayerMovementUpdate packet = new Network.PlayerMovementUpdate(name, playerXCoord, playerYCoord);
				DungeonDigger.SERVER.sendToAllTCP(packet);
				// NO BREAK; Allow to flow into SINGLEPLAYER condition
			case SINGLEPLAYER:
				this.setPlayerXCoord( this.getProposedPlayerX() );
				this.setPlayerYCoord( this.getProposedPlayerY() );
				break;
		}
	}
	/***********************
	 * GETTERS AND SETTERS *
	 ***********************/
	public Point getPlayerCenterPoint() {
		return new Point( this.playerXCoord, this.playerYCoord);
	}
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
	public LinkedList<Point> getMovementList() {
		return movementList;
	}
	public void setMovementList(LinkedList<Point> movementList) {
		this.movementList = movementList;
	}	
	/**
	 * Used only to calculate collision with map terrain
	 * @return
	 */
	public Rectangle getTerrainCollisionBox() {
		return new Rectangle(this.playerXCoord, this.playerYCoord, this.icon.getWidth(), this.icon.getHeight());
	}
	/**
	 * Used to calculate collision with players, projectiles, anything that isn't map terrain
	 * @return
	 */
	public Rectangle getEntityCollisionBox() {
		return new Rectangle(this.playerXCoord, this.playerYCoord + this.icon.getHeight()/2, this.icon.getWidth(), this.icon.getHeight()/2);
	}

	////////////////
	// Key Events //
	////////////////
	@Override
	public void setInput(Input input) { 
		System.out.println("input set");
		this.inputs = input; }

	@Override
	public boolean isAcceptingInput() { return this.inputs != null; }

	@Override
	public void inputEnded() { }

	@Override
	public void inputStarted() { }

	@Override
	public void keyReleased(int key, char c) { 
		if( !DungeonDigger.KEY_BINDINGS.containsKey(key) ) { return; }
		switch(DungeonDigger.KEY_BINDINGS.get(key)) {
			case "moveUp":
				movingUp = false;
				break;
			case "moveDown":
				movingDown = false;
				break;
			case "moveLeft":
				movingLeft = false;
				break;
			case "moveRight":
				movingRight = false;
				break;
			default:
				//DungeonDigger.ABILITY_FACTORY.use(DungeonDigger.SLOT_BINDINGS.get(DungeonDigger.KEY_BINDINGS.get(key)), this.getName());
				break;
		}
	}

	@Override
	public void keyPressed(int key, char c) {
		if( !DungeonDigger.KEY_BINDINGS.containsKey(key) ) { return; }
		switch(DungeonDigger.KEY_BINDINGS.get(key)) {
			case "moveUp":
				movingUp = true;
				break;
			case "moveDown":
				movingDown = true;
				break;
			case "moveLeft":
				movingLeft = true;
				break;
			case "moveRight":
				movingRight = true;
				break;
			default:
				DungeonDigger.ABILITY_FACTORY.use(DungeonDigger.SLOT_BINDINGS.get(DungeonDigger.KEY_BINDINGS.get(key)), this);
				break;
		}
	}

	//////////////////
	// Mouse Events //
	//////////////////
	@Override
	public void mouseWheelMoved(int change) { }

	@Override
	public void mouseClicked(int button, int x, int y, int clickCount) {
		System.out.println("Mouse clicked at: " + x + ", " + y);
		if( this.getQueuedAbility() != null  && this.getQueuedAbility().isWaitingForClick() ) {
			this.getQueuedAbility().setEndPoint(DungeonDigger.myCharacter.getPlayerXCoord() - 320 + x,
												DungeonDigger.myCharacter.getPlayerYCoord() - 320 + y);
			this.getQueuedAbility().setActive(true);
			this.getQueuedAbility().setWaitingForClick(false);
			this.setQueuedAbility(null);
		}
	}

	@Override
	public void mousePressed(int button, int x, int y) { }

	@Override
	public void mouseReleased(int button, int x, int y) { }

	@Override
	public void mouseMoved(int oldx, int oldy, int newx, int newy) { }

	@Override
	public void mouseDragged(int oldx, int oldy, int newx, int newy) { }
}
