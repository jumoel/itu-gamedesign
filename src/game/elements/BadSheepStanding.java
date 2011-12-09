package game.elements;

import game.BunnyHat;
import game.graphics.Animation;
import game.master.GameMaster;
import game.sound.Stereophone;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

public class BadSheepStanding extends GameElement
{
	private Animation sheepAnimation;
	private int timeTillNextSound = 0;
	private String[] sounds = {"145", "146", "147", "148", "149", 
			                   "150", "151", "152", "153", "154",
			                   "155", "156", "157", "158", "159",
			                   "160", "161", "162", "163", "164"};
	
	
	public BadSheepStanding(double x, double y, PApplet processing)
	{
		super(x, y, 3, 3, processing);
		this.processing = processing;
		this.setCollisionEffect(Effects.BAD_SHEEP_BOUNCE);
		
		sheepAnimation = new Animation(processing, "graphics/animations/badSheepStanding");
		sheepAnimation.start(true, true);
		timeTillNextSound = GameMaster.getNewTimeTillNextAction(5000, 0.7);
	}

	
	
	@Override
	public PImage getCurrentTexture()
	{
		int time = processing.millis();
		return sheepAnimation.getCurrentImage(time);
	}

	@Override
	public void collisionDraw(PGraphics cb, int xOff, int yOff)
	{
		// TODO Auto-generated method stub

	}
	
	@Override
	public void update(int deltaT) {
		if (this.timeTillNextSound < 0) {
			double dice = Math.random();
			String soundToPlay = sounds[(int)(dice * sounds.length)];
			
			playSound(soundToPlay, "bad sheep standing", 300);
			
			this.timeTillNextSound = GameMaster.getNewTimeTillNextAction(5000, 0.7);
		} else {
			this.timeTillNextSound -= deltaT;
		}
	}

}
