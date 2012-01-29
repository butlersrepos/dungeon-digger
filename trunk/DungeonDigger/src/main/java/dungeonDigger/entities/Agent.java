package dungeonDigger.entities;

import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;


public abstract class Agent extends GameObject {
	protected String name;
	transient protected Ability queuedAbility;
	private SpriteSheet spriteSheet;
	
	public void setName(String name) { this.name = name; }
	public String getName() { return name; }
	
	public void setQueuedAbility(Ability ability) { this.queuedAbility = ability; }
	public Ability getQueuedAbility() { return this.queuedAbility; }
	
	public Vector2f getCenterPoint() {
		return new Vector2f( this.getCollisionBox().getCenterX(), this.getCollisionBox().getCenterY());
	}
	
	public SpriteSheet getSpriteSheet() {
		return spriteSheet;
	}
	public void setSpriteSheet(SpriteSheet spriteSheet) {
		this.spriteSheet = spriteSheet;
	}
}
