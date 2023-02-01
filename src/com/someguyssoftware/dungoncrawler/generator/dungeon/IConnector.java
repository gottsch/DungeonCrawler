package com.someguyssoftware.dungoncrawler.generator.dungeon;

import com.someguyssoftware.dungoncrawler.generator.Coords2D;

/**
 * 
 * @author Mark Gottschling on Jun 23, 2022
 *
 */
public interface IConnector {

	Direction2D getDirection();
	void setDirection(Direction2D direction);	

	Coords2D getCoords();
	void setCoords(Coords2D coords);
}
