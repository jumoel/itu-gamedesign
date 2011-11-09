package game.gui;

import java.text.DecimalFormat;

import game.BunnyHat;
import game.State;
import processing.core.*;

public class RaceIndicator extends Updateable
{
	private int width;
	private int height;
	private PApplet processing;
	private BunnyHat bunnyhat;
	private PFont font;
    private DecimalFormat df;

    private int triangleWidth;
    private int triangleHeight;
	
	public RaceIndicator(int width, int height, BunnyHat applet)
	{
		this.width = width;
		this.height = height;
		this.processing = applet;
		this.bunnyhat = applet;
		
		this.font = processing.loadFont("Monospaced-20.vlw");
		this.df = new DecimalFormat("###.##");
		this.triangleWidth = 10;
		this.triangleHeight = 10;
	}
	
	public void update(State state, int xpos, int ypos, int deltaT)
	{
		processing.noStroke();
		processing.fill(processing.color(255, 0, 0, 255));
		processing.rect(xpos, ypos, width, height);

		processing.fill(255);
		
		int levelOneLength = bunnyhat.view1.getLevelLength();
		int playerOneProgress = bunnyhat.view1.getPlayerPosition();
		double percentageOne = (double)playerOneProgress / (double)levelOneLength;
		
		int xOne = (int)(width * percentageOne);
		
		processing.triangle(xOne, ypos, xOne - triangleWidth / 2, ypos + triangleHeight, xOne + triangleWidth / 2, ypos + triangleHeight);
		

		int levelTwoLength = bunnyhat.view2.getLevelLength();
		int playerTwoProgress = bunnyhat.view2.getPlayerPosition();
		double percentageTwo = (double)playerTwoProgress / (double)levelTwoLength;
		
		int xTwo = (int)(width * percentageTwo);
		
		processing.triangle(xTwo, ypos + height, xTwo - triangleWidth / 2, ypos + height - triangleHeight, xTwo + triangleWidth / 2, ypos + height - triangleHeight);
	}
}
