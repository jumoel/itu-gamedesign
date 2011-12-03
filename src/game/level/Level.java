package game.level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import game.BunnyHat;
import game.elements.GameElement;
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
		Obstacle(1),
		SpawnPoint(2),
		FinishLine(3),
		DoorSpawnPoint(4);
		
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
	private int levelData[];
	private int levelDataForeground[];
	private int metaData[];
	private ArrayList creaturesAndObjects;
	
	public String levelName;
	
	public int spawnX;
	public int spawnY;
	
	private String imageFile;
	private int imageWidth;
	private int imageHeight;
	
	private PApplet processing;
	
	
	public Level (PApplet p, String levelName)
	{
		super(p);
		this.processing = p;
		this.levelName = levelName;
		
		loadXML();
		tiles = BImage.cutImageSprite(processing, processing.loadImage("levels/" + imageFile), BunnyHat.TILEDIMENSION, BunnyHat.TILEDIMENSION);
		
		// setup collision level
		this.collisionSetup();
		
		creaturesAndObjects = new ArrayList();
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
	
	public void updateCreaturesAndObjects(int deltaT) {
		Iterator cnos = creaturesAndObjects.iterator();
		while (cnos.hasNext()) {
			GameElement currentCreature = (GameElement)cnos.next();
			currentCreature.update(deltaT);
		}
	}
	
	public void drawCreaturesAndObjects(int x, int y, int width, int height, PGraphics graphics) {
		Iterator cnos = creaturesAndObjects.iterator();
		GameElement toBeDestroyed = null;
		while (cnos.hasNext()) {
			GameElement currentCreature = (GameElement)cnos.next();
			if (currentCreature.destroyed) {
				toBeDestroyed = currentCreature;
			} else {
				//System.out.println("want to draw creature");
				int xcoord = (int)(currentCreature.x() * BunnyHat.TILEDIMENSION - x);
				int ycoord = (int)((height - currentCreature.y() * BunnyHat.TILEDIMENSION));
				//int drawx = (int)(currentCreature.x() * BunnyHat.TILEDIMENSION - x);
				//int drawy = (int)(((height - currentCreature.y()) * BunnyHat.TILEDIMENSION) + y);
				//System.out.println("draw at " + xcoord +":"+ycoord +" with x:"+ x +" ,y:"+y +", pos:"+currentCreature.x()+":"+currentCreature.y());
				//int lvlHpx = levelHeight * BunnyHat.TILEDIMENSION;
				if (xcoord+currentCreature.collisionBoxWidth() > 0 && xcoord <  width 
						&& ycoord + currentCreature.collisionBoxHeight() > 0 && ycoord <  height) {
					//int drawx = (int)(currentCreature.x() * BunnyHat.TILEDIMENSION - x);
					//int drawy = (int)(currentCreature.y() * BunnyHat.TILEDIMENSION - y);
					PImage image = currentCreature.getCurrentTexture();
					
					graphics.image(image, xcoord, ycoord-image.height);
				}
			}
		}
		if (toBeDestroyed != null) {
			creaturesAndObjects.remove(toBeDestroyed);
		}
	}
	
	public void addCreature(GameElement c) {
		creaturesAndObjects.add(c);
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
			
			imageFile = ((String) imgFileXPath.evaluate(doc, XPathConstants.STRING));
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
				
				if (metaData[i] == MetaTiles.SpawnPoint.index())
				{
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