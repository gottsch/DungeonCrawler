/**
 * 
 */
package dungoncrawler.generator.dungeon;

import java.util.ArrayList;
import java.util.List;

import dungoncrawler.generator.Coords2D;
import dungoncrawler.generator.INode;
import dungoncrawler.generator.NodeType;
import dungoncrawler.generator.Rectangle2D;

/**
 * @author Mark Gottschling on Sep 15, 2020
 *
 */
public class Room extends BaseRoom implements IDungeonElement {
	private int id;
	private int maxDegrees;
	private NodeType nodeType;
	// TODO move below out to BaseRoom
	private Rectangle2D box;
	private RoomRole roomRole;
	private List<RoomFlag> flags; // TODO should be a Set<>
	private List<Coords2D> exits;
	
	/**
	 * Empty constructor
	 */
	public Room() {
		// ensure all required fields (ex box) are generated lazily in getters if allowing empty constructors
		super();
	}
	
	/*
	 * 
	 */
	public Room(int x, int y, int width, int depth) {
		this(new Coords2D(x, y), width, depth);
	}
	
	/**
	 * 
	 * @param origin
	 * @param width
	 * @param depth
	 */
	public Room(Coords2D origin, int width, int depth) {
		super();
		this.box = new Rectangle2D(origin, width, depth);
		this.maxDegrees = 3;
		this.nodeType = NodeType.STANDARD;
	}
	
	public Rectangle2D getBox() {
		if (box == null) {
			box = new Rectangle2D(0, 0, 0, 0);
		}
		return box;
	}
	
	public void setBox(Rectangle2D box) {
		this.box = box;
	}
	
	@Override
	public Coords2D getOrigin() {
		return getBox().getOrigin();
	}

	@Override
	public void setOrigin(Coords2D origin) {
		this.getBox().setOrigin(origin);
	}
	
	@Override
	public Coords2D getCenter() {
		return getBox().getCenter();
	}
	
	@Override
	public int getMinX() {
		return this.getBox().getMinX();
	}
	
	@Override
	public int getMaxX() {
		return this.getBox().getMaxX();
	}
	
	@Override
	public int getMinY() {
		return this.getBox().getMinY();
	}
	
	@Override
	public int getMaxY() {
		return this.getBox().getMaxY();
	}

	@Override
	public NodeType getType() {
		return nodeType;
	}

	@Override
	public INode setType(NodeType type) {
		this.nodeType = type;
		return this;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public int getMaxDegrees() {
		return maxDegrees;
	}

	@Override
	public INode setMaxDegrees(int degrees) {
		this.maxDegrees = degrees;
		return this;
	}

	@Override
	public RoomRole getRole() {
		return roomRole;
	}

	@Override
	public IRoom setRole(RoomRole roomRole) {
		this.roomRole = roomRole;
		return this;
	}

	@Override
	public String toString() {
		return "DungeonRoom [id=" + id + ", box=" + box + "]";
	}

	@Override
	public List<Coords2D> getExits() {
		if (exits == null) {
			exits = new ArrayList<>();
		}
		return exits;
	}

	@Override
	public void setExits(List<Coords2D> exits) {
		this.exits = exits;
	}

	@Override
	public List<RoomFlag> getFlags() {
		if (flags == null) {
			flags = new ArrayList<>();
		}
		return flags;
	}

	@Override
	public void setFlags(List<RoomFlag> flags) {
		this.flags = flags;
	}
	
	@Override
	public boolean hasFlag(RoomFlag flag) {
		return getFlags().contains(flag);
	}
}
