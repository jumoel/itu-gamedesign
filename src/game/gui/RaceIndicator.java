package game.gui;

import game.BunnyHat;
import game.State;
import processing.core.*;

public class RaceIndicator extends Updateable
{
	private int width;
	private int height;
	private PApplet processing;
	private BunnyHat bunnyhat;
    private int triangleWidth;
    private int triangleHeight;
    private PImage p1, p2;
	
	public RaceIndicator(int width, int height, BunnyHat applet)
	{
		this.width = width;
		this.height = height;
		this.processing = applet;
		this.bunnyhat = applet;
		
		this.triangleWidth = 10;
		this.triangleHeight = 10;
		
		this.p1 = applet.loadImage("animations/objects/BunnyMarker16x20.png");
		this.p2 = applet.loadImage("animations/objects/BirdMarker16x20.png");
	}
	
	public void update(int xpos, int ypos, int deltaT)
	{
		processing.noStroke();
		processing.fill(processing.color(126, 147, 154, 255));
		processing.rect(xpos, ypos, width, height);

		processing.fill(255);
		
		int levelOneLength = bunnyhat.view1.getLevelLength();
		int playerOneProgress = bunnyhat.view1.getPlayerPosition();
		double percentageOne = (double)playerOneProgress / (double)levelOneLength;
		
		int xOne = (int)(width * percentageOne);
		
		processing.image(p1, xOne, ypos);
		//processing.triangle(xOne, ypos, xOne - triangleWidth / 2, ypos + triangleHeight, xOne + triangleWidth / 2, ypos + triangleHeight);
		

		int levelTwoLength = bunnyhat.view2.getLevelLength();
		int playerTwoProgress = bunnyhat.view2.getPlayerPosition();
		double percentageTwo = (double)playerTwoProgress / (double)levelTwoLength;
		
		int xTwo = (int)(width * percentageTwo);
		
		processing.image(p2, xTwo, ypos + height - p2.height);
		//processing.triangle(xTwo, ypos + height, xTwo - triangleWidth / 2, ypos + height - triangleHeight, xTwo + triangleWidth / 2, ypos + height - triangleHeight);
	}
}
