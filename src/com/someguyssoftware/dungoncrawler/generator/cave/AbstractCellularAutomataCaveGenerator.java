/**
 * 
 */
package com.someguyssoftware.dungoncrawler.generator.cave;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

import java.util.Map.Entry;

import com.someguyssoftware.dungoncrawler.generator.Coords2D;
import com.someguyssoftware.dungoncrawler.generator.Coords2DComparator;
import com.someguyssoftware.dungoncrawler.generator.ILevelGenerator;
import com.someguyssoftware.dungoncrawler.graph.mst.Edge;

import io.github.jdiemke.triangulation.DelaunayTriangulator;
import io.github.jdiemke.triangulation.Edge2D;
import io.github.jdiemke.triangulation.NotEnoughPointsException;
import io.github.jdiemke.triangulation.Triangle2D;
import io.github.jdiemke.triangulation.Vector2D;

/**
 * 
 * @author Mark Gottschling on Jun 25, 2020
 *
 */
public abstract class AbstractCellularAutomataCaveGenerator implements ICellularAutomataCaveGenerator {
	private float chanceToStartSolid = 0.4f;
	private int width = 96;
	private int height = 96;
	private int growthLimit = 4;
	private int decayLimit = 3;
	private int iterations = 2;

	private int smoothing = 3;
	private int fill = 4;
	
	private static final int MIN_CAVE_SIZE = 30;
	private static final List<Coords2D> DIRECTIONS;	
	private static final List<Coords2D> TRAILING_DIRECTIONS;
	
//	protected static final Logger logger = LogManager.getLogger();
	
	static {
		DIRECTIONS = Arrays.asList(new Coords2D[] {
				new Coords2D(0, -1), 	// n
				new Coords2D(0, 1),	// s
				new Coords2D(1,0),		// e
				new Coords2D(-1, 0),	// w
				new Coords2D(1, -1),	// ne
				new Coords2D(-1, -1),	// nw
				new Coords2D(-1, 1),	// se
				new Coords2D(1, 1),		// sw
				new Coords2D(0, 0)		// c
		});
		
		TRAILING_DIRECTIONS = Arrays.asList(new Coords2D[] {
				new Coords2D(0, -1), 	// n
				new Coords2D(-1, 0)	// w
		});
	}
	
	/**
	 * 
	 * @param random
	 * @return
	 */
	protected boolean[][] initMap(Random random) {
		return initMap(width, height, random);
	}
	
	/**
	 * 
	 * @param width
	 * @param height
	 * @param random
	 * @return
	 */
	private boolean[][] initMap(int width, int height, Random random) {
		boolean[][] map = new boolean[width][height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (random.nextDouble() < chanceToStartSolid) {
					map[x][y] = true;
				}
			}
		}
		return map;
	}

	/**
	 * 
	 * @return
	 */
	protected Integer[][] initIDMap() {
		Integer[][] map = new Integer[width][height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
					map[x][y] = -1;
			}
		}
		return map;
	}
	
	/**
	 * 
	 * @param oldMap
	 * @return
	 */
	protected boolean[][] smooth(boolean[][] oldMap) {
		boolean[][] newMap = new boolean[oldMap.length][oldMap[0].length];

		// iterate smoothing n times
		for (int n = 0; n < 5; n++) {
			
			for (int x = 0; x < oldMap.length; x++) {
				for (int y = 0; y < oldMap[0].length; y++) {
					int neighborCount = countNeighbors(oldMap, x, y, false);
					if (oldMap[x][y]) {
						if (neighborCount >= getSmoothing()) {
							newMap[x][y] = false;
						} 
						else {
							newMap[x][y] = true;
						}
					}
					else {
						newMap[x][y] = oldMap[x][y];
					}
				}
			}
		}
		return newMap;
	}
	
	/**
	 * 
	 * @param oldMap
	 * @return
	 */
	protected boolean[][] fill(boolean[][] oldMap) {
		boolean[][] newMap = new boolean[oldMap.length][oldMap[0].length];

		for (int x = 0; x < oldMap.length; x++) {
			for (int y = 0; y < oldMap[0].length; y++) {
				int neighborCount = countNeighbors(oldMap, x, y, true);
				if (!oldMap[x][y]) {
					if (neighborCount >= getFill()) {
						newMap[x][y] = true;
					} 
					else {
						newMap[x][y] = false;
					}
				}
				else {
					newMap[x][y] = oldMap[x][y];
				}
			}
		}
		return newMap;
	}
	
	/*
	 * Returns the number of cells in a ring around (x,y) that are solid.
	 */
	@Deprecated
	protected int countSolidNeighbors(boolean[][] map, int x, int y) {
		int count = 0;
		for (int i = -1; i < 2; i++) {
			for (int j = -1; j < 2; j++) {
				int neighbourX = x + i;
				int neighbourY = y + j;
				// if processing the original index
				if (i == 0 && j == 0) {
					// do nothing
					continue;
				}
				// in case the index we're looking at it off the edge of the map, treat as solid
				else if (neighbourX < 0 || neighbourY < 0 || neighbourX >= map.length || neighbourY >= map[0].length) {
					count = count + 1;
				}
				// else, a normal check of the neighbour
				else if (map[neighbourX][neighbourY]) {
					count = count + 1;
				}
			}
		}
		return count;
	}
	
	/**
	 * 
	 * @param map
	 * @param x
	 * @param y
	 * @param isSolid
	 * @return
	 */
	protected int countNeighbors(boolean[][] map, int x, int y, boolean isSolid) {
		int count = 0;
		for (Coords2D direction : DIRECTIONS) {
			int neighbourX = x + direction.getX();
			int neighbourY = y + direction.getY();
			// if processing the original index
			if (direction.getX() == 0 && direction.getY() == 0) {
				// do nothing
				continue;
			}
			// in case the index we're looking at it off the edge of the map, treat as solid
			else if (neighbourX < 0 || neighbourY < 0 || neighbourX >= map.length || neighbourY >= map[0].length) {
				if (isSolid) {
					count = count + 1;
				}
			}
			// else, a normal check of the neighbour
			else if (map[neighbourX][neighbourY] == isSolid) {
				count = count + 1;
			}
		}
		return count;
	}

	/**
	 * 
	 * @param map
	 * @return
	 */
	public CaveLevel findCaves(boolean[][] map) {
		CaveLevel caveData = new CaveLevel();
		Map<Integer, ICave> caves = new HashMap<>();
		ICave cave = null;
		Integer[][] idMap = new Integer[map.length][map[0].length];
		Integer caveID = 0;
		Integer caveIDCounter = 0;
		boolean isCaveIDGenerated = false;
		Coords2D cell;
		// cycle through all the cells in the map
		for (int x = 0; x < getWidth(); x++) {
			for (int y = 0; y < getHeight(); y++) {
				if (!map[x][y]) {
					// create new cave ID if needed
					if (!isCaveIDGenerated) {
						caveID = ++caveIDCounter;
						System.out.println("New ID " + caveID + " at " + x + ", " + y);
						cave = new Cave(caveID);
						caves.put(caveID, cave);
						isCaveIDGenerated = true;
					}
					// create new cell for coords
					cell = new Coords2D(x, y);
					// add cell to cells in cave
					cave.getCells().add(cell);
					// update the caveID map
					idMap[x][y] = caveID;
					
					if (checkForMerge(cell, idMap)) {
						System.out.println("requires merging for cell @ " + x + ", " + y);
						cave = merge(caveID, idMap[x-1][y], caves, idMap);
						// set the current ID to that of the newly merged cave
						caveID = cave.getId();
						System.out.println("Cave ID of merged cave -> " + caveID);
					}
				}
				else {
					isCaveIDGenerated = false;
				}
			}
		}
		caveData.setCaves(caves);
		caveData.setCellMap(map);
		caveData.setIdMap(idMap);		
		return caveData;
	}
	
	/**
	 * 
	 * @param id1
	 * @param id2
	 * @param caves
	 * @return
	 */
	private ICave merge(Integer id1, Integer id2, Map<Integer, ICave> caves, Integer[][] caveIDs) {
		ICave resultCave = null;
		// get the cave the cell belongs to
		ICave cave = caves.get(id1);
		// get the trailing cave of the cell
		ICave otherCave = caves.get(id2);
//		System.out.println(String.format("[%s]cave1.size -> %s, [%s]cave2.size -> [%s]", cave.getId(),  cave.getCells().size(), otherCave.getId(), otherCave.getCells().size()));
		if (cave.getCells().size() > otherCave.getCells().size()) {
			// cycle through all cells updating the ID array
			for (Coords2D cell : otherCave.getCells()) {
				// update ID array
				caveIDs[cell.getX()][cell.getY()] = cave.getId();
				// add cell to cave
				cave.getCells().add(cell);
			}
			otherCave.getCells().clear();
			resultCave = cave;
		}
		else {
			// cycle through all cells updating the ID array
			for (Coords2D cell : cave.getCells()) {
				caveIDs[cell.getX()][cell.getY()] = otherCave.getId();
				otherCave.getCells().add(cell);
			}
			cave.getCells().clear();
			resultCave = otherCave;
		}
		return resultCave;
	}

	/**
	 * true = merge required
	 * false = no merge required
	 * @param cell
	 * @param caveIDs
	 * @return
	 */
	private boolean checkForMerge(Coords2D cell, Integer[][] caveIDs) {
		if (cell.getX() - 1 >= 0) {
			if (caveIDs[cell.getX() - 1][cell.getY()] != null && 
					caveIDs[cell.getX()][cell.getY()].compareTo(caveIDs[cell.getX() - 1][cell.getY()]) != 0) {
//				System.out.println(String.format("cave[cell].id -> %s, cavel[-1].id -> %s", caveIDs[cell.getX()][cell.getY()], caveIDs[cell.getX()-1][cell.getY()]));
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 
	 * @param caveLevel
	 * @return
	 */
	protected CaveLevel pruneCaves(CaveLevel caveLevel) {
		CaveLevel newCaveLevel = new CaveLevel();

		for (Entry<Integer, ICave> entry : caveLevel.getCaves().entrySet()) {
			ICave cave = entry.getValue();
			if (cave.getCells().size() < MIN_CAVE_SIZE) {

				for (Coords2D cell : cave.getCells()) {
					// set all cells to closed (true)
					caveLevel.getCellMap()[cell.getX()][cell.getY()] = true;
					// set all IDs to -1
					caveLevel.getIdMap()[cell.getX()][cell.getY()] = -1;
				}				
			}
			else {
				newCaveLevel.getCaves().put(cave.getId(), cave);
			}
		}
		newCaveLevel.setCellMap(caveLevel.getCellMap());
		newCaveLevel.setIdMap(caveLevel.getIdMap());
		return newCaveLevel;
	}

	/**
	 * 
	 * @param cave
	 * @return
	 */
	protected ICave updateCaveProperites(ICave cave) {
		// calculate all cave properties
		int minX, maxX;
		int minY, maxY;

		minX = maxX = cave.getCells().get(0).getX();
		minY = maxY = cave.getCells().get(0).getY();
		// find the min and max of x and y
		for (Coords2D cell : cave.getCells()) {
			if (cell.getX() < minX) {
				minX = cell.getX();
			}
			else if (cell.getX() > maxX) {
				maxX = cell.getX();
			}

			if (cell.getY() < minY) {
				minY = cell.getY();
			}
			else if (cell.getY() > maxY) {
				maxY = cell.getY();
			}
		}
		cave.setCoords(new Coords2D(minX, minY));
		cave.setWidth(maxX - minX);
		cave.setHeight(maxY - minY);

		// sort the cells
		Collections.sort(cave.getCells(), new Coords2DComparator());

		return cave;
	}
	
	/**
	 * 
	 * @param caves
	 * @return
	 */
	protected List<Edge> triangulate(List<ICave> caves) {
		/*
		 * maps all rooms by XZ plane (ie x:z)
		 * this is required for the Delaunay Triangulation library because it only returns edges without any identifying properties, only points
		 */
		Map<String, ICave> map = new HashMap<>();
		
		/*
		 * holds all caves in Vector2D format.
		 * used for the Delaunay Triangulation library to calculate all the edges between caves.
		 * 
		 */
		Vector<Vector2D> pointSet = new Vector<>();		
		
		/*
		 * holds all the edges that are produced from triangulation
		 */
		List<Edge> edges = new ArrayList<>();
		
		/*
		 *  weight/cost array of all rooms
		 */
		double[][] matrix = getDistanceMatrix(caves);
		/**
		 * a flag to indicate that an edge leading to the "end" room is created
		 */
		boolean isEndEdgeMet = false;
		int endEdgeCount = 0;

		// sort caves by id - why?
//		Collections.sort(rooms, Room.idComparator);

		// map all rooms by XZ plane and build all edges.
		for (ICave cave : caves) {
//			ICoords center = room.getCoords();
			Coords2D center = cave.getCenter();
			
			// map out the rooms by IDs
//			map.put(center.getX() + ":" + center.getZ(), cave);
			// convert coords into vector2d for triangulation
			Vector2D v = new Vector2D(center.getX(), center.getY());
//			logger.debug(String.format("Room.id: %d = Vector2D: %s", room.getId(), v.toString()));
			pointSet.add(v);
		}

		// triangulate the set of points
		DelaunayTriangulator triangulator = null;
		try {
			triangulator = new DelaunayTriangulator(pointSet);
			triangulator.triangulate();
		}
		catch(NotEnoughPointsException e) {
//			logger.warn("Not enough points where provided for triangulation. Level generation aborted.");
			return null; // TODO return empty list
		}
		catch(Exception e) {
//			if (caves !=null) logger.debug("rooms.size=" + caves.size());
//			else logger.debug("Rooms is NULL!");
//			if (pointSet != null) logger.debug("Pointset.size=" + pointSet.size());
//			else logger.debug("Pointset is NULL!");
			
//			logger.error("Unable to triangulate: ", e);
		}

		// retrieve all the triangles from triangulation
		List<Triangle2D> triangles = triangulator.getTriangles();

		for(Triangle2D triangle : triangles) {
			// locate the corresponding rooms from the points of the triangles
			ICave r1 = map.get((int)triangle.a.x + ":" + (int)triangle.a.y);
			ICave r2 = map.get((int)triangle.b.x + ":" + (int)triangle.b.y);
			ICave r3 = map.get((int)triangle.c.x + ":" + (int)triangle.c.y);

			// build an edge based on room distance matrix
			// begin Minimum Spanning Tree calculations
			Edge e = new Edge(r1.getId(), r2.getId(), matrix[r1.getId()][r2.getId()]);
			
			// TODO for boss room, not necessarily end room
			// remove any edges that lead to the end room if the end room already has one edge
			// remove (or don't add) any edges that lead to the end room if the end room already has it's maximum edges (degrees)
//			if (!r1.isEnd() && !r2.isEnd()) {
////			if (!r1.getType().equals(Type.BOSS) && !r2.getType().equals(Type.BOSS)) {
//				edges.add(e);
//			}
//			else if (r1.isStart() || r2.isStart()) {
//				// skip if start joins the end
//			}
//			else if (!isEndEdgeMet) {
////				 add the edge
				edges.add(e);
//				// increment the number of edges leading to the end room
//				endEdgeCount++;
//				// get the end room
//				ICave end = r1.isEnd() ? r1 : r2;
//				if (endEdgeCount >= end.getDegrees()) {
//					isEndEdgeMet = true;
//				}
//			}
			
			e = new Edge(r2.getId(), r3.getId(), matrix[r2.getId()][r3.getId()]);
//			if (!r2.isEnd() && !r3.isEnd()) {
//				edges.add(e);
//			}
//			else if (r1.isStart() || r2.isStart()) {
//				// skip
//			}
//			else if (!isEndEdgeMet) {
				edges.add(e);
//				isEndEdgeMet = true;
//			}
			
			e = new Edge(r1.getId(), r3.getId(), matrix[r1.getId()][r3.getId()]);
//			if (!r1.isEnd() && !r3.isEnd()) {
//				edges.add(e);
//			}
//			else if (r1.isStart() || r2.isStart()) {
//				// skip
//			}
//			else if (!isEndEdgeMet) {
				edges.add(e);
//				isEndEdgeMet = true;
//			}
		}
		return edges;
	}
	
	// TODO this can be moved into ILevelGenerator -> works for caves, rooms, etc
	// TODO ICave needs to extend something more generic that can be applied to caves and rooms, like ISpace
	/**
	 * It is assumed that the caves/rooms list is sorted in some fashion or the caller has a method to map the matrix indices back to a cave/room object
	 * @param caves
	 * @return
	 */
	protected static double[][] getDistanceMatrix(List<ICave> caves) {
		double[][] matrix = new double[caves.size()][caves.size()];

		for (int i = 0; i < caves.size(); i++) {
			ICave cave = caves.get(i);
			for (int j = 0; j < caves.size(); j++) {
				ICave node = caves.get(j);
				if (cave == node) {
					matrix[i][j] = 0.0;
				}
				else {
					if (matrix[i][j] == 0.0) {
						// calculate distance;
						double dist = cave.getCenter().getDistance(node.getCenter());
						matrix[i][j] = dist;
						matrix[j][i] = dist;
					}
				}
			}
		}
		return matrix;
	}
	
	public float getChanceToStartSolid() {
		return chanceToStartSolid;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	@Override
	public ILevelGenerator withWidth(int width) {
		this.width = width;
		return this;
	}
	
	@Override
	public ILevelGenerator withHeight(int height) {
		this.height = height;
		return this;
	}

	public int getGrowthLimit() {
		return growthLimit;
	}

	public int getDecayLimit() {
		return decayLimit;
	}
	
	public int getIterations() {
		return iterations;
	}
	
	public int getSmoothing() {
		return smoothing;
	}
	
	public int getFill() {
		return fill;
	}
	
	@Override
	public ICellularAutomataCaveGenerator withGrowthLimit(int limit) {
		this.growthLimit = limit;
		return this;
	}
	
	@Override
	public ICellularAutomataCaveGenerator withDecayLimit(int limit) {
		this.decayLimit = limit;
		return this;
	}

	@Override
	public ICellularAutomataCaveGenerator withIterations(int iterations) {
		this.iterations = iterations;
		return this;
	}
	
	public AbstractCellularAutomataCaveGenerator withChanceToStartSolid(float chance) {
		this.chanceToStartSolid = chance;
		return this;
	}
	
	public AbstractCellularAutomataCaveGenerator withSmoothing(int smoothing) {
		this.smoothing = smoothing;
		return this;
	}
	
	public AbstractCellularAutomataCaveGenerator withFill(int fill) {
		this.fill = fill;
		return this;
	}
}
