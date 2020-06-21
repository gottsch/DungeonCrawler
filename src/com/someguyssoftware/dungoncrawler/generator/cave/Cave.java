/**
 * 
 */
package com.someguyssoftware.dungoncrawler.generator.cave;

import java.util.ArrayList;
import java.util.List;

import com.someguyssoftware.dungoncrawler.generator.Coords2D;

/**
 * @author Mark Gottschling on Jun 21, 2020
 *
 */
public class Cave {
	private int id;
	private List<Coords2D> cells;
	
	public Cave(int id) {
		this.id = id;
		this.cells = new ArrayList<>();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<Coords2D> getCells() {
		return cells;
	}

	public void setCells(List<Coords2D> cells) {
		this.cells = cells;
	}
	
}
