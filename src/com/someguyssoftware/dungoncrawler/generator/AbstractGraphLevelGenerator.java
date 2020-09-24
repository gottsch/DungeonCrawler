/**
 * 
 */
package com.someguyssoftware.dungoncrawler.generator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mark Gottschling on Sep 18, 2020
 *
 */
public abstract class AbstractGraphLevelGenerator implements ILevelGenerator {

	// TODO finish
	
	@Override
	public abstract ILevel build();

	@Override
	public ILevelGenerator withWidth(int width) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILevelGenerator withHeight(int height) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILevel init() {
		// TODO Auto-generated method stub
		return null;
	}

}
