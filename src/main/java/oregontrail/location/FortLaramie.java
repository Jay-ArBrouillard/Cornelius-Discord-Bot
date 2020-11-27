package oregontrail.location;

public class FortLaramie extends Location {
    public FortLaramie(int distance) {
        super(distance);
        setDescription();
    }

    private void setDescription() {
        super.setDescription("Fort Laramie is a US Army post near the junction of the North Platte and" +
                "Laramie Rivers.  Originally called Fort William, it was founded as a" +
                "fur-trading post in 1834.  It was renamed for Jacques Laramie, a French trapper" +
                "who worked in the region earlier in the century." +
                "There is a general store available to buy supplies.");
    }

    @Override
    public String toString() {
        return "Fort Laramie";
    }
}
