package game.gui;

import game.BunnyHat;
import game.Player;
import game.State;
import game.level.Level;
import processing.core.*;

public class PlayerView extends Updateable
{
	private enum Horizontal { LEFT, RIGHT, MIDDLE }
	private enum Vertical { TOP, BOTTOM, CENTER }
	
	private int width;
	private int halfwidth;
	private int height;
	private int halfheight;
	private PApplet processing;
	
	private PGraphics buffers[];
	private int currentBuffer;
	private static int NUMBER_OF_BUFFERS = 2;
	
	private int viewNumber;
	private int xCoordCamera;
	private int xCoordCameraMiddle;
	private int yCoordCamera;
	private int yCoordCameraMiddle;
	
	private int playerPosition;

	private Player ownPlayer;
	private Player otherPlayer;
	
	private int levelLength;
	
	private Level level;
	
	// dream switch data
	private boolean switchHappening = false;
	
	//dream switch interaction methods
	public Level getLevel() {return level;}
	public void switchPrepare() { switchHappening = true; }
	public void switchExecute(Level lv) {}
	public void switchFinish() { switchHappening = false; }
	
	
	public PlayerView(int width, int height, PApplet applet, int viewNumber)
	{	
		this.width = width;
		this.halfwidth = this.width / 2;
		this.height = height;
		this.halfheight = this.height / 2;
		
		this.processing = applet;
		
		buffers = new PGraphics[NUMBER_OF_BUFFERS];
		currentBuffer = 0;
		
		for (int i = 0; i < NUMBER_OF_BUFFERS; i++)
		{
			buffers[i] = processing.createGraphics(this.width, this.height, PConstants.JAVA2D);
		}
		
		this.viewNumber = viewNumber;
		
		this.level = new Level(processing, "levels/test_24.tmx");
		this.levelLength = level.levelWidth * BunnyHat.TILEDIMENSION;
		
		this.ownPlayer = new Player(processing, viewNumber, level);
		this.otherPlayer = null;
		
		this.playerPosition = 0;
		
		this.xCoordCamera = 0;
		this.xCoordCameraMiddle = xCoordCamera + halfwidth;
	}
	
	private void handleInput(State state)
	{
		if (switchHappening) return; // no input while a switch is happening
		
		boolean jumpbutton = (viewNumber == 1) ?
				(state.containsKey('w') && state.get('w')) :
				(state.containsKey('i') && state.get('i'));
		
		boolean leftbutton = (viewNumber == 1) ?
				(state.containsKey('a') && state.get('a')) :
				(state.containsKey('j') && state.get('j'));
				
		boolean rightbutton = (viewNumber == 1) ?
				(state.containsKey('d') && state.get('d')) :
				(state.containsKey('l') && state.get('l'));
				
		boolean downbutton = (viewNumber == 1) ?
				(state.containsKey('s') && state.get('s')) :
				(state.containsKey('k') && state.get('k'));

		
		if (jumpbutton && ownPlayer != null)
		{
			ownPlayer.jump();
		}
		
		if (leftbutton && ownPlayer != null)
		{
			ownPlayer.isMovingSideways = true;
			ownPlayer.moveLeft();
		}
		else if (rightbutton && ownPlayer != null)
		{
			ownPlayer.isMovingSideways = true;
			ownPlayer.moveRight();
			
		}
		
		if (downbutton && ownPlayer != null)
		{
			// Use stuff
		}
	}
	
	public void update(State state, int xpos, int ypos, int deltaT)
	{	
		if (switchHappening) return; // yes, nothing happens while we are switching

		if (ownPlayer == null)
		{
			return;
		}
		
		// Just a shortcut :)
		PGraphics cb = buffers[currentBuffer];
		
		
		ownPlayer.isMovingSideways = false;
		
		handleInput(state);
		
		// Update the players physics, etc.
		ownPlayer.update(deltaT);
		
		// Draw a white background
		cb.beginDraw();
		cb.background(255);
		cb.endDraw();
		
		// Draw the player
		int pxpos = (int) (ownPlayer.xpos * BunnyHat.TILEDIMENSION);
		int pypos = (int) (ownPlayer.ypos * BunnyHat.TILEDIMENSION);

		int drawpxpos;
		int drawpypos;
		
		if (pxpos < 0)
		{
			pxpos = 0;
			ownPlayer.cannotMoveLeft = true;
		}
		
		if (pxpos > level.levelWidth * BunnyHat.TILEDIMENSION)
		{
			pxpos = level.levelWidth * BunnyHat.TILEDIMENSION;
			ownPlayer.cannotMoveRight = true;
		}

		if (pypos < 0)
		{
			pypos = 0;
		}
		
		if (pypos + ownPlayer.getCurrentTexture().height > level.levelHeight * BunnyHat.TILEDIMENSION)
		{
			pypos = level.levelHeight * BunnyHat.TILEDIMENSION - ownPlayer.getCurrentTexture().height;
		}
		
		this.playerPosition = pxpos;
		
		// Place the player in the middle
		xCoordCameraMiddle = pxpos;
		xCoordCamera = xCoordCameraMiddle - halfwidth;
		yCoordCameraMiddle = pypos;
		yCoordCamera = yCoordCameraMiddle - halfheight;
		
		drawpxpos = halfwidth;
		drawpypos = halfheight;
		
		if (xCoordCamera < 0)
		{
			int diff = -xCoordCamera;
			drawpxpos = drawpxpos - diff;
			xCoordCamera = 0;
			xCoordCameraMiddle = halfwidth;
		}
		
		int maxCameraPosX = (level.levelWidth - BunnyHat.PLAYERVIEWTILEWIDTH) * BunnyHat.TILEDIMENSION;
		
		if (xCoordCamera > maxCameraPosX)
		{
			int diff = xCoordCamera - maxCameraPosX;
			drawpxpos = drawpxpos + diff;
			xCoordCamera = maxCameraPosX;
			xCoordCameraMiddle = xCoordCamera + halfwidth;
		}
		
		if (yCoordCamera < 0)
		{
			int diff = -yCoordCamera;
			drawpypos = drawpypos - diff;
			yCoordCamera = 0;
			yCoordCameraMiddle = halfheight;
		}
		
		int maxCameraPosY = (level.levelHeight - BunnyHat.PLAYERVIEWTILEHEIGHT) * BunnyHat.TILEDIMENSION;
		if (yCoordCamera > maxCameraPosY)
		{
			int diff = yCoordCamera - maxCameraPosY;
			drawpypos = drawpypos + diff;
			yCoordCamera = maxCameraPosY;
			yCoordCameraMiddle = yCoordCamera + halfheight;
		}
		
		drawpypos = BunnyHat.PLAYERVIEWTILEHEIGHT * BunnyHat.TILEDIMENSION - drawpypos;
		
		drawLevelGraphics(cb);
		
		drawImage(ownPlayer.getCurrentTexture(), cb, drawpxpos, drawpypos, Horizontal.MIDDLE, Vertical.BOTTOM);
		
		// Draw the image to the surface
		processing.image(cb, xpos, ypos);
		
		// Swap the buffers
		currentBuffer = (currentBuffer + 1) % NUMBER_OF_BUFFERS;
	}
	
	private void drawLevelGraphics(PGraphics graphics)
	{
		graphics.beginDraw();
		
		int minimumTileX = xCoordCamera / BunnyHat.TILEDIMENSION;
		if (minimumTileX < 0)
		{
			minimumTileX = 0;
		}
		
		int maximumTileX = (xCoordCamera + graphics.width) / BunnyHat.TILEDIMENSION;
		if (maximumTileX > level.levelWidth)
		{
			maximumTileX = level.levelWidth;
		}
		
		int minimumTileY = yCoordCamera / BunnyHat.TILEDIMENSION;
		if (minimumTileY < 0)
		{
			minimumTileY = 0;
		}
		
		int maximumTileY = (yCoordCamera + graphics.height) / BunnyHat.TILEDIMENSION;
		if (maximumTileY > level.levelHeight)
		{
			maximumTileY = level.levelHeight;
		}
		
		// Counting y from down towards the sky
		for (int reversey = minimumTileY; reversey < maximumTileY; reversey++)
		{
			// Counting x from left towards right
			for (int x = minimumTileX; x <= maximumTileX; x++)
			{
				int y = level.levelHeight - reversey;
				PImage tile = level.getLevelImageAt(x, y);
				
				int xcoord = x * BunnyHat.TILEDIMENSION - xCoordCamera;
				int ycoord = (BunnyHat.PLAYERVIEWTILEHEIGHT - reversey) * BunnyHat.TILEDIMENSION + yCoordCamera;
				
				if (tile != null)
				{
					graphics.image(tile, xcoord, ycoord);
				}
			}
		}
		
		/*
		// Debug below
		graphics.line(minimumTileCoord, 0, minimumTileCoord, graphics.height);
		graphics.line(maximumTileCoord, 0, maximumTileCoord, graphics.height);
		
		// Even more debug below
		int nextTile = minimumTile + 1;
		int nextTileCoord = nextTile * BunnyHat.TILEDIMENSION;
		int cameraNextTileCoord = nextTileCoord - xCoordCamera;
		
		int cameraSecondTileCoord = cameraNextTileCoord + BunnyHat.TILEDIMENSION;
		
		graphics.textFont(font);
		graphics.fill(0);
		graphics.text(minimumTile, 0, 20);

		graphics.line(cameraNextTileCoord, 0, cameraNextTileCoord, graphics.height);
		graphics.line(cameraSecondTileCoord, 0, cameraSecondTileCoord, graphics.height);
		*/
		
		graphics.endDraw();
	}
	
	public int getPlayerPosition()
	{
		return playerPosition;
	}
	
	public int getLevelLength()
	{
		return levelLength;
	}
	
	private void drawImage(PImage image, PGraphics graphics, int xpos, int ypos, Horizontal horizontal, Vertical vertical)
	{
		graphics.beginDraw();
		
		int tx = xpos, ty = ypos;
		
		switch (horizontal)
		{
			case LEFT:
				tx = xpos;
				break;
			case RIGHT:
				tx = xpos - image.width;
				break;
			case MIDDLE:
				tx = xpos - image.width / 2;
				break;
		}
		
		switch (vertical)
		{
			case TOP:
				ty = ypos;
				break;
			case BOTTOM:
				ty = ypos - image.height;
				break;
			case CENTER:
				ty = ypos - image.height / 2;
				break;
		}
		
		graphics.image(image, tx, ty);
		graphics.fill(processing.color(255, 0, 0, 255));
		graphics.ellipse(xpos, ypos, 4, 4);
		
		graphics.endDraw();
	}
}
