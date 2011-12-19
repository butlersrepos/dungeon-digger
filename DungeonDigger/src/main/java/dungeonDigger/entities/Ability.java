package dungeonDigger.entities;

import java.awt.Point;

import org.newdawn.slick.Animation;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.state.StateBasedGame;

import dungeonDigger.Enums.AbilityDeliveryMethod;
import dungeonDigger.gameFlow.DungeonDigger;

public class Ability {
	public static Ability EMPTY_ABILITY = new Ability("Empty") {
		@Override
		public void setActive(boolean value) { }
		@Override
		public void setChanneling(boolean value) { }
	};
	private String name;
	private boolean active, damaging, channeling, friendly, mouse, inited;
	private SpriteSheet spriteSheet, hitFrames;
	private Animation animation;
	private String ownerName;
	private Point startPoint, middlePoint, endPoint;
	private int speed;
	private AbilityDeliveryMethod adm;
	
	public Ability(String name) { this.name = name; }
	
	// Setup animation and such
	public void init() {
		
		inited = true;
	}
	
	// Where the magick happens, lawl...
	public void update(GameContainer container, StateBasedGame game, int delta) {
		if( !inited ) { init(); }
		if( !active ) { return; }
		
	}
	
	public void render(GameContainer container, Graphics g) {
		if( !active ) { return; }
	
	}
	
	/*
	 * GETTERS AND SETTERS AND BORING STUFF BELOW HERE
	 */
	public void setStartPoint(Point p) { this.startPoint = p; }
	public void setStartPoint(float x, float y) { this.startPoint = new Point((int) x, (int)y); }
	public Point getStartPoint() { return this.startPoint; }
	
	public void setMiddlePoint(Point p) { this.middlePoint = p; }
	public void setMiddlePoint(float x, float y) { this.middlePoint = new Point((int) x, (int)y); }
	public Point getMiddlePoint() { return this.middlePoint; }
	
	public void setEndPoint(Point p) { this.endPoint = p; }
	public void setEndPoint(float x, float y) { this.endPoint = new Point((int) x, (int)y); }
	public Point getEndPoint() { return this.endPoint; }
	
	public void setName(String name) { this.name = name; }
	public String getName() { return this.name; }
	
	public void setOwner(String ownerName) { this.ownerName = ownerName; }
	public String getOwner() { return this.ownerName; }
	
	public void setActive(boolean value) { 
		this.active = value; 
		this.channeling = value;
		if( !value ) { DungeonDigger.ACTIVE_ABILITIES.remove(this); }
	}
	public boolean isActive() { return active; }
	
	public void setDamaging(boolean damaging) { this.damaging = damaging; }
	public boolean isDamaging() { return damaging; }
	
	public void setSpriteSheet(SpriteSheet ss) { this.spriteSheet = ss;	}
	public SpriteSheet getSpriteSheet() { return this.spriteSheet; }
	
	public void setHitFrames(SpriteSheet ss) { this.hitFrames = ss;	}
	public SpriteSheet getHitFrames() { return this.hitFrames; }
	
	public void setAnimation(Animation a) { 
		this.animation = a; 
		this.animation.setLooping(false);
	}
	public Animation getAnimation() { return this.animation; }
	
	public void setChanneling(boolean value) { this.channeling = value; }
	public boolean isChanneling() { return this.channeling; }
	
	public void setSpeed(int speed) { this.speed = speed; }
	public int getSpeed() { return this.speed; }
	
	public void setFriendly(boolean value) { this.friendly = value; }
	public boolean getFriendly() { return this.friendly; }
	
	public void setMouse(boolean value) { this.mouse = value; }
	public boolean getMouse() { return this.mouse; }
	
	public void setDeliveryMethod(AbilityDeliveryMethod adm) { this.adm = adm; }
	public AbilityDeliveryMethod getDeliveryMethod() { return this.adm; }
	
	/** Resets the animation frames and takes the first string (if there is one) as the new owner. **/
	public void reset(String... owner) {
		this.ownerName = owner[0];
		this.active = true;
		this.channeling = true;
		this.inited = false;
		DungeonDigger.ACTIVE_ABILITIES.add(this);
	}
	
	/** Returns a new copy, exact to the original **/
	public Ability clone() {
		Ability a = new Ability(this.name);
		a.active = this.active;
		a.damaging = this.damaging;
		a.channeling = this.channeling;
		a.friendly = this.friendly;
		a.mouse = this.mouse;
		a.inited = this.inited;
		a.spriteSheet = this.spriteSheet;
		a.hitFrames = this.hitFrames;
		a.animation = this.animation.copy();
		a.ownerName = this.ownerName;
		a.startPoint = this.startPoint != null ? new Point(this.startPoint.x, this.startPoint.y) : null;
		a.middlePoint = this.middlePoint != null ? new Point(this.middlePoint.x, this.middlePoint.y) : null;
		a.endPoint = this.endPoint != null ? new Point(this.endPoint.x, this.endPoint.y) : null;
		a.speed = this.speed;
		a.adm = this.adm;
		return a;
	}
}
