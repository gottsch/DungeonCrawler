/**
 * 
 */
package com.someguyssoftware.dungoncrawler.generator.dungeon;

import java.util.List;

import com.someguyssoftware.dungoncrawler.generator.Coords2D;
import com.someguyssoftware.dungoncrawler.generator.INode;
import com.someguyssoftware.dungoncrawler.generator.Rectangle2D;

/**
 * @author Mark Gottschling on Sep 15, 2020
 *
 */
public interface IRoom extends INode {
	public Rectangle2D getBox();
	public void setBox(Rectangle2D box);
	
//	public boolean isMain();
//	public IDungeonRoom setMain(boolean isMain);
	
	RoomRole getRole();
	IRoom setRole(RoomRole roomRole);
	
	List<Coords2D> getExits();
	void setExits(List<Coords2D> exits);
	
}
