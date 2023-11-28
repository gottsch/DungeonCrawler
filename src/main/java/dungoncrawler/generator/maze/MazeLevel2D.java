package dungoncrawler.generator.maze;


import java.beans.Transient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Mark Gottschling on Oct Nov 8, 2023
 *
 */
public class MazeLevel2D implements ILevel2D {
    private int width;
    private int height;
    private Grid2D grid;

    private IRoom2D startRoom;
    private IRoom2D endRoom;
    private List<IRoom2D> rooms;
//    @Deprecated
//    transient private List<Connector2D> connectors;
//    @Deprecated
//    transient private Map<Integer, MazeRegion2D> regionMap;

    /**
     *
     * @param width
     * @param height
     */
    public MazeLevel2D(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new Grid2D(width, height);
    }

    @Override
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public IRoom2D getStartRoom() {
        return startRoom;
    }

    @Override
    public void setStartRoom(IRoom2D startRoom) {
        this.startRoom = startRoom;
    }

    @Override
    public IRoom2D getEndRoom() {
        return endRoom;
    }

    @Override
    public void setEndRoom(IRoom2D endRoom) {
        this.endRoom = endRoom;
    }

    @Override
    public List<IRoom2D> getRooms() {
        return rooms;
    }

    @Override
    public void setRooms(List<IRoom2D> rooms) {
        this.rooms = rooms;
    }

    @Override
    public Grid2D getGrid() {
        return this.grid;
    }

    public void setGrid(Grid2D grid) {
        this.grid = grid;
    }

//    @Deprecated
//    @Override
//    public Map<Integer, MazeRegion2D> getRegionMap() {
//        return regionMap;
//    }

//    @Override
//    public void setRegionMap(Map<Integer, MazeRegion2D> regionMap) {
//        this.regionMap = regionMap;
//    }

//    @Deprecated
//    @Override
//    public List<Connector2D> getConnectors() {
//        if (connectors == null) {
//            connectors = new ArrayList<>();
//        }
//        return connectors;
//    }
//
//    @Override
//    public void setConnectors(List<Connector2D> connectors) {
//        this.connectors = connectors;
//    }
}
