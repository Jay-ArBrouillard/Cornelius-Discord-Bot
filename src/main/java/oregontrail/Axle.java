package oregontrail;

public class Axle extends Part {
    /**
     * Default health of this part (value is 100)
     */
    public static final int DEFAULT_HEALTH = 100;

    /**
     * Default weight of this part (value is 5)
     */
    public static final int DEFAULT_WEIGHT = 5;

    /**
     * Constructor without arguments
     */
    public Axle(){
        this(DEFAULT_HEALTH);
    }

    /**
     * Constructor with health given
     * @param health int
     */
    public Axle(int health) {
        super(health);
    }

    /**
     * Gets the name of the part in string form
     * @return String
     */
    @Override
    public String toString() {
        return "Axle";
    }

    /**
     * Get the weight of the part
     * @return int Part weight
     */
    public static int getWeight(){
        return DEFAULT_WEIGHT;
    }
}
