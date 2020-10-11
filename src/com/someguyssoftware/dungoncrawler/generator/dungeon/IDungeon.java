package com.someguyssoftware.dungoncrawler.generator.dungeon;

import java.util.Set;

import com.someguyssoftware.dungoncrawler.generator.ILevel;

/**
 * @author Mark Gottschling on Oct 7, 2020
 */
public interface IDungeon {
    public Set<ILevel> getLevels();
    public void setLevels(Set<ILevel> levels);
}