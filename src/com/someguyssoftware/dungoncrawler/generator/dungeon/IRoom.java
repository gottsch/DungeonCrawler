/**
 * 
 */
package com.someguyssoftware.dungoncrawler.generator.dungeon;

import java.util.List;
import java.util.Map;

import com.someguyssoftware.dungoncrawler.generator.Coords2D;
import com.someguyssoftware.dungoncrawler.generator.INode;
import com.someguyssoftware.dungoncrawler.generator.Rectangle2D;

/**
 * @author Mark Gottschling on Sep 15, 2020
 *
 */
// TODO should probably be totally disconnected from the graphing classes
public interface IRoom extends INode {
	public Rectangle2D getBox();
	public void setBox(Rectangle2D box);
	
	RoomRole getRole();
	IRoom setRole(RoomRole roomRole);
	
	List<Coords2D> getExits();
	void setExits(List<Coords2D> exits);
	
	List<RoomFlag> getFlags();
	void setFlags(List<RoomFlag> flags);
	boolean hasFlag(RoomFlag flag);
	
	boolean hasConnectors();
	Map<Direction2D, List<IConnector>> getConnectors();
}
