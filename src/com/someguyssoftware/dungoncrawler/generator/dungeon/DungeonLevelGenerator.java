/**
 * 
 */
package com.someguyssoftware.dungoncrawler.generator.dungeon;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.someguyssoftware.dungoncrawler.generator.Coords2D;
import com.someguyssoftware.dungoncrawler.generator.ILevel;
import com.someguyssoftware.dungoncrawler.generator.ILevelGenerator;

/**
 * Overlapping rectangle code based on https://stackoverflow.com/questions/3265986/an-algorithm-to-space-out-overlapping-rectangles
 * @author Mark Gottschling on Sep 15, 2020
 *
 */
public class DungeonLevelGenerator implements ILevelGenerator {
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
		 
		// TODO init rooms taking info from controls - ex # of rooms, min width, max width, min depth, max depth, size of map, spawn boundary, etc.
		List<IDungeonRoom> rooms = initRooms(random);
		
		// TODO separate rooms
		rooms = separateRooms(rooms, movementFactor);
		
		// TODO select main rooms
		rooms = selectMainRooms(rooms, meanFactor);
		
		// TODO triangulate
		
		// TODO minimum spanning tree
		
		// TODO add extra edges
		
		// TODO add hallways
		
		// TODO reintroduce minor rooms that intersect with hallways
		

		cellMap = updateCellMap(cellMap, rooms);

		// save cell map and rooms to the level
		dungeonData.setCellMap(cellMap);
		dungeonData.setRooms(rooms);
		return dungeonData;
	}
	
	/**
	 * 
	 * @param rooms
	 * @param meanFactor
	 * @return
	 */
	public List<IDungeonRoom> selectMainRooms(List<IDungeonRoom> rooms, double meanFactor) {
		int totalX = 0;
		int totalY = 0;
		int totalArea = 0;
		for (IDungeonRoom room : rooms) {
//			totalX += room.getBox().getWidth();
//			totalY += room.getBox().getHeight();
			totalArea += room.getBox().getWidth() * room.getBox().getHeight();
		}
//		int meanX = (int) ((totalX / rooms.size()) * meanFactor);
//		int meanY =(int) ((totalY / rooms.size()) * meanFactor);
//		int meanArea = meanX*meanY;
		int meanArea = (int) totalArea / rooms.size();
		
//		System.out.printf("meanX=%s, meanY=%s, meanArea=%s", meanX, meanY, meanArea);
		System.out.printf("meanArea=%s\n", meanArea);
		rooms.forEach(room -> {
//			if (room.getBox().getWidth() > meanX
//					&& room.getBox().getHeight() > meanY) {
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
//				if (random.nextDouble() < chanceToStartSolid) {
					map[x][y] = true;
//				}
			}
		}
		return map;
	}

	private List<IDungeonRoom> initRooms(Random random) {
		return initRooms(random, width, height);
	}

	private List<IDungeonRoom> initRooms(Random random, final int width, final int height) {
		List<IDungeonRoom> rooms = new ArrayList<>();
		Coords2D centerPoint = new Coords2D(width/2, height / 2);

		
		// TODO need to add start room and flagged
		for (int roomIndex = 0; roomIndex < numberOfRooms; roomIndex++) {
			int sizeX = random.nextInt(maxRoomSize - minRoomSize) + minRoomSize;
			int sizeZ = random.nextInt(maxRoomSize - minRoomSize) + minRoomSize;
			int offsetX = (random.nextInt(30) - 15) - (sizeX / 2); // TODO make these values a constant somewhere or a passed in value
			int offsetZ = (random.nextInt(30) - 15) - (sizeZ / 2);

			DungeonRoom room = new DungeonRoom(new Coords2D(centerPoint.getX() + offsetX, centerPoint.getY() + offsetZ), sizeX, sizeZ);
			rooms.add(room);
		}
		return rooms;
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
