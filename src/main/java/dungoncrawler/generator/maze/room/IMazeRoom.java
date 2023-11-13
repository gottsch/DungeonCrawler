package dungoncrawler.generator.maze.room;

import dungoncrawler.generator.Coords2D;
import dungoncrawler.generator.Rectangle2D;

import java.util.List;

/**
 * @author Mark Gottschling on Oct Nov 1, 2023
 *
 */
public interface IMazeRoom {
    boolean isStart();
    boolean isEnd();

    Coords2D getOrigin();

    void setOrigin(Coords2D origin);

    Coords2D getCenter();

    int getMinX();

    int getMaxX();

    int getMinY();

    int getMaxY();

    int getId();

    void setId(int id);

    Rectangle2D getBox();
    void setBox(Rectangle2D box);

    RoomType getType();
    public void setType(RoomType type);

    RoomRole getRole();
    public void setRole(RoomRole role);

    List<RoomFlag> getFlags();
}
