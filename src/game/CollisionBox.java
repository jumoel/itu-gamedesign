package game;

import game.level.Level;

import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

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
		
		return false;
	}
	
	
	
	
	
}
