package util;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

public class BImage
{
	public static PImage[] cutImageSprite(PApplet processing, PImage original, int tileWidth, int tileHeight)
	{
		int imageHeight = original.height;
		int imageWidth = original.width;

		System.out.println("imageHeight: " + imageHeight);
		System.out.println("imageWidth: " + imageWidth);
		
		int xTiles = imageWidth / tileWidth;
		int yTiles = imageHeight / tileHeight;

		System.out.println("xTiles: " + xTiles);
		System.out.println("yTiles: " + yTiles);
		
		PImage retval[] = new PImage[xTiles * yTiles];
		
		for (int x = 0; x < xTiles; x++)
		{
			for (int y = 0; y < yTiles; y++)
			{
				retval[x*y + x] = processing.createImage(tileWidth, tileHeight, PConstants.RGB);
				retval[x*y + x].copy(original, x * tileWidth, y * tileHeight, tileWidth, tileHeight, 0, 0, tileWidth, tileHeight);
			}
		}
		
		return retval;
	}
}
