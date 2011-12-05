package game.elements;

import game.elements.GameElement.Facing;
import game.graphics.Animation;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import util.BImage;

public class BadSheep extends CrossingGameElement
{
	private Animation badSheepAnimation;
	private PApplet processing;
	private GoodSheep goodTwin;
	
	public BadSheep(double x, double y, double width, double height, 
			PApplet processing, GoodSheep twinElement)
	{
		super(x, y, width, height, twinElement);
		super.setCollisionEffect(Effects.BAD_SHEEP_BOUNCE);
		this.processing = processing;
		this.badSheepAnimation = new Animation(processing, "graphics/animations/badSheep");
		this.badSheepAnimation.start();
		this.goodTwin = twinElement;
	}

	@Override
	public PImage getCurrentTexture()
	{
		int time = processing.millis();
		PImage ret = this.badSheepAnimation.getCurrentImage(time);
		if (facing == Facing.RIGHT)
		{
			ret = BImage.mirrorAroundY(processing, ret);
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
