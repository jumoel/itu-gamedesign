package game.gui;

import game.BunnyHat;
import game.control.PatternDetector;
import game.level.Level;
import game.master.GameMaster;
import game.util.Animator;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import processing.core.PApplet;

public class AmazingSwitchWitch extends Observable implements Observer, Runnable
{
	
	//Thread control
	private Thread ourThread;
	private static boolean awake = false;
	
	private final static int SWITCH_DURATION = 2000;
	
	private static PlayerView playerView1, playerView2;
	
	private static boolean shouldSwitchDreams = false;
	private static boolean shouldSwitchPlayerBack = false;
	private static boolean shouldSpawnDoors = false;
	private static int doorSpawnDarling = -1;
	
	private PApplet ourPApplet;
	
	private int tintColor;
	
	private Level player1backup, player2backup;
	private double player1xbackup, player1ybackup;
	private double player2xbackup, player2ybackup;
	private boolean player1switched, player2switched;
	
	// animate camera position
	private class MoveCamera extends Animator {
		private PlayerView pv;
		
		
		public MoveCamera(int from, int to, int stepSize, int timeSpan, PlayerView pv)
		{
			super(from, to, stepSize, timeSpan);
			this.pv = pv;
			super.begin();
		}

		@Override
		protected void applyValue(int value)
		{
			pv.cameraOffsetX = value;
		}
		
	}
	
	// make a nice switch transition
	private class SwitchTransition extends Animator {
		private PlayerView pv1, pv2;

		public SwitchTransition(int from, int to, int stepSize, int timeSpan, 
				PlayerView pv1, PlayerView pv2)
		{
			super(from, to, stepSize, timeSpan);
			this.pv1 = pv1; this.pv2 = pv2;
			super.begin();
		}

		@Override
		protected void applyValue(int value)
		{
			pv1.colorLayerVisibility = pv2.colorLayerVisibility = value;
			if (value == 0) {
				pv1.physicsTimeFactor = pv2.physicsTimeFactor = 1.0;
			} else if (value > 0 && value < 100) {
				pv1.physicsTimeFactor = pv2.physicsTimeFactor = 0.5;
			} else if (value >= 100 && value < 200) {
				pv1.physicsTimeFactor = pv2.physicsTimeFactor = 0.1;
			} else {
				pv1.physicsTimeFactor = pv2.physicsTimeFactor = 0.0;
			}
		}
		
	}
	
	
	public AmazingSwitchWitch(PlayerView pv1, PlayerView pv2, PApplet papplet) {
		playerView1 = pv1;
		playerView2 = pv2;
		ourPApplet = papplet;
		/*int r = BunnyHat.SETTINGS.getValue("gui/colors/tintr");
		int g = BunnyHat.SETTINGS.getValue("gui/colors/tintg");
		int b = BunnyHat.SETTINGS.getValue("gui/colors/tintb");
		
		tintColor = ourPApplet.color(r, g, b);*/
		player1switched = player2switched = false;
	}
	
	public void swapPlayer1()
	{
		new MoveCamera(0, playerView2.getWidth()/2 - 100, 2, 1000, playerView2);
		
		playerView1.drawOwnPlayer = false;
		
		player1backup = playerView1.getLevel();
		
		player1xbackup = playerView1.getPlayer().xpos;
		player1ybackup = playerView1.getPlayer().ypos;
		playerView1.xbackup = player1xbackup;
		playerView1.ybackup = player1ybackup;

		playerView1.getPlayer().xpos = playerView2.getPlayer().xpos;
		playerView1.getPlayer().ypos = playerView2.getPlayer().ypos;
		playerView1.setLevel(playerView2.getLevel());
		
		playerView2.drawOtherPlayer = true;
		player1switched = true;
	}
	
	public void resetPlayer1()
	{
		playerView2.drawOtherPlayer = false;
		
		playerView1.getPlayer().ypos = player1ybackup;
		playerView1.getPlayer().xpos = player1xbackup;
		
		playerView1.setLevel(player1backup);
		
		playerView1.drawOwnPlayer = true;
		player1switched = false;
	}
	
	public void swapPlayer2()
	{
		playerView2.drawOwnPlayer = false;
		
		player2backup = playerView2.getLevel();
		
		player2xbackup = playerView2.getPlayer().xpos;
		player2ybackup = playerView2.getPlayer().ypos;
		playerView2.xbackup = player2xbackup;
		playerView2.ybackup = player2ybackup;

		playerView2.getPlayer().xpos = playerView1.getPlayer().xpos;
		playerView2.getPlayer().ypos = playerView1.getPlayer().ypos;
		playerView2.setLevel(playerView1.getLevel());
		
		playerView1.drawOtherPlayer = true;
		player2switched = true;
	}
	
	public void resetPlayer2()
	{
		playerView1.drawOtherPlayer = false;
		
		playerView2.getPlayer().ypos = player2ybackup;
		playerView2.getPlayer().xpos = player2xbackup;
		
		playerView2.setLevel(player1backup);
		
		playerView2.drawOwnPlayer = true;
		player2switched = false;
	}
	
	public void setupDoors(int number) {
		int cameraMoveDuration = 1000;
		
		// show first door
		if (number == 1) {
			playerView1.setupDoor();
		} else {
			playerView2.setupDoor();
		}
		
		//move camera 
		if (number == 1) {
			new MoveCamera(0, playerView2.getWidth()/2 - 100, 2, cameraMoveDuration, playerView2);
		} else {
			new MoveCamera(0, playerView1.getWidth()/2 - 100, 2, cameraMoveDuration, playerView1);
		}
		
		// wait till the camera is moved
		try
		{
			Thread.currentThread().sleep(cameraMoveDuration);
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// show 2nd door
		if (number == 1) {
			playerView2.setupDoor();
		} else {
			playerView1.setupDoor();
		}
		
	}

	public void switchDreams() {
		Level l1, l2;
		l1 = playerView1.getLevel();
		l2 = playerView2.getLevel();
		playerView1.switchPrepare();
		playerView2.switchPrepare();

		new SwitchTransition(0, 255, 3, SWITCH_DURATION/2, playerView1, playerView2);
		
		try
		{
			Thread.currentThread().sleep(SWITCH_DURATION/2);
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		playerView1.switchExecute(l2);
		playerView2.switchExecute(l1);
		
		new SwitchTransition(255, 0, 2, SWITCH_DURATION/2, playerView1, playerView2);
		try
		{
			Thread.currentThread().sleep(SWITCH_DURATION/2);
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ourPApplet.noTint();
		
		this.hasChanged();
		this.notifyObservers("startAnimations");
		
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
				if (shouldSwitchDreams) {switchDreams(); shouldSwitchDreams = false;}
				if (shouldSwitchPlayerBack) {switchPlayerBack(); shouldSwitchPlayerBack = false;}
				if (shouldSpawnDoors) {setupDoors(doorSpawnDarling); shouldSpawnDoors = false;}
				
				Thread.currentThread().sleep(200);
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void switchPlayerBack()
	{
		// TODO Auto-generated method stub
		if (this.player1switched) {
			this.resetPlayer1();
		} else {
			this.resetPlayer2();
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
				case DOORS_SPAWN_STOP:
					shouldSwitchPlayerBack = true;
					break;
				case DOORS_SPAWN_START_PLAYER_1:
					doorSpawnDarling = 1;
					shouldSpawnDoors = true;
					break;
				case DOORS_SPAWN_START_PLAYER_2:
					doorSpawnDarling = 2;
					shouldSpawnDoors = true;
					break;
				case SWITCH_PLAYER_1:
					this.swapPlayer1();
					break;
				case SWITCH_PLAYER_2:
					this.swapPlayer2();
					break;
			}
		}
	}

}
