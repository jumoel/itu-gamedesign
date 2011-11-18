package game.control;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;

/**
 * Pattern detector for sound analysis
 * @author Samuel Walz <samuel.walz@gmail.com>
 *
 */
public class PatternDetector extends Observable {
	public static final boolean DEBUG = false;
	
	private class PDS { // Pattern Detector State
		// Decision base
		public PDS onNone = null;
		public PDS onSame = null;
		public PDS onHigher = null;
		public PDS onLower = null;
		public int countNone = 0;
		public int countSame = 0;
		public int countHigher = 0;
		public int countLower = 0;
		public int weightNone = 1;
		public int weightSame = 1;
		public int weightHigher = 1;
		public int weightLower = 1;
		
		// name 'n stuff
		public String name = "(no name)";
		public int pattern = PATTERN_NONE;
	}
	
	public static final int SNDPT_BIG_GAP = 0;
	public static final int SNDPT_GAP_HIGHER = 1;
	public static final int SNDPT_GAP_SAME = 2;
	public static final int SNDPT_GAP_LOWER = 3;
	public static final int SNDPT_HIGHER = 4;
	public static final int SNDPT_SAME = 5;
	public static final int SNDPT_SAME_STOP = 51;
	public static final int SNDPT_LOWER = 6;
	public static final int SNDPT_NONE = 7;
	
	public static final int PATTERN_NONE = 0;
	public static final int PATTERN_UP_DOWN_SOLID = 1;
	public static final int PATTERN_UP_DOWN_GAPISH = 2;
	public static final int PATTERN_STRAIGHT_SOLID = 3;
	public static final int PATTERN_STRAIGHT_GAPISH = 4;
	
	//general tuning options
    static final int minStraightSolidLength = 7;
    
    //debug
    static final boolean showStages = false;
	
    private ArrayList<PDS> currentStates;
    private ArrayList<PDS> initialStates;
    private ArrayList<String> patternNames;
	
	
	
	private String name;
	
	public PatternDetector(String name) {
		this.name = name;
		currentStates = new ArrayList<PDS>();
		initialStates = new ArrayList<PDS>();
		patternNames = new ArrayList<String>();
		
		// setting up our state graphs
		// straight solid
		PDS initSS = new PDS();
		PDS confirmSS = new PDS();
		initSS.weightSame = 7; // at least 5 identical tones to make it straight solid
		initSS.onSame = confirmSS; // it is confirmed after 5 tones in aline
		initSS.name = "Initial SS";
		confirmSS.onSame = confirmSS; // every other same tone keeps it confirmed
		confirmSS.onNone = initSS; 
		confirmSS.weightNone = 2; // 2 times none in a row : not straight solid anymore
		confirmSS.name = "Straight Solid! It is confirmed!";
		confirmSS.pattern = PATTERN_STRAIGHT_SOLID;
		
		addPattern(initSS, "Straight Solid");
		
		// straight gapish
		PDS initSG = new PDS();
		PDS pre0SG = new PDS();
		PDS pre1SG = new PDS();
		PDS pre2SG = new PDS();
		PDS conf0SG = new PDS();
		PDS conf1SG = new PDS();
		initSG.weightSame = 3;
		initSG.onSame = pre0SG;
		pre0SG.weightNone = 3;
		pre0SG.weightSame = 4;
		pre0SG.onNone = pre1SG;
		pre1SG.weightNone = 4;
		pre1SG.weightSame = 3;
		pre1SG.onSame = pre2SG;
		pre2SG.weightNone = 3;
		pre2SG.weightSame = 4;
		pre2SG.onNone = conf0SG;
		conf0SG.weightNone = 4;
		conf0SG.weightSame = 3;
		conf0SG.onSame = conf1SG;
		conf0SG.pattern = PATTERN_STRAIGHT_GAPISH;
		conf1SG.weightNone = 3;
		conf1SG.weightSame = 4;
		conf1SG.onNone = conf0SG;
		conf1SG.pattern = PATTERN_STRAIGHT_GAPISH;
		
		addPattern(initSG, "Straight Gapish");
		
	}
	
	public int getPattern() {
		Iterator<PDS> states = currentStates.iterator();
		while (states.hasNext()) {
			PDS state = states.next();
			if (state.pattern != PATTERN_NONE) {
				return state.pattern;
			}
		}
		return PATTERN_NONE;
	}
	
	private void addPattern(PDS initialState, String name) {
		currentStates.add(initialState);
		initialStates.add(initialState);
		patternNames.add(name);
	}
	
	public void printCurrentStates() {
		System.out.print("Current States of Pattern Detector '"+name+"'\n- - -\n");
		Iterator<PDS> states = currentStates.iterator();
		Iterator<String> names = patternNames.iterator();
		while (states.hasNext() && names.hasNext()) {
			System.out.print(" "+names.next()+": "+states.next().name+"\n");
		}
	}
	
	public PDS applyEvent(int type, PDS pds, PDS initial) {
		PDS newState = pds;
		boolean stateSwitch = false;
		
		switch (type) {
		case SNDPT_NONE:
			pds.countNone++;
			break;
		case SNDPT_SAME:
			pds.countSame++;
			break;
		case SNDPT_HIGHER:
			pds.countHigher++;
			break;
		case SNDPT_LOWER:
			pds.countLower++;
			break;
		default:
			pds.countNone++;
			break;
		}
		if (pds.countNone >= pds.weightNone) {
			newState = pds.onNone;
			stateSwitch = true;
		} else if (pds.countSame >= pds.weightSame) {
			newState = pds.onSame;
			stateSwitch = true;
		} else if (pds.countHigher >= pds.weightHigher) {
			newState = pds.onHigher;
			stateSwitch = true;
		} else if (pds.countLower >= pds.weightLower) {
			newState = pds.onLower;
			stateSwitch = true;
		}
		
		if (newState == null) {
			newState = initial;
		}
		
		if (stateSwitch) {
			pds.countHigher = 0;
			pds.countLower = 0;
			pds.countNone = 0;
			pds.countSame = 0;
		}
		
		// inform observers
		if (newState.pattern != PATTERN_NONE) {
			this.setChanged();
			this.notifyObservers(newState.pattern);
		}
		
		return newState;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void pushEvent(int type) {
		// apply the new event to all state graphs
		for (int i = 0; i < currentStates.size(); i++) {
			PDS state = currentStates.get(i);
			PDS initialState = initialStates.get(i);
			currentStates.set(i, applyEvent(type, state, initialState));
		}
	}
	public String stringEvent(int type) {
		String name = "";
		switch(type) {
		case SNDPT_BIG_GAP:
			name = "Big Gap";
			break;
		case SNDPT_GAP_HIGHER:
			name = "Gap Higher";
			break;
		case SNDPT_GAP_SAME:
			name = "Gap Same";
			break;
		case SNDPT_GAP_LOWER:
			name = "Gap Lower";
			break;
		case SNDPT_HIGHER:
			name = "Higher";
			break;
		case SNDPT_SAME:
			name = "Same";
			break;
		case SNDPT_LOWER:
			name = "Lower";
			break;
		case SNDPT_NONE:
			name = "None";
			break;
		case SNDPT_SAME_STOP:
			name = "Same Stop";
			break;
		default:
			name ="unknown: "+type;
			break;
		}
		return name;
	}
}
