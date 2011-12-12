package game.elements;

import java.util.Observable;
import java.util.Observer;

import game.BunnyHat;
import game.graphics.Animation;
import game.master.GameMaster;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

public class DreamSwitch extends GameElement implements Observer
{
	private final static int BUTTON_COOLDOWN = BunnyHat.SETTINGS.getValue("gameplay/dreamswitchbuttoncooldown");
	
	private PApplet processing;
	private Animation dreamSwitchAnimation;
	private Animation dreamSwitchPressAnimation;
	private Animation dreamSwitchAlarmAnimation;
	private Animation dreamSwitchOffAnimation;
	private int msTillSwitchWorksAgain = 0;
	public boolean usable = false;
	private boolean alarmRunning = false;
	
	public DreamSwitch(double x, double y, PApplet p)
	{
		super(x, y, 3, 1, p);
		this.setCollisionEffect(Effects.BOUNCE);
		
		processing = p;
		
		this.setGameElement(this);
		
		this.dreamSwitchAnimation = new Animation(p, "graphics/animations/dreamSwitch");
		this.dreamSwitchPressAnimation = new Animation(p, "graphics/animations/dreamSwitchPress");
		this.dreamSwitchAlarmAnimation = new Animation(p, "graphics/animations/dreamSwitchAlarm");
		this.dreamSwitchOffAnimation = new Animation(p, "graphics/animations/dreamSwitchOff");
		this.dreamSwitchAnimation.start();
	}

	@Override
	public PImage getCurrentTexture()
	{
		PImage ret;
		int time = processing.millis();
		
		if (this.dreamSwitchAlarmAnimation.isRunning())
		{
			ret = this.dreamSwitchAlarmAnimation.getCurrentImage(time);
		}
		else if (this.dreamSwitchPressAnimation.isRunning())
		{
			ret = this.dreamSwitchPressAnimation.getCurrentImage(time);
		}
		else if (this.dreamSwitchOffAnimation.isRunning())
		{
			ret = this.dreamSwitchOffAnimation.getCurrentImage(time);
		}
		else
		{
			this.dreamSwitchAnimation.start();
			ret = this.dreamSwitchAnimation.getCurrentImage(time);
		}
		
		return ret;
	}

	@Override
	public void collisionDraw(PGraphics cb, int xOff, int yOff)
	{
		// TODO Auto-generated method stub
		

	}
	
	@Override
	public void update(int deltaT) {
		if (!usable && !alarmRunning) {
			msTillSwitchWorksAgain -= deltaT;
			if (msTillSwitchWorksAgain < 0) {
				usable = true;
				this.setCollisionEffect(Effects.BOUNCE);
				this.dreamSwitchAnimation.start();
				this.dreamSwitchAlarmAnimation.stop();
				this.dreamSwitchOffAnimation.stop();
				this.dreamSwitchPressAnimation.stop();
			}
		}
	}
	
	private void setAlarmRunning(boolean alarm) {
		this.alarmRunning = alarm;
		this.usable = false;
		if (alarm) {
			this.setCollisionEffect(Effects.NONE);
			this.dreamSwitchAlarmAnimation.start();
			this.dreamSwitchAnimation.stop();
			this.dreamSwitchPressAnimation.stop();
			this.dreamSwitchOffAnimation.stop();
		} else {
			this.setCollisionEffect(Effects.BOUNCE);
			this.dreamSwitchAnimation.start();
			this.dreamSwitchAlarmAnimation.stop();
			this.dreamSwitchOffAnimation.stop();
			this.dreamSwitchPressAnimation.stop();
		}
	}
	
	public void pressDreamSwitch() {
		if (!dreamSwitchAlarmAnimation.isRunning()) {
			this.dreamSwitchPressAnimation.start(false, true);
			this.dreamSwitchAnimation.stop();
			this.dreamSwitchAlarmAnimation.stop();
			this.dreamSwitchOffAnimation.stop();
		} else {
			this.dreamSwitchOffAnimation.start();
			this.dreamSwitchAlarmAnimation.stop();
			this.dreamSwitchPressAnimation.stop();
			this.dreamSwitchAnimation.stop();
		}
		this.usable = false;
		//this.changeCollisionBoxHeight(0.4);
		this.setCollisionEffect(Effects.NONE);
		this.msTillSwitchWorksAgain = BUTTON_COOLDOWN;
	}
	
	

	@Override
	public void update(Observable arg0, Object arg1)
	{
		if (arg0 instanceof GameMaster && arg1 instanceof GameMaster.MSG) {
			switch((GameMaster.MSG)arg1) {
				case SWITCH_ALERT_START:
					this.setAlarmRunning(true);
					break;
				case SWITCH_DREAMS:
					this.pressDreamSwitch();
					this.alarmRunning = false;
					break;
				case DOORS_SPAWN_STOP:
					this.usable = true;
					this.dreamSwitchAnimation.start();
					break;
				case DOORS_SPAWN_START_PLAYER_1:
				case DOORS_SPAWN_START_PLAYER_2:
					this.usable = false;
					this.dreamSwitchOffAnimation.start();
					break;
				case SWITCH_PLAYER_1:
				case SWITCH_PLAYER_2:
					
					break;
			}
		}
		
	}

}
