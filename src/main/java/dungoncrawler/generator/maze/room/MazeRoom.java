package dungoncrawler.generator.maze.room;

import dungoncrawler.generator.Coords2D;
import dungoncrawler.generator.Rectangle2D;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mark Gottschling on Oct Nov 1, 2023
 *
 */
public class MazeRoom implements IMazeRoom {
    private int id;

    private Rectangle2D box;

    private RoomType type;

    private RoomRole role;

    private List<RoomFlag> flags;

    // TODO connectors
    // TODO exits (are just connectors that are used)

    public MazeRoom() {}

    public MazeRoom(int x, int y, int width, int depth) {
        this(new Coords2D(x, y), width, depth);
    }

    public MazeRoom(Coords2D origin, int width, int depth) {
        super();
        this.box = new Rectangle2D(origin, width, depth);
    }

    @Override
    public boolean isStart() {
        return RoomType.START == getType();
    }

    @Override
    public boolean isEnd() {
        return RoomType.END == getType();
    }

    @Override
    public Coords2D getOrigin() {
        return getBox().getOrigin();
    }

    @Override
    public void setOrigin(Coords2D origin) {
        this.getBox().setOrigin(origin);
    }

    @Override
    public Coords2D getCenter() {
        return getBox().getCenter();
    }

    @Override
    public int getMinX() {
        return this.getBox().getMinX();
    }

    @Override
    public int getMaxX() {
        return this.getBox().getMaxX();
    }

    @Override
    public int getMinY() {
        return this.getBox().getMinY();
    }

    @Override
    public int getMaxY() {
        return this.getBox().getMaxY();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public Rectangle2D getBox() {
        return this.box;
    }

    @Override
    public void setBox(Rectangle2D box) {
        this.box = box;
    }

    public void setFlags(List<RoomFlag> flags) {
        this.flags = flags;
    }

    @Override
    public RoomType getType() {
        return type;
    }

    public void setType(RoomType type) {
        this.type = type;
    }

    @Override
    public RoomRole getRole() {
        return role;
    }

    public void setRole(RoomRole role) {
        this.role = role;
    }

    @Override
    public List<RoomFlag> getFlags() {
        if (flags == null) {
            flags = new ArrayList<>();
        }
        return flags;
    }

    @Override
    public String toString() {
        return "MazeRoom{" +
                "width=" + box.getWidth() +
                ", height =" + box.getHeight() +
                ", type=" + type +
                ", role=" + role +
                ", flags=" + flags +
                '}';
    }
}
