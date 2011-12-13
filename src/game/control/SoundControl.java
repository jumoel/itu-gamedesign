package game.control;





import game.BunnyHat;
import game.graphics.Animation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Observable;
import java.util.Observer;

import processing.core.*;
import ddf.minim.analysis.*;
import ddf.minim.*;


public class SoundControl extends Observable implements Observer, Runnable {
	private final boolean showFPS = false;
	
	private final boolean DYNAMIC_ATTENTION_WINDOW_WIDTH = false;
	
	// Thread Control
	private Thread ourThread;
	private boolean keepListening = false;
	
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
			this.cFMin = this.iFMin = freqMin;
			this.cFMax = this.iFMax = freqMax;
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
			if (DYNAMIC_ATTENTION_WINDOW_WIDTH) {
				cFMin = theoFMin;
				cFMax = theoFMax;
			}

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
	
	
	
	
	int frequencyBorder = BunnyHat.SETTINGS.getValue("soundControl/frequencyBorder"); // frequency to determine high frequency
	int frequencyBorderPos; // array pos of the border
	int minGapLength; //  
	int maxGapLength; //
	float freqGraphFactor = 1f; // how much to amplify the freq graph display
	
	
	Minim minim;
	AudioInput myinput;
	FFT fft;
	
	
	//general settings
	private long frameSize = 42; // 42ms = roundabout 23 fps

	private int bufferSize = 84;
	
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

	RingBuffer<float[]> ourFrequencyBuffer;
	RingBuffer<int[]> ourPeakBuffer;
	//RingBuffer<int[]> ourPeakBufferW;
	RingBuffer<Integer> ourEventBufferLF;
	RingBuffer<Integer> ourEventBufferHF;
	//RingBuffer<Integer> ourEventBufferW;
	RingBuffer<Integer> ourDetectedPatternBufferLF;
	RingBuffer<Integer> ourDetectedPatternBufferHF;
	//RingBuffer<Integer> ourDetectedPatternBufferW;
	
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
		 61.74f, //B1 	
		 65.41f, //C2 - begin of human voice range	0
		 69.30f, //C#2/Db2 	
		 73.42f, //D2 	
		 77.78f, //D#2/Eb2 	
		 82.41f,*/ //E2 	     - start of human voice range
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
	
	// keyboard control
		public boolean drawAnalysis = true;
		private boolean showAllFrequencies = true;
		private boolean markPeakTypes = true;
		public boolean drawLines = false;
		private boolean hideWeakFrequencies = false;
		public boolean markStrongestPeaks = true;
		
	// graphics
	PGraphics displayBuffer;
	PGraphics historyBuffer;
	PGraphics statesBuffer;
	Animation blueGum, redGum, blueIdle, blueShoot, redIdle, redShoot;
	
	PApplet ourPApplet;
	
	private float[] freqArray;
	private int[] peakArray;
	 
	public SoundControl(PApplet papplet)
	{
	  ourPApplet = papplet;
	  
	  blueGum = new Animation(ourPApplet, "graphics/animations/bubbleGumBoy");
	  redGum = new Animation(ourPApplet, "graphics/animations/bubbleGumGirl");
	  blueGum.start();
	  redGum.start();
	  blueIdle =  new Animation(ourPApplet, "graphics/animations/player1idleGun");
	  blueShoot = new Animation(ourPApplet, "graphics/animations/player1shot");
	  redIdle = new Animation(ourPApplet, "graphics/animations/player2idleGun");
	  redShoot = new Animation(ourPApplet, "graphics/animations/player2shot");
	  blueIdle.start();
	  redIdle.start();
	  
	  movingAverageCompareThreshold = 0.5f;
	  mainPeakThreshold = 3;
	  lonelyPeakThreshold = 20;
	  
	  
	  minGapLength = 2; 
	  maxGapLength = 7;
	  
	  
	  
	  // human voice frequency range: 60 Hz - 7000 Hz
	  // whistling range: 1300 Hz - 4000 Hz
	  frequencyBorderPos = 42; //position in the array
	  for (int i = 0; i < frequenciesOfInterest.length; i++) {
		  if (frequencyBorder <= frequenciesOfInterest[i]) {
			  frequencyBorderPos = i;
			  break;
		  }
	  }
	  
	  // setup attention windows
	  
	  
	  int minPosLF =getFreqMinPos((Integer)BunnyHat.SETTINGS.getValue("soundControl/attentionWindowLF/frequencyMin")); 
	  int maxPosLF =getFreqMaxPos((Integer)BunnyHat.SETTINGS.getValue("soundControl/attentionWindowLF/frequencyMax"));
	  int minPosHF =getFreqMinPos((Integer)BunnyHat.SETTINGS.getValue("soundControl/attentionWindowHF/frequencyMin")); 
	  int maxPosHF =getFreqMaxPos((Integer)BunnyHat.SETTINGS.getValue("soundControl/attentionWindowHF/frequencyMax"));
	  iAWLF = new AW(initStrengthMin, initStrengthMax, minPosLF, maxPosLF, minHeight, minWidth);
	  iAWHF = new AW(initStrengthMin, initStrengthMax, minPosHF, maxPosHF, minHeight, minWidth);
	  
	  iAWLF.setFA((iAWLF.getFMax()-iAWLF.getFMin())/2); 
	  iAWHF.setFA((iAWHF.getFMax() - iAWHF.getFMin()) / 2);
	  
	 
	  minim = new Minim(papplet);
	  myinput = minim.getLineIn(Minim.MONO, 2048, 44100.0f);
	  if (myinput == null) {
		  System.out.println("did not find a supported sound input device - disabling sound control");
		  BunnyHat.SOUND_CONTROL_SUPPORTED = false;
		  return;
	  }
	  
	  fft = new FFT(myinput.bufferSize(), myinput.sampleRate());
	  fft.window(FourierTransform.HAMMING);
	  
	  
	  
	  ourFrequencyBuffer = new RingBuffer<float[]>(bufferSize);
	  ourPeakBuffer = new RingBuffer<int[]>(bufferSize);
	  //ourPeakBufferW = new RingBuffer<int[]>(42);
	  ourEventBufferLF = new RingBuffer<Integer>(bufferSize + 1);
	  ourEventBufferHF = new RingBuffer<Integer>(bufferSize + 1);
	  //ourEventBufferW = new RingBuffer<Integer>(freqHistoryHeight/lineHeight + 1);
	  ourDetectedPatternBufferLF = new RingBuffer<Integer>(bufferSize + 1);
	  ourDetectedPatternBufferHF = new RingBuffer<Integer>(bufferSize + 1);
	  //ourDetectedPatternBufferW = new RingBuffer<Integer>(freqHistoryHeight/lineHeight + 1);
	  
	  lastEventLF = lastEventHF = PatternDetector.SNDPT_NONE;
	  lastPeakPosLF = lastPeakPosHF = -1;
	  try
	  {
			pattyLF = new PatternDetector("LF", "patternDetectors/lowFreq/", papplet);
			pattyHF = new PatternDetector("HF", "patternDetectors/highFreq/", papplet);
	  }
	  catch (Exception e)
	  {
			// TODO Auto-generated catch block
			e.printStackTrace();
	  }
	  

	  pattyLF.addObserver(this);
	  pattyHF.addObserver(this);
	  
	  freqArray = new float[frequenciesOfInterest.length];
	  peakArray = new int[frequenciesOfInterest.length];
	  
	  
	}
	
	private int getFreqMinPos(int frequency) {
		int pos = 0;
		for (int i = 0; i < frequenciesOfInterest.length; i++) {
			if (frequency < frequenciesOfInterest[i]) break;
			pos = i;
		}
		return pos;
	}
	
	private int getFreqMaxPos(int frequency) {
		for (int i = 0; i < frequenciesOfInterest.length; i++) {
			if (frequency < frequenciesOfInterest[i]) return i;
		}
		return frequenciesOfInterest.length-1;
	}
	
	
	public PImage drawPatternDetectorStates() {
		PImage imgPattyHF = pattyHF.drawPatternDetectorStates();
		PImage imgPattyLF = pattyLF.drawPatternDetectorStates();
		
		if (statesBuffer == null || statesBuffer.width != imgPattyLF.width + imgPattyHF.width 
				|| statesBuffer.height < imgPattyLF.height || statesBuffer.height < imgPattyHF.height) {
			statesBuffer = ourPApplet.createGraphics(imgPattyLF.width + imgPattyHF.width, 
					imgPattyLF.height > imgPattyHF.height ? imgPattyLF.height : imgPattyHF.height, PConstants.JAVA2D);
		}
		statesBuffer.beginDraw();
		
		statesBuffer.image(imgPattyLF, 0, statesBuffer.height - imgPattyLF.height);
		statesBuffer.image(imgPattyHF, imgPattyLF.width, statesBuffer.height - imgPattyHF.height);
		
		statesBuffer.endDraw();
		return statesBuffer;
	}
	
	public void updateFreqHistory(int x, int y, int totalWidth, int height) {
		int width = totalWidth - blueIdle.getCurrentImage(ourPApplet.millis()).width;
		
		int w = new Integer(height/frequenciesOfInterest.length);
		
		if (historyBuffer == null || historyBuffer.width != width || historyBuffer.height != height) {
			historyBuffer = ourPApplet.createGraphics(width, height, PConstants.JAVA2D);
		} else if (!drawAnalysis) {
			PImage girl = redIdle.getCurrentImage(ourPApplet.millis());
			  PImage boy = blueIdle.getCurrentImage(ourPApplet.millis());
			  
			  ourPApplet.image(historyBuffer, blueIdle.getCurrentImage(ourPApplet.millis()).width, y);
			  ourPApplet.image(girl, 0, height-lastPeakPosHF*w-girl.height/3*2);
			  ourPApplet.image(boy, 0, height-lastPeakPosLF*w-boy.height/3*2);
			
			return;
		}
		
		historyBuffer.beginDraw();
		
		
		historyBuffer.background(0, 0);
		//historyBuffer.rect(x, y, width, height);
		historyBuffer.strokeWeight(1);
		  
		  int lineHeight = width/bufferSize;
		  
		  
		  //draw the freqRecord so far
		  Iterator<float[]> ringIt = ourFrequencyBuffer.iterator();
		  Iterator<int[]> ringItPeak = ourPeakBuffer.iterator();
		  
		  //and draw our events
		  Iterator<Integer> ringItEventLF = ourEventBufferLF.iterator();
		  Iterator<Integer> ringItEventHF = ourEventBufferHF.iterator();
		  
		  
		  int currentLine = height/lineHeight + 1;
		  int lastLowPitchX = -1;
		  int lastLowPitchY = -1;
		  int lastHighPitchX = -1;
		  int lastHighPitchY = -1;
		  int lastHFPeakDistance = 0;
		  int lastLFPeakDistance = 0;
		  int lastHFPeakPos = -1;
		  int lastLFPeakPos = -1;
		  
		  boolean thereHasBeenAPeak = false;
		  
		  //boolean newEventRecorded = false;
		  int lastFoundEventHF = PatternDetector.SNDPT_NONE;
		  int lastFoundEventLF = PatternDetector.SNDPT_NONE;
		  
		  
		while(ringIt.hasNext()) {
			  currentLine--;
			  float[] freqArray = ringIt.next();
			  int[] peakArray = ringItPeak.next();
			  
			  lastFoundEventLF = PatternDetector.SNDPT_NONE;
			  lastFoundEventHF = PatternDetector.SNDPT_NONE;
			  
			  
			  
			  int currentX = 0;
	 		  for (int i = 0; i < freqArray.length; i++)
			  {
	 			 thereHasBeenAPeak = false;
	 			 
	 			 int currentY = height-i*w;
	 			 currentX = height-currentLine*lineHeight;
	 			  
	 			historyBuffer.noStroke();
	 			//historyBuffer.fill((int)Math.log(freqArray[i])*42);
	 			historyBuffer.noFill();

			    if (peakArray[i] == GENERAL_PEAK && markPeakTypes)
			    {
			    	historyBuffer.stroke(0, 200, 0);
			    	historyBuffer.fill(0, (int)Math.log(freqArray[i])*42, 0);   
			    }
			    else if (peakArray[i] == LONELY_PEAK && markPeakTypes)
			    {
			    	historyBuffer.stroke(200, 0, 0);
			    	historyBuffer.fill((int)Math.log(freqArray[i])*42, 0, 0);
			    }
			    
			    if (peakArray[i] == STRONGEST_PEAK && markStrongestPeaks)
	 			  {
				    	historyBuffer.stroke(0, 0, 200);
				    	historyBuffer.fill(0, 0, (int)Math.log(freqArray[i]+1)*42);
				    	thereHasBeenAPeak = true;
	 			  }
			    
			    
			    //historyBuffer.rect(currentX, currentY, w, -lineHeight + 1);
			    
			    
			    // process transition to high frequency:
			    // has there been any peak in the lowFQ range?
			    if (i == frequencyBorderPos-1) {
			    	if (!thereHasBeenAPeak) {
			    		lastLFPeakDistance++;
			    		
			    	}
			    }
			    
			    
			    // draw peak line
			    if (peakArray[i] == STRONGEST_PEAK)
			    {
			    	
			    	if (i < frequencyBorderPos) {
			    		
			    		
			    		historyBuffer.stroke(0, 0, 255);
			    		if (lastLowPitchX > -1 
			    				&& lastLowPitchY > -1
			    				&& lastLFPeakPos > - 1)
			    		{
			    			int oldXmed = (lastLowPitchX + w/2) ;
				    		int oldYmed = (lastLowPitchY - lineHeight/2 + 1);
				    		int newYmed = (currentY + w/2);
				    		int newXmed = (currentX - lineHeight/2 + 1);
				    		
				    		int posDifference = i - lastLFPeakPos;
				    		
				    		boolean gap = (lastLFPeakDistance > minGapLength
		    				         && lastLFPeakDistance < maxGapLength);
				    		boolean bigGap = (lastLFPeakDistance >= maxGapLength);
				    		if (bigGap) posDifference = 0;
				    		
				    		
				    		
				    		
				    		
				    		if (posDifference > 0) {
				    			historyBuffer.stroke(255, 0, (gap?255:0));
				    		} else if (posDifference < 0) {
				    			historyBuffer.stroke(0, 255, (gap?255:0));
				    		} else {
				    			historyBuffer.stroke(255, 255, (gap?255:0));
				    		}
				    	
				    		if (bigGap) { historyBuffer.stroke(0, 0, 0); historyBuffer.noStroke();}
			    			if (drawLines) historyBuffer.line(oldXmed, oldYmed, newXmed, newYmed);
			    			
			    			lastLFPeakDistance = 0;
			    		} 
			    		
			    		lastLFPeakPos = i;
			    		lastLowPitchY = currentY;
			    		lastLowPitchX = currentX;
			    		
			    	} else {
			    		
			    		historyBuffer.stroke(0, 0, 255);
			    		if (lastHighPitchX > -1 
			    				&& lastHighPitchY > -1
			    				&& lastHFPeakPos > - 1)
			    		{
			    			int oldXmed = (lastHighPitchX + w/2) ;
				    		int oldYmed = (lastHighPitchY - lineHeight/2 + 1);
				    		int newYmed = (currentY + w/2);
				    		int newXmed = (currentX - lineHeight/2 + 1);
				    		
				    		int posDifference = i - lastHFPeakPos;
				    		
				    		boolean gap = (lastHFPeakDistance > minGapLength
				    				         && lastHFPeakDistance < maxGapLength);
				    		boolean bigGap = (lastHFPeakDistance >= maxGapLength);
				    		
				    		if (posDifference > 0) {
				    			historyBuffer.stroke(255, 0, (gap?255:0));
				    		} else if (posDifference < 0) {
				    			historyBuffer.stroke(0, 255, (gap?255:0));
				    		} else {
				    			historyBuffer.stroke(255, 255, (gap?255:0));
				    		}
				    	
				    		if (bigGap) { historyBuffer.stroke(0, 0, 0); historyBuffer.noStroke();}
				    		if (drawLines) historyBuffer.line(oldXmed, oldYmed, newXmed, newYmed);
			    			
			    			lastHFPeakDistance = 0;
			    		} 
			    		
			    		lastHFPeakPos = i;
			    		lastHighPitchY = currentY;
			    		lastHighPitchX = currentX;
			    	}
			    } 
			    
			    
			    
			    
			    
			  }
	 		  
	 		  for (int i = 0; i < peakArray.length; i++)
			  {
	 			 int currentY = height-i*w;
	 			  if (peakArray[i] == STRONGEST_PEAK && markStrongestPeaks)
	 			  {
				    	
				    	// nicer
				    	int ballY = (int)(currentY);
				    	int ballX = (int)(currentX-lineHeight);
				    	if (i < frequencyBorderPos) {
				    		historyBuffer.image(blueGum.getCurrentImage(ourPApplet.millis()), ballX, ballY);
				    	} else {
				    		historyBuffer.image(redGum.getCurrentImage(ourPApplet.millis()), ballX, ballY);
				    	}
	 			  }
			  }
	 		 historyBuffer.noStroke();
	 		  
	 		  
	 		  
	 		 if (!thereHasBeenAPeak) {
		    	lastHFPeakDistance++;
		    	
		     }
	 		 
	 		 //mark events
	 		currentX = height-(currentLine)*lineHeight;
			switch (ringItEventLF.next()) {
		    case PatternDetector.SNDPT_SAME:
		    	historyBuffer.fill(255, 255, 0);
		    	break;
		    case PatternDetector.SNDPT_HIGHER:
		    	historyBuffer.fill(0, 255, 0);
		    	break;
		    case PatternDetector.SNDPT_LOWER:
		    	historyBuffer.fill(255, 0, 0);
		    	break;
		    case PatternDetector.SNDPT_GAP_SAME:
		    	historyBuffer.fill(255, 255, 255);
		    	break;
		    case PatternDetector.SNDPT_GAP_HIGHER:
		    	historyBuffer.fill(0, 255, 255);
		    	break;
		    case PatternDetector.SNDPT_GAP_LOWER:
		    	historyBuffer.fill(255, 0, 255);
		    	break;
		    case PatternDetector.PATTERN_NONE:
		    default:
		    	historyBuffer.noFill();
		    	break;
		    }
			historyBuffer.rect(currentX, height-12, - lineHeight + 1, 12);
			
			currentX = height-(currentLine)*lineHeight;
			switch (ringItEventHF.next()) {
		    case PatternDetector.SNDPT_SAME:
		    	historyBuffer.fill(255, 255, 0);
		    	break;
		    case PatternDetector.SNDPT_HIGHER:
		    	historyBuffer.fill(0, 255, 0);
		    	break;
		    case PatternDetector.SNDPT_LOWER:
		    	historyBuffer.fill(255, 0, 0);
		    	break;
		    case PatternDetector.SNDPT_GAP_SAME:
		    	historyBuffer.fill(255, 255, 255);
		    	break;
		    case PatternDetector.SNDPT_GAP_HIGHER:
		    	historyBuffer.fill(0, 255, 255);
		    	break;
		    case PatternDetector.SNDPT_GAP_LOWER:
		    	historyBuffer.fill(255, 0, 255);
		    	break;
		    default:
		    	historyBuffer.noFill();
		    	break;
		    }
			historyBuffer.rect(currentX, 0, - lineHeight + 1, 12);
	 		
		    
		    
	 		 
	 		 
			}
		  if (lastFoundEventHF == PatternDetector.SNDPT_NONE) {
			  if (lastHFPeakDistance > maxGapLength ) {
				 
				  lastHFPeakPos = -1;
			  } 
		  }
		  
		//mark patterns
		Iterator<Integer> ringItPatternLF = ourDetectedPatternBufferLF.iterator();
		markPattern(historyBuffer, width, lineHeight, height-13, height-frequencyBorderPos*w, ringItPatternLF);
		
		
		Iterator<Integer> ringItPatternHF = ourDetectedPatternBufferHF.iterator();
		markPattern(historyBuffer, width, lineHeight, 13, frequencyBorderPos*w, ringItPatternHF);
			
		  
		historyBuffer.endDraw();
	  
	  	PImage girl = redIdle.getCurrentImage(ourPApplet.millis());
	  	PImage boy = blueIdle.getCurrentImage(ourPApplet.millis());
	  
	  	ourPApplet.image(historyBuffer, blueIdle.getCurrentImage(ourPApplet.millis()).width, y);
	  	ourPApplet.image(girl, 0, height-lastPeakPosHF*w-girl.height/3*2);
	  	ourPApplet.image(boy, 0, height-lastPeakPosLF*w-boy.height/3*2);
	}
	
	private void markPattern(PGraphics buffer, int width, int lineHeight, int y0, int y1, Iterator<Integer> ringItPattern) { 
		int currentLine = width/lineHeight + 1;
		int lastPattern = PatternDetector.PATTERN_NONE;
		historyBuffer.noStroke();
		while (ringItPattern.hasNext()) {
			currentLine--;
			int currentX = width-(currentLine+1)*lineHeight;
			int pattern = ringItPattern.next();
			switch (pattern) {
		    case PatternDetector.PATTERN_NONE:
		    	historyBuffer.noStroke();
		    	historyBuffer.noFill();
		    	break;
		    case PatternDetector.PATTERN_STRAIGHT_GAPISH:
		    	historyBuffer.fill(0, 255, 255);
		    	historyBuffer.stroke(0, 255, 255);
		    	break;
		    case PatternDetector.PATTERN_STRAIGHT_SOLID:
		    	historyBuffer.fill(0 , 0, 255);
		    	historyBuffer.stroke(0 , 0, 255);
		    	break;
		    case PatternDetector.PATTERN_UP_DOWN_GAPISH:
		    	historyBuffer.fill(255, 255, 0);
		    	historyBuffer.stroke(255, 255, 0);
		    	break;
		    case PatternDetector.PATTERN_UP_DOWN_SOLID:
		    	historyBuffer.fill(255, 0, 0);
		    	historyBuffer.stroke(255, 0, 0);
		    	break;
		    default:
		    	historyBuffer.noStroke();
		    	historyBuffer.noFill();
		    	break;
		    }
			
		    
		    historyBuffer.rect(currentX+lineHeight/2+2, y0, - lineHeight , -(y0 < y1?-4:4));
		    if (lastPattern != pattern) {
		    	historyBuffer.line(currentX - lineHeight/2 +2 , y0, currentX - lineHeight/2 + 2, y1);
		    	historyBuffer.ellipse(currentX - 2, y1, 4, 4);
		    }
		    
		    lastPattern = pattern;
			
		}
	}
	
	
	
	public void updateFreqDisplay(int x, int y, int width, int height) {
		if (displayBuffer == null || displayBuffer.width != width || displayBuffer.height != height) {
			displayBuffer = ourPApplet.createGraphics(width, height, PConstants.JAVA2D);
		}
		displayBuffer.beginDraw();
		
		displayBuffer.background(0);
		
		this.displayBuffer.noStroke();
		
		
		int w = width / freqArray.length;
		for (int i = 0; i < freqArray.length; i++) {
			float freqStrength = freqArray[i];
			
			
			
			if (peakArray[i] == GENERAL_PEAK) {
				displayBuffer.fill(0,255,0);
				displayBuffer.rect(i*w, height, w,  - freqStrength*freqGraphFactor);
			} else if (peakArray[i] == LONELY_PEAK) {
				displayBuffer.fill(255,0,0);
				displayBuffer.rect(i*w, height, w,  - freqStrength*freqGraphFactor);
			} else {
				displayBuffer.fill(255);
				displayBuffer.rect(i*w, height, w, -freqStrength*freqGraphFactor);
			}
		}
		
		
		// draw moving average lines
		  if (true) {
			  // STRENGTH
			  //LF
			  float currentY = height - iAWLF.getSA()*freqGraphFactor;
			  displayBuffer.stroke(100, 0, 0);
			  displayBuffer.line(0, currentY , frequencyBorderPos*w, currentY);
			  
			  //HF
			  currentY = height - iAWHF.getSA()*freqGraphFactor;
			  displayBuffer.stroke(0, 100, 0);
			  displayBuffer.line(frequencyBorderPos*w, currentY ,width , currentY);
			  
			  
			 
			  
			  
			  // FREQ
			  // LF
			  float currentX = w*iAWLF.getFA();
			  displayBuffer.stroke(100, 0, 0);
			  displayBuffer.line(currentX, height, currentX, 0);
			  
			  // HF
			  currentX = w*iAWHF.getFA();
			  displayBuffer.stroke(0, 100, 0);
			  displayBuffer.line(currentX, height, currentX, 0);
			  
			  
			  //BOXES
			  displayBuffer.noFill();
			  // Freq / Strength LF min / max 
			  float xFMaxLF = w * iAWLF.getFMax();
			  float xFMinLF = w * iAWLF.getFMin();
			  float ySMaxLF = height - iAWLF.getSMax()*freqGraphFactor;
			  float ySMinLF = height - iAWLF.getSMin()*freqGraphFactor;
			  displayBuffer.stroke(255, 0, 0);
			  displayBuffer.rect(xFMinLF, ySMinLF, xFMaxLF - xFMinLF, ySMaxLF - ySMinLF);
			  // Freq HF min / max
			  float xFMaxHF = w * iAWHF.getFMax();
			  float xFMinHF = w * iAWHF.getFMin();
			  float ySMaxHF = height - iAWHF.getSMax()*freqGraphFactor;
			  float ySMinHF = height - iAWHF.getSMin()*freqGraphFactor;
			  displayBuffer.stroke(0, 255, 0);
			  displayBuffer.rect(xFMinHF, ySMinHF, xFMaxHF-xFMinHF, ySMaxHF-ySMinHF);
			  
		  }
		  
		//draw separation line
		  displayBuffer.stroke(255);
		  displayBuffer.line(frequencyBorderPos*w, height, frequencyBorderPos*w, 0);
		  displayBuffer.endDraw();
		  ourPApplet.image(displayBuffer, x, y);
	}
	
	private void analyseNextChunk()
	{
		
	  
	  
	 
	  int lastFoundEventHF = PatternDetector.SNDPT_NONE;
	  int lastFoundEventLF = PatternDetector.SNDPT_NONE;
	  
	  
	 
	  fft.forward(myinput.mix);
	  
	  freqArray = new float[frequenciesOfInterest.length];
	  peakArray = new int[frequenciesOfInterest.length];
	  
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
			    	peakArray[i] = GENERAL_PEAK;
				} else if (freqStrength > iAWHF.getSMin() && freqStrength < iAWHF.getSMax()
						&& i > iAWHF.getFMin() && i < iAWHF.getFMax()) { //MAYBE HF?
					if (freqStrength > strongestPitchStrengthHF) {
						strongestPitchStrengthHF = freqStrength;
						strongestPitchPositionHF = i;
					}
					peakArray[i] = GENERAL_PEAK;
				} 
				
				
		    }
	    }
		
	  	  
	  		
	  		
	    
	  }
	  
	  // strongest LF
	  if (strongestPitchPositionLF > -1) {
		  iAWLF.setSA(calcMovAverage(strongestPitchStrengthLF, 1f, iAWLF.getSA(), numberOfPeaksForMovingAverage));
		  iAWLF.setFA(calcMovAverage(strongestPitchPositionLF, 1f, iAWLF.getFA(), numberOfPeaksForMovingFreqAverage));
    	  iAWPeakPercentageLF = calcMovAverage(1f, windowContractionSpeed, iAWPeakPercentageLF, peakPercentageSampleSize);
    	  
    	  
    	 
    			peakArray[strongestPitchPositionLF] = STRONGEST_PEAK;
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
		  
		  //iAWLF.setSA(calcMovAverage(0f, windowExpansionSpeed, iAWLF.getSA(), numberOfPeaksForMovingAverage));
		  iAWLF.setFA(calcMovAverage(0f, windowExpansionSpeed, iAWLF.getFA(), numberOfPeaksForMovingFreqAverage));
		  iAWPeakPercentageLF = calcMovAverage(0f, windowExpansionSpeed, iAWPeakPercentageLF, peakPercentageSampleSize);
		  
		  
	  }
	  
	  iAWLF.recalc(iAWPeakPercentageLF);
	 
	  
  
	  
	 
	  
	  
	  // mark strongest Peak
	  if (strongestPitchPositionHF > -1)
	  {
		  iAWHF.setSA(calcMovAverage(strongestPitchStrengthHF, 1f, iAWHF.getSA(), numberOfPeaksForMovingAverage));
		  iAWHF.setFA(calcMovAverage(strongestPitchPositionHF, 1f, iAWHF.getFA(), numberOfPeaksForMovingFreqAverage));
		  
		  iAWPeakPercentageHF = calcMovAverage(1f, windowContractionSpeed, iAWPeakPercentageHF, peakPercentageSampleSize);
		  
    	 
    	  
    			peakArray[strongestPitchPositionHF] = STRONGEST_PEAK;
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
		  iAWHF.setSA(calcMovAverage(0f, windowExpansionSpeed, iAWHF.getSA(), numberOfPeaksForMovingAverage));
		  //iAWHF.setFA(calcMovAverage(0f, windowExpansionSpeed, iAWHF.getFA(), numberOfPeaksForMovingFreqAverage));
		  iAWPeakPercentageHF = calcMovAverage(0f, windowExpansionSpeed, iAWPeakPercentageHF, peakPercentageSampleSize);
	  }
	  iAWHF.recalc(iAWPeakPercentageHF);
	 
	  
	 
	  //pattyLF.pushEvent(lastFoundEventLF);
	  //pattyHF.pushEvent(lastFoundEventHF);
	  
	  if (drawAnalysis) {
		  ourFrequencyBuffer.enqueue(freqArray);
		  ourPeakBuffer.enqueue(peakArray);
		  //ourPeakBufferW.enqueue(peakArrayW);
		  
		  pushEvent(lastFoundEventLF, pattyLF, 
				  ourEventBufferLF, ourDetectedPatternBufferLF);
		  pushEvent(lastFoundEventHF, pattyHF,
				  ourEventBufferHF, ourDetectedPatternBufferHF);
		  /*pushEvent(lastFoundEventW, ourPatternW, pattyW,
				  ourEventBufferW, ourDetectedPatternBufferW);*/
		  
	  }
	     
	}
	
	public void pushEvent(int type, PatternDetector patty,
			RingBuffer<Integer> eventBuffer, RingBuffer<Integer> patternBuffer) {
		
		
		patty.pushEvent(type);
		eventBuffer.enqueue(type); 
		patternBuffer.enqueue(patty.getPatternNumber());
		
		
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
		//((PatternDetector)arg0).printCurrentStates();
		
		// notify BunnyHat and whoever might be interested
		this.setChanged();
		HashMap arguments = new HashMap();
		arguments.put("detector", ((PatternDetector)arg0).getName());
		arguments.put("pattern", ((PatternDetector)arg0).getPattern());
		this.notifyObservers(arguments);
		
	}

	
	public void startListening() {
		keepListening = true;
		ourThread = new Thread(this);
		ourThread.start();
	}
	
	public void stopListening() {
		keepListening = false;
	}


	@SuppressWarnings("static-access")
	@Override
	public void run()
	{
		
		
		int fps = 0;
		int lastFrameTime = 0; 
		int lastFpsTime = 0;
		while (keepListening) {
			try
			{
				Thread.currentThread().sleep(frameSize);
				
				int currentTimeStamp = ourPApplet.millis();
				
				if ((currentTimeStamp - lastFrameTime) >= frameSize) {
					lastFrameTime = currentTimeStamp;
				
				
					// analyse
					this.analyseNextChunk();
					
					// Print the fps
					
					if ((currentTimeStamp - lastFpsTime) > 1000)
					{
						if (showFPS) System.out.println("FPS-sound: " + fps);
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
