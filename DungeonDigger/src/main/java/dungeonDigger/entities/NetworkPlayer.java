package dungeonDigger.entities;

import java.util.LinkedList;
import java.util.logging.Logger;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Color;
import org.newdawn.slick.Input;
import org.newdawn.slick.KeyListener;
import org.newdawn.slick.MouseListener;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;

import dungeonDigger.Enums.Direction;
import dungeonDigger.Tools.References;
import dungeonDigger.gameFlow.DungeonDigger;
import dungeonDigger.gameFlow.MultiplayerDungeon;
import dungeonDigger.network.Network;
import dungeonDigger.network.Network.PlayerMovementUpdate;

public class NetworkPlayer extends Agent implements KeyListener, MouseListener {
	/* Actual stored fields of the Player */			
	/** Image/avatar short filename our player uses **/
	private String iconName = "engy";						
	/** If 0: die **/
	private int hitPoints = 20;								
	/** How many pixels our character can move per step **/
	private int speed = 6;		
	/** Used for local rendering while we query server to validate movement **/
	transient private float proposedPlayerX, proposedPlayerY;	
	transient private double reload = 500;
	/** Tracks time passes until we can fire **/
	transient private double reloadTimer = 0;				
	/** Tells if we're facing left **/
	transient private boolean flippedLeft, pendingValidation;					
	transient Logger logger = Logger.getLogger("NetworkPlayer");
	transient LinkedList<Vector2f> movementList = new LinkedList<Vector2f>();
	transient boolean movingUp, movingDown, movingLeft, movingRight;
	transient Input inputs = null;
	transient Vector2f currentClick;
	
	public NetworkPlayer() {
		this.setName("MyPlayer");
		try {
			this.setSpriteSheet(new SpriteSheet(new Image("engy.png", Color.magenta), 60, 60));
			this.getCollisionBox().setWidth(60);
			this.getCollisionBox().setHeight(60);
		} catch( SlickException e ) { e.printStackTrace(); }
	}
	
	@Override
	public void update(GameContainer container, int delta) {
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
		this.setProposedPlayerX( this.getPosition().x );	
		this.setProposedPlayerY( this.getPosition().y );
		
		/* PRESSED UP */
		if( movingUp && (movement  = References.CLIENT_VIEW.canMove(Direction.NORTH, this.getTerrainCollisionBox(), speed))  > 0) {
			this.setProposedPlayerY( this.getPosition().y - movement );	
		} 
		/* PRESSED DOWN */
		if( movingDown && (movement  = References.CLIENT_VIEW.canMove(Direction.SOUTH, this.getTerrainCollisionBox(), speed))  > 0) { 
			this.setProposedPlayerY( this.getPosition().y + movement );
		} 
		/* PRESSED LEFT */
		if( movingLeft && (movement  = References.CLIENT_VIEW.canMove(Direction.WEST, this.getTerrainCollisionBox(), speed))  > 0) { 
			this.setProposedPlayerX( this.getPosition().x - movement );
			setFlippedLeft(true);
		} 
		/* PRESSED RIGHT */
		if( movingRight && (movement  = References.CLIENT_VIEW.canMove(Direction.EAST, this.getTerrainCollisionBox(), speed))  > 0) {
			this.setProposedPlayerX( this.getPosition().x + movement );
			setFlippedLeft(false);
		}	
		// If we move then handle it based on the server scenario we're in
		switch(References.STATE) {
			case INGAME:
				if( this.movementList.size() == 0 ) {
					this.movementList.add( new Vector2f( this.getPosition().x, this.getPosition().y ) );
				}
				this.movementList.add( new Vector2f( this.getProposedPlayerX(), this.getProposedPlayerY() ) );
				References.CLIENT.sendTCP(new Network.PlayerMovementRequest(name, proposedPlayerX, proposedPlayerY));
				this.setPosition( this.getProposedPlayerX(), this.getProposedPlayerY() );
				break;
			case HOSTINGGAME:
				PlayerMovementUpdate packet = new Network.PlayerMovementUpdate(name, this.getPosition().x, this.getPosition().y);
				References.SERVER.sendToAllTCP(packet);
				// NO BREAK; Allow to flow into SINGLEPLAYER condition
			case SINGLEPLAYER:
				this.setPosition( this.getProposedPlayerX(), this.getProposedPlayerY() );
				break;
		}
	}
	
	/***********************
	 * GETTERS AND SETTERS *
	 ***********************/
	@Override
	public float getWidth() {
		return this.getIcon().getWidth();
	}	
	@Override
	public float getHeight() {
		return this.getIcon().getHeight();
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
	public Image getIcon() {
		return this.getSpriteSheet().getSprite(0, 0);
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
	public void setProposedPlayerX(float proposedPlayerX) {
		this.proposedPlayerX = proposedPlayerX;
	}
	public float getProposedPlayerX() {
		return proposedPlayerX;
	}
	public void setProposedPlayerY(float proposedPlayerY) {
		this.proposedPlayerY = proposedPlayerY;
	}
	public float getProposedPlayerY() {
		return proposedPlayerY;
	}
	public void setPendingValidation(boolean pendingValidation) {
		this.pendingValidation = pendingValidation;
	}
	public boolean isPendingValidation() {
		return pendingValidation;
	}
	public LinkedList<Vector2f> getMovementList() {
		return movementList;
	}
	public void setMovementList(LinkedList<Vector2f> movementList) {
		this.movementList = movementList;
	}	
	/**
	 * Used only to calculate collision with map terrain
	 * @return
	 */
	public Rectangle getTerrainCollisionBox() {
		return this.getCollisionBox();
	}
	/**
	 * Used to calculate collision with players, projectiles, anything that isn't map terrain
	 * @return
	 */
	public Rectangle getEntityCollisionBox() {
		return new Rectangle(this.getPosition().x, this.getPosition().y + this.getIcon().getHeight()/2, this.getIcon().getWidth(), this.getIcon().getHeight()/2);
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
		if( !References.KEY_BINDINGS.containsKey(key) ) { return; }
		switch(References.KEY_BINDINGS.get(key)) {
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
		if( !References.KEY_BINDINGS.containsKey(key) ) { return; }
		switch(References.KEY_BINDINGS.get(key)) {
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
			case "pause":
				References.PAUSED = !References.PAUSED;
				System.out.println("\f");
				References.QUAD_COLLISION_MANIFOLD.printTree();
				break;
			default:
				References.ABILITY_FACTORY.use(References.SLOT_BINDINGS.get(References.KEY_BINDINGS.get(key)), this);
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
		References.log.info("Mouse clicked at: " + x + ", " + y);
		if( this.getQueuedAbility() != null  && this.getQueuedAbility().isWaitingForClick() ) {
			this.getQueuedAbility().setEndPoint(References.myCharacter.getPosition().x - 320 + x,
												References.myCharacter.getPosition().y - 320 + y);
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
