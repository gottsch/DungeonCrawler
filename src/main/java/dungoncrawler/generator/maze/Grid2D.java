package dungoncrawler.generator.maze;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dungoncrawler.generator.Coords2D;

import java.util.List;


public class Grid2D {
    private final Cell[][] cells;

    
    public Grid2D(int width, int height) {
        cells = new Cell[width][height];

        // initialize ie add walls to the borders
        initialize(width, height);
    }

    private Grid2D(Cell[][] cells) {
        this.cells = cells.clone();
    }

    private void initialize(int width, int height) {
//        for (int x = 0; x < width; x++) {
//            tiles[x][0] = 1;
//            tiles[x][height-1] = 1;
//        }
//
//        for (int y = 0; y < height; y++) {
//            tiles[0][y] = 1;
//            tiles[width-1][y] = 1;
//        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cells[x][y] = new Cell(x, y);
                if (x == 0 || x == width -1 || y == 0 || y == height -1) {
                    cells[x][y].setType(CellType.WALL);
                }
            }
        }
    }

//    /**
//     *
//     * @param x
//     * @param y
//     * @return
//     */
//    public int getId(int x, int y) {
//        return tiles[x][y];
//    }
//
//    public void setId(int x, int y, int id) {
//        tiles[x][y] = id;
//    }
//

    @JsonIgnore
    public Coords2D getSize() {
        return new Coords2D(cells.length, cells[0].length);
    }

    @JsonIgnore
    public int getWidth() {
        return cells.length;
    }

    @JsonIgnore
    public int getHeight() {
        return cells[0].length;
    }

    @JsonIgnore
    public Cell get(Coords2D coords) {
        return cells[coords.getX()][coords.getY()];
    }

    @JsonIgnore
    public void set(Coords2D coords, Cell cell) {
        cells[coords.getX()][coords.getY()] = cell;
    }

    public Cell get(int x, int y) {
        return cells[x][y];
    }

    public void set(int x, int y, Cell cell) {
        cells[x][y] = cell;
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
                        cells[xOffset + x][yOffset + y].setType(CellType.WALL);
                    } else {
                        // else update tiles with the id of the room
                        cells[xOffset + x][yOffset + y].setType((CellType.ROOM));
                        cells[xOffset + x][yOffset + y].setRegionId((int)room.getId());
                    }
                }
            }
        });
    }

    @Override
    protected Grid2D clone() throws CloneNotSupportedException {
        return new Grid2D(this.cells);
    }
}
