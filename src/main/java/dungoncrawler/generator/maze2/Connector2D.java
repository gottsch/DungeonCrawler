package dungoncrawler.generator.maze2;

import dungoncrawler.generator.Coords2D;

/**
 * A class that represents a connection between two regions.
 * @author Mark Gottschling on Oct Nov 9, 2023
 *
 */
public class Connector2D {
    private Coords2D coords;
    private MazeRegion2D region1;
    private MazeRegion2D region2;

    public Connector2D(int x, int y, MazeRegion2D region1, MazeRegion2D region2) {
        this(new Coords2D(x, y), region1, region2);
    }

    public Connector2D(Coords2D coords, MazeRegion2D region1, MazeRegion2D region2) {
        this.coords = coords;
        this.region1 = region1;
        this.region2 = region2;
    }

    public Coords2D getCoords() {
        return coords;
    }

    public void setCoords(Coords2D coords) {
        this.coords = coords;
    }

    public MazeRegion2D getRegion1() {
        return region1;
    }

    public void setRegion1(MazeRegion2D region1) {
        this.region1 = region1;
    }

    public MazeRegion2D getRegion2() {
        return region2;
    }

    public void setRegion2(MazeRegion2D region2) {
        this.region2 = region2;
    }
}
