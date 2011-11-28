package game.creatures;
import processing.core.*;
import game.BunnyHat;
import game.CollisionBox;
import game.Player;
import game.graphics.Animation;
import game.level.Level;

/**
 * Standard creature - only existing in one of the dreams
 * 	
 * use it for e.g. temporary creatures in just one dream
 * 
 * @author samuelwalz
 *
 */
public abstract class Creature extends CollisionBox
{
	
	private PApplet processing;
	private Animation idleAnimation;
	private Animation interactAnimation;
	private Animation walkAnimation;
	public int creatureNumber;
	
	public int xPos;
	public int yPos;
	public int lastXPos;
	public int lastYPos;
	private int MOVESPEED = BunnyHat.SETTINGS.getValue("gameplay/creatures/speed");
	private boolean moveRight = false;
	
	/*
	 * effect types
	 * effect 1:
	 * effect 2:
	 * 
	 */
	private int effectType;
	
	public Creature(PApplet applet, int creatureNumber, int effect, Level level){
		super(level.spawnX + 0.5, level.spawnY + 0.5, 2, 3);
		super.setLevel(level);
		super.setGameElement(this);
		
		this.processing = applet;

		this.idleAnimation = new Animation(processing, "graphics/animations/creature" + creatureNumber + "idle");
		this.interactAnimation = new Animation(processing, "graphics/animations/creature" + creatureNumber + "interact");
		this.walkAnimation = new Animation(processing, "graphics/animations/creature" + creatureNumber + "walk");
		
		this.idleAnimation.start();

	}
	
	/* Called when players collides with creatures from PlayerView */
	public void contact(Player player){
	
		walkAnimation.stop();
		interactAnimation.start();

		switch(effectType){
			
			case 1:
				player.xSpeed = -player.xSpeed;
				
				break;
			case 2:
				
				break;
				
			default:
				
				break;
		}
	}
	
	/* creature moves */
	public void update(){
		lastXPos = xPos;
		/*
		 * Implement collision with walls
		 */
		
		if (moveRight){
			xPos = xPos + MOVESPEED;
		} else {
			xPos = xPos - MOVESPEED;
		}
		
		
		 	
		
	}
		
	
	
	
	
}
