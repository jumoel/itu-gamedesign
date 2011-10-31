import processing.core.*;

public class Obstacle
{

	PApplet parent;
	public int height;
	public int width;
	public float xPos;
	public float yPos;

	public Obstacle(PApplet p, float x, float y, int w, int h)
	{

		parent = p;
		height = h;
		width = w;
		xPos = x;
		yPos = y;

	}

	public void draw()
	{
		parent.fill(255);
		parent.stroke(255);
		parent.rect(xPos, yPos, width, height);
	}

	public boolean collidesWith(Player p)
	{
		return (p.y >= yPos && p.y <= (yPos + height) && p.x >= xPos && p.x <= (xPos + width));
	}
}
