package dungoncrawler.generator;

/**
 * @author Mark Gottschling on Nov 10, 2023
 *
 * */
public class IdGenerator {
    private int startId;
    private int id;

    public IdGenerator() {
        this(0);
    }

    public IdGenerator(int startingId) {
        this.startId = startingId;
        this.id = startingId;
    }

    public int next() {
        return this.id++;
    }

    public void reset() {
        this.id = this.startId;
    }

    public int getStart() { return this.startId;}
}
