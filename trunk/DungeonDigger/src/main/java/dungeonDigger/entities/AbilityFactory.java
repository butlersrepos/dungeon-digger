package dungeonDigger.entities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.state.StateBasedGame;

import dungeonDigger.Tools.References;
import dungeonDigger.collisions.QuadCollisionEngine;
import dungeonDigger.contentGeneration.DungeonGenerator;
import dungeonDigger.gameFlow.DungeonDigger;

public class AbilityFactory {
	private static HashMap<String, Vector<Ability>> storedAbilities = new HashMap<>();
	private static HashMap<String, Vector<Ability>> activeAbilities = new HashMap<>();
	// TODO: Find good numbers of clones for each spell to pre-load, ie more Turrets than fireballs?
	
	public void init() {
		for( Map.Entry<String, Ability> me : References.ABILITY_TEMPLATES.entrySet() ) {
			Vector<Ability> temp = new Vector<Ability>();
			temp.add(me.getValue().clone());
			storedAbilities.put(me.getKey(), temp);
			activeAbilities.put(me.getKey(), new Vector<Ability>());
		}
	}
	
	public void update(GameContainer container, StateBasedGame game, int delta) {
		for( String name : activeAbilities.keySet() ) {
			for( Ability a : activeAbilities.get(name) ) {
				a.update(container, delta);
			}
		}
	}
	
	public void render(GameContainer container, Graphics g, int col, int row) {
		for( String name : activeAbilities.keySet() ) {
			Iterator<Ability> it = activeAbilities.get(name).iterator();
			while( it.hasNext() ) {
				Ability a = it.next();
				if((int)(a.getCurrentPoint().x/DungeonGenerator.ratioCol) == col
						&& (int)(a.getCurrentPoint().y/DungeonGenerator.ratioRow) == row ) {
					a.render(container, g);
				}
				if( !a.isActive() && !a.isWaitingForClick() ) {
					storedAbilities.get(name).add(a);
					QuadCollisionEngine.removeObjectFromGame(a);
					it.remove();
				}
			}
		}
	}
	
	public void use(String abilityName, Agent owner) {
		if( abilityName.equals("empty") ) { return; }
		Iterator<Ability> it = storedAbilities.get(abilityName).iterator();
		while( it.hasNext() ) {
			Ability a = it.next();
			activeAbilities.get(abilityName).add(a);
			a.reset(owner);
			QuadCollisionEngine.addObjectToGame(a);
			it.remove();
			return;
		}
		References.log.info("No cached ability found, creating a " + abilityName);
		Ability b = References.ABILITY_TEMPLATES.get(abilityName).clone();
		activeAbilities.get(abilityName).add(b);
		b.reset(owner);
		QuadCollisionEngine.addObjectToGame(b);
	}

	public static HashMap<String, Vector<Ability>> getActiveAbilities() {
		return activeAbilities;
	}
}
