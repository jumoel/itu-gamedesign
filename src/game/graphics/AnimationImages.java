package game.graphics;

import game.BunnyHat;

import java.util.HashMap;

import processing.core.PApplet;
import processing.core.PImage;
import util.BImage;

public class AnimationImages
{
	private HashMap<String, PImage[]> spritemap;
	private PApplet processing;
	private static String ANIMATIONPATH = BunnyHat.SETTINGS.getValue("graphics/animations/animationpath");
	
	public AnimationImages(PApplet applet)
	{
		this.processing = applet;
		this.spritemap = new HashMap<String, PImage[]>();
	}
	
	public PImage[] getSprites(String animationname)
	{
		String spritesheet = BunnyHat.SETTINGS.getValue(animationname + "/path");
		int width = BunnyHat.SETTINGS.getValue(animationname + "/framewidth");
		int height = BunnyHat.SETTINGS.getValue(animationname + "/frameheight");
		
		if (!spritemap.containsKey(animationname))
		{
			spritemap.put(animationname, BImage.cutImageSprite(processing, processing.loadImage(ANIMATIONPATH + spritesheet), width, height));
		}
		
		return spritemap.get(animationname);
	}
}
