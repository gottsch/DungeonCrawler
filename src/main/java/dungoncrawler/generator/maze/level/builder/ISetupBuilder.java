package dungoncrawler.generator.maze.level.builder;

import dungoncrawler.generator.Coords2D;
import dungoncrawler.generator.ILevel;
import dungoncrawler.generator.maze.level.IMazeLevelGenerator;
import dungoncrawler.generator.maze.level.SpawnPosition;

import java.util.Random;

public interface ISetupBuilder {
    ISetupBuilder meanFactor(double meanFactor);

    public ISetupBuilder random(Random random);
    public ISetupBuilder size(int width, int depth);
    public ISetupBuilder spawnRegion(int width, int depth, Coords2D coords);
    public ISetupBuilder spawnRegion(int width, int depth, SpawnPosition position);
    public IRoomsBuilder rooms();
    public IMazeLevelGenerator create();

    public ISetupBuilder movementFactor(int i);
}
