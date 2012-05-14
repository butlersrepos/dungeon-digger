package dungeonDigger.entities;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Line;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;

import dungeonDigger.Tools.Constants;
import dungeonDigger.Tools.References;
import dungeonDigger.collisions.QuadCollisionEngine;

public abstract class GameObject {
	/** Actual pixel measurements **/
	private Vector2f			position		= new Vector2f();
	private Rectangle			collisionBox	= new Rectangle(0, 0, 0, 0);
	private QuadCollisionEngine	parentNode		= null;

	public abstract void update(GameContainer c, int d);

	public abstract void render(GameContainer c, Graphics g);

	public abstract float getWidth();

	public abstract float getHeight();

	public boolean hasLOS(GameObject target) {
		Line line = new Line(target.getPosition().x, target.getPosition().y, this.getPosition().x, this.getPosition().y);
		int top, bottom, left, right;
		top = (int)(Math.min(this.getPosition().y, target.getPosition().y) / References.CLIENT_VIEW.ratioRow);
		bottom = (int)(Math.max(this.getPosition().y, target.getPosition().y) / References.CLIENT_VIEW.ratioRow);
		left = (int)(Math.min(this.getPosition().x, target.getPosition().x) / References.CLIENT_VIEW.ratioCol);
		right = (int)(Math.max(this.getPosition().x, target.getPosition().x) / References.CLIENT_VIEW.ratioCol);
		
		for(int col = left; col <= right; col++) {
			for(int row = top; row <= bottom; row++) {
				if( line.intersects(References.CLIENT_VIEW.dungeon[row][col].getCollisionBox()) && References.CLIENT_VIEW.dungeon[row][col].isTileLetter('W') ) {
					return false;
				}
			}
		}
		return true;
	}
	
	/** @return the position */
	public Vector2f getPosition() {
		return this.position;
	}

	/** @param position the position to set */
	public void setPosition(Vector2f position) {
		this.position = position;
	}

	public void setPosition(float x, float y) {
		this.position.x = x;
		this.position.y = y;
		this.getCollisionBox().setX(x);
		this.getCollisionBox().setY(y);
	}

	/** @return the collisionBox */
	public Rectangle getCollisionBox() {
		return this.collisionBox;
	}

	/** @param collisionBox the collisionBox to set */
	public void setCollisionBox(Rectangle collisionBox) {
		this.collisionBox = collisionBox;
	}

	/** @param collisionBox the collisionBox to set */
	public void setCollisionBox(float x, float y, float w, float h) {
		this.collisionBox.setBounds(x, y, w, h);
	}

	public QuadCollisionEngine getParentNode() {
		return parentNode;
	}

	public void setParentNode(QuadCollisionEngine parentNode) {
		this.parentNode = parentNode;
	}
}
