/**
 * 
 */
package com.someguyssoftware.dungoncrawler.generator.cave;

import java.util.Map.Entry;
import java.util.Random;

import com.someguyssoftware.dungoncrawler.generator.Coords2D;
import com.someguyssoftware.dungoncrawler.generator.ILevel;

/**
 * @author Mark Gottschling
 *
 */
public class CaveLevelGenerator extends AbstractCellularAutomataCaveGenerator {

	private static final int MIN_CAVE_SIZE = 30;

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
		
		caveData = findCaves(cellMap);
		
		caveData = pruneCaves(caveData);
		
		return caveData;
	}
	
	/**
	 * 
	 * @param caveLevel
	 * @return
	 */
	private CaveLevel pruneCaves(CaveLevel caveLevel) {
		CaveLevel newCaveLevel = new CaveLevel();

		for (Entry<Integer, Cave> entry : caveLevel.getCaves().entrySet()) {
			Cave cave = entry.getValue();
			if (cave.getCells().size() < MIN_CAVE_SIZE) {

				for (Coords2D cell : cave.getCells()) {
					// set all cells to closed (true)
					caveLevel.getCellMap()[cell.getX()][cell.getY()] = true;
					// set all IDs to -1
					caveLevel.getIdMap()[cell.getX()][cell.getY()] = -1;
				}				
			}
			else {
				newCaveLevel.getCaves().put(cave.getId(), cave);
			}
		}
		newCaveLevel.setCellMap(caveLevel.getCellMap());
		newCaveLevel.setIdMap(caveLevel.getIdMap());
		return newCaveLevel;
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
}
