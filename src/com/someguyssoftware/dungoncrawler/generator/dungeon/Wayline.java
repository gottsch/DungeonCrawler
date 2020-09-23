/**
 * 
 */
package com.someguyssoftware.dungoncrawler.generator.dungeon;

import com.someguyssoftware.dungoncrawler.generator.Rectangle2D;

/**
 * @author Mark Gottschling on Sep 21, 2020
 *
 */
public class Wayline {
	private WayConnector connector1;
	private WayConnector connector2;
	private Rectangle2D box;
	
	public Wayline() {}
	
	public Wayline(WayConnector connector1, WayConnector connector2) {
		this.connector1 = connector1;
		this.connector2 = connector2;
		this.box = new Rectangle2D(connector1.getCoords(), connector2.getCoords());
	}

	public WayConnector getConnector1() {
		return connector1;
	}

	public void setConnector1(WayConnector connector1) {
		this.connector1 = connector1;
	}

	public WayConnector getConnector2() {
		return connector2;
	}

	public void setConnector2(WayConnector connector2) {
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

	@Override
	public String toString() {
		return "Wayline [connector1=" + connector1 == null ? "null" : connector1 + ", connector2=" + connector2 == null ? "null" : connector2 + ", box=" + box == null ? "null" : box + "]";
	}
}
