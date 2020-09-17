/**
 * 
 */
package com.someguyssoftware.dungoncrawler.generator.cave;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.someguyssoftware.dungoncrawler.generator.Coords2D;
import com.someguyssoftware.dungoncrawler.generator.Coords2DComparator;
import com.someguyssoftware.dungoncrawler.generator.ILevel;
import com.someguyssoftware.dungoncrawler.graph.mst.Edge;

/**
 * 
 * @author Mark Gottschling on Jun 20, 2020
 *
 */
public class CaveLevelGenerator extends AbstractCellularAutomataCaveGenerator {

	/**
	 * 
	 * @return
	 */
	@Override
	public ILevel build() {
		Random random = new Random();
		boolean[][] cellMap = initMap(random);
		CaveLevel caveData;

		for (int stepIndex = 0; stepIndex < getIterations(); stepIndex++) {
			cellMap = process(cellMap);
		}
		cellMap = smooth(cellMap);
		cellMap = fill(cellMap);
		// add one more process iteration to clean up
		cellMap = process(cellMap);

		// locate all the caves
		caveData = findCaves(cellMap);

		// remove invalid/small caves
		caveData = pruneCaves(caveData);

		// update the properties
		for (ICave cave : caveData.getCaves().values()) {
			updateCaveProperites(cave);
		}
		
		// connect caves with hallways/corridors/paths
//		caveData = connectCaves(caveData);
		
		return caveData;
	}

	private CaveLevel connectCaves(CaveLevel caveData) {
		if (caveData.getCaves().size() <= 1) {
			return caveData;
		}
		
		/*
		 * resultant list of edges from triangulation of rooms.
		 */
		List<Edge> edges = new ArrayList<>();
		
		// randomly select a start cave
		ICave startCave = null;
		
		// randomly select an end cave
		ICave endCave = null;
		
		// triangulate valid rooms
		edges = triangulate((List<ICave>)caveData.getCaves().values());
		if (edges == null) {
//			return EMPTY_LEVEL;
			return null;
		}
		
		
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 
	 * @param oldMap
	 * @return
	 */
	@Override
	public boolean[][] process(boolean[][] oldMap) {
		boolean[][] newMap = new boolean[oldMap.length][oldMap[0].length];
		// loop over each row and column of the map
		for (int x = 0; x < oldMap.length; x++) {
			for (int y = 0; y < oldMap[0].length; y++) {
				int solidCount = countNeighbors(oldMap, x, y, true);
				// the new value is based on our simulation rules
				// first, if a cell is solid but has too few neighbours, decay it.
				if (oldMap[x][y]) {
					if (solidCount < getDecayLimit()) {
						newMap[x][y] = false;
					} 
					else {
						newMap[x][y] = true;
					}
				} // otherwise, if the cell is empty now, check if it has the right number of neighbours to grow
				else {
					if (solidCount > getGrowthLimit()) {
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

	@Override
	public ILevel init() {
		// TODO Auto-generated method stub
		return null;
	}
}
