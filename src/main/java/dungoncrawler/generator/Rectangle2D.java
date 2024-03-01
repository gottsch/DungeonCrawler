/**
 * 
 */
package dungoncrawler.generator;


import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Uses some elements of java.awt.geo.Rectangle2D
 * @author Mark Gottschling on Sep 16, 2020
 *
 */
public class Rectangle2D {
	Coords2D origin;
	int width;
	int height;

	/**
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public Rectangle2D(int x, int y, int width, int height) {
		this(new Coords2D(x, y), width, height);
	}

	/**
	 * 
	 * @param coords
	 * @param width
	 * @param height
	 */
	public Rectangle2D(Coords2D coords, int width, int height) {
		this.origin = coords;
		this.width = width;
		this.height = height;
	}

	/**
	 * 
	 * @param coords1
	 * @param coords2
	 */
	public Rectangle2D(Coords2D coords1, Coords2D coords2) {
		Coords2D origin = coords1.getX() < coords2.getX() || coords1.getY() < coords2.getY() ? coords1 : coords2;
		this.origin = origin;
		this.width = Math.abs(coords1.getX() - coords2.getX()) + 1;
		this.height = Math.abs(coords1.getY() - coords2.getY()) + 1;
//		this.width = width == 0 ? 1 : width;
//		this.height = height == 0 ? 1 : height;
	}
	
	/**
	 * 
	 * @param r
	 * @return
	 */
	public boolean intersects(Rectangle2D r) {
		return intersects(r.getOrigin().getX(), r.getOrigin().getY(), r.getWidth(), r.getHeight());
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @return
	 */
	public boolean intersects(int x, int y, int w, int h) {
		// NOTE changed from w <=0 and h <=0, meaning sharing walls is allowed
		if (isEmpty() || w < 0 || h < 0) {
			return false;
		}
		double xOrigin = getOrigin().getX();
		double yOrigin = getOrigin().getY();
		return (x + w > xOrigin &&
				y + h > yOrigin &&
				x < xOrigin + getWidth() &&
				y < yOrigin + getHeight());
	}

	/**
	 * 
	 * @return
	 */
	@JsonIgnore
	public boolean isEmpty() {
		// NOTE changed from width <=0 and height <=0
		return (width < 0) || (height < 0);
	}

	@JsonIgnore
	public int getCenterX() {
		return getOrigin().getX() + getWidth() / 2;
	}

	@JsonIgnore
	public int getCenterY() {
		return getOrigin().getY() + getHeight() / 2;
	}

	@JsonIgnore
	public Coords2D getCenter() {
		return new Coords2D(getCenterX(), getCenterY());
	}

	@JsonIgnore
	public int getMinX() {
		return getOrigin().getX();
	}

	@JsonIgnore
	public int getMaxX() {
		// TODO -1 doesn't seem right ??
		// if (1, 1) & w = 3 => (1, 1) -> (4, 4) non-inclusive (ie 4 in the boundary but not included in the render)
		// but if you use -1, then => (1, 1) -> (3, 3) inclusive
		return getOrigin().getX() + getWidth() - 1;
	}

	@JsonIgnore
	public int getMinY() {
		return getOrigin().getY();
	}

	@JsonIgnore
	public int getMaxY() {
		return getOrigin().getY() + getHeight() - 1;
	}

	public Coords2D getOrigin() {
		return origin;
	}

	public void setOrigin(Coords2D origin) {
		this.origin = origin;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	@Override
	public String toString() {
		return "Rectangle2D [origin=" + origin + ", width=" + width + ", height=" + height + "]";
	}
}
