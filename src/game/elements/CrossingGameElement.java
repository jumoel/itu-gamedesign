package game.elements;

import game.level.Level;
import processing.core.PApplet;

/**
 * Crossing GameElement
 * this kind of element is moving in x-axis like it's twinElement in the other level
 * 
 * @author Samuel Walz <samuel.walz@gmail.com>
 *
 */
public abstract class CrossingGameElement extends GameElement
{
	protected GameElement myTwinElement;

	public CrossingGameElement(double x, double y, double width, double height, PApplet processing,
			GameElement twinElement)
	{
		super(x, y, width, height, processing);
		this.myTwinElement = twinElement;
	}
	
	@Override
	public void update(int deltaT) {
		super.update(deltaT);
		this.xSpeed = this.myTwinElement.xSpeed;
	}
}
