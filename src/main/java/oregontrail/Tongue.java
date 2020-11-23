package oregontrail;

public class Tongue extends Part {
    /**
     * Default health of this part (value is 125)
     */
    private static final int DEFAULT_HEALTH = 125;

    /**
     * Default weight of this part (value is 5)
     */
    private static final int DEFAULT_WEIGHT = 5;

    /**
     * Create a tongue with specified health
     *
     * @param health
     *            int
     */
    public Tongue(int health) {
        super(health);
    }

    /**
     * Create a tongue with full health
     */
    public Tongue() {
        this(DEFAULT_HEALTH);
    }

    /**
     * Get weight of a tongue
     *
     * @return int Tongue weight
     */
    public static int getWeight() {
        return DEFAULT_WEIGHT;
    }

    /**
     * Standard toString
     *
     * @return String
     */
    @Override
    public String toString() {
        return "Tongue";
    }
}
