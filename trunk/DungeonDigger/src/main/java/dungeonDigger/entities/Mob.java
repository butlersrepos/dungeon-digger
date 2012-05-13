package dungeonDigger.entities;

import org.newdawn.slick.Animation;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.ShapeRenderer;
import org.newdawn.slick.geom.Vector2f;

import dungeonDigger.Enums.Direction;
import dungeonDigger.Tools.References;
import dungeonDigger.Tools.Toolbox;
import dungeonDigger.contentGeneration.DungeonGenerator;

public class Mob extends Agent {
	private SpriteSheet sprites;
	private Animation animation;
	private Vector2f destination;
	private int currentHitPoints, maxHitPoints, speed;
	private boolean friendly = false, exists = false, inited = false;
	private transient float movementVariance = 2f;
	public Mob(String name) {
		this.setName(name);
	}
	
	public void init() {
		animation.setSpeed(0.3f);
		animation.restart();
		animation.setLooping(true);
		this.setCollisionBox(this.getPosition().x, this.getPosition().y, 
				this.getAnimation().getCurrentFrame().getWidth(), 
				this.getAnimation().getCurrentFrame().getHeight());
		References.log.info("Mob Inited");
		inited = true;
	}
	
	@Override
	public void update(GameContainer c, int delta) {
		if( !exists() ) { return; }
		if( !inited ) { init(); }
		
		animation.update(delta);
		
		// Basic stupid zombie movement
		// Get the signum directions toward the player
		float xMove = Math.signum(References.myCharacter.getPosition().x - this.getPosition().x);
		float yMove = Math.signum((References.myCharacter.getPosition().y - (Math.max(0,this.getAnimation().getCurrentFrame().getHeight() - References.myCharacter.getHeight()))) - this.getPosition().y);
		// Calculate a random magnitude to add (for zombies 0-2)
		int stepVariance = Math.round((float)Math.random() * movementVariance);
		// Increase our directional magnitude by that much magnitude, maintaining the directionality
		xMove += stepVariance * Math.signum(xMove);
		yMove += stepVariance * Math.signum(yMove);
		//System.out.println("\n\n\n\n\n\n\nxmove = " + xMove + "\tyMove = " + yMove);
		
		int canX = References.CLIENT_VIEW.canMove(Toolbox.getCardinalDirection(xMove, 0), this.getCollisionBox(), Math.abs(xMove));
		if( canX > 0 ) {
			this.setPosition(this.getPosition().x + canX*Math.signum(xMove), this.getPosition().y);
			this.setCollisionBox(this.getPosition().x, this.getPosition().y, 
					this.getAnimation().getCurrentFrame().getWidth(), 
					this.getAnimation().getCurrentFrame().getHeight());
		}
		int canY = References.CLIENT_VIEW.canMove(Toolbox.getCardinalDirection(0, yMove), this.getCollisionBox(), Math.abs(yMove));
		if( canY > 0 ) {
			this.setPosition(this.getPosition().x, this.getPosition().y + canY*Math.signum(yMove));
			this.setCollisionBox(this.getPosition().x, this.getPosition().y, 
					this.getAnimation().getCurrentFrame().getWidth(), 
					this.getAnimation().getCurrentFrame().getHeight());
		}
		
		this.setCollisionBox(this.getPosition().x, this.getPosition().y, 
				this.getAnimation().getCurrentFrame().getWidth(), 
				this.getAnimation().getCurrentFrame().getHeight());
	}

	@Override
	public void render(GameContainer c, Graphics g) {
		if( !exists() ) { return; }
		animation.draw(this.getPosition().x, this.getPosition().y);
		//ShapeRenderer.draw(this.getCollisionBox());
	}
	
	@Override
	public Rectangle getCollisionBox() {
		return new Rectangle( this.getPosition().x, this.getPosition().y, 
							this.animation.getCurrentFrame().getWidth(), this.animation.getCurrentFrame().getHeight() );
	}

	@Override
	public float getWidth() {
		return this.animation.getCurrentFrame().getWidth();
	}
	
	@Override
	public float getHeight() {
		return this.animation.getCurrentFrame().getHeight();
	}
	
	public void spawn(Vector2f pos) {
		this.setPosition(pos.copy());
		setExists(true);
		this.currentHitPoints = this.maxHitPoints;
	}
	
	public void die() {
		setExists(false);
	}
	
	public Mob clone() {
		Mob m = new Mob(this.name);
		m.setAnimation(this.animation.copy());
		m.setFriendly(this.friendly);
		m.setMaxHitPoints(this.maxHitPoints);
		m.setCurrentHitPoints(this.maxHitPoints);
		m.setSpeed(this.speed);
		m.setSprites(this.sprites);
		return m;
	}
	
	public SpriteSheet getSprites() {
		return sprites;
	}

	public void setSprites(SpriteSheet sprites) {
		this.sprites = sprites;
	}

	public Animation getAnimation() {
		return animation;
	}

	public void setAnimation(Animation animation) {
		this.animation = animation;
	}

	public Vector2f getDestination() {
		return destination;
	}

	public void setDestination(Vector2f destination) {
		this.destination = destination;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public boolean isFriendly() {
		return friendly;
	}

	public void setFriendly(boolean friendly) {
		this.friendly = friendly;
	}
	public int getCurrentHitPoints() {
		return currentHitPoints;
	}

	public void setCurrentHitPoints(int currentHitPoints) {
		this.currentHitPoints = currentHitPoints;
	}

	public int getMaxHitPoints() {
		return maxHitPoints;
	}

	public void setMaxHitPoints(int maxHitPoints) {
		this.maxHitPoints = maxHitPoints;
	}

	public boolean exists() {
		return exists;
	}
	
	public void setExists(boolean b) {
		this.exists = b;
	}
}
