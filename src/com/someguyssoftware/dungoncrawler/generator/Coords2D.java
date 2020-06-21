/**
 * 
 */
package com.someguyssoftware.dungoncrawler.generator;

/**
 * @author Mark Gottschling on Jun 21, 2020
 *
 */
public class Coords2D {
	private int x;
	private int y;
	
	public Coords2D() {}
	public Coords2D(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
}
