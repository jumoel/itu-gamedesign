package game.graphics;

import game.BunnyHat;
import processing.core.PApplet;
import processing.core.PImage;
import util.BImage;

public class Animation
{
	public static String ANIMATIONPATH = BunnyHat.SETTINGS.getValue("graphics/animations/animationpath");
	
	
	private PApplet processing;
	private PImage[] sprites;
	private int millisPerFrame;
	private int numberOfFrames;
	
	private boolean isRunning;
	private int startTime;
	
	public Animation(PApplet p, String settingskey)
	{
		int fps = BunnyHat.SETTINGS.getValue(settingskey + "/fps");
		int width = BunnyHat.SETTINGS.getValue(settingskey + "/framewidth");
		int height = BunnyHat.SETTINGS.getValue(settingskey + "/frameheight");
		String spritesheet = BunnyHat.SETTINGS.getValue(settingskey + "/path");
		
		this.processing = p;
		this.millisPerFrame = 1000 / fps;
		
		this.sprites = BImage.cutImageSprite(processing, processing.loadImage(ANIMATIONPATH + spritesheet), width, height);
		this.numberOfFrames = sprites.length;
		
		this.isRunning = false;
	}
	
	public boolean isRunning()
	{
		return isRunning;
	}
	
	public boolean isStopped()
	{
		return !isRunning;
	}
	
	public void start()
	{
		this.isRunning = true;
		this.startTime = processing.millis();
	}
	
	public void stop()
	{
		this.isRunning = false;
	}
	
	public PImage getCurrentImage(int time)
	{
		if (!isRunning)
		{
			return null;
		}
		
		int diff = time - startTime;
		int timeindex = diff % 1000;
		
		int frame = (timeindex / millisPerFrame) % numberOfFrames;
		
		
		if (frame < numberOfFrames)
		{
			return sprites[frame];
		}
		else
		{
			System.out.println("Frame: " + frame + ". Number of frames: " + numberOfFrames);
			System.out.println("Diff: " + diff + ". Timeindex: " + timeindex + ". Millis per frame: " + millisPerFrame);
			return null;
		}
	}
}
