package game;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;


import game.control.SoundControl;
import game.elements.BubbleGunGum;
import game.elements.BubbleGunGum.BallColor;
import game.elements.DreamSwitch;
import game.elements.GameElement; 
import game.elements.StandardCreature;
import game.graphics.Animation;
import game.gui.AmazingSwitchWitch;
import game.level.Level;
import game.level.Level.DreamStyle;
import game.sound.Stereophone;
import processing.core.*;
import util.BImage;
import util.BMath;
import util.BPoint;

public class Player extends GameElement implements Observer
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

	
	
	//private double yAcceleration;
	//public double ySpeed;
	//public double xSpeed;
	
	//private Facing facing;

	private boolean unarmed = true;
	private boolean onSpeed = false;
	private int speedLeftMs = 0;
	private boolean onValium = false;
	private int valiumLeftMs = 0;
	private boolean airJump = false;
	
	private Animation walkAnimation;
	private Animation jumpAnimation;
	private Animation idleAnimation;
	private Animation walkAnimationGun;
	private Animation jumpAnimationGun;
	private Animation idleAnimationGun;
	private Animation winAnimation;
	private Animation loseAnimation;
	private Animation stuckToTheGroundAnimation;
	private Animation pushAnimation;
	
	private Level level;
	private Player myTwin; public void setTwin(Player twin) {myTwin=twin;}

	private static double JUMPFORCE = BunnyHat.SETTINGS.getValue("gameplay/jumpforce");

	private static double MOVEACCEL_GROUND = BunnyHat.SETTINGS.getValue("gameplay/moveacceleration/ground");
	private static double MOVEACCEL_AIR = BunnyHat.SETTINGS.getValue("gameplay/moveacceleration/air");
	private static double MAXSPEED = BunnyHat.SETTINGS.getValue("gameplay/maxspeed");
	// gum stuff
	private static double GUMSPEED = BunnyHat.SETTINGS.getValue("gameplay/gumspeed");
	private static int GUM_STUCK_TIME = BunnyHat.SETTINGS.getValue("gameplay/gumstucktime");
	
	private static double CLAMPTOZERO = BunnyHat.SETTINGS.getValue("gameplay/clamptozero");
	
	private int theWinner = -1;
	public boolean tookTheDoor = false;
	
	private boolean stuckToTheGround = false;
	private int stuckToTheGroundStartTime;
	
	private double moveAccelModifier = 1.0;
	
	//sound stuff
	private boolean soundHitBottomPlayed = false;

	public void giveWeapon() {
		this.unarmed = false;
	}
	public void takeWeapon() {
		this.unarmed = true;
	}
	
	public void setLevel(Level level) {
		this.level = level;
		super.setLevel(level);
	}
	
	public Player(PApplet applet, int playerNumber, Level level)
	{
		super(level.spawnX + 0.5, level.spawnY + 0.5, 2, 3, applet);
		super.setLevel(level);
		super.setGameElement(this);
		this.isAbleToPush = true; // hardcore pusher!
		
		this.updateMe = false;
		this.zIndex = 100;
		this.drawTrail = false;
		
		this.myID = playerNumber;
		
		this.processing = applet;
		
		xSpeed = ySpeed = yAcceleration = 0.0;
		
		this.level = level;
		
		// determine our position
		this.xpos = level.spawnX; //+ 5;// in case
		this.ypos = level.spawnY;// + 0.5; // there is no spawn point set
		
		//System.out.println("before: " + this.xpos + ", " + this.ypos);
		
		for (int x = 0; x < level.levelWidth; x++) {
			for (int y = 0; y < level.levelHeight; y++) {
				if (level.getMetaDataAt(x, y) == Level.MetaTiles.SPAWNPOINT.index()) {
					this.xpos = x; // in case
					this.ypos = y; // there is a spawn point
					break;
				}
			}
		}
		//System.out.println("before: " + this.xpos + ", " + this.ypos);
		
		this.setCollisionEffect(Effects.NONE);
		//System.out.println(this.xpos + ", " + this.ypos);
		
		isJumping = true;
		//isMovingSideways = false;
		//cannotMoveLeft = false;

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
		this.pushAnimation = new Animation(processing, "graphics/animations/player" + playerNumber + "push");
		
		this.idleAnimation.start();
		
		this.facing = Facing.RIGHT;
	}
	
	private void startAnimation(Animation anim) {
		anim.start();
		if (anim != walkAnimation) this.walkAnimation.stop();
		if (anim != idleAnimation) this.idleAnimation.stop();
		if (anim != jumpAnimation) this.jumpAnimation.stop();
		if (anim != walkAnimationGun) this.walkAnimationGun.stop();
		if (anim != idleAnimationGun) this.idleAnimationGun.stop();
		if (anim != jumpAnimationGun) this.jumpAnimationGun.stop();
		if (anim != winAnimation) this.winAnimation.stop();
		if (anim != loseAnimation) this.loseAnimation.stop();
		if (anim != stuckToTheGroundAnimation) this.stuckToTheGroundAnimation.stop();
		if (anim != pushAnimation) this.pushAnimation.stop();
	}
	
	
	
	// Return the current texture (ie. specific animation sprite)
	public PImage getCurrentTexture()
	{
		PImage ret;
		int time = processing.millis();
	
		
		if (pushAnimation.isRunning()) {
			ret = pushAnimation.getCurrentImage(time);
		}else if (jumpAnimation.isRunning() || jumpAnimationGun.isRunning())
		{
			ret = unarmed?jumpAnimation.getCurrentImage(time):jumpAnimationGun.getCurrentImage(time);
		}
		else if (walkAnimation.isRunning() || walkAnimationGun.isRunning())
		{
			ret = unarmed?walkAnimation.getCurrentImage(time):walkAnimationGun.getCurrentImage(time);
		}
		else if (idleAnimation.isRunning() || idleAnimationGun.isRunning())
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
		
		if (ret == null) System.out.println("no anim? serious trouble!"+time);
		
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
			xAcceleration = 0;
			return;
		}
		
		if (isInAir)
		{
			xAcceleration = -MOVEACCEL_AIR * this.moveAccelModifier;
		}
		else
		{
			xAcceleration = -MOVEACCEL_GROUND * this.moveAccelModifier;
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
			xAcceleration = 0;
			return;
		}
		
		if (isInAir)
		{
			xAcceleration = MOVEACCEL_AIR * this.moveAccelModifier;
		}
		else
		{
			xAcceleration = MOVEACCEL_GROUND * this.moveAccelModifier;
		}
	}
	
	public void fireGum() {
		double gumSpeedX = xSpeed + (this.facing == Player.Facing.RIGHT?this.GUMSPEED:-this.GUMSPEED);
		double gumX = (this.facing == Player.Facing.LEFT?xpos-0.2-7/24.0:xpos+2.2);
		BubbleGunGum gum = new BubbleGunGum(gumX, ypos+0.7, gumSpeedX, ySpeed*0.1+0.2, 
				processing, (this.myID == 1 ? BallColor.GIRL : BallColor.BOY));
		gum.setLevel(level);
		
		this.level.addElement(gum);
		double dice = Math.random();
		Stereophone.playSound(dice > 0.5 ? "010" : "011", "gun shoot", 100);
	}
	
	
	private void controlAnimations()
	{
		boolean hasXSpeed = Math.abs(xSpeed) > CLAMPTOZERO;
		
		
		if (ourPushable != null) {
			if (pushAnimation.isStopped()) startAnimation(pushAnimation);
		}
		else if (isInAir)
		{
			if (unarmed) {
				if (jumpAnimation.isStopped()) startAnimation(jumpAnimation);
			} else {
				if (jumpAnimationGun.isStopped()) startAnimation(jumpAnimationGun);
			}
		}
		else if (theWinner != -1) {
			if (theWinner == myID) {
				if (winAnimation.isStopped()) {
					startAnimation(winAnimation);
				}
			} else {
				if (loseAnimation.isStopped()) {
					startAnimation(loseAnimation);
				}
			}
		}
		else if (stuckToTheGround) {
			
			if (stuckToTheGroundAnimation.isStopped()) {
				startAnimation(stuckToTheGroundAnimation);
			}
		}
		else if (hasXSpeed)
		{
			if (unarmed) {
				if (walkAnimation.isStopped()) startAnimation(walkAnimation);
			} else {	
				if (walkAnimationGun.isStopped()) startAnimation(walkAnimationGun);
			}
		}
		else
		{	
			if (unarmed) {
				if (idleAnimation.isStopped()) startAnimation(idleAnimation); 
			} else {
				if (idleAnimationGun.isStopped()) startAnimation(idleAnimationGun);
			}
		}
	}

	
	
	public void update(State state, int deltaT)
	{
		if (theWinner == -1) handleInput(state);
	
		// effects counter each other
		if (onValium && onSpeed) onValium = onSpeed = false; 
		
		if (onSpeed) {
			if (this.speedLeftMs < 0) {
				onSpeed = false;
				this.drawTrail = false;
				this.moveAccelModifier = 1.0;
			} else {
				this.speedLeftMs -= deltaT;
				this.drawTrail = true;
				this.moveAccelModifier = 1.5;
			}
		}
		
		if (onValium) {
			if (this.valiumLeftMs < 0) {
				onValium = false;
				this.drawTrail = false;
				this.moveAccelModifier = 1.0;
			} else {
				this.valiumLeftMs -= deltaT;
				this.drawTrail = true;
				this.moveAccelModifier = 0.5;
			}
		}
		
		if (isJumping && airJump) ySpeed = JUMPFORCE; 
		if (airJump) airJump = false;
		
		
		while ((level.getMetaDataAt((int)(xpos+0.2), (int)(ypos+1.5)) == Level.MetaTiles.OBSTACLE.index())
				|| level.getMetaDataAt((int)(xpos+0.8), (int)(ypos+1.5)) == Level.MetaTiles.OBSTACLE.index()) {
			System.out.println("correct");
			
			ypos += 1;
		}
		
		super.update(deltaT);
		
		double xSignum = Math.signum(xSpeed);
		xSpeed = BMath.clamp(xSpeed, 0, xSignum * MAXSPEED * this.moveAccelModifier);
		
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
		if (unarmed && gameElement instanceof FinishLine) { // player has to be unarmed
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
		} else if (gameElement instanceof Door 
				&& !this.myTwin.tookTheDoor
				&& this.myTwin.x() > this.x()) { // only one twin can use the door
			
			this.setChanged();
			HashMap map = new HashMap();
			map.put("OHDOORTAKEMEAWAY", myID);
			this.notifyObservers(map);
		} else if (unarmed && gameElement instanceof BubbleGunGum) {
			this.stuckToTheGroundStartTime = processing.millis();
			this.stuckToTheGround = true;
			this.resetBouncePartner();
			this.setChanged();
			HashMap map = new HashMap();
			map.put("IGOTGUMMED", myID);
			this.notifyObservers(map);
		} else if (unarmed && gameElement instanceof DreamSwitch 
				&& ySpeed < 0 && ((DreamSwitch)gameElement).usable) {
			this.resetBouncePartner();
			this.setChanged();
			HashMap map = new HashMap();
			map.put("IHITTHESWITCH", myID);
			this.notifyObservers(map);
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
		
		if (!rightbutton && !leftbutton) {
			xAcceleration = 0;
		}
		
		if (downbutton)
		{
			// Use stuff
		}
		
		if (shootbutton && !unarmed && !didFire) {
			didFire = true;
			state.put('.', false);
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
	
	public void deleteAllTheStuff()
	{
		if (walkAnimation != null)
		{
			walkAnimation.deleteAllTheStuff();
			walkAnimation = null;
		}
		
		if (jumpAnimation != null)
		{
			jumpAnimation.deleteAllTheStuff();
			jumpAnimation = null;
		}
		
		if (idleAnimation != null)
		{
			idleAnimation.deleteAllTheStuff();
			idleAnimation = null;
		}
		
		if (walkAnimationGun != null)
		{
			walkAnimationGun.deleteAllTheStuff();
			walkAnimationGun = null;
		}
		
		if (jumpAnimationGun != null)
		{
			jumpAnimationGun.deleteAllTheStuff();
			jumpAnimationGun = null;
		}
		
		if (idleAnimationGun != null)
		{
			idleAnimationGun.deleteAllTheStuff();
			idleAnimationGun = null;
		}
		
		if (winAnimation != null)
		{
			winAnimation.deleteAllTheStuff();
			winAnimation = null;
		}
		
		if (loseAnimation != null)
		{
			loseAnimation.deleteAllTheStuff();
			loseAnimation = null;
		}
		
		if (stuckToTheGroundAnimation != null)
		{
			stuckToTheGroundAnimation.deleteAllTheStuff();
			stuckToTheGroundAnimation = null;
		}
		
		myTwin = null;
		
		processing = null;
	}
	@Override
	public void update(Observable arg0, Object arg1)
	{
		if (arg0 instanceof SoundControl) {
			HashMap map = (HashMap)arg1;
			String detector = (String)map.get("detector");
			String pattern = (String)map.get("pattern");
			//System.out.println("fand:"+pattern);
			if (detector == "HF" && level.dream == DreamStyle.GOOD) {
				//System.out.println("HF detector sagt");
				if (pattern.contentEquals("SpeedUp")) {
					System.out.println("let's speed up");
					this.onSpeed = true;
					this.speedLeftMs = 10000;
					//this.moveAccelModifier = 1.5;
					//this.drawTrail = true;
				} else if (pattern.contentEquals("AirJump")) {
					this.airJump = true;
				}
			} else if (detector == "LF" && level.dream == DreamStyle.BAD) {
				
			} else if (detector == "HF" && level.dream == DreamStyle.BAD) {
				if (pattern.contentEquals("SlowDown")) {
					System.out.println("let's slow down");
					this.onValium = true;
					this.valiumLeftMs = 10000;
					//this.moveAccelModifier = 1.5;
					//this.drawTrail = true;
				}
			}
		}
		
	}

}
