package game.level;

import game.BunnyHat;
import processing.core.*;
import util.BImage;
import util.BString;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.*;

import org.w3c.dom.Document;

public class Level
{
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
	
	private PApplet processing;
	
	private PImage tiles[];
	private int metaData[];
	private int levelData[];
	
	public String levelName;
	
	private String imageFile;
	private int imageWidth;
	private int imageHeight;
	
	public int levelWidth;
	public int levelHeight;
	
	public Level (PApplet p, String levelName)
	{
		this.processing = p;
		this.levelName = levelName;
		
		loadXML();
		tiles = BImage.cutImageSprite(processing, processing.loadImage("levels/" + imageFile), BunnyHat.TILEDIMENSION, BunnyHat.TILEDIMENSION);
	}
	
	public int getLevelDataAt(int x, int y)
	{
		int index = levelWidth*y + x;
		
		if (index >= 0 && index < levelData.length)
		{
			return levelData[index];
		}
		else
		{
			return 0;
		}
	}
	
	public int getMetaDataAt(int x, int y)
	{
		int index = levelWidth*y + x;
		
		if (index >= 0 && index < levelData.length)
		{
			return metaData[index];
		}
		else
		{
			return 0;
		}
	}
	
	public PImage getLevelImageAt(int x, int y)
	{
		int leveldata = getLevelDataAt(x, y);
		
		if (leveldata == 0)
		{
			return null;
		}
		else
		{
			return tiles[leveldata - 1];
		}
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
			
			
			int tileWidth  = ((Double) tileWidthXPath.evaluate(doc, XPathConstants.NUMBER)).intValue();
			int tileHeight  = ((Double) tileHeightXPath.evaluate(doc, XPathConstants.NUMBER)).intValue();
			
			// The tile dimensions should match up, otherwise something is wrong.
			if (tileWidth != tileHeight || tileWidth != BunnyHat.TILEDIMENSION)
			{
				System.err.println(
						"The tile dimensions in the level '" + levelName + "' aren't square, " +
						"or doesn't match with the tile dimension in the settings file," +
						"or the level doesn't conform to the quality control guidelines.");
				
				System.exit(-1);
			}
			
			imageFile = ((String) imgFileXPath.evaluate(doc, XPathConstants.STRING));
			imageWidth = ((Double) imgWidthXPath.evaluate(doc, XPathConstants.NUMBER)).intValue();
			imageHeight = ((Double) imgHeightXPath.evaluate(doc, XPathConstants.NUMBER)).intValue();
			levelWidth = ((Double) levelWidthXPath.evaluate(doc, XPathConstants.NUMBER)).intValue();
			levelHeight = ((Double) levelHeightXPath.evaluate(doc, XPathConstants.NUMBER)).intValue();
			
			String levelDataRaw = (String) levelDataXpath.evaluate(doc, XPathConstants.STRING);
			String metaDataRaw = (String) metaDataXpath.evaluate(doc, XPathConstants.STRING);
			
			// Convert the loaded strings to a int arrays
			String metaDataStrings[] = BString.join(metaDataRaw.split("\n"), "").split(",");
			metaData = new int[metaDataStrings.length];
			
			for (int i = 0; i < metaDataStrings.length; i++)
			{
				metaData[i] = Integer.parseInt(metaDataStrings[i]);
			}
			
			String levelDataStrings[] = BString.join(levelDataRaw.split("\n"), "").split(",");
			levelData = new int[metaDataStrings.length];
			
			for (int i = 0; i < levelDataStrings.length; i++)
			{
				levelData[i] = Integer.parseInt(levelDataStrings[i]);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}