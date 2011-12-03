package game.elements;

import game.level.Level;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

public class BubbleGunGum extends GameElement
{
	
	
	public BubbleGunGum(double x, double y, double xSpeed, double ySpeed)
	{
		super(x, y, 1, 1);
		super.setCollisionEffect(Effects.NONE);
		// TODO Auto-generated constructor stub
	}

	@Override
	public PImage getCurrentTexture()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void collisionDraw(PGraphics cb, int xOff, int yOff)
	{
		// TODO Auto-generated method stub

	}


}
