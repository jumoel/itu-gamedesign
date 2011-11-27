package game.gui;

import processing.core.PGraphics;
import game.State;

public abstract class Updateable
{
	public abstract void update(State state, int xpos, int ypos, int deltaT, PGraphics cb);
}
