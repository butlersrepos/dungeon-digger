package dungeonDigger.contentGeneration;

public class GameSquare {
	private char tileLetter = 'W';
	transient private Room belongsTo = null;
	
	public GameSquare() { }
	
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
	/**
	 * @return the tileImage
	 */
	public char getTileLetter() {
		return tileLetter;
	}
	/**
	 * @return True if the String parameter is the same.
	 */
	public boolean getTileLetter(char s) {
		return tileLetter == s;
	}

	public void setBelongsTo(Room belongsTo) {
		this.belongsTo = belongsTo;
	}

	public Room getBelongsTo() {
		return belongsTo;
	}
}
