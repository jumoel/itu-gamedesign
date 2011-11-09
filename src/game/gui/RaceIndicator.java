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
	
	public RaceIndicator(int width, int height, BunnyHat applet)
	{
		this.width = width;
		this.height = height;
		this.processing = applet;
		this.bunnyhat = applet;
		
		this.font = processing.loadFont("Monospaced-20.vlw");
		this.df = new DecimalFormat("###.##");
	}
	
	public void update(State state, int xpos, int ypos, int deltaT)
	{
		processing.noStroke();
		processing.fill(processing.color(255, 0, 0, 255));
		processing.rect(xpos, ypos, width, height);

		int levelOneLength = bunnyhat.view1.getLevelLength();
		int levelTwoLength = bunnyhat.view2.getLevelLength();

		int playerOneProgress = bunnyhat.view1.getPlayerPosition();
		int playerTwoProgress = bunnyhat.view2.getPlayerPosition();

		double percentageOne = (double)playerOneProgress / (double)levelOneLength * 100.0;
		double percentageTwo = (double)playerTwoProgress / (double)levelTwoLength * 100.0;
		
		processing.fill(0);
		processing.text("Player 1: " + df.format(percentageOne) + "% -- Player 2: " + df.format(percentageTwo) + "%", xpos, ypos + height / 2);
	}
}
