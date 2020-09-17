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
	
	/**
	 * 
	 */
	public Coords2D() {}
	
	/**
	 * 
	 * @param x
	 * @param y
	 */
	public Coords2D(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Coords2D(Coords2D coords) {
		this.x = coords.getX();
		this.y = coords.getY();		
	}
	
	public double getDistance(Coords2D destination) {
	    double d0 = this.getX() - destination.getX();
	    double d1 = this.getY() - destination.getY();
	    return Math.sqrt(d0 * d0 + d1 * d1);
	}
	
    public void translate(int xDistance, int yDistance) {
        this.x += xDistance;
        this.y += yDistance;
    }
    
	public void setLocation(int x, int y) {
		setX(x);
		setY(y);
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
