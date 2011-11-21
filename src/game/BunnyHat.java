package game;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import game.gui.PlayerView;
import game.gui.RaceIndicator;
import game.level.Level;
import game.sound.Stereophone;
import game.control.SoundControl;
import processing.core.*;

@SuppressWarnings("serial")
public class BunnyHat extends PApplet implements Observer
{
	public static Settings SETTINGS = new Settings();
	
	public static int TILEDIMENSION = SETTINGS.getValue("gui/tiledimension");
	
	private static int RACEINDICATORHEIGHT = 2 * TILEDIMENSION;
	public static int PLAYERVIEWTILEHEIGHT = SETTINGS.getValue("gui/playerviewtileheight");
	public static int PLAYERVIEWTILEWIDTH = SETTINGS.getValue("gui/playerviewtilewidth");
	public static int PLAYERVIEWHEIGHT = PLAYERVIEWTILEHEIGHT * TILEDIMENSION;

	private static int VIEW1Y = 0;
	private static int RACEINDICATORY = PLAYERVIEWHEIGHT;
	private static int VIEW2Y = RACEINDICATORY + RACEINDICATORHEIGHT;
	
	private static int LEFT = 0;
	
	private static int WINDOWHEIGHT = RACEINDICATORHEIGHT + 2 * PLAYERVIEWHEIGHT;
	private static int WINDOWWIDTH = PLAYERVIEWTILEWIDTH * TILEDIMENSION;
	
	public PlayerView view1;
	public PlayerView view2;
	public RaceIndicator indicator;
	private SoundControl sndCtrl;
	private Thread sndCtrlThread;
	private Stereophone sndOut;

	private State inputState;
	
	private int lastTimestamp;
	private int currentTimestamp;
	private int deltaT;
	
	private int lastFpsTime;
	private int fps;
	
	public void setup()
	{	
		inputState = new State();
		view1 = new PlayerView(WINDOWWIDTH, PLAYERVIEWHEIGHT, this, 1);
		view2 = new PlayerView(WINDOWWIDTH, PLAYERVIEWHEIGHT, this, 2);
		indicator = new RaceIndicator(WINDOWWIDTH, RACEINDICATORHEIGHT, this);
		sndCtrl = new SoundControl(this);
		
		size(WINDOWWIDTH, WINDOWHEIGHT);
		background(0);
		
		frameRate(2000);
		
		lastTimestamp = currentTimestamp = millis();
		deltaT = 0;
		lastFpsTime = 0;
		fps = 0;
		
		//testing Level
		Level level = new Level(this, "levels/test.tmx");
		
		//setup & run sound input
		sndCtrl = new SoundControl(this);
		sndCtrl.addObserver(this);
		sndCtrlThread = new Thread(sndCtrl);
		sndCtrlThread.start();
		
		//setup sound output
		sndOut = new Stereophone("sounds");
		sndOut.printSounds();
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
		
		
		
		// Print the fps
		if ((currentTimestamp - lastFpsTime) > 1000)
		{
			System.out.println("FPS: " + fps);
			lastFpsTime = currentTimestamp;
			fps = 0;
		}
		else
		{
			fps++;
		}
	}

	public static void main(String args[])
	{
		PApplet.main(new String[]
		{ "--present", "BunnyHat" });
	}

	public void keyPressed()
	{
		inputState.put(key, true);
		
		if (key == 'd')
		{
			inputState.put('a', false);
		}
		if (key == 'a')
		{
			inputState.put('d', false);
		}
		
		if (key == 'j')
		{
			inputState.put('l', false);
		}
		if (key == 'l')
		{
			inputState.put('j', false);
		}
	}

	public void keyReleased()
	{
		inputState.put(key, false);
	}

	@Override
	public void update(Observable o, Object arg)
	{
		if (arg instanceof HashMap) {
			HashMap map = (HashMap)arg;
			String detector = (String)map.get("detector");
			String pattern = (String)map.get("pattern");
			if (detector.contentEquals("HF")) {
				inputState.put('d', (pattern.contentEquals("Straight Solid")));
			} else {
				inputState.put('l', (pattern.contentEquals("Straight Solid")));
			}
		}
		
	}
}
