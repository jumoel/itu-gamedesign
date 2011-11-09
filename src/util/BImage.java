package util;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

public class BImage
{
	/***
	 * Cuts an image into multiple sprites.
	 * The function will not save 'half empty' tiles, so preferably,
	 * the dimensions of the original image should be divisible by the
	 * tileWidth and the tileHeight.
	 * 
	 * @param processing The processing instance doing the hard lifting
	 * @param original The original image
	 * @param tileWidth The width of the tiles
	 * @param tileHeight The height of the tiles
	 * @return
	 */
	public static PImage[] cutImageSprite(PApplet processing, PImage original, int tileWidth, int tileHeight)
	{
		int imageHeight = original.height;
		int imageWidth = original.width;

		int xTiles = imageWidth / tileWidth;
		int yTiles = imageHeight / tileHeight;
		
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
