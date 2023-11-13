package dungoncrawler.generator;

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
	public INode setMaxDegrees(int degrees);

	public NodeType getType();
	public INode setType(NodeType type);

	// TODO rethink these - they probably belong in IRoom as a Node doesn't have a
	// size.
	/*
	 * convenience methods
	 */
	public Coords2D getCenter();

	public int getMinX();
	public int getMaxX();

	public int getMinY();
	public int getMaxY();
}
