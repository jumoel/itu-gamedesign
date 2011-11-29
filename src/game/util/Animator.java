package game.util;

public abstract class Animator implements Runnable
{
	private boolean keepRunning = false;
	private int intervalLength;
	private int currentValue;
	private int stepSize;
	private int maximumValue;
	
	public Animator(int from, int to, int stepSize, int timeSpan) {
		int stepNumber = (to - from)/stepSize;
		
		// setup stuff
		this.currentValue = from;
		this.intervalLength = timeSpan / stepNumber;
		this.maximumValue = to;
		this.stepSize = stepSize;
		
		
		
	}
	
	protected void begin() {
		// start the thread
		Thread t = new Thread(this);
		t.start();
	}
	
	@Override
	public void run()
	{
		
		this.keepRunning = true;
		this.applyValue(this.currentValue);
		while (keepRunning) {
			try
			{
				Thread.currentThread().sleep(this.intervalLength);
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.currentValue += this.stepSize;
			if (this.currentValue < this.maximumValue) {
				this.applyValue(this.currentValue);
			} else {
				this.applyValue(this.maximumValue);
				this.keepRunning = false;
			}
		}
	}
	
	protected abstract void applyValue(int value);

}
