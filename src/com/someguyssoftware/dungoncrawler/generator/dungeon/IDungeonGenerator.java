package com.someguyssoftware.dungoncrawler.generator.dungeon;

/**
 * @author Mark Gottschling on Oct 7, 2020
 */
public interface IDungeonGenerator {
    /**
     * Builds a dungeon based on input values
     * @return  the built dungeon
     */
    public IDungeon build();

    /**
     * Determines if all inputs are valid values
     * @return
     */
    boolean isValid();
}