package dungoncrawler.generator.maze;

import dungoncrawler.generator.Rectangle2D;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mark Gottschling on Oct Nov 9, 2023
 *
 */
public class MazeRegion2D {
    private Integer id;
    private Rectangle2D box;
    private MazeRegionType type;
    private boolean merged;

    // TODO not sure keeping a list of tiles in a region is needed for anything
//    private List<Tile> tiles;
    private List<Connector2D> connectors;

    public MazeRegion2D() {}

    public MazeRegion2D(Integer id) {
        this.id = id;
//        tiles = new ArrayList<>();
        connectors = new ArrayList<>();
    }

    public MazeRegion2D(Integer id, Rectangle2D box) {
        this(id);
        this.box = box;
    }

//    public void addTile(Tile tile) {
//        tiles.add(tile);
//    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean isMerged() {
        return merged;
    }

    public void setMerged(boolean merged) {
        this.merged = merged;
    }

//    public List<Tile> getTiles() {
//        return tiles;
//    }

//    public void setTiles(List<Tile> tiles) {
//        this.tiles = tiles;
//    }

    public List<Connector2D> getConnectors() {
        return connectors;
    }

    public void setConnectors(List<Connector2D> connectors) {
        this.connectors = connectors;
    }

    public MazeRegionType getType() {
        return type;
    }

    public void setType(MazeRegionType type) {
        this.type = type;
    }

    public Rectangle2D getBox() {
        return box;
    }

    public void setBox(Rectangle2D box) {
        this.box = box;
    }
}
