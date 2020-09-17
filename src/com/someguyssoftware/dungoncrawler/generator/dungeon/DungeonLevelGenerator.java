/**
 * 
 */
package com.someguyssoftware.dungoncrawler.generator.dungeon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.someguyssoftware.dungoncrawler.generator.Coords2D;
import com.someguyssoftware.dungoncrawler.generator.ILevel;
import com.someguyssoftware.dungoncrawler.generator.ILevelGenerator;
import com.someguyssoftware.dungoncrawler.generator.INode;
import com.someguyssoftware.dungoncrawler.generator.Rectangle2D;
import com.someguyssoftware.dungoncrawler.graph.mst.Edge;

import io.github.jdiemke.triangulation.DelaunayTriangulator;
import io.github.jdiemke.triangulation.NotEnoughPointsException;
import io.github.jdiemke.triangulation.Triangle2D;
import io.github.jdiemke.triangulation.Vector2D;

/**
 * Overlapping rectangle code based on https://stackoverflow.com/questions/3265986/an-algorithm-to-space-out-overlapping-rectangles
 * @author Mark Gottschling on Sep 15, 2020
 *
 */
public class DungeonLevelGenerator implements ILevelGenerator {
	
	protected static final Logger LOGGER = LogManager.getLogger(DungeonLevelGenerator.class);
	
	private int width = 96;
	private int height = 96;
	private int numberOfRooms = 15;
	private int minRoomSize = 5;
	private int maxRoomSize = 15;
	private int movementFactor = 1;
	private double meanFactor = 1.15;
	
	@Override
	public ILevel init() {
		DungeonLevel dungeonData = new DungeonLevel();
		Random random = new Random();
		boolean[][] cellMap = initMap(random);

		List<IDungeonRoom> rooms = initRooms(random);
		cellMap = updateCellMap(cellMap, rooms);
		
		dungeonData.setCellMap(cellMap);
		dungeonData.setRooms(rooms);
		return dungeonData;
	}
	
	// TODO build() needs to call init();
	@Override
	public ILevel build() {
		DungeonLevel dungeonData = new DungeonLevel();
		Random random = new Random();
		boolean[][] cellMap = initMap(random);
		 
		// TODO init rooms taking info from controls - ex # of rooms, min width, max width, min height, max height, size of map, spawn boundary, etc.
		List<IDungeonRoom> rooms = initRooms(random);
		
		// separate rooms
		rooms = separateRooms(rooms, movementFactor);
		
		// select main rooms
		rooms = selectMainRooms(rooms, meanFactor);
		
		// TODO triangulate
		// triangulate valid rooms
//		List<Edge> edges = null;
//		edges = triangulate(rooms);
//		if (edges == null) {
//			return null;//EMPTY_LEVEL;
//		}
		
		// TODO minimum spanning tree
		
		// TODO add extra edges
		
		// TODO add hallways
		
		// TODO reintroduce minor rooms that intersect with hallways
		
		// TODO add elevation variance
		

		cellMap = updateCellMap(cellMap, rooms);

		// save cell map and rooms to the level
		dungeonData.setCellMap(cellMap);
		dungeonData.setRooms(rooms);
		return dungeonData;
	}
	
//	private List<Edge> triangulate(List<? extends INode> nodes) {
//		/*
//		 * maps all rooms by XZ plane (ie x:z)
//		 * this is required for the Delaunay Triangulation library because it only returns edges without any identifying properties, only points
//		 */
//		Map<String, INode> map = new HashMap<>();
//		/*
//		 * holds all rooms in Vector2D format.
//		 * used for the Delaunay Triangulation library to calculate all the edges between rooms.
//		 * 
//		 */
//		Vector<Vector2D> pointSet = new Vector<>();		
//		/*
//		 * holds all the edges that are produced from triangulation
//		 */
//		List<Edge> edges = new ArrayList<>();
//		/*
//		 *  weight/cost array of all rooms
//		 */
//		double[][] matrix = ILevelGenerator.getDistanceMatrix(nodes);
//		/**
//		 * a flag to indicate that an edge leading to the "end" room is created
//		 */
//		boolean isEndEdgeMet = false;
//		int endEdgeCount = 0;
//
////		 sort rooms by id - WHY? BECAUSE the getDistanceMatrix is assuming the ids match their order in the list.... need a better way
////		Collections.sort(rooms, Room.idComparator);
//
//		// map all rooms by XZ plane and build all edges.
//		for (INode node : nodes) {
//			Coords2D origin = node.getOrigin();
//			// map out the rooms by IDs
//			map.put(origin.getX() + ":" + origin.getY(), node);
//			// convert coords into vector2d for triangulation
//			Vector2D v = new Vector2D(origin.getX(), origin.getY());
////			Dungeons2.log.debug(String.format("Room.id: %d = Vector2D: %s", room.getId(), v.toString()));
//			pointSet.add(v);
//		}
//
//		// triangulate the set of points
//		DelaunayTriangulator triangulator = null;
//		try {
//			triangulator = new DelaunayTriangulator(pointSet);
//			triangulator.triangulate();
//		}
//		catch(NotEnoughPointsException e) {
//			LOGGER.warn("Not enough points where provided for triangulation. Level generation aborted.");
//			return null; // TODO return empty list
//		}
//		catch(Exception e) {
////			if (nodes !=null) Dungeons2.log.debug("rooms.size=" + nodes.size());
////			else Dungeons2.log.debug("Rooms is NULL!");
////			if (pointSet != null) Dungeons2.log.debug("Pointset.size=" + pointSet.size());
////			else Dungeons2.log.debug("Pointset is NULL!");
//			
//			LOGGER.error("Unable to triangulate: ", e);
//		}
//
//		// retrieve all the triangles from triangulation
//		List<Triangle2D> triangles = triangulator.getTriangles();
//
//		for(Triangle2D triangle : triangles) {
//			// locate the corresponding rooms from the points of the triangles
//			INode r1 = map.get((int)triangle.a.x + ":" + (int)triangle.a.y);
//			INode r2 = map.get((int)triangle.b.x + ":" + (int)triangle.b.y);
//			INode r3 = map.get((int)triangle.c.x + ":" + (int)triangle.c.y);
//
//			// build an edge based on room distance matrix
//			// begin Minimum Spanning Tree calculations
//			Edge e = new Edge(r1.getId(), r2.getId(), matrix[r1.getId()][r2.getId()]);
//			
//			// TODO for boss room, not necessarily end room
//			// remove any edges that lead to the end room if the end room already has one edge
//			// remove (or don't add) any edges that lead to the end room if the end room already has it's maximum edges (degrees)
//			if (!r1.isEnd() && !r2.isEnd()) {
////			if (!r1.getType().equals(Type.BOSS) && !r2.getType().equals(Type.BOSS)) {
//				edges.add(e);
//			}
//			else if (r1.isStart() || r2.isStart()) {
//				// skip if start joins the end
//			}
//			else if (!isEndEdgeMet) {
//				// add the edge
//				edges.add(e);
//				// increment the number of edges leading to the end room
//				endEdgeCount++;
//				// get the end room
//				Room end = r1.isEnd() ? r1 : r2;
//				if (endEdgeCount >= end.getDegrees()) {
//					isEndEdgeMet = true;
//				}
//			}
//			
//			e = new Edge(r2.getId(), r3.getId(), matrix[r2.getId()][r3.getId()]);
//			if (!r2.isEnd() && !r3.isEnd()) {
//				edges.add(e);
//			}
//			else if (r1.isStart() || r2.isStart()) {
//				// skip
//			}
//			else if (!isEndEdgeMet) {
//				edges.add(e);
//				isEndEdgeMet = true;
//			}
//			
//			e = new Edge(r1.getId(), r3.getId(), matrix[r1.getId()][r3.getId()]);
//			if (!r1.isEnd() && !r3.isEnd()) {
//				edges.add(e);
//			}
//			else if (r1.isStart() || r2.isStart()) {
//				// skip
//			}
//			else if (!isEndEdgeMet) {
//				edges.add(e);
//				isEndEdgeMet = true;
//			}
//		}
//		return edges;
//	}

	/**
	 * 
	 * @param rooms
	 * @param meanFactor
	 * @return
	 */
	public List<IDungeonRoom> selectMainRooms(List<IDungeonRoom> rooms, double meanFactor) {
		int totalArea = 0;
		for (IDungeonRoom room : rooms) {
			totalArea += room.getBox().getWidth() * room.getBox().getHeight();
		}

		int meanArea = (int) totalArea / rooms.size();

		System.out.printf("meanArea=%s\n", meanArea);
		rooms.forEach(room -> {
			if (room.getBox().getWidth() * room.getBox().getHeight() > meanArea) {
				room.setMain(true);
			}
		});
		
		return rooms;
	}

	// TODO currently this is updating the map that is passed in, not creating a new map
	public boolean[][] updateCellMap(boolean cellMap[][], List<IDungeonRoom> rooms) {
		rooms.forEach(room -> {
//			System.out.println("generating room @ (" + room.getOrigin().getX() + ", " + room.getOrigin().getY() + "), width=" + room.getBox().getWidth() + ", height=" + room.getBox().getHeight());
			for (int w = 0; w < room.getBox().getWidth(); w++) {
				for (int d = 0; d < room.getBox().getHeight(); d++) {
					int x = room.getOrigin().getX() + w;
					int z =  room.getOrigin().getY() + d;
					if (x < 96 && z < 96
							&& x >= 0 && z >= 0) {
						cellMap[room.getOrigin().getX() + w][room.getOrigin().getY() + d] = false;
					}
				}
			}
		});
		return cellMap;
	}

	/**
	 * 
	 * @param sourceRooms
	 * @param movementFactor dictates how much to move a R at a time. lower number mean the Rs will be closer together but more iterations performed
	 * @return
	 */
	public List<IDungeonRoom> separateRooms(List<IDungeonRoom> sourceRooms, int movementFactor) {
		// duplicate the soure rooms
		List<IDungeonRoom> workingRooms = new ArrayList<>(sourceRooms);

		// find the center C of the bounding box of all the rooms.
		Coords2D center = getCenter(workingRooms);

		boolean hasIntersections = true;
		while (hasIntersections) {			
			hasIntersections = iterateSeparationStep(center, workingRooms, movementFactor);
		}
		
		return workingRooms;
	}
	
	/**
	 * 
	 * @param workingRooms
	 * @return
	 */
	public Coords2D getCenter(List<IDungeonRoom> workingRooms) {
		IDungeonRoom boundingBox = getBoundingBox(workingRooms);
		Coords2D center = boundingBox.getCenter();
		return center;
	}

	/**
	 * 
	 * @param workingRooms
	 * @return
	 */
	public boolean iterateSeparationStep(Coords2D center, List<IDungeonRoom> workingRooms, int movementFactor) {
		boolean hasIntersections = false;
		
		// TODO add checks for anchor rooms ex. start and end
		
		for (IDungeonRoom room : workingRooms) {
			// find all the rectangles R' that overlap R.
			List<IDungeonRoom> intersectingRooms = findIntersections(room, workingRooms);
			
			if (intersectingRooms.size() > 0) {

				// Define a movement vector v.
				Coords2D movementVector = new Coords2D(0, 0);
				
				Coords2D centerR = new Coords2D(room.getCenter());
				
				// for each rectangle R that overlaps another.
				for (IDungeonRoom rPrime : intersectingRooms) {
					Coords2D centerRPrime = new Coords2D(rPrime.getCenter());

					int translatedX = centerR.getX() - centerRPrime.getX();
					int translatedY = centerR.getY() - centerRPrime.getY();

					// TODO this statement is not exactly true. it is not "proportional", but increments by 1 (or the movementFactor)
					// add a vector to v proportional to the vector between the center of R and R'.
					movementVector.translate(translatedX < 0 ? -movementFactor : movementFactor,
							translatedY < 0 ? -movementFactor : movementFactor);
				}
				
				int translatedX = centerR.getX() - center.getX();
				int translatedY = centerR.getY() - center.getY();

				// add a vector to v proportional to the vector between C and the center of R.
				movementVector.translate(translatedX < 0 ? -movementFactor : movementFactor,
						translatedY < 0 ? -movementFactor : movementFactor);

				// translate R by v.
				room.setOrigin(new Coords2D(room.getOrigin().getX() + movementVector.getX(), room.getOrigin().getY() + movementVector.getY()));

				// repeat until nothing overlaps.
				hasIntersections = true;
			}
		}
		return hasIntersections;
	}

	private List<IDungeonRoom> findIntersections(IDungeonRoom room, List<IDungeonRoom> sourceRooms) {
		ArrayList<IDungeonRoom> intersections = new ArrayList<IDungeonRoom>();

		for (IDungeonRoom intersectingRect : sourceRooms) {
			if (!room.equals(intersectingRect) && intersectingRect.getBox().intersects(room.getBox())) {
				intersections.add(intersectingRect);
			}
		}

		return intersections;
	}

	/**
	 * 
	 * @param rooms
	 * @return
	 */
	private IDungeonRoom getBoundingBox(List<IDungeonRoom> rooms) {
		Coords2D topLeft = null;
		Coords2D bottomRight = null;

		for (IDungeonRoom rect : rooms) {
			if (topLeft == null) {
				topLeft = new Coords2D(rect.getOrigin().getX(), rect.getOrigin().getY());
			} else {
				if (rect.getOrigin().getX() < topLeft.getX()) {
					topLeft.setLocation(rect.getOrigin().getX(), topLeft.getY());
				}

				if (rect.getOrigin().getY() < topLeft.getY()) {
					topLeft.setLocation(topLeft.getX(), rect.getOrigin().getY());
				}
			}

			if (bottomRight == null) {
				bottomRight = new Coords2D(rect.getBox().getMaxX() , (int) rect.getBox().getMaxY());
			} else {
				if (rect.getBox().getMaxX() > bottomRight.getX()) {
					bottomRight.setLocation((int) rect.getBox().getMaxX(), bottomRight.getY());
				}

				if (rect.getBox().getMaxY() > bottomRight.getY()) {
					bottomRight.setLocation(bottomRight.getX(), (int) rect.getBox().getMaxY());
				}
			}
		}

		return new DungeonRoom(topLeft.getX(), topLeft.getY(), bottomRight.getX() - topLeft.getX(),
				bottomRight.getY() - topLeft.getY());
	}

	/**
	 * 
	 * @param random
	 * @return
	 */
	protected boolean[][] initMap(Random random) {
		return initMap(this.width, this.height, random);
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
					map[x][y] = true;
			}
		}
		return map;
	}

	private List<IDungeonRoom> initRooms(Random random) {
		return initRooms(random, this.width, this.height, this.minRoomSize, this.maxRoomSize);
	}

	/**
	 * 
	 * @param random
	 * @param width
	 * @param height
	 * @return
	 */
	private List<IDungeonRoom> initRooms(Random random, final int width, final int height, final int minRoomSize, final int maxRoomSize) {
		List<IDungeonRoom> rooms = new ArrayList<>();
		Coords2D centerPoint = new Coords2D(width/2, height / 2);

		// TODO this needs to be defined somewhere
		Rectangle2D boundingBox = new Rectangle2D(0, 0, 30, 30);	
		
		IDungeonRoom startRoom = generateRoom(random, centerPoint, boundingBox, minRoomSize, maxRoomSize);
		startRoom
			.setRoomType(DungeonRoomType.START)
			.setMain(true)
			.setId(0);		
		rooms.add(startRoom);
		
		// TODO need to add start room and flagged
		for (int roomIndex = 0; roomIndex < numberOfRooms; roomIndex++) {
			IDungeonRoom room = generateRoom(random, centerPoint, boundingBox, minRoomSize, maxRoomSize);
			room.setId(roomIndex + 1);
			rooms.add(room);
		}
		
		// have to have at least one end room
		IDungeonRoom endRoom = generateRoom(random, centerPoint, boundingBox, minRoomSize, maxRoomSize);
		endRoom
			.setRoomType(DungeonRoomType.END)
			.setMain(true)
			.setId(rooms.size() + 1);		
		rooms.add(endRoom);
		
		return rooms;
	}

	/**
	 * 
	 * @param random
	 * @param centerPoint
	 * @param boundingBox
	 * @param minRoomSize2
	 * @param maxRoomSize2
	 * @return
	 */
	private IDungeonRoom generateRoom(Random random, Coords2D centerPoint, Rectangle2D boundingBox, int minRoomSize2,
			int maxRoomSize2) {
		
		int sizeX = random.nextInt(maxRoomSize - minRoomSize) + minRoomSize;
		int sizeZ = random.nextInt(maxRoomSize - minRoomSize) + minRoomSize;
		int offsetX = (random.nextInt(boundingBox.getWidth()) - (boundingBox.getWidth()/2)) - (sizeX / 2);
		int offsetZ = (random.nextInt(boundingBox.getHeight()) - (boundingBox.getHeight()/2)) - (sizeZ / 2);
		
		IDungeonRoom room = new DungeonRoom(new Coords2D(centerPoint.getX() + offsetX, centerPoint.getY() + offsetZ), sizeX, sizeZ);
		return room;
	}

	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getNumberOfRooms() {
		return numberOfRooms;
	}
	
	public int getMinRoomSize() {
		return minRoomSize;
	}
	
	public int getMaxRoomSize() {
		return maxRoomSize;
	}
	
	public int getMovementFactor() {
		return movementFactor;
	}
	
	public double getMeanFactor() {
		return meanFactor;
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

	public DungeonLevelGenerator withNumberOfRooms(int num) {
		this.numberOfRooms = num;
		return this;
	}
	
	public DungeonLevelGenerator withMinRoomSize(int num) {
		this.minRoomSize = num;
		return this;
	}
	
	public DungeonLevelGenerator withMaxRoomSize(int num) {
		this.maxRoomSize = num;
		return this;
	}
	
	public DungeonLevelGenerator withMovementFactor(int factor) {
		this.movementFactor = factor;
		return this;
	}
	
	public DungeonLevelGenerator withMeanFactor(double factor) {
		this.meanFactor = factor;
		return this;
	}
}
