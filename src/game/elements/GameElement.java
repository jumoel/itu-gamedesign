package game.elements;
import java.util.HashMap;

import processing.core.*;
import util.BMath;
import util.BPoint;
import game.BunnyHat;
import game.CollisionBox;
import game.Door;
import game.FinishLine;
import game.Player;
import game.control.RingBuffer;
import game.graphics.Animation;
import game.level.Level;
import game.sound.Stereophone;

/**
 * Standard creature - only existing in one of the dreams
 * 	
 * use it for e.g. temporary creatures in just one dream
 * 
 * @author samuelwalz
 *
 */
public abstract class GameElement extends CollisionBox
{
	protected enum Facing { LEFT, RIGHT };
	
	private static double GRAVITY = BunnyHat.SETTINGS.getValue("gameplay/gravity");
	private static double JUMPFORCE = BunnyHat.SETTINGS.getValue("gameplay/jumpforce");
	
	private static double BREAKACCEL_GROUND = BunnyHat.SETTINGS.getValue("gameplay/breakacceleration/ground");
	private static double BREAKACCEL_AIR = BunnyHat.SETTINGS.getValue("gameplay/breakacceleration/air");
	
	private static double MAX_Y_SPEED = BunnyHat.SETTINGS.getValue("gameplay/maxYspeed");
	private static double MAX_X_SPEED = BunnyHat.SETTINGS.getValue("gameplay/maxXspeed");
	
	private static int DELTAT_DIVIDENT = BunnyHat.SETTINGS.getValue("gameplay/deltatdivident");
	private static double CLAMPTOZERO = BunnyHat.SETTINGS.getValue("gameplay/clamptozero");
	
	private static double PUSH_SPEED_MAX = BunnyHat.SETTINGS.getValue("gameplay/pushspeedmax");
	private static double PUSH_ACCEL_FACTOR = BunnyHat.SETTINGS.getValue("gameplay/pushaccelerationfactor");
	
	private static double MAX_SOUND_DISTANCE = BunnyHat.SETTINGS.getValue("sound/maxdistance");

	protected Facing facing;
	protected PApplet processing;
	
	// information for Level and ColisionLevel
	public boolean destroyed = false; 
	public boolean drawMe = true;
	public boolean updateMe = true;
	public int zIndex = 0; // higher index = further in front
	public boolean drawTrail = false;
	public class TrailPos {public double x, y; public PImage image; public TrailPos(PImage image, double x, double y) {this.image = image; this.x = x; this.y = y;}}
	public RingBuffer<TrailPos> drawTrailPositions;
	
	protected double xSpeed, ySpeed; 
	public double getYSpeed() {return ySpeed;} public void setYSpeed(double speed) {ySpeed = speed;}
	protected double xpos, ypos, previous_xpos, previous_ypos;
	protected double yAcceleration, xAcceleration;
	protected boolean isInAir;
	protected boolean hasMovedX = false;
	public boolean cannotMoveLeft;
	public boolean cannotMoveRight;
	protected double gravityFactor = 1.0;
	protected double breakAccelAirFactor = 1.0;
	protected double breakAccelGroundFactor = 1.0;
	
	public void movePos(double x, double y) {
		this.hasMovedX = (this.xpos != x);
		setPos(x, y);
	}
	
	public void setPos(double x, double y) {
		
		this.xpos = x;
		this.ypos = y;
		this.updatePosition(x, y);
	}
	
	public GameElement(double x, double y, double width, double height, PApplet processing){
		super(x, y, width, height);
		this.processing = processing;
		this.xpos = x;
		this.ypos = y;
		this.xSpeed = 0.0;
		this.ySpeed = 0.0;
		this.facing = Facing.LEFT;
	}
	
	public void setTwinElement(GameElement e) {
		this.collisionPartnerX = e;
	}
	
	// calculating movement
	public void update(int deltaT) {
		previous_xpos = xpos;
		previous_ypos = ypos;
		double deltaFactor = deltaT / (double)DELTAT_DIVIDENT;
		
		// X
		boolean hasXSpeed = Math.abs(xSpeed) > CLAMPTOZERO;
		
		// would we lose our pushable?
		if (ourPushable != null) {
			if ((pushRight && xSpeed < 0 && hasXSpeed)
					|| (!pushRight && xSpeed > 0 && hasXSpeed)) {
				ourPushable.isBeingPushedRight = ourPushable.isBeingPushedLeft = false;
				ourPushable = null;
			} else if (!(this.y()+this.collisionBoxHeight() >= ourPushable.y()
					&& this.y() <= ourPushable.y() + ourPushable.collisionBoxHeight())) {
				ourPushable.isBeingPushedRight = ourPushable.isBeingPushedLeft = false;
				ourPushable = null;
			}
		}
		
		if (hasXSpeed)
		{
			double xSignum = Math.signum(xSpeed);
			
			
			double absXSpeed = Math.abs(xSpeed);
			double breakAmount = 0;
			if (xAcceleration == 0) {
				if (isInAir)
				{
					breakAmount = BREAKACCEL_AIR * deltaFactor * breakAccelAirFactor;
				}
				else
				{
					breakAmount = BREAKACCEL_GROUND * deltaFactor * breakAccelGroundFactor;
				}
				if (absXSpeed > breakAmount) {
					xSpeed = (absXSpeed - breakAmount) * xSignum;
				} else {
					xSpeed = 0.0;
				}
			}
			
			if (this.ourPushable == null) {
				xSpeed = BMath.clamp(xSpeed, 0, xSignum * MAX_X_SPEED);
			} else {
				xSpeed = BMath.clamp(xSpeed, 0, xSignum * this.PUSH_SPEED_MAX);
			}
		}
		else
		{	
			xSpeed = 0;
		}
		
		
		// new xpos, xSpeed
		xpos += xSpeed * deltaFactor * (this.ourPushable == null ? 1 : this.PUSH_ACCEL_FACTOR)
				+ 0.5 * xAcceleration*(this.ourPushable == null ? 1 : this.PUSH_ACCEL_FACTOR)*Math.pow(deltaFactor, 2);
		xSpeed += xAcceleration * deltaFactor;
		
		
		
		yAcceleration = GRAVITY * gravityFactor;
		
		// new ypos, ySpeed
		ypos += ySpeed * deltaFactor + 0.5*yAcceleration*Math.pow(deltaFactor, 2);// + yAcceleration;
		ySpeed += yAcceleration * deltaFactor;
		
		double ySignum = Math.signum(ySpeed);
		ySpeed = BMath.clamp(ySpeed, 0, ySignum * MAX_Y_SPEED);
		
		
		
		//make sure, ypos and xpos did not travel to far for one frame 
		// once they travel too far, they can cross a collision box  - and we surely do not want that!
		// but we have a better idea: how about scanning stepwise the whole way & stop once we encounter
		// any collision?
		int numCollisionScanSteps = 1;
		double yDiff = ypos - previous_ypos;
		double xDiff = xpos - previous_xpos;
		double collisionScanStepDistanceX = xDiff;
		double collisionScanStepDistanceY = yDiff;
		double maxDistance = 0.8;
		if (Math.abs(yDiff)>=maxDistance || Math.abs(xDiff)>=maxDistance) {
			if (Math.abs(yDiff) > Math.abs(xDiff)) {
				numCollisionScanSteps = (int)Math.ceil(Math.abs(yDiff) / maxDistance);  
			} else {
				numCollisionScanSteps = (int)Math.ceil(Math.abs(xDiff) / maxDistance);
			}
			
			
			collisionScanStepDistanceX = xDiff / numCollisionScanSteps;
			collisionScanStepDistanceY = yDiff / numCollisionScanSteps;
			
		}
		
		
		// stepwise scan for collision
		for (int i = 1; i <= numCollisionScanSteps; i++) {
			if (this.isColliding(previous_xpos + collisionScanStepDistanceX*i, 
					previous_ypos + collisionScanStepDistanceY*i, xSpeed, ySpeed)) {
				xpos = this.getNewX(); ypos = this.getNewY();
				xSpeed = this.getNewXSpeed(); ySpeed = this.getNewYSpeed();
				if (ySpeed == 0) {
					isInAir = false;
				}
				break;
			} 
		}
		
		if (ySpeed != 0) isInAir = true;
		
		
		if (xSpeed > 0) this.facing = Facing.RIGHT;
		else if (xSpeed < 0) this.facing = Facing.LEFT;
			
		
		
	
		// update the position of the characters collision box
		this.updatePosition(xpos, ypos);
		// update pushable position
		this.hasMovedX = (previous_xpos != xpos) || hasMovedX;
		
		if (ourPushable != null) {
			this.ourPushable.movePos(pushRight?xpos + collisionBoxWidth():xpos-ourPushable.collisionBoxWidth(),
					ourPushable.y());
		}
		
		if (collisionPartnerX != null && hasMovedX) {
			((GameElement) collisionPartnerX).setPos(xpos, collisionPartnerX.y());
			((GameElement) collisionPartnerX).facing = facing;
			hasMovedX = false;
		}
		
		if (collisionPartnerY != null) {
			((GameElement) collisionPartnerY).setPos(collisionPartnerY.x(), ypos);
		}
	}
	
	protected void playSound(String sound, String id, int time) {
		//System.out.println(this.collisionLevel.getDistanceClosestPlayer(this.xpos));
		if (this.collisionLevel.getDistanceClosestPlayer(this.xpos) < MAX_SOUND_DISTANCE) {
			Stereophone.playSound(sound, id, time);
		}
	}

	public abstract PImage getCurrentTexture();
		

}
