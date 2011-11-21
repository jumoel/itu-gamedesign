package game.control;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Observable;

import org.yaml.snakeyaml.Yaml;


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
		private boolean confirming = false; public boolean isConfirming() {return confirming;} 
		private ArrayList<PDS> states;
		
		public PDS(ArrayList<PDS> knownStates) {
			states = knownStates;
		}
		
		
		public void configure(LinkedHashMap<String, Object> conf) {
			// name, confirming?
			if (conf.containsKey("name")) {this.name = (String)conf.get("name");}
			if (conf.containsKey("confirmed")) {this.confirming = true;}
			if (conf.containsKey("onSame")) {this.onSame = states.get((Integer)conf.get("onSame"));}
			if (conf.containsKey("weightSame")) {this.weightSame = (Integer)conf.get("weightSame");}
			if (conf.containsKey("onNone")) {this.onNone = states.get((Integer)conf.get("onNone"));}
			if (conf.containsKey("weightNone")) {this.weightNone = (Integer)conf.get("weightNone");}
			if (conf.containsKey("onHigher")) {this.onHigher = states.get((Integer)conf.get("onHigher"));}
			if (conf.containsKey("weightHigher")) {this.weightHigher = (Integer)conf.get("weightHigher");}
			if (conf.containsKey("onLower")) {this.onLower = states.get((Integer)conf.get("onLower"));}
			if (conf.containsKey("weightLower")) {this.weightLower = (Integer)conf.get("weightLower");}
		}
		
		
		
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
	
    
    //debug
    static final boolean showStages = false;
	
    private ArrayList<PDS> currentStates;
    private ArrayList<PDS> initialStates;
    private ArrayList<String> patternNames;
	
	
	
	private String name; // name of this detector
	
	
	
	
	
	public PatternDetector(String name, String confDirName) throws Exception {
		this.name = name;
	
		currentStates = new ArrayList<PDS>();
		initialStates = new ArrayList<PDS>();
		patternNames = new ArrayList<String>();
		
		// reading configurations from our configuration directory
		File confDir = new File(confDirName);
		File[] confFiles = confDir.listFiles();
		Yaml yaml = new Yaml();
		// process all configuration files
		for (int i = 0; i < confFiles.length; i++) {
			// in case it is a valid file and ends on ".pyml" it should be a pattern conf file
			if (confFiles[i].isFile() && confFiles[i].getName().endsWith(".yaml")) {
				// setup defined pattern
				ArrayList<PDS> newStates = new ArrayList<PDS>();
				InputStream is = this.getClass().getClassLoader().getResourceAsStream(confFiles[i].getPath());
				Object possibleLinkedHashMap = yaml.load(is);
				LinkedHashMap<String, Object> values = null;
				if (possibleLinkedHashMap instanceof LinkedHashMap<?, ?>) {
					values = (LinkedHashMap<String, Object>) possibleLinkedHashMap;
				} else {
					throw new Exception("PatternDetector: "+confFiles[i].getName()+ " is not compatible\n");
				}
				
				
				// get all the states
				if (!values.containsKey("states")) {throw new Exception(confFiles[i].getName()+" is missing any states\n");}
				Object possibleArrayList = values.get("states");
				if (!(possibleArrayList instanceof ArrayList<?>)) {throw new Exception(confFiles[i].getName()+" states list faulty\n");}
				ArrayList<Object> stateConfs = (ArrayList<Object>)possibleArrayList;
				for (int e = 0; e < stateConfs.size(); e++) { // create them
					newStates.add(new PDS(newStates));
				}
				// and here a second loop, because first we needed all states to exist,
				// so the states self configuration process can find them for the transitions!
				for (int e = 0; e < stateConfs.size(); e++) { // configure them
					// just tell them to go configure themselves!
					Object possibleLinkedHashMapConfs = stateConfs.get(e);
					if (!(possibleLinkedHashMapConfs instanceof LinkedHashMap<?, ?>)) {throw new Exception(confFiles[i].getName()+" states list entry No."+e+" faulty\n");}
					newStates.get(e).configure((LinkedHashMap<String, Object>)stateConfs.get(e));
				}
				
				addPattern(newStates.get(0), (String)values.get("name"));
			}
		}
		
		
		
	}
	
	public String getPattern() {
		Iterator<PDS> states = currentStates.iterator();
		Iterator<String> names = patternNames.iterator(); 
		while (states.hasNext() && names.hasNext()) {
			PDS state = states.next();
			String name = names.next();
			if (state.isConfirming()) {
				return name;
			}
		}
		return "";
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
			PDS newState = applyEvent(type, state, initialState);
			currentStates.set(i, newState);
			// inform observers
			if (newState.isConfirming() != state.isConfirming()) {
				this.setChanged();
				this.notifyObservers();
			} 
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
