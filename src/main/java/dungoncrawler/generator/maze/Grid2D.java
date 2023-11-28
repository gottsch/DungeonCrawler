package dungoncrawler.generator.maze;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dungoncrawler.generator.Coords2D;

import java.util.List;

/**
 * 0 = rock
 * 1 = wall
 * n = id of region
 */
public class Grid2D {
    private byte[][] tiles;

    public Grid2D(int width, int height) {
        tiles = new byte[width][height];

        // initialize ie add walls to the borders
        initialize(width, height);
    }

    private void initialize(int width, int height) {
        for (int x = 0; x < width; x++) {
            tiles[x][0] = 1;
            tiles[x][height-1] = 1;
        }

        for (int y = 0; y < height; y++) {
            tiles[0][y] = 1;
            tiles[width-1][y] = 1;
        }
    }

    /**
     *
     * @param x
     * @param y
     * @return
     */
    public byte getId(int x, int y) {
        return tiles[x][y];
    }

    public void setId(int x, int y, byte id) {
        tiles[x][y] = id;
    }

    @JsonIgnore
    public byte getId(Coords2D coords) {
        return tiles[coords.getX()][coords.getY()];
    }

    @JsonIgnore
    public void setId(Coords2D coords, byte id) {
        tiles[coords.getX()][coords.getY()] = id;
    }

    /**
     *
     * @param rooms
     */
    public void add(List<IRoom2D> rooms) {
        rooms.forEach(room -> {
            int xOffset = room.getOrigin().getX();
            int yOffset = room.getOrigin().getY();
            for (int x = 0; x < room.getWidth(); x++) {
                for (int y = 0; y < room.getHeight(); y++) {
                    // test for wall indexes
                    if (x ==0 || y == 0 || x == room.getWidth()-1 || y == room.getHeight()-1) {
                        tiles[xOffset + x][yOffset + y] = (byte)1;
                    } else {
                        // else update tiles with the id of the room
                        tiles[xOffset + x][yOffset + y] = (byte)room.getId(); //Integer.valueOf(room.getId()).byteValue();
                    }
                }
            }
        });
    }
}
