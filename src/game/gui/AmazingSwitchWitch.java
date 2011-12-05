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
	
	private BunnyHat bunnyHat;
	
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
	
	// affect camera offset factor
	private class ChangeCameraOffsetFactor extends Animator {
		private PlayerView pv;
		private int maxValue;
		
		public ChangeCameraOffsetFactor(int from, int to, int stepSize, int timeSpan, PlayerView pv)
		{
			super(from, to, stepSize, timeSpan);
			this.pv = pv;
			super.begin();
			this.maxValue = from < to ? to : from;
		}
		
		@Override
		protected void applyValue(int value)
		{
			pv.cameraOffsetFactor = 1.0 * value / maxValue;
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
		private BunnyHat bunnyHat;

		public SwitchTransition(int from, int to, int stepSize, int timeSpan, 
				PlayerView pv1, PlayerView pv2, BunnyHat bunnyHat)
		{
			super(from, to, stepSize, timeSpan);
			this.pv1 = pv1; this.pv2 = pv2;
			this.bunnyHat = bunnyHat;
			super.begin();
		}

		@Override
		protected void applyValue(int value)
		{
			pv1.colorLayerVisibility = pv2.colorLayerVisibility = value;
			if (value == 0) {
				bunnyHat.physicsTimeFactor = bunnyHat.physicsTimeFactor = 1.0;
			} else if (value > 0 && value < 100) {
				bunnyHat.physicsTimeFactor = bunnyHat.physicsTimeFactor = 0.5;
			} else if (value >= 100 && value < 200) {
				bunnyHat.physicsTimeFactor = bunnyHat.physicsTimeFactor = 0.1;
			} else {
				bunnyHat.physicsTimeFactor = bunnyHat.physicsTimeFactor = 0.0;
			}
		}
		
	}
	
	
	public AmazingSwitchWitch(PlayerView pv1, PlayerView pv2, BunnyHat papplet) {
		playerView1 = pv1;
		playerView2 = pv2;
		bunnyHat = papplet;
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
		
		
		
		
		
		pvSource.drawOwnPlayer = false;
		
		
		pvSource.getLevel().removeElement(pvSource.getPlayer());
		pvTarget.getLevel().addElement(pvSource.getPlayer());
		pvSource.getPlayer().setLevel(pvTarget.getLevel());
		
		playerBackupX = pvSource.getPlayer().x();
		playerBackupY = pvSource.getPlayer().y();
		pvSource.xbackup = playerBackupX;
		pvSource.ybackup = playerBackupY;

		pvTarget.setDoorPosition(pvSource.getPlayer());
		pvSource.getPlayer().removeCollisionGroundPath();
		
		pvSource.getPlayer().giveWeapon();
		
		pvTarget.drawOtherPlayer = true;
		
	}
	
	private void untunnelTwin() {
		System.out.println("untunneling twin"+playerSwitched);
		if (this.playerSwitched == -1) return;
		
		PlayerView pvSource = (playerSwitched == 1 ? playerView1 : playerView2);
		PlayerView pvTarget = (pvSource == playerView1 ? playerView2 : playerView1);
		
		
		pvTarget.drawOtherPlayer = false;
		
		pvSource.getPlayer().setPos(playerBackupX, playerBackupY);
		
		
		pvTarget.getLevel().removeElement(pvSource.getPlayer());
		pvSource.getLevel().addElement(pvSource.getPlayer());
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
		PlayerView pvDarling = number == 1 ? playerView1 : playerView2;
		PlayerView pvVictim = pvDarling == playerView1 ? playerView2 : playerView1;
		
		// show first door
		pvDarling.initShowDoor(true);
		// show second door
		pvVictim.initShowDoor();
		new ChangeCameraOffsetFactor(0, 100, 2, cameraMoveDuration, pvVictim);
		
		
		
		
	}
	
	public void blowDoors() {
		PlayerView pvSource = (doorSpawnDarling == 1 ? playerView1 : playerView2);
		PlayerView pvTarget = (pvSource == playerView1 ? playerView2 : playerView1);
		
		//new MoveCamera(pvTarget.getWidth()/2 - 100, 0, 2, DOOR_CAMERA_MOVE_DURATION, pvTarget);
		new ChangeCameraOffsetFactor(100, 0, 2, DOOR_CAMERA_MOVE_DURATION, pvTarget);
		
		playerView1.initBlowDoor();
		playerView2.initBlowDoor();
	}

	public void switchDreams() {
		
		this.setChanged();
		this.notifyObservers("switchPrepare");
		//playerView1.initSwitchPrepare();
		//playerView2.initSwitchPrepare();

		new SwitchTransition(0, 255, 3, SWITCH_DURATION/2, playerView1, playerView2, bunnyHat);
		
		try
		{
			Thread.currentThread().sleep(SWITCH_DURATION/2);
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.setChanged();
		this.notifyObservers("switchExecute");
		//playerView1.initSwitchExecute();
		//playerView2.initSwitchExecute();
		
		new SwitchTransition(255, 0, 2, SWITCH_DURATION/2, playerView1, playerView2, bunnyHat);
		try
		{
			Thread.currentThread().sleep(SWITCH_DURATION/2);
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//bunnyHat.noTint();
		
		this.hasChanged();
		this.notifyObservers("startAnimations");
		
		this.setChanged();
		this.notifyObservers("switchFinish");
		//playerView1.initSwitchFinish();
		//playerView2.initSwitchFinish();
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
					Thread.currentThread().sleep(100);
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
