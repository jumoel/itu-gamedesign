package game;
import game.gui.PlayerView;
import game.gui.RaceIndicator;

import processing.core.*;

@SuppressWarnings("serial")
public class BunnyHat extends PApplet
{
	private static int TILEDIMENSION = 16;
	
	private static int RACEINDICATORHEIGHT = 2 * TILEDIMENSION;
	private static int PLAYERVIEWTILES = 25;
	private static int PLAYERVIEWHEIGHT = PLAYERVIEWTILES * TILEDIMENSION;

	private static int VIEW1Y = 0;
	private static int RACEINDICATORY = PLAYERVIEWHEIGHT;
	private static int VIEW2Y = RACEINDICATORY + RACEINDICATORHEIGHT;
	
	private static int LEFT = 0;
	
	private static int WINDOWHEIGHT = RACEINDICATORHEIGHT + 2 * PLAYERVIEWHEIGHT;
	private static int WINDOWWIDTH = 64 * TILEDIMENSION;
	
	private game.gui.PlayerView view1;
	private game.gui.PlayerView view2;
	private game.gui.RaceIndicator indicator;

	private State inputState;
	
	public void setup()
	{
		inputState = new State();
		view1 = new PlayerView(WINDOWWIDTH, PLAYERVIEWHEIGHT, this);
		view2 = new PlayerView(WINDOWWIDTH, PLAYERVIEWHEIGHT, this);
		indicator = new RaceIndicator(WINDOWWIDTH, RACEINDICATORHEIGHT, this);
		
		size(WINDOWWIDTH, WINDOWHEIGHT);
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
