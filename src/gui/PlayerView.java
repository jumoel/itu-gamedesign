package gui;

import game.State;
import processing.core.*;

public class PlayerView extends Updateable
{
	private int width;
	private int height;
	private PApplet processing;
	
	public PlayerView(int width, int height, PApplet applet)
	{
		this.width = width;
		this.height = height;
		this.processing = applet;
	}
	
	public void update(State state, int xpos, int ypos)
	{
		processing.fill(255);
		processing.rect(xpos, ypos, width, height);
	}
}
