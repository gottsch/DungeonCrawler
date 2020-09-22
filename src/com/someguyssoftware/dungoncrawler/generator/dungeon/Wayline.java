/**
 * 
 */
package com.someguyssoftware.dungoncrawler.generator.dungeon;

import com.someguyssoftware.dungoncrawler.generator.Coords2D;
import com.someguyssoftware.dungoncrawler.generator.Rectangle2D;

/**
 * @author Mark Gottschling on Sep 21, 2020
 *
 */
public class Wayline {
	private Coords2D connector1;
	private Coords2D connector2;
	private Rectangle2D box;
	
	public Wayline() {}
	
	public Wayline(Coords2D connector1, Coords2D connector2) {
		this.connector1 = connector1;
		this.connector2 = connector2;
		this.box = new Rectangle2D(connector1, connector2);
	}

	public Coords2D getConnector1() {
		return connector1;
	}

	public void setConnector1(Coords2D connector1) {
		this.connector1 = connector1;
	}

	public Coords2D getConnector2() {
		return connector2;
	}

	public void setConnector2(Coords2D connector2) {
		this.connector2 = connector2;
	}

	public int getWeight() {
		return Math.max(this.box.getWidth(), this.box.getHeight());
	}

	public Rectangle2D getBox() {
		return box;
	}

	public void setBox(Rectangle2D box) {
		this.box = box;
	}
}
