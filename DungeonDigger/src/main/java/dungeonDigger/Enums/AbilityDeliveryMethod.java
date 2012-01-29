package dungeonDigger.Enums;

public enum AbilityDeliveryMethod {
	SELF_ONLY, 				// Buff
	MOUSE_PROJECTILE,		// Immediately fires in direction of mouse
	CLICK_PROJECTILE,		// Waits until you click and then fires in direction of mouse
	FRONTAL_PROJECTILE,		// Immediately fires in direction your character is facing
	BLAST, 					// Waits until you click and then bursts from where your mouse is
	BURST,					// Immediately bursts from your character
	FRONTAL_CONE,			// Immediately fires on all in front of your character
	MOUSE_CONE;				// Waits until you click and then fires on all in front of your character
}
