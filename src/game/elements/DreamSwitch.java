package game.elements;

import game.graphics.Animation;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

public class DreamSwitch extends GameElement
{

	private PApplet processing;
	private Animation dreamSwitchAnimation;
	
	public DreamSwitch(double x, double y, PApplet p)
	{
		super(x, y, 3, 1);
		this.setCollisionEffect(Effects.BOUNCE);
		
		processing = p;
		
		this.setGameElement(this);
		
		this.dreamSwitchAnimation = new Animation(p, "graphics/animations/dreamSwitch");
		this.dreamSwitchAnimation.start();
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
			this.dreamSwitchAnimation.start();
			ret = this.dreamSwitchAnimation.getCurrentImage(time);
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
		
	}

}