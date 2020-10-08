package com.someguyssoftware.dungoncrawler.generator.dungeon;

import java.util.Set;

import com.someguyssoftware.dungoncrawler.generator.ILevel;

/**
 * 
 */
public interface IDungeon {
    public Set<ILevel> getLevels();
    public void setLevels(Set<ILevel> levels);
}