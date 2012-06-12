package dungeonDigger.entities.templates;

import dungeonDigger.entities.Agent;

public class ZombieTemplate extends CreatureTemplate {
	private float strengthChange = 1.30f;
	private float speedChange = 0.65f;
	private float hpChange = 2f;
	private float intelligenceChange = 0.1f; 
	// Increment time period, 1s base per tick
	private int hungerTickTime = 1000;	
	// Time since last tick
	private int hungerTimer = 0;
	// Amount of hunger to gain per time period
	private int hungerTickAmount = 1;
	// Max hunger is 1000 == 100%
	private int hunger = 0;
	
	public void update(int delta) {
		hungerTimer += delta;
		if( hungerTimer >= hungerTickTime ) {
			hunger = hunger >= 1000 ? 1000 : hunger + hungerTickAmount;
			hungerTimer = 0;
		}
	}

	@Override
	public void applyTo(Agent host) {
		// TODO: make them into a zombie if they aren't already
		this.setHost(host);
	}
}
