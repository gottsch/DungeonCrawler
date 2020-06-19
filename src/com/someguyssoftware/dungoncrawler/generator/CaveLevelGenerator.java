/**
 * 
 */
package com.someguyssoftware.dungoncrawler.generator;

import java.util.Random;

/**
 * @author Mark
 *
 */
public class CaveLevelGenerator implements ILevelGenerator {
	private float chanceToStartAlive = 0.4f;
	private int growthLimit = 4;
	private int decayLimit =3;

	/**
	 * 
	 * @param width
	 * @param height
	 * @param random
	 * @return
	 */
	public boolean[][] initMap(int width, int height, Random random) {
		boolean[][] map = new boolean[width][height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (random.nextDouble() < chanceToStartAlive) {
					map[x][y] = true;
				}
			}
		}
		return map;
	}

	/**
	 * 
	 * @param oldMap
	 * @return
	 */
	public boolean[][] doSimulationStep(boolean[][] oldMap) {
		boolean[][] newMap = new boolean[oldMap.length][oldMap[0].length];
		// Loop over each row and column of the map
		for (int x = 0; x < oldMap.length; x++) {
			for (int y = 0; y < oldMap[0].length; y++) {
				int nbs = countAliveNeighbours(oldMap, x, y);
				// The new value is based on our simulation rules
				// First, if a cell is alive (solid | non-cave) but has too few neighbours, kill it.
				if (oldMap[x][y]) {
					if (nbs < decayLimit) {
						newMap[x][y] = false;
					} else {
						newMap[x][y] = true;
					}
				} // Otherwise, if the cell is dead now, check if it has the right number of
					// neighbours to be 'born'
				else {
					if (nbs > growthLimit) {
						newMap[x][y] = true;
					} else {
						newMap[x][y] = false;
					}
				}
			}
		}
		return newMap;
	}

	/*
	 * Returns the number of cells in a ring around (x,y) that are alive.
	 */
	public int countAliveNeighbours(boolean[][] map, int x, int y) {
		int count = 0;
		for (int i = -1; i < 2; i++) {
			for (int j = -1; j < 2; j++) {
				int neighbourX = x + i;
				int neighbourY = y + j;
				// If we're looking at the middle point
				if (i == 0 && j == 0) {
					// Do nothing, we don't want to add ourselves in!
					continue;
				}
				// In case the index we're looking at it off the edge of the map
				else if (neighbourX < 0 || neighbourY < 0 || neighbourX >= map.length || neighbourY >= map[0].length) {
					count = count + 1;
				}
				// Otherwise, a normal check of the neighbour
				else if (map[neighbourX][neighbourY]) {
					count = count + 1;
				}
			}
		}
		return count;
	}
}
