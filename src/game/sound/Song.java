package game.sound;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

public class Song implements Runnable
{
	
		private Clip clip;
		private boolean playSong = true;
		private Thread ourThread;
		private String songfile;
		
		public Song(String filename) {
			this.songfile = filename;
			
		}
		public void startPlaying() {
			playSong = true;
			ourThread = new Thread(this);
			ourThread.start();
		}
		
		
		public void run() {
			InputStream in;
			AudioStream as = null;
			try
			{
				in = new FileInputStream(songfile);
				as = new AudioStream(in);
			}
			catch (FileNotFoundException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			catch (IOException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}         
			// Use the static class member "player" from class AudioPlayer to play
			// clip.
			if (as != null) {
				AudioPlayer.player.start(as);
			} else {
				return;
			}
			// Similarly, to stop the audio.
			
			
	        while (playSong) {
	        	try
				{
					Thread.currentThread().sleep(200);
				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	        AudioPlayer.player.stop(as); 
	    }
		public void stopPlaying() {
			playSong = false;
		}
	
}
