package dungeonDigger.contentGeneration;

import org.newdawn.slick.Image;

public class GameSquare {
	private String tileLetter = "O";
	transient private String imageName;
	transient private String marked = "";
	transient private Room belongsTo = null;
	transient private Image tileImage;
	
	public GameSquare() {
	}
	
	public void change() {
		if( marked.length() == 1 ) {
			this.tileLetter = this.marked;
			this.marked = "";
		}
	}
	public void clearChanges() {
		this.marked = "";
	}
	/**
	 * @param marked the marked to set
	 */
	public void setMarked(String marked) {
		this.marked = marked;
	}
	/**
	 * @return the marked
	 */
	public String getMarked() {
		return marked;
	}
	/**
	 * @param tileImage the tileImage to set
	 */
	public void setTileLetter(String tileLetter) {
		this.tileLetter = tileLetter;
	}
	/**
	 * @return the tileImage
	 */
	public String getTileLetter() {
		return tileLetter;
	}
	/**
	 * @return True if the String parameter is the same.
	 */
	public boolean getTileLetter(String s) {
		return tileLetter.equalsIgnoreCase(s);
	}

	public void setBelongsTo(Room belongsTo) {
		this.belongsTo = belongsTo;
	}

	public Room getBelongsTo() {
		return belongsTo;
	}

	public void setTileImage(Image tileImage) {
		this.tileImage = tileImage;
	}

	public Image getTileImage() {
		return tileImage;
	}
}
