package dungoncrawler.generator._maze.level.builder;


import dungoncrawler.generator.Coords2D;
import dungoncrawler.generator._maze.room.RoomFlag;

import java.util.List;

public abstract class AbstractRoomBuilder implements IRoomBuilder {
    private int width = -1;
    private int height = -1;
    private Coords2D spawn = Coords2D.EMPTY;
    private List<RoomFlag> flags;

    @Override
    public IRoomBuilder size(int width, int depth) {
        this.width = width;
        this.height = depth;
        return this;
    }

    @Override
    public IRoomBuilder spawn(Coords2D coords) {
        this.spawn = coords;
        return this;
    }

    @Override
    public IRoomBuilder addFlag(RoomFlag flag) {
        getFlags().add(flag);
        return this;
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
    public Coords2D getSpawn() {
        return spawn;
    }

    public void setSpawn(Coords2D spawn) {
        this.spawn = spawn;
    }

    @Override
    public List<RoomFlag> getFlags() {
        return flags;
    }

    public void setFlags(List<RoomFlag> flags) {
        this.flags = flags;
    }
}
