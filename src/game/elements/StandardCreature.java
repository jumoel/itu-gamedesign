package game.elements;

import game.BunnyHat;
import game.Player;
import game.graphics.Animation;
import game.level.Level;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

public class StandardCreature extends GameElement
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

	public StandardCreature(PApplet applet, int creatureNumber, int effect, Level level)
	{
		super(10, 10, 2, 3);
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
	
	public void update() {
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
	
	@Override
	public PImage getCurrentTexture()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void collisionDraw(PGraphics cb, int xOff, int yOff)
	{
		// TODO Auto-generated method stub

	}

}
