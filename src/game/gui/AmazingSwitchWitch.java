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
	private final static int DOOR_CAMERA_MOVE_DURATION = 500;
	
	private static PlayerView playerView1, playerView2;
	
	private static boolean shouldSwitchDreams = false;
	private static boolean shouldSwitchPlayerBack = false;
	private static boolean shouldSpawnDoors = false;
	private static int doorSpawnDarling = -1;
	
	private PApplet ourPApplet;
	
	private int tintColor;
	
	private Level player1backup, player2backup;
	private double playerBackupX, playerBackupY;
	private double player2xbackup, player2ybackup;
	private boolean player1switched, player2switched;
	private int playerSwitched = -1;
	
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
	
	//shake camera
	private class ShakeCameraX extends Animator {
		private PlayerView pv;

		public ShakeCameraX(int from, int to, int stepSize, int timeSpan, PlayerView pv)
		{
			super(from, to, stepSize, timeSpan, true);
			this.pv = pv;
			super.begin();
		}

		@Override
		protected void applyValue(int value)
		{
			this.pv.cameraOffsetX = value;
		}
		
	}
	
	private class ShakeCameraY extends Animator {
		private PlayerView pv;

		public ShakeCameraY(int from, int to, int stepSize, int timeSpan, PlayerView pv)
		{
			super(from, to, stepSize, timeSpan, true);
			this.pv = pv;
			super.begin();
		}

		@Override
		protected void applyValue(int value)
		{
			this.pv.cameraOffsetY = value;
		}
		
	}
	
	private class ShakeCamera extends Animator {
		Animator shakeX, shakeY;

		public ShakeCamera(PlayerView pv)
		{
			super(0, 10, 1000);
			shakeX = new ShakeCameraX(0, 55, 2, 100, pv);
			shakeY = new ShakeCameraY(0, 51, 2, 119, pv);
			super.begin();
		}

		@Override
		protected void applyValue(int value)
		{
			if (value == 10) {
				shakeX.finishLoop();
				shakeY.finishLoop();
			}
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
		player1switched = player2switched = false;
	}
	
	public void swapPlayer1()
	{
		tunnelTwin(1);
	}
	
	private void tunnelTwin(int number) {
		playerSwitched = number;
		
		PlayerView pvSource = (number == 1 ? playerView1 : playerView2);
		PlayerView pvTarget = (pvSource == playerView1 ? playerView2 : playerView1);
		
		
		
		//new MoveCamera(0, pvTarget.getWidth()/2 - 100, 2, DOOR_CAMERA_MOVE_DURATION, pvTarget);
		
		pvSource.drawOwnPlayer = false;
		
		//player1backup = playerView1.getLevel();
		pvSource.getPlayer().setLevel(pvTarget.getLevel());
		
		playerBackupX = pvSource.getPlayer().xpos;
		playerBackupY = pvSource.getPlayer().ypos;
		pvSource.xbackup = playerBackupX;
		pvSource.ybackup = playerBackupY;

		pvTarget.setDoorPosition(pvSource.getPlayer());
		pvSource.getPlayer().removeCollisionGroundPath();
		//pvSource.getPlayer().xpos = pvTarget.getPlayer().xpos;
		//pvSource.getPlayer().ypos = pvTarget.getPlayer().ypos;
		//playerView1.setLevel(playerView2.getLevel());
		pvSource.getPlayer().giveWeapon();
		
		pvTarget.drawOtherPlayer = true;
		
	}
	
	private void untunnelTwin() {
		if (this.playerSwitched == -1) return;
		
		PlayerView pvSource = (playerSwitched == 1 ? playerView1 : playerView2);
		PlayerView pvTarget = (pvSource == playerView1 ? playerView2 : playerView1);
		
		new MoveCamera(pvTarget.getWidth()/2 - 100, 0, 2, DOOR_CAMERA_MOVE_DURATION, pvTarget);
		
		pvTarget.drawOtherPlayer = false;
		
		pvSource.getPlayer().ypos = playerBackupY;
		pvSource.getPlayer().xpos = playerBackupX;
		
		//playerView1.setLevel(player1backup);
		pvSource.getPlayer().setLevel(pvSource.getLevel());
		
		pvSource.getPlayer().takeWeapon();
		pvSource.drawOwnPlayer = true;
		playerSwitched = -1;
	}
	
	public void resetPlayer1()
	{
		untunnelTwin();
	}
	
	public void swapPlayer2()
	{
		tunnelTwin(2);
	}
	
	public void resetPlayer2()
	{
		untunnelTwin();
	}
	
	public void setupDoors(int number) {
		int cameraMoveDuration = 500;
		
		// show first door
		if (number == 1) {
			playerView1.initShowDoor(true);
		} else {
			playerView2.initShowDoor(true);
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
			playerView2.initShowDoor();
		} else {
			playerView1.initShowDoor();
		}
		
	}
	
	public void blowDoors() {
		playerView1.initBlowDoor();
		playerView2.initBlowDoor();
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
				if (shouldSwitchPlayerBack) {
					this.blowDoors();
					this.untunnelTwin();
					shouldSwitchPlayerBack = false;
				}
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
					if (this.playerSwitched == -1) this.tunnelTwin(1);
					break;
				case SWITCH_PLAYER_2:
					if (this.playerSwitched == -1) this.tunnelTwin(2);
					break;
			}
		}
	}

}
