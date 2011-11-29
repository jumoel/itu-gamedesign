package game.level;

import processing.core.PApplet;
import processing.core.PGraphics;
import game.CollisionBox;
import game.CollisionBox.Effects;
import game.Door;
import game.FinishLine;
import game.Obstacle;

public abstract class CollisionLevel
{
	private PApplet processing;
	
	public int levelWidth;
	public int levelHeight;
	
	
	private CollisionBox[] fixedObjects;
	
	public CollisionLevel(PApplet p) {
		this.processing = p;
	}
	
	protected void collisionSetup() {
		fixedObjects = new CollisionBox[levelWidth * levelHeight];
		
		for (int x = 0; x < levelWidth; x++) {
			for (int y = 0; y < levelHeight; y++) {
				CollisionBox cBox = null;
				if (getMetaDataAt(x, y) == Level.MetaTiles.Obstacle.index()) {
					cBox = new Obstacle(processing, x, y, 1, 1);
					cBox.setCollisionEffect(Effects.STOP);
				} else if (getMetaDataAt(x, y) == Level.MetaTiles.FinishLine.index()) {
					cBox = new FinishLine(processing, x, y, 1, 1);
					cBox.setCollisionEffect(Effects.FINISH);
				} else if (getMetaDataAt(x, y) == Level.MetaTiles.DoorSpawnPoint.index()) {
					cBox = new Door(processing, x, y, 1, 1);
					cBox.setCollisionEffect(Effects.STOP);
				}
				setBoxAt(x, y, cBox);
			}
		}
	}
	
	public CollisionBox getBoxAt(int x, int y) {
		int index = levelWidth*y + x;
		if (index < 0 || index > fixedObjects.length) {
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
}
