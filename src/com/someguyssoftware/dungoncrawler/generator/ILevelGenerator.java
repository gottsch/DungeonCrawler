/**
 * 
 */
package com.someguyssoftware.dungoncrawler.generator;

/**
 * @author Mark
 *
 */
public interface ILevelGenerator {

	// TODO may need to change boolean[][] to some sort of class array
	/**
	 * 
	 * @return
	 */
	ILevel build();

	ILevelGenerator withWidth(int width);

	ILevelGenerator withHeight(int height);

}
