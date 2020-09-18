package com.someguyssoftware.dungoncrawler.generator.dungeon;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.someguyssoftware.dungoncrawler.generator.ILevel;
import com.someguyssoftware.dungoncrawler.graph.mst.Edge;

public class DungeonLevel implements ILevel {
	private int width;
	private int depth;
	
	/*
	 *  map of cells in a level.
	 *  each room consists of length * width of cells
	 */
	private boolean[][] cellMap;
	
	/*
	 * a list of all the rooms
	 */
	private List<IDungeonRoom> rooms;

	/*
	 * map of all the rooms by id
	 */
	private Map<Integer, IDungeonRoom> roomMap;
	
	// TODO don't really like this part of DungeonLevel as it is a graphing object
	/*
	 * list of all edges as a result of triangulation of rooms
	 */
	private List<Edge> edges;
	
	private List<Edge> paths;
	
	
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

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public void setWidth(int width) {
		this.width = width;
	}

	@Override
	public int getDepth() {
		return depth;
	}

	@Override
	public void setDepth(int depth) {
		this.depth = depth;
	}

	public List<Edge> getEdges() {
		return edges;
	}

	public void setEdges(List<Edge> edges) {
		this.edges = edges;
	}

	public Map<Integer, IDungeonRoom> getRoomMap() {
		return roomMap;
	}

	public void setRoomMap(Map<Integer, IDungeonRoom> roomMap) {
		this.roomMap = roomMap;
	}

	public List<Edge> getPaths() {
		return paths;
	}

	public void setPaths(List<Edge> paths) {
		this.paths = paths;
	}
	
}
