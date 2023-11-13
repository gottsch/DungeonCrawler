package dungoncrawler.generator.maze2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dungoncrawler.generator.Coords2D;
import dungoncrawler.generator.Rectangle2D;

import java.util.List;

public interface IRoom2D {

    /////// convenience methods /////
    Coords2D getOrigin();

    int getWidth();

    @JsonIgnore
    void setWidth(int width);

    int getHeight();
    void setHeight(int height);

    ///// mutators /////
    int getId();

    void setId(int id);

    ///// mutators /////
    Rectangle2D getBox();

    void setBox(Rectangle2D box);

    boolean isStart();

    void setStart(boolean start);

    boolean isEnd();

    void setEnd(boolean end);

    int getDegrees();

    void setDegrees(int degrees);

    List<Coords2D> getDoorways();
    void setDoorways(List<Coords2D> doorways);
}
