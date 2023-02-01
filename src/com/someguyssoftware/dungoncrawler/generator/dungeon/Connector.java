package com.someguyssoftware.dungoncrawler.generator.dungeon;

import com.someguyssoftware.dungoncrawler.generator.Coords2D;

public class Connector implements IConnector {

	private Direction2D direction;
	private Coords2D coords;
	
	public Connector() {}
	
	public Connector(Direction2D direction, Coords2D coords) {
		this.direction = direction;
		this.coords = coords;
	}
	
	@Override
	public Direction2D getDirection() {
		return direction;
	}

	@Override
	public void setDirection(Direction2D direction) {
		this.direction = direction;
	}

	@Override
	public Coords2D getCoords() {
		return coords;
	}

	@Override
	public void setCoords(Coords2D coords) {
		this.coords = coords;
	}

}
