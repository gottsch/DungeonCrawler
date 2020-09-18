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
	private int id;
	private Rectangle2D box;
	private boolean isMain;
	private NodeType nodeType;
	
	/*
	 * 
	 */
	public DungeonRoom(int x, int y, int width, int depth) {
		this(new Coords2D(x, y), width, depth);
	}
	
	/**
	 * 
	 * @param origin
	 * @param width
	 * @param depth
	 */
	public DungeonRoom(Coords2D origin, int width, int depth) {
		this.box = new Rectangle2D(origin, width, depth);
		this.roomType = DungeonRoomType.STANDARD;
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
	public IDungeonRoom setMain(boolean isMain) {
		this.isMain = isMain;
		return this;
	}

	@Override
	public NodeType getType() {
		return nodeType;
	}

	@Override
	public IDungeonRoom setType(NodeType type) {
		this.nodeType = type;
		return this;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

}
