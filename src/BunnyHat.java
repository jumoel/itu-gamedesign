import java.util.LinkedList;

import processing.core.*;

import org.newdawn.slick.SlickException;
import org.newdawn.slick.tiled.*;

@SuppressWarnings("serial")
public class BunnyHat extends PApplet
{

	public Player player2;
	public LinkedList<Obstacle> obstacleList;

	public void setup()
	{
		size(1024, 768);
		background(0);
		
		/* Example: Read map data
	
		BHTiledMap tm;
		try
		{
			tm = new BHTiledMap("/Users/jmoeller/Desktop/untitled.tmx");
			
			System.out.println("Layers: " + tm.getLayerCount());
			System.out.println("Width:" + tm.getWidth() + " Height: " + tm.getHeight());
			
			Layer l = tm.getLayer(0);
			System.out.println(l.name);
		}
		catch (SlickException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/

		player2 = new Player(this);

		obstacleList = new LinkedList<Obstacle>();
		obstacleList.add(new Obstacle(this, 0, 500, 600, 50));
		obstacleList.add(new Obstacle(this, 0, 700, 1024, 68));
	}

	public void draw()
	{
		background(0);

		this.update();
		player2.update();

		for (Obstacle o : obstacleList)
		{
			o.draw();
		}

		player2.draw();
	}

	public static void main(String args[])
	{
		PApplet.main(new String[]
		{ "--present", "BunnyHat" });
	}

	public void update()
	{
		if (wIsDown)
		{
			player2.jump();
		}
		if (aIsDown)
		{
			player2.moveLeft();
		}
		if (dIsDown)
		{
			player2.moveRight();
		}
	}

	private boolean wIsDown = false;
	private boolean aIsDown = false;
	// private boolean sIsDown = false;
	private boolean dIsDown = false;

	public void keyPressed()
	{
		if (key == 'd')
		{
			dIsDown = true;
			aIsDown = false;
		}
		if (key == 'a')
		{
			aIsDown = true;
			dIsDown = false;
		}
		if (key == 'w')
		{
			wIsDown = true;
		}
	}

	public void keyReleased()
	{
		if (key == 'd')
		{
			dIsDown = false;
		}
		if (key == 'a')
		{
			aIsDown = false;
		}
		if (key == 'w')
		{
			wIsDown = false;
		}
	}
}
