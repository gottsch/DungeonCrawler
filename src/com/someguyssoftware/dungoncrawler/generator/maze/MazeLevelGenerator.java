package com.someguyssoftware.dungoncrawler.generator.maze;

import com.someguyssoftware.dungoncrawler.generator.ILevel;
import com.someguyssoftware.dungoncrawler.generator.ILevelGenerator;

/**
 * A 3d level generator/builder. This includes how high the level is.
 * Created by Mark Gottschling on 11/1/2023
 */
public class MazeLevelGenerator {
    private int width;
    private int height;
    private int depth;

    private int minRooms;
    private int maxRooms;

    public MazeLevelGenerator(int width, int height, int depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    public MazeLevelGenerator rooms(int min, int max) {
        this.minRooms = min;
        this.maxRooms = max;
        return this;
    }

    public ILevel build() {
        return null;
    }

}
