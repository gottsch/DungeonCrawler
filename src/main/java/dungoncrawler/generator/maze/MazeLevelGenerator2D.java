package dungoncrawler.generator.maze;

import dungoncrawler.generator.Coords2D;
import dungoncrawler.generator.IdGenerator;
import dungoncrawler.generator.Rectangle2D;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Consumer;

/**
 *
 * @author Mark Gottschling on Nov 8, 2023
 *
 */
public class MazeLevelGenerator2D {
    protected static final Logger LOGGER = LogManager.getLogger();

    ///////////// Default Constants ///////////////
    /*
     * dimensions of the level and rooms have to be odd in order
     * for proper spacing of passage/corridor/halls.
     */
    private static final int DEFAULT_WIDTH = 95;
    private static final int DEFAULT_HEIGHT = 95;
    private static final int DEFAULT_NUMBER_OF_ROOMS = 20;
    private static final int DEFAULT_MIN_SIZE = 7;
    private static final int DEFAULT_MAX_SIZE = 19;
    private static final int DEFAULT_MIN_DEGREES = 3;
    private static final int DEFAULT_MAX_DEGREES = 5;
    private static final double DEFAULT_MEAN_FACTOR = 1.15;
    private static final double DEFAULT_RUN_FACTOR = 0.8; // 0 = anywhere, 1 = tail
    private static final double DEFAULT_CURVE_FACTOR = 0.8; // 0 = curvy, 1 = straight
    private static final int DEFAULT_MAX_ATTEMPTS = 400;
    private static final int DEFAULT_MIN_CORRIDOR_SIZE = 250;
    private static final int DEFAULT_MAX_CORRIDOR_SIZE = 500;

    ////// convenience constants /////
    // initial state of tile in the grid
//    public static final byte ROCK = (byte) CellType.ROCK.getId();
//    // aka visited rock state
//    public static final byte WALL = (byte) CellType.WALL.getId();
//    public static final byte CONNECTOR = (byte) CellType.CONNECTOR.getId();
//    public static final byte DOOR = (byte) CellType.DOOR.getId();

    ////////////// generator properties ///////////////
    private int width = DEFAULT_WIDTH;
    private int height = DEFAULT_HEIGHT;
    private Rectangle2D levelBoundary = new Rectangle2D(0, 0, this.width, this.height);

    private int numberOfRooms = DEFAULT_NUMBER_OF_ROOMS;
    private int minSize = DEFAULT_MIN_SIZE;
    private int maxSize = DEFAULT_MAX_SIZE;
    private int minDegrees = DEFAULT_MIN_DEGREES;
    private int maxDegrees = DEFAULT_MAX_DEGREES;
    private int maxAttempts = DEFAULT_MAX_ATTEMPTS;
    private int minCorridorSize = DEFAULT_MIN_CORRIDOR_SIZE;
    private int maxCorridorSize = DEFAULT_MAX_CORRIDOR_SIZE;
    private double meanFactor = DEFAULT_MEAN_FACTOR;
    private double runFactor = DEFAULT_RUN_FACTOR;
    private double curveFactor = DEFAULT_CURVE_FACTOR;
    private Random random = new Random();

    /*
     * An object to generate and keep track of ids.
     */
    private final IdGenerator idGenerator = new IdGenerator(CellType.values().length + 1);

    /*
     * A list of all rooms that are supplied as input to the generator.
     */
    private List<IRoom2D> suppliedRooms;
    private IRoom2D startRoom;
    private IRoom2D endRoom;

    /*
     * working variables
     */
    private List<Connector2D> connectors;
    private Map<Integer, MazeRegion2D> regionMap;

    /**
     *
     * @param builder
     */
    public MazeLevelGenerator2D(Builder builder) {
        setWidth(builder.width);
        setHeight(builder.height);
        this.minSize = builder.minSize;
        this.maxSize = builder.maxSize;
        this.minDegrees = builder.minDegrees;
        this.maxDegrees = builder.maxDegrees;
        this.numberOfRooms = builder.numberOfRooms;
        this.maxAttempts = builder.attemptsMax;
        this.meanFactor = builder.meanFactor;
        this.runFactor = builder.runFactor;
        this.curveFactor = builder.curveFactor;

        this.startRoom = builder.startRoom;
        this.endRoom = builder.endRoom;

        if (builder.random != null) {
            this.random = builder.random;
        }
    }

    /**
     *
     * @return
     */
    public Optional<ILevel2D> generate() {
        // initialize values
        MazeLevel2D level = new MazeLevel2D(width, height);
        connectors = new ArrayList<>();
        regionMap = new HashMap<>();

        // TODO validate() <-- or this goes in the Builder.build()... maybe not as you can set individual properties in the generator too.
        isValidInitialProperties();

        // generateRooms() includes checking constraints as each room is added
        List<IRoom2D> rooms = generateRooms(level, this.meanFactor);

        // NOTE This is moot as rooms are filtered as they are added
        // filter some rooms out. if something like this is re-implemented, add it to generateRooms
//        rooms = removeRoomsBelowAreaMean(rooms, meanFactor);

        // map rooms to regions
        Map<Integer, MazeRegion2D> regions = mapRegions(rooms);
        setRegionMap(regions);

        // TODO should this be part of generateRooms() ie carve() updates the grid directly
        // load rooms into the grid
//        level.getGrid().add(rooms);

        // carve passages
        carve(level);

        // add connectors
        boolean isDiscoverSuccess = discoverConnectors(level);
        if (!isDiscoverSuccess) {
            return Optional.empty();
        }

        // merge regions
        mergeRegions(level, random);

        // back-fill dead-ends
        backFill(level);

        return Optional.of(level);
    }

    /**
     *
     */
    private boolean isValidInitialProperties() {
        // NOTE can't alter values of start, end and custom rooms - just fail

        // TODO if the room is outside the boundary, then fail

        // TODO all the rest

        return true;
    }

    /**
     * TODO figure out a way to seperate generating start, end, and rooms from this method
     * TODO this should be only random room collection. also don't update level from here
     * TODO OR all main method work on level directly, but then have to ensure that nothing is using
     * TODO the values from generator, ie generator.startRoom
     * @param level the level to add rooms to.
     * @param localMeanFactor the mean factor to compare room area against.
     * @return a list of generated rooms.
     */
    public List<IRoom2D> generateRooms(ILevel2D level, double localMeanFactor) {
        List<IRoom2D> rooms = new ArrayList<>();
        int roomCount = 0;
        idGenerator.reset();
//        int id = idGenerator.next();

        int totalArea = 0;
        int meanArea = 0;

        int localMinSize = minSize;
        int localMaxSize = maxSize;

        // calculate the random ranges
        int xRange = this.width - this.minSize;
        int yRange = this.height - this.minSize;

        // generate a start room
        IRoom2D startRoom = generateStartRoom(xRange, yRange, minSize, maxSize, levelBoundary, random);
        rooms.add(startRoom);
        level.setStartRoom(startRoom);

        // generate an end room
        IRoom2D endRoom = generateEndRoom(xRange, yRange, minSize, maxSize, levelBoundary, rooms, random);
        rooms.add(endRoom);
        level.setEndRoom(endRoom);

        // random rooms
        double consecutiveFailures = 0;
        for (int attemptCount = 0; attemptCount < maxAttempts; attemptCount++) {
            if (consecutiveFailures > 0 && (consecutiveFailures / maxAttempts) > 0.1) {
                // reduce the size of the mean by 10% (0.1)
                localMeanFactor = Math.max(0, localMeanFactor - 0.1);
                localMinSize = Math.max(5, localMinSize -1);
                localMaxSize = Math.max(localMinSize, localMaxSize - 3);
                consecutiveFailures = 0;
                LOGGER.debug("reducing mean to -> {}, maxSize -> {}", localMeanFactor, localMaxSize);
            }

            Optional<IRoom2D> roomOptional = generateRoom(xRange, yRange, localMinSize, localMaxSize, levelBoundary, minDegrees, maxDegrees, random);
            if (roomOptional.isEmpty()) {
                consecutiveFailures++;
                continue;
            }
            IRoom2D room = roomOptional.get();

            //ensure that the box doesn't overlap another existing box
            // NOTE this would be more efficient if using a Interval-BST
            if (hasIntersections(room.getBox(), rooms)) {
                consecutiveFailures++;
                continue;
            }

            // ensure that the box is greater that the area criteria
            if (roomCount > 1) {
                if (room.getWidth() * room.getHeight() < meanArea) {
                    consecutiveFailures++;
                    continue;
                }
            }
            room.setId(idGenerator.next());
            rooms.add(room);
            roomCount++;

            // add room's area to total
            totalArea += room.getBox().getWidth() * room.getBox().getHeight();
            meanArea = (int) ((totalArea / rooms.size()) * localMeanFactor);

            if (roomCount >= numberOfRooms) {
                LOGGER.debug("attemptCount -> {}", attemptCount);
                break;
            }
            consecutiveFailures = 0;
        }

        LOGGER.debug("roomCount -> {}, numberfOfRooms -> {}", roomCount, numberOfRooms);

        // update level
        level.setRooms(rooms);
        level.getGrid().add(rooms);

        return rooms;
    }

    private IRoom2D generateStartRoom(int xRange, int yRange, int minSize, int maxSize, Rectangle2D levelBoundary, Random random) {
        IRoom2D room = null;
        if (this.startRoom == null) {
            while (room == null) {
                Optional<IRoom2D> optionalRoom = generateRoom(xRange, yRange, minSize, maxSize, levelBoundary, 2, maxDegrees, random);
                room = optionalRoom.orElse(null);
                // TODO should return an optional
            }
            room.setStart(true);
        }
        // assign an id to the room (whether provided or generated)
        if (room != null) {
            room.setId(idGenerator.next());
        }
        return room;
    }

    private IRoom2D generateEndRoom(int xRange, int yRange, int minSize, int maxSize, Rectangle2D levelBoundary, List<IRoom2D> rooms, Random random) {
        IRoom2D room = null;
        boolean hasIntersections = false;
        if (this.endRoom == null) {
            while (room == null || hasIntersections) {
                Optional<IRoom2D> optionalRoom = generateRoom(xRange, yRange, minSize, maxSize, levelBoundary, 1, 1, random);
                room = optionalRoom.orElse(null);
                // TODO should return an optional
                // ensure endRoom doesn't overlap startRoom or any other room (though there shouldn't be any)
                if (room != null) {
                    hasIntersections = hasIntersections(room.getBox(), rooms);
                }
            }
            room.setEnd(true);
        }
        if (room != null) {
            room.setId((idGenerator.next()));
        }
        return room;
    }

    /**
     * NOTE this method does not assign an id to a room.
     * @param xRange
     * @param yRange
     * @param minSize
     * @param maxSize
     * @param boundary
     * @param minDegrees
     * @param maxDegrees
     * @param random
     * @return
     */
    public Optional<IRoom2D> generateRoom(int xRange, int yRange, int minSize, int maxSize,
                  Rectangle2D boundary, int minDegrees, int maxDegrees, Random random) {

        Optional<IRoom2D> result = Optional.empty();

        int x = random.nextInt(xRange);
        int y = random.nextInt(yRange);
        // ensure x and y are even numbers (because the map is 0-indexed ie. a wall will always be on 0)
        if (x % 2 != 0 || y % 2 != 0) {
            return result;
        }
        int xSize = maxSize == minSize ? minSize : random.nextInt(maxSize - minSize) + minSize;
        int ySize = maxSize == minSize ? minSize : random.nextInt(maxSize - minSize) + minSize;

        // ensure that xSize and ySize are odd lengths
        if (xSize % 2 != 1) {
            xSize++;
        }
        if (ySize % 2 != 1) {
            ySize++;
        }
        // ensure that the box is within the level boundaries
        Rectangle2D box = new Rectangle2D(x, y, xSize, ySize);
        if (!isWithinBoundary(box, boundary)) {
            return result;
        }

        // calculate degrees
        int degrees = (maxDegrees == minDegrees) ? minDegrees : random.nextInt(maxDegrees - minDegrees) + minDegrees;

        IRoom2D room = new MazeRoom2D(box);
        room.setDegrees(degrees);

        return Optional.of(room);
    }

    /**
     *
     * @param rooms
     * @param meanFactor
     * @return
     */
    public List<IRoom2D> removeRoomsBelowAreaMean(List<IRoom2D> rooms, final double meanFactor) {
        List<IRoom2D> mainRooms = new ArrayList<>();
        int totalArea = 0;
        for (IRoom2D room : rooms) {
            totalArea += room.getBox().getWidth() * room.getBox().getHeight();
        }

        int meanArea = (int) ((totalArea / rooms.size()) * meanFactor);

        // process each room
        rooms.forEach(room -> {
            if (room.getWidth() * room.getHeight() >= meanArea) {
                mainRooms.add(room);
            }
        });

        return mainRooms;
    }

    /**
     *
     * @param rooms
     * @return
     */
    public Map<Integer, MazeRegion2D> mapRegions(List<IRoom2D> rooms) {
        Map<Integer, MazeRegion2D> regions = new HashMap<>();
        rooms.forEach(room -> {
            MazeRegion2D region = new MazeRegion2D(room.getId(), room.getBox());
            region.setType(MazeRegionType.ROOM);
            regions.put(room.getId(), region);
        });
        return regions;
    }

    private boolean isWithinBoundary(Rectangle2D boundary, IRoom2D room) {
        return isWithinBoundary(boundary, room.getBox());
    }

    private boolean isWithinBoundary(Rectangle2D room, Rectangle2D boundary) {
        return room.getMaxX() <= boundary.getMaxX() && room.getMinX() >= boundary.getMinX()
                && room.getMaxY() <= boundary.getMaxY()
                && room.getMinY() >= boundary.getMinY();
    }

    /**
     *
     * @param room
     * @param rooms
     * @return
     */
    public boolean hasIntersections(Rectangle2D room, List<IRoom2D> rooms) {

        for (IRoom2D intersectingRect : rooms) {
            if (!room.equals(intersectingRect.getBox()) && intersectingRect.getBox().intersects(room)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param level
     */
    public void carve(ILevel2D level) {
        // scan all cells for solid rock
        // NOTE skip border cells as they are "walls"
        for (int x = 1; x < level.getWidth()-1; x+=2) {
            for (int y = 1; y < level.getHeight()-1; y+=2) {
                // find an unvisited rock
                if (level.getGrid().get(x, y).getType() == CellType.ROCK) {
                    // add cell to the active list
                    prims(level, new Coords2D(x, y));
                }
            }
        }
    }

    /**
     *
     * @param level
     */
    public boolean discoverConnectors(ILevel2D level) {

        // scan all rooms to see if they already have doorways
        List<Integer> ignoreIds = new ArrayList<>();
        boolean customConnectorsSuccess = true;
        for(IRoom2D room : level.getRooms()) {
            if (!room.getDoorways().isEmpty()) {
                // add doorways to connectors
                for(Coords2D door : room.getDoorways()) {
                    int x = door.getX();
                    int y = door.getY();
                    customConnectorsSuccess &= generateConnector(level, connectors, x, y);
                }
                // add room/region id to list so they aren't re-processed
                ignoreIds.add(room.getId());
            }
        }

        if (!customConnectorsSuccess) {
            LOGGER.warn("failed to merge custom room to the level.");
            return false;
        }

        // scan all cells for
        // 1. wall
        // 2. adjacent to two regions of different ids.
        // NOTE skip border cells as they will not have connectors
        for (int x = 1; x < level.getWidth()-1; x++) {
            for (int y = 1; y < level.getHeight()-1; y++) {
                // find any wall
                if (level.getGrid().get(x, y).getType() == CellType.WALL) {
                    generateConnector(level, connectors, x, y, ignoreIds);
                }
            }
        }

        return true;
    }

    private boolean generateConnector(ILevel2D level, List<Connector2D> connectors, int x, int y) {
        return generateConnector(level, connectors, x, y, null);
    }

    /**
     *
     * @param level
     * @param x
     * @param y
     */
    private boolean generateConnector(ILevel2D level, List<Connector2D> connectors, int x, int y, List<Integer> ignoreIds) {
        int northId = level.getGrid().get(x, y-1).getRegionId();
        int southId = level.getGrid().get(x, y+1).getRegionId();
        // test east and west
        int eastId = level.getGrid().get(x+1, y).getRegionId();
        int westId = level.getGrid().get(x-1, y).getRegionId();

        if (ignoreIds != null && (ignoreIds.contains((int)northId) || ignoreIds.contains((int)southId) ||
                ignoreIds.contains((int)eastId) || ignoreIds.contains((int)westId))) {
            return false;
        }

        if (northId >= idGenerator.getStart() && southId >= idGenerator.getStart() && northId != southId) {
            // get regions
            MazeRegion2D region1 = getRegionMap().get((int)northId);
            MazeRegion2D region2 = getRegionMap().get((int)southId);

            // test that x,y is valid position on the x-axis (east-west) ie away from corners
            if (region1.getType() == MazeRegionType.ROOM) {
                if (x < region1.getBox().getMinX() + 2 || x > region1.getBox().getMaxX() - 2) {
                    return false;
                }
            }
            if (region2.getType() == MazeRegionType.ROOM) {
                if (x < region2.getBox().getMinX() + 2 || x > region2.getBox().getMaxX() - 2) {
                    return false;
                }
            }

            Connector2D connector = new Connector2D(x, y, region1, region2);
            // add connector to list
            connectors.add(connector);
            // update grid with id = CONNECTOR
            level.getGrid().get(connector.getCoords()).setType(CellType.CONNECTOR);
        }
        else if (eastId >= idGenerator.getStart() && westId >= idGenerator.getStart() && eastId != westId) {
            MazeRegion2D region1 = getRegionMap().get((int)eastId);
            MazeRegion2D region2 = getRegionMap().get((int)westId);

            if (region1.getType() == MazeRegionType.ROOM) {
                if (y < region1.getBox().getMinY() + 2 || y > region1.getBox().getMaxY() - 2) {
                    return false;
                }
            }
            if (region2.getType() == MazeRegionType.ROOM) {
                if (y < region2.getBox().getMinY() + 2 || y > region2.getBox().getMaxY() - 2) {
                    return false;
                }
            }

            Connector2D connector = new Connector2D(x, y, region1, region2);
            connectors.add(connector);
            level.getGrid().get(connector.getCoords()).setType(CellType.CONNECTOR);
        }
        return true;
    }

    /**
     * also need to add the selected connectors/doors to the Room list of doors
     * @param level
     */
    public void mergeRegions(ILevel2D level, Random random) {

        // build a map of rooms
        Map<Integer, IRoom2D> roomMap = new HashMap<>();
        level.getRooms().forEach(r -> {
            roomMap.put(Integer.valueOf(r.getId()), r);
        });

        List<Connector2D> connectors = new ArrayList<>();

        while (!getConnectors().isEmpty()) {
            connectors.clear();
            // randomly select a connector
            Connector2D connector = getConnectors().get(random.nextInt(getConnectors().size()));

            /*
             * gather all connectors that match the regions
             */
            // scan list for start room connectors
            getConnectors().forEach(c -> {
                if ((Objects.equals(c.getRegion1().getId(), connector.getRegion1().getId())
                        && Objects.equals(c.getRegion2().getId(), connector.getRegion2().getId()))
                || (Objects.equals(c.getRegion1().getId(), connector.getRegion2().getId())
                        && Objects.equals(c.getRegion2().getId(), connector.getRegion1().getId()))) {
                    connectors.add(c);
                }
            });

            /*
             * create a door from selected connector
             */
            addDoor(level, connector, roomMap);

            // mark regions as merged (not sure if this serves a purpose yet)
            connector.getRegion1().setMerged(true);
            connector.getRegion2().setMerged(true);

            /*
             * cull extra connectors
             */
            List<Connector2D> mainDoorAdjacents = selectAdjacentConnectors(connector, connectors);
            connectors.removeAll(mainDoorAdjacents);
            // remove initial connector from level and working list
            getConnectors().removeAll(mainDoorAdjacents);
            mainDoorAdjacents.remove(connector);
            mainDoorAdjacents.forEach(adjacent -> {
                level.getGrid().get(adjacent.getCoords()).setType(CellType.WALL);
                getConnectors().remove(adjacent);
            });

            // for each of the remaining connectors in the working list
            List<Connector2D> ignoreList = new ArrayList<>();
            int connectorCount = 1;
            for(Connector2D c : connectors) {
                IRoom2D room1 = roomMap.get(c.getRegion1().getId());
                IRoom2D room2 = roomMap.get(c.getRegion2().getId());
                int degrees = (room1 != null && room2 != null) ? Math.min(room1.getDegrees(), room2.getDegrees())
                        : room1 == null ? room2 == null ? maxDegrees : room2.getDegrees() : room1.getDegrees();

                if (random.nextDouble() > 0.965 && !ignoreList.contains(c) && connectorCount < degrees) {
                    // build extra door
                    addDoor(level, c, roomMap);
                    // replace the adjacents with walls
                    List<Connector2D> adjacents = selectAdjacentConnectors(c, connectors);
                    adjacents.remove(c);
                    adjacents.forEach(c2 -> {
                        level.getGrid().get(c2.getCoords()).setType(CellType.WALL);
                        getConnectors().remove(c2);
                    });
                    // add all the connectors to a temporary remove list so as to not process again
                    ignoreList.addAll(adjacents);
                    connectorCount++;
                } else {
                    // replace with wall
                    level.getGrid().get(c.getCoords()).setType(CellType.WALL);
                }
                // remove connector from level
                getConnectors().remove(c);
            }
        }
    }

    /**
     *
     * @param connector
     * @param connectors
     * @return
     */
    public List<Connector2D> selectAdjacentConnectors(Connector2D connector, List<Connector2D> connectors) {
        List<Connector2D> removeList = new ArrayList<>(connectors.stream()
                .filter(c -> (c.getCoords().getX() == connector.getCoords().getX() && c.getCoords().getY() == connector.getCoords().getY() + 1)
                || (c.getCoords().getX() == connector.getCoords().getX() && c.getCoords().getY() == connector.getCoords().getY() - 1)
                || (c.getCoords().getX() == connector.getCoords().getX() + 1 && c.getCoords().getY() == connector.getCoords().getY())
                || (c.getCoords().getX() == connector.getCoords().getX() - 1 && c.getCoords().getY() == connector.getCoords().getY()))
                .toList());
        removeList.add(connector);
        return removeList;
    }

    /**
     *
     * @param level
     */
    public void backFill(ILevel2D level) {
        // scan all cells for solid rock
        // NOTE skip border cells as they are "walls"
        for (int x = 1; x < level.getWidth()-1; x+=2) {
            for (int y = 1; y < level.getHeight()-1; y+=2) {
                // process at coords
                backFill(level, new Coords2D(x, y));
            }
        }
    }

    /**
     *
     * @param level
     * @param startingCoords
     */
    private void backFill(ILevel2D level, Coords2D startingCoords) {
        Coords2D active = startingCoords;
        List<CellType> elements = Arrays.asList(CellType.ROCK, CellType.WALL);

        while (active != null) {
            int wallCount = 0;
            Coords2D next = null;

            // test for 3 wall
            if (elements.contains(level.getGrid().get(active.getX(), active.getY()-1).getType())) {
                wallCount++;
            }
            else {
                next = new Coords2D(active.getX(), active.getY()-1);
            }
            if (elements.contains(level.getGrid().get(active.getX(), active.getY()+1).getType())) {
                wallCount++;
            }
            else {
                next = new Coords2D(active.getX(), active.getY()+1);
            }
            if (elements.contains(level.getGrid().get(active.getX()+1, active.getY()).getType())) {
                wallCount++;
            }
            else {
                next = new Coords2D(active.getX()+1, active.getY());
            }
            if (elements.contains(level.getGrid().get(active.getX()-1, active.getY()).getType())) {
                wallCount++;
            }
            else {
                next = new Coords2D(active.getX()-1, active.getY());
            }

            if (wallCount >= 3) {
                // update level with WALL
                level.getGrid().get(active).setType(CellType.ROCK);

                // move to the tile in the open direction
                active = next;
            }
            else {
                active = null;
            }
        }
    }

    /**
     *
     * @param level
     * @param connector
     * @param roomMap
     */
    private void addDoor(ILevel2D level, Connector2D connector, Map<Integer, IRoom2D> roomMap) {
        level.getGrid().get(connector.getCoords()).setType(CellType.DOOR);
        // update the rooms
        IRoom2D room = roomMap.get(connector.getRegion1().getId());
        if (room != null) {
            room.getDoorways().add(connector.getCoords());
        }
        room = roomMap.get(connector.getRegion2().getId());
        if (room != null) {
            room.getDoorways().add(connector.getCoords());
        }
    }

    /**
     *
     * @param level
     * @param startCoords
     */
    private void prims(ILevel2D level, Coords2D startCoords) {
        List<PrimsTile2D> activeList = new ArrayList<>();
        Map<Direction2D, PrimsTile2D> neighbors = new HashMap<>();
        Map<Direction2D, PrimsTile2D> passages = new HashMap<>();
        int maxRun = random.nextInt(maxCorridorSize - minCorridorSize) + minCorridorSize;
        // create tile
        PrimsTile2D tile = new PrimsTile2D(startCoords, Direction2D.SOUTH);

        // add tile to activeList
        activeList.add(tile);

        // create a region
        MazeRegion2D region = new MazeRegion2D(idGenerator.next());
        region.setType(MazeRegionType.CORRIDOR);

        // add tile to region
//        region.addTile(tile);

        // add region to the level
        getRegionMap().put(region.getId(), region);

        // update the grid with the active region
        level.getGrid().get(tile.getCoords()).setType(CellType.CORRIDOR);
        level.getGrid().get(tile.getCoords()).setRegionId(region.getId());

        int runCount = 0;
        while(!activeList.isEmpty()) {
            // randomly select a cell from the active list
            PrimsTile2D active = null;
            if (random.nextDouble() < runFactor) {
                active = activeList.get(activeList.size()-1);
            } else {
                active = activeList.get(random.nextInt(activeList.size()));
            }

            // init temporary variables
            neighbors.clear();
            passages.clear();

            // TODO how was -1, -1 array index prevented before ???
            // get neighbors of active
            if (level.getGrid().get(active.getX(), active.getY()-1).getType() == CellType.ROCK) {
                if (level.getGrid().get(active.getX(), active.getY()-2).getType() == CellType.ROCK) {
                    neighbors.put(Direction2D.NORTH, new PrimsTile2D(active.getX(), active.getY()-2, Direction2D.NORTH));
                    passages.put(Direction2D.NORTH, new PrimsTile2D(active.getX(), active.getY()-1, Direction2D.NORTH));
                } else {
                    // ineligible space for neighbor, make the in-between as wall
                    level.getGrid().get(active.getX(), active.getY()-1).setType(CellType.WALL);
                }

            }
            if (level.getGrid().get(active.getX(), active.getY()+1).getType() == CellType.ROCK) {
                if (level.getGrid().get(active.getX(), active.getY()+2).getType() == CellType.ROCK) {
                    neighbors.put(Direction2D.SOUTH, new PrimsTile2D(active.getX(), active.getY() + 2, Direction2D.SOUTH));
                    passages.put(Direction2D.SOUTH, new PrimsTile2D(active.getX(), active.getY() + 1, Direction2D.SOUTH));
                } else {
                    level.getGrid().get(active.getX(), active.getY()+1).setType(CellType.WALL);
                }
            }
            if (level.getGrid().get(active.getX()+1, active.getY()).getType() == CellType.ROCK) {
                if (level.getGrid().get(active.getX()+2, active.getY()).getType() == CellType.ROCK) {
                    neighbors.put(Direction2D.EAST, new PrimsTile2D(active.getX()+2, active.getY() , Direction2D.EAST));
                    passages.put(Direction2D.EAST, new PrimsTile2D(active.getX()+1, active.getY(), Direction2D.EAST));
                } else {
                    level.getGrid().get(active.getX()+1, active.getY()).setType(CellType.WALL);
                }
            }
            if (level.getGrid().get(active.getX()-1, active.getY()).getType() == CellType.ROCK) {
                if (level.getGrid().get(active.getX()-2, active.getY()).getType() == CellType.ROCK) {
                    neighbors.put(Direction2D.WEST, new PrimsTile2D(active.getX() - 2, active.getY(), Direction2D.WEST));
                    passages.put(Direction2D.WEST, new PrimsTile2D(active.getX() - 1, active.getY(), Direction2D.WEST));
                } else {
                    level.getGrid().get(active.getX()-1, active.getY()).setType(CellType.WALL);
                }
            }

            if (neighbors.isEmpty()) {
                activeList.remove(active);
                continue;
            }

            PrimsTile2D selected = null;
            PrimsTile2D passage = null;
            // if available pick the same direction 80% of time
            if (random.nextDouble() < curveFactor && neighbors.containsKey(active.getDirection())) {
                // move in the same direction as last time
                selected = neighbors.get(active.getDirection());
                selected.setDirection(active.getDirection());
                passage = passages.get(active.getDirection());
            }
            else {
                // randomly select a direction
                List<Direction2D> directions = neighbors.keySet().stream().toList();
                Direction2D direction = directions.get(random.nextInt(directions.size()));
                if (direction != null) {
                    selected = neighbors.get(direction);
                    selected.setDirection(direction);
                    passage = passages.get(direction);
                }
            }

            if (selected == null) {
                activeList.remove(active);
                continue;
            }

            activeList.add(selected);

            // add neighbor and passage to region
//            region.addTile(selected);
//            region.addTile(passage);

            // update grid with id of region
            level.getGrid().get(selected.getCoords()).setRegionId(region.getId());
            level.getGrid().get(selected.getCoords()).setType(CellType.CORRIDOR);

            level.getGrid().get(passage.getCoords()).setRegionId(region.getId());
            level.getGrid().get(passage.getCoords()).setType(CellType.CORRIDOR);

            // update sides of passage perpendicular to direction needs to turn into wall
            switch(passage.getDirection()) {
                case NORTH, SOUTH -> {
                    level.getGrid().get(passage.getX()-1, passage.getY()).setType(CellType.WALL);
                    level.getGrid().get(passage.getX()+1, passage.getY()).setType(CellType.WALL);
                }
                case EAST, WEST -> {
                    level.getGrid().get(passage.getX(), passage.getY()-1).setType(CellType.WALL);
                    level.getGrid().get(passage.getX(), passage.getY()+1).setType(CellType.WALL);
                }
            }

            runCount++;
            if (runCount > maxRun) {
                activeList.clear();
            }
        }
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
        setLevelBoundary(new Rectangle2D(0, 0, width, height));
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
        setLevelBoundary(new Rectangle2D(0, 0, width, height));
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public IRoom2D getStartRoom() {
        return startRoom;
    }

    public void setStartRoom(IRoom2D startRoom) {
        this.startRoom = startRoom;
    }

    public IRoom2D getEndRoom() {
        return endRoom;
    }

    public void setEndRoom(IRoom2D endRoom) {
        this.endRoom = endRoom;
    }

    public int getNumberOfRooms() {
        return numberOfRooms;
    }

    public void setNumberOfRooms(int numberOfRooms) {
        this.numberOfRooms = numberOfRooms;
    }

    public double getCurveFactor() {
        return curveFactor;
    }

    public void setCurveFactor(double curveFactor) {
        this.curveFactor = curveFactor;
    }

    public double getRunFactor() {
        return runFactor;
    }

    public void setRunFactor(double runFactor) {
        this.runFactor = runFactor;
    }

    public Rectangle2D getLevelBoundary() {
        return levelBoundary;
    }

    private void setLevelBoundary(Rectangle2D levelBoundary) {
        this.levelBoundary = levelBoundary;
    }

    public int getMinCorridorSize() {
        return minCorridorSize;
    }

    public void setMinCorridorSize(int minCorridorSize) {
        this.minCorridorSize = minCorridorSize;
    }

    public int getMaxCorridorSize() {
        return maxCorridorSize;
    }

    public void setMaxCorridorSize(int maxCorridorSize) {
        this.maxCorridorSize = maxCorridorSize;
    }

    public List<Connector2D> getConnectors() {
        return connectors;
    }

    private void setConnectors(List<Connector2D> connectors) {
        this.connectors = connectors;
    }

    public Map<Integer, MazeRegion2D> getRegionMap() {
        return regionMap;
    }

    private void setRegionMap(Map<Integer, MazeRegion2D> regionMap) {
        this.regionMap = regionMap;
    }

    //////////////////////////////////////////////////////////////////
    public static class Builder {
        public int width = DEFAULT_WIDTH;
        public int height = DEFAULT_HEIGHT;
        public int minSize = DEFAULT_MIN_SIZE;
        public int maxSize = DEFAULT_MAX_SIZE;
        public int minDegrees = DEFAULT_MIN_DEGREES;
        public int maxDegrees = DEFAULT_MAX_DEGREES;
        public int numberOfRooms = DEFAULT_NUMBER_OF_ROOMS;
        public int minCorridorSize = DEFAULT_MIN_CORRIDOR_SIZE;
        public int maxCorridorSize = DEFAULT_MAX_CORRIDOR_SIZE;
        public int attemptsMax = DEFAULT_MAX_ATTEMPTS;
        public double meanFactor = DEFAULT_MEAN_FACTOR;
        public double runFactor = DEFAULT_RUN_FACTOR;
        public double curveFactor = DEFAULT_CURVE_FACTOR;

        public IRoom2D startRoom;
        public IRoom2D endRoom;
        public Random random;

        public Builder with(Consumer<Builder> builder) {
            builder.accept(this);
            return this;
        }

        public MazeLevelGenerator2D build() {
            return new MazeLevelGenerator2D(this);
        }
    }
}
