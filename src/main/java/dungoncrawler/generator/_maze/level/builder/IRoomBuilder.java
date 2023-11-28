package dungoncrawler.generator._maze.level.builder;

import dungoncrawler.generator.Coords2D;
import dungoncrawler.generator._maze.room.RoomFlag;

import java.util.List;

public interface IRoomBuilder {
    public IRoomBuilder size(int width, int depth);
    public IRoomBuilder spawn(Coords2D coords);
    public IRoomBuilder addFlag(RoomFlag flag);
    public IRoomsBuilder next();

    public int getWidth();
    public int getHeight();


    Coords2D getSpawn();

    List<RoomFlag> getFlags();
}
