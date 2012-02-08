package dungeonDigger.Tools;

import java.awt.Point;

import org.newdawn.slick.geom.Line;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;

import dungeonDigger.Enums.Direction;

public class Toolbox {
	public static double distanceBetween(Point p1, Point p2){
		return Math.sqrt( ((p1.x - p2.x)*(p1.x - p2.x)) + ((p1.y - p2.y)*(p1.y - p2.y)) );
	}
	public static float distanceBetween(Vector2f v1, Vector2f v2){
		return (float) Math.sqrt( ((v1.x - v2.x)*(v1.x - v2.x)) + ((v1.y - v2.y)*(v1.y - v2.y)) );
	}
	
	/** Use for comparisons of magnitude, not precision.
	 * @return Distance Squared */
	public static float euclideanDistanceSq2D(float x1, float y1, float x2, float y2) {
		float dx = x1-x2;
		float dy = y1-y2; 

		return dx*dx + dy*dy;
	}
	/** Use for comparisons of magnitude, not precision.
	 * @return Distance Squared */
	public static float euclideanDistanceSq2D(Vector2f v1, Vector2f v2) {
		if( v1 == null || v2 == null ) { return Float.MAX_VALUE; }
		float dx = v1.x-v2.x;
		float dy = v1.y-v2.y; 

		return dx*dx + dy*dy;
	}
	
	public static Vector2f directionFrom1To2(Vector2f v1, Vector2f v2){
		return new Vector2f(v2.x - v1.x, v2.y - v1.y);
	}
	public static Direction getCardinalDirection(float xOrigin, float yOrigin, float xDestination, float yDestination) {
		if( xOrigin == xDestination && yOrigin == yDestination ) { return Direction.NONE; }
		float ETW = Math.signum(xDestination - xOrigin);
		float NTS = Math.signum(yDestination - yOrigin);
		String d = "";
		
		if( NTS < 0 ) { d += "NORTH"; }
		else if( NTS > 0 ) { d += "SOUTH"; }
		
		if( ETW > 0 ) { d += "EAST"; }
		else if( ETW < 0 ) { d += "WEST"; }
		
		return Direction.valueOf(d);
	}
	public static Direction getCardinalDirection(Line line) {
		return getCardinalDirection(line.getStart().x, line.getStart().y, line.getEnd().x, line.getEnd().y);
	}
	public static Direction getCardinalDirection(Vector2f origin, Vector2f destination) {
		return getCardinalDirection(origin.x, origin.y, destination.x, destination.y);
	}
	/** Calculates the point where two lines intersect.
	 **/
	public static Vector2f lineIntersectsLine(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
		float denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
		if (denom == 0.0) { // Lines are parallel.
			return null;
		}
		float ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3))/denom;
		float ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3))/denom;
		if (ua >= 0.0f && ua <= 1.0f && ub >= 0.0f && ub <= 1.0f) {
			// Get the intersection point.
			return new Vector2f((x1 + ua*(x2 - x1)), (y1 + ua*(y2 - y1)));
		}
		return null;
	}
	
	public static Vector2f lineIntersectsRectangle(Line line, Rectangle r) {
		Line rLeft, rTop, rBottom, rRight;
		rLeft = new Line(r.getMinX(), r.getMinY(), r.getMinX(), r.getMaxY());
		rTop = new Line(r.getMinX(), r.getMinY(), r.getMaxX(), r.getMinY());
		rBottom = new Line(r.getMinX(), r.getMaxY(), r.getMaxX(), r.getMaxY());
		rRight = new Line(r.getMaxX(), r.getMinY(), r.getMaxX(), r.getMaxY());
		Vector2f leftP = lineIntersectsLine(line.getStart().x, line.getStart().y, line.getEnd().x, line.getEnd().y, 
												rLeft.getStart().x, rLeft.getStart().y, rLeft.getEnd().x, rLeft.getEnd().y);
		Vector2f rightP = lineIntersectsLine(line.getStart().x, line.getStart().y, line.getEnd().x, line.getEnd().y, 
												rRight.getStart().x, rRight.getStart().y, rRight.getEnd().x, rRight.getEnd().y);
		Vector2f topP = lineIntersectsLine(line.getStart().x, line.getStart().y, line.getEnd().x, line.getEnd().y, 
												rTop.getStart().x, rTop.getStart().y, rTop.getEnd().x, rTop.getEnd().y);
		Vector2f bottomP = lineIntersectsLine(line.getStart().x, line.getStart().y, line.getEnd().x, line.getEnd().y, 
												rBottom.getStart().x, rBottom.getStart().y, rBottom.getEnd().x, rBottom.getEnd().y);
		float distLeft = leftP == null ? Float.MAX_VALUE : euclideanDistanceSq2D(line.getStart(), leftP);
		float distRight = rightP == null ? Float.MAX_VALUE : euclideanDistanceSq2D(line.getStart(), rightP);
		float distTop = topP == null ? Float.MAX_VALUE : euclideanDistanceSq2D(line.getStart(), topP);
		float distBottom = bottomP == null ? Float.MAX_VALUE : euclideanDistanceSq2D(line.getStart(), bottomP);

		float smallest = distLeft;
		smallest = Math.min(smallest, distRight);
		smallest = Math.min(smallest, distTop);
		smallest = Math.min(smallest, distBottom);
		
		if( smallest == distLeft ) { return leftP; }
		if( smallest == distRight ) { return rightP; }
		if( smallest == distTop ) { return topP; }
		if( smallest == distBottom ) { return bottomP; }
		return null;
	}
}
