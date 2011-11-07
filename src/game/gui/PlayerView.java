package game.gui;

import game.Player;
import game.State;
import processing.core.*;

public class PlayerView extends Updateable
{
	private int width;
	private int height;
	private PApplet processing;
	
	private PGraphics buffers[];
	private int currentBuffer;
	private static int NUMBER_OF_BUFFERS = 2;
	
	private int viewNumber;

	private Player ownPlayer;
	private Player otherPlayer;
	
	public PlayerView(int width, int height, PApplet applet, int viewNumber)
	{	
		this.width = width;
		this.height = height;
		this.processing = applet;
		
		buffers = new PGraphics[NUMBER_OF_BUFFERS];
		currentBuffer = 0;
		
		for (int i = 0; i < NUMBER_OF_BUFFERS; i++)
		{
			buffers[i] = processing.createGraphics(width, height, PConstants.P2D);
		}
		
		this.viewNumber = viewNumber;
		
		this.ownPlayer = new Player(processing);
		this.otherPlayer = null;
	}
	
	public void update(State state, int xpos, int ypos, int deltaT)
	{
		PGraphics cb = buffers[currentBuffer];
		cb.beginDraw();
		cb.background(255);
		
		ownPlayer.update(cb);
		
		cb.endDraw();
		
		processing.image(cb, xpos, ypos);
		
		
		currentBuffer = (currentBuffer + 1) % NUMBER_OF_BUFFERS;
	}
}
