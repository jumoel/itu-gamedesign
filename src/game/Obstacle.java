package game;
import processing.core.*;

public class Obstacle extends CollisionBox
{

	PApplet parent;
	float xPos, yPos, width, height;
	
	public Obstacle(PApplet p, double x, double y, int w, int h)
	{
		super(x, y, w, h);
		super.setGameElement(this);
		
		xPos = (float)x;
		yPos = (float)y;
		width = w;
		height = h;
		
		parent = p;
	}

	public void collisionDraw(PGraphics cb, int xOff, int yOff)
	{
		cb.noFill();
		cb.stroke(0);
		cb.rect(xPos*2, yPos*2+yOff, width*2, height*2);
	}

	@Override
	protected void bounce(Object gameElement)
	{
		// TODO Auto-generated method stub
		
	}

	
}
