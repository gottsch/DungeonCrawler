/**
 * 
 */
package dungoncrawler.generator.dungeon;

import java.util.ArrayList;
import java.util.List;

import dungoncrawler.generator.Axis;
import dungoncrawler.generator.Coords2D;
import dungoncrawler.generator.Rectangle2D;

/**
 * A corridor is a room-like object with a width of a least 3 (1 for hall, 2 for
 * walls)
 * 
 * @author Mark Gottschling on Sep 22, 2020
 *
 */
public class Corridor implements IDungeonElement {
	private int id;
	private Rectangle2D box;
	private List<Coords2D> exits;
	private Axis axis;
	private List<IRoom> connectsTo;
	private List<IRoom> intersectsWith;

	/**
	 * 
	 */
	public Corridor() {
	}

	/**
	 * 
	 * @param box
	 */
	public Corridor(Rectangle2D box) {
		this.box = box;
	}

	/**
	 * 
	 */
	public void findIntersections(List<IRoom> rooms) {

		for (IRoom room : rooms) {
			if (room.getBox().intersects(getBox())) {
//				DungeonLevelGenerator.LOGGER.info("room -> {} intersects with hall -> {}", room.getBox(), getBox());
					getIntersectsWith().add(room);
			}
		}
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

	public List<IRoom> getConnectsTo() {
		if (connectsTo == null) {
			connectsTo = new ArrayList<>();
		}
		return connectsTo;
	}

	public void setConnectsTo(List<IRoom> connectsTo) {
		this.connectsTo = connectsTo;
	}

	public List<IRoom> getIntersectsWith() {
		if (intersectsWith == null) {
			intersectsWith = new ArrayList<>();
		}
		return intersectsWith;
	}

	public void setIntersectsWith(List<IRoom> intersectsWith) {
		this.intersectsWith = intersectsWith;
	}
}
