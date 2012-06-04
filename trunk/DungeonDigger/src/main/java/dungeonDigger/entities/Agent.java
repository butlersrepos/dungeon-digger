package dungeonDigger.entities;

import java.util.List;
import java.util.Vector;

import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;

import dungeonDigger.entities.templates.CreatureTemplate;


public abstract class Agent extends GameObject {
	protected String name;
	transient protected Ability queuedAbility;
	private SpriteSheet spriteSheet;
	private Vector<CreatureTemplate> templates = new Vector<CreatureTemplate>();
	
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
	
	public void setTemplates(Vector<CreatureTemplate> templates) {
		this.templates = templates;
	}
	public List<CreatureTemplate> getTemplates() {
		return templates;
	}
	public boolean addTemplate(CreatureTemplate template) {
		// TODO: check if template already exists, allow some to multi-apply?
		return this.templates.add(template);
	}
	public void clearTemplates() {
		this.templates.clear();
	}
}
