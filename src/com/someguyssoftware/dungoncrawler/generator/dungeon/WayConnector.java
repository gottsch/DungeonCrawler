/**
 * 
 */
package com.someguyssoftware.dungoncrawler.generator.dungeon;

import com.someguyssoftware.dungoncrawler.generator.Coords2D;

/**
 * @author Mark Gottschling on Sep 22, 2020
 *
 */
public class WayConnector {
	private Coords2D coords;
	private IRoom room;
	
	public WayConnector(Coords2D coords) {
		this.coords = coords;
	}
	
	public WayConnector(Coords2D coords, IRoom room) {
		this(coords);
		this.room = room;
	}
	
	public WayConnector(WayConnector wc) {
		this(new Coords2D(wc.getCoords()), new Room());
	}
	
	public Coords2D getCoords() {
		return coords;
	}
	public void setCoords(Coords2D coords) {
		this.coords = coords;
	}
	public IRoom getRoom() {
		return room;
	}
	public void setRoom(IRoom room) {
		this.room = room;
	}

	@Override
	public String toString() {
		return "WayConnector [coords=" + ((coords == null) ? "null" : coords) + ", room.id=" + ((room == null) ? "null" : room.getId()) + "]";
	}
}
