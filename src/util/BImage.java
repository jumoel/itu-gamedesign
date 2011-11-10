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
		
		int index;
		for (int y = 0; y < yTiles; y++)
		{
			for (int x = 0; x < xTiles; x++)
			{
				index = xTiles*y + x;
				retval[index] = processing.createImage(tileWidth, tileHeight, PConstants.ARGB);
				retval[index].copy(original, x * tileWidth, y * tileHeight, tileWidth, tileHeight, 0, 0, tileWidth, tileHeight);
			}
		}
		
		return retval;
	}
	
	public static PImage mirrorAroundY(PApplet processing, PImage original)
	{
		PImage ret = processing.createImage(original.width, original.height, PConstants.ARGB);
		
		int width = original.width;
		int height = original.height;
		
		for (int x = 0; x < (width / 2) + 1; x++)
		{
			for (int y = 0; y < height; y++)
			{
				int xreverse = (width - x - 1);
				ret.pixels[y * width + x] = original.pixels[y * width + xreverse];
				ret.pixels[y * width + xreverse] = original.pixels[y * width + x];
			}
		}
		
		return ret;
	}
}
