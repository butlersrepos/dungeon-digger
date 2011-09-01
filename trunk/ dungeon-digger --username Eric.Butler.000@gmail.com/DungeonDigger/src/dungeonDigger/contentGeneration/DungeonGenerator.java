package dungeonDigger.contentGeneration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Logger;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Point;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.util.ResourceLoader;

import dungeonDigger.gameFlow.Direction;
import dungeonDigger.gameFlow.DungeonDigger;
import dungeonDigger.gameFlow.NetworkPlayer;
import dungeonDigger.network.Network;
import dungeonDigger.network.Network.GameStartPacket;

public class DungeonGenerator {
	public GameSquare[][] dungeon;
	public Vector<NetworkPlayer> playerList = new Vector<NetworkPlayer>();
	
	private int dungeonHeight = 10, dungeonWidth = 10, ratio = 5;
	private Random r = new Random(System.currentTimeMillis());
	private Vector<Room> roomList = new Vector<Room>();
	private HashMap<Integer, Room> roomDefinitionMap = new HashMap<Integer, Room>();
	private boolean isInitialized = false;	
	private Image roomWallImage, dirtFloorImage, roomFloorImage, dirtWallImage, entranceImage;
	private static final int ratioX = 99;
	private static final int ratioY = 82;
	private Vector2f entrance;
	
	private enum BorderCheck { ORTHOGONAL, DIAGONAL, ALL }
	
	public DungeonGenerator() {		
		try {
			roomWallImage = new Image( ResourceLoader.getResourceAsStream("dirt floor 100x120.png"), "dirt floor 100x120.png", false);
			dirtFloorImage =  new Image( ResourceLoader.getResourceAsStream("Dirt Block.png"), "Dirt Block.png", false);
			roomFloorImage =  new Image( ResourceLoader.getResourceAsStream("dirt floor 100x120.png"), "dirt floor 100x120.png", false);
			dirtWallImage = new Image( ResourceLoader.getResourceAsStream("Wall Block Tall.png"), "Wall Block Tall.png", false);
			entranceImage = new Image( ResourceLoader.getResourceAsStream("Stone Block.png"), "Stone Block.png", false);			
		} catch( SlickException se){ }
		
		{
			Room room1 = new Room();
			room1.setName("Chamber");
			room1.setRoomID(1);
			room1.setWidth(3);
			room1.setHeight(3);
			roomDefinitionMap.put(room1.getRoomID(), room1);
			
			Room room2 = new Room();
			room2.setName("Dining Hall");
			room2.setRoomID(2);
			room2.setWidth(5);
			room2.setHeight(5);
			roomDefinitionMap.put(room2.getRoomID(), room2);
		}
	}
	
	public DungeonGenerator(int rows, int cols) {
		this();
		this.initializeDungeon(rows, cols);
	}
	
	public void renderDungeon(GameContainer container, Graphics g) {
		if( !this.isInitialized() ){ return; }
		NetworkPlayer guy = DungeonDigger.myCharacter;
		Rectangle viewPort = new Rectangle( guy.getPlayerXCoord() - container.getWidth()/2, guy.getPlayerYCoord() - container.getHeight()/2 - 60,
												container.getWidth() + guy.getIcon().getWidth(), container.getHeight() + guy.getIcon().getHeight());
		HashSet<Point> corners = new HashSet<Point>(4);
		boolean inView = false;
		int count = 0;
		//////////////
		// Draw map //
		//////////////
		for(int row = 0; row < dungeonHeight; row++) {
			for(int col = 0; col < dungeonWidth; col++) {
				inView = false;
				// Get the cornerpoints of the tile in question
				corners.add(new Point(col*ratioX, row*ratioY));
				corners.add(new Point((col+1)*ratioX, row*ratioY));
				corners.add(new Point((col+1)*ratioX, (row+1)*ratioY));
				corners.add(new Point(col*ratioX, (row+1)*ratioY));
				
				// See if it's in our screen
				for( Point p : corners ) {
					if( viewPort.contains(p.getX(), p.getY())) {
						inView = true;
						corners.clear();
						break;
					}
				}
				corners.clear();
				
				// If it's not, don't render it
				if( !inView ) { continue; }
				count++;
				if( dungeon[row][col].getTileLetter().equalsIgnoreCase("X") ) {
					dirtWallImage.draw(col*ratioX, row*ratioY);
					//ShapeRenderer.draw(new Rectangle(col*ratioX, row*ratioY, dirtWallImage.getWidth(),dirtWallImage.getHeight()));
				}else if( dungeon[row][col].getTileLetter().equalsIgnoreCase("O") ) {
					dirtFloorImage.draw(col*ratioX, row*ratioY);
					//ShapeRenderer.draw(new Rectangle(col*ratioX, row*ratioY, dirtFloorImage.getWidth(),dirtFloorImage.getHeight()));
				}			
			}
		}
		for( NetworkPlayer player : this.getPlayerList() ) {
			if( player == guy ) { continue; }
			inView = false;
			// Get the cornerpoints of the player in question
			corners.add(new Point(player.getPlayerXCoord(), player.getPlayerYCoord()));
			corners.add(new Point((player.getPlayerXCoord()+player.getIcon().getWidth()), player.getPlayerYCoord()));
			corners.add(new Point((player.getPlayerXCoord()+player.getIcon().getWidth()), (player.getPlayerYCoord()+player.getIcon().getHeight())));
			corners.add(new Point(player.getPlayerXCoord(), (player.getPlayerYCoord()+player.getIcon().getHeight())));
			
			// See if it's in our screen
			for( Point p : corners ) {
				if( viewPort.contains(p.getX(), p.getY())) {
					inView = true;
					corners.clear();
					break;
				}
			}
			corners.clear();
			
			// If it's not, don't render it
			if( !inView ) { continue; }
			
			g.drawImage(player.getIcon().getFlippedCopy( player.isFlippedLeft(), false), player.getPlayerXCoord(), player.getPlayerYCoord());
		}
		// Draw player
		g.drawImage(guy.getIcon().getFlippedCopy( guy.isFlippedLeft(), false), guy.getPlayerXCoord(), guy.getPlayerYCoord());
		//ShapeRenderer.draw(new Rectangle(guy.getPlayerXCoord(), guy.getPlayerYCoord()+30, guy.getIcon().getWidth(), guy.getIcon().getHeight()-30));

	}
	
	/**
	 * Wipes the current dungeon and fills a new one with walls ('X')
	 * @param h
	 * @param w
	 * @return The newly initialized array.
	 */
	public void initializeDungeon( int h, int w ) {
		GameSquare[][] result = new GameSquare[h][w];
		this.roomList.clear();
		
		this.setDungeonHeight(h);
		this.setDungeonWidth(w);
		
		for(int i = 0; i < h; i++) {
			for(int j = 0; j < w; j++){
				result[i][j] = new GameSquare();
				result[i][j].setTileLetter("X");
				result[i][j].setTileImage(dirtWallImage);
			}
		}
		this.setInitialized(true);
		this.dungeon = result;
	}

	/**
	 * Real Dungeon Generation
	 * @param height
	 * @param width
	 */
	public void generateDungeon1(int height, int width, double density, double[] hallwayDensity) {		
		this.initializeDungeon(height, width);
		
		int row, column, roomID;
		// Room placing algorithm
		while( getDungeonDensity() < density ) {
			column = r.nextInt(width);
			row = r.nextInt(height);
			
			// Incremented to accomodate roomIDs that are 1-based
			roomID = r.nextInt(2)+1;
			for(int i = 0; i < 50; i++) {
				if( placeRoom(roomID, row, column) ) {
					break;
				}
			}
		}		
		
		findEntranceSquare();
		
		// Hallway generation
		for( double d : hallwayDensity ) {
			this.generateRandomRoomWalk(d);
		}
		
		
	}
	
	/**
	 * Attempted copy of the algorithmn found at:<br />
	 * {@link http://www.evilscience.co.uk/?p=53}
	 * @param height Rows
	 * @param width Columns
	 * @param iterations Big ass number, 50000?
	 * @param neighbors Default = 4
	 * @param closedness 0.00 - 1.00 chance a square is 'X' 
	 * @param specifier true or false, no idea how this affects?
	 */
	public void generateDungeon2(int height, int width, int iterations, int neighbors, double closedness, boolean specifier) {
		this.initializeDungeon(height, width);
		// RNG the whole map
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				if(  r.nextDouble() < closedness ) {
					this.dungeon[y][x].setTileLetter( "X" );
					this.dungeon[y][x].setTileImage(dirtWallImage);
				} else {
					this.dungeon[y][x].setTileLetter( "O" );
					this.dungeon[y][x].setTileImage(dirtFloorImage);
				}
			}
		}
		
		for(int n = 0; n < iterations; n++) {
			int w = r.nextInt(width);
			int h = r.nextInt(height);
			
			if (wallNeighbors(h, w) > neighbors) {
            	this.dungeon[h][w].setTileLetter(specifier ? "X" : "O");
            	this.dungeon[h][w].setTileImage(specifier ? dirtWallImage : dirtFloorImage);
            }
            else {
            	this.dungeon[h][w].setTileLetter(specifier ? "O" : "X");
            	this.dungeon[h][w].setTileImage(specifier ? dirtFloorImage : dirtWallImage);
            }
		}
	}
	
	/**
	 * Connects hallways between rooms of an ALREADY DEFINED dungeon
	 * @see generateDungeon1()
	 * @param height
	 * @param width
	 */
	private void generateRandomRoomWalk(double chance) { int diffX, diffY, startX, startY, destX, destY, i = 0, startedHalls = 0, halls = 0;
		// Starting vertically (0) or horizontally (1)
		int startDirection = 0;
		
		// Iterate over every room
		for( Room startRoom : this.roomList ) {
			if( r.nextDouble() >= chance ) { continue; }
			startedHalls++;
			// Grab a random room that's not this one
			do {
				i = r.nextInt(roomList.size());
			} while( roomList.get(i) == startRoom );
			
			// Find the distance vectors and directionality
			Room destRoom = roomList.get(i);
			
			// Add some variance to where on the border of the room we start from and goto
			startX = startRoom.getColumn() + r.nextInt( startRoom.getWidth() );
			startY = startRoom.getRow() + r.nextInt( startRoom.getHeight() );
			destX = destRoom.getColumn() + r.nextInt( destRoom.getWidth() );
			destY = destRoom.getRow() + r.nextInt( destRoom.getHeight() );
			
			diffX = destX - startX;
			diffY = destY - startY;
			
			// Pick a random axis to begin from 0-vertical, 1-horizontal
			startDirection = r.nextInt(2);
			
			// Loop to draw the hallway using the Marked flag of subsequent squares as it "digs"
			// That way we can roll it back if we need to, otherwise process them
			int currentRow, currentColumn;
			Hallway currentHallway = new Hallway();
			
			digHallway:	//Loop label for the digging of the current hallway
			for(int n = 1; n < Math.abs(diffX) + Math.abs(diffY); n++) {					
				// This IF block makes our "cursor" follow a 90degree path from start to dest
				if( startDirection == 0 ) {
					// Going vertically first
					currentRow = (int) (startY + Math.min( Math.abs(diffY), n) * Math.signum((double)diffY));			
					currentColumn = (int) (startX + Math.max( n - Math.abs(diffY), 0) * Math.signum((double)diffX));	
				} else {
					// Going horizontally first
					currentColumn = (int) (startX + Math.min( Math.abs(diffX), n) * Math.signum((double)diffX));		
					currentRow = (int) (startY + Math.max(n - Math.abs(diffX), 0) * Math.signum((double)diffY));		
				}
				
				
				GameSquare currentSquare = this.dungeon[currentRow][currentColumn];
				
				// If we're still in the startRoom, move on
				if( currentSquare.getBelongsTo() == startRoom ) { 
					continue; 
				} else {
					// Check our orthogonal neighbors
					for(int y = -1; y < 2; y++) {
						for(int x = -1; x < 2; x++) {
							// Guarantees orthogonal checks only, no diagnols
							if( y != 0 && x != 0 ) { continue; }
							// Keeps us in bounds
							if( currentRow+x < 0 || currentRow+x >= this.dungeonHeight 
									|| currentColumn+y < 0 || currentColumn+y >= this.dungeonWidth ) {
								continue;
							}
							GameSquare inspectee = this.dungeon[currentRow+y][currentColumn+x];
							// Checks owners to determine our actions, stop if its not startRoom or our hallway
							if( inspectee.getTileLetter().equalsIgnoreCase("O") &&
									inspectee.getBelongsTo() != startRoom &&
									inspectee.getBelongsTo() != currentHallway) {
								// If we're still next to the start room, only claim if the triggering square is opposite from the startRoom
								// otherwise the triggering hall is probably connecting to the startRoom
								if( checkBordersOwners(currentRow, currentColumn, BorderCheck.ORTHOGONAL, startRoom).size() > 0) {
									if( this.dungeon[currentRow+y*-1][currentColumn+x*-1].getBelongsTo() == startRoom ) {
										currentSquare.setTileLetter("O");
										currentSquare.setTileImage(dirtFloorImage);
										currentSquare.setBelongsTo(currentHallway);
									}
								} else {
									currentSquare.setTileLetter("O");
									currentSquare.setTileImage(dirtFloorImage);
									currentSquare.setBelongsTo(currentHallway);
								}
								halls++;
								break digHallway;
							}
						}
					}
					currentSquare.setTileLetter("O");
					currentSquare.setTileImage(dirtFloorImage);
					currentSquare.setBelongsTo(currentHallway);
				}
			}
		}
	}
	
	/**
	 * This method finds out how many identical cells this cell has bordering it
	 * @param row Row of our target
	 * @param col Column of our target
	 * @param likeMe True if we want to know how many identical neighbors, False for how many dissimilar neighbors
	 * @return How many we found that are identical
	 */
	public int friendlyNeighbors(int row, int col) {
		int result = 0;
		String me = dungeon[row][col].getTileLetter();
		
		try {
			for(int y = -1; y < 2; y++) {
				for(int x = -1; x < 2; x++) {
					if( this.dungeon[col+y][row+x].getTileLetter().equalsIgnoreCase(me) ) {
						result++;
					}
				}
			}
		} catch( ArrayIndexOutOfBoundsException e ){ }
		
		return result;
	}
	
	/**
	 * This method finds out how many walls this cell has bordering it
	 * @param row Row of our target
	 * @param col Column of our target
	 * @param likeMe True if we want to know how many identical neighbors, False for how many dissimilar neighbors
	 * @return How many we found that are walls
	 */
	public int wallNeighbors(int row, int col) {
		int result = 0;
		
		for(int y = -1; y < 2; y++) {
			for(int x = -1; x < 2; x++) {
				if( checkCell(row+x, col+y) ) {
					result++;
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Returns true if room was placed successfully
	 * @param roomID
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean placeRoom( int roomID, int row, int column ) {
		// Failsafes
		if( row < 0 ) { return false; }
		if( column < 0 ) { return false; }
		if( roomDefinitionMap.get(roomID).getWidth() + column > this.dungeonWidth ) { return false; }
		if( roomDefinitionMap.get(roomID).getHeight() + row > this.dungeonHeight ) { return false; }
		
		// Make sure the area is clear for the room
		for(int i = -1; i <= roomDefinitionMap.get(roomID).getWidth(); i++) {
			for(int n = -1; n <= roomDefinitionMap.get(roomID).getHeight(); n++) {
				if( !checkCell(row+n, column+i) ) { 
					return false; 
				}
			}
		}
		
		Room newRoom = roomDefinitionMap.get(roomID).copy();
		newRoom.setRow(row);
		newRoom.setColumn(column);
		roomList.add( newRoom );
		// Actually place the room
		// TODO: apply real properties instead of just an 'O'
		for(int i = 0;i < roomDefinitionMap.get(roomID).getWidth(); i++) {
			for(int n = 0;n < roomDefinitionMap.get(roomID).getHeight(); n++) {
				this.dungeon[row+n][column+i].setTileLetter("O");
				this.dungeon[row+n][column+i].setTileImage(roomFloorImage);
				this.dungeon[row+n][column+i].setBelongsTo(newRoom);
			}
		}
		
		return true;
	}
	
	/**
	 * Check if the cell is closed (is an X)
	 * @param columnX
	 * @param rowY
	 * @return
	 */
	private boolean checkCell(int X, int Y) {
		if( X < 0 || X >= this.dungeonHeight || Y < 0 || Y >= this.dungeonWidth ) {
			return false;
		}
		
		if( this.dungeon[X][Y].getTileLetter().equalsIgnoreCase("X") ) {
			return true;
		}
		
		return false;
	}
	
	public void serverSendMap() {
		for(int i = 0; i < this.dungeonWidth; i++) {
			for(int n = 0; n < this.dungeonHeight; n++) {
				Logger.getAnonymousLogger().info("Sending a tile packet to clients.");
				DungeonDigger.SERVER.sendToAllTCP(new Network.TileResponse(n, i, dungeon[n][i]));
			}
		}
		
		DungeonDigger.SERVER.sendToAllTCP(new GameStartPacket((int)getEntranceCoords().x, (int)getEntranceCoords().y));
	}
	/**
	 * Returns true if one of the bordering square is owned by the passed owner room
	 * @param row
	 * @param col
	 * @param method BorderCheck enum value
	 * @param owner
	 * @return
	 */
	private Vector<Vector2f> checkBordersOwners( int row, int col, BorderCheck method, Room owner) {
		Vector<Vector2f> result = new Vector<Vector2f>();
		for(int y = -1; y < 2; y++) {
			for(int x = -1; x < 2; x++) {
				if( method == BorderCheck.ORTHOGONAL && x != 0 && y != 0 ) { continue; }
				if( method == BorderCheck.DIAGONAL && x == 0 && y == 0 ) { continue; }
				if( this.dungeon[row+x][col+y].getBelongsTo() == owner ) {
					result.add(new Vector2f( row+x, col+y));
				}
			}
		}
		return result;
	}
	
	private double getDungeonDensity() {
		double result  = 0;
		
		for(int i = 0; i < this.dungeonWidth; i++) {
			for(int n = 0; n < this.dungeonHeight; n++) {
				if( this.dungeon[n][i].getTileLetter().equalsIgnoreCase("O") ) {
					result++;
				}
			}			
		}
		return result / (this.dungeonHeight * this.dungeonWidth);
	}
	
	/**@param dir Direction Enum, cardinal direction.
	 * @param playerY Array number[0 - dungeonWidth-1], not pixel or grid count.
	 * @param playerX Array number[0 - dungeonHeight-1], not pixel or grid count.
	 * @param distance Speed or distance to attempt to move.
	 * @return The amount of distance the character could move in that direction from 0 to distance(speed) passed in. */
	public int canMove(Direction dir, int playerY, int playerX, int distance) {		
		int goodToGo = 0;
		for(int i = 1; i <= distance; i++) {
			// Assemble suggested new position bounds to check		
			HashSet<Vector2f> collisionPoints = new HashSet<Vector2f>();
			collisionPoints.add( new Vector2f(playerX+dir.adjX()*i, playerY+dir.adjY()*i) );
			collisionPoints.add( new Vector2f(playerX+60+dir.adjX()*i, playerY+dir.adjY()*i) );
			collisionPoints.add( new Vector2f(playerX+dir.adjX()*i, playerY+60+dir.adjY()*i) );
			collisionPoints.add( new Vector2f(playerX+60+dir.adjX()*i, playerY+60+dir.adjY()*i) );
			
			for( Vector2f point : collisionPoints ) {
				// Out of bounds check
				if( point.y < 0 || point.y > this.dungeonHeight*ratioY ||
						point.x < 0 || point.x > this.dungeonWidth*ratioX ) {
					return goodToGo;
				}
				
				int checkX = (int) (point.x / ratioX);
				int checkY = (int) (point.y / ratioY);
				
				if( this.dungeon[checkY][checkX].getTileLetter("X") ) {
					return goodToGo;
				}
			}
			goodToGo++;
		}
		return goodToGo;
	}
	
	private void findEntranceSquare() {		
		for(int z = 0; z < Math.min(dungeonHeight, dungeonWidth); z++) {
			for(int i = 0; i <= z; i++) {
				if( dungeon[z-i][0+i].getTileLetter("O") ) {
					// Set x to column and y to row
					entrance = new Vector2f(0+i, z-i);
					return;
				}
			}
		}
	}
	/***********************
	 * GETTERS AND SETTERS *
	 ***********************/
	public int getDungeonHeight() {
		return dungeonHeight;
	}
	public void setDungeonHeight(int dungeonHeight) {
		this.dungeonHeight = dungeonHeight;
	}
	public int getDungeonWidth() {
		return dungeonWidth;
	}
	public void setDungeonWidth(int dungeonWidth) {
		this.dungeonWidth = dungeonWidth;
	}
	public void setInitialized(boolean isInitialized) {
		this.isInitialized = isInitialized;
	}
	public boolean isInitialized() {
		return isInitialized;
	}
	public Vector<NetworkPlayer> getPlayerList() {
		return playerList;
	}
	public void setPlayerList(Vector<NetworkPlayer> playerList) {
		this.playerList = playerList;
	}
	public int getRatioX() {
		return ratioX;
	}
	public int getRatioY() {
		return ratioY;
	}

	/**
	 * @return Array coords of start point
	 */
	public Vector2f getEntranceSqure() {
		return entrance;
	}
	/**
	 * @return Array coords of start point
	 */
	public Vector2f getEntranceCoords() {
		return new Vector2f(entrance.x * ratioX, entrance.y * ratioY + ratioY/2);
	}

	public void setMap(GameSquare[][] dungeon2) {
		for(int row = 0; row < dungeon2.length; row++) {
			for(int col = 0; col < dungeon2[row].length; col++) {
				this.dungeon[row][col] = dungeon2[row][col];
			}
		}	
		this.setInitialized(true);
	}
}
