package game.level;

import java.awt.geom.Rectangle2D;

import processing.core.PApplet;
import processing.core.PGraphics;
import game.CollisionBox;
import game.CollisionBox.Effects;
import game.Door;
import game.FinishLine;
import game.Obstacle;
import game.Player;

public abstract class CollisionLevel
{
	private PApplet processing;
	
	public int levelWidth;
	public int levelHeight;
	
	protected Player ownPlayer; public void setPlayer(Player pl){this.ownPlayer = pl;}
	
	
	private CollisionBox[] fixedObjects;
	
	public CollisionLevel(PApplet p) {
		this.processing = p;
	}
	
	protected void collisionSetup() {
		fixedObjects = new CollisionBox[levelWidth * levelHeight];
		
		for (int x = 0; x < levelWidth; x++) {
			for (int y = 0; y < levelHeight; y++) {
				CollisionBox cBox = null;
				if (getMetaDataAt(x, y) == Level.MetaTiles.OBSTACLE.index()) {
					cBox = new Obstacle(processing, x, y, 1, 1);
					cBox.setCollisionEffect(Effects.STOP);
				} else if (getMetaDataAt(x, y) == Level.MetaTiles.FINISHLINE.index()) {
					cBox = new FinishLine(processing, x, y, 1, 1);
					cBox.setCollisionEffect(Effects.FINISH);
				}
				setBoxAt(x, y, cBox);
			}
		}
	}
	
	public void setDoorAt(int x, int y, CollisionBox door) {
		for (int xOff = 0; xOff < 2; xOff++) {
			for (int yOff = 0; yOff < 3; yOff++) {
				setBoxAt(x+xOff, y+yOff, door);
			}
		}
	}
	
	public void removeDoorAt(int x, int y) {
		for (int xOff = 0; xOff < 2; xOff++) {
			for (int yOff = 0; yOff < 3; yOff++) {
				setBoxAt(x+xOff, y+yOff, null);
			}
		}
	}
	
	public CollisionBox getBoxAt(int x, int y) {
		int index = levelWidth*y + x;
		if (index < 0 || index >= fixedObjects.length) {
			return null;
		} else {
			return fixedObjects[index];
		}
	}
	
	private void setBoxAt(int x, int y, CollisionBox b) {
		fixedObjects[levelWidth*y + x] = b;
	}
	
	/**
	 * draw all collision boxes
	 */
	public void collisionDraw(PGraphics cb, int xpos, int ypos) {
		for (int i = 0; i < fixedObjects.length; i++) {
			if (fixedObjects[i] != null) {
				fixedObjects[i].collisionDraw(cb, xpos, ypos);
			}
		}
	}
	
	// all that has to implemented by the level
	public abstract int getMetaDataAt(int x, int y);
	
	public abstract CollisionBox getCollider(CollisionBox cBox);
}
