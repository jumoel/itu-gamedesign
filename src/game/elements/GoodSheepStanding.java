package game.elements;

import game.BunnyHat;
import game.graphics.Animation;
import game.master.GameMaster;
import game.sound.Stereophone;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

public class GoodSheepStanding extends GameElement
{
	private Animation sheepAnimation;
	private int timeTillNextSound = 0;
	private String[] sounds = {"131", "132", "133", "134", "135", 
			                   "136", "137", "138", "139", "140",
			                   "141", "142", "143"};
	
	
	public GoodSheepStanding(double x, double y, PApplet processing)
	{
		super(x, y, 3, 3, processing);
		this.processing = processing;
		this.setCollisionEffect(Effects.GOOD_SHEEP_BOUNCE);
		
		sheepAnimation = new Animation(processing, "graphics/animations/goodSheepStanding");
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
			playSound(soundToPlay, "good sheep standing", 300);
			this.timeTillNextSound = GameMaster.getNewTimeTillNextAction(5000, 0.7);
		} else {
			this.timeTillNextSound -= deltaT;
		}
	}

}
