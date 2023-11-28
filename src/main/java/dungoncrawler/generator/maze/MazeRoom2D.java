package dungoncrawler.generator.maze;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dungoncrawler.generator.Coords2D;
import dungoncrawler.generator.Rectangle2D;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mark Gottschling on Oct Nov 8, 2023
 *
 */
public class MazeRoom2D implements IRoom2D {
    private int id;
    private Rectangle2D box;

    private boolean isStart;
    private boolean isEnd;

    // TODO degrees is a temporary value and should belong in Region
    private int degrees = 1;

    private List<Coords2D> doorways;


    public MazeRoom2D(Rectangle2D box) {
        this.box = box;
    }
    public MazeRoom2D(int id, Rectangle2D box) {
        this(box);
        this.id = id;
    }

    /////// convenience methods /////
    @JsonIgnore
    @Override
    public Coords2D getOrigin() {
        return box.getOrigin();
    }

    @JsonIgnore
    @Override
    public int getWidth() {
        return box.getWidth();
    }

    @JsonIgnore
    @Override
    public void setWidth(int width) {
        box.setWidth(width);
    }

    @JsonIgnore
    @Override
    public int getHeight() {
        return box.getHeight();
    }

    @JsonIgnore
    @Override
    public void setHeight(int height) {
        box.setHeight(height);
    }

    ///// mutators /////
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
        return box;
    }
    @Override
    public void setBox(Rectangle2D box) {
        this.box = box;
    }

    @Override
    public boolean isStart() {
        return isStart;
    }

    @Override
    public void setStart(boolean start) {
        isStart = start;
    }

    @Override
    public boolean isEnd() {
        return isEnd;
    }

    @Override
    public void setEnd(boolean end) {
        isEnd = end;
    }

    @Override
    public int getDegrees() {
        return degrees;
    }

    @Override
    public void setDegrees(int degrees) {
        this.degrees = degrees;
    }

    @Override
    public List<Coords2D> getDoorways() {
        if (doorways == null) {
            doorways = new ArrayList<>();
        }
        return doorways;
    }

    @Override
    public void setDoorways(List<Coords2D> doorways) {
        this.doorways = doorways;
    }
}
