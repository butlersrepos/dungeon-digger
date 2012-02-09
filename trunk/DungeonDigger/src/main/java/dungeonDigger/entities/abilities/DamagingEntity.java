package dungeonDigger.entities.abilities;

import dungeonDigger.entities.GameObject;

public interface DamagingEntity {
	public abstract void dealDamage(GameObject... objs);
}
