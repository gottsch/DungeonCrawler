package dungoncrawler.generator._maze.level;

import dungoncrawler.generator._maze.room.IMazeRoom;

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
