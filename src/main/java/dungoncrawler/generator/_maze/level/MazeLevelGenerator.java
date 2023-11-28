package dungoncrawler.generator._maze.level;

import dungoncrawler.generator.Coords2D;
import dungoncrawler.generator.Rectangle2D;
import dungoncrawler.generator._maze.level.builder.*;
import dungoncrawler.generator._maze.room.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class MazeLevelGenerator implements IMazeLevelGenerator, ISetupBuilder, IRoomsBuilder {
    protected static final Logger LOGGER = LogManager.getLogger();

    private static final int DEFAULT_WIDTH = 96;
    private static final int DEFAULT_HEIGHT = 96;
    private static final int DEFAULT_SPAWN_WIDTH = 48;
    private static final int DEFAULT_SPAWN_HEIGHT = 48;
    private static final int DEFAULT_NUMBER_OF_ROOMS = 20;
    private static final int DEFAULT_MIN_SIZE = 7;
    private static final int DEFAULT_MAX_SIZE = 19;
    private static final int DEFAULT_MIN_DEGREES = 3;
    private static final int DEFAULT_MAX_DEGREES = 5;
    private static final int DEFAULT_NUMBER_OF_SEEDS = 5;
    private static final double DEFAULT_MEAN_FACTOR = 1.15;

    private static final Coords2D DEFAULT_CENTER_COORDS = new Coords2D(DEFAULT_WIDTH, DEFAULT_HEIGHT);

    private int width = DEFAULT_WIDTH;
    private int height = DEFAULT_HEIGHT;
    private int spawnWidth = DEFAULT_SPAWN_WIDTH;
    private int spawnHeight = DEFAULT_SPAWN_HEIGHT;
    private Coords2D spawnCoords = new Coords2D(DEFAULT_SPAWN_HEIGHT, DEFAULT_SPAWN_WIDTH);
    private SpawnPosition spawnPosition = SpawnPosition.NONE;
    private Random random = new Random();

    private int numberOfRooms = DEFAULT_NUMBER_OF_ROOMS;

    private int minSize = DEFAULT_MIN_SIZE;
    private int maxSize = DEFAULT_MAX_SIZE;
    private int minDegrees = DEFAULT_MIN_DEGREES;
    private int maxDegrees = DEFAULT_MAX_DEGREES;
    private int numberOfSeeds = DEFAULT_NUMBER_OF_SEEDS;

    private int movementFactor = 1;
    private double meanFactor = DEFAULT_MEAN_FACTOR;

    // TODO ensure to update this when width/height are mutated
    private Rectangle2D levelBoundary = new Rectangle2D(0, 0, this.width, this.height);

//    private Coords2D centerCoords = DEFAULT_CENTER_COORDS;

    /*
     * A list of all rooms that are supplied as input to the generator.
     */
    private List<IMazeRoom> suppliedRooms;
    private IMazeRoom startRoom;
    private IMazeRoom endRoom;

    /**
     * Example usage:
     * MazeLevelGenerator.create()
     *  .spawnRegion(30, 30)
     *  .spawnLocation(center)
     *  .next()
     *  .rooms(this) // this could be implicit in the rooms() method. ie inside call: this.callback = this;
     *      .addSet() // creates a RoomRandomizer
     *          .amount(15)
     *          .minSize(7)
     *          .maxSize(19)
     *          .degrees(3,5)
     *          .build()
     *       )
     *      .add(new StartRoom(15, 15, fixed)) || .startRoom().width(15).depth(15).structure(location).fixed(true).coords(x, y, z).build()
     *      .add(new EndRoom(10, 10))
     *      .seeds(5)
     *  .build()
     */
    private MazeLevelGenerator() {}

    private MazeLevelGenerator(int width, int depth) {
        this.width = width;
        this.height = depth;
    }

    /**
     * main workhorse to generate/build the level
     * @return
     */
    public IMazeLevel generate() {
        CellData[][] cellMap= new CellData[this.width][this.height];
        // TODO everything

        // create the working list of rooms
        List<IMazeRoom> rooms = generateRooms();

        // TODO ensure that the start and end rooms never get kicked out
        // have then stop at the boundary and become FIXED.
        // space the rooms out
        rooms = separateRooms(rooms, movementFactor);

        // remove/trim rooms outside boundary
        rooms = checkConstraints(rooms);

        // select main rooms
        List<IMazeRoom> mainRooms = selectMainRooms(rooms, meanFactor);

        // NOTE don't need to reorder ids since not doing triangulation
        // NOTE don't do triangulation, edges, path, waylines

        // TODO write room regions to cell map
        /// START MAZE CODE //



        // TEMP - json output of generator state
//        LOGGER.debug("generator state -> {}", this);
//        ObjectMapper mapper = new ObjectMapper();
//        try {
//            String generatorJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
//            LOGGER.debug("generator json -> {}", generatorJson);
//        } catch(Exception e) {
//            LOGGER.warn("unable to convert MazeLevelGenerator to JSON.");
//        }
        IMazeLevel level = new MazeLevel();
        level.setRooms(mainRooms);
        return level;
    }

    /**
     *
     * @return
     */
    List<IMazeRoom> generateRooms() {
        List<IMazeRoom> rooms = new ArrayList<>();

        Coords2D centerCoords = getCenterCoords();

        // TODO determine spawnCenterPoint ie. if user sets the spawn location or position
        Coords2D spawnCenterCoords = centerCoords;

        int id = 1;

        // determine spawn box
        Rectangle2D spawnBoundingBox = new Rectangle2D(0, 0, this.spawnWidth, this.spawnHeight);

        // test for start room
        if (this.startRoom == null) {
            // generate a start room
            IMazeRoom startRoom = generateRoom(this.random, spawnCenterCoords, spawnBoundingBox, this.minSize, this.maxSize);
            startRoom.setType(RoomType.START);
            startRoom.setRole(RoomRole.MAIN);
            startRoom.getFlags().add(RoomFlag.NO_INTERSECTION);
            startRoom.setId(id);
        }
        rooms.add(this.startRoom);
        id = this.startRoom.getId() + 1;

        // add the supplied rooms
        if (!this.suppliedRooms.isEmpty()) {
            for (IMazeRoom room : suppliedRooms) {
                room.setId(id++);
                // TODO add validation checks on all other properties
                rooms.add(room);
            }
        }

        // add random rooms
        if (this.numberOfRooms > 0) {
            for (int i = 0; i < this.numberOfRooms; i++) {
                IMazeRoom room = generateRoom(this.random, spawnCenterCoords, spawnBoundingBox, this.minSize, this.maxSize);
                // set the id
                room.setId(id++);
                room.setType(RoomType.STANDARD);
                room.setRole(RoomRole.MAIN);
                // randomly select a room to disallow intersetions
                if (random.nextInt(100) < 25) {
                    room.getFlags().add(RoomFlag.NO_INTERSECTION);
                }
                rooms.add(room);
            }
        }
        // add seeds
        if (this.numberOfSeeds > 0) {
            for (int i = 0; i < this.numberOfSeeds; i++) {
                IMazeRoom room = generateRoom(random, spawnCenterCoords, spawnBoundingBox, 5, 5); // TODO 5 -> make variable
                // constrainsts check

                // set the id
                room.setId(id++);
                room.getFlags().add(RoomFlag.SEED);

                // add to list
                rooms.add(room);
                LOGGER.debug("seed room id -> {}, size -> {}", room.getId(), room.getBox());
            }
        }

        // test for end room
        if (this.endRoom == null) {
            IMazeRoom endRoom = generateRoom(this.random, spawnCenterCoords, spawnBoundingBox, this.minSize, this.maxSize);
            endRoom.setType(RoomType.END);
            endRoom.setRole(RoomRole.MAIN);
            endRoom.getFlags().add(RoomFlag.NO_INTERSECTION);
        }
        // update the id
        endRoom.setId(rooms.size() + 1);
        rooms.add(this.endRoom);

        // generate the rooms according to supplied properties

        return rooms;
    }

    /**
     *
     * @return
     */
    private Coords2D getCenterCoords() {
        return new Coords2D(this.width / 2, this.height / 2);
    }

    private Coords2D getSpawnCoords() {
        return this.spawnCoords;
    }

    /**
     *
     * @param random
     * @param centerPoint
     * @param boundingBox
     * @param minRoomSize
     * @param maxRoomSize
     * @return
     */
    private IMazeRoom generateRoom(Random random, Coords2D centerPoint, Rectangle2D boundingBox, int minRoomSize,
                                   int maxRoomSize) {
        int sizeX = maxRoomSize == minRoomSize ? minRoomSize : random.nextInt(maxRoomSize - minRoomSize) + minRoomSize;
        int sizeY = maxRoomSize == minRoomSize ? minRoomSize : random.nextInt(maxRoomSize - minRoomSize) + minRoomSize;
        Coords2D offset = getRoomOffset(random, boundingBox, sizeX, sizeY);
        IMazeRoom room = new MazeRoom(new Coords2D(centerPoint.getX() + offset.getX(), centerPoint.getY() + offset.getY()), sizeX, sizeY);
        return room;
    }

    /**
     *
     * @param random
     * @param boundingBox
     * @param width
     * @param height
     * @return
     */
    private Coords2D getRoomOffset(Random random, Rectangle2D boundingBox, int width, int height) {
        int offsetX = (random.nextInt(boundingBox.getWidth()) - (boundingBox.getWidth() / 2)) - (width / 2);
        int offsetY = (random.nextInt(boundingBox.getHeight()) - (boundingBox.getHeight() / 2)) - (height / 2);
        return new Coords2D(offsetX, offsetY);
    }

    /**
     *
     * @param sourceRooms
     * @param movementFactor dictates how much to move a room at a time. lower number
     * mean the rooms will be closer together but more iterations performed
     * @return
     */
    public List<IMazeRoom> separateRooms(List<IMazeRoom> sourceRooms, int movementFactor) {
        // duplicate the soure rooms
        List<IMazeRoom> workingRooms = new ArrayList<>(sourceRooms);

        // find the center C of the bounding box of all the rooms.
        Coords2D center = getRoomsCenter(workingRooms);

        boolean hasIntersections = true;
        while (hasIntersections) {
            hasIntersections = iterateSeparationStep(center, workingRooms, movementFactor);
        }
        return workingRooms;
    }

    /**
     *
     * @param rooms
     * @return
     */
    public boolean iterateSeparationStep(Coords2D center, List<IMazeRoom> rooms, int movementFactor) {
        boolean hasIntersections = false;
        int totalMovement = 0;

        for (IMazeRoom room : rooms) {
            if (room.getFlags().contains(RoomFlag.FIXED)) {
                continue;
            }

            // find all the rectangles R' that overlap room R.
            List<IMazeRoom> intersectingRooms = findIntersections(room, rooms);

            if (!intersectingRooms.isEmpty()) {
                // define a movement vector v.
                Coords2D movementVector = new Coords2D(0, 0);

                Coords2D centerR = new Coords2D(room.getCenter());

                // for each rectangle R that overlaps another.
                for (IMazeRoom rPrime : intersectingRooms) {
                    Coords2D centerRPrime = new Coords2D(rPrime.getCenter());

                    int translatedX = centerR.getX() - centerRPrime.getX();
                    int translatedY = centerR.getY() - centerRPrime.getY();

                    // add a vector to v proportional to the vector between the center of R and R'.
                    // (this statement is not exactly true. it is not "proportional", but
                    // increments by 1 or the movementFactor)
                    movementVector.translate(translatedX < 0 ? -movementFactor : movementFactor,
                            translatedY < 0 ? -movementFactor : movementFactor);
                }

                int translatedX = centerR.getX() - center.getX();
                int translatedY = centerR.getY() - center.getY();

                // add a vector to v proportional to the vector between C and the center of R.
                movementVector.translate(translatedX < 0 ? -movementFactor : movementFactor,
                        translatedY < 0 ? -movementFactor : movementFactor);

                if (room.isStart() || room.isEnd()) {
                    IMazeRoom tempRoom = new MazeRoom(room.getOrigin(), room.getBox().getWidth(),
                            room.getBox().getHeight());

                    // check new coords against boundary
                    Coords2D translatedCoords = new Coords2D(room.getOrigin().getX() + movementVector.getX(),
                            room.getOrigin().getY() + movementVector.getY());

                    tempRoom.setOrigin(translatedCoords);

                    // TODO make this method call cleaner ie don't generate a new boundary every time
                    if (!boundaryConstraint(new Rectangle2D(0, 0, this.width, this.height), tempRoom)) {
                        // add FIXED role to room at original coords
                        room.getFlags().add(RoomFlag.FIXED);

                        // check that START and END don't overlap and aren't both FIXED
                        /*
                         TODO this can still cause overlap problems ie if something already
                         exists at center and has already been process for intersects, it will
                         not be processed again and END will overlap it.
                         */
                        if (this.startRoom.getFlags().contains(RoomFlag.FIXED)
                                && this.endRoom.getFlags().contains(RoomFlag.FIXED)
                                && this.startRoom.getBox().intersects(this.endRoom.getBox())) {
                            // move end room back to center
                            this.endRoom.setOrigin(this.getCenterCoords());
                            this.endRoom.getFlags().remove(RoomFlag.FIXED);
                        }
                        // continue to next room
                        continue;
                    }
                }

                // translate R by v.
                room.setOrigin(new Coords2D(room.getOrigin().getX() + movementVector.getX(),
                        room.getOrigin().getY() + movementVector.getY()));

                // repeat until nothing overlaps.
                hasIntersections = true;
                // add movement to the total
                totalMovement += (movementVector.getX() + movementVector.getY());
            }

        }

        // now do a check
        if (hasIntersections && totalMovement == 0) {
            List<IMazeRoom> roomsToRemove = new ArrayList<>();
            // loop through all the rooms
            rooms.forEach(room -> {
                // don't process if room is already in the roomsToRemove
                if (roomsToRemove.contains(room)) {
                    return;
                }
                List<IMazeRoom> intersectingRooms = findIntersections(room, rooms);
                if (intersectingRooms.size() > 0) {
                    intersectingRooms.forEach(intersectRoom -> {
                        /*
                         * Remove the intersecting room if: 1) it is NOT a START or END room 2) a) the
                         * main room IS a START or END room b) OR the intersecting room is NOT an FIXED c)
                         * OR both main room and intersecting rooms are FIXED
                         */
                        if ((intersectRoom.getType() != RoomType.START && intersectRoom.getType() != RoomType.END)) {
                            LOGGER.debug("intersect room {} is not START nor END", intersectRoom.getId());
                            if ((room.getType() == RoomType.START || room.getType() == RoomType.END)
                                    || !intersectRoom.getFlags().contains(RoomFlag.FIXED)
                                    || (room.getFlags().contains(RoomFlag.FIXED)
                                    && intersectRoom.getFlags().contains(RoomFlag.FIXED))) {
                                roomsToRemove.add(intersectRoom);
                                LOGGER.debug("adding room.id -> {} to remove", intersectRoom.getId());
                            }
                        }

                    });
                }
            });
            // NOTE modifies rooms list here
            rooms.removeAll(roomsToRemove);
        }

        return hasIntersections;
    }

    /**
     *
     * @param room
     * @param rooms
     * @return
     */
    public List<IMazeRoom> findIntersections(IMazeRoom room, List<IMazeRoom> rooms) {
        ArrayList<IMazeRoom> intersections = new ArrayList<>();

        for (IMazeRoom intersectingRect : rooms) {
            if (!room.equals(intersectingRect) && intersectingRect.getBox().intersects(room.getBox())) {
                intersections.add(intersectingRect);
            }
        }
        return intersections;
    }

    /**
     * Returns a subset of rooms that meet the mean factor criteria. Start and End
     * rooms are included as main rooms. Note that the original list is updated as
     * well.
     *
     * @param rooms
     * @param meanFactor
     * @return
     */
    public List<IMazeRoom> selectMainRooms(List<IMazeRoom> rooms, final double meanFactor) {
        List<IMazeRoom> mainRooms = new ArrayList<>();
        int totalArea = 0;
        for (IMazeRoom room : rooms) {
            totalArea += room.getBox().getWidth() * room.getBox().getHeight();
        }

        int meanArea = (int) ((totalArea / rooms.size()) * meanFactor);

        // process each room
        rooms.forEach(room -> {
            if (room.getRole() == RoomRole.MAIN || room.getType() == RoomType.START
                    || room.getType() == RoomType.END
                    || room.getBox().getWidth() * room.getBox().getHeight() > meanArea) {
                room.setRole(RoomRole.MAIN);
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
    public Coords2D getRoomsCenter(List<IMazeRoom> rooms) {
        Rectangle2D boundingBox = getBoundingBox(rooms);
        return boundingBox.getCenter();
    }

    /**
     *
     * @param rooms
     * @return
     */
    private Rectangle2D getBoundingBox(List<IMazeRoom> rooms) {
        Coords2D topLeft = null;
        Coords2D bottomRight = null;

        for (IMazeRoom room : rooms) {
            if (topLeft == null) {
                topLeft = new Coords2D(room.getOrigin().getX(), room.getOrigin().getY());
            } else {
                if (room.getOrigin().getX() < topLeft.getX()) {
                    topLeft.setLocation(room.getOrigin().getX(), topLeft.getY());
                }

                if (room.getOrigin().getY() < topLeft.getY()) {
                    topLeft.setLocation(topLeft.getX(), room.getOrigin().getY());
                }
            }

            if (bottomRight == null) {
                bottomRight = new Coords2D(room.getMaxX(), (int) room.getMaxY());
            } else {
                if (room.getMaxX() > bottomRight.getX()) {
                    bottomRight.setLocation((int) room.getMaxX(), bottomRight.getY());
                }

                if (room.getMaxY() > bottomRight.getY()) {
                    bottomRight.setLocation(bottomRight.getX(), (int) room.getMaxY());
                }
            }
        }
        return new Rectangle2D(topLeft.getX(), topLeft.getY(), bottomRight.getX() - topLeft.getX(),
                bottomRight.getY() - topLeft.getY());
    }

    /**
     * TODO review
     * @param rooms
     * @return
     */
    private List<IMazeRoom> checkConstraints(List<IMazeRoom> rooms) {
        List<IMazeRoom> validRooms = new ArrayList<>();
        Rectangle2D boundary = new Rectangle2D(0, 0, this.width, this.height);
        LOGGER.debug("constraints boundary -> {}\n-=-=-=-=-=-=", boundary);
        rooms.forEach(room -> {
            LOGGER.debug("testing room {} at {} [w:{}, h:{}]", room.getId(), room.getBox().getOrigin(),
                    room.getBox().getWidth(), room.getBox().getHeight());

            if (boundaryConstraint(boundary, room)) {
                validRooms.add(room);
            } else {
                LOGGER.debug("room {} at {} [w:{}, h:{}] failed boundaries test", room.getId(), room.getBox().getOrigin(),
                        room.getBox().getWidth(), room.getBox().getHeight());
            }
        });
        LOGGER.debug("-=-==-=-=-=-=-=-=-=");
        return validRooms;
    }

    /**
     *
     * @param boundary
     * @param room
     * @return
     */
    private boolean boundaryConstraint(Rectangle2D boundary, IMazeRoom room) {
        return room.getMaxX() <= boundary.getMaxX() && room.getMinX() >= boundary.getMinX()
                && room.getMaxY() <= boundary.getMaxY()
                && room.getMinY() >= boundary.getMinY();
    }

    /**
     * Starts the builder process.
     * @return
     */
    public static ISetupBuilder setup() {
        return new MazeLevelGenerator();
    }

    /////////// Common Builder Methods ////////

    /**
     * this method simply brings control back to the MazeLevelGenerator
     * @return
     */
    @Override
    public IMazeLevelGenerator create() {
        return this;
    }

    /////////// ISetupBuilder Methods ///////////
    @Override
    public ISetupBuilder size(int width, int depth) {
        this.width = width;
        this.height = depth;
        return this;
    }

    @Override
    public ISetupBuilder spawnRegion(int width, int depth, Coords2D coords) {
        this.spawnWidth = width;
        this.spawnHeight = depth;
        this.spawnCoords = coords;
        return this;
    }

    @Override
    public ISetupBuilder spawnRegion(int width, int depth, SpawnPosition position) {
        this.spawnWidth = width;
        this.spawnHeight = depth;
        this.spawnPosition = position;
        return this;
    }

    @Override
    public ISetupBuilder movementFactor(int movementFactor) {
        this.movementFactor = movementFactor;
        return this;
    }

    @Override
    public ISetupBuilder meanFactor(double meanFactor) {
        this.meanFactor = meanFactor;
        return this;
    }

    @Override
    public ISetupBuilder random(Random random) {
        this.random = random;
        return this;
    }

    /**
     * defined in ISetupBuilder.
     * kicks off the next room building step.
     * @return
     */
    @Override
    public IRoomsBuilder rooms() {
        return this;
    }

    /////// IRoomsBuilder Methods //////////////////
    @Override
    public IRoomsBuilder amount(int amount) {
        this.numberOfRooms = amount;
        return this;
    }

    @Override
    public IRoomsBuilder sizes(int min, int max) {
        this.minSize = min;
        this.maxSize = max;
        return this;
    }

    @Override
    public IRoomsBuilder degrees(int min, int max) {
        this.minDegrees = min;
        this.maxDegrees = max;
        return this;
    }

    @Override
    public IRoomsBuilder seeds(int amount) {
        this.numberOfSeeds = amount;
        return this;
    }

    @Override
    public IRoomsBuilder add(IMazeRoom room) {
        // TODO ensure all the proper values are valid
        getSuppliedRooms().add(room);
        return this;
    }

    public IRoomBuilder startRoom() {
        return new StartRoomBuilder(this);
    }

    @Override
    public IRoomBuilder endRoom() {
        return new EndRoomBuilder(this);
    }

    ///////////////////////////////////////////////


    @Override
    public List<IMazeRoom> getSuppliedRooms() {
        if (suppliedRooms == null) {
            suppliedRooms = new ArrayList<>();
        }
        return suppliedRooms;
    }

    @Override
    public IMazeRoom getStartRoom() {
        return startRoom;
    }

    @Override
    public IMazeRoom getEndRoom() {
        return endRoom;
    }

    @Override
    public Random getRandom() {
        return this.random;
    }

    @Override
    public String toString() {
        return "MazeLevelGenerator{" +
                "width=" + width +
                ", depth=" + height +
                ", spawnWidth=" + spawnWidth +
                ", spawnDepth=" + spawnHeight +
                ", spawnCoords=" + spawnCoords +
                ", numberOfRooms=" + numberOfRooms +
                ", minSize=" + minSize +
                ", maxSize=" + maxSize +
                ", minDegrees=" + minDegrees +
                ", maxDegrees=" + maxDegrees +
                ", numberOfSeeds=" + numberOfSeeds +
                ", startRoom=" + startRoom +
                ", endRoom=" + endRoom +
                '}';
    }

    /**
     *
     */
    public class StartRoomBuilder extends AbstractRoomBuilder {

        IMazeLevelGenerator mazeLevelGenerator;

        public StartRoomBuilder(IMazeLevelGenerator generator) {
            mazeLevelGenerator = generator;
        }

        @Override
        public IRoomsBuilder next() {
            IMazeRoom room = new MazeRoom();
            if (getWidth() == -1 || getHeight() == -1) {
                room = generateRoom(mazeLevelGenerator.getRandom(), getSpawnCoords(), new Rectangle2D(0, 0, MazeLevelGenerator.this.spawnWidth, MazeLevelGenerator.this.spawnHeight),
                        MazeLevelGenerator.this.minSize, MazeLevelGenerator.this.maxSize);
            }
            else {
                if (Coords2D.EMPTY.equals(getSpawn())) {
                    Coords2D offset = getRoomOffset(mazeLevelGenerator.getRandom(), new Rectangle2D(0, 0, MazeLevelGenerator.this.spawnWidth, MazeLevelGenerator.this.spawnHeight),
                            getWidth(), getHeight());
                    room.setBox(new Rectangle2D(getSpawnCoords().getX() + offset.getX(), getSpawnCoords().getY() + offset.getY(), getWidth(), getHeight()));

                }
                else {
                    room.setBox(new Rectangle2D(this.getSpawn().getX(),
                            this.getSpawn().getY(),
                            this.getWidth(),
                            this.getHeight()));
                }
            }
            room.setId(1);
            room.setType(RoomType.START);
            room.setRole(RoomRole.MAIN);
            room.getFlags().add(RoomFlag.NO_INTERSECTION);
            MazeLevelGenerator.this.startRoom = room;
//            MazeLevelGenerator.this.getSuppliedRooms().add(room);
            return (IRoomsBuilder)mazeLevelGenerator;
        }
    }

    /**
     *
     */
    public class EndRoomBuilder extends AbstractRoomBuilder {
        IMazeLevelGenerator mazeLevelGenerator;

        public EndRoomBuilder(IMazeLevelGenerator generator) {
            mazeLevelGenerator = generator;
        }

        @Override
        public IRoomsBuilder next() {
            IMazeRoom room = new MazeRoom();
//            if (this.getWidth() == -1 || this.getHeight() == -1) {
//                this.setWidth(MazeLevelGenerator.this.random.nextInt(MazeLevelGenerator.this.maxSize - MazeLevelGenerator.this.minSize) + MazeLevelGenerator.this.minSize);
//                this.setHeight(MazeLevelGenerator.this.random.nextInt(MazeLevelGenerator.this.maxSize - MazeLevelGenerator.this.minSize) + MazeLevelGenerator.this.minSize);
//            }

            if (getWidth() == -1 || getHeight() == -1) {
                room = generateRoom(mazeLevelGenerator.getRandom(), getCenterCoords(), new Rectangle2D(0, 0, MazeLevelGenerator.this.spawnWidth, MazeLevelGenerator.this.spawnHeight),
                        MazeLevelGenerator.this.minSize, MazeLevelGenerator.this.maxSize);
            }
            else {
                room.setBox(new Rectangle2D(this.getSpawn().getX(),
                        this.getSpawn().getY(),
                        this.getWidth(),
                        this.getHeight()));
            }
            // pre-determined values
            room.setType(RoomType.END);
            room.setRole(RoomRole.MAIN);
            room.getFlags().add(RoomFlag.NO_INTERSECTION);
            MazeLevelGenerator.this.endRoom = room;
//            MazeLevelGenerator.this.getSuppliedRooms().add(room);
            return (IRoomsBuilder)mazeLevelGenerator;
        }
    }
}
