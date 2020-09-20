/**
 * 
 */
package com.someguyssoftware.dungoncrawler.generator.dungeon;

import com.someguyssoftware.dungoncrawler.generator.Coords2D;
import com.someguyssoftware.dungoncrawler.generator.INode;
import com.someguyssoftware.dungoncrawler.generator.NodeType;
import com.someguyssoftware.dungoncrawler.generator.Rectangle2D;

/**
 * @author Mark Gottschling on Sep 15, 2020
 *
 */
// TODO extends AbstractGraphNode
public class DungeonRoom implements IDungeonRoom {
	private int id;
	private Rectangle2D box;
	private int maxDegrees;
	private NodeType nodeType;
	private RoomRole roomRole;
	private boolean isMain;
	
	/**
	 * Empty constructor
	 */
	public DungeonRoom() {
		// ensure all required fields (ex box) are generated lazily in getters if allowing empty constructors
	}
	
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
		this.maxDegrees = 3;
		this.nodeType = NodeType.STANDARD;
	}
	
	public Rectangle2D getBox() {
		if (box == null) {
			box = new Rectangle2D(0, 0, 0, 0);
		}
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
	public int getMinX() {
		return this.getBox().getMinX();
	}
	
	@Override
	public int getMaxX() {
		return this.getBox().getMaxX();
	}
	
	@Override
	public int getMinY() {
		return this.getBox().getMinY();
	}
	
	@Override
	public int getMaxY() {
		return this.getBox().getMaxY();
	}
	
//	@Override
//	public boolean isMain() {
//		return isMain;
//	}
	
//	@Override
//	public IDungeonRoom setMain(boolean isMain) {
//		this.isMain = isMain;
//		return this;
//	}

	@Override
	public NodeType getType() {
		return nodeType;
	}

	@Override
	public INode setType(NodeType type) {
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

	@Override
	public int getMaxDegrees() {
		return maxDegrees;
	}

	@Override
	public void setMaxDegrees(int degrees) {
		this.maxDegrees = degrees;
	}

	@Override
	public RoomRole getRole() {
		return roomRole;
	}

	@Override
	public IDungeonRoom setRole(RoomRole roomRole) {
		this.roomRole = roomRole;
		return this;
	}

	@Override
	public String toString() {
		return "DungeonRoom [id=" + id + ", box=" + box + "]";
	}

}
