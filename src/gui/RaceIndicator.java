package gui;

import game.State;
import processing.core.*;

public class RaceIndicator extends Updateable
{
	private int width;
	private int height;
	private PApplet processing;
	
	public RaceIndicator(int width, int height, PApplet applet)
	{
		this.width = width;
		this.height = height;
		this.processing = applet;
	}
	
	public void update(State state, int xpos, int ypos)
	{
		processing.fill(processing.color(255, 0, 0, 255));
		processing.rect(xpos, ypos, width, height);
	}
}
