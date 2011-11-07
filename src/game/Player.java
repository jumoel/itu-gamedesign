package game;
import processing.core.*;

public class Player
{
	private PApplet processing;
	private PImage texture;
	private int xpos, ypos;

	public Player(PApplet applet)
	{
		this.processing = applet;
		
		texture = processing.loadImage("../player.png");
		
		xpos = 50;
		ypos = 50;
	}

	public void update(PGraphics g)
	{
		int texX, texY;
		
		texX = xpos - texture.width / 2;
		texY = ypos - texture.height;
		g.image(texture, texX, texY);
		
		g.fill(processing.color(255, 0, 0, 255));
		g.ellipse(xpos, ypos, 4, 4);
	}
}
