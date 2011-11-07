package game;
import processing.core.*;

public class Player
{
	private PApplet processing;
	private PImage texture;
	public int xpos, ypos;
	
	private boolean isInAir;
	
	private float yForce;
	private float xForce;

	public Player(PApplet applet)
	{
		this.processing = applet;
		
		texture = processing.loadImage("player.png");
		
		xpos = 50;
		ypos = 50;
		
		isInAir = true;
	}
	
	// Return the current texture (ie. specific animation sprite)
	public PImage getCurrentTexture()
	{
		return texture;
	}

	public void update(int deltaT)
	{
		
	}
}
