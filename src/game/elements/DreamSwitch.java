package game.elements;

import processing.core.PGraphics;
import processing.core.PImage;

public class DreamSwitch extends GameElement
{

	public DreamSwitch(double x, double y)
	{
		super(x, y, 3, 1);
		this.setCollisionEffect(Effects.BOUNCE);
		this.updateMe = true;
		// TODO Auto-generated constructor stub
	}

	@Override
	public PImage getCurrentTexture()
	{
		return null;
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
