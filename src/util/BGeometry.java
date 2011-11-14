package util;

import processing.core.PImage;
import game.BunnyHat;
import game.Player;

public class BGeometry
{
	public static boolean RectanglesOverlap(double centerX1, double centerY1, int width1, int height1, double centerX2, double centerY2, int width2, int height2)
	{
		double minx1, maxx1, miny1, maxy1,
			   minx2, maxx2, miny2, maxy2;

		double halfwidth1 = width1 / 2.0,
			   halfheight1 = height1 / 2.0;
		
		double halfwidth2 = width2 / 2.0,
			   halfheight2 = height2 / 2.0;

		minx1 = centerX1 - halfwidth1;
		maxx1 = centerX1 + halfwidth1;
		miny1 = centerY1 - halfheight1;
		maxy1 = centerY1 + halfheight1;

		minx2 = centerX2 - halfwidth2;
		maxx2 = centerX2 + halfwidth2;
		miny2 = centerY2 - halfheight2;
		maxy2 = centerY2 + halfheight2;
		
		boolean xoverlap = false,
				yoverlap = false;
		
		if ((minx2 > minx1 && minx2 < maxx1) || (maxx2 > minx1 && maxx2 < maxx1))
		{
			xoverlap = false;
		}
		
		if ((miny2 > miny1 && miny2 < maxy1) || (maxy2 > miny1 && maxy2 < maxy1))
		{
			yoverlap = false;
		}
		
		return xoverlap && yoverlap;
	}
	
	public static boolean PlayerCollidesWithTile(Player player, int x, int y)
	{
		PImage playertexture = player.getCurrentTexture();
		
		int tiledimension = BunnyHat.TILEDIMENSION;
		
		return RectanglesOverlap(
				player.xpos, (int) player.ypos - playertexture.height / 2,
				playertexture.width, playertexture.height,
				
				x + tiledimension / 2.0, y + tiledimension / 2.0,
				tiledimension, tiledimension
				);
	}
}
