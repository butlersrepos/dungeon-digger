package dungeonDigger.entities;

import org.newdawn.slick.Animation;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;

public class Mob extends Agent {
	private SpriteSheet sprites;
	private Animation animation;
	private Vector2f destination;
	private int currentHitPoints, maxHitPoints, speed;
	private boolean friendly = false, exists = false, inited = false;
	
	public Mob(String name) {
		this.setName(name);
	}
	
	public void init() {
		animation.setSpeed(0.3f);
		animation.restart();
		animation.setLooping(true);
		System.out.println("Mob Inited");
		inited = true;
	}
	
	@Override
	public void update(GameContainer c, int delta) {
		if( !exists() ) { return; }
		if( !inited ) { init(); }
		
		animation.update(delta);
	}

	@Override
	public void render(GameContainer c, Graphics g) {
		if( !exists() ) { return; }
		animation.draw(this.getPosition().x - animation.getWidth()/2, this.getPosition().y - animation.getHeight()/2);
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
