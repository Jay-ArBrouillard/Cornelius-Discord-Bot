package oregontrail.location;

public class FortBoise extends Location {
    public FortBoise(int distance) {
        super(distance);
        setDescription();
    }

    private void setDescription() {
        super.setDescription("Fort Boise was built by the Hudson's Bay Company in 1834 as a fur-trading outpost.  Its name comes from the French word \"boise,\" meaning \"wooded.\n"  +
                "There is a general store available to buy supplies.");
    }

    @Override
    public String toString() {
        return "Fort Boise";
    }
}
