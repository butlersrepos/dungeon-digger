package dungeonDigger.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import dungeonDigger.gameFlow.DungeonDigger;

public class AbilityFactory {
	private static HashMap<String, Vector<Ability>> storedAbilities = new HashMap<>();
	// TODO: Find good numbers of clones for each spell to pre-load, ie more Turrets than fireballs?
	
	public void init() {
		for( Map.Entry<String, Ability> me : DungeonDigger.ABILITY_TEMPLATES.entrySet() ) {
			Vector<Ability> temp = new Vector<>();
			temp.add(me.getValue().clone());
			storedAbilities.put(me.getKey(), temp);
		}
	}
	
	public Ability use(String abilityName, String ownerName) {
		if( abilityName.equals("empty") ) { return null; }
		for( Ability a : storedAbilities.get(abilityName) ) {
			if( !a.isActive() ) {
				a.reset(ownerName);
				return a;
			}
		}
		Ability b = DungeonDigger.ABILITY_TEMPLATES.get(abilityName).clone();
		storedAbilities.get(abilityName).add(b);
		b.reset(ownerName);
		return b;
	}
}
