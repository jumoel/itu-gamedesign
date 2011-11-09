package game;
import processing.core.*;
import util.BMath;

public class Player
{
	private PApplet processing;
	private PImage texture;
	public double xpos, ypos;
	
	private boolean isInAir;
	private boolean isJumping;
	
	public boolean isMovingSideways;
	
	public boolean cannotMoveLeft;
	
	private double yAcceleration;
	private double ySpeed;
	private double xSpeed;

	private static double GRAVITY = BunnyHat.SETTINGS.getValue("gameplay/gravity");
	private static double JUMPFORCE = BunnyHat.SETTINGS.getValue("gameplay/jumpforce");

	private static double MOVEACCEL_GROUND = BunnyHat.SETTINGS.getValue("gameplay/moveacceleration/ground");
	private static double MOVEACCEL_AIR = BunnyHat.SETTINGS.getValue("gameplay/moveacceleration/air");
	
	private static double BREAKACCEL_GROUND = BunnyHat.SETTINGS.getValue("gameplay/breakacceleration/ground");
	private static double BREAKACCEL_AIR = BunnyHat.SETTINGS.getValue("gameplay/breakacceleration/air");
	
	private static double MAXSPEED = BunnyHat.SETTINGS.getValue("gameplay/maxspeed");
	
	private static double CLAMPTOZERO = BunnyHat.SETTINGS.getValue("gameplay/clamptozero");
	
	private static int DELTAT_DIVIDENT = BunnyHat.SETTINGS.getValue("gameplay/deltatdivident");

	public Player(PApplet applet)
	{
		this.processing = applet;
		
		texture = processing.loadImage("player.png");
		
		xSpeed = ySpeed = yAcceleration = 0.0;
		
		xpos = 10;
		ypos = 10;
		
		isInAir = true;
		isJumping = true;
		isMovingSideways = false;
		cannotMoveLeft = false;
	}
	
	// Return the current texture (ie. specific animation sprite)
	public PImage getCurrentTexture()
	{
		return texture;
	}
	
	public void jump()
	{
		if (!isJumping)
		{
			ySpeed = JUMPFORCE;
			isJumping = true;
			isInAir = true;
		}
	}
	
	public void moveLeft()
	{
		if (cannotMoveLeft)
		{
			xSpeed = 0;
			return;
		}
		
		if (isInAir)
		{
			xSpeed -= MOVEACCEL_AIR;
		}
		else
		{
			xSpeed -= MOVEACCEL_GROUND;
		}
	}
	
	public void moveRight()
	{
		cannotMoveLeft = false;
		
		if (isInAir)
		{
			xSpeed += MOVEACCEL_AIR;
		}
		else
		{
			xSpeed += MOVEACCEL_GROUND;
		}
	}

	public void update(int deltaT)
	{
		// X
		if (Math.abs(xSpeed) > CLAMPTOZERO)
		{
			if (!isMovingSideways)
			{
				xSpeed = BMath.addTowardsZero(xSpeed, BREAKACCEL_GROUND);
			}
			
			xSpeed = BMath.clamp(xSpeed, 0, Math.signum(xSpeed) * MAXSPEED);
		}
		else
		{
			xSpeed = 0;
		}
		
		xpos = xpos + xSpeed * deltaT / DELTAT_DIVIDENT;
		
		
		// Y
		if (isInAir)
		{
			yAcceleration = GRAVITY;
		}
		
		
		ypos = ypos + ySpeed * deltaT + 0.5 * yAcceleration * Math.pow(deltaT, 2);
		ySpeed = ySpeed + yAcceleration * deltaT;
		
		if (ypos < 0)
		{
			ySpeed = 0;
			ypos = 0;
			isInAir = false;
			yAcceleration = 0;
			isJumping = false;
		}
	}
}
