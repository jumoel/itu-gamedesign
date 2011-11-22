package game.gui;

import game.level.Level;
import game.master.GameMaster;

import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;

public class AmazingSwitchWitch extends Observable implements Observer, Runnable
{
	
	
	private final int SWITCH_SLEEP = 500;
	
	PlayerView playerView1, playerView2; 
	
	public AmazingSwitchWitch(PlayerView pv1, PlayerView pv2) {
		playerView1 = pv1;
		playerView2 = pv2;
	}

	public void switchDreams() {
		Level l1, l2;
		l1 = playerView1.getLevel();
		l2 = playerView2.getLevel();
		playerView1.switchPrepare();
		playerView2.switchPrepare();
		
		try
		{
			Thread.currentThread().sleep(SWITCH_SLEEP);
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
			Thread.currentThread().sleep(SWITCH_SLEEP);
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		playerView1.switchFinish();
		playerView2.switchFinish();
	}
	
	
	/**
	 * doing the actual switch
	 */
	@Override
	public void run()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(Observable o, Object arg)
	{
		if (o instanceof GameMaster && arg instanceof GameMaster.MSG) {
			switch((GameMaster.MSG)arg) {
				case SWITCH_DREAMS:
					this.switchDreams();
					break;
			}
		}
	}

}
