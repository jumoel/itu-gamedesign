package game.control;

import java.util.Observable;
import java.util.Observer;

import processing.core.*;
import ddf.minim.analysis.*;
import ddf.minim.*;


public class SoundControl extends Observable implements Observer, Runnable {
	 
	/**
	 * Attention Window
	 * Dimensions of our Window of Sound Interest
	 * 
	 * @author samuelwalz
	 *
	 */
	private class AW {
		private float iSMin; // initial strength minimum
		private float iSMax; // initial strength maximum
		private float iFMin; // initial frequency minimum
		private float iFMax; // initial frequency maximum
		private float minW; // minimal frequency spectrum width
		private float minH; // minimal strength height
		private float distSA2SMin = 20f; // distance from the loudness average to the minimum loudness
		
		private float maxW; // maximum width
		private float maxH; // maximum height
		
		private float cSMax; // current strength maximum
		private float cSMin; // current strength minimum
		private float cFMax; // current frequency maximum
		private float cFMin; // current frequency minimum
		private float cSA; // current strength average
		private float cFA; // current frequency average
		
		/**
		 * 
		 * @param strMin minimum strength
		 * @param strMax maximum strength
		 * @param freqMin min freq
		 * @param freqMax max freq
		 * @param minHeight min height
		 * @param minWidth min width
		 */
		public AW(float strMin, float strMax, float freqMin, float freqMax,
				float minHeight, float minWidth) {
			this.iSMin = strMin;
			this.iSMax = strMax;
			this.iFMin = freqMin;
			this.iFMax = freqMax;
			this.minW = minWidth;
			this.minH = minHeight;
			this.maxW = iFMax - iFMin;
			this.maxH = iSMax - iSMin;
		}
		
		public void recalc(float percentage) {
			// calculate theoretical new minimum/maximum strength
			float theoSMin = cSA - distSA2SMin;
			float theoHeight = minH + (maxH-minH)*(1-percentage);
			float theoSMax = theoSMin + theoHeight;
			// corrections in case of border collision
			if (theoSMin < iSMin) {
				theoSMax += iSMin - theoSMin;
				theoSMin = iSMin;
			} else if (theoSMax > iSMax) {
				theoSMin -= theoSMax - iSMax;
				theoSMax = iSMax;
			}
			cSMin = theoSMin;
			cSMax = theoSMax;
			
			// calculate theoretical new minimum/maximum frequency
			float theoWidth = minW + (maxW-minW)*(1-percentage);
			float theoFMin = cFA - theoWidth/2;
			float theoFMax = theoFMin + theoWidth;
			// corrections in case of border collision
			if (theoFMin < iFMin) {
				theoFMax += iFMin - theoFMin;
				theoFMin = iFMin;
			} else if (theoFMax > iFMax) {
				theoFMin -= theoFMax - iFMax;
				theoFMax = iFMax;
			}
			cFMin = theoFMin;
			cFMax = theoFMax;

		}
		
		//setters
		public void setSA(float strengthAverage) {
			if (strengthAverage < iSMin) {
				this.cSA = iSMin;
			} else if (strengthAverage > iSMax) {
				this.cSA = iSMax;
			} else {
				this.cSA = strengthAverage;
			}
		}
		public void setFA(float frequencyAverage) {
			if (frequencyAverage < iFMin) {
				this.cFA = iFMin;
			} else if (frequencyAverage > iFMax) {
				this.cFA = iFMax;
			} else {
				this.cFA = frequencyAverage;
			}
		}
		
		// getters
		public float getSA() { return this.cSA; }
		public float getFA() { return this.cFA; }
		public float getSMin() { return this.cSMin; }
		public float getSMax() { return this.cSMax; }
		public float getFMin() { return this.cFMin; }
		public float getFMax() { return this.cFMax; }
	}
	
	
	
	 
	
	
	public static final int NO_PEAK = 0;
	public static final int GENERAL_PEAK = 1;
	public static final int LONELY_PEAK = 2;
	public static final int STRONGEST_PEAK = 3;
	
	public static final int WHISTLING_PEAK = 5;
	
	
	
	
	
	Minim minim;
	AudioInput myinput;
	FFT fft;
	
	// for fps statistics
	private int currentTimeStamp;
	private int lastFrameTime;
	private int lastFpsTime;
	private int fps = 0;
	
	//general settings
	private long frameSize = 42; // 42ms = roundabout 23 fps

	//input Attention Windows: Settings
	float initStrengthMin = 5f;
	float initStrengthMax = 200f;
	float windowContractionSpeed = 2f;
	float windowExpansionSpeed = 0.5f;
	
	float minHeight = 100;
	int minWidth = 14;
	int peakPercentageSampleSize = 100; // how many frames to take into account when calculating the number of detected peaks
	// input Attention Windows: Dimensions
	// input Attention Window LF
	AW iAWLF;
	float iAWPeakPercentageLF;
	// input Attention Window HF
	AW iAWHF;
	float iAWPeakPercentageHF;
	
	
	
	
	
	
	
	// moving freq averages
	int numberOfPeaksForMovingFreqAverage = 13;
	float decreaseFreqAverageFactor = 0.5f;
	// moving strength averages
	int numberOfPeaksForMovingAverage = 42;
	float decreaseStrengthAverageFactor = 0.5f; // slow down the decreasing in case no peak is detected
	float movingAverageCompareThreshold;
	int mainPeakThreshold;
	int lonelyPeakThreshold;
	float whistlingPeakNeighbourDistance = 1; //logarithmic distance
	int lastEventLF, lastEventHF; // last Event
	int lastPeakPosLF, lastPeakPosHF;
	PatternDetector pattyLF, pattyHF;

	float[] frequenciesOfInterest =
			{/*16.35f, //C0
			 17.32f, //C#0/Db0
			 18.35f, //D0 	
			 19.45f, //D#0/Eb0
			 20.60f, //E0 	
			 21.83f, //F0 
			 23.12f, //F#0/Gb0 	
			 24.50f, //G0 	
			 25.96f, //G#0/Ab0
			 27.50f, //A0 	
			 29.14f, //A#0/Bb0 	
			 30.87f, //B0 	
			 32.70f, //C1 	
			 34.65f, //C#1/Db1 	
			 36.71f, //D1
             38.89f, //D#1/Eb1 
			 41.20f, //E1  
			 43.65f, //F1 	
			 46.25f, //F#1/Gb1 	
			 49.00f, //G1 	
			 51.91f, //G#1/Ab1 	
			 55.00f, //A1 	
			 58.27f, //A#1/Bb1 
			 61.74f, //B1 */	
			 65.41f, //C2 - begin of human voice range	0
			 69.30f, //C#2/Db2 	
			 73.42f, //D2 	
			 77.78f, //D#2/Eb2 	
			 82.41f, //E2 	     - start of human voice range
			 87.31f, //F2 	
			 92.50f, //F#2/Gb2 	
			 98.00f, //G2 	
			 103.83f, //G#2/Ab2 	
			 110.00f, //A2 	
			 116.54f, //A#2/Bb2 	10
			 123.47f, //B2 	
			 130.81f, //C3 	      + our start voice range
			 138.59f, //C#3/Db3
			 146.83f, //D3 	
			 155.56f, //D#3/Eb3 	
			 164.81f, //E3 	
			 174.61f, //F3 	
			 185.00f, //F#3/Gb3 	
			 196.00f, //G3 	
			 207.65f, //G#3/Ab3 	20
			 220.00f, //A3 	
			 233.08f, //A#3/Bb3 	
			 246.94f, //B3 	
			 261.63f, //C4 	        - start of human common range
			 277.18f, //C#4/Db4 
			 293.66f, //D4 
			 311.13f, //D#4/Eb4 
			 329.63f, //E4 	        - end of human common range
			 349.23f, //F4 
			 369.99f, //F#4/Gb4 	30
			 392.00f, //G4 
			 415.30f, //G#4/Ab4 
			 440.00f, //A4 
			 466.16f, //A#4/Bb4 
			 493.88f, //B4 	
			 523.25f, //C5 
			 554.37f, //C#5/Db5 
			 587.33f, //D5 
			 622.25f, //D#5/Eb5 
			 659.26f, //E5         40     - our end for voice range
			 698.46f, //F5 
			 739.99f, //F#5/Gb5 
			 783.99f, //G5 
			 830.61f, //G#5/Ab5 
			 880.00f, //A5        - end of human voice range  
			 932.33f, //A#5/Bb5 
			 987.77f, //B5 
			 1046.50f, //C6 
			 1108.73f, //C#6/Db6 
			 1174.66f, //D6         50
			 1244.51f, //D#6/Eb6 
			 1318.51f, //E6   - begin of whistling range   52
			 1396.91f, //F6 
			 1479.98f, //F#6/Gb6 
			 1567.98f, //G6 
			 1661.22f, //G#6/Ab6 
			 1760.00f, //A6 
			 1864.66f, //A#6/Bb6 
			 1975.53f, //B6 
			 2093.00f, //C7          60
			 2217.46f, //C#7/Db7 
			 2349.32f, //D7 
			 2489.02f, //D#7/Eb7 
			 2637.02f, //E7 
			 2793.83f, //F7 
			 2959.96f, //F#7/Gb7 
			 3135.96f, //G7 
			 3322.44f, //G#7/Ab7 
			 3520.00f, //A7 
			 3729.31f, //A#7/Bb7     70
			 3951.07f, //B7 - end of whistling range      71
			 4186.01f, //C8 
			 4434.92f, //C#8/Db8
			 4698.64f, //D8 
			 4978.03f  //D#8/Eb8
			};
	
	PApplet ourPApplet;
	 
	public SoundControl(PApplet papplet)
	{
	  ourPApplet = papplet;
	  
	  movingAverageCompareThreshold = 0.5f;
	  mainPeakThreshold = 3;
	  lonelyPeakThreshold = 20;
	  
	  
	  
	  
	  // human voice frequency range: 60 Hz - 7000 Hz
	  // whistling range: 1300 Hz - 4000 Hz
	  //frequencyBorderPos = 42; //position in the array
	  
	  // setup attention windows
	  
	  
	  
	  iAWLF = new AW(initStrengthMin, initStrengthMax, 12f, 40f, minHeight, minWidth);
	  iAWHF = new AW(initStrengthMin, initStrengthMax, 52f, 71f, minHeight, minWidth);
	  
	  iAWLF.setFA((iAWLF.getFMax()-iAWLF.getFMin())/2); 
	  iAWHF.setFA((iAWHF.getFMax() - iAWHF.getFMin()) / 2);
	  
	 
	  minim = new Minim(papplet);
	  myinput = minim.getLineIn(Minim.MONO, 2048, 44100.0f);
	  
	  fft = new FFT(myinput.bufferSize(), myinput.sampleRate());
	  fft.window(FourierTransform.HAMMING);
	  
	  
	  
	  lastEventLF = lastEventHF = PatternDetector.SNDPT_NONE;
	  lastPeakPosLF = lastPeakPosHF = -1;
	  pattyLF = new PatternDetector("LF", "patternDetectors/lowFreq");
	  pattyHF = new PatternDetector("HF", "patternDetectors/lowFreq");

	  pattyLF.addObserver(this);
	  pattyHF.addObserver(this);
	  
	  
	}
	 
	
	
	
	public void analyseNextChunk()
	{
		
	  
	  
	  
	  
	  
	 
	  int lastFoundEventHF = PatternDetector.SNDPT_NONE;
	  int lastFoundEventLF = PatternDetector.SNDPT_NONE;
	  
	  
	 
	  
	  
	  
	 
	  fft.forward(myinput.mix);
	  
	  float[] freqArray = new float[frequenciesOfInterest.length];
	  
	  int strongestPitchPositionLF = -1;
	  int strongestPitchPositionHF = -1;
	  
	  float strongestPitchStrengthLF = -1f;
	  float strongestPitchStrengthHF = -1f;
	  
	  
	 
	  for(int i = 0; i < frequenciesOfInterest.length; i++)
	  {
		
		
	    // draw a rectangle for each average, multiply the value by 5 so we can see it better
		float freqStrength = fft.getFreq(frequenciesOfInterest[i]);
		
		 
		freqArray[i] = freqStrength;
		
		
		// mark the peaks
	    if (i > 0 && i < frequenciesOfInterest.length -1)
	    {
	    	float prevFreqStrength = fft.getFreq(frequenciesOfInterest[i-1]);
		    float nextFreqStrength = fft.getFreq(frequenciesOfInterest[i+1]);
	    	
		    if (freqStrength > prevFreqStrength && freqStrength >= nextFreqStrength) {
		    
				if (freqStrength > iAWLF.getSMin() && freqStrength < iAWLF.getSMax()
						&& i > iAWLF.getFMin() && i < iAWLF.getFMax()) { // LF?
			    	if (freqStrength > strongestPitchStrengthLF) {
						strongestPitchStrengthLF = freqStrength;
						strongestPitchPositionLF = i;
					}
			    	//peakArray[i] = GENERAL_PEAK;
				} else if (freqStrength > iAWHF.getSMin() && freqStrength < iAWHF.getSMax()
						&& i > iAWHF.getFMin() && i < iAWHF.getFMax()) { //MAYBE HF?
					if (freqStrength > strongestPitchStrengthHF) {
						strongestPitchStrengthHF = freqStrength;
						strongestPitchPositionHF = i;
					}
					//peakArray[i] = GENERAL_PEAK;
				} 
				
				
		    }
	    }
		
		
	    
	     
	   
	      
	      
	    
	  		

	  	  
	  		
	  		
	    
	  }
	  
	  // strongest LF
	  if (strongestPitchPositionLF > -1) {
		  iAWLF.setSA(calcMovAverage(strongestPitchStrengthLF, 1f, iAWLF.getSA(), numberOfPeaksForMovingAverage));
		  iAWLF.setFA(calcMovAverage(strongestPitchPositionLF, 1f, iAWLF.getFA(), numberOfPeaksForMovingFreqAverage));
    	  iAWPeakPercentageLF = calcMovAverage(1f, windowContractionSpeed, iAWPeakPercentageLF, peakPercentageSampleSize);
    	  
    	  
    	 
    			//peakArray[strongestPitchPositionLF] = STRONGEST_PEAK;
    			if (lastPeakPosLF != -1) {
    				if (lastPeakPosLF < strongestPitchPositionLF) {
    					lastFoundEventLF = PatternDetector.SNDPT_HIGHER;
    				} else if (lastPeakPosLF > strongestPitchPositionLF) {
    					lastFoundEventLF = PatternDetector.SNDPT_LOWER;
    				} else {
    					lastFoundEventLF = PatternDetector.SNDPT_SAME;
    				}
    			}
    			lastPeakPosLF = strongestPitchPositionLF;
    	  		
    	  
    	 
    	  strongestPitchPositionLF = -1;
    	  strongestPitchStrengthLF = 0;
	  } else {
		  
		 
		  iAWPeakPercentageLF = calcMovAverage(0f, windowExpansionSpeed, iAWPeakPercentageLF, peakPercentageSampleSize);
		  
		  
	  }
	  
	  iAWLF.recalc(iAWPeakPercentageLF);
	 
	  
  
	  
	 
	  
	  
	  // mark strongest Peak
	  if (strongestPitchPositionHF > -1)
	  {
		  iAWHF.setSA(calcMovAverage(strongestPitchStrengthHF, 1f, iAWHF.getSA(), numberOfPeaksForMovingAverage));
		  iAWHF.setFA(calcMovAverage(strongestPitchPositionHF, 1f, iAWHF.getFA(), numberOfPeaksForMovingFreqAverage));
		  
		  iAWPeakPercentageHF = calcMovAverage(1f, windowContractionSpeed, iAWPeakPercentageHF, peakPercentageSampleSize);
		  
    	 
    	  
    			//peakArray[strongestPitchPositionHF] = STRONGEST_PEAK;
    			if (lastPeakPosHF != -1) {
    				if (lastPeakPosHF < strongestPitchPositionHF) {
    					lastFoundEventHF = PatternDetector.SNDPT_HIGHER;
    				} else if (lastPeakPosHF > strongestPitchPositionHF) {
    					lastFoundEventHF = PatternDetector.SNDPT_LOWER;
    				} else {
    					lastFoundEventHF = PatternDetector.SNDPT_SAME;
    				}
    			}
    			lastPeakPosHF = strongestPitchPositionHF;
    	  		strongestPitchPositionHF = -1;
    	  		
    	  
    	  		
    	  		
    	  
    	    
    	  strongestPitchStrengthHF = 0;
	  } else {
	
		  iAWPeakPercentageHF = calcMovAverage(0f, windowExpansionSpeed, iAWPeakPercentageHF, peakPercentageSampleSize);
	  }
	  iAWHF.recalc(iAWPeakPercentageHF);
	 
	  
	  
	  
	  
	 
	  
	  
	  pattyLF.pushEvent(lastFoundEventLF);
	  pattyHF.pushEvent(lastFoundEventHF);
	  
	  
	  
	  
	  	
	    
	    
	}
	
	
	
	
	 
	
	public void stop()
	{
	  // always close Minim audio classes when you are done with them
	  myinput.close();
	  // always stop Minim before exiting
	  minim.stop();
	 
	  
	}
	
	/**
	 * 
	 * @param probe the probe to add 
	 * @param probeWeight weight of the probe (e.g. 2: double the weight / 0.5 half the weight)
	 * @param average current calculated average
	 * @param windowSize  the number of probes taken into consideration - including the new one
	 * @return
	 */
	private float calcMovAverage(float probe, float probeWeight, float average, int windowSize) {
		return (average * (windowSize - probeWeight) 
				+ probe * probeWeight)
				/ windowSize;
	}



	@Override
	public void update(Observable arg0, Object arg1)
	{
		// print current states
		((PatternDetector)arg0).printCurrentStates();
		
		// notify BunnyHat
		this.setChanged();
		this.notifyObservers();
	}




	@Override
	public void run()
	{
		
		
		
		lastFrameTime = 0; 
		lastFpsTime = 0;
		while (true) {
			try
			{
				Thread.currentThread().sleep(frameSize);
				
				currentTimeStamp = ourPApplet.millis();
				
				if ((currentTimeStamp - lastFrameTime) >= frameSize) {
					lastFrameTime = currentTimeStamp;
				
				
					// analyse
					this.analyseNextChunk();
					
					// Print the fps
					
					if ((currentTimeStamp - lastFpsTime) > 1000)
					{
						System.out.println("FPS-sound: " + fps);
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
	
	
}
