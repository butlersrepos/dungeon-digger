package dungeonDigger.entities;

import java.util.HashMap;

import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Vector2f;

import dungeonDigger.Enums.CreatureStat;
import dungeonDigger.entities.templates.TypeTemplate;


public abstract class Agent extends GameObject {
	protected String name;
	transient protected Ability queuedAbility;
	private SpriteSheet spriteSheet;
	private TypeTemplate typeTemplate = null;
	private HashMap<CreatureStat, Integer> stats = new HashMap<>();
	private HashMap<CreatureStat, Integer> baseStats = new HashMap<>();
	
	public Agent() {
		// Setup basic stats
		this.baseStats.put(CreatureStat.STRENGTH, 10);
		this.baseStats.put(CreatureStat.DEXTERITY, 10);
		this.baseStats.put(CreatureStat.INTELLIGENCE, 10);
		this.baseStats.put(CreatureStat.WISDOM, 10);
		this.baseStats.put(CreatureStat.CONSTITUTION, 10);
		this.baseStats.put(CreatureStat.CHARISMA, 10);
		this.baseStats.put(CreatureStat.MOVEMENT, 6);
		this.setStats(this.baseStats);
	}
	
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
	
	public TypeTemplate getTypeTemplate() {
		return typeTemplate;
	}
	public void setTypeTemplate(TypeTemplate typeTemplate, boolean... applying) {
		if( applying == null ) { applying = new boolean[]{false}; }
		if( this.getTypeTemplate() == null && !applying[0]) {
			typeTemplate.applyTo(this);
		} else if( this.getTypeTemplate() == null && applying[0] ) {
			this.typeTemplate = typeTemplate;
		}
	}

	public HashMap<CreatureStat, Integer> getStats() {
		return stats;
	}
	public void setStats(HashMap<CreatureStat, Integer> stats) {
		this.stats = stats;
	}
	
	public HashMap<CreatureStat, Integer> getBaseStats() {
		return baseStats;
	}
}
