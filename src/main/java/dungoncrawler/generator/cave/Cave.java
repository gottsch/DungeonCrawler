/**
 * 
 */
package dungoncrawler.generator.cave;

import java.util.ArrayList;
import java.util.List;

import dungoncrawler.generator.Coords2D;

/**
 * @author Mark Gottschling on Jun 21, 2020
 *
 */
public class Cave implements ICave {
	private int id;
	private List<Coords2D> cells;
	private int width;
	private int height;
	private Coords2D coords;
	
	/**
	 * 
	 * @param id
	 */
	public Cave(int id) {
		this.id = id;
		this.cells = new ArrayList<>();
	}
	
	@Override
	public Coords2D getCoords() {
		return coords;
	}

	@Override
	public void setCoords(Coords2D coords) {
		this.coords = coords;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public void setWidth(int width) {
		this.width = width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void setHeight(int height) {
		this.height = height;
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
	public List<Coords2D> getCells() {
		return cells;
	}

	@Override
	public void setCells(List<Coords2D> cells) {
		this.cells = cells;
	}

	@Override
	public Coords2D getCenter() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
