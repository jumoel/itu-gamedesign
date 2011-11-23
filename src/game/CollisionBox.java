package game;

import game.level.Level;

import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;

/**
 * All we need for a nice collision detection
 * 
 * @author samuelwalz
 *
 */
public abstract class CollisionBox
{
	public enum Effects {STOP, BOUNCE, SLOW_DOWN}
	private Effects collisionEffect = Effects.STOP;
	
	private Level collisionLevel;
	
	private CollisionBox currentCollisionPartner;
	
	/**
	 * Describing the path, a object can move along while being on ground
	 */
	private Line2D.Double collisionGroundPath;
	
	/**
	 * collision box for this object
	 */
	private Rectangle2D.Double collisionBoundaries;
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public CollisionBox(double x, double y, double width, double height) {
		collisionBoundaries = new Rectangle2D.Double(x, y, width, height);
	}
	
	protected void setLevel(Level lvl) {
		collisionLevel = lvl;
	}
	
	protected void setCollisionEffect(Effects effect) {
		this.collisionEffect = effect;
	}
	
	public Effects getCollisionEffect() {
		return this.collisionEffect;
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 */
	protected void updatePosition(double x, double y) {
		collisionBoundaries.setRect(x, y, collisionBoundaries.width, collisionBoundaries.height);
	}
	
	protected Rectangle2D.Double getFutureCollisionBox(double x, double y) {
		return new Rectangle2D.Double(x, y, collisionBoundaries.width, collisionBoundaries.height);
	}
	
	protected boolean isInTheWay(Line2D.Double line) {
		return line.intersects(collisionBoundaries);
	}
	
	protected boolean isCollidingWith(CollisionBox theOtherBox) {
		return collisionBoundaries.intersects(theOtherBox.collisionBoundaries);
	}
	
	/**
	 * 
	 * @param x future x
	 * @param y future y
	 * @return
	 */
	protected boolean isColliding (double x, double y) {
		
		// get all the tiles which might be of interest
		Rectangle2D.Double areaOfInterest = new Rectangle2D.Double();
		Rectangle2D.union(collisionBoundaries, getFutureCollisionBox(x, y), areaOfInterest);
		for (int curX = (int)areaOfInterest.x; 
				 curX <= (int)(areaOfInterest.x + areaOfInterest.width); 
				 curX++) {
			for (int curY = (int)areaOfInterest.y; 
					 curY <= (int)(areaOfInterest.y + areaOfInterest.height); 
					 curY++) {
				int metaType = collisionLevel.getMetaDataAt(curX, curY);
				switch (metaType) {
					case 1:
						
					case 2:
						
					default:
						
						break;
				}
			}
			
		}
		
		// check whether there will be a collision
		return false;
	}
	
	protected Effects getColliderEffect() {
		return Effects.STOP;
	}
	
	
	
	
	
}
