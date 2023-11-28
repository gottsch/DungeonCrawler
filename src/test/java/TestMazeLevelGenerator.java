import dungoncrawler.generator.Coords2D;
import dungoncrawler.generator._maze.level.IMazeLevel;
import dungoncrawler.generator._maze.level.IMazeLevelGenerator;
import dungoncrawler.generator._maze.level.MazeLevelGenerator;
import dungoncrawler.generator._maze.room.MazeRoom;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.util.Random;


public class TestMazeLevelGenerator {
    protected static final Logger LOGGER = LogManager.getLogger();

    @Test
    public void createDefaultMazeLevelGen() {
        IMazeLevel level = MazeLevelGenerator.setup().create().generate();
    }

    @Test
    public void createMazeLevelGen() {
        IMazeLevelGenerator generator = MazeLevelGenerator.setup()
                .random(new Random())
                .size(96, 96)
                .spawnRegion(48, 48, new Coords2D(48, 48))
                .movementFactor(1)
                .meanFactor(0.85)
                .rooms()
                    .amount(5)
                    .sizes(7, 19)
                    .degrees(3, 5)
                .add(new MazeRoom()) // example - add fixed multilevel room or obstacles
                    .startRoom()
                        .size(15, 15) // will be provided as the last end rooms size
                        .spawn(new Coords2D(10, 10))

                        .next()
                    .endRoom()
//                        .addFlag(RoomFlag.FIXED)
                        .next()


                .seeds(5) // TODO in future could be its own builder and allows different settings
                .create();

                IMazeLevel level = generator.generate();
    }
}
