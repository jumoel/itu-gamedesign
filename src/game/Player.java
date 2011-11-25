package game;
import game.graphics.Animation;
import game.gui.AmazingSwitchWitch;
import game.level.Level;
import game.sound.Stereophone;
import processing.core.*;
import util.BImage;
import util.BMath;
import util.BPoint;

public class Player extends CollisionBox
{
	private enum Facing { LEFT, RIGHT };
	
	private PApplet processing;
	public double xpos, ypos, previous_xpos, previous_ypos;
	
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

	public void holdAnimation() {
		this.walkAnimation.holdAnimation();
		this.jumpAnimation.holdAnimation();
		this.idleAnimation.holdAnimation();
	}
	
	public void unholdAnimation() {
		this.walkAnimation.unholdAnimation();
		this.jumpAnimation.unholdAnimation();
		this.idleAnimation.unholdAnimation();
	}
	
	public void setLevel(Level level) {
		this.level = level;
		super.setLevel(level);
	}
	
	public Player(PApplet applet, int playerNumber, Level level)
	{
		super(level.spawnX + 0.5, level.spawnY + 0.5, 2, 3);
		super.setLevel(level);
		super.setGameElement(this);
		
		this.processing = applet;
		
		xSpeed = ySpeed = yAcceleration = 0.0;
		
		this.level = level;
		
		this.xpos = level.spawnX + 5;
		this.ypos = level.spawnY + 0.5;
		
		System.out.println(this.xpos + ", " + this.ypos);
		
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
			//isJumping = true;
			//isInAir = true;
			
			//play sound
			//Stereophone.playSound(0);
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

	
	private int feetCollisionCount = 0; //counts collisions in series
	private int collisionCount = 0;
	public void update(int deltaT)
	{
		previous_xpos = xpos;
		previous_ypos = ypos;
		
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
		
		
		if(deltaT > 84) {
			System.out.println("high deltaT: "+ deltaT);
			//xpos = previous_xpos;
			//ypos = previous_ypos;
		}
		
		//make sure, ypos and xpos did not travel to far for one frame 
		// once they travel too far, they can cross a collision box  - and we surely do not want that!
		double yDiff = ypos - previous_ypos;
		double xDiff = xpos - previous_xpos;
		double maxDistance = 0.8;
		if (Math.abs(yDiff)>=maxDistance) {
			ypos = previous_ypos + maxDistance * (yDiff /Math.abs(yDiff));
			//ySpeed = (yDiff/Math.abs(yDiff)) * 0.05;
		}
		if (Math.abs(xDiff)>=maxDistance) {
			xpos = previous_xpos + maxDistance * (xDiff / Math.abs(xDiff));
		}
		
		
		PImage currentTexture = getCurrentTexture();

		/*int xmin, xmax, ymin, ymax;
		BPoint collision;*/
		
		/*ymax = (int)Math.ceil(ypos + currentTexture.height / BunnyHat.TILEDIMENSION );
		ymin = (int)Math.floor(ypos);
		xmax = (int)Math.ceil(xpos + currentTexture.width / BunnyHat.TILEDIMENSION / 2 );
		xmin = (int)Math.floor(xpos - currentTexture.width / BunnyHat.TILEDIMENSION / 2 );*/
		
		double x0, y0, x1, y1; // edge points of our body
		x0 = (xpos - currentTexture.width / BunnyHat.TILEDIMENSION / 2);
		/*y0 = (ypos+1);
		x1 = (xpos + currentTexture.width / BunnyHat.TILEDIMENSION / 2);
		y1 = (ypos + currentTexture.height / BunnyHat.TILEDIMENSION);*/
		
		//double collisionY = detectCollisionBoxWiseY(x0, y0, x1, y1);
		//double collisionX = detectCollisionBoxWiseX(x0, y0, x1, y1);
		
		//collision = detectCollision(xmin, xmax, ymin, ymax);

		
		
		CollisionBox.CollisionBoxData data = new CollisionBoxData();
		data.x = xpos-1; data.y = ypos;
		data.xSpeed = xSpeed; data.ySpeed = ySpeed;
		data = this.isColliding(data);
		xpos = data.x+1; ypos = data.y;
		xSpeed = data.xSpeed; ySpeed = data.ySpeed;
		if (ySpeed == 0) isInAir = isJumping = false;
		else isInAir = isJumping = true;
		
			
		
		
	
		// update the position of the characters collision box
		this.updatePosition(xpos-1, ypos);
		
	}
	
	
	
	/**
	 * deprecated method - only use in case of emergency!
	 * 
	 * @param xmin_tile
	 * @param xmax_tile
	 * @param ymin_tile
	 * @param ymax_tile
	 * @return
	 */
	private BPoint detectCollision(int xmin_tile, int xmax_tile, int ymin_tile, int ymax_tile)
	{	
		boolean breakbreak = false;
		
		int collideX = -1, collideY = -1;
		
		for (int y = ymin_tile; y <= ymax_tile && !breakbreak; y++)
		{
			for (int x = xmin_tile; x <= xmax_tile && !breakbreak; x++)
			{
				
					int metadata = level.getMetaDataAt(x, y);
					
					boolean collides = (metadata == Level.MetaTiles.Obstacle.index());
					
					
					if (collides)
					{
						//breakbreak = true;
						
						if (!((x==xmin_tile && y==ymin_tile) 
								|| (x==xmin_tile && y==ymax_tile)
								||(x==xmax_tile && y==ymin_tile) 
								|| (x==xmax_tile && y==ymax_tile))) collideX = x;
						if ((y==ymin_tile || y==ymax_tile)) collideY = y;
						
					}
				
			}
		}
		
		if (collideX == -1 && collideY == -1)
		{
			return null;
		}
		else
		{
			return new BPoint(collideX, collideY);
		}
	}

	@Override
	public void collisionDraw()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void bounce(Object gameElement)
	{
		// TODO Auto-generated method stub
		
	}
}
