package level;

import game.BunnyHat;
import processing.core.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;

public class Level
{
	public static int TILEDIMENSION = BunnyHat.SETTINGS.getValue("gui/tiledimension");
	PImage level;
	PImage tiles[];
	private PApplet processing;
	public String levelName;
	
	public Level (PApplet p, String levelName){
		this.processing = p;
		this.levelName = levelName; 
	}
	//copy paste TODO!!!
	public void loadXML(){
		try {
			 
			File fXmlFile = new File(levelName);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			
			
		  } catch (Exception e) {
			e.printStackTrace();
		  }
	}
	
	private static String getTagValue(String sTag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
	 
	        Node nValue = (Node) nlList.item(0);
	 
		return nValue.getNodeValue();
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