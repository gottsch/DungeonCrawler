package com.someguyssoftware.dungoncrawler.generator.dungeon;

import java.util.ArrayList;
import java.util.List;

import com.someguyssoftware.dungoncrawler.generator.ILevel;

public class DungeonLevel implements ILevel {
	/*
	 *  map of cells in a level.
	 *  each room consists of length * width of cells
	 */
	private boolean[][] cellMap;
	
	private List<IDungeonRoom> rooms;

	public boolean[][] getCellMap() {
		return cellMap;
	}

	public void setCellMap(boolean[][] cellMap) {
		this.cellMap = cellMap;
	}

	public List<IDungeonRoom> getRooms() {
		if (rooms == null) {
			rooms = new ArrayList<>();
		}
		return rooms;
	}

	public void setRooms(List<IDungeonRoom> rooms) {
		this.rooms = rooms;
	}
	
}
