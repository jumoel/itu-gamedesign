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
	private static double GRAVITY = BunnyHat.SETTINGS.getValue("gameplay/gravity");
	private static double JUMPFORCE = BunnyHat.SETTINGS.getValue("gameplay/jumpforce");
	
	private static double BREAKACCEL_GROUND = BunnyHat.SETTINGS.getValue("gameplay/breakacceleration/ground");
	private static double BREAKACCEL_AIR = BunnyHat.SETTINGS.getValue("gameplay/breakacceleration/air");
	
	private static double MAXSPEED = BunnyHat.SETTINGS.getValue("gameplay/maxspeed");
	
	private static int DELTAT_DIVIDENT = BunnyHat.SETTINGS.getValue("gameplay/deltatdivident");
	private static double CLAMPTOZERO = BunnyHat.SETTINGS.getValue("gameplay/clamptozero");

	
	public boolean destroyed = false; 
	
	protected double xSpeed, ySpeed;
	protected double xpos, ypos, previous_xpos, previous_ypos;
	protected double yAcceleration;
	protected boolean isInAir;
	protected double gravityFactor = 1.0;
	protected double breakAccelAirFactor = 1.0;
	
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
				breakAmount = BREAKACCEL_GROUND * deltaFactor;
			}
			if (absXSpeed > breakAmount) {
				xSpeed = (absXSpeed - breakAmount) * xSignum;
			} else {
				xSpeed = 0.0;
			}
			
			
			xSpeed = BMath.clamp(xSpeed, 0, xSignum * MAXSPEED);
		}
		else
		{	
			xSpeed = 0;
		}
		
		xpos = xpos + xSpeed * deltaFactor;
		
		
		
		// Y
		//if (isInAir)
		//{
			yAcceleration = GRAVITY * gravityFactor;
			//ypos = ypos + ySpeed * deltaFactor + 0.5 * yAcceleration * Math.pow(deltaFactor, 2);
			ySpeed += yAcceleration * deltaFactor;
			ypos += ySpeed * deltaFactor;// + yAcceleration;
			//System.out.println("ySpeed:"+ySpeed+" yPos:"+ypos+" deltaT:"+deltaT+" deltaFactor:"+deltaFactor);
		/*}
		else
		{
			yAcceleration = 0.0;
			ypos = ypos + ySpeed * deltaFactor;
		}*/
		
		
		
		
		
		
		
		
		if(deltaT > 84) {
			System.out.println("high deltaT: "+ deltaT);
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
			//isInAir = isJumping = this.getNewIsJumping();
			
			// any interesting collision partners?
			
			

		} 
		
		if (ySpeed != 0) isInAir = true;
		
		
		
			
		
		
	
		// update the position of the characters collision box
		this.updatePosition(xpos, ypos);
	}
		

	public abstract PImage getCurrentTexture();
		

}
