package dungeonDigger.contentGeneration;

import org.newdawn.slick.Color;

public class Hallway extends Room {
	// Hallway roomID = 100
	public Hallway() {
		this.setColor(Color.gray);
		this.setRoomID(100);
		this.setName("Hallway");
	}
}
