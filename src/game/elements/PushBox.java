package game.elements;

import game.graphics.Animation;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

public class PushBox extends GameElement
{

	private PApplet processing;
	private Animation pushBoxAnimation;
	private PushBox myTwinBox; 
	
	public PushBox(double x, double y, PApplet p)
	{
		super(x, y, 3, 2.8, p);
		this.setCollisionEffect(Effects.PUSH);
		
		processing = p;
		
		this.setGameElement(this);
		
		this.pushBoxAnimation = new Animation(p, "graphics/animations/pushBox");
		this.pushBoxAnimation.start();
	}
	
	public void setBoxTwin(PushBox twin) {
		myTwinBox = twin;
		this.collisionPartnerX = twin;
	}
	
	public boolean twinBoxPushedRight() {
		return myTwinBox.isBeingPushedRight;
	}
	public boolean twinBoxPushedLeft() {
		return myTwinBox.isBeingPushedLeft;
	}

	@Override
	public PImage getCurrentTexture()
	{
		PImage ret;
		int time = processing.millis();
		
		if (false)
		{
		}
		else
		{
			this.pushBoxAnimation.start();
			ret = this.pushBoxAnimation.getCurrentImage(time);
		}
		
		return ret;
	}

	
	@Override
	public void collisionDraw(PGraphics cb, int xOff, int yOff)
	{
		// TODO Auto-generated method stub
		

	}
	
	
	
	@Override
	public void update(int deltaT) {
		if ((isBeingPushedRight && !myTwinBox.isBeingPushedLeft) 
				|| (isBeingPushedLeft && !myTwinBox.isBeingPushedRight)) { // we don't have to update always, right?
			//System.out.println("update my twin box");
			/*double boxDistance = xpos - myTwinBox.x();
			if (boxDistance > 0.1) { 
				myTwinBox.xpos = myTwinBox.x()+0.1;
				myTwinBox.ySpeed = ySpeed;
				myTwinBox.updatePosition(myTwinBox.xpos, myTwinBox.ypos);
				myTwinBox.isInAir = true;
			} else if (boxDistance < -0.1) {
				myTwinBox.xpos = myTwinBox.x()-0.1;
				myTwinBox.isInAir = true;
			}*/
			//myTwinBox.xpos = xpos;
			//System.out.println(ypos+" vs "+myTwinBox.ypos);
		}
		super.update(deltaT);
	}

}
