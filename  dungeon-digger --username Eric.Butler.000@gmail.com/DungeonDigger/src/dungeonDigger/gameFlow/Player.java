package dungeonDigger.gameFlow;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;

import dungeonDigger.contentGeneration.DungeonGenerator;

public class Player {
	private String name;
	private int playerXCoord = 500, playerYCoord = 500;
	private Image icon;					// Image/avatar our player uses
	private int hitPoints = 20;			// If 0: die
	private int speed = 3;				// Lower = faster, used as a reload time for moving
	private double reload = 500;		// Time between attacks
	private double movementTimer = 0;	// Tracks time passed until we can move
	private double reloadTimer = 0;		// Tracks time passes until we can fire
	private boolean flippedLeft;		// Tells if we're facing left
	private DungeonGenerator myLevel;	// The current dungeon

	public Player() {
		try {
			setIcon( new Image("dungeonDigger/resources/dwarf1.png", new Color(255, 0, 255) ) );
		} catch (SlickException e) { e.printStackTrace(); }
	}
	
	public void update(GameContainer container, int delta) {
		Input inputs = container.getInput();
		addReloadTimer(delta);
		
		///////////////
		// MOVEMENTS //
		///////////////
		
		if (inputs.isKeyDown(Keyboard.KEY_UP)) { 
			setPlayerYCoord( getPlayerYCoord() - myLevel.canMove( Direction.NORTH, playerYCoord, playerXCoord, getSpeed() ) ); 
		} 

		if (inputs.isKeyDown(Keyboard.KEY_DOWN)) { 
			setPlayerYCoord( getPlayerYCoord() + myLevel.canMove( Direction.SOUTH, playerYCoord, playerXCoord, getSpeed() ) ); 
		} 

		if (inputs.isKeyDown(Keyboard.KEY_LEFT)) { 
			setFlippedLeft(true);		
			setPlayerXCoord( getPlayerXCoord() - myLevel.canMove( Direction.WEST, playerYCoord, playerXCoord, getSpeed() ) ); 
		} 

		if (inputs.isKeyDown(Keyboard.KEY_RIGHT)) {
			setFlippedLeft(false);
			setPlayerXCoord( getPlayerXCoord() + myLevel.canMove( Direction.EAST, playerYCoord, playerXCoord, getSpeed() ) ); 
		} 
		
		// Check inputs
		if (inputs.isKeyDown(Keyboard.KEY_W)) {  } 
		else {  }
		if (inputs.isKeyDown(Keyboard.KEY_S)) {  } 
		else {  }
		if (inputs.isKeyDown(Keyboard.KEY_A)) {  } 
		else {  }
		if (inputs.isKeyDown(Keyboard.KEY_D)) {  } 
		else {  }
	}
	
	/***********************
	 * GETTERS AND SETTERS *
	 ***********************/

	public int getPlayerXCoord() {
		return playerXCoord;
	}
	public int getRelativePlayerX() {
		return playerXCoord*myLevel.getRatioX();
	}
	public void setPlayerXCoord(int playerXCoord) {
		this.playerXCoord = playerXCoord;
	}
	public int getPlayerYCoord() {
		return playerYCoord;
	}
	public int getRelativePlayerY() {
		return playerYCoord*myLevel.getRatioY();
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
	public void setMovementTimer(double movementTimer) {
		this.movementTimer = movementTimer;
	}
	public void addMovementTimer(double movementTimer) {
		this.movementTimer += movementTimer;
		if( movementTimer > speed ) { movementTimer = speed; }
	}
	public double getMovementTimer() {
		return movementTimer;
	}
	public void setMyLevel(DungeonGenerator myLevel) {
		this.myLevel = myLevel;
	}
	public DungeonGenerator getMyLevel() {
		return myLevel;
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
}
