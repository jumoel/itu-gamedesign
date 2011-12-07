package game;
import game.graphics.Animation;
import processing.core.*;
import util.BImage;

public class Door extends CollisionBox
{

	PApplet processing;
	float xPos, yPos, width, height;
	
	private Animation doorShow, doorStay, doorBlow;
	
	
	public Door(PApplet p, double x, double y, int w, int h)
	{
		super(x, y, w, h);
		super.setCollisionEffect(Effects.NONE);
		
		xPos = (float)x;
		yPos = (float)y;
		width = w;
		height = h;
		
		processing = p;
		
		this.setGameElement(this);
		
		this.doorShow = new Animation(p, "graphics/animations/doorShow");
		this.doorStay = new Animation(p, "graphics/animations/doorStay");
		this.doorBlow = new Animation(p, "graphics/animations/doorBlow");
		this.doorStay.start();
	}
	
	public void updatePosition(double x, double y) {
		super.updatePosition(x, y);
		this.xPos = (float)x;
		this.yPos = (float)y;
	}
	
	// Return the current texture (ie. specific animation sprite)
	public PImage getCurrentTexture()
	{
		PImage ret;
		int time = processing.millis();
		
		if (doorShow.isRunning())
		{
			ret = doorShow.getCurrentImage(time);
		}
		else if (doorBlow.isRunning())
		{
			ret = doorBlow.getCurrentImage(time);
		}
		else
		{
			doorStay.start();
			ret = doorStay.getCurrentImage(time);
		}
		
		return ret;
	}
	
	public void showDoor() {
		doorShow.start(false);
		doorBlow.stop();
		doorStay.stop();
	}
	
	public void blowDoor() {
		doorShow.stop();
		doorStay.stop();
		doorBlow.start(false);
		
	}

	public void collisionDraw(PGraphics cb, int xOff, int yOff)
	{
		cb.fill(0, 255, 0);
		cb.stroke(255, 255, 0);
		cb.rect(xPos*2, yPos*2+yOff, width*2, height*2);
	}

	@Override
	protected void bounce(Object gameElement)
	{
		// TODO Auto-generated method stub
		
	}

	
}
