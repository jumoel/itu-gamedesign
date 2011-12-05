package game.gui;

import java.util.Observable;

import processing.core.PGraphics;
import game.State;

public abstract class Updateable extends Observable
{
	public abstract void update(int xpos, int ypos, int deltaT);
}
