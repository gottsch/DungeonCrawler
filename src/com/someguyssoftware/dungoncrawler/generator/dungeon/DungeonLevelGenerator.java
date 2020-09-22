/**
 * 
 */
package com.someguyssoftware.dungoncrawler.generator.dungeon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.function.Supplier;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.someguyssoftware.dungoncrawler.generator.AbstractGraphLevelGenerator;
import com.someguyssoftware.dungoncrawler.generator.Axis;
import com.someguyssoftware.dungoncrawler.generator.Coords2D;
import com.someguyssoftware.dungoncrawler.generator.ILevel;
import com.someguyssoftware.dungoncrawler.generator.ILevelGenerator;
import com.someguyssoftware.dungoncrawler.generator.INode;
import com.someguyssoftware.dungoncrawler.generator.NodeType;
import com.someguyssoftware.dungoncrawler.generator.Rectangle2D;
import com.someguyssoftware.dungoncrawler.graph.mst.Edge;
import com.someguyssoftware.dungoncrawler.graph.mst.EdgeWeightedGraph;
import com.someguyssoftware.dungoncrawler.graph.mst.LazyPrimMST;

import io.github.jdiemke.triangulation.DelaunayTriangulator;
import io.github.jdiemke.triangulation.NotEnoughPointsException;
import io.github.jdiemke.triangulation.Triangle2D;
import io.github.jdiemke.triangulation.Vector2D;

/**
 * This is a 2D level generator. Therfore dimensions are described as x-aix [length] and y-axis [width] and the observer is on the z-axis [height], looking down.
 *  So when something is assigned a dimension value that adds volumn to a plane, it will be on the z-axis.
 *  This is different from a 3D view where the Y is the height and Z is the width (or depth).
 * Overlapping rectangle code based on https://stackoverflow.com/questions/3265986/an-algorithm-to-space-out-overlapping-rectangles
 * @author Mark Gottschling on Sep 15, 2020
 *
 */
public class DungeonLevelGenerator extends AbstractGraphLevelGenerator {
	
	protected static final Logger LOGGER = LogManager.getLogger(DungeonLevelGenerator.class);
	
	private static final ILevel EMPTY_LEVEL = new DungeonLevel();

	private static final int MIN_ROOM_OVERLAP = 3;
	
	private int width = 96;
	private int height = 96;
	private int spawnBoxWidth = 30;
	private int spawnBoxHeight = 30;
	private int numberOfRooms = 15;
	private int minRoomSize = 5;
	private int maxRoomSize = 15;
	private int movementFactor = 1;
	private double meanFactor = 1.15;
	private double pathFactor = 0.25;
	
	@Override
	public ILevel init() {
		DungeonLevel dungeonData = new DungeonLevel();
		Random random = new Random();
		boolean[][] cellMap = initMap(random);

		List<IDungeonRoom> rooms = initRooms(random);
		Map<Integer, IDungeonRoom> roomMap = mapRooms(rooms);
		cellMap = updateCellMap(cellMap, rooms);
		
		dungeonData.setCellMap(cellMap);
		dungeonData.setRooms(rooms);
		dungeonData.setRoomMap(roomMap);
		dungeonData.setEdges(new ArrayList<Edge>());
		dungeonData.setPaths(new ArrayList<Edge>());
		dungeonData.setWaylines(new ArrayList<Wayline>());
		return dungeonData;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ILevel build() {
		DungeonLevel dungeonData = (DungeonLevel) init();
		Random random = new Random();
		boolean[][] cellMap = dungeonData.getCellMap();
		List<IDungeonRoom> rooms = dungeonData.getRooms();
		
		// separate rooms
		rooms = (List<IDungeonRoom>)(List<?>)separateNodes(rooms, movementFactor);
		
		// select main rooms
		List<IDungeonRoom> mainRooms = selectMainRooms(rooms, meanFactor);
		
		/*
		 * because of how DelaunayTriangulator works, the main rooms need to in a ordered list
		 * AND their ids need to be reset according to their position in the list
		 */
		IDungeonRoom start = null;
		IDungeonRoom end = null; // TODO can be a list (multiple end rooms, one is primary the others are decoys)
		List<IDungeonRoom> orderedRooms = new LinkedList<>();
		for(IDungeonRoom room : mainRooms) {
			orderedRooms.add(room);
			room.setId(orderedRooms.size()-1);
			if (room.getType() == NodeType.START) {
				start = room;
			}
			else if (room.getType() == NodeType.END) {
				end = room;
			}
		}
				
		// map the rooms
//		Map<Integer, IDungeonRoom> roomMap = mapRooms(orderedMainRooms);
		
		// TODO move to abstract
		/*
		 * DelaunayTriangulator library requires that the list of nodes be in an ordered list
		 * (the actual sort order doesn't matter, as long as it keeps its order).
		 */
		// triangulate valid rooms
		List<Edge> edges = triangulate(orderedRooms);
		if (edges == null) {
			return EMPTY_LEVEL;
		}
		
		// TODO move to abstract
		// get the paths using minimum spanning tree
		List<Edge> paths = getPaths(random, edges, orderedRooms);
		
		// TODO move to abstract
		// test if start room can reach the end room
		if (!breadthFirstSearch(start.getId(), end.getId(), orderedRooms, paths)) {
			LOGGER.debug("A path doesn't exist from start room to end room on level.");
			return EMPTY_LEVEL;
		}
			
		// add waylines
		List<Wayline> waylines = getWaylines(random, paths, orderedRooms);
		
		// add exits to main rooms
		orderedRooms = addExits(waylines, orderedRooms);

		// reintroduce minor rooms into ordered list. these are not auxiliary rooms until it is determine that they intersect with a corridor
		orderedRooms = selectAuxiliaryRooms(waylines, rooms, orderedRooms);
		
		// TODO add elevation variance
		
		// TODO will take the corridor edges as well
		cellMap = updateCellMap(cellMap, orderedRooms);

		// save cell map and rooms to the level
		dungeonData.setCellMap(cellMap);
		dungeonData.setRooms(orderedRooms);
//		dungeonData.setRoomMap(roomMap);
		dungeonData.setEdges(edges);
		dungeonData.setPaths(paths);
		dungeonData.getWaylines().addAll(waylines);
		
		return dungeonData;
	}
	
	/**
	 * 
	 * @param waylines
	 * @param rooms
	 * @param orderedRooms
	 * @return
	 */
	private List<IDungeonRoom> selectAuxiliaryRooms(List<Wayline> waylines, List<IDungeonRoom> rooms,
			List<IDungeonRoom> orderedRooms) {
		List<IDungeonRoom> newOrderedRooms = new LinkedList<>(orderedRooms);
		rooms.forEach(room -> {
//			LOGGER.debug("processing room -> {}, role -> {}\n", room.getId(), room.getRole());
			if (room.getRole() != RoomRole.MAIN) {
//				LOGGER.debug("Not main -> {}\n", room.getId());
				room.setId(orderedRooms.size());
				if (intersects(room, waylines, orderedRooms)) {
					room.setRole(RoomRole.AUXILIARY);
					addExits(room, waylines, orderedRooms);
				}
				room.setId(newOrderedRooms.size());
				newOrderedRooms.add(room);
			}
		});
		return newOrderedRooms;
	}
	
	/**
	 * 
	 * @param waylines
	 * @param orderedRooms
	 * @return
	 */
	private List<IDungeonRoom> addExits(List<Wayline> waylines, List<IDungeonRoom> orderedRooms) {
		final List<IDungeonRoom> newOrderedRooms = new LinkedList<>(orderedRooms);
		newOrderedRooms.forEach(room -> {
			addExits(room, waylines, orderedRooms);
		});
		return newOrderedRooms;
	}
	
	/**
	 * 
	 * @param room
	 * @param waylines
	 * @param orderedRooms
	 */
	private void addExits(IDungeonRoom room, List<Wayline> waylines, List<IDungeonRoom> orderedRooms) {
		waylines.forEach(wayline -> {
			addExits(room, wayline, orderedRooms);
		});
	}
	
	/**
	 * 
	 * @param room
	 * @param wayline
	 * @param orderedRooms
	 */
	private void addExits(IDungeonRoom room, Wayline wayline, List<IDungeonRoom> orderedRooms) {
		if (intersects(room, wayline, orderedRooms)) {
			Coords2D coords1 = wayline.getConnector1();
			Coords2D coords2 = wayline.getConnector2();
			Axis axis = (coords1.getX() == coords2.getX())	? Axis.Y : Axis.X;
			switch(axis) {
			case X:
				if (coords1.getX() < room.getMinX() || coords2.getX() < room.getMinX()) {
					if ((coords1.getX() >= room.getMinX() && coords1.getX() <= room.getMaxX()) 
							|| (coords2.getX() >= room.getMinX() && coords2.getX() <= room.getMaxX())) {
						room.getExits().add(new Coords2D(room.getMinX(), coords1.getY()));
					}
					else {
						room.getExits().add(new Coords2D(room.getMinX(), coords1.getY()));
						room.getExits().add(new Coords2D(room.getMaxX(), coords1.getY()));
					}
				}
				else {
					room.getExits().add(new Coords2D(room.getMaxX(), coords1.getY()));
				}
				break;
			case Y:
				if (coords1.getY() < room.getMinY() || coords2.getY() < room.getMinY()) {
					if ((coords1.getY() >= room.getMinY() && coords1.getY() <= room.getMaxY()) 
							|| (coords2.getY() >= room.getMinY() && coords2.getY() <= room.getMaxY())) {
						room.getExits().add(new Coords2D(coords1.getX(), room.getMinY()));
					}
					else {
						room.getExits().add(new Coords2D(coords1.getX(),room.getMinY()));
						room.getExits().add(new Coords2D(coords1.getX(),room.getMaxY()));
					}
				}
				else {
					room.getExits().add(new Coords2D(coords1.getX(),room.getMaxY()));
				}
				break;	
			}
		}
	}
	
	/**
	 * 
	 * @param room
	 * @param waylines
	 * @param rooms
	 * @return
	 */
	private boolean intersects(IDungeonRoom room, List<Wayline> waylines, List<? extends IDungeonRoom> rooms) {
//		LOGGER.debug("room to intersect with -> {}", room.getBox());
		for (Wayline wayline : waylines) {
			if (intersects(room, wayline, rooms)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param room
	 * @param wayline
	 * @param nodes
	 * @return
	 */
	private boolean intersects(IDungeonRoom room, Wayline wayline, List<? extends IDungeonRoom> nodes) {
		Rectangle2D rectangle = wayline.getBox();
		if (rectangle.intersects(room.getBox()) || room.getBox().intersects(rectangle)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Waylines is a dungeon method, not a graph method, so use IDungeonRoom instead of INodes
	 * @param rand
	 * @param paths
	 * @param nodes
	 * @param factory
	 * @return
	 */
	protected List<Wayline> getWaylines(Random rand, List<Edge> paths, List<? extends INode> nodes) {
		
		/*
		 * a list of a the waylines constructed from paths
		 */
	
		List<Wayline> waylines = new ArrayList<>();
		
		for (Edge path : paths) {
			// get the nodes
			INode node1 = nodes.get(path.v);
			INode node2 = nodes.get(path.w);
			
			/*
			 * NOTE if the rooms overlap each other on a single axis, they are "close" enough that a single wayline (not-elbow) can be used to connect them.
			 */
			
			// horizontal wayline (east-west)
			if (checkHorizontalConnectivity(node1, node2)) {
				Wayline wayline = getHorizontalWayline(node1, node2);		
				waylines.add(wayline);
			}
			// vertical wayline (north-south)
			else if (checkVerticalConnectivity(node1, node2)) {
				Wayline wayline = getVerticalWayline(node1, node2);		
				waylines.add(wayline);
			}
			// elbow wayline
			else {
				Coords2D node1Center = node1.getCenter();

				Coords2D connector1 = null;
				// node2 is to the right/east/positive-x of node1
				if (node2.getCenter().getX() > node1.getCenter().getX()) {
					 connector1 = new Coords2D(node1.getMaxX()-1, node1Center.getY());
				}
				// node2 is to the left/west/negative-x of node1
				else {
					// NOTE only thing that is differences from if() is that node1p uses minX as opposed to maxX and the +1/-1
					connector1 = new Coords2D(node1.getMinX()+1, node1Center.getY());
				}
				
				// NOTE connector2 is the "destination" or "joint" node, so it should be shared with both segments
				Coords2D connector2 = new Coords2D(node2.getCenter().getX(), node1Center.getY());
				Wayline wayline1 = new Wayline(connector1, connector2);
				
				// room2 is up (postivie-y) of room 1
				if (node2.getCenter().getY() > node1.getCenter().getY()) {
					connector1 = new Coords2D(node2.getCenter().getX(), node2.getMinY()+1);
				}
				// room2 is down (negative-y) of room 1
				else {
					connector1 = new Coords2D(node2.getCenter().getX(), node2.getMaxY()-1);
				}
				Wayline wayline2 = new Wayline(connector1, connector2);
				if (wayline1 != null && wayline2 != null) {
					waylines.add(wayline1);
					waylines.add(wayline2);
				}
			}
		}
		return waylines;
	}
	
	/**
	 * Dungeon Method
	 * @param node1
	 * @param node2
	 * @return
	 */
	private boolean checkHorizontalConnectivity(INode node1, INode node2) {
		if ((node1.getMaxY() <= node2.getMaxY() && node1.getMaxY() > (node2.getMinY() + MIN_ROOM_OVERLAP)) ||
				(node2.getMaxY() <= node1.getMaxY() && node2.getMaxY() > (node1.getMinY() + MIN_ROOM_OVERLAP)) ||
				(node1.getMinY() >= node2.getMinY() && node1.getMinY() < (node2.getMaxY() - MIN_ROOM_OVERLAP)) ||
				(node2.getMinY() >= node1.getMinY() && node2.getMinY() < (node1.getMaxY() - MIN_ROOM_OVERLAP))) {
			return true;
		}
		return false;
	}
	
	/**
	 * Dungeon Method
	 * @param node1
	 * @param node2
	 * @return
	 */
	private boolean checkVerticalConnectivity(INode node1, INode node2) {
		// vertical wayline (north-south)
		if ((node1.getMaxX() <= node2.getMaxX() && node1.getMaxX() > (node2.getMinX() + MIN_ROOM_OVERLAP)) ||
				(node2.getMaxX() <= node1.getMaxX() && node2.getMaxX() > (node1.getMinX() + MIN_ROOM_OVERLAP)) ||
				(node1.getMinX() > node2.getMinX() && node1.getMinX() <= (node2.getMaxX() - MIN_ROOM_OVERLAP)) ||
				(node2.getMinX() > node1.getMinX() && node2.getMinX() <= (node1.getMaxX() - MIN_ROOM_OVERLAP))) {
			return true;
		}
		return false;
	}
	
	/**
	 * Dungeon method
	 * @param node1
	 * @param node2
	 * @param nodes
	 * @param factory
	 * @return
	 */
	public Wayline getHorizontalWayline(final INode node1, final INode node2) {
		/*
		 * get the min of the max's of x-axis from the 2 nodes
		 * AND
		 * get the max of the min's of x-axis from the 2 nodes
		 */
		int innerMaxX = Math.min(node1.getMaxX(),  node2.getMaxX());
		int innerMinX = Math.max(node1.getMinX(),  node2.getMinX());
		int innerMaxY = Math.min(node1.getMaxY(), node2.getMaxY());
		int innerMinY = Math.max(node1.getMinY(), node2.getMinY());
		
		int waylineY = (innerMaxY + innerMinY) / 2;

		Coords2D connector1 = new Coords2D(innerMinX+1, waylineY);
		Coords2D connector2 = new Coords2D(innerMaxX-1, waylineY);
		Wayline wayline = new Wayline(connector1, connector2);
		return wayline;
	}

	/**
	 * Dungeon method
	 * @param node1
	 * @param node2
	 * @param nodes
	 * @param factory
	 * @return
	 */
	public Wayline getVerticalWayline(final INode node1, final INode node2) {
		/*
		 * get the min of the max's of x-axis from the 2 nodes
		 * AND
		 * get the max of the min's of x-axis from the 2 nodes
		 */
		int innerMaxX = Math.min(node1.getMaxX(),  node2.getMaxX());
		int innerMinX = Math.max(node1.getMinX(),  node2.getMinX());
		int innerMaxY = Math.min(node1.getMaxY(), node2.getMaxY());
		int innerMinY = Math.max(node1.getMinY(), node2.getMinY());
		
		int waylineX = (innerMaxX + innerMinX) / 2;
		
		Coords2D connector1 = new Coords2D(waylineX, innerMinY + 1);
		Coords2D connector2 = new Coords2D(waylineX, innerMaxY - 1);
		Wayline wayline = new Wayline(connector1, connector2);
		return wayline;
	}
	
	/**
	 * 
	 * @param edges
	 * @param mainRooms
	 * @return
	 */
	public List<Edge> getPaths(Random random, List<Edge> edges, List<? extends INode> nodes) {
		/*
		 * paths are the reduced edges generated by the Minimun Spanning Tree
		 */
		List<Edge> paths = new ArrayList<>();
		
		/**
		 * counts the number of edges that are assigned to each node/vertex
		 */
		int[] edgeCount = new int[nodes.size()];
		
		/*
		 * Map to keep track of which edges for source list have already been used (in MST and extra edges)
		 */
		Map<String, Edge> usedEdges = new HashMap<>();
		
		// reduce all edges to MST
		EdgeWeightedGraph graph = new EdgeWeightedGraph(nodes.size(), edges);
		LazyPrimMST mst = new LazyPrimMST(graph);
		for (Edge edge : mst.edges()) {
//			if (nodeMap.containsKey(Integer.valueOf(e.v)) && nodeMap.containsKey(Integer.valueOf(e.w))) {
			if (edge.v < nodes.size() && edge.w < nodes.size()) {
				INode node1 = nodes.get(edge.v);
				INode node2 = nodes.get(edge.w);	
				paths.add(edge);
				edgeCount[node1.getId()]++;
				edgeCount[node2.getId()]++;
			}
			else {
				//LOGGER.warn(String.format("Ignored Room: array out-of-bounds: v: %d, w: %d", edge.v, edge.w));
			}
		}		
		
		// add more edges
		int addtionalEdges = (int) (edges.size() * this.pathFactor); // TODO get the % from config
		for (int i = 0 ; i < addtionalEdges; i++) {
			int pos = random.nextInt(edges.size());
			Edge edge = edges.get(pos);
			
			// TODO ensure that only non-used edges are selected (and doesn't increment the counter)
			// this would require a list of edges used, BUT first need to match up the edges from mst.edges and
			// param edges - their array indexes wouldn't align.
			INode node1 = nodes.get(edge.v);
			INode node2 = nodes.get(edge.w);
			if (node1.getType() != NodeType.END && node2.getType() != NodeType.END &&
					edgeCount[node1.getId()] < node1.getMaxDegrees() && edgeCount[node2.getId()] < node2.getMaxDegrees()) {
				paths.add(edge);
				edgeCount[node1.getId()]++;
				edgeCount[node2.getId()]++;				
			}

		}
		return paths;
	}

	/**
	 * 
	 * @param rooms
	 * @return
	 */
	public Map<Integer, IDungeonRoom> mapRooms(List<IDungeonRoom> rooms) {
		Map<Integer, IDungeonRoom> map = new HashMap<>();
		rooms.forEach(room -> {
			map.put(Integer.valueOf(room.getId()), room);
		});
		return map;
	}

	private List<Edge> triangulate(List<? extends INode> nodes) {
		/*
		 *  weight/cost array of all rooms
		 *  the indexes is a "map" to the nodes by their position in the list
		 */
        double[][] matrix = ILevelGenerator.getDistanceMatrix(nodes);
        
		/*
		 * maps all nodes by origin(x,y)
		 * this is required for the Delaunay Triangulation library because it only returns edges without any identifying properties, only points
		 */
		Map<String, INode> map = new HashMap<>();
        
        /*
		 * holds all nodes in Vector2D format.
		 * used for the Delaunay Triangulation library to calculate all the edges between nodes.
		 * 
		 */
		Vector<Vector2D> pointSet = new Vector<>();		
        
        /*
		 * holds all the edges that are produced from triangulation
		 */
		List<Edge> edges = new ArrayList<>();
		


		/**
		 * a flag to indicate that an edge leading to the "end" room is created
		 */
		boolean isEndEdgeMet = false;
		int endEdgeCount = 0;

		// map all main rooms by origin(x,y) and build all edges.
		for (INode node : nodes) {
			Coords2D origin = node.getOrigin();
			// map out the rooms by origin
			map.put(origin.getX() + ":" + origin.getY(), node);
			// convert coords into vector2d for triangulation
			Vector2D v = new Vector2D(origin.getX(), origin.getY());
//			LOGGER.debug(String.format("Room.id: %d = Vector2D: %s", node.getId(), v.toString()));
			pointSet.add(v);
		}

		// triangulate the set of points
		DelaunayTriangulator triangulator = null;
		try {
			triangulator = new DelaunayTriangulator(pointSet);
			triangulator.triangulate();
		}
		catch(NotEnoughPointsException e) {
			//LOGGER.warn("Not enough points where provided for triangulation. Level generation aborted.");
			return edges;
		}
		catch(Exception e) {
//			if (nodes !=null) {
//				LOGGER.debug("rooms.size=" + nodes.size());
//			}
//			else {
//				LOGGER.debug("Rooms is NULL!");
//			}
//			if (pointSet != null) {
//				LOGGER.debug("Pointset.size=" + pointSet.size());
//			}
//			else {
//				LOGGER.debug("Pointset is NULL!");
//			}			
			LOGGER.error("Unable to triangulate: ", e);
		}

		// retrieve all the triangles from triangulation
		List<Triangle2D> triangles = triangulator.getTriangles();

		for(Triangle2D triangle : triangles) {
			// locate the corresponding rooms from the points of the triangles
			INode node1 = map.get((int)triangle.a.x + ":" + (int)triangle.a.y);
			INode node2 = map.get((int)triangle.b.x + ":" + (int)triangle.b.y);
			INode node3 = map.get((int)triangle.c.x + ":" + (int)triangle.c.y);			

			// build an edge based on room distance matrix
			Edge edge = new Edge(node1.getId(), node2.getId(), matrix[node1.getId()][node2.getId()]/*matrix.get(ILevelGenerator.getKey(node1, node2))*/);
			
			// TODO for boss room, not necessarily end room
			// remove any edges that lead to the end room(s) if the end room already has one edge
			// remove (or don't add) any edges that lead to the end room if the end room already has it's maximum edges (degrees)
			if (node1.getType() != NodeType.END && node2.getType() != NodeType.END) {
//			if (!r1.getType().equals(Type.BOSS) && !r2.getType().equals(Type.BOSS)) {
				edges.add(edge);
			}
			else if (node1.getType() == NodeType.START || node2.getType()  == NodeType.START) {
				// skip if start joins the end
			}
			else if (!isEndEdgeMet) {
				// add the edge
				edges.add(edge);
				// increment the number of edges leading to the end room
				endEdgeCount++;
				// get the end room
				INode end = node1.getType() == NodeType.END ? node1 : node2;
				if (endEdgeCount >= end.getMaxDegrees()) {
					isEndEdgeMet = true;
				}
			}
			
			edge = new Edge(node2.getId(), node3.getId(), matrix[node2.getId()][node3.getId()]/*matrix.get(ILevelGenerator.getKey(node2, node3))*/);
			if (node2.getType() != NodeType.END && node3.getType() != NodeType.END) {
				edges.add(edge);
			}
			else if (node1.getType() == NodeType.START || node2.getType() == NodeType.START) {
				// skip
			}
			else if (!isEndEdgeMet) {
				edges.add(edge);
				isEndEdgeMet = true;
			}
			
			edge = new Edge(node1.getId(), node3.getId(), matrix[node1.getId()][node3.getId()]/*matrix.get(ILevelGenerator.getKey(node1, node3))*/);
			if (node1.getType() != NodeType.END && node3.getType() != NodeType.END) {
				edges.add(edge);
			}
			else if (node1.getType() == NodeType.START || node2.getType() == NodeType.START) {
				// skip
			}
			else if (!isEndEdgeMet) {
				edges.add(edge);
				isEndEdgeMet = true;
			}
		}
		return edges;
	}

	/**
	 * Returns a subset of rooms that meet the mean factor criteria. Start and End rooms are included as main rooms.
	 * Note that the original list is updated as well.
	 * @param rooms
	 * @param meanFactor
	 * @return
	 */
	public List<IDungeonRoom> selectMainRooms(List<IDungeonRoom> rooms, final double meanFactor) {
		List<IDungeonRoom> mainRooms = new ArrayList<>();
		int totalArea = 0;
		for (IDungeonRoom room : rooms) {
			totalArea += room.getBox().getWidth() * room.getBox().getHeight();
		}

		int meanArea = (int) totalArea / rooms.size();

		rooms.forEach(room -> {
			if (room.getType() == NodeType.START || room.getType() == NodeType.END || room.getBox().getWidth() * room.getBox().getHeight() > meanArea) {
				room.setRole(RoomRole.MAIN);
				mainRooms.add(room);
			}
		});
		
		return mainRooms;
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
	 */
	@Override
	public List<? super INode> findIntersections(INode node, List<? extends INode> nodes) {
		ArrayList<? super INode> intersections = new ArrayList<>();

		for (INode intersectingRect : nodes) {
			if (!((IDungeonRoom)node).equals(intersectingRect) && ((IDungeonRoom)intersectingRect).getBox().intersects(((IDungeonRoom)node).getBox())) {
				intersections.add(intersectingRect);
			}
		}
		return intersections;
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
		Rectangle2D boundingBox = new Rectangle2D(0, 0, spawnBoxWidth, spawnBoxHeight);	
		
		IDungeonRoom startRoom = generateRoom(random, centerPoint, boundingBox, minRoomSize, maxRoomSize);
		startRoom
			.setRole(RoomRole.MAIN)
			.setType(NodeType.START)
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
			.setRole(RoomRole.MAIN)	
			.setType(NodeType.END)
			.setId(rooms.size()); // TODO check if still works without +1
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
		
		int sizeX = maxRoomSize == minRoomSize ? minRoomSize : random.nextInt(maxRoomSize - minRoomSize) + minRoomSize;
		int sizeY = maxRoomSize == minRoomSize ? minRoomSize : random.nextInt(maxRoomSize - minRoomSize) + minRoomSize;
		int offsetX = (random.nextInt(boundingBox.getWidth()) - (boundingBox.getWidth()/2)) - (sizeX / 2);
		int offsetY = (random.nextInt(boundingBox.getHeight()) - (boundingBox.getHeight()/2)) - (sizeY / 2);
		
		IDungeonRoom room = new DungeonRoom(new Coords2D(centerPoint.getX() + offsetX, centerPoint.getY() + offsetY), sizeX, sizeY);
		return room;
	}

	/**
	 * perform a breadth first search against the list of edges to determine if a path exists
	 * from one node to another.
	 * @param start
	 * @param end
	 * @param nodes
	 * @param edges
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected boolean breadthFirstSearch(int start, int end, List<? extends INode> nodes, List<Edge> edges) {
		// build an adjacency list
		LinkedList<Integer> adj[];

		adj = new LinkedList[nodes.size()];
		for (INode r : nodes) {
			adj[r.getId()] = new LinkedList<>();
		}
		
        for (Edge e : edges) {
        	adj[e.v].add(e.w);
        	// add both directions to ensure all adjacencies are covered
        	adj[e.w].add(e.v);     	
        }

		// mark all the vertices as not visited(By default set as false)
		boolean visited[] = new boolean[nodes.size()];

		// create a queue for BFS
		LinkedList<Integer> queue = new LinkedList<Integer>();

		// mark the current node as visited and enqueue it
		visited[start]=true;
		queue.add(start);

		while (queue.size() != 0) {
			// Dequeue a vertex from queue and print it
			int s = queue.poll();

			// get all adjacent vertices of the dequeued vertex s
			// if a adjacent has not been visited, then mark it
			// visited and enqueue it
			Iterator<Integer> i = adj[s].listIterator();
			while (i.hasNext()) {
				int n = i.next();
				if (n == end) return true;
				
				if (!visited[n]) {
					visited[n] = true;
					queue.add(n);
				}
			}
		}		
		return false;
	}
	
	/**
	 * 
	 * @param workingRooms
	 * @return
	 */
//	public Coords2D getCenter(List<IDungeonRoom> workingRooms) {
//		IDungeonRoom boundingBox = getBoundingBox(workingRooms);
//		Coords2D center = boundingBox.getCenter();
//		return center;
//	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getSpawnBoxWidth() {
		return spawnBoxWidth;
	}
	
	public int getSpawnBoxHeight() {
		return spawnBoxHeight;
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
	
	public double getPathFactor() {
		return pathFactor;
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
	
	public DungeonLevelGenerator withSpawnBoxWidth(int width) {
		this.spawnBoxWidth = width;
		return this;
	}
	
	public DungeonLevelGenerator withSpawnBoxHeight(int height) {
		this.spawnBoxHeight = height;
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
	
	public DungeonLevelGenerator withPathFactor(double factor) {
		this.pathFactor = factor;
		return this;
	}
}
