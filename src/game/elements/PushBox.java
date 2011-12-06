package game.elements;

import game.graphics.Animation;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

public class PushBox extends GameElement
{

	private PApplet processing;
	private Animation pushBoxAnimation;
	
	public PushBox(double x, double y, PApplet p)
	{
		super(x, y, 3, 3);
		this.setCollisionEffect(Effects.PUSH);
		
		processing = p;
		
		this.setGameElement(this);
		
		this.pushBoxAnimation = new Animation(p, "graphics/animations/pushBox");
		this.pushBoxAnimation.start();
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
		super.update(deltaT);
	}

}
