package game.elements;

import game.graphics.Animation;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import util.BImage;

public class GoodSheep extends GameElement
{
	private Animation goodSheepAnimation;
	private PApplet processing;
	private double oldXSpeed;

	public GoodSheep(double x, double y, double width, double height, PApplet processing)
	{
		super(x, y, width, height, processing);
		super.setCollisionEffect(Effects.GOOD_SHEEP_BOUNCE);
		this.processing = processing;
		this.goodSheepAnimation = new Animation(processing, "graphics/animations/goodSheep");
		this.goodSheepAnimation.start();
		this.oldXSpeed = this.xSpeed = 0.7;
	}

	@Override
	public PImage getCurrentTexture()
	{
		int time = processing.millis();
		PImage ret = this.goodSheepAnimation.getCurrentImage(time);
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
		if (this.xSpeed == 0) {
			this.oldXSpeed = this.xSpeed = -this.oldXSpeed; 
		} else {
			this.xSpeed = this.oldXSpeed;
		}
	}

}
