/**
 * 
 */
package com.someguyssoftware.dungoncrawler.generator.dungeon;

import java.util.ArrayList;
import java.util.List;

import com.someguyssoftware.dungoncrawler.generator.Axis;
import com.someguyssoftware.dungoncrawler.generator.Coords2D;
import com.someguyssoftware.dungoncrawler.generator.Rectangle2D;

/**
 * A corridor is a room-like object with a width of a least 3 (1 for hall, 2 for walls)
 * @author Mark Gottschling on Sep 22, 2020
 *
 */
public class Corridor {
	private int id;	
	private Rectangle2D box;
	private List<Coords2D> exits;
	private Axis axis;
	
	public Corridor() {}
	public Corridor(Rectangle2D box) {
		this.box = box;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Rectangle2D getBox() {
		return box;
	}
	public void setBox(Rectangle2D box) {
		this.box = box;
	}
	public List<Coords2D> getExits() {
		if (exits == null) {
			exits = new ArrayList<Coords2D>();
		}
		return exits;
	}
	public void setExits(List<Coords2D> exits) {
		this.exits = exits;
	}
	public Axis getAxis() {
		return axis;
	}
	public void setAxis(Axis axis) {
		this.axis = axis;
	}
	@Override
	public String toString() {
		return "Corridor [id=" + id + ", box=" + box + ", axis=" + axis + "]";
	}
}
