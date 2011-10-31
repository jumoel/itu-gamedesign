package game;
import game.gui.PlayerView;
import game.gui.RaceIndicator;

import java.util.LinkedList;

import processing.core.*;

@SuppressWarnings("serial")
public class BunnyHat extends PApplet
{
	private static int WINDOWHEIGHT = 768;
	private static int WINDOWWIDTH = 1024;
	private static int RACEINDICATORHEIGHT = 24;
	private static int PLAYERVIEWHEIGHT = (WINDOWHEIGHT - RACEINDICATORHEIGHT) / 2;

	private static int VIEW1Y = 0;
	private static int RACEINDICATORY = PLAYERVIEWHEIGHT;
	private static int VIEW2Y = RACEINDICATORY + RACEINDICATORHEIGHT;
	
	private static int LEFT = 0;
	
	private game.gui.PlayerView view1;
	private game.gui.PlayerView view2;
	private game.gui.RaceIndicator indicator;
	
	// TODO: REMOVE
	public LinkedList<Obstacle> obstacleList;

	private State inputState;
	
	public void setup()
	{
		inputState = new State();
		view1 = new PlayerView(WINDOWWIDTH, PLAYERVIEWHEIGHT, this);
		view2 = new PlayerView(WINDOWWIDTH, PLAYERVIEWHEIGHT, this);
		indicator = new RaceIndicator(WINDOWWIDTH, RACEINDICATORHEIGHT, this);
		
		size(1024, 768);
		background(0);
	}

	public void draw()
	{
		background(0);

		view1.update(inputState, LEFT, VIEW1Y);
		view2.update(inputState, LEFT, VIEW2Y);
		indicator.update(inputState, LEFT, RACEINDICATORY);
	}

	public static void main(String args[])
	{
		PApplet.main(new String[]
		{ "--present", "BunnyHat" });
	}

	public void keyPressed()
	{
		if (key == 'd')
		{
			inputState.put("d", true);
			inputState.put("a", false);
		}
		if (key == 'a')
		{
			inputState.put("a", true);
			inputState.put("d", false);
		}
		if (key == 'w')
		{
			inputState.put("w", true);
		}
	}

	public void keyReleased()
	{
		if (key == 'd')
		{
			inputState.put("d", false);
		}
		if (key == 'a')
		{
			inputState.put("a", false);
		}
		if (key == 'w')
		{
			inputState.put("w", false);
		}
	}
}
