package dungoncrawler.generator.maze2;

import java.util.*;

/**
 * @author Mark Gottschling on Oct Nov 9, 2023
 * called Cell for now, for lack of a better term.
 */
@Deprecated
public class Cell {
    private int x;
    private int y;
    private Cell north;
    private Cell east;
    private Cell south;
    private Cell west;
    private Map<Cell, Boolean> links = new HashMap<>(4, 0.75f);

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void link(Cell cell) {
        links.put(cell, true);
        cell.getLinks().put(this, true);
    }

    public void unlink(Cell cell) {
        links.remove(cell);
        cell.getLinks().remove(this);
    }

    public Set<Cell> links() {
        return links.keySet();
    }

    public boolean isLinked(Cell cell) {
        return links.containsKey(cell);
    }

    public Set<Cell> neighbors() {
        Set<Cell> neighbors = new HashSet<>();
        if (north != null) neighbors.add(north);
        if (south != null) neighbors.add(south);
        if (east != null) neighbors.add(east);
        if (west != null) neighbors.add(west);
        return neighbors;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Cell getNorth() {
        return north;
    }

    public void setNorth(Cell north) {
        this.north = north;
    }

    public Cell getEast() {
        return east;
    }

    public void setEast(Cell east) {
        this.east = east;
    }

    public Cell getSouth() {
        return south;
    }

    public void setSouth(Cell south) {
        this.south = south;
    }

    public Cell getWest() {
        return west;
    }

    public void setWest(Cell west) {
        this.west = west;
    }

    public Map<Cell, Boolean> getLinks() {
        return links;
    }

    public void setLinks(Map<Cell, Boolean> links) {
        this.links = links;
    }
}
