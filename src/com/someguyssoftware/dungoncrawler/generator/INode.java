package com.someguyssoftware.dungoncrawler.generator;

/**
 * 
 * @author Mark Gottschling on Sep 17, 2020
 *
 */
public interface INode {
	
	int getId();
	void setId(int id);
	
	public Coords2D getOrigin();
	public void setOrigin(Coords2D origin);
	
	public Coords2D getCenter();
}
