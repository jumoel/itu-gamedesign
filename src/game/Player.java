package game;
import game.graphics.Animation;
import game.level.Level;
import processing.core.*;
import util.BImage;
import util.BMath;

public class Player
{
	private enum Facing { LEFT, RIGHT };
	
	private PApplet processing;
	public double xpos, ypos;
	
	private boolean isInAir;
	private boolean isJumping;
	
	public boolean isMovingSideways;

	public boolean cannotMoveLeft;
	public boolean cannotMoveRight;
	
	private double yAcceleration;
	private double ySpeed;
	private double xSpeed;
	
	private Facing facing;

	private Animation walkAnimation;
	private Animation jumpAnimation;
	private Animation idleAnimation;
	
	private Level level;

	private static double GRAVITY = BunnyHat.SETTINGS.getValue("gameplay/gravity");
	private static double JUMPFORCE = BunnyHat.SETTINGS.getValue("gameplay/jumpforce");

	private static double MOVEACCEL_GROUND = BunnyHat.SETTINGS.getValue("gameplay/moveacceleration/ground");
	private static double MOVEACCEL_AIR = BunnyHat.SETTINGS.getValue("gameplay/moveacceleration/air");
	
	private static double BREAKACCEL_GROUND = BunnyHat.SETTINGS.getValue("gameplay/breakacceleration/ground");
	private static double BREAKACCEL_AIR = BunnyHat.SETTINGS.getValue("gameplay/breakacceleration/air");
	
	private static double MAXSPEED = BunnyHat.SETTINGS.getValue("gameplay/maxspeed");
	
	private static double CLAMPTOZERO = BunnyHat.SETTINGS.getValue("gameplay/clamptozero");
	
	private static int DELTAT_DIVIDENT = BunnyHat.SETTINGS.getValue("gameplay/deltatdivident");

	public Player(PApplet applet, int playerNumber, Level level)
	{
		this.processing = applet;
		
		xSpeed = ySpeed = yAcceleration = 0.0;
		
		this.level = level;
		
		this.xpos = level.spawnX + 0.5;
		this.ypos = level.spawnY + 0.5;
		
		isInAir = true;
		isJumping = true;
		isMovingSideways = false;
		cannotMoveLeft = false;

		this.walkAnimation = new Animation(processing, "graphics/animations/player" + playerNumber + "run");
		this.idleAnimation = new Animation(processing, "graphics/animations/player" + playerNumber + "idle");
		this.jumpAnimation = new Animation(processing, "graphics/animations/player" + playerNumber + "jump");
		
		this.idleAnimation.start();
		
		this.facing = Facing.RIGHT;
	}
	
	// Return the current texture (ie. specific animation sprite)
	public PImage getCurrentTexture()
	{
		PImage ret;
		int time = processing.millis();
		
		if (jumpAnimation.isRunning())
		{
			ret = jumpAnimation.getCurrentImage(time);
		}
		else if (walkAnimation.isRunning())
		{
			ret = walkAnimation.getCurrentImage(time);
		}
		else if (idleAnimation.isRunning())
		{
			ret = idleAnimation.getCurrentImage(time);
		}
		else
		{
			idleAnimation.start();
			ret = idleAnimation.getCurrentImage(time);
		}
		
		if (facing == Facing.LEFT)
		{
			ret = BImage.mirrorAroundY(processing, ret);
		}
		
		return ret;
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
		cannotMoveRight = false;
		
		facing = Facing.LEFT;
		
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
		
		facing = Facing.RIGHT;
		
		if (cannotMoveRight)
		{
			xSpeed = 0;
			return;
		}
		
		if (isInAir)
		{
			xSpeed += MOVEACCEL_AIR;
		}
		else
		{
			xSpeed += MOVEACCEL_GROUND;
		}
	}
	
	private void controlAnimations()
	{
		boolean hasXSpeed = Math.abs(xSpeed) > CLAMPTOZERO;
		
		if (isInAir)
		{
			idleAnimation.stop();
			walkAnimation.stop();
			
			if (jumpAnimation.isStopped())
			{
				jumpAnimation.start();
			}
		}
		else if (hasXSpeed || isMovingSideways)
		{
			idleAnimation.stop();
			jumpAnimation.stop();
			
			if (walkAnimation.isStopped())
			{
				walkAnimation.start();
			}
		}
		else
		{
			jumpAnimation.stop();
			walkAnimation.stop();
			
			if (idleAnimation.isStopped())
			{
				idleAnimation.start();
			}
		}
	}
	
	public void checkCollisions()
	{
		
	}

	public void update(int deltaT)
	{
		// X
		
		boolean hasXSpeed = Math.abs(xSpeed) > CLAMPTOZERO;
		
		controlAnimations();
		
		if (hasXSpeed)
		{
			if (!isMovingSideways)
			{
				if (isInAir)
				{
					xSpeed = BMath.addTowardsZero(xSpeed, BREAKACCEL_AIR);
				}
				else
				{
					xSpeed = BMath.addTowardsZero(xSpeed, BREAKACCEL_GROUND);
				}
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
		
		
		
		// Old collision detection
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
