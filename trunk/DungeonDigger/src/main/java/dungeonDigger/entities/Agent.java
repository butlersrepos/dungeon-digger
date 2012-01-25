package dungeonDigger.entities;

import org.newdawn.slick.geom.Vector2f;

public abstract class Agent extends GameObject {
	protected String name;
	transient protected Ability queuedAbility;
	
	public void setName(String name) { this.name = name; }
	public String getName() { return name; }
	
	public void setQueuedAbility(Ability ability) { this.queuedAbility = ability; }
	public Ability getQueuedAbility() { return this.queuedAbility; }
}
