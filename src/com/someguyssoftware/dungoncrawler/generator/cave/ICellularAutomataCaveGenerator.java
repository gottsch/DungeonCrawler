/**
 * 
 */
package com.someguyssoftware.dungoncrawler.generator.cave;

import com.someguyssoftware.dungoncrawler.generator.ILevelGenerator;

/**
 * @author Mark
 *
 */
public interface ICellularAutomataCaveGenerator extends ILevelGenerator {

	public boolean[][] process(boolean[][] oldMap);

	ICellularAutomataCaveGenerator withGrowthLimit(int limit);

	ICellularAutomataCaveGenerator withDecayLimit(int limit);

	ICellularAutomataCaveGenerator withIterations(int iterations);
}
