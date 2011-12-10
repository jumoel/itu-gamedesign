package game.sound;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import processing.core.PApplet;



public class Stereophone
{
	static File[] sounds;
	static HashMap snds;
	static HashMap soundCooldowns; // contains timestamps for played sounds
	static PApplet processing;
	static Song ourSong;
	
	
	
	public Stereophone(String soundDirName, PApplet processing) {
		File soundDir = new File(soundDirName);
		if (!soundDir.isDirectory()) {System.out.print(soundDirName + " is not a valid directory (just in case you care about having sounds.. .)\n");}
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
		        
				return name.endsWith(".wav");
			}
		};
		sounds = soundDir.listFiles(filter);
		snds = new HashMap();
		for (int i = 0; i < sounds.length; i++) {
			String firstThreeChars = sounds[i].getName().substring(0, 3);
			//System.out.println("file starts with:"+firstThreeChars);
			snds.put(firstThreeChars, sounds[i]);
		}
		soundCooldowns = new HashMap();
		this.processing = processing;
	}
	
	public static void playSong(String filename) {
		ourSong = new Song(filename); 
		ourSong.startPlaying();
	}
	
	public static void stopSong() {
		ourSong.stopPlaying();
	}
	
	public static void playSound(final String number, String identifier, int msCooldown) {
		int currentTime = processing.millis();
		if (soundCooldowns.containsKey(identifier)) {
			if (currentTime - ((Integer)soundCooldowns.get(identifier)) > msCooldown) {
				playSound(number);
				soundCooldowns.put(identifier, currentTime);
			}
		} else {
			playSound(number);
			soundCooldowns.put(identifier, currentTime);
		}
		
	}
	
	public static synchronized void playSound(final String number) {
		if (!snds.containsKey(number)) {return;}
		
	    new Thread(new Runnable() {
	      public void run() {
	        try {
	          Clip clip = AudioSystem.getClip();
	          AudioInputStream inputStream = 
	        		  AudioSystem.getAudioInputStream(
	        				  this.getClass().getClassLoader().getResourceAsStream(((File)snds.get(number)).getPath()));
	          clip.open(inputStream);
	          clip.start(); 
	        } catch (Exception e) {
	          System.err.println(e.getMessage());
	        }
	      }
	    }).start();
	  }

	public void printSounds() {
		if (sounds != null) {
			System.out.print("There are "+sounds.length+" sounds in the library:\n");
			for (int i = 0; i < sounds.length; i++) {
				System.out.print(" "+sounds[i].getPath()+"\n");
			}
		}
	}

}
