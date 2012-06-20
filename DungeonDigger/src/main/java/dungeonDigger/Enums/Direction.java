package dungeonDigger.Enums;

public enum Direction {
	NORTH (0, -1, 270f), 
	NORTHEAST (1, -1, 315f), 
	EAST (1, 0, 0f), 
	SOUTHEAST (1, 1, 45f), 
	SOUTH (0, 1, 90f), 
	SOUTHWEST (-1, 1, 135f), 
	WEST (-1, 0, 180f), 
	NORTHWEST (-1, -1, 225f),
	NONE (0, 0, 0f);
	
	private final int adjX, adjY;
	private final float abilityRotation;
	Direction(int x, int y, float abRot) {
		this.adjX = x;
		this.adjY = y;
		this.abilityRotation = abRot;
	}
	
	public int adjX() {
		return this.adjX;
	}
	public int adjY() {
		return this.adjY;
	}
	public float abilityRotation() {
		return this.abilityRotation;
	}
}
