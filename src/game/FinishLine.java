package game;
import processing.core.*;

public class FinishLine extends CollisionBox
{

	PApplet parent;
	float xPos, yPos, width, height;
	
	public FinishLine(PApplet p, double x, double y, int w, int h)
	{
		super(x, y, w, h);
		
		xPos = (float)x;
		yPos = (float)y;
		width = w;
		height = h;
		
		parent = p;
		
		this.setGameElement(this);
	}

	public void collisionDraw(PGraphics cb, int xOff, int yOff)
	{
		cb.fill(0, 255, 0);
		cb.stroke(0, 255, 0);
		cb.rect(xPos*2, yPos*2+yOff, width*2, height*2);
	}

	@Override
	protected void bounce(Object gameElement)
	{
		// TODO Auto-generated method stub
		
	}

	
}
