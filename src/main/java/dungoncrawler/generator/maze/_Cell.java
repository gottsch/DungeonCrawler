package dungoncrawler.generator.maze;

import java.util.*;

/**
 * @author Mark Gottschling on Oct Nov 9, 2023
 * called Cell for now, for lack of a better term.
 */
@Deprecated
public class _Cell {
    private int x;
    private int y;
    private _Cell north;
    private _Cell east;
    private _Cell south;
    private _Cell west;
    private Map<_Cell, Boolean> links = new HashMap<>(4, 0.75f);

    public _Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void link(_Cell cell) {
        links.put(cell, true);
        cell.getLinks().put(this, true);
    }

    public void unlink(_Cell cell) {
        links.remove(cell);
        cell.getLinks().remove(this);
    }

    public Set<_Cell> links() {
        return links.keySet();
    }

    public boolean isLinked(_Cell cell) {
        return links.containsKey(cell);
    }

    public Set<_Cell> neighbors() {
        Set<_Cell> neighbors = new HashSet<>();
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

    public _Cell getNorth() {
        return north;
    }

    public void setNorth(_Cell north) {
        this.north = north;
    }

    public _Cell getEast() {
        return east;
    }

    public void setEast(_Cell east) {
        this.east = east;
    }

    public _Cell getSouth() {
        return south;
    }

    public void setSouth(_Cell south) {
        this.south = south;
    }

    public _Cell getWest() {
        return west;
    }

    public void setWest(_Cell west) {
        this.west = west;
    }

    public Map<_Cell, Boolean> getLinks() {
        return links;
    }

    public void setLinks(Map<_Cell, Boolean> links) {
        this.links = links;
    }
}
