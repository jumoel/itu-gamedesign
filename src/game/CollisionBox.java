package game;

import game.level.Level;

import java.awt.Polygon;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
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
	private Rectangle2D.Double cBox;
	
	public class CollisionBoxData {
		double xSpeed, ySpeed, x, y;
		
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public CollisionBox(double x, double y, double width, double height) {
		cBox = new Rectangle2D.Double(x, y, width, height);
	}
	
	protected void setLevel(Level lvl) {
		collisionLevel = lvl;
	}
	
	public void setCollisionEffect(Effects effect) {
		this.collisionEffect = effect;
	}
	
	protected Effects getCollisionEffect() {
		return this.collisionEffect;
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 */
	protected void updatePosition(double x, double y) {
		cBox.setRect(x, y, cBox.width, cBox.height);
	}
	
	protected Rectangle2D.Double getFutureCollisionBox(double x, double y) {
		return new Rectangle2D.Double(x, y, cBox.width, cBox.height);
	}
	
	protected boolean isInTheWay(Line2D.Double line) {
		return line.intersects(cBox);
	}
	
	protected boolean isCollidingWith(CollisionBox theOtherBox) {
		return cBox.intersects(theOtherBox.cBox);
	}
	
	/**
	 * 
	 * @param x future x
	 * @param y future y
	 * @return
	 */
	protected CollisionBoxData isColliding (CollisionBoxData data) {
		CollisionBoxData newData = new CollisionBoxData();
		newData.x = data.x;
		newData.y = data.y;
		newData.xSpeed = data.xSpeed;
		newData.ySpeed = data.ySpeed;
		
		//determine directions
		double xDistance = data.x - cBox.x;
		double yDistance = data.y - cBox.y;
		int xDirection = (int)(xDistance / Math.abs(xDistance));
		int yDirection = (int)(yDistance / Math.abs(yDistance));
		
		//determine points
		int x0, y0, x1, y1, fx0, fy0, fx1, fy1;
		x0 = (int)(cBox.x); y0 = (int)(cBox.y + 1);
		x1 = (int)(cBox.x + cBox.width); y1 = (int)(cBox.y + cBox.height);
		fx0 = (int)data.x; fy0 = (int)data.y + 1;
		fx1 = (int)(data.x + cBox.width); fy1 = (int)(data.y + cBox.height);
		
		CollisionBox collider;
		if (xDirection > 0) {
			for (int curY = y0; curY <= y1; curY++) {
				collider = collisionLevel.getBoxAt(fx1, curY);
				if (collider != null) {
					newData.xSpeed = 0;
					newData.x = fx1-2;
				}
			}
		} else if (xDirection < 0) {
			for (int curY = y0; curY <= y1; curY++) {
				collider = collisionLevel.getBoxAt(fx0, curY);
				if (collider != null) {
					newData.xSpeed = 0;
					newData.x = fx0+1;
				}
			}
		}
		if (collisionGroundPath != null) {
			double xLeftEnd = newData.x;
			double xRightEnd = newData.x + 2;
			if (collisionGroundPath.x1 > xRightEnd
					|| collisionGroundPath.x2 < xLeftEnd) {
				collisionGroundPath = null; // we fell of an edge : no path anymore
			}
		}
		
		
		if (yDirection > 0) {
			this.collisionGroundPath = null; // jump or so: we are losing connection to ground
			
			for (int curX = x0; curX <= x1; curX++) {
				collider = collisionLevel.getBoxAt(curX, fy1);
				if (collider != null) {
					// hit head
					newData.ySpeed = 0;
					newData.y = fy1-3;
				}
			}
		} else if (yDirection < 0) {
			if (collisionGroundPath != null) {
				newData.ySpeed = 0;
				newData.y = cBox.y;
				//System.out.print("onground \n");
			} else {
				System.out.print("in air\n");
				for (int curX = x0; curX <= x1; curX++) {
					collider = collisionLevel.getBoxAt(curX, fy0);
					if (collider != null) {
						// hit feet
						if (collider.getCollisionEffect() == Effects.STOP) {
							newData.ySpeed = 0;
							newData.y = fy0;
							this.collisionGroundPath = collider.getHeadline();
						} else if (collider.getCollisionEffect() == Effects.BOUNCE) {
							newData.ySpeed = -data.ySpeed;
							newData.y = fy0;
						}
					}
				}
			}
		}
		
		
		
		
		
		//TODO iterate over the big box and get all the meta tiles within the polygone
		
		
		// get all the tiles which might be of interest
		
		
		
		// check whether there will be a collision
		return newData;
	}
	
	protected Line2D.Double getHeadline() {
		return new Line2D.Double(cBox.x, cBox.y + cBox.height, cBox.x + cBox.width, cBox.y + cBox.height);
	}
	
	
	
	
	
	public abstract void collisionDraw();
	
	
}
