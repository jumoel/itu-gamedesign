package game.elements;
import java.util.HashMap;

import processing.core.*;
import util.BMath;
import game.BunnyHat;
import game.CollisionBox;
import game.Door;
import game.FinishLine;
import game.Player;
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

	protected Facing facing;
	
	// information for Level and ColisionLevel
	public boolean destroyed = false; 
	public boolean drawMe = true;
	public boolean updateMe = true;
	
	protected double xSpeed, ySpeed;
	protected double xpos, ypos, previous_xpos, previous_ypos;
	protected double yAcceleration;
	protected boolean isInAir;
	protected double gravityFactor = 1.0;
	protected double breakAccelAirFactor = 1.0;
	protected double breakAccelGroundFactor = 1.0;
	
	public void setPos(double x, double y) {
		this.xpos = x;
		this.ypos = y;
		this.updatePosition(x, y);
	}
	
	public GameElement(double x, double y, double width, double height){
		super(x, y, width, height);
		this.xpos = x;
		this.ypos = y;
		this.xSpeed = 0.0;
		this.ySpeed = 0.0;
		this.facing = Facing.LEFT;
	}
	
	// calculating movement
	public void update(int deltaT) {
		previous_xpos = xpos;
		previous_ypos = ypos;
		double deltaFactor = deltaT / (double)DELTAT_DIVIDENT;
		
		// X
		boolean hasXSpeed = Math.abs(xSpeed) > CLAMPTOZERO;
		
		if (hasXSpeed)
		{
			double xSignum = Math.signum(xSpeed);
			
			
			double absXSpeed = Math.abs(xSpeed);
			double breakAmount = 0;
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
			
			
			xSpeed = BMath.clamp(xSpeed, 0, xSignum * MAX_X_SPEED);
		}
		else
		{	
			xSpeed = 0;
		}
		
		xpos = xpos + xSpeed * deltaFactor;
		
		
		
		
		yAcceleration = GRAVITY * gravityFactor;
		
		ySpeed += yAcceleration * deltaFactor;
		double ySignum = Math.signum(ySpeed);
		ySpeed = BMath.clamp(ySpeed, 0, ySignum * MAX_Y_SPEED);
		
		ypos += ySpeed * deltaFactor;// + yAcceleration;
		
		
		
		
		
		
		
		
		
		if(deltaT > 84) {
			//System.out.println("high deltaT: "+ deltaT);
		}
		
		//make sure, ypos and xpos did not travel to far for one frame 
		// once they travel too far, they can cross a collision box  - and we surely do not want that!
		double yDiff = ypos - previous_ypos;
		double xDiff = xpos - previous_xpos;
		double maxDistance = 0.9;
		if (Math.abs(yDiff)>=maxDistance) {
			ypos = previous_ypos + maxDistance * Math.signum(yDiff);
		}
		if (Math.abs(xDiff)>=maxDistance) {
			xpos = previous_xpos + maxDistance * Math.signum(xDiff);
		}
		

		
		if (this.isColliding(xpos, ypos, xSpeed, ySpeed)) {
			xpos = this.getNewX(); ypos = this.getNewY();
			xSpeed = this.getNewXSpeed(); ySpeed = this.getNewYSpeed();
			if (ySpeed == 0) {
				isInAir = false;
			}
			
			
			

		} 
		
		if (ySpeed != 0) isInAir = true;
		
		
		if (xSpeed > 0) this.facing = Facing.RIGHT;
		else if (xSpeed < 0) this.facing = Facing.LEFT;
			
		
		
	
		// update the position of the characters collision box
		this.updatePosition(xpos, ypos);
	}
		

	public abstract PImage getCurrentTexture();
		

}
