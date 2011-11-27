package game.master;

import game.BunnyHat;
import game.sound.Stereophone;

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
		DOORS_SPAWN_START, DOORS_SPAWN_STOP} 
	
	private final static int SWITCH_TIME_TILL_NEXT = BunnyHat.SETTINGS.getValue("gamerules/switch/timeTillNext"); 
	private final static double SWITCH_TIME_VARIATION = BunnyHat.SETTINGS.getValue("gamerules/switch/timeVariation");
	private final static int SWITCH_ALERT_PHASE_DURATION = BunnyHat.SETTINGS.getValue("gamerules/switch/alertPhaseDuration");
	
	private final static double DOORS_PLAYER_MIN_DISTANCE = BunnyHat.SETTINGS.getValue("gamerules/doors/playerMinDistance");
	private final static int DOORS_DURATION = BunnyHat.SETTINGS.getValue("gamerules/doors/doorDuration");
	
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
	
	//players
	private class PlayerStats {
		
	}
	PlayerStats statsP1, statsP2;
	
	
	public GameMaster(PApplet papplet){
		ourPApplet = papplet;
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
		// time for a switch?
		msTillNextSwitch -= msSinceLastDecisions;
		if (msTillNextSwitch < 0) { // yes, it is time
			switchHappening = true;
			this.setChanged();
			this.notifyObservers(GameMaster.MSG.SWITCH_ALERT_STOP);
			this.setChanged();
			this.notifyObservers(GameMaster.MSG.SWITCH_DREAMS);
			msTillNextSwitch = this.getNewTimeTillNextSwitch();
			switchAlertStarted = false;
			//TODO: switch dreams, stop alert
			return;
		} else if (msTillNextSwitch < SWITCH_ALERT_PHASE_DURATION
				&& !switchAlertStarted) { // time to warn the players
			switchAlertStarted = true;
			this.setChanged();
			this.notifyObservers(GameMaster.MSG.SWITCH_ALERT_START);
			Stereophone.playSound("302", "switchwarning", 1000);
			// TODO: start alert
		}
		
		
		// time for some doors?
		if (!doorsHappening) {
			//TODO: measure distance, decide
		} else {
			//TODO: doors are happening : countdown & stuff
			
		}
	}
	
	@Override
	public void run()
	{
		int lastFrameTime = 0; 
		int lastFpsTime = 0;
		int fps = 0;
		int timeStepSize = 100;
		while (runGame) {
			try
			{
				Thread.currentThread().sleep(timeStepSize);
				
				int currentTimeStamp = ourPApplet.millis();
				
				int deltaT = currentTimeStamp - lastFrameTime; 
				if ((deltaT) >= timeStepSize) {
					
					// make up your mind about the game
					makeDecisions(deltaT);
					
					
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
		// TODO: receive interesting informations
		
	}
	
}
