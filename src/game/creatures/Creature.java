package game.creatures;
import processing.core.*;
import game.BunnyHat;
import game.CollisionBox;
import game.Player;
import game.graphics.Animation;
import game.level.Level;

/**
 * Standard creature - only existing in one of the dreams
 * 	
 * use it for e.g. temporary creatures in just one dream
 * 
 * @author samuelwalz
 *
 */
public abstract class Creature extends CollisionBox
{
	
	public boolean destroyed = false; 
	
	
	
	public Creature(double x, double y, double width, double height){
		super(x, y, width, height);
	}
	
	public abstract void update();
		

	public abstract PImage getCurrentTexture();
		

}
