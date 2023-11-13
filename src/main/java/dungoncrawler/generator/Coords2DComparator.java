/**
 * 
 */
package dungoncrawler.generator;

import java.util.Comparator;

/**
 * @author Mark Gottschling on Jun 24, 2020
 *
 */
public class Coords2DComparator implements Comparator<Coords2D> {

	@Override
	public int compare(Coords2D coords1, Coords2D coords2) {
		if (coords1.getX() < coords2.getX()) {
			return -1;
		}
		else if (coords1.getX() > coords2.getX()) {
			return 1;
		}
		else {
			if (coords1.getY() < coords2.getY()) {
				return -1;
			}
			else if (coords1.getY() > coords2.getY()) {
				return 1;
			}
			else {
				return 0;
			}
		}
	}
}
