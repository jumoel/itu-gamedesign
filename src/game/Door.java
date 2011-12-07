package game;
import game.graphics.Animation;
import processing.core.*;
import util.BImage;

public class Door extends CollisionBox
{

	PApplet processing;
	float xPos, yPos, width, height;
	public boolean accessible = true;
	
	private Animation doorShow, doorStay, doorBlow, noDoorShow, noDoorStay, noDoorBlow;
	
	
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
		this.noDoorShow = new Animation(p, "graphics/animations/noDoorShow");
		this.noDoorStay = new Animation(p, "graphics/animations/noDoorStay");
		this.noDoorBlow = new Animation(p, "graphics/animations/doorBlow");
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
			if (accessible) {
				ret = doorShow.getCurrentImage(time);
			} else {
				ret = noDoorShow.getCurrentImage(time);
			}
		}
		else if (doorBlow.isRunning())
		{
			if (accessible) {
				ret = doorBlow.getCurrentImage(time);
			} else {
				ret = noDoorBlow.getCurrentImage(time);
			}
		}
		else
		{
			if (accessible) {
				doorStay.start();
				ret = doorStay.getCurrentImage(time);
			} else {
				noDoorStay.start();
				ret = noDoorStay.getCurrentImage(time);
			}
		}
		
		return ret;
	}
	
	public void showDoor() {
		doorShow.start(false, true);
		doorBlow.stop();
		doorStay.stop();
		noDoorShow.start(false, true);
		noDoorBlow.stop();
		noDoorStay.stop();
	}
	
	public void blowDoor() {
		doorShow.stop();
		doorStay.stop();
		doorBlow.start(false, true);
		noDoorShow.stop();
		noDoorStay.stop();
		noDoorBlow.start(false, true);
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
