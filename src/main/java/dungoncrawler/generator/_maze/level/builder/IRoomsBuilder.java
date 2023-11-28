package dungoncrawler.generator._maze.level.builder;

import dungoncrawler.generator._maze.level.IMazeLevelGenerator;
import dungoncrawler.generator._maze.room.IMazeRoom;

/**
 * @author Mark Gottschling on Oct Nov 1, 2023
 *
 */
public interface IRoomsBuilder {
    public IRoomsBuilder amount(int amount);

    public IRoomsBuilder sizes(int min, int max);
    public IRoomsBuilder degrees(int min, int max);
    public IRoomsBuilder seeds(int amount);
    public IRoomsBuilder add(IMazeRoom room);

    public IRoomBuilder startRoom();
    public IRoomBuilder endRoom();
    public IMazeLevelGenerator create();

    // any next step would here
}
