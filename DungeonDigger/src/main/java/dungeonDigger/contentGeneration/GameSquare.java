package dungeonDigger.contentGeneration;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import dungeonDigger.Tools.References;
import dungeonDigger.entities.GameObject;
import dungeonDigger.gameFlow.DungeonDigger;

public class GameSquare extends GameObject {
	private char			tileLetter	= 'W';
	private int				row, col;
	transient private Room	belongsTo	= null;

	public GameSquare(int row, int col) {
		this('W', row, col);
	}

	public GameSquare(char c, int row, int col) {
		this.tileLetter = c;
		this.row = row;
		this.col = col;
		this.setPosition(col * DungeonGenerator.ratioCol, row * DungeonGenerator.ratioRow);
		this.setCollisionBox(col * DungeonGenerator.ratioCol, row * DungeonGenerator.ratioRow, DungeonGenerator.ratioCol, DungeonGenerator.ratioRow);
	}

	@Override
	public void update(GameContainer c, int d) {}

	@Override
	public void render(GameContainer c, Graphics g) {
		switch( this.getTileLetter() ) {
			case 'W':
				References.IMAGES.get("dirtWallImage").draw(col * DungeonGenerator.ratioCol, row * DungeonGenerator.ratioRow);
				//ShapeRenderer.draw(new Rectangle(col*DungeonGenerator.ratioCol, row*DungeonGenerator.ratioRow, dirtWallImage.getWidth(),dirtWallImage.getHeight()));
				break;
			case 'O':
				References.IMAGES.get("dirtFloorImage").draw(col * DungeonGenerator.ratioCol, row * DungeonGenerator.ratioRow);
				//ShapeRenderer.draw(new Rectangle(col*DungeonGenerator.ratioCol, row*DungeonGenerator.ratioRow, dirtFloorImage.getWidth(),dirtFloorImage.getHeight()));
				break;
			case 'E':
			case 'X':
				References.IMAGES.get("entranceImage").draw(col * DungeonGenerator.ratioCol, row * DungeonGenerator.ratioRow);
				break;
		}
	}

	@Override
	public float getWidth() {
		return DungeonGenerator.ratioCol;
	}
	
	@Override
	public float getHeight() {
		return DungeonGenerator.ratioRow;
	}
	
	/////////////////////////
	// GETTERS AND SETTERS //
	/////////////////////////
	public boolean isPassable() {
		return tileLetter != 'W';
	}

	/** @param tileImage the tileImage to set */
	public void setTileLetter(char tileLetter) {
		this.tileLetter = tileLetter;
	}

	/** @return the tileImage */
	public char getTileLetter() {
		return tileLetter;
	}

	/** @return True if the String parameter is the same. */
	public boolean getTileLetter(char s) {
		return tileLetter == s;
	}

	public void setBelongsTo(Room belongsTo) {
		this.belongsTo = belongsTo;
	}

	public Room getBelongsTo() {
		return belongsTo;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}
}
