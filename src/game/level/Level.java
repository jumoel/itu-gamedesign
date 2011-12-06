package game.level;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import game.BunnyHat;
import game.CollisionBox;
import game.Player;
import game.elements.BadSheep;
import game.elements.BubbleGunGum;
import game.elements.GameElement;
import game.elements.GoodSheep;
import processing.core.*;
import util.BImage;
import util.BString;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.*;

import org.w3c.dom.Document;

public class Level extends CollisionLevel
{
	public enum Layer {FOREGROUND, BACKGROUND}
	public enum DreamStyle {GOOD, BAD}
	
	public enum MetaTiles
	{
		OBSTACLE(1),
		SPAWNPOINT(2),
		FINISHLINE(3),
		DOORSPAWNPOINT(4),
		CROSSINGSHEEP(5);
		
		private final int index;
		
		MetaTiles(int index)
		{
			this.index = index;
		}
		
		public int index()
		{
			return this.index;
		}
	}
	
	
	
	private PImage tiles[];
	public PImage bigPicture;
	private int levelData[];
	private int levelDataForeground[];
	private int metaData[];
	private ArrayList<GameElement> gameElements;
	
	public String levelName;
	private String levelPath;
	public DreamStyle dream;
	private Level twinDream;
	
	public int spawnX;
	public int spawnY;
	
	private String imageFile;
	private int imageWidth;
	private int imageHeight;
	
	private PApplet processing;
	
	
	public Level (PApplet p, String levelName, DreamStyle style)
	{
		super(p);
		this.processing = p;
		this.levelName = levelName;
		this.levelPath = levelName.substring(0, levelName.lastIndexOf(File.separatorChar));
		this.dream = style;
		
		loadXML();
		tiles = BImage.cutImageSprite(processing, processing.loadImage(imageFile), BunnyHat.TILEDIMENSION, BunnyHat.TILEDIMENSION);
		
		// setup collision level
		this.collisionSetup();
		
		gameElements = new ArrayList<GameElement>();
		//this.setupTheBigPiture();
	}
	
	// creating a complete picture of our level
	public void setupTheBigPiture() {
		int dim = BunnyHat.TILEDIMENSION;
		int width = this.levelWidth * dim;
		int height = this.levelHeight * dim;
		
		bigPicture = processing.createImage(width, height, processing.ARGB);
		//PGraphics bigPic = processing.createGraphics(width, height, PConstants.JAVA2D);
		
		for (int x = 0; x < levelWidth; x++) {
			for (int y = 0; y < levelHeight; y++) {
				//System.out.println(x+":"+y);
				PImage img = this.getLevelImageAt(x, y, Layer.BACKGROUND);
				if (img != null) {
					bigPicture.copy(img, 0, 0, dim, dim, 
							x * dim, (y-1) * dim, dim, dim);
				}
			}
		}
	}
	
	public void setTwinDream(Level dream) {
		this.twinDream = dream;
	}
	
	public void insertGameElements() {
		for (int x = 0; x < levelWidth; x++) {
			for (int y = 0; y < levelHeight; y++) {
				//if (this.level.getMetaDataAt(x, y) > 1) {System.out.println(this.level.getMetaDataAt(x, y));}
				/*
				 * TODO: Sam should fix his leaky sheep :)
				 * */
				if (this.getMetaDataAt(x, y) == Level.MetaTiles.CROSSINGSHEEP.index()) {
					GoodSheep goodSheep = new GoodSheep(x, y, 3, 3, processing);
					BadSheep badSheep = new BadSheep(x, y, 3, 3, processing, goodSheep);
					if (dream == DreamStyle.GOOD) {
						twinDream.addElement(badSheep);
						this.addElement(goodSheep);
					} else {
						//twinDream.addElement(goodSheep);
						//this.addElement(badSheep);
					}
				}
				/**/
			}
		}
	}
	
	public int getLevelDataAt(int x, int y)
	{
		return getLevelDataAt(x, y, Layer.BACKGROUND);
	}
	
	public int getLevelDataAt(int x, int y, Layer l) {
		int index = levelWidth*y + x;
		int tileNo = 0;
		
		if (index >= 0 && index < levelData.length)
		{
			switch (l) {
			case FOREGROUND:
				tileNo = levelDataForeground[index];
				break;
			case BACKGROUND:
				tileNo = levelData[index];
				break;
			}
		}
		
		return tileNo;
	}
	
	public int getMetaDataAt(int x, int y)
	{
		int realy = levelHeight - y;
		int index = levelWidth*realy + x;
		
		if (index >= 0 && index < levelData.length)
		{
			return metaData[index];
		}
		else
		{
			return 0;
		}
	}
	
	public PImage getLevelImageAt(int x, int y, Layer l)
	{
		int leveldata = getLevelDataAt(x, y, l);
		
		if (leveldata == 0)
		{
			return null;
		}
		else
		{
			return tiles[leveldata - 1];
		}
	}
	
	public CollisionBox getCollider(CollisionBox box) {
		CollisionBox ret = null;
		Iterator cnos = gameElements.iterator();
		while (cnos.hasNext()) {
			CollisionBox currentCBox = (CollisionBox)cnos.next();
			if (currentCBox != box && box.getCBox().intersects(currentCBox.getCBox())) {
				return currentCBox;
			}
		}
		/*if (box.getGameElement() != ownPlayer  && ownPlayer.getCBox().intersects(box.getCBox())) {
			return ownPlayer;
		}*/
		return ret;
	}
	
	public void updateGameElements(int deltaT) {
		Iterator<GameElement> cnos = gameElements.iterator();
		while (cnos.hasNext()) {
			GameElement currentCreature = cnos.next();
			if (currentCreature.updateMe) {
				currentCreature.update(deltaT);
			}
		}
		
		// sort once, get elements with high zIndex further to the front
		// sorting once is enough, to get e.g. one player to the front
		for (int i = 1; i < gameElements.size(); i++) {
			if (gameElements.get(i-1).zIndex > gameElements.get(i).zIndex) {
				GameElement element = gameElements.get(i);
				gameElements.set(i, gameElements.get(i-1));
				gameElements.set(i-1, element);
			}
		}
	}
	
	public void drawCreaturesAndObjects(int x, int y, int width, int height, PGraphics graphics) {
		Iterator<GameElement> cnos = gameElements.iterator();
		GameElement toBeDestroyed = null;
		while (cnos.hasNext()) {
			GameElement currentCreature = cnos.next();
			if (currentCreature.destroyed) {
				toBeDestroyed = currentCreature;
			} else if (currentCreature.drawMe) {
				
				int xcoord = (int)(currentCreature.x() * BunnyHat.TILEDIMENSION - x);
				int ycoord = (int)((height - currentCreature.y() * BunnyHat.TILEDIMENSION));
				
				if (xcoord+currentCreature.collisionBoxWidth()+200 > 0 && xcoord <  width 
						&& ycoord - currentCreature.collisionBoxHeight() > 0 && ycoord <  height) {
					
					PImage image = currentCreature.getCurrentTexture();
					
					graphics.image(image, xcoord, ycoord-image.height);
				}
			}
		}
		if (toBeDestroyed != null) {
			gameElements.remove(toBeDestroyed);
		}
	}
	
	public void addElement(GameElement e) {
		e.setLevel(this);
		gameElements.add(e);
	}
	
	public void removeElement(GameElement e) {
		gameElements.remove(e);
	}
	
	
	private void loadXML()
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true); // never forget this!
											 // Ok!

			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(levelName);
			
			XPathFactory factory2 = XPathFactory.newInstance();
			XPath xpath = factory2.newXPath();
			XPathExpression tileWidthXPath = xpath.compile("/map[1]//tileset[@name='Graphics']/@tilewidth");
			XPathExpression tileHeightXPath = xpath.compile("/map[1]//tileset[@name='Graphics']/@tileheight");
			XPathExpression imgFileXPath = xpath.compile("/map[1]//tileset[@name='Graphics']/image/@source");
			XPathExpression imgWidthXPath = xpath.compile("/map[1]//tileset[@name='Graphics']/image/@width");
			XPathExpression imgHeightXPath = xpath.compile("/map[1]//tileset[@name='Graphics']/image/@height");
			XPathExpression levelWidthXPath = xpath.compile("/map[1]//layer[@name='Graphics']/@width");
			XPathExpression levelHeightXPath = xpath.compile("/map[1]//layer[@name='Graphics']/@height");
			XPathExpression levelDataXpath = xpath.compile("/map[1]//layer[@name='Graphics']/data/text()");
			XPathExpression metaDataXpath = xpath.compile("/map[1]//layer[@name='Meta']/data/text()");
			XPathExpression levelDataForegroundXpath = xpath.compile("/map[1]//layer[@name='Foreground']/data/text()");
			XPathExpression metaDataFirstGid = xpath.compile("/map[1]//tileset[@name='Meta']/@firstgid");
			
			
			int tileWidth  = ((Double) tileWidthXPath.evaluate(doc, XPathConstants.NUMBER)).intValue();
			int tileHeight  = ((Double) tileHeightXPath.evaluate(doc, XPathConstants.NUMBER)).intValue();
			
			// The tile dimensions should match up, otherwise something is wrong.
			if (tileWidth != tileHeight || tileWidth != BunnyHat.TILEDIMENSION)
			{
				System.err.println(
						"The tile dimensions in the level '" + levelName + "' aren't square (check: "+tileWidth+" by "+tileHeight+"),\n " +
						"or doesn't match with the tile dimension in the settings file (check: "+BunnyHat.TILEDIMENSION+"),\n" +
						"or the level doesn't conform to the quality control guidelines.");
				
				System.exit(-1);
			}
			
			imageFile = levelPath+File.separator+((String) imgFileXPath.evaluate(doc, XPathConstants.STRING));
			imageWidth = ((Double) imgWidthXPath.evaluate(doc, XPathConstants.NUMBER)).intValue();
			imageHeight = ((Double) imgHeightXPath.evaluate(doc, XPathConstants.NUMBER)).intValue();
			levelWidth = ((Double) levelWidthXPath.evaluate(doc, XPathConstants.NUMBER)).intValue();
			levelHeight = ((Double) levelHeightXPath.evaluate(doc, XPathConstants.NUMBER)).intValue();
			int metaFirstGid = ((Double) metaDataFirstGid.evaluate(doc, XPathConstants.NUMBER)).intValue();
			
			String levelDataRaw = (String) levelDataXpath.evaluate(doc, XPathConstants.STRING);
			String metaDataRaw = (String) metaDataXpath.evaluate(doc, XPathConstants.STRING);
			String levelDataForegroundRaw = (String) levelDataForegroundXpath.evaluate(doc, XPathConstants.STRING);
			
			// Convert the loaded strings to a int arrays
			String metaDataStrings[] = BString.join(metaDataRaw.split("\n"), "").split(",");
			metaData = new int[metaDataStrings.length];
			
			for (int i = 0; i < metaDataStrings.length; i++)
			{
				metaData[i] = Integer.parseInt(metaDataStrings[i]);
				
				if (metaData[i] != 0)
				{
					metaData[i] = metaData[i] - metaFirstGid + 1;
				}
				
				if (metaData[i] == MetaTiles.SPAWNPOINT.index())
				{
					System.out.println("Found it");
					this.spawnX = i % levelWidth;
					this.spawnY = i / levelWidth;
				}
			}
			
			String levelDataStrings[] = BString.join(levelDataRaw.split("\n"), "").split(",");
			levelData = new int[levelDataStrings.length];
			
			for (int i = 0; i < levelDataStrings.length; i++)
			{
				levelData[i] = Integer.parseInt(levelDataStrings[i]);
			}
			
			String levelDataForegroundStrings[] = BString.join(levelDataForegroundRaw.split("\n"), "").split(",");
			levelDataForeground = new int[levelDataForegroundStrings.length];
			
			for (int i = 0; i < levelDataForegroundStrings.length; i++)
			{
				levelDataForeground[i] = Integer.parseInt(levelDataForegroundStrings[i]);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}