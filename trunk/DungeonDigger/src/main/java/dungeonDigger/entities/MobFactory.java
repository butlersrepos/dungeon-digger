package dungeonDigger.entities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.state.StateBasedGame;

import dungeonDigger.Tools.References;
import dungeonDigger.contentGeneration.DungeonGenerator;
import dungeonDigger.gameFlow.DungeonDigger;

public class MobFactory {
	private static HashMap<String, Vector<Mob>> theCryoTubes = new HashMap<>();
	private static HashMap<String, Vector<Mob>> roamingMobs = new HashMap<>();
	// TODO: Find good numbers of clones for each enemy to pre-load, ie more zombies than demons?
	
	public void init() {
		for( Map.Entry<String, Mob> me : References.MOB_TEMPLATES.entrySet() ) {
			Vector<Mob> temp = new Vector<>();
			temp.add(me.getValue().clone());
			theCryoTubes.put(me.getKey(), temp);
			roamingMobs.put(me.getKey(), new Vector<Mob>());
		}
	}
	
	public void update(GameContainer container, StateBasedGame game, int delta) {
		for( String name : roamingMobs.keySet() ) {
			for( Mob m : roamingMobs.get(name) ) {
				if( m.exists() ) {
					m.update(container, delta);
				}
			}
		}
	}
	
	public void render(GameContainer container, Graphics g, int col, int row) {
		for( String name : roamingMobs.keySet() ) {
			Iterator<Mob> it = roamingMobs.get(name).iterator();
			while( it.hasNext() ) {
				Mob a = it.next();
				if((int)(a.getPosition().x/DungeonGenerator.ratioCol) == col
						&& (int)(a.getPosition().y/DungeonGenerator.ratioRow) == row ) {
					a.render(container, g);
				}
				if( !a.exists() ) {
					theCryoTubes.get(name).add(a);
					it.remove();
				}
			}
		}
	}
	
	public Mob spawn(String mobName, Vector2f pos) {
		if( mobName.equals("empty") ) { return null; }
		for( Mob m : theCryoTubes.get(mobName) ) {
			if( !m.exists() ) {
				System.out.println("Found mob: " + m.getName() + " and spawning it.");
				m.spawn(pos);
				roamingMobs.get(mobName).add(m);
				return m;
			}
		}
		System.out.println("No cached mob found, creating a " + mobName);
		Mob b = References.MOB_TEMPLATES.get(mobName).clone();
		b.spawn(pos);
		roamingMobs.get(mobName).add(b);		
		return b;
	}

	public static HashMap<String, Vector<Mob>> getRoamingMobs() {
		return roamingMobs;
	}
}
