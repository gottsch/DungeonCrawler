import com.fasterxml.jackson.databind.ObjectMapper;
import dungoncrawler.generator.maze2.ILevel2D;
import dungoncrawler.generator.maze2.MazeLevelGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.util.Optional;


public class TestMazeLevelGenerator2 {
    protected static final Logger LOGGER = LogManager.getLogger();

    @Test
    public void createMazeLevelGen() {
        MazeLevelGenerator generator = new MazeLevelGenerator.Builder()
                .with($ -> {
                    $.width = 95;
                    $.height = 95;
                    $.numberOfRooms = 25;
                    $.attemptsMax = 250;
                }).build();

        Optional<ILevel2D> level = generator.generate();

        //////////////////////////////////////////
        // TEMP - json output of generator state
        ObjectMapper mapper = new ObjectMapper();
        try {
            LOGGER.debug("generator state -> {}", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(generator));
            String generatorJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(level.orElseThrow());
            LOGGER.debug("level json -> {}", generatorJson);
        } catch(Exception e) {
            LOGGER.warn("unable to convert MazeLevelGenerator to JSON.");
        }

    }
}
