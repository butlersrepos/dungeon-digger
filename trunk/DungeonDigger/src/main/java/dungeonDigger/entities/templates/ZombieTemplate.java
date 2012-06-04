package dungeonDigger.entities.templates;

import dungeonDigger.entities.Agent;

public class ZombieTemplate extends CreatureTemplate {
	private float strengthChange = 1.30f;
	private float speedChange = 0.65f;
	private float hpChange = 2f;
	private float intelligenceChange = 0.1f; 
	@Override
	public void invoke(Agent host) {
		// TODO: increment hunger
		
	}
	@Override
	public void applyTo(Agent host) {
		// TODO: make them into a zombie if they aren't already
		
	}
}
