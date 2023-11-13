/**
 * 
 */
package dungoncrawler.generator.cave;

import dungoncrawler.generator.ILevelGenerator;

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
