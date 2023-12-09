package dungoncrawler.generator.maze;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dungoncrawler.generator.Coords2D;

/**
 * @author Mark Gottschling on Oct Nov 10, 2023
 * similar to Cell, but using it for a different purpose ie Prim's Growing Tree w/ modifications
 */
public class Tile2D {
    private Coords2D coords;
    private Direction2D direction;

    public Tile2D(int x, int y, Direction2D direction) {
        this(new Coords2D(x, y), direction);
    }

    public Tile2D(Coords2D coords, Direction2D direction) {
        this.coords = coords;
        this.direction = direction;
    }

    @JsonIgnore
    public int getX() {
        return getCoords().getX();
    }

    @JsonIgnore
    public int getY() {
        return getCoords().getY();
    }

    public Coords2D getCoords() {
        return coords;
    }

    public void setCoords(Coords2D coords) {
        this.coords = coords;
    }

    public Direction2D getDirection() {
        return direction;
    }

    public void setDirection(Direction2D direction) {
        this.direction = direction;
    }
}
