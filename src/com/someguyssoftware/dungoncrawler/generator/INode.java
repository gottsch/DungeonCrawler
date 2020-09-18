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
	
	public int getMaxDegrees();
	public void setMaxDegrees(int degrees);
	
    public NodeType getType();
    public INode setType(NodeType type);
    
	public Coords2D getCenter();
}
