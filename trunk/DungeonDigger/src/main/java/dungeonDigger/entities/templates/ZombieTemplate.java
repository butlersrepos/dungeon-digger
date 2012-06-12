package dungeonDigger.entities.templates;

import java.util.logging.Level;

import dungeonDigger.Enums.CreatureStat;
import dungeonDigger.Tools.References;
import dungeonDigger.entities.Agent;
import dungeonDigger.entities.NetworkPlayer;

public class ZombieTemplate extends TypeTemplate {
	public static int ZOMBIE_INT = 3;
	public static int ZOMBIE_WIS = 3;
	public static int ZOMBIE_CHA = 3;
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
		References.log.entering("ZombieTemplate", "applyTo");
		References.log.info("Host is " + host + "log level is: " + References.log.getLevel());
		// Transform their stats into zombitizied stats
		if( host.getTypeTemplate() == null ) {
			host.setTypeTemplate(this, true);
			this.setHost(host);
			host.getStats().put(CreatureStat.STRENGTH, newStr(host.getBaseStats().get(CreatureStat.STRENGTH)));
			host.getStats().put(CreatureStat.DEXTERITY, newDex(host.getBaseStats().get(CreatureStat.DEXTERITY)));
			host.getStats().put(CreatureStat.CONSTITUTION, newCon(host.getBaseStats().get(CreatureStat.CONSTITUTION)));
			host.getStats().put(CreatureStat.CHARISMA, newCha());
			host.getStats().put(CreatureStat.INTELLIGENCE, newInt());
			host.getStats().put(CreatureStat.WISDOM, newWis());
			host.getStats().put(CreatureStat.MOVEMENT, newMovement(host.getBaseStats().get(CreatureStat.STRENGTH)));
		}
		References.log.exiting("ZombieTemplate", "applyTo");
	}
	
	public int newStr(int oldStr) {
		return oldStr + 4;
	}
	public int newDex(int oldStat) {
		return Math.max(oldStat - 4, 3);
	}
	public int newCon(int oldStat) {
		return oldStat + 4;
	}
	public int newInt() {
		return ZOMBIE_INT;
	}
	public int newWis() {
		return ZOMBIE_WIS;
	}
	public int newCha() {
		return ZOMBIE_CHA;
	}
	public int newMovement(int oldStat) {
		if( this.getHost() instanceof NetworkPlayer ) {
			return (int)Math.max(Math.floor(oldStat / 2), 1);
		}
		return 1;
	}
}
