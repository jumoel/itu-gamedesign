package level;

import game.BunnyHat;
import processing.core.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.*;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import org.w3c.dom.NamedNodeMap;




public class Level
{
	public static int TILEDIMENSION = BunnyHat.SETTINGS.getValue("gui/tiledimension");
	PImage level;
	private PImage tiles[];
	private PApplet processing;
	public String levelName;
	private String imgFile;
	private int imgWidth;
	private int imgHeight;
	private int levelWidth;
	private int levelHeight;
	
	public Level (PApplet p, String levelName){
		this.processing = p;
		this.levelName = levelName;
		loadXML();
	}
	public void loadXML(){
		try {
			 
			File fXmlFile = new File(levelName);
			
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
			XPathExpression levelWidthXPath = xpath.compile("/map[1]//layer[@name='Graphics']/image/@width");
			XPathExpression levelHeightXPath = xpath.compile("/map[1]//layer[@name='Graphics']/image/@height");
			
			//THIS DOES NOT WORK YET ->
			
			
			NodeList tileWidth  = (NodeList) tileWidthXPath.evaluate(doc, XPathConstants.NODESET);
			int tileHeight  = ((Double) tileHeightXPath.evaluate(doc, XPathConstants.NUMBER)).intValue();
			
			System.out.println("tileWidth: " + tileWidth.getLength());

			imgFile = ((String) imgFileXPath.evaluate(doc, XPathConstants.STRING));
			imgWidth = ((Double) imgWidthXPath.evaluate(doc, XPathConstants.NUMBER)).intValue();
			imgHeight = ((Double) imgHeightXPath.evaluate(doc, XPathConstants.NUMBER)).intValue();
			levelWidth = ((Double) levelWidthXPath.evaluate(doc, XPathConstants.NUMBER)).intValue();
			levelHeight = ((Double) levelHeightXPath.evaluate(doc, XPathConstants.NUMBER)).intValue();
			
			
			
		  } catch (Exception e) {
			e.printStackTrace();
		  }
	}
	
	
	
	public void cropImage(){
		level = processing.loadImage(levelName);
		
		for (int y = 0; y < level.height/TILEDIMENSION; y = y + TILEDIMENSION){
			
			for (int x = 0; x < level.width/TILEDIMENSION; x = x + TILEDIMENSION){
				tiles[y*x+x+1] = level.get(x, y, TILEDIMENSION, TILEDIMENSION);
			}
		
		}
	}	
}