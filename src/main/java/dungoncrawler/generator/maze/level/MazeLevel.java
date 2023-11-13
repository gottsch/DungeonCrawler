package dungoncrawler.generator.maze.level;

import dungoncrawler.generator.maze.room.IMazeRoom;

import java.util.List;

public class MazeLevel implements IMazeLevel {
    private List<IMazeRoom> rooms;

    public List<IMazeRoom> getRooms() {
        return rooms;
    }

    public void setRooms(List<IMazeRoom> rooms) {
        this.rooms = rooms;
    }
}
