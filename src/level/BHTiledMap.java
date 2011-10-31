package level;

import org.newdawn.slick.SlickException;
import org.newdawn.slick.tiled.Layer;
import org.newdawn.slick.tiled.TiledMap;

public class BHTiledMap extends TiledMap
{
	public BHTiledMap(String filename) throws SlickException
	{
		super(filename, false);
	}
	
	public Layer getLayer(int index)
	{
		return (Layer) super.layers.get(index);
	}
}