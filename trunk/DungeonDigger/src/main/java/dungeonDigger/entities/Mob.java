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
	private int currentHitPoints, maxHitPoints, speed, intelligence = 1;
	private boolean friendly = false, exists = false, inited = false;
	private transient float movementVariance = 2f;
	private transient int aggroRange = 750;
	// Out of 1000 being 100%
	private int hunger = 0;
	private transient int hungerTimer = 0;
	
	public Mob(String name) {
		this.setName(name);
	}
	
	public void init() {
		// reset hunger level
		setHunger(0);
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
		hungerTimer += delta;
		if( hungerTimer > 1000 ) {
			hungerTimer = 0;
			hunger = hunger >= 1000 ? 1000 : hunger + 1;
		}
		
		animation.update(delta);
		
		
		this.getPosition().add( MobAI.updateMovement(this) );
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

	public float getMovementVariance() {
		return movementVariance;
	}

	public void setMovementVariance(float movementVariance) {
		this.movementVariance = movementVariance;
	}

	public int getAggroRange() {
		return aggroRange;
	}

	public void setAggroRange(int aggroRange) {
		this.aggroRange = aggroRange;
	}

	public int getIntelligence() {
		return intelligence;
	}

	public void setIntelligence(int intelligence) {
		this.intelligence = intelligence;
	}

	public void setHunger(int hunger) {
		this.hunger = hunger;
	}

	public int getHunger() {
		return hunger;
	}
	// TODO: make stats into objects? return a map of the bonuses the zombie gets at that level of hunger, spd, str, etc.
	public Map<Stats> getHungerFactor() {
		double factor = 0;
		switch(hunger) {
		
		}
		return factor;
	}
}
