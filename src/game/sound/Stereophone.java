package game.sound;

import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Stereophone
{
	static File[] sounds;
	
	public Stereophone(String soundDirName) {
		File soundDir = new File(soundDirName);
		if (!soundDir.isDirectory()) {System.out.print(soundDirName + " is not a valid directory (just in case you care about having sounds.. .)\n");}
		
	}
	
	public static synchronized void playSound(final int number) {
		if (number >= sounds.length) {return;}
		
	    new Thread(new Runnable() {
	      public void run() {
	        try {
	          Clip clip = AudioSystem.getClip();
	          AudioInputStream inputStream = AudioSystem.getAudioInputStream(this.getClass().getClassLoader().getResourceAsStream(sounds[number].getPath()));
	          clip.open(inputStream);
	          clip.start(); 
	        } catch (Exception e) {
	          System.err.println(e.getMessage());
	        }
	      }
	    }).start();
	  }

	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// TODO Auto-generated method stub
		// put some test routine here
	}

}
