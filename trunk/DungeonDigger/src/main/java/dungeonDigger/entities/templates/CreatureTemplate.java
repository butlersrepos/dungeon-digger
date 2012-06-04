package dungeonDigger.entities.templates;

import dungeonDigger.entities.Agent;

public abstract class CreatureTemplate {
	public abstract void invoke(Agent host);
	public abstract void applyTo(Agent host);
}
