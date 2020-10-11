package com.someguyssoftware.dungoncrawler.generator.dungeon;

import java.util.LinkedHashSet;
import java.util.Set;

import com.someguyssoftware.dungoncrawler.generator.ILevel;

/**
 * @author Mark Gottschling on Oct 7, 2020
 */
public class Dungeon {
    private Set<ILevel> levels;

    public Dungeon() {

    }

    public Set<ILevel> getLevels() {
        if (levels == null) {
            levels = new LinkedHashSet<>();
        }
        return levels;
    }

    public void setLevels(Set<ILevel> levels) {
        this.levels = levels;
    }
}