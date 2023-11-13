package dungoncrawler.generator.maze2;

import java.util.List;
import java.util.Map;

public interface ILevel2D {
    int getWidth();

    int getHeight();

    IRoom2D getStartRoom();
    void setStartRoom(IRoom2D startRoom);
    IRoom2D getEndRoom();
    void setEndRoom(IRoom2D endRoom);

    public List<IRoom2D> getRooms();
    public void setRooms(List<IRoom2D> rooms);

    Grid2D getGrid();

    Map<Integer, MazeRegion2D> getRegionMap();

    void setRegionMap(Map<Integer, MazeRegion2D> regionMap);


    List<Connector2D> getConnectors();

    void setConnectors(List<Connector2D> connectors);
}
