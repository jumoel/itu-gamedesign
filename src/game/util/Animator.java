package game.util;

/**
 * Changes values over time following given restrictions
 * 
 * @author Samuel Walz <samuel.walz@gmail.com>
 *
 */
public abstract class Animator implements Runnable
{
	private boolean keepRunning = false;
	private boolean loop;
	private boolean finishLoop = false;
	private int intervalLength;
	private int currentValue;
	private int stepSize;
	private int maximumValue;
	private boolean backwards;
	private int from, to;
	
	
	/**
	 * no given stepSize, it will be 1
	 * @param from
	 * @param to
	 * @param timeSpan
	 */
	public Animator(int from, int to, int timeSpan) {
		this.setup(from, to, 1, timeSpan, false);
	}
	
	public Animator(int from, int to, int stepSize, int timeSpan) {
		this.setup(from, to, stepSize, timeSpan, false);
	}
	
	public Animator(int from, int to, int stepSize, int timeSpan, boolean loop) {
		this.setup(from, to, stepSize, timeSpan, loop);
	}
	
	
	
	private void setup(int from, int to, int stepSize, int timeSpan, boolean loop) {
		this.from = from; this.to = to;
		this.loop = loop;
		// count backwards or forward?
		this.backwards =  (from > to);
		
		int stepNumber = (backwards?(from - to):(to - from))/stepSize;
		
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
	
	public void stopAnimation() {
		this.keepRunning = false;
	}
	
	public void finishLoop() {
		this.finishLoop = true;
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
			//direction dependent value change
			if (backwards) {
				this.currentValue -= this.stepSize;
			} else {
				this.currentValue += this.stepSize;
			}
			// end reached?
			if ((this.currentValue < this.maximumValue && !backwards)
					|| (this.currentValue > this.maximumValue && backwards)) {
				this.applyValue(this.currentValue);
			} else {
				this.applyValue(this.maximumValue);
				if (!this.loop) {
					this.keepRunning = false;
				} else if (finishLoop && this.maximumValue == this.from) {
					this.keepRunning = false;
				} else {
					// change direction
					this.backwards = !this.backwards;
					// change maximum
					this.maximumValue = (this.maximumValue == this.from?this.to:this.from);
				}
			}
		}
	}
	
	protected abstract void applyValue(int value);

}
