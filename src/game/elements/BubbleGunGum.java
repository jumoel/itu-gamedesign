package game.elements;

import game.Obstacle;
import game.Player;
import game.graphics.Animation;
import game.level.Level;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

public class BubbleGunGum extends GameElement
{
	private Animation ballAnimation;
	private PApplet processing;
	public enum BallColor {GIRL, BOY};
	private int startTime;
	private static int TIME_TO_LIVE = 200000;
	private double oldSpeed;
	
	public BubbleGunGum(double x, double y, double xSpeed, double ySpeed, 
			PApplet processing, BallColor ballColor)
	{
		super(x, y, 7/24.0, 7/24.0);
		super.setCollisionEffect(Effects.NONE);
		super.setGameElement(this);
		
		this.processing = processing;
		this.oldSpeed = this.xSpeed = xSpeed;
		this.ySpeed = ySpeed;
		this.gravityFactor = 0.08;
		this.breakAccelAirFactor = 0.0;
		this.breakAccelGroundFactor = 0.2;
		
		this.startTime = processing.millis();
		
		switch (ballColor) {
			case GIRL:
				ballAnimation = new Animation(processing, "graphics/animations/bubbleGumGirl");
				break;
			case BOY:
				ballAnimation = new Animation(processing, "graphics/animations/bubbleGumBoy");
				break;
		}
		ballAnimation.start();
	}

	@Override
	public PImage getCurrentTexture()
	{
		int time = processing.millis();
		return ballAnimation.getCurrentImage(time);
	}

	@Override
	public void collisionDraw(PGraphics cb, int xOff, int yOff)
	{
		// TODO Auto-generated method stub

	}
	
	@Override
	public void update(int deltaT) {
		super.update(deltaT);
		if (processing.millis() - this.startTime > TIME_TO_LIVE) {
			this.destroyed = true;
		} else if (this.getBouncePartner() instanceof Player) {
			this.destroyed = true;
		} else if (this.getBouncePartner() instanceof Obstacle) {
			if (xSpeed == 0 && ySpeed != 0) {
				this.xSpeed = -this.oldSpeed;
			}
		}
	}


}