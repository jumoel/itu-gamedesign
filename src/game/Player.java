package game;
import java.util.HashMap;


import game.elements.BubbleGunGum;
import game.elements.BubbleGunGum.BallColor;
import game.elements.GameElement; 
import game.elements.StandardCreature;
import game.graphics.Animation;
import game.gui.AmazingSwitchWitch;
import game.level.Level;
import game.sound.Stereophone;
import processing.core.*;
import util.BImage;
import util.BMath;
import util.BPoint;

public class Player extends GameElement
{
	//private enum Facing { LEFT, RIGHT };
	private int myID;
	
	private PApplet processing;
	//public double xpos, ypos, previous_xpos, previous_ypos;
	
	// jumping stuff
	//private boolean isInAir;
	private boolean isJumping;
	private boolean didJump = false;
	private boolean didFire = false;
	
	//public boolean isMovingSideways;

	public boolean cannotMoveLeft;
	public boolean cannotMoveRight;
	
	//private double yAcceleration;
	//public double ySpeed;
	//public double xSpeed;
	
	//private Facing facing;

	private boolean unarmed;
	
	private Animation walkAnimation;
	private Animation jumpAnimation;
	private Animation idleAnimation;
	private Animation walkAnimationGun;
	private Animation jumpAnimationGun;
	private Animation idleAnimationGun;
	private Animation winAnimation;
	private Animation loseAnimation;
	private Animation stuckToTheGroundAnimation;
	
	private Level level;
	private Player myTwin; public void setTwin(Player twin) {myTwin=twin;}

	private static double JUMPFORCE = BunnyHat.SETTINGS.getValue("gameplay/jumpforce");

	private static double MOVEACCEL_GROUND = BunnyHat.SETTINGS.getValue("gameplay/moveacceleration/ground");
	private static double MOVEACCEL_AIR = BunnyHat.SETTINGS.getValue("gameplay/moveacceleration/air");
	
	// gum stuff
	private static double GUMSPEED = BunnyHat.SETTINGS.getValue("gameplay/gumspeed");
	private static int GUM_STUCK_TIME = BunnyHat.SETTINGS.getValue("gameplay/gumstucktime");
	
	private static double CLAMPTOZERO = BunnyHat.SETTINGS.getValue("gameplay/clamptozero");
	
	private int theWinner = -1;
	
	private boolean stuckToTheGround = false;
	private int stuckToTheGroundStartTime;
	
	//sound stuff
	private boolean soundHitBottomPlayed = false;

	public void giveWeapon() {
		this.unarmed = false;
	}
	public void takeWeapon() {
		this.unarmed = true;
	}
	
	public void holdAnimation() {
		
		this.walkAnimation.holdAnimation();
		this.jumpAnimation.holdAnimation();
		this.idleAnimation.holdAnimation();
		this.walkAnimationGun.holdAnimation();
		this.jumpAnimationGun.holdAnimation();
		this.idleAnimationGun.holdAnimation();
	}
	
	public void unholdAnimation() {
		this.walkAnimation.unholdAnimation();
		this.jumpAnimation.unholdAnimation();
		this.idleAnimation.unholdAnimation();
		this.walkAnimationGun.unholdAnimation();
		this.jumpAnimationGun.unholdAnimation();
		this.idleAnimationGun.unholdAnimation();
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
		
		this.updateMe = false;
		
		this.myID = playerNumber;
		
		this.processing = applet;
		
		xSpeed = ySpeed = yAcceleration = 0.0;
		
		this.level = level;
		
		// determine our position
		this.xpos = level.spawnX + 5;// in case
		this.ypos = level.spawnY + 0.5; // there is no spawn point set
		for (int x = 0; x < level.levelWidth; x++) {
			for (int y = 0; y < level.levelHeight; y++) {
				if (level.getMetaDataAt(x, y) == Level.MetaTiles.SpawnPoint.index()) {
					this.xpos = x; // in case
					this.ypos = y; // there is a spawn point
					break;
				}
			}
		}
		
		this.setCollisionEffect(Effects.NONE);
		System.out.println(this.xpos + ", " + this.ypos);
		
		isJumping = true;
		//isMovingSideways = false;
		cannotMoveLeft = false;

		unarmed = true;
		
		this.walkAnimation = new Animation(processing, "graphics/animations/player" + playerNumber + "run");
		this.idleAnimation = new Animation(processing, "graphics/animations/player" + playerNumber + "idle");
		this.jumpAnimation = new Animation(processing, "graphics/animations/player" + playerNumber + "jump");
		this.walkAnimationGun = new Animation(processing, "graphics/animations/player" + playerNumber + "runGun");
		this.idleAnimationGun = new Animation(processing, "graphics/animations/player" + playerNumber + "idleGun");
		this.jumpAnimationGun = new Animation(processing, "graphics/animations/player" + playerNumber + "jumpGun");
		this.winAnimation = new Animation(processing, "graphics/animations/player" + playerNumber + "win");
		this.loseAnimation = new Animation(processing, "graphics/animations/player" + playerNumber + "lose");
		this.stuckToTheGroundAnimation = new Animation(processing, "graphics/animations/player" + playerNumber + "stuck");
		
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
			ret = unarmed?jumpAnimation.getCurrentImage(time):jumpAnimationGun.getCurrentImage(time);
		}
		else if (walkAnimation.isRunning())
		{
			ret = unarmed?walkAnimation.getCurrentImage(time):walkAnimationGun.getCurrentImage(time);
		}
		else if (idleAnimation.isRunning())
		{
			ret = unarmed?idleAnimation.getCurrentImage(time):idleAnimationGun.getCurrentImage(time);
		}
		else if (winAnimation.isRunning()) {
			ret = winAnimation.getCurrentImage(time);
		}
		else if (loseAnimation.isRunning()) {
			ret = loseAnimation.getCurrentImage(time);
		}
		else if (stuckToTheGroundAnimation.isRunning()) {
			ret = stuckToTheGroundAnimation.getCurrentImage(time);
		}
		else
		{
			idleAnimation.start();
			ret = unarmed?idleAnimation.getCurrentImage(time):idleAnimationGun.getCurrentImage(time);
		}
		
		if (facing == Facing.LEFT)
		{
			ret = BImage.mirrorAroundY(processing, ret);
		}
		
		return ret;
	}
	
	public void jump()
	{
		if (stuckToTheGround) return;
		
		if (!isJumping)
		{
			ySpeed = JUMPFORCE;
		}
	}
	
	public void moveLeft()
	{
		if (stuckToTheGround) return;
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
		if (stuckToTheGround) return;
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
	
	public void fireGum() {
		double gumSpeedX = xSpeed + (this.facing == Player.Facing.RIGHT?this.GUMSPEED:-this.GUMSPEED);
		double gumX = (this.facing == Player.Facing.LEFT?xpos-0.2-7/24.0:xpos+2.2);
		BubbleGunGum gum = new BubbleGunGum(gumX, ypos+0.7, gumSpeedX, ySpeed*0.1+0.2, 
				processing, (this.myID == 1 ? BallColor.GIRL : BallColor.BOY));
		gum.setLevel(level);
		
		this.level.addElement(gum);
	}
	
	
	private void controlAnimations()
	{
		boolean hasXSpeed = Math.abs(xSpeed) > CLAMPTOZERO;
		
		
		
		if (isInAir)
		{
			idleAnimation.stop();
			walkAnimation.stop();
			idleAnimationGun.stop();
			walkAnimationGun.stop();
			
			if (jumpAnimation.isStopped())
			{
				jumpAnimation.start();
				jumpAnimationGun.start();
			}
		}
		else if (theWinner != -1) {
			idleAnimation.stop();
			walkAnimation.stop();
			jumpAnimation.stop();
			idleAnimationGun.stop();
			walkAnimationGun.stop();
			jumpAnimationGun.stop();
			stuckToTheGroundAnimation.stop();
			if (theWinner == myID) {
				if (winAnimation.isStopped()) {
					winAnimation.start();
				}
			} else {
				if (loseAnimation.isStopped()) {
					loseAnimation.start();
				}
			}
		}
		else if (stuckToTheGround) {
			idleAnimation.stop();
			walkAnimation.stop();
			jumpAnimation.stop();
			idleAnimationGun.stop();
			walkAnimationGun.stop();
			jumpAnimationGun.stop();
			
			if (stuckToTheGroundAnimation.isStopped()) {
				stuckToTheGroundAnimation.start();
			}
		}
		else if (hasXSpeed)
		{
			idleAnimation.stop();
			jumpAnimation.stop();
			idleAnimationGun.stop();
			jumpAnimationGun.stop();
			stuckToTheGroundAnimation.stop();
			
			if (walkAnimation.isStopped())
			{
				walkAnimation.start();
				walkAnimationGun.start();
			}
		}
		else
		{
			jumpAnimation.stop();
			walkAnimation.stop();
			jumpAnimationGun.stop();
			walkAnimationGun.stop();
			stuckToTheGroundAnimation.stop();
			
			if (idleAnimation.isStopped())
			{
				idleAnimation.start();
				idleAnimationGun.start();
			}
		}
	}

	
	
	public void update(State state, int deltaT)
	{
		if (theWinner == -1) handleInput(state);
		super.update(deltaT);
		controlAnimations();
		
		if (stuckToTheGround) {
			if (processing.millis() - this.stuckToTheGroundStartTime >= this.GUM_STUCK_TIME) {
				stuckToTheGround = false;
			}
		}
		
		if (ySpeed == 0) {
			isJumping = false;
			if (! this.soundHitBottomPlayed) {
				Stereophone.playSound("001", "player_hitground", 100);
				this.soundHitBottomPlayed = true;
			}
		} else {
			this.soundHitBottomPlayed = false;
		}
		//isInAir = isJumping = this.getNewIsJumping();
		
		// any interesting collision partners?
		Object gameElement = this.getBouncePartner();
		if (gameElement instanceof FinishLine) {
			// we won!!!
			this.setChanged();
			HashMap map = new HashMap();
			map.put("IFUCKINGWON", myID);
			this.notifyObservers(map);
			this.myTwin.theWinner = this.theWinner = myID;
			Stereophone.playSound("310", "playerwon", 10000);
		} else if (gameElement instanceof StandardCreature) {
			// player hit a creature
			//((StandardCreature) gameElement).contact(this); creature is informed via collision box
		} else if (gameElement instanceof Door) {
			this.setChanged();
			HashMap map = new HashMap();
			map.put("OHDOORTAKEMEAWAY", myID);
			this.notifyObservers(map);
		} else if (unarmed && gameElement instanceof BubbleGunGum) {
			this.stuckToTheGroundStartTime = processing.millis();
			this.stuckToTheGround = true;
			this.resetBouncePartner();
		}
			

		
		
		if (ySpeed != 0) isInAir = isJumping = true;
		
		
	}
	
	
	private void handleInput(State state)
	{
		//if (switchHappening) return; // no input while a switch is happening
		
		boolean jumpbutton = (this.myID == 1) ?
				(state.containsKey('w') && state.get('w')) :
				(state.containsKey('i') && state.get('i'));
		
		boolean leftbutton = (this.myID == 1) ?
				(state.containsKey('a') && state.get('a')) :
				(state.containsKey('j') && state.get('j'));
				
		boolean rightbutton = (this.myID == 1) ?
				(state.containsKey('d') && state.get('d')) :
				(state.containsKey('l') && state.get('l'));
				
		boolean downbutton = (this.myID == 1) ?
				(state.containsKey('s') && state.get('s')) :
				(state.containsKey('k') && state.get('k'));
				
		boolean shootbutton = state.containsKey('.') && state.get('.');

		// a player should always have to press jump again for another jump
		if (didJump && !jumpbutton) didJump = false;  
		// same for firing a gum
		if (didFire && !shootbutton) didFire = false;
		
		if (jumpbutton && !didJump)
		{
			jump();
			this.didJump = true;
			if (BunnyHat.TWIN_JUMP)
			{
				myTwin.jump();
			}
		}
		
		if (leftbutton)
		{
			moveLeft();
		}
		
		if (rightbutton)
		{
			moveRight();
			
		}
		
		if (downbutton)
		{
			// Use stuff
		}
		
		if (shootbutton && !unarmed && !didFire) {
			didFire = true;
			fireGum();
		}
		
	}
	

	@Override
	public void collisionDraw(PGraphics cb, int xOff, int yOff)
	{
		// TODO Auto-generated method stub
		cb.noFill();
		cb.stroke(255, 0, 0);
		//System.out.println("draw player "+xpos+":"+ypos);
		cb.rect((float)xpos*2, (float)ypos*2+yOff, (float)this.collisionBoxWidth()*2, (float)this.collisionBoxHeight()*2);
	}

}
