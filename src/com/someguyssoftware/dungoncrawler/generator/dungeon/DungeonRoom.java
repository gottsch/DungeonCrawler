/**
 * 
 */
package com.someguyssoftware.dungoncrawler.generator.dungeon;

import com.someguyssoftware.dungoncrawler.generator.Coords2D;
import com.someguyssoftware.dungoncrawler.generator.Rectangle2D;

/**
 * @author Mark Gottschling on Sep 15, 2020
 *
 */
public class DungeonRoom implements IDungeonRoom {
	private Rectangle2D box;
	private boolean isMain;
//	private int width;
//	private int depth;
//	private Coords2D origin;
//	private Coords2D center;

	public DungeonRoom(int x, int y, int width, int depth) {
		this(new Coords2D(x, y), width, depth);
	}
	public DungeonRoom(Coords2D origin, int width, int depth) {
//		super(origin, width, depth);
		this.box = new Rectangle2D(origin, width, depth);
//		this.origin = origin;
//		this.width = width;
//		this.depth = depth;
//		this.center = new Coords2D((origin.getX() + width)/2, (origin.getY() + depth)/2);
	}
	public Rectangle2D getBox() {
		return box;
	}
	public void setBox(Rectangle2D box) {
		this.box = box;
	}
	
	@Override
	public Coords2D getOrigin() {
		return getBox().getOrigin();
	}

	@Override
	public void setOrigin(Coords2D origin) {
		this.getBox().setOrigin(origin);
	}
	
	@Override
	public Coords2D getCenter() {
		return getBox().getCenter();
	}
	
	@Override
	public boolean isMain() {
		return isMain;
	}
	
	@Override
	public void setMain(boolean isMain) {
		this.isMain = isMain;
	}

//
//	@Override
//	public int getWidth() {
//		return width;
//	}
//
//	@Override
//	public void setWidth(int width) {
//		this.width = width;
//	}
//
//	@Override
//	public int getHeight() {
//		return depth;
//	}
//
//	@Override
//	public void setDepth(int depth) {
//		this.depth = depth;
//	}

}
