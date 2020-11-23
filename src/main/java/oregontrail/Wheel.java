package oregontrail;

public class Wheel extends Part {
    /**
     * Default health of this part (value is 80)
     */
    private static final int DEFAULT_HEALTH = 80;

    /**
     * Default weight of this part (value is 5)
     */
    private static final int DEFAULT_WEIGHT = 5;

    /**
     * Constructor for Wheel.
     *
     * @param health
     *            int
     */
    public Wheel(int health) {
        super(health);
    }

    /**
     * Plain wheel
     */
    public Wheel() {
        this(DEFAULT_HEALTH);
    }

    /**
     * Weight of this part
     *
     * @return int
     */
    public static int getWeight() {
        return DEFAULT_WEIGHT;
    }

    /**
     * Standard toString.
     *
     * @return String
     */
    @Override public String toString() {
        return "Wheel";
    }
}
