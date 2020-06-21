/**
 * 
 */
package com.someguyssoftware.dungoncrawler.generator.cave;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.someguyssoftware.dungoncrawler.generator.Coords2D;
import com.someguyssoftware.dungoncrawler.generator.ILevelGenerator;

/**
 * @author Mark
 *
 */
public abstract class AbstractCellularAutomataCaveGenerator implements ICellularAutomataCaveGenerator {
	private float chanceToStartSolid = 0.4f;
	private int width = 96;
	private int height = 96;
	private int growthLimit = 4;
	private int decayLimit = 3;
	private int iterations = 2;

	private int smoothing = 3;
	private int fill = 4;
	
	private static final List<Coords2D> DIRECTIONS;	
	private static final List<Coords2D> TRAILING_DIRECTIONS;
	
	static {
		DIRECTIONS = Arrays.asList(new Coords2D[] {
				new Coords2D(0, -1), 	// n
				new Coords2D(0, 1),	// s
				new Coords2D(1,0),		// e
				new Coords2D(-1, 0),	// w
				new Coords2D(1, -1),	// ne
				new Coords2D(-1, -1),	// nw
				new Coords2D(-1, 1),	// se
				new Coords2D(1, 1),		// sw
				new Coords2D(0, 0)		// c
		});
		
		TRAILING_DIRECTIONS = Arrays.asList(new Coords2D[] {
				new Coords2D(0, -1), 	// n
				new Coords2D(-1, 0)	// w
		});
	}
	
	/**
	 * 
	 * @param random
	 * @return
	 */
	protected boolean[][] initMap(Random random) {
		return initMap(width, height, random);
	}
	
	/**
	 * 
	 * @param width
	 * @param height
	 * @param random
	 * @return
	 */
	private boolean[][] initMap(int width, int height, Random random) {
		boolean[][] map = new boolean[width][height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (random.nextDouble() < chanceToStartSolid) {
					map[x][y] = true;
				}
			}
		}
		return map;
	}

	/**
	 * 
	 * @return
	 */
	protected Integer[][] initIDMap() {
		Integer[][] map = new Integer[width][height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
					map[x][y] = -1;
			}
		}
		return map;
	}
	
	/**
	 * 
	 * @param oldMap
	 * @return
	 */
	protected boolean[][] smooth(boolean[][] oldMap) {
		boolean[][] newMap = new boolean[oldMap.length][oldMap[0].length];

		// iterate smoothing n times
		for (int n = 0; n < 5; n++) {
			
			for (int x = 0; x < oldMap.length; x++) {
				for (int y = 0; y < oldMap[0].length; y++) {
					int neighborCount = countNeighbors(oldMap, x, y, false);
					if (oldMap[x][y]) {
						if (neighborCount >= getSmoothing()) {
							newMap[x][y] = false;
						} 
						else {
							newMap[x][y] = true;
						}
					}
					else {
						newMap[x][y] = oldMap[x][y];
					}
				}
			}
		}
		return newMap;
	}
	
	/**
	 * 
	 * @param oldMap
	 * @return
	 */
	protected boolean[][] fill(boolean[][] oldMap) {
		boolean[][] newMap = new boolean[oldMap.length][oldMap[0].length];

		for (int x = 0; x < oldMap.length; x++) {
			for (int y = 0; y < oldMap[0].length; y++) {
				int neighborCount = countNeighbors(oldMap, x, y, true);
				if (!oldMap[x][y]) {
					if (neighborCount >= getFill()) {
						newMap[x][y] = true;
					} 
					else {
						newMap[x][y] = false;
					}
				}
				else {
					newMap[x][y] = oldMap[x][y];
				}
			}
		}
		return newMap;
	}
	
	/*
	 * Returns the number of cells in a ring around (x,y) that are solid.
	 */
	@Deprecated
	protected int countSolidNeighbors(boolean[][] map, int x, int y) {
		int count = 0;
		for (int i = -1; i < 2; i++) {
			for (int j = -1; j < 2; j++) {
				int neighbourX = x + i;
				int neighbourY = y + j;
				// if processing the original index
				if (i == 0 && j == 0) {
					// do nothing
					continue;
				}
				// in case the index we're looking at it off the edge of the map, treat as solid
				else if (neighbourX < 0 || neighbourY < 0 || neighbourX >= map.length || neighbourY >= map[0].length) {
					count = count + 1;
				}
				// else, a normal check of the neighbour
				else if (map[neighbourX][neighbourY]) {
					count = count + 1;
				}
			}
		}
		return count;
	}
	
	/**
	 * 
	 * @param map
	 * @param x
	 * @param y
	 * @param isSolid
	 * @return
	 */
	protected int countNeighbors(boolean[][] map, int x, int y, boolean isSolid) {
		int count = 0;
		for (Coords2D direction : DIRECTIONS) {
			int neighbourX = x + direction.getX();
			int neighbourY = y + direction.getY();
			// if processing the original index
			if (direction.getX() == 0 && direction.getY() == 0) {
				// do nothing
				continue;
			}
			// in case the index we're looking at it off the edge of the map, treat as solid
			else if (neighbourX < 0 || neighbourY < 0 || neighbourX >= map.length || neighbourY >= map[0].length) {
				if (isSolid) {
					count = count + 1;
				}
			}
			// else, a normal check of the neighbour
			else if (map[neighbourX][neighbourY] == isSolid) {
				count = count + 1;
			}
		}
		return count;
	}

	/**
	 * 
	 * @param map
	 * @return
	 */
	public CaveLevel findCaves(boolean[][] map) {
		CaveLevel caveData = new CaveLevel();
		Map<Integer, Cave> caves = new HashMap<>();
		Cave cave = null;
		Integer[][] idMap = new Integer[map.length][map[0].length];
		Integer caveID = 0;
		Integer caveIDCounter = 0;
		boolean isCaveIDGenerated = false;
		Coords2D cell;
		// cycle through all the cells in the map
		for (int x = 0; x < getWidth(); x++) {
			for (int y = 0; y < getHeight(); y++) {
				if (!map[x][y]) {
					// create new cave ID if needed
					if (!isCaveIDGenerated) {
						caveID = ++caveIDCounter;
						System.out.println("New ID " + caveID + " at " + x + ", " + y);
						cave = new Cave(caveID);
						caves.put(caveID, cave);
						isCaveIDGenerated = true;
					}
					// create new cell for coords
					cell = new Coords2D(x, y);
					// add cell to cells in cave
					cave.getCells().add(cell);
					// update the caveID map
					idMap[x][y] = caveID;
					
					if (checkForMerge(cell, idMap)) {
						System.out.println("requires merging for cell @ " + x + ", " + y);
						cave = merge(caveID, idMap[x-1][y], caves, idMap);
						// set the current ID to that of the newly merged cave
						caveID = cave.getId();
						System.out.println("Cave ID of merged cave -> " + caveID);
					}
				}
				else {
					isCaveIDGenerated = false;
				}
			}
		}
		caveData.setCaves(caves);
		caveData.setCellMap(map);
		caveData.setIdMap(idMap);		
		return caveData;
	}
	
	/**
	 * 
	 * @param id1
	 * @param id2
	 * @param caves
	 * @return
	 */
	private Cave merge(Integer id1, Integer id2, Map<Integer, Cave> caves, Integer[][] caveIDs) {
		Cave resultCave = null;
		// get the cave the cell belongs to
		Cave cave = caves.get(id1);
		// get the trailing cave of the cell
		Cave otherCave = caves.get(id2);
//		System.out.println(String.format("[%s]cave1.size -> %s, [%s]cave2.size -> [%s]", cave.getId(),  cave.getCells().size(), otherCave.getId(), otherCave.getCells().size()));
		if (cave.getCells().size() > otherCave.getCells().size()) {
			// cycle through all cells updating the ID array
			for (Coords2D cell : otherCave.getCells()) {
				// update ID array
				caveIDs[cell.getX()][cell.getY()] = cave.getId();
				// add cell to cave
				cave.getCells().add(cell);
			}
			otherCave.getCells().clear();
			resultCave = cave;
		}
		else {
			// cycle through all cells updating the ID array
			for (Coords2D cell : cave.getCells()) {
				caveIDs[cell.getX()][cell.getY()] = otherCave.getId();
				otherCave.getCells().add(cell);
			}
			cave.getCells().clear();
			resultCave = otherCave;
		}
		return resultCave;
	}

	/**
	 * true = merge required
	 * false = no merge required
	 * @param cell
	 * @param caveIDs
	 * @return
	 */
	private boolean checkForMerge(Coords2D cell, Integer[][] caveIDs) {
		if (cell.getX() - 1 >= 0) {
			if (caveIDs[cell.getX() - 1][cell.getY()] != null && 
					caveIDs[cell.getX()][cell.getY()].compareTo(caveIDs[cell.getX() - 1][cell.getY()]) != 0) {
//				System.out.println(String.format("cave[cell].id -> %s, cavel[-1].id -> %s", caveIDs[cell.getX()][cell.getY()], caveIDs[cell.getX()-1][cell.getY()]));
				return true;
			}
		}
		
		return false;
	}

	public float getChanceToStartSolid() {
		return chanceToStartSolid;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	@Override
	public ILevelGenerator withWidth(int width) {
		this.width = width;
		return this;
	}
	
	@Override
	public ILevelGenerator withHeight(int height) {
		this.height = height;
		return this;
	}

	public int getGrowthLimit() {
		return growthLimit;
	}

	public int getDecayLimit() {
		return decayLimit;
	}
	
	public int getIterations() {
		return iterations;
	}
	
	public int getSmoothing() {
		return smoothing;
	}
	
	public int getFill() {
		return fill;
	}
	
	@Override
	public ICellularAutomataCaveGenerator withGrowthLimit(int limit) {
		this.growthLimit = limit;
		return this;
	}
	
	@Override
	public ICellularAutomataCaveGenerator withDecayLimit(int limit) {
		this.decayLimit = limit;
		return this;
	}

	@Override
	public ICellularAutomataCaveGenerator withIterations(int iterations) {
		this.iterations = iterations;
		return this;
	}
	
	public AbstractCellularAutomataCaveGenerator withChanceToStartSolid(float chance) {
		this.chanceToStartSolid = chance;
		return this;
	}
	
	public AbstractCellularAutomataCaveGenerator withSmoothing(int smoothing) {
		this.smoothing = smoothing;
		return this;
	}
	
	public AbstractCellularAutomataCaveGenerator withFill(int fill) {
		this.fill = fill;
		return this;
	}
}
