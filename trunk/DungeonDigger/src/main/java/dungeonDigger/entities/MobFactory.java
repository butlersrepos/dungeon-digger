package dungeonDigger.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.newdawn.slick.geom.Vector2f;

import dungeonDigger.gameFlow.DungeonDigger;

public class MobFactory {
	private static HashMap<String, Vector<Mob>> storedMobs = new HashMap<>();
	// TODO: Find good numbers of clones for each enemy to pre-load, ie more zombies than demons?
	
	public void init() {
		for( Map.Entry<String, Mob> me : DungeonDigger.MOB_TEMPLATES.entrySet() ) {
			Vector<Mob> temp = new Vector<>();
			temp.add(me.getValue().clone());
			storedMobs.put(me.getKey(), temp);
		}
	}
	
	public Mob spawn(String mobName, Vector2f pos) {
		if( mobName.equals("empty") ) { return null; }
		for( Mob m : storedMobs.get(mobName) ) {
			if( !m.exists() ) {
				System.out.println("Found mob: " + m.getName() + " and spawning it.");
				m.spawn(pos);
				return m;
			}
		}
		System.out.println("No cached mob found, creating a " + mobName);
		Mob b = DungeonDigger.MOB_TEMPLATES.get(mobName).clone();
		storedMobs.get(mobName).add(b);
		b.spawn(pos);
		return b;
	}
}
