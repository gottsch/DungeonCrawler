package dungoncrawler.generator._maze.level;

import dungoncrawler.generator._maze.room.IMazeRoom;

import java.util.List;
import java.util.Random;

/**
 * @author Mark Gottschling on Oct Nov 2, 2023
 *
 */
public interface IMazeLevelGenerator {


    public List<IMazeRoom> getSuppliedRooms();

    IMazeRoom getStartRoom();

    IMazeRoom getEndRoom();

    public IMazeLevel generate();

    public Random getRandom();
}
