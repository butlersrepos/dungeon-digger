package dungeonDigger.collisions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import org.newdawn.slick.geom.Rectangle;

import dungeonDigger.Tools.Constants;
import dungeonDigger.Tools.References;
import dungeonDigger.contentGeneration.DungeonGenerator;
import dungeonDigger.entities.GameObject;

public class QuadCollisionEngine {
	private ArrayList<QuadCollisionEngine>	children	= new ArrayList<QuadCollisionEngine>();
	private ArrayList<GameObject>			list		= new ArrayList<GameObject>();
	private QuadCollisionEngine				parent		= null;
	private int								tier		= 0;
	// These responsibility measurements are in CELLS not pixels. Multiplying by the ratioRow/ratioCol gives the pixels.
	private int								responsibleX, responsibleY, responsibleWidth, responsibleHeight, breakingPoint;
	private boolean							isSplit		= false;

	public static QuadCollisionEngine initiateNodeZero(Object[][] obj) {
		int w = obj[0].length, h = 0;
		for(int row = 0; row < obj.length; row++) {
			if( obj[row].length != w ) {
				Logger.getAnonymousLogger().warning("Irregular array! Aborting QuadCollisionEngine!");
				return null;
			}
			w = obj[row].length;
		}
		h = obj.length * DungeonGenerator.ratioRow;
		w = obj[0].length * DungeonGenerator.ratioCol;
		return new QuadCollisionEngine(0,0,w,h, 0);
	}
	
	private QuadCollisionEngine(int x, int y, int w, int h, int tier) {
		this.responsibleX = x;
		this.responsibleY = y;
		this.responsibleWidth = w;
		this.responsibleHeight = h;
		this.breakingPoint = Constants.N_PER_LEAF;
		this.tier = tier;
		//System.out.println("Tier " + this.tier + " Node: My range is X: " + x + " - " + w + " and Y: " + y + " - " + h);
		this.populateMe();
	}

	private void quadSplit() {
		int leftWidth = (int)Math.floor(this.responsibleWidth / 2), 
			topHeight = (int)Math.floor(this.responsibleHeight / 2), 
			rightWidth = (int)Math.ceil(this.responsibleWidth / 2), 
			bottomHeight = (int)Math.ceil(this.responsibleHeight / 2);
		QuadCollisionEngine upperLeftChild = new QuadCollisionEngine(this.responsibleX, this.responsibleY, leftWidth, topHeight, this.tier+1);
		children.add(upperLeftChild);
		QuadCollisionEngine upperRightChild = new QuadCollisionEngine(this.responsibleX + leftWidth, this.responsibleY, rightWidth, topHeight, this.tier+1);
		children.add(upperRightChild);
		QuadCollisionEngine lowerLeftChild = new QuadCollisionEngine(this.responsibleX, this.responsibleY + topHeight, leftWidth, bottomHeight, this.tier+1);
		children.add(lowerLeftChild);
		QuadCollisionEngine lowerRightChild = new QuadCollisionEngine(this.responsibleX + leftWidth, this.responsibleY + topHeight, rightWidth, bottomHeight, this.tier+1);
		children.add(lowerRightChild);
		upperLeftChild.populateMe();
		upperRightChild.populateMe();
		lowerLeftChild.populateMe();
		lowerRightChild.populateMe();
		isSplit = true;
	}

	public ArrayList<GameObject> populateMe() {
		ArrayList<GameObject> temp = new ArrayList<>();
		int counter = 0;
		for( GameObject obj : References.getAllEntites() ) {
			//System.out.println("Tier " + this.tier + " Node: Checking if object is inside my range.");
			if( this.contains(obj) ) {
				counter++;
				temp.add(obj);
				if( !isSplit && counter > this.breakingPoint ) {
					//System.out.println("Tier " + this.tier + " Node: Requiring a quadsplit!");
					this.quadSplit();
				}
			}
		}
		Iterator<GameObject> it = temp.iterator();
		while( it.hasNext() ) {
			GameObject obj = it.next();
			if( obj.getParentNode() == null ) {
				obj.setParentNode(this);
			} else {
				it.remove();
			}
		}
		temp.trimToSize();
		System.out.println("Tier " + this.tier + " Node: I have " + temp.size() + " items to track!");
		this.list = temp;
		return temp;
	}
	
	public boolean contains(GameObject obj) {
		Rectangle r = obj.getCollisionBox();
		if( r.getMaxX() > this.responsibleX + this.responsibleWidth ) {
			return false;
		} else if( r.getMinX() < this.responsibleX ) {
			return false;
		} else if( r.getMaxY() > this.responsibleY + this.responsibleHeight ) {
			return false;
		} else if( r.getMinY() < this.responsibleX ) {
			return false;
		}
		return true;
	}
	
	public void addChildWaypoint(QuadCollisionEngine wp) {
		if( wp.parent != null & wp.parent != this ) {
			wp.parent.children.remove(wp);
		}
		wp.parent = this;
		if( !this.children.contains(wp) ) {
			this.children.add(wp);
		}
	}

	public void removeChildWaypoint(QuadCollisionEngine wp) {
		this.children.remove(wp);
		wp.parent = null;
	}

	/** @return the children, Waypoints on the lower tier */
	public ArrayList<QuadCollisionEngine> getChildren() {
		return children;
	}

	/** @return the list of objects the Waypoint is tracking */
	public ArrayList<GameObject> getList() {
		return list;
	}

	/** @param list the list to set, primarily used for copying */
	public void setList(ArrayList<GameObject> list) {
		this.list = list;
	}

	/** @return the parent Waypoint of this Waypoint */
	public QuadCollisionEngine getParent() {
		return parent;
	}

	/** @param parent the parent to set for this Waypoint */
	public void setParent(QuadCollisionEngine wp) {
		if( this.parent != null && this.parent != wp ) {
			this.parent.removeChildWaypoint(this);
		}
		this.parent = wp;
		if( !wp.children.contains(this) ) {
			wp.children.add(this);
		}
	}

	/** @param item the item nearby to keep track of */
	public void addObject(GameObject item) {
		this.list.add(item);
	}

	/** @param item the item to remove from this Waypoint's tracking */
	public void removeObject(GameObject item) {
		this.list.remove(item);
	}

	/* These getters return the actual pixel measurements for boundary checking.
	 * The responsibility variables are measured in CELLS for the array, not pixels
	 * so this conversion is necessary. */
	public float getPixelX() {
		return this.responsibleX * DungeonGenerator.ratioCol;
	}	
	public float getPixelY() {
		return this.responsibleY * DungeonGenerator.ratioRow;
	}	
	public float getPixelWidth() {
		return this.responsibleWidth * DungeonGenerator.ratioCol;
	}
	public float getPixelHeight() {
		return this.responsibleHeight * DungeonGenerator.ratioRow;
	}
}
