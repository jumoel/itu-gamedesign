package level;

import game.BunnyHat;
import processing.core.*;
import util.BImage;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.*;

import org.w3c.dom.Document;

public class Level
{	
	private PApplet processing;
	
	private PImage tiles[];
	private int metaTiles[];
	
	public String levelName;
	
	private String imageFile;
	private int imageWidth;
	private int imageHeight;
	private int levelWidth;
	private int levelHeight;
	
	public Level (PApplet p, String levelName)
	{
		this.processing = p;
		this.levelName = levelName;
		
		loadXML();
		tiles = BImage.cutImageSprite(processing, processing.loadImage("levels/" + imageFile), BunnyHat.TILEDIMENSION, BunnyHat.TILEDIMENSION);
	}
	
	private void loadXML()
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true); // never forget this!
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
			
			
			int tileWidth  = ((Double) tileWidthXPath.evaluate(doc, XPathConstants.NUMBER)).intValue();
			int tileHeight  = ((Double) tileHeightXPath.evaluate(doc, XPathConstants.NUMBER)).intValue();
			
			// The tile dimensions should match up.
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
			
			System.out.println(imageFile + ", " + imageWidth + ", " + imageHeight + ", " + levelWidth + ", " + levelHeight);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}