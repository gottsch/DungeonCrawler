/**
 * 
 */
package com.someguyssoftware.dungoncrawler.generator.dungeon;

import com.someguyssoftware.dungoncrawler.generator.INode;
import com.someguyssoftware.dungoncrawler.generator.Rectangle2D;

/**
 * @author Mark Gottschling on Sep 15, 2020
 *
 */
public interface IDungeonRoom extends INode {

	
	public Rectangle2D getBox();
	public void setBox(Rectangle2D box);
	
	public boolean isMain();
	public IDungeonRoom setMain(boolean isMain);

	DungeonRoomType getRoomType();
	IDungeonRoom setRoomType(DungeonRoomType roomType);
}
