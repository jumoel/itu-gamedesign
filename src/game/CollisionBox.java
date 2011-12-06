package game;

import game.elements.BubbleGunGum;
import game.elements.GameElement;
import game.elements.PushBox;
import game.level.Level;
import game.sound.Stereophone;

import java.awt.Polygon;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.util.Observable;

import processing.core.PGraphics;

/**
 * All we need for a nice collision detection
 * 
 * @author samuelwalz
 *
 */
public abstract class CollisionBox extends Observable
{
	public enum Effects {STOP, BOUNCE, SLOW_DOWN, FINISH, NONE,
		GOOD_SHEEP_BOUNCE, BAD_SHEEP_BOUNCE, BALL_BOUNCE, PUSH}
	private Effects collisionEffect = Effects.STOP;
	
	private Level collisionLevel;
	
	private Object gameElement;
	public Object getGameElement() {
		return gameElement;
	}
	
	private CollisionBox currentCollisionPartner;
	
	private Object collidingGameElement;
	private double newX, newY, newXSpeed, newYSpeed;
	private boolean newIsJumping, newIsInAir;
	protected boolean getNewIsJumping() {return newIsJumping;}
	protected boolean getNewIsInAir() {return newIsInAir;}
	protected double getNewX() {return newX;}
	protected double getNewY() {return newY;}
	protected double getNewXSpeed() {return newXSpeed;}
	protected double getNewYSpeed() {return newYSpeed;}
	protected Object getBouncePartner() { return collidingGameElement; }
	public void resetBouncePartner() {collidingGameElement = null; }
	protected boolean contactLeft = false;
	protected boolean contactRight = false;
	protected boolean contactTop = false;
	protected boolean contactBottom = false;
	protected boolean isAbleToPush = false;
	protected boolean pushRight = false;
	
	// if we have crossing elements, we are interested in their collision too
	protected GameElement collisionPartnerX, collisionPartnerY;
	
	protected boolean isBeingPushedRight = false;
	protected boolean isBeingPushedLeft = false;
	protected GameElement ourPushable; // something we can push
	public void removePushable() {ourPushable = null;}
	/**
	 * Describing the path, a object can move along while being on ground
	 */
	private Line2D.Double collisionGroundPath;
	private CollisionBox collisionGroundPathSource;
	
	/**
	 * collision box for this object
	 */
	private Rectangle2D.Double cBox; public Rectangle2D.Double getCBox() {return cBox;}
	
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
	
	public double collisionBoxWidth() {
		return this.cBox.width;
	}
	
	public double collisionBoxHeight() {
		return this.cBox.height;
	}
	
	public double x() {
		return this.cBox.x;
	}
	
	public double y() {
		return this.cBox.y;
	}
	
	protected void setGameElement(Object gameElement) {
		this.gameElement = gameElement;
	}
	
	public void setLevel(Level lvl) {
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
	public void updatePosition(double x, double y) {
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
	protected boolean isCollidingPartner (double x, double y, double xSpeed, double ySpeed, 
			double xDist, double yDist) {
		return isColliding (x, y, xSpeed, ySpeed, 0, 0, true);
	}
	
	protected boolean isColliding (double x, double y, double xSpeed, double ySpeed) {
		return isColliding (x, y, xSpeed, ySpeed, 0, 0, false);
	}
	
	protected boolean isColliding (double x, double y, double xSpeed, double ySpeed, 
			double xDist, double yDist, boolean collisionPartnerRequest) {
		contactLeft = contactRight = contactTop = contactBottom = false;
		boolean collision = false;
		newIsInAir = newIsJumping = true;
		newX = x; newY = y; newXSpeed = xSpeed; newYSpeed = ySpeed;
		
		
		//determine directions
		double xDistance = x - cBox.x;
		double yDistance = y - cBox.y;
		if (xDist != 0) xDistance = xDist;
		if (yDist != 0) yDistance = yDist;
		//int xDirection = (int)(xDistance / Math.abs(xDistance));
		//int yDirection = (int)(yDistance / Math.abs(yDistance));
		
		// anti inside stuff
		
		
		
		//determine points
		int x0, y0, x1, y1, fx0, fy0, fx1, fy1;
		x0 = (int)(cBox.x + 0.2); x1 = (int)(cBox.x + cBox.width - 0.2);
		y0 = (int)(cBox.y+1.2); y1 = (int)(cBox.y + cBox.height + 0.8);
		fx0 = (int)x; fy0 = (int)y + 1;
		fx1 = (int)(x + cBox.width); fy1 = (int)(y + 1 + cBox.height);
		
		
		CollisionBox collider = null;
		
		if (xDistance != 0 && collisionPartnerX != null && !collisionPartnerRequest) { 
			// check auf collisionPartnerRequest um endlosschleifen zu vermeiden
			// lŠsst sich mit einer HashMap interessanter gestalten
			//double partnerYSpeedBackup = ((GameElement)collisionPartnerX).getYSpeed();
			
			if (collisionPartnerX.isBeingPushedLeft 
					&& ((GameElement)gameElement).isBeingPushedRight) {
				newXSpeed = 0;
				newX = collisionPartnerX.x();
				collision = true;
				System.out.println("lock left right");
			} else if (collisionPartnerX.isBeingPushedRight 
					&& ((GameElement)gameElement).isBeingPushedLeft) {
				newXSpeed = 0;
				newX = collisionPartnerX.x();
				collision = true;
				System.out.println("lock right left");
			} else if (collisionPartnerX.isCollidingPartner(x, collisionPartnerX.y(), xSpeed, 0, xDistance, 0)) {
				newXSpeed = collisionPartnerX.getNewXSpeed();
				newX = collisionPartnerX.getNewX();
				collision = true;
			}
			
			//((GameElement)collisionPartnerX).setYSpeed(partnerYSpeedBackup);
		}
		
		if (ourPushable != null) {
			double newBoxX; 
			if (pushRight) {
				newBoxX = this.x() + this.collisionBoxWidth();
				//ourPushable.updatePosition(cBox.x + cBox.width, ourPushable.y());
			} else {
				newBoxX = this.x() - ourPushable.collisionBoxWidth();
				//ourPushable.updatePosition(cBox.x + ourPushable.collisionBoxWidth(), ourPushable.y());
			}
			if (ourPushable.isColliding(newBoxX, ourPushable.y(), xSpeed, 0, xDistance, 0, false)) {
				newXSpeed = ourPushable.getNewXSpeed();
				
				if (pushRight) {
					newX = ourPushable.getNewX() - this.collisionBoxWidth();
				} else {
					newX = ourPushable.getNewX() + ourPushable.collisionBoxWidth();
				}
				//System.out.println("hmm, "+newX+ " vorher:"+x);
				collision = true;
			} 
		} else {
			if (xDistance > 0) {
				for (int curY = y0; curY <= y1; curY++) {
					//System.out.println(curY);
					collider = collisionLevel.getBoxAt(fx1, curY);
					if (collider != null) {
						contactRight = true;
						if ((this.getCollisionEffect() == Effects.BALL_BOUNCE
								&& collider.getCollisionEffect() == Effects.STOP)) {
							newXSpeed = -xSpeed;
							newX = collider.x() - this.cBox.width - 0.2;
						} else if (collider.getCollisionEffect() == Effects.STOP) {
							newXSpeed = 0;
							newX = collider.x()-this.cBox.width;
							//System.out.println("new x:"+newX);
						} 
						collision = true;
						updateCollisionPartners(collider);
						break;
					}
						
					
				}
				
			} else if (xDistance < 0) {
				for (int curY = y0; curY <= y1; curY++) {
					collider = collisionLevel.getBoxAt(fx0, curY);
					if (collider != null) {
						contactLeft = true;
						if ((this.getCollisionEffect() == Effects.BALL_BOUNCE
								&& collider.getCollisionEffect() == Effects.STOP)) {
							newXSpeed = -xSpeed;
							newX = collider.x()+collider.collisionBoxWidth() + 0.2;
						} else if (collider.getCollisionEffect() == Effects.STOP) {
							newXSpeed = 0;
							newX = collider.x()+collider.collisionBoxWidth();
						}
						collision = true;
						updateCollisionPartners(collider);
						//System.out.println("newX:"+newX+" x:"+x);
						break;
					}
					
					
				}
				
			}
		}
		
		
		
		if (collisionGroundPath != null) {
			collisionGroundPath = collisionGroundPathSource.getHeadline();// refresh - some objects move
			double xLeftEnd = newX;
			double xRightEnd = newX + this.cBox.width;
			if (collisionGroundPath.x1 >= xRightEnd
					|| collisionGroundPath.x2 <= xLeftEnd) {
				collisionGroundPath = null; // we fell of an edge : no path anymore
				collisionGroundPathSource = null;
			}
		}
		
		if (yDistance > 0) {
			this.collisionGroundPath = null; // jump or so: we are losing connection to ground
			this.collisionGroundPathSource = null;
			
			for (int curX = x0; curX <= x1; curX++) {
				collider = collisionLevel.getBoxAt(curX, fy1);
				if (collider != null) {
					contactTop = true;
					if (collider.getCollisionEffect() == Effects.STOP) {
						newYSpeed = 0.0;
						newY = collider.y()-this.cBox.height-1;
					}
					collision = true;
					updateCollisionPartners(collider);
					break;
				}
			}
		} else if (yDistance < 0) {
			if (collisionGroundPath != null) {
				newYSpeed = 0;
				newY = cBox.y;
				newIsJumping = false;
				newIsInAir = false;
				collision = true;
				//System.out.print("onground \n");
			} else {
				//System.out.print("in air\n");
				for (int curX = x0; curX <= x1; curX++) {
					collider = collisionLevel.getBoxAt(curX, fy0); 
					if (collider != null) {
						contactBottom = true;
						// hit feet
						if (collider.getCollisionEffect() == Effects.STOP) {
							newYSpeed = 0;
							newY = fy0;
							newIsInAir = false;
							//newIsJumping = true;
							this.collisionGroundPath = collider.getHeadline();
							this.collisionGroundPathSource = collider;
						} 
						collision = true;
						updateCollisionPartners(collider);
						break;
					} 
				}
			}
		}
		
		
		CollisionBox hardCollider = collider; // backup the hard stuff
		
		
		//if (true || collider == null) {
			collider = collisionLevel.getCollider(this);
			if (collider != null) {
				collision = true;
				updateCollisionPartners(collider);
				
				if (collider.getCollisionEffect() != Effects.NONE) {
					double xDiff = (this.x() + this.collisionBoxWidth()/2) 
							- (collider.x() + collider.collisionBoxWidth()/2);
					double yDiff = (this.y() + this.collisionBoxHeight()/2) 
							- (collider.y() + this.collisionBoxHeight()/2);
					boolean topOrBottomHit = (Math.abs(yDiff) >=Math.abs(xDiff));
					boolean rightHit = xDiff < 0;
					boolean topHit = yDiff > 0;
					switch (collider.getCollisionEffect()) {
						case BOUNCE:
							if (topOrBottomHit) {
								newYSpeed = -ySpeed;
								newY = collider.y() + collider.collisionBoxHeight();
							} else {
								/*newXSpeed = -xSpeed;
								if (rightHit) {
									newX = collider.x() - this.collisionBoxWidth();
								} else {
									newX = collider.x() + collider.collisionBoxWidth();
								}*/
							}
							break;
						case GOOD_SHEEP_BOUNCE:
							if (gameElement instanceof Player) {
								if (topOrBottomHit) {
									newYSpeed = Math.abs(ySpeed) * 1.5;
									newXSpeed = xSpeed * 1.5;
									newY = collider.y() + collider.collisionBoxHeight();
									double dice = Math.random();
									Stereophone.playSound(dice > 0.5?"100":"101", "good_sheep_bounce", 1000);
								}
							}
							break;
						case BAD_SHEEP_BOUNCE:
							if (gameElement instanceof Player) {
								newYSpeed = 3;
								newXSpeed = -6;
								double dice = Math.random();
								Stereophone.playSound(dice > 0.5?"102":"103", "bad_sheep_bounce", 250);
							}
							break;
						case PUSH:
							if (ourPushable == collider) break;
							if (!topOrBottomHit && isAbleToPush) {
								newXSpeed = 0;
								if (rightHit) {
									newX = collider.x() - cBox.width;
									this.pushRight = true;
									collider.isBeingPushedRight = true;
								} else {
									newX = collider.x() + collider.collisionBoxWidth();
									this.pushRight = false;
									collider.isBeingPushedLeft = true;
								}
								ourPushable = (GameElement)collider.gameElement;
								break;
							}
						case STOP:
							if (topOrBottomHit) {
								if (!topHit) {
									newYSpeed = 0.0;
									newY = collider.y() - cBox.height;
								} else {
									newY = collider.y() + collider.collisionBoxHeight();
									this.collisionGroundPath = collider.getHeadline();
									this.collisionGroundPathSource = collider;
								}
							} else {
								if (rightHit) {
									newX = collider.x() - cBox.width;
									((GameElement)gameElement).cannotMoveRight = true;
									
								} else {
									newX = collider.x() + collider.collisionBoxWidth();
									((GameElement)gameElement).cannotMoveLeft = true;
								}
							}
							break;
						
					}
				}
			}
		//}
		
		
		return collision;
	}
	
	private void updateCollisionPartners(CollisionBox collider) {
		// inform collision partner
		collider.bounce(this.gameElement);
		// inform yourself
		this.collidingGameElement = collider.gameElement;
	}
	
	protected Line2D.Double getHeadline() {
		return new Line2D.Double(cBox.x, cBox.y + cBox.height, cBox.x + cBox.width, cBox.y + cBox.height);
	}
	
	public void removeCollisionGroundPath() {
		this.collisionGroundPath = null;
		this.collisionGroundPathSource = null;
	}
	
	/**
	 * informs about being bounced by another box
	 */
	protected void bounce(Object gameElement) {
		collidingGameElement = gameElement;
	}
	
	public abstract void collisionDraw(PGraphics cb, int xOff, int yOff);
	
	
}
