package dungeonDigger.entities;

import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;

import dungeonDigger.Tools.References;
import dungeonDigger.Tools.Toolbox;

public class MobAI {
	public static Vector2f updateMovement(Mob thinker) {
		switch( thinker.getIntelligence() ) {
			case 1:
				return zombieMovement(thinker);
			case 2:
				//TODO: animal movement?
			case 3:
				//TODO: stupid person movement?
			case 4:
				//TODO: avg human movement
			case 5:
				//TODO: crafty muthafucka movement?
			default:
				return new Vector2f(0,0);
		}
	}

	// Basic stupid zombie movement
	private static Vector2f zombieMovement(Mob thinker) {
		Vector2f result = new Vector2f();
		int top, bottom, left, right;
		top = (int)(thinker.getPosition().y - thinker.getAggroRange()/2);
		bottom = (int)(thinker.getPosition().y + thinker.getAggroRange()/2);
		left = (int)(thinker.getPosition().x - thinker.getAggroRange()/2);
		right = (int)(thinker.getPosition().x + thinker.getAggroRange()/2);

		// Check if the player is close enough to care
		if( References.myCharacter.getPosition().x > left && References.myCharacter.getPosition().x < right
				&& References.myCharacter.getPosition().y > top && References.myCharacter.getPosition().y < bottom 
				&& thinker.hasLOS(References.myCharacter) ) {
			thinker.setDestination( References.myCharacter.getPosition().copy() );
			// Get the signum directions toward the player
			float xMove = Math.signum(References.myCharacter.getPosition().x - thinker.getPosition().x);
			float yMove = Math.signum((References.myCharacter.getPosition().y - (Math.max(0,thinker.getAnimation().getCurrentFrame().getHeight() - References.myCharacter.getHeight()))) - thinker.getPosition().y);
			// Calculate a random magnitude to add (for zombies 0-2 base PLUS their hunger factor)
			int stepVariance = Math.round((float)Math.random() * thinker.getMovementVariance());
			if( thinker.getTemplates().contains("zombie") ) {
				// TODO: - stepVariance += thinker.getHungerFactor().get("MOVEMENT");
			}
			// Increase our directional magnitude by that much magnitude, maintaining the directionality
			xMove += stepVariance * Math.signum(xMove);
			yMove += stepVariance * Math.signum(yMove);
			
			int canX = References.CLIENT_VIEW.canMove(Toolbox.getCardinalDirection(xMove, 0), thinker.getCollisionBox(), Math.abs(xMove));
			if( canX > 0 ) {
				result.x = canX*Math.signum(xMove);
			}
			Rectangle nextStepBox = new Rectangle(thinker.getCollisionBox().getX()+result.x, thinker.getCollisionBox().getY(), thinker.getCollisionBox().getWidth(), thinker.getCollisionBox().getHeight());
			int canY = References.CLIENT_VIEW.canMove(Toolbox.getCardinalDirection(0, yMove), nextStepBox, Math.abs(yMove));
			if( canY > 0 ) {
				result.y = canY*Math.signum(yMove);
			}
			return result;
		} else if( thinker.getDestination() != null && !thinker.getDestination().equals(thinker.getPosition()) ) {
			return stepTowards(thinker, thinker.getDestination());
		} else if( thinker.getDestination() != null && thinker.getDestination().equals(thinker.getPosition()) ) {
			thinker.setDestination(null);
			// TODO: create method to "zombiewander"
		}
		return new Vector2f(0,0);
	}
	
	private static Vector2f stepTowards(Mob thinker, Vector2f target) {
		Vector2f result = new Vector2f();
		// Get the signum directions toward the target
		float xMove = Math.signum(target.x - thinker.getPosition().x);
		float yMove = Math.signum(target.y - thinker.getPosition().y);
		// Calculate a random magnitude to add (for zombies 0-2)
		int stepVariance = Math.round((float)Math.random() * thinker.getMovementVariance());
		// Increase our directional magnitude by that much magnitude, maintaining the directionality
		xMove += stepVariance * Math.signum(xMove);
		yMove += stepVariance * Math.signum(yMove);
		
		int canX = References.CLIENT_VIEW.canMove(Toolbox.getCardinalDirection(xMove, 0), thinker.getCollisionBox(), Math.abs(xMove));
		if( canX > 0 ) {
			result.x = canX*Math.signum(xMove);
		}
		Rectangle nextStepBox = new Rectangle(thinker.getCollisionBox().getX()+result.x, thinker.getCollisionBox().getY(), thinker.getCollisionBox().getWidth(), thinker.getCollisionBox().getHeight());
		int canY = References.CLIENT_VIEW.canMove(Toolbox.getCardinalDirection(0, yMove), nextStepBox, Math.abs(yMove));
		if( canY > 0 ) {
			result.y = canY*Math.signum(yMove);
		}
		return result;
	}
}
