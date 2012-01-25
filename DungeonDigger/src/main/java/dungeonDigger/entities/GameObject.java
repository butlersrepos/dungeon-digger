package dungeonDigger.entities;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;

public abstract class GameObject {
	public Vector2f		position		= new Vector2f();
	public Rectangle	collisionBox	= new Rectangle(0, 0, 0, 0);

	public abstract void update(GameContainer c, int d);

	public abstract void render(GameContainer c, Graphics g);
	
	/** @return the position */
	public Vector2f getPosition() {
		return this.position;
	}

	/** @param position the position to set */
	public void setPosition(Vector2f position) {
		this.position = position;
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
}
