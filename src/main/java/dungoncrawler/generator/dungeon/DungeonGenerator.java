package dungoncrawler.generator.dungeon;

/**
 * @author Mark Gottschling on Oct 7, 2020
 */
public class DungeonGenerator implements IDungeonGenerator {

    private int numberOfLevels = 1;

    @Override
    public IDungeon build() {
        boolean isValid = isValid();
        if (!isValid) { }

        IDungeon dungeon = (IDungeon) new Dungeon();
        return dungeon;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    public int getNumberOfLevels() {
        return numberOfLevels;
    }

    private DungeonGenerator withNumberOfLevels(int numberOfLevels) {
        this.numberOfLevels = numberOfLevels;
        return this;
    }
}