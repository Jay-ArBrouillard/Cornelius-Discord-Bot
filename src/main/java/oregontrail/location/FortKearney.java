package oregontrail.location;

public class FortKearney extends Location {
    public FortKearney(int distance) {
        super(distance);
        setDescription();
    }

    private void setDescription() {
        super.setDescription("Fort Kearney is a U.S. Army post established in 1848 near the Platte River." +
                             "There is a general store available to buy supplies.");
    }

    @Override
    public String toString() {
        return "Fort Kearney";
    }
}
