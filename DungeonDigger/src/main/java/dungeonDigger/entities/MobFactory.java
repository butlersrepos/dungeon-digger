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
import dungeonDigger.collisions.QuadCollisionEngine;
import dungeonDigger.contentGeneration.DungeonGenerator;

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
				Mob m = it.next();
				if((int)(m.getPosition().x/DungeonGenerator.ratioCol) == col
						&& (int)(m.getPosition().y/DungeonGenerator.ratioRow) == row ) {
					m.render(container, g);
				}
				if( !m.exists() ) {
					theCryoTubes.get(name).add(m);
					QuadCollisionEngine.removeObjectFromGame(m);
					it.remove();
				}
			}
		}
	}
	
	/**
	 * Spawns the named mob in the exact position (in pixels). If there is an unused mob in
	 * memory (cryotubes), then the first one is reactivated and used, otherwise a new mob object
	 * is created and placed in the world.
	 * @param mobName Examples: 'zombie'
	 * @param pos Exact position of the mob's upper-left corner of hitbox, in pixels.
	 * @return
	 */
	public Mob spawn(String mobName, Vector2f pos) {
		if( mobName.equals("empty") ) { return null; }
		for( Mob m : theCryoTubes.get(mobName) ) {
			if( !m.exists() ) {
				References.log.info("Found mob: " + m.getName() + " and spawning it.");
				m.spawn(pos);
				roamingMobs.get(mobName).add(m);
				QuadCollisionEngine.addObjectToGame(m);
				return m;
			}
		}
		References.log.info("No cached mob found, creating a " + mobName + " at position: " + pos.x + ", " + pos.y);
		Mob b = References.MOB_TEMPLATES.get(mobName).clone();
		b.spawn(pos);
		roamingMobs.get(mobName).add(b);	
		QuadCollisionEngine.addObjectToGame(b);	
		return b;
	}

	public static HashMap<String, Vector<Mob>> getRoamingMobs() {
		return roamingMobs;
	}
}
