package game;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import game.graphics.AnimationImages;
import game.gui.AmazingSwitchWitch;
import game.gui.PlayerView;
import game.gui.RaceIndicator;
import game.level.Level;
import game.level.Level.DreamStyle;
import game.master.GameMaster;
import game.sound.Stereophone;
import game.control.SoundControl;
import processing.core.*;
import fullscreen.*;

@SuppressWarnings("serial")
public class BunnyHat extends PApplet implements Observer
{
	public static AnimationImages ANIMATION_IMAGES;
	private static HashMap<String, PImage> imageBuffer = new HashMap<String, PImage>();
	
	private class LevelSource {
		String goodLevelFile, badLevelFile;
		ArrayList goodBackgroundImages, badBackgroundImages;
		String previewImage; 
	}
	private ArrayList<LevelSource> levelSources;
	
	
	public static Settings SETTINGS = new Settings();
	
	boolean SHOW_FPS = SETTINGS.getValue("debug/fps");
	int FPS_AVERAGE_SAMPLE_SIZE = 10; // number of last measurements to take into account 
	
	public static int TILEDIMENSION = SETTINGS.getValue("gui/tiledimension");
	
	private static int RACEINDICATORHEIGHT = 2 * TILEDIMENSION;
	public static int PLAYERVIEWTILEHEIGHT = SETTINGS.getValue("gui/playerviewtileheight");
	public static int PLAYERVIEWTILEWIDTH = SETTINGS.getValue("gui/playerviewtilewidth");
	public static int PLAYERVIEWHEIGHT = PLAYERVIEWTILEHEIGHT * TILEDIMENSION;

	private static int VIEW1Y = 0;
	private static int RACEINDICATORY = PLAYERVIEWHEIGHT;
	private static int VIEW2Y = RACEINDICATORY + RACEINDICATORHEIGHT;
	
	private static int LEFT = 0;
	
	private static int WINDOWHEIGHT = RACEINDICATORHEIGHT + 2 * PLAYERVIEWHEIGHT;
	private static int WINDOWWIDTH = PLAYERVIEWTILEWIDTH * TILEDIMENSION;
	
	private Player player1, player2;
	public PlayerView view1, view2, view3;
	private Level goodDream, badDream;
	private RaceIndicator indicator;
	private SoundControl sndCtrl;
	private Stereophone sndOut;
	private static GameMaster gameMaster; public static GameMaster getGameMaster() {return gameMaster;} 
	private AmazingSwitchWitch switcher;

	private State inputState;
	
	private int lastTimestamp;
	private int currentTimestamp;
	private int deltaT;
	public static double physicsTimeFactor = 1.0;
	
	// statistics
	private int lastFpsTime;
	private int fps;
	private int gameSeconds;
	private double fpsAverage;
	
	// not working FullScreen stuff
	private FullScreen fs;
	
	// which screen we are on right now?
	public static enum Screens {GAME, MENU_MAIN, MENU_GAME, MENU_STORY, MENU_CREDITS, MENU_SETUP, INTRO}
	private static Screens currentView;
	private static int currentButton = 0;
	private static int buttonCount = 0;
	private static int currentLevel = 0;
	private boolean pressedLeft = false;
	private boolean pressedRight = false;
	
	// game modes
	private static boolean TWIN_JUMP_SPACEBAR = false;
	public static boolean TWIN_JUMP = false;
	private static boolean SAME_DREAM = false;
	private static boolean SOUND_CONTROL = false;
	private int[] screenResolutionHeight = {768, 768, 900, 900, 1200};
	private int[] screenResolutionWidth = {1024, 1366, 1440, 1600, 1600};
	
	
	//buffer stuff
	private PGraphics buffer;
	
	
	public void setup()
	{
		ANIMATION_IMAGES = new AnimationImages(this);
		
		currentView = Screens.INTRO;
		
		inputState = new State();
		
		sndCtrl = new SoundControl(this);
		
		size(1024, 768);
		
		frameRate(23);
		
		//setup buffers 
		//buffer = createGraphics(this.width, this.height, PConstants.JAVA2D);
		
		//setup statistics
		currentTimestamp = millis();
		deltaT = 0;
		lastFpsTime = 0;
		fps = 0;
		fpsAverage = 0;
		
		
		//setup & run sound input
		sndCtrl = new SoundControl(this);
		sndCtrl.addObserver(this);
		
		
		//setup sound output
		sndOut = new Stereophone("sounds", this);
		sndOut.printSounds();
		
		
		// get levels
		levelSources = new ArrayList<LevelSource>();
		File levelDirRoot = new File("levels/");
		File[] levelDirs = levelDirRoot.listFiles();
		PImage dummy;
		for (int i = 0; i < levelDirs.length; i++) {
			if (levelDirs[i].isDirectory() && levelDirs[i].getName().startsWith("lvl")) {
				File levelDir = new File(levelDirs[i].getPath());
				File[] files = levelDir.listFiles();
				LevelSource lvlSrc = new LevelSource();
				lvlSrc.badBackgroundImages = new ArrayList();
				lvlSrc.goodBackgroundImages = new ArrayList();
				for (int e = 0; e < files.length; e++) {
					String name = files[e].getName();
					if (name.endsWith("ad.tmx")) {
						lvlSrc.badLevelFile = files[e].getPath();
					} else if (name.endsWith("od.tmx")) {
						lvlSrc.goodLevelFile = files[e].getPath();
					} else if (name.endsWith(".png")) {
						String path = files[e].getPath();
						if (name.startsWith("good")) {
							lvlSrc.goodBackgroundImages.add(loadImage(path));
						} else if (name.startsWith("bad")) {
							lvlSrc.badBackgroundImages.add(loadImage(path));
						} else if (name.contentEquals("preview.png")) {
							lvlSrc.previewImage = path;
							dummy = loadImage(path);
						}
					}
				}
				levelSources.add(lvlSrc);
			}
		}
		
		
		//attempt to get a full screen mode - not working - null pointer exception 		
		/*fs = new FullScreen(this);
		if (fs.available()) {
			fs.enter();
		}*/
				
	}

	public void draw()
	{
		/*PGraphics cb = buffer;
		cb.beginDraw();
		cb.background(255);*/
		
		lastTimestamp = currentTimestamp;
		currentTimestamp = millis();
		
		deltaT = currentTimestamp - lastTimestamp;
		if (deltaT==0) deltaT=10;
		if(deltaT > 84) {
			System.out.println("high deltaT: "+ deltaT);
		}
		
		switch (currentView) {
		case INTRO:
			drawIntroScreen();
			break;
		case MENU_MAIN:
			drawMenuMainScreen();
			break;
		case MENU_GAME:
			drawMenuGameScreen();
			break;
		case MENU_STORY:
			drawMenuStoryScreen();
			break;
		case MENU_CREDITS:
			drawMenuCreditsScreen();
			break;
		case GAME:
			deltaT = (int)(deltaT * physicsTimeFactor);
			goodDream.updateGameElements(deltaT);
			badDream.updateGameElements(deltaT);
			player1.update(inputState, deltaT);
			player2.update(inputState, deltaT);
			view1.update(LEFT, VIEW1Y, deltaT);
			view2.update(LEFT, (height-RACEINDICATORHEIGHT)/2+RACEINDICATORHEIGHT, deltaT);
			//view3.update(LEFT + width/2, VIEW2Y, deltaT);
			indicator.update(LEFT, (height-RACEINDICATORHEIGHT)/2, deltaT);
			break;
		}
		
		/*cb.endDraw();
		image(cb, 0, 0);*/
		
		// Print the fps
		if ((currentTimestamp - lastFpsTime) > 1000)
		{
			gameSeconds++;
			fpsAverage += fps;
			if (SHOW_FPS)
			{
				System.out.println("FPS: " + fps + " (Average: "+fpsAverage/gameSeconds+")  " + gameSeconds +"sec.");
			}
			
			lastFpsTime = currentTimestamp;
			fps = 0;
		}
		else
		{
			fps++;
		}
	}

	
	
	private void drawMenuMainScreen()
	{
		background(255);
		fill(0);
		int currentY = height/3;
		int buttonNumber = 0;
		int buttonSpacing = 100;
		
		
		// button new game
		drawButton("menu/main/", "Play", buttonNumber, currentY);
		buttonNumber++; currentY += buttonSpacing;
		
		// button setup
		drawButton("menu/main/", "Setup", buttonNumber, currentY);
		buttonNumber++; currentY += buttonSpacing;
		
		// button story
		drawButton("menu/main/", "Story", buttonNumber, currentY);
		buttonNumber++; currentY += buttonSpacing;
		
		// button credits
		drawButton("menu/main/", "Credits", buttonNumber, currentY);
		buttonNumber++; currentY += buttonSpacing;
		
		// button exit
		drawButton("menu/main/", "Exit", buttonNumber, currentY);
		buttonNumber++; currentY += buttonSpacing;
		
		buttonCount = buttonNumber;
	}
	
	private void drawButton(String path, String name, int number, int y) {
		PImage image = loadImage(path+"button"+name+(currentButton==number?"Selected":"")+".png"); 
		image(image, width/2-image.width/2, y);
	}
	
	private void drawOptionButton(String path, String name, int number, int y, boolean option) {
		PImage image = loadImage(path+"button"+name+(option?"On":"Off")+(currentButton==number?"Selected":"")+".png"); 
		image(image, width/2-image.width/2, y);
	}

	private void drawMenuGameScreen()
	{
		background(255);
		fill(0);
		int currentY = 100;
		int buttonNumber = 0;
		int buttonSpacing = 100;
		
		// button select level
		text(levelSources.get(currentLevel).goodLevelFile, width/2, currentY);
		buttonNumber++; currentY += buttonSpacing;
		
		// button twin jump
		drawOptionButton("menu/game/", "TwinJump", buttonNumber, currentY, this.TWIN_JUMP_SPACEBAR);
		buttonNumber++; currentY += buttonSpacing;
		
		// button run together / alone
		drawOptionButton("menu/game/", "DreamOpposite", buttonNumber, currentY, !this.SAME_DREAM);
		buttonNumber++; currentY += buttonSpacing;
		
		// button sound control
		drawOptionButton("menu/game/", "Sound", buttonNumber, currentY, this.SOUND_CONTROL);
		buttonNumber++; currentY += buttonSpacing;
		
		// button exit
		//drawButton("menu/game/", "Back", buttonNumber, currentY);
		buttonNumber++; currentY += buttonSpacing;
		
		
		
	}
	
	private void drawMenuStoryScreen()
	{
		
	}
	
	private void drawMenuCreditsScreen()
	{
		
	}
	
	private void drawIntroScreen()
	{
		PImage b = loadImage("menu/TitleScreen1024x768.png");
		image(b, 0, 0);
		//text("d(^_^)b amaaazing intro screen - press space to go on!", width/2, height/2);
	}

	public static void main(String args[])
	{
		PApplet.main(new String[]
		{ "--present", "BunnyHat" });
	}

	public void keyPressed()
	{
		switch (this.currentView) {
		case GAME:
			handleKeyGame();
			break;
		case MENU_MAIN:
			handleKeyMenuMain();
			break;
		case MENU_GAME:
			handleKeyMenuGame();
			break;
		case INTRO:
			handleKeyIntro();
			break;
		}
	}
	
	private void handleKeyGame() {
		
		if (key == 'd')
		{
			inputState.put('a', false);
		}
		
		if (key == 'a')
		{
			inputState.put('d', false);
		}
		
		if (key == 'j')
		{
			inputState.put('l', false);
		}
		
		if (key == 'l')
		{
			inputState.put('j', false);
		}
		
		if (key == 'f')
		{
			SHOW_FPS = !SHOW_FPS;
		}
		
		if (key == 'g') {
			frameRate(23);
			currentView = Screens.MENU_GAME;
			gameMaster.stopGame();
			this.deleteAllTheStuff();
		}
		
		if (key == 's' || key == 'k') {
			inputState.put('.', true);
		}
		
		if (TWIN_JUMP_SPACEBAR && (key == 'w' || key == 'i'))
		{
			//nothing for the moment
		}
		else if (TWIN_JUMP_SPACEBAR && key == ' ')
		{
			inputState.put('w', true);
			inputState.put('i', true);
		}
		else
		{
			inputState.put(key, true);
		}
	}
	
	public void deleteAllTheStuff()
	{
		if (view1 != null)
		{
			view1.deleteAllTheStuff();
			view1 = null;
		}
		
		if (view2 != null)
		{
			view2.deleteAllTheStuff();
			view2 = null;
		}
		
		if (view3 != null)
		{
			view3.deleteAllTheStuff();
			view3 = null;
		}
		/*
		if (player1 != null)
		{
			player1.deleteAllTheStuff();
		}
		
		if (player2 != null)
		{
			player2.deleteAllTheStuff();
		}
		*/
	}
	
	private void handleKeyMenuMain() {
		handleKeyMenu();
		
		if (keyCode == ENTER || key == ' ') {
			switch (currentButton) {
				case 0:
					currentView = Screens.MENU_GAME;
					break;
				case 1:
					currentView = Screens.MENU_SETUP;
					break;
				case 2:
					
					break;
				case 3:
					break;
				case 4:
					break;
			}
		}
		
	}
	
	private void handleKeyMenu() {
		if (key == 'w' || key == 'i' || keyCode == UP) {
			currentButton = (currentButton + buttonCount - 1) % buttonCount;
		} else if (key == 's' || key == 'k' || keyCode == DOWN || keyCode == TAB) {
			currentButton = (currentButton + 1) % buttonCount;
		}
		
		if (key == 'a' || key == 'j' || keyCode == LEFT) {
			pressedLeft = true;
		}
		
		if (key == 'd' || key == 'l' || keyCode == RIGHT) {
			pressedRight = true;
		}
	}
	
	private void handleKeyMenuGame() {
		handleKeyMenu();
		switch (currentButton) {
			case 0: // level selection
				if (pressedLeft) {
					currentLevel = (currentLevel + this.levelSources.size() - 1) % this.levelSources.size();
				} else if (pressedRight) {
					currentLevel = (currentLevel + 1) % this.levelSources.size();
				}
				if (keyCode == ENTER || keyCode == ' ') {
					setupGame(currentLevel);
					frameRate(60);
					currentView = Screens.GAME;
				}
				break;
			case 1: // jump style
				if (pressedLeft || pressedRight || keyCode == ENTER || key == ' ') 
					this.TWIN_JUMP_SPACEBAR = !TWIN_JUMP_SPACEBAR;
				break;
			case 2: // together / alone
				if (pressedLeft || pressedRight || keyCode == ENTER || key == ' ') 
					this.SAME_DREAM = !SAME_DREAM;
				break;
			case 3: // snd Ctrl
				if (pressedLeft || pressedRight || keyCode == ENTER || key == ' ') 
					this.SOUND_CONTROL = !SOUND_CONTROL;
				break;
			case 4: // back to main
				if (keyCode==ENTER || key ==' ') currentView = Screens.MENU_MAIN;
				break;
		}
		if (keyCode == ESC) {
			currentView = Screens.MENU_MAIN;
		}
		pressedLeft = pressedRight = false;
	}
	
	private void handleKeyIntro() {
		//if (key == ' ') {
			currentView = Screens.MENU_MAIN;
		//}
	}

	public void keyReleased()
	{
		switch (this.currentView) {
		case GAME:
			if (TWIN_JUMP_SPACEBAR && (key == 'w' || key == 'i')) {
				//nothing for the moment
			} 
			else if (TWIN_JUMP_SPACEBAR && key == ' ') {
				inputState.put('w', false);
				inputState.put('i', false);
			}
			else if (keyCode == 's' || keyCode == 'k') {
				inputState.put('.', false);
			}
			else
			{
				inputState.put(key, false);
			}
			break;
		case MENU_MAIN:
			break;
		}
	}
	
	private void setupGame(int level) {
		//setup game master
		gameMaster = new GameMaster(this);
		
		LevelSource lvlSrc = (LevelSource) levelSources.get(level);
		
		goodDream = new Level(this, lvlSrc.goodLevelFile, DreamStyle.GOOD);
		badDream = new Level(this, lvlSrc.badLevelFile, DreamStyle.BAD);
		
		// throw die: who starts in the good dream?
		double dice = Math.random();
		DreamStyle stylePlayer1 = dice > 0.5 ? DreamStyle.GOOD : DreamStyle.BAD;
		DreamStyle stylePlayer2 = SAME_DREAM ? stylePlayer1 : 
			(stylePlayer1 == DreamStyle.GOOD ? DreamStyle.BAD : DreamStyle.GOOD);
		view1 = new PlayerView(width, (height - RACEINDICATORHEIGHT)/2, this, 1, 
				goodDream, badDream, gameMaster, stylePlayer1,
				lvlSrc.goodBackgroundImages, lvlSrc.badBackgroundImages);
		view2 = new PlayerView(width, (height - RACEINDICATORHEIGHT)/2, this, 2,
				goodDream, badDream, gameMaster, stylePlayer2,
				lvlSrc.goodBackgroundImages, lvlSrc.badBackgroundImages);
		/*view3 = new PlayerView(width/2, PLAYERVIEWHEIGHT, this, 2,
				(String)SETTINGS.getValue("levels/level"+level+"/bad"), gameMaster);*/
		player1 = new Player(this, 1, view1.getLevel());
		player2 = new Player(this, 2, view2.getLevel());
		player1.setTwin(player2);
		player2.setTwin(player1);
		view1.setOwnPlayer(player1);
		view2.setOwnPlayer(player2);
		//view3.setOwnPlayer(player2);
		view1.setOtherPlayerView(view2);
		view2.setOtherPlayerView(view1);
		gameMaster.setTwins(player1, player2);
		goodDream.setTwinDream(badDream);
		badDream.setTwinDream(goodDream);
		goodDream.insertGameElements();
		badDream.insertGameElements();
		
		
		indicator = new RaceIndicator(width, RACEINDICATORHEIGHT, this);

		if (SOUND_CONTROL) sndCtrl.startListening();


		// setup our special workers
		switcher = new AmazingSwitchWitch(view1, view2, this);
		switcher.wakeHer();

		// setup communication
		gameMaster.addObserver(switcher); // listen for level switch message
		switcher.addObserver(gameMaster); // also in a good relationship.. .)
		player1.addObserver(view1);
		player2.addObserver(view2);
		player1.addObserver(gameMaster);
		player2.addObserver(gameMaster);
		switcher.addObserver(view1);
		switcher.addObserver(view2);
		view1.addObserver(switcher);
		view2.addObserver(switcher);
		view1.addObserver(gameMaster);
		view2.addObserver(gameMaster);
		
		//switcher.addObserver(view1);
		//switcher.addObserver(view2);
		
		//setup and run game master
		gameMaster.startGame();
		
	}

	@Override
	public void update(Observable o, Object arg)
	{
		if (arg instanceof HashMap) {
			HashMap map = (HashMap)arg;
			String detector = (String)map.get("detector");
			String pattern = (String)map.get("pattern");
			if (detector.contentEquals("HF")) {
				inputState.put('d', (pattern.contentEquals("Straight Solid")));
			} else {
				inputState.put('l', (pattern.contentEquals("Straight Solid")));
			}
		}
		
	}
	
	@Override
	public PImage loadImage(String filename) {
		if (imageBuffer.containsKey(filename)) {
			//System.out.println(filename);
			return imageBuffer.get(filename);
		} else {
			PImage image = super.loadImage(filename);
			imageBuffer.put(filename, image);
			return image;
		}
	}
	
	
}
