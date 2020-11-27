package oregontrail.location;

public class FortHall extends Location {
    public FortHall(int distance) {
        super(distance);
        setDescription();
    }

    private void setDescription() {
        super.setDescription("Fort Hall is an outpost on the banks of the Snake River. It was originally a fur-trading post, founded by Nathaniel Wyeth in 1834.  Later it was bought by the Hudson's Bay Company.");
    }

    @Override
    public String toString() {
        return "Fort Hall";
    }
}
