package game;
import game.gui.PlayerView;
import game.gui.RaceIndicator;

import processing.core.*;

@SuppressWarnings("serial")
public class BunnyHat extends PApplet
{
	public static Settings settings = new Settings();
	
	
	private static int TILEDIMENSION = settings.getValue("gui/tiledimension");
	
	private static int RACEINDICATORHEIGHT = 2 * TILEDIMENSION;
	private static int PLAYERVIEWTILES = settings.getValue("gui/playerviewtiles");;
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
	
	private int lastTimestamp;
	private int currentTimestamp;
	private int deltaT;
	
	public void setup()
	{	
		inputState = new State();
		view1 = new PlayerView(WINDOWWIDTH, PLAYERVIEWHEIGHT, this, 1);
		view2 = new PlayerView(WINDOWWIDTH, PLAYERVIEWHEIGHT, this, 2);
		indicator = new RaceIndicator(WINDOWWIDTH, RACEINDICATORHEIGHT, this);
		
		size(WINDOWWIDTH, WINDOWHEIGHT);
		background(0);
		
		frameRate(60);
		
		lastTimestamp = currentTimestamp = millis();
		deltaT = 0;
	}

	public void draw()
	{
		lastTimestamp = currentTimestamp;
		currentTimestamp = millis();
		
		deltaT = currentTimestamp - lastTimestamp;
		
		
		background(0);

		view1.update(inputState, LEFT, VIEW1Y, deltaT);
		view2.update(inputState, LEFT, VIEW2Y, deltaT);
		indicator.update(inputState, LEFT, RACEINDICATORY, deltaT);
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
