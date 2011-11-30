package dungeonDigger.contentGeneration;

import org.newdawn.slick.geom.Vector2f;

public class Toolbox {
	public static float distanceBetween(Vector2f v1, Vector2f v2){
		return (float) Math.sqrt( ((v1.x - v2.x)*(v1.x - v2.x)) + ((v1.y - v2.y)*(v1.y - v2.y)) );
	}
	public static Vector2f directionFrom1To2(Vector2f v1, Vector2f v2){
		return new Vector2f(v2.x - v1.x, v2.y - v1.y);
	}
}
