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
	private float chanceToStartSolid = 0.4f;
	private int growthLimit = 4;
	private int decayLimit = 3;
	private int width = 96;
	private int height = 96;
	private int iterations = 2;

	public boolean[][] build() {
		Random random = new Random();
		boolean[][] map = initMap(random);
		for (int stepIndex = 0; stepIndex < iterations; stepIndex++) {
			map = process(map);
		}
		return map;
	}
	
	/**
	 * 
	 * @param random
	 * @return
	 */
	private boolean[][] initMap(Random random) {
		return initMap(width, height, random);
	}
	
	/**
	 * 
	 * @param width
	 * @param height
	 * @param random
	 * @return
	 */
	// TODO make private
	public boolean[][] initMap(int width, int height, Random random) {
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
	 * @param oldMap
	 * @return
	 */
	public boolean[][] process(boolean[][] oldMap) {
		boolean[][] newMap = new boolean[oldMap.length][oldMap[0].length];
		// loop over each row and column of the map
		for (int x = 0; x < oldMap.length; x++) {
			for (int y = 0; y < oldMap[0].length; y++) {
				int nbs = countSolidNeighbours(oldMap, x, y);
				// the new value is based on our simulation rules
				// first, if a cell is solid but has too few neighbours, decay it.
				if (oldMap[x][y]) {
					if (nbs < decayLimit) {
						newMap[x][y] = false;
					} 
					else {
						newMap[x][y] = true;
					}
				} // otherwise, if the cell is empty now, check if it has the right number of neighbours to grow
				else {
					if (nbs > growthLimit) {
						newMap[x][y] = true;
					}
					else {
						newMap[x][y] = false;
					}
				}
			}
		}
		return newMap;
	}

	/*
	 * Returns the number of cells in a ring around (x,y) that are solid.
	 */
	private int countSolidNeighbours(boolean[][] map, int x, int y) {
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

	public float getChanceToStartSolid() {
		return chanceToStartSolid;
	}

	public int getGrowthLimit() {
		return growthLimit;
	}

	public int getDecayLimit() {
		return decayLimit;
	}

	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getIterations() {
		return iterations;
	}
	
	public CaveLevelGenerator withChanceToStartSolid(float chance) {
		this.chanceToStartSolid = chance;
		return this;
	}

	public CaveLevelGenerator withGrowthLimit(int limit) {
		this.growthLimit = limit;
		return this;
	}

	public CaveLevelGenerator withDecayLimit(int limit) {
		this.decayLimit = limit;
		return this;
	}
	
	public CaveLevelGenerator withWidth(int width) {
		this.width = width;
		return this;
	}
	
	public CaveLevelGenerator withHeight(int height) {
		this.height = height;
		return this;
	}
	
	public CaveLevelGenerator withIterations(int iterations) {
		this.iterations = iterations;
		return this;
	}
}
