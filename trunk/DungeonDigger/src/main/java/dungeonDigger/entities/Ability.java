package dungeonDigger.entities;

import java.util.ArrayList;
import java.util.logging.Level;

import org.newdawn.slick.Animation;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Line;
import org.newdawn.slick.geom.Vector2f;

import dungeonDigger.Enums.AbilityDeliveryMethod;
import dungeonDigger.Enums.Direction;
import dungeonDigger.Tools.References;
import dungeonDigger.Tools.Toolbox;
import dungeonDigger.collisions.QuadCollisionEngine;
import dungeonDigger.gameFlow.MultiplayerDungeon;

public class Ability extends GameObject {
	public static Ability EMPTY_ABILITY = new Ability("Empty") {
		@Override
		public void setActive(boolean value) { }
		@Override
		public void setChanneling(boolean value) { }
	};
	private String name;
	private boolean active, damaging, channeling, friendly, mouse, inited, waitForClick;
	private SpriteSheet spriteSheet, hitFrames;
	private Animation animation;
	private Agent owner;
	private Vector2f startPoint = new Vector2f(), middlePoint, endPoint;
	private int speed;
	private AbilityDeliveryMethod adm;
	transient double distance = -1;
	transient double intervals = 0;
	transient int step = 0;
	transient Vector2f collisionPoint = null;
	transient boolean collided = false;
	
	public Ability(String name) { this.name = name; }
	
	// Setup animation and such
	public void init() {
		References.log.info("Ability inited");
		if( adm == AbilityDeliveryMethod.CLICK_PROJECTILE || adm == AbilityDeliveryMethod.MOUSE_CONE || adm == AbilityDeliveryMethod.BLAST ) {
			References.log.info("Setting waitForClick");
			waitForClick = true;
		} else { 
			active = true;
		}
		startPoint.x = this.owner.getCenterPoint().x;
		startPoint.y = this.owner.getCenterPoint().y;
		this.getPosition().x = startPoint.x;
		this.getPosition().y = startPoint.y;
		animation.setSpeed(1);
		animation.restart();
		this.setCollisionBox(this.getPosition().x, this.getPosition().y, 
								this.getAnimation().getCurrentFrame().getWidth(), 
								this.getAnimation().getCurrentFrame().getHeight());
		inited = true;
	}
	 
	// Where the magick happens, lawl...
	@Override
	public void update(GameContainer container, int delta) {
		if( !inited ) { init(); }
		if( !active ) { 
			startPoint.x = this.owner.getCenterPoint().x;
			startPoint.y = this.owner.getCenterPoint().y;
			return; 
		}
		
		animation.update(delta);
		this.setCollisionBox(this.getPosition().x, this.getPosition().y, 
								this.getAnimation().getCurrentFrame().getWidth(), 
								this.getAnimation().getCurrentFrame().getHeight());
		calculateMovement();
		
		if( this.animation.isStopped() ) { 
			References.log.fine("<==ABILITY==> Animation stopped.");
			active = false; 
			inited = false;
		}
	}
	
	@Override
	public void render(GameContainer container, Graphics g) {
		Input inputs = container.getInput();
		if( !active ) { 
			if( waitForClick ) {
				// draw aimer thingy, spinning
				References.IMAGES.get("magicReticle").setRotation( References.IMAGES.get("magicReticle").getRotation() + 5 );
				References.IMAGES.get("magicReticle").draw(
						References.myCharacter.getPosition().x - container.getWidth()/2 + inputs.getMouseX() - References.IMAGES.get("magicReticle").getWidth()/2, 
						References.myCharacter.getPosition().y - container.getHeight()/2 + inputs.getMouseY() - References.IMAGES.get("magicReticle").getHeight()/2);
			}
			return; 
		}
		animation.draw(this.getPosition().x - animation.getWidth()/2, this.getPosition().y - animation.getHeight()/2);
	}
	
	public void calculateMovement() {
		if( step > intervals ) {
			References.log.fine("Steps > Intervals, stopping");
			distance = -1;
			step = 0;
			animation.stop();
			if( collided ) {
				// TODO: explosion, secondary animation etc?
			}
			return;
		}
		if( distance == -1 ) { 
			References.log.fine("Setting up pathing items");
			distance = Toolbox.distanceBetween(startPoint, endPoint); 
			intervals = distance / speed;
			References.log.fine("Distance: " + distance + " Intervals: " + intervals);
			step = 0;
		}
		int newX = (int)(startPoint.x + (endPoint.x - startPoint.x) * (step / intervals)); 
		int newY = (int)(startPoint.y + (endPoint.y - startPoint.y) * (step / intervals)); 

		//Check terrain
		/*Line path = new Line(this.getPosition().copy(), new Vector2f(newX, newY));
		Direction dir = Toolbox.getCardinalDirection(path);
		collisionPoint = null;
		for( int row = 0; row != dir.adjY(); row += dir.adjY() ) {
			for( int col = 0; col != dir.adjX(); col += dir.adjX() ) {
				if( !MultiplayerDungeon.CLIENT_VIEW.dungeon[row][col].isPassable() ) {
					if( path.intersects(MultiplayerDungeon.CLIENT_VIEW.dungeon[row][col].getCollisionBox() ) ) {
						collisionPoint = Toolbox.lineIntersectsRectangle(path, MultiplayerDungeon.CLIENT_VIEW.dungeon[row][col].getCollisionBox());
						System.out.println("HIT A WALL!");
						collided = true;
						break;
					}
				}
			}
			if( collided ) { break; }
		}*/ 
		// Move
		if( collided ) {
			this.setPosition(this.collisionPoint.copy());
			collisionPoint = null;
			step = (int)Math.ceil(intervals);
		} else {
			this.getPosition().x = newX;
			this.getPosition().y = newY;
		}
		// Repopulate Quads
		QuadCollisionEngine.relocate(this);
		// Check collisions			
		ArrayList<GameObject> obstacles = QuadCollisionEngine.checkCollisions(this);
		if( obstacles != null ) {
			this.handleCollisions(obstacles);
		}
		
		step++;
	}
	
	private void handleCollisions( ArrayList<GameObject> objects ) {
		References.log.fine("<==ABILITY==> Handling collisions with " + objects.size() + " objects.");
		for( GameObject g : objects ) {
			if( !this.isActive() ) { break; }
			if( g instanceof Mob ) {
				Mob m = (Mob)g;
				References.log.fine("\n\n\n\n\n\n\n\n\n");
				References.log.fine(this.getName() + " COLLIDED WITH A MOB - " + m.getName());
				if( m.exists() ) { m.die(); }
				this.end();
			} else if( g instanceof NetworkPlayer ) {
				NetworkPlayer p = (NetworkPlayer)g;
				References.log.fine("\n\n\n\n\n\n\n\n\n");
				References.log.fine(this.getName() + " COLLIDED WITH A PLAYER - " + p.getName());
			} else if( g instanceof Ability ) {
				Ability a = (Ability)g;
				References.log.fine("\n\n\n\n\n\n\n\n\n");
				References.log.fine(this.getName() + " COLLIDED WITH AN ABILITY - " + a.getName());
			}
		}
	}
	
	public void end() {
		References.log.fine(this.name + ": I am ending with owner: " + this.owner.getName());
		this.owner = null;
		this.active = false;
		this.channeling = false;
		this.waitForClick = false;
		this.step = 0;
		this.distance = -1;
	}
	
	/* GETTERS AND SETTERS AND BORING STUFF BELOW HERE  */
	@Override
	public float getWidth() {
		return this.animation.getCurrentFrame().getWidth();
	}	
	@Override
	public float getHeight() {
		return this.animation.getCurrentFrame().getHeight();
	}
	public void setStartPoint(Vector2f p) { this.startPoint = p; }
	public void setStartPoint(float x, float y) { this.startPoint = new Vector2f(x, y); }
	public Vector2f getStartPoint() { return this.startPoint; }
	
	public void setMiddlePoint(Vector2f p) { this.middlePoint = p; }
	public void setMiddlePoint(float x, float y) { this.middlePoint = new Vector2f( x, y); }
	public Vector2f getMiddlePoint() { return this.middlePoint; }
	
	public void setEndPoint(Vector2f p) { this.endPoint = p; }
	public void setEndPoint(float x, float y) { this.endPoint = new Vector2f(x, y); }
	public Vector2f getEndPoint() { return this.endPoint; }
	
	public void setCurrentPoint(Vector2f p) { this.setPosition(p.copy()); }
	public void setCurrentPoint(float x, float y) { this.setPosition(x, y); }
	public Vector2f getCurrentPoint() { return this.getPosition(); }
	
	public void setName(String name) { this.name = name; }
	public String getName() { return this.name; }
	
	public void setOwner(Agent owner) { this.owner = owner; }
	public Agent getOwner() { return this.owner; }
	
	public void setActive(boolean value) { 
		this.active = value; 
		this.channeling = value;
		if( !value ) {References.ACTIVE_ABILITIES.remove(this); }
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
		this.animation.setLooping(true);
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

	public boolean isWaitingForClick() { return waitForClick; }
	public void setWaitingForClick( boolean b ) { waitForClick = b; }
	
	/** Resets the animation frames and takes the first string (if there is one) as the new owner. **/
	public void reset(Agent owner) {
		References.log.fine(this.name + ": I am reset with owner: " + owner.getName());
		this.owner = owner;
		this.active = false;
		this.channeling = true;
		this.init();
		if( this.owner.getQueuedAbility() != null ) {
			this.owner.getQueuedAbility().setActive(false);
			this.owner.getQueuedAbility().setWaitingForClick(false);
		}
		this.owner.setQueuedAbility(this);
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
		a.owner = this.owner;
		a.startPoint = this.startPoint != null ? new Vector2f(this.startPoint.x, this.startPoint.y) : null;
		a.middlePoint = this.middlePoint != null ? new Vector2f(this.middlePoint.x, this.middlePoint.y) : null;
		a.endPoint = this.endPoint != null ? new Vector2f(this.endPoint.x, this.endPoint.y) : null;
		a.speed = this.speed;
		a.adm = this.adm;
		return a;
	}

}
