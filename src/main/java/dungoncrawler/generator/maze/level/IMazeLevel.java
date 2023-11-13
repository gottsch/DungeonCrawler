package dungoncrawler.generator.maze.level;

import dungoncrawler.generator.maze.room.IMazeRoom;

import java.util.List;

public interface IMazeLevel {
    public List<IMazeRoom> getRooms();
    public void setRooms(List<IMazeRoom> rooms);
}
