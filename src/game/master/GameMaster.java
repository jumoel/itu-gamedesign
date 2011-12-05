package game.master;

import game.BunnyHat;
import game.Player;
import game.sound.Stereophone;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import processing.core.PApplet;

/**
 * This is the game master - he does not care about files, pixels and stuff
 * He is totally about the actual game 
 * 
 * He decides about major game events like the switching and doors 
 * 
 * @author Samuel Walz <samuel.walz@gmail.com>
 *
 */
public class GameMaster extends Observable implements Observer, Runnable
{
	public static enum MSG {SWITCH_DREAMS, SWITCH_ALERT_START, SWITCH_ALERT_STOP, 
		DOORS_SPAWN_START_PLAYER_1, DOORS_SPAWN_START_PLAYER_2, 
		DOORS_SPAWN_STOP, SWITCH_PLAYER_1, SWITCH_PLAYER_2} 
	
	private final static int SWITCH_TIME_TILL_NEXT = BunnyHat.SETTINGS.getValue("gamerules/switch/timeTillNext"); 
	private final static double SWITCH_TIME_VARIATION = BunnyHat.SETTINGS.getValue("gamerules/switch/timeVariation");
	private final static int SWITCH_ALERT_PHASE_DURATION = BunnyHat.SETTINGS.getValue("gamerules/switch/alertPhaseDuration");
	
	private final static double DOORS_PLAYER_MIN_BEHIND_DISTANCE = BunnyHat.SETTINGS.getValue("gamerules/doors/playerMinBehindDistance");
	private final static int DOORS_DURATION = BunnyHat.SETTINGS.getValue("gamerules/doors/doorDuration");
	private final static int DOORS_MIN_BEHIND_TIME = BunnyHat.SETTINGS.getValue("gamerules/doors/playerMinBehindTime");
	
	private final static boolean SHOW_FPS = false;
	private PApplet ourPApplet;
	private Thread ourThread;
	private boolean runGame = false;
	
	
	// GAME FACTS
	// general
	private int msTillNextSwitch = getNewTimeTillNextSwitch();
	private int msTillDoorsEnd = 0;
	private boolean switchHappening = false;
	private boolean doorsHappening = false;
	private boolean switchAlertStarted = false;
	private boolean gameOver = false;
	private int winner = -1; public int getWinner() {return winner;};
	
	//players
	Player player1, player2;
	private int playerFarAhead = -1;
	private int leadingTime = 0;
	private int showDoors = -1;
	
	
	public GameMaster(PApplet papplet){
		ourPApplet = papplet;
	}
	
	public void setTwins(Player p1, Player p2) {
		player1 = p1;
		player2 = p2;
	}
	
	private static int getNewTimeTillNextSwitch() {
		double variationSpan = SWITCH_TIME_TILL_NEXT * SWITCH_TIME_VARIATION;
		double variation = Math.random() * variationSpan;
		return (int)(SWITCH_TIME_TILL_NEXT - (variationSpan / 2) + variation);
	}
	
	/**
	 * Yay! Let's play a game!
	 */
	public void startGame() {
		runGame = true;
		ourThread = new Thread(this);
		ourThread.start();
	}
	
	/**
	 * Game to be stopped?
	 * Why? Who would want to stop a game?
	 * But we will play again, right?!
	 */
	public void stopGame() {
		runGame = false;
	}
	
	/**
	 * This is where our game master
	 * makes its decisions
	 */
	private void makeDecisions(int msSinceLastDecisions) {
		//System.out.println(msTillNextSwitch + " - " + msSinceLastDecisions);
		// time for a switch?
		if (!doorsHappening) msTillNextSwitch -= msSinceLastDecisions; // stop countdown for doors
		if (msTillNextSwitch < 0) { // yes, it is time
			switchHappening = true;
			this.setChanged();
			this.notifyObservers(GameMaster.MSG.SWITCH_ALERT_STOP);
			this.setChanged();
			this.notifyObservers(GameMaster.MSG.SWITCH_DREAMS);
			msTillNextSwitch = this.getNewTimeTillNextSwitch();
			switchAlertStarted = false;
			//return;
		} else if (msTillNextSwitch < SWITCH_ALERT_PHASE_DURATION
				&& !switchAlertStarted) { // time to warn the players
			switchAlertStarted = true;
			this.setChanged();
			this.notifyObservers(GameMaster.MSG.SWITCH_ALERT_START);
			Stereophone.playSound("302", "switchwarning", 1000);
		} else { switchHappening = false;}
		
		// time for some doors?
		if (!doorsHappening && !switchAlertStarted && !switchHappening) {
			// player behind?
			double playerDistance = player1.x() - player2.x();
			if (playerDistance > this.DOORS_PLAYER_MIN_BEHIND_DISTANCE) {
				// player1 further ahead
				this.playerFarAhead = 1;
				//System.out.println("player1 is leading");
			} else if (playerDistance < this.DOORS_PLAYER_MIN_BEHIND_DISTANCE * -1) {
				// player 2 is further ahead
				this.playerFarAhead = 2;
			} else {
				this.playerFarAhead = -1; // nobody far ahead
			}
			
			switch (playerFarAhead) {
				case 1:
					this.leadingTime += msSinceLastDecisions;
					break;
				case 2:
					this.leadingTime -= msSinceLastDecisions;
					break;
			}
			
			if (leadingTime > this.DOORS_MIN_BEHIND_TIME) {
				// twin 1 leaded to long
				doorsHappening = true;
				showDoors = 2; // twin 2 gets a door
				this.msTillDoorsEnd = this.DOORS_DURATION;
				this.leadingTime = 0;
			} else if (leadingTime < this.DOORS_MIN_BEHIND_TIME*-1) {
				// twin 2 leaded to long
				doorsHappening = true;
				showDoors = 1; // twin 1 gets a door
				this.msTillDoorsEnd = this.DOORS_DURATION;
				this.leadingTime = 0;
			}
		}
		
		if (doorsHappening) {
			this.msTillDoorsEnd -= msSinceLastDecisions; // door countdown
			if (showDoors != -1) {
				this.setChanged();
				switch (showDoors) {
					case 1:
						this.notifyObservers(MSG.DOORS_SPAWN_START_PLAYER_1);
						break;
					case 2:
						this.notifyObservers(MSG.DOORS_SPAWN_START_PLAYER_2);
						break;
				}
				this.showDoors = -1;
			} else if (this.msTillDoorsEnd <= 0) {
				doorsHappening = false;
				this.setChanged();
				this.notifyObservers(MSG.DOORS_SPAWN_STOP);
			} 
		}
	}
	
	@Override
	public void run()
	{
		int lastFrameTime = ourPApplet.millis(); 
		int lastFpsTime = 0;
		int fps = 0;
		int timeStepSize = 100;
		this.msTillNextSwitch = getNewTimeTillNextSwitch();
		while (runGame) {
			try
			{
				Thread.currentThread().sleep(timeStepSize);
				
				int currentTimeStamp = ourPApplet.millis();
				
				int deltaT = currentTimeStamp - lastFrameTime; 
				if ((deltaT) >= timeStepSize) {
					
					// make up your mind about the game
					if (!gameOver) makeDecisions(deltaT);
					
					
					lastFrameTime = currentTimeStamp;
				
					
					
					
					// Print the fps
					if ((currentTimeStamp - lastFpsTime) > 1000)
					{
						if (SHOW_FPS) System.out.println("FPS-GM: " + fps);
						lastFpsTime = currentTimeStamp;
						fps = 0;
					}
					else
					{
						fps++;
					}
				}
				
			}
			catch (Exception e)
			{
				
				e.printStackTrace();
			}
		}
		
		
	}

	/**
	 * If you want to talk to the Game Master: do it that way
	 * (use the Observer Pattern)
	 */
	@Override
	public void update(Observable arg0, Object arg1)
	{
		if (arg1 instanceof HashMap) {
			HashMap map = (HashMap)arg1;
			if (map.containsKey("IFUCKINGWON") && !gameOver) {
				this.gameOver = true;
			} else if (map.containsKey("OHDOORTAKEMEAWAY") && !gameOver) {
				if ((Integer)map.get("OHDOORTAKEMEAWAY") == 1) {
					this.setChanged();
					this.notifyObservers(MSG.SWITCH_PLAYER_1);
				} else {
					this.setChanged();
					this.notifyObservers(MSG.SWITCH_PLAYER_2);
				}
			} else if (map.containsKey("IGOTGUMMED") && !gameOver) {
				// player got hit by gum: finish doors
				this.msTillDoorsEnd = 0;
			}
		}
		
	}
	
}
