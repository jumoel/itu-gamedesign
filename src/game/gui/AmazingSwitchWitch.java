package game.gui;

import game.level.Level;
import game.master.GameMaster;

import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;

import processing.core.PApplet;

public class AmazingSwitchWitch extends Observable implements Observer, Runnable
{
	
	//Thread control
	private Thread ourThread;
	private static boolean awake = false;
	
	private final static int SWITCH_SLEEP = 500;
	
	private static PlayerView playerView1, playerView2;
	
	private static boolean shouldSwitchDreams = false; 
	
	private PApplet ourPApplet;
	
	
	public AmazingSwitchWitch(PlayerView pv1, PlayerView pv2, PApplet papplet) {
		playerView1 = pv1;
		playerView2 = pv2;
		ourPApplet = papplet;
	}

	public void switchDreams() {
		System.out.print("will switch dreams");
		Level l1, l2;
		l1 = playerView1.getLevel();
		l2 = playerView2.getLevel();
		playerView1.switchPrepare();
		playerView2.switchPrepare();
		
		
		
		
		try
		{
			for (int i = 0; i < 255; i += 10) {
				Thread.currentThread().sleep(SWITCH_SLEEP/25);
				ourPApplet.tint(ourPApplet.color(255, 0, 0), i);
			}
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		playerView1.switchExecute(l2);
		playerView2.switchExecute(l1);
		
		try
		{
			for (int i = 255; i > 0; i -= 10) {
				Thread.currentThread().sleep(SWITCH_SLEEP/25);
				ourPApplet.tint(ourPApplet.color(255, 0, 0), i);
			}
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ourPApplet.noTint();
		
		playerView1.switchFinish();
		playerView2.switchFinish();
	}
	
	public void wakeHer() {
		awake = true;
		ourThread = new Thread(this);
		ourThread.start();
	}
	
	public void makeHerSleep() {
		awake = false;
	}
	
	/**
	 * doing the actual switch
	 */
	@Override
	public void run()
	{
		
		while (awake) {
			try
			{
				if (shouldSwitchDreams) switchDreams();
				shouldSwitchDreams = false;
				Thread.currentThread().sleep(200);
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void update(Observable o, Object arg)
	{
		if (o instanceof GameMaster && arg instanceof GameMaster.MSG) {
			switch((GameMaster.MSG)arg) {
				case SWITCH_DREAMS:
					shouldSwitchDreams = true;
					break;
			}
		}
	}

}
