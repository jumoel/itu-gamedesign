package game.gui;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import game.BunnyHat;
import game.Player;
import game.State;
import game.level.Level;
import game.master.GameMaster;
import processing.core.*;

public class PlayerView extends Updateable implements Observer
{
	private enum Horizontal { LEFT, RIGHT, MIDDLE }
	private enum Vertical { TOP, BOTTOM, CENTER }
	
	private int width; public int getWidth() {return width;}
	private int halfwidth;
	private int height;
	private int halfheight;
	private PApplet processing;
	
	private int viewNumber;
	private int xCoordCamera;
	private int xCoordCameraMiddle;
	private int yCoordCamera;
	private int yCoordCameraMiddle;
	
	private int playerPosition;

	private GameMaster gameMaster;
	
	private Player ownPlayer;
	private Player otherPlayer;
	
	private PlayerView otherPlayerView;
	private boolean ownPlayerWon = false;
	private boolean gameOver = false;

	protected boolean drawOwnPlayer = true;
	protected boolean drawOtherPlayer = false;
	
	private PGraphics buffer; 
	private PGraphics colorLayer; 
	protected int colorLayerVisibility = 0;
	private int[] colorLayerColor;
	
	protected double physicsTimeFactor = 1.0;
	
	protected double xbackup;
	protected double ybackup;
	
	protected int cameraOffsetX = 0;
	
	private int levelLength;
	
	private static int timeSlowingFactor;
	
	private Level level; public void setLevel(Level lvl) {level = lvl; ownPlayer.setLevel(lvl);}
	
	// dream switch data
	private boolean switchHappening = false;
	
	// jumping stuff
	private boolean didJump = false;
	
	//dream switch interaction methods
	public Level getLevel() {return level;}
	
	public void switchPrepare() { 
		switchHappening = true;
		ownPlayer.holdAnimation();
		// getting the player y-Offset (distance above ground)
	}
	
	public void switchExecute(Level lvl) {
		setLevel(lvl);
		//TODO: transfer player to same distance above ground
	}
	
	public void switchFinish() {
		ownPlayer.unholdAnimation();
		switchHappening = false;
	}
	
	// show the next best door
	protected void setupDoor() {
		
	}
	
	protected Player getPlayer() {
		return this.ownPlayer;
	}
	
	public void setOtherPlayerView(PlayerView pv) {
		this.otherPlayerView = pv;
		this.ownPlayer.addObserver(pv);
		this.otherPlayer = pv.getPlayer();
	}
	
	public PlayerView(int width, int height, PApplet applet, int viewNumber, String levelPath, GameMaster gameMaster)
	{	
		this.buffer = applet.createGraphics(width, height, PConstants.JAVA2D);
		this.colorLayerColor = new int[3];
		colorLayerColor[0] = BunnyHat.SETTINGS.getValue("gui/colors/tintr");
		colorLayerColor[1] = BunnyHat.SETTINGS.getValue("gui/colors/tintg");
		colorLayerColor[2] = BunnyHat.SETTINGS.getValue("gui/colors/tintb");
		
		
		
		//this.colorLayer.background(colorLayerColor, colorLayerVisibility);
		
		this.width = width;
		this.halfwidth = this.width / 2;
		this.height = height;
		this.halfheight = this.height / 2;
		
		this.processing = applet;
		this.gameMaster = gameMaster;
		
		this.viewNumber = viewNumber;
		
		this.level = new Level(processing, levelPath);
		this.levelLength = level.levelWidth * BunnyHat.TILEDIMENSION;
		
		this.ownPlayer = new Player(processing, viewNumber, this.level);
		this.ownPlayer.addObserver(this);
		this.ownPlayer.addObserver(gameMaster);
		
		this.playerPosition = 0;
		
		this.xCoordCamera = 0;
		this.xCoordCameraMiddle = xCoordCamera + halfwidth;
		
		this.timeSlowingFactor = 1; // normal speed
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

		// a player should always have to press jump again for another jump
		if (didJump && !jumpbutton) didJump = false;  
		// once the game is over, players can not move left / right
		if (gameOver) leftbutton = rightbutton = false;
		
		if (jumpbutton && ownPlayer != null && !didJump)
		{
			ownPlayer.jump();
			this.didJump = true;
			if (BunnyHat.TWIN_JUMP)
			{
				otherPlayer.jump();
			}
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
		buffer.beginDraw();
		buffer.background(255);
		
		//if (switchHappening) deltaT = 0; // freeze time
		deltaT = (int)(deltaT * this.physicsTimeFactor);

		if (ownPlayer == null)
		{
			return;
		}
		
		ownPlayer.isMovingSideways = false;
		
		handleInput(state);
		
		// Update the players physics, etc.
		ownPlayer.update(deltaT);
		
		// Draw the player
		int pxpos, pypos;
		
		if (drawOwnPlayer)
		{
			pxpos = (int) (ownPlayer.xpos * BunnyHat.TILEDIMENSION);
			pypos = (int) (ownPlayer.ypos * BunnyHat.TILEDIMENSION);
		}
		else
		{
			pxpos = (int) (xbackup * BunnyHat.TILEDIMENSION);
			pypos = (int) (ybackup * BunnyHat.TILEDIMENSION);
		}

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
		xCoordCamera = xCoordCameraMiddle - halfwidth + cameraOffsetX;
		yCoordCameraMiddle = pypos;
		yCoordCamera = yCoordCameraMiddle - halfheight;
		
		drawpxpos = halfwidth - cameraOffsetX;
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
			drawpypos = (drawpypos + diff);
			yCoordCamera = maxCameraPosY;
			yCoordCameraMiddle = yCoordCamera + halfheight;
		}
		
		drawpypos = (BunnyHat.PLAYERVIEWTILEHEIGHT * BunnyHat.TILEDIMENSION - drawpypos);
		
		drawLevelGraphics(buffer);
		
		if (drawOwnPlayer)
		{
			drawImage(ownPlayer.getCurrentTexture(), buffer, drawpxpos, drawpypos, Horizontal.MIDDLE, Vertical.BOTTOM);
		}
		
		if (drawOtherPlayer)
		{
			int opxpos = (int) (otherPlayer.xpos * BunnyHat.TILEDIMENSION);
			int opypos = (int) (otherPlayer.ypos * BunnyHat.TILEDIMENSION);
			
			if (opxpos < 0)
			{
				opxpos = 0;
				otherPlayer.cannotMoveLeft = true;
			}
			
			if (opxpos > level.levelWidth * BunnyHat.TILEDIMENSION)
			{
				opxpos = level.levelWidth * BunnyHat.TILEDIMENSION;
				otherPlayer.cannotMoveRight = true;
			}

			if (opypos < 0)
			{
				opypos = 0;
			}
			
			if (opypos + otherPlayer.getCurrentTexture().height > level.levelHeight * BunnyHat.TILEDIMENSION)
			{
				opypos = level.levelHeight * BunnyHat.TILEDIMENSION - otherPlayer.getCurrentTexture().height;
			}
			
			int xdiff = opxpos - pxpos;
			int ydiff = opypos - pypos;
			

			int drawopypos = drawpypos - ydiff;
			int drawopxpos = drawpxpos + xdiff;
			
			if (drawopypos < (yCoordCamera + this.height))drawImage(otherPlayer.getCurrentTexture(), buffer, drawopxpos, drawopypos, Horizontal.MIDDLE, Vertical.BOTTOM);
		}
		
		if (gameOver)
		{
			drawLevelEndScreen(buffer);	
		}
		// put some color on, babe!
		if (colorLayerVisibility > 0) {
			//colorLayer.beginDraw();
			//colorLayer.background(colorLayerColor, colorLayerVisibility);
			//colorLayer.endDraw();
			//buffer.image(colorLayer, 0, 0);
			buffer.noStroke();
			buffer.fill(colorLayerColor[0], colorLayerColor[1], colorLayerColor[2], colorLayerVisibility);
			buffer.rect(0, 0, width, height);
		}
		buffer.endDraw();
		processing.image(buffer, xpos, ypos);
	}
	
	private void drawLevelEndScreen(PGraphics graphics) {
		// draw something cool!
		graphics.fill(0, 0, 0);
		graphics.text((ownPlayerWon?"WIN!!!":"LOSE :/\n\npress q -> main screen"), width / 2, height / 3, 200, 100);
	}
	
	private void drawLevelGraphics(PGraphics graphics)
	{	
		int minimumTileX = xCoordCamera / BunnyHat.TILEDIMENSION;
		if (minimumTileX < 0)
		{
			minimumTileX = 0;
		}
		
		int maximumTileX = (xCoordCamera + this.width) / BunnyHat.TILEDIMENSION;
		if (maximumTileX > level.levelWidth)
		{
			maximumTileX = level.levelWidth;
		}
		
		int minimumTileY = yCoordCamera / BunnyHat.TILEDIMENSION;
		if (minimumTileY < 0)
		{
			minimumTileY = 0;
		}
		
		int maximumTileY = ((yCoordCamera + this.height) / BunnyHat.TILEDIMENSION)+1;
		if (maximumTileY > level.levelHeight)
		{
			maximumTileY = level.levelHeight;
		}
		
		// Counting y from down towards the sky
		for (int reversey = minimumTileY; reversey <= maximumTileY; reversey++)
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
		
		level.collisionDraw(graphics, 0, 0);
		this.ownPlayer.collisionDraw(graphics, 0, 0);
		
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
		//graphics.beginDraw();
		
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
		
		//graphics.endDraw();
	}
	
	@Override
	public void update(Observable arg0, Object arg1)
	{
		if (arg1 instanceof HashMap) {
			HashMap map = (HashMap)arg1;
			if (map.containsKey("IFUCKINGWON") && !gameOver) {
				this.gameOver = true;
				if (((Integer)map.get("IFUCKINGWON"))==this.viewNumber) {
					this.ownPlayerWon = true;
				}
			}
		}
	}
}
