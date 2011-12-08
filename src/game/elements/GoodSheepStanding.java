package game.elements;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

public class GoodSheepStanding extends GameElement
{

	private PApplet processing;
	
	public GoodSheepStanding(double x, double y, PApplet processing)
	{
		super(x, y, 3, 3, processing);
		this.processing = processing;
		this.setCollisionEffect(Effects.BOUNCE);
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
	
	@Override
	public void update(int deltaT) {
		// TODO random sheep sounds
		
	}

}
