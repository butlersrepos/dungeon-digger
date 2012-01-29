package dungeonDigger.entities;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;

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
