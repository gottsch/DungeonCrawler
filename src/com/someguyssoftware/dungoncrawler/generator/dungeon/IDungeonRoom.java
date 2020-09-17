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
public interface IDungeonRoom {

	public Coords2D getOrigin();
	public void setOrigin(Coords2D origin);
	
	public Coords2D getCenter();
	
	public Rectangle2D getBox();
	public void setBox(Rectangle2D box);
	
	public boolean isMain();
	public void setMain(boolean isMain);
//	
//	public int getWidth();
//	public void setWidth(int width);
//	
//	public int getHeight();
//	public void setDepth(int depth);
}
