package oregontrail.location;

public class FortBridger extends Location {
    public FortBridger(int distance) {
        super(distance);
        setDescription();
    }

    private void setDescription() {
        super.setDescription("Fort Bridger is a U.S. army outpost, although it was founded in 1843 by fur trader and scout Jim Bridger as a trading post and way station."  +
                "There is a general store available to buy supplies.");
    }

    @Override
    public String toString() {
        return "Fort Bridger";
    }
}
