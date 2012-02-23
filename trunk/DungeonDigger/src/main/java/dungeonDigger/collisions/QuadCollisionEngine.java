package dungeonDigger.collisions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;

import org.newdawn.slick.geom.Rectangle;

import dungeonDigger.Tools.Constants;
import dungeonDigger.Tools.References;
import dungeonDigger.contentGeneration.DungeonGenerator;
import dungeonDigger.entities.GameObject;
import dungeonDigger.entities.Mob;

public class QuadCollisionEngine {
	private ArrayList<QuadCollisionEngine>	children	= new ArrayList<QuadCollisionEngine>();
	private ArrayList<GameObject>			list		= new ArrayList<GameObject>();
	private QuadCollisionEngine				parent		= null;
	private int								tier		= 0;
	private static int						loopStop	= 0;
	// These responsibility measurements are in CELLS not pixels. Multiplying by the ratioRow/ratioCol gives the pixels.
	private int								responsibleX, responsibleY, responsibleWidth, responsibleHeight, breakingPoint;
	private boolean							isSplit		= false;
	private static QuadCollisionEngine 		NODE_ZERO	= null;
	
	public static void initiateNodeZero(Object[][] obj) {
		References.log.setLevel(Level.OFF);
		int w = obj[0].length, h = 0;
		for(int row = 0; row < obj.length; row++) {
			if( obj[row].length != w ) {
				References.log.fine("Irregular array! Aborting QuadCollisionEngine!");
			}
			w = obj[row].length;
		}
		h = obj.length * DungeonGenerator.ratioRow;
		w = obj[0].length * DungeonGenerator.ratioCol;
		NODE_ZERO = new QuadCollisionEngine(0,0,w,h, 0);
	}

	public static void relocate(GameObject obj) {
		QuadCollisionEngine newParent = null, objParent = obj.getParentNode();
		if( !NODE_ZERO.contains(obj) ) {
			References.log.severe("QUADCOLLISIONENGINE::RELOCATE - GameObject("+obj.toString()+") was not found within NODE_ZERO's area. Cannot relocate.");
			obj.setParentNode(null);
		}
		loopStop = 0;
		newParent = findResponsibleParent(obj, NODE_ZERO);
		objParent.populateMe();
		obj.setParentNode(null);
		newParent.populateMe();
	}
	
	public static ArrayList<GameObject> checkCollisions(GameObject obj) {
		References.log.finer("<==QCE==> Checking collisions for a(n) " + obj.getClass());
		ArrayList<GameObject> obstacles = new ArrayList<>();
		QuadCollisionEngine currentNodeToCheck = obj.getParentNode();
		obstacles.addAll( collisionChecker(obj, currentNodeToCheck) );
		return obstacles;
	}
	
	private static ArrayList<GameObject> collisionChecker(GameObject obj, QuadCollisionEngine node) {
		ArrayList<GameObject> obstacles = new ArrayList<>();
		
		if( node.getChildren().size() > 0 ) {
			for( QuadCollisionEngine q : node.getChildren() ) {
				obstacles.addAll( collisionChecker(obj, q) );
			}
		} 
		for( GameObject g : node.getList() ) {
			if( g == obj ) { continue; }
			if( g instanceof Mob ) {
				if( g.getCollisionBox().intersects(obj.getCollisionBox()) ) {
					if( obstacles == null ) { obstacles = new ArrayList<>(); }
					References.log.finer("<==QCE==> Found a collision with a " + g.getClass() + "!");
					obstacles.add(g);
				}
			}
		}
		
		obstacles.trimToSize();
		return obstacles;
	}
	
	public static boolean addObjectToGame(GameObject obj) {
		References.log.finer("<==QCE==> Adding a " + obj.getClass() + " to the collision tree.");
		if( NODE_ZERO.contains(obj) ) {
			loopStop = 0;
			obj.setParentNode( findResponsibleParent(obj, NODE_ZERO) );
			obj.getParentNode().populateMe();
			References.log.finer("<==QCE==> " + obj.getClass() + " added to tier " + obj.getParentNode().getTier());
			return true;
		} else {
			References.log.finer("<==QCE==> Couldn't add " + obj.getClass() + " to tree!");
			return false;
		}
	}
	
	public static boolean removeObjectFromGame(GameObject obj) {
		References.log.finer("<==QCE==> Removing a " + obj.getClass() + " from the collision tree.");
		QuadCollisionEngine parent = obj.getParentNode();
		if( parent == null ) { return false; }
		obj.setParentNode(null);
		obj.setPosition(-100, -100);
		parent.getList().remove(obj);
		return true;
	}
	
	private static QuadCollisionEngine findResponsibleParent(GameObject obj, QuadCollisionEngine startNode) {
		if( loopStop > 10000 ) {
			System.out.println("SHE'S SPIRALING OUT OF CONTROL CAPTAIN!");
			References.log.severe("SHE'S SPIRALING OUT OF CONTROL CAPTAIN!");
		}
		References.log.finer("<==QCE==> Finding parent for a(n) " + obj.getClass());
		for( QuadCollisionEngine q : startNode.getChildren() ) {
			if( q.contains(obj) ) { 
				loopStop++;
				return findResponsibleParent(obj, q); 
			}
		}
		return startNode;
	}
	
	private QuadCollisionEngine(int x, int y, int w, int h, int tier) {
		this.responsibleX = x;
		this.responsibleY = y;
		this.responsibleWidth = w;
		this.responsibleHeight = h;
		this.breakingPoint = Constants.N_PER_LEAF;
		this.tier = tier;
		//References.log.finer("Tier " + this.tier + " Node: My range is X: " + x + " - " + w + " and Y: " + y + " - " + h);
		this.populateMe();
	}

	private void quadSplit() {
		int leftWidth = (int)Math.floor(this.responsibleWidth / 2), 
			topHeight = (int)Math.floor(this.responsibleHeight / 2), 
			rightWidth = (int)Math.ceil(this.responsibleWidth / 2), 
			bottomHeight = (int)Math.ceil(this.responsibleHeight / 2);
		QuadCollisionEngine upperLeftChild = new QuadCollisionEngine(this.responsibleX, this.responsibleY, leftWidth, topHeight, this.tier+1);
		children.add(upperLeftChild);
		upperLeftChild.setParent(this);
		
		QuadCollisionEngine upperRightChild = new QuadCollisionEngine(this.responsibleX + leftWidth, this.responsibleY, rightWidth, topHeight, this.tier+1);
		children.add(upperRightChild);
		upperRightChild.setParent(this);
		
		QuadCollisionEngine lowerLeftChild = new QuadCollisionEngine(this.responsibleX, this.responsibleY + topHeight, leftWidth, bottomHeight, this.tier+1);
		children.add(lowerLeftChild);
		lowerLeftChild.setParent(this);
		
		QuadCollisionEngine lowerRightChild = new QuadCollisionEngine(this.responsibleX + leftWidth, this.responsibleY + topHeight, rightWidth, bottomHeight, this.tier+1);
		children.add(lowerRightChild);
		lowerRightChild.setParent(this);
		
		upperLeftChild.populateMe();
		upperRightChild.populateMe();
		lowerLeftChild.populateMe();
		lowerRightChild.populateMe();
		isSplit = true;
	}

	private ArrayList<GameObject> populateMe() {
		this.list.clear();
		ArrayList<GameObject> temp = new ArrayList<>();
		int counter = 0;
		boolean populatedKids = false;
		for( GameObject obj : References.getAllEntites() ) {
			References.log.finer("\tChecking if " + obj.getClass() + " is in my range.");
			References.log.finer("\tObj is at ("+obj.getPosition().x + ", " + obj.getPosition().y + ")");
			References.log.finer("\tObj is "+obj.getCollisionBox().getWidth() + " wide and " + obj.getCollisionBox().getHeight() + " tall.");
			if( this.contains(obj) ) {
				References.log.finer("\t\t" + obj.getClass() + " was within range.");
				counter++;
				temp.add(obj);
				if( counter > this.breakingPoint && !populatedKids) {
					if( isSplit ) {
						for( QuadCollisionEngine q : this.getChildren() ) {
							q.populateMe();
						}
					} else { this.quadSplit(); }
					populatedKids = true;
				}
			} else {
				References.log.finer("\t\t" + obj.getClass() + " was NOT within range.");
			}
		}
		Iterator<GameObject> it = temp.iterator();
		while( it.hasNext() ) {
			GameObject obj = it.next();
			if( obj.getParentNode() == null || obj.getParentNode() == this || obj.getParentNode().getTier() < this.tier) {
				obj.setParentNode(this);
			} else {
				it.remove();
			}
		}
		temp.trimToSize();
		References.log.fine("Tier " + this.tier + " Node: I have " + temp.size() + " items to track!");
		this.list.addAll(temp);
		return list;
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

	public int getTier() {
		return tier;
	}

	public void setTier(int tier) {
		this.tier = tier;
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
