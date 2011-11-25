package game;
import processing.core.*;

public class Obstacle extends CollisionBox
{

	PApplet parent;
	float xPos, yPos, width, height;
	
	public Obstacle(PApplet p, double x, double y, int w, int h)
	{
		super(x, y, w, h);
		
		xPos = (float)x;
		yPos = (float)y;
		width = w;
		height = h;
		
		parent = p;
	}

	public void collisionDraw()
	{
		parent.noFill();
		parent.stroke(0);
		parent.rect(xPos*2, yPos*2, width*2, height*2);
	}

	@Override
	protected void bounce(Object gameElement)
	{
		// TODO Auto-generated method stub
		
	}

	
}
