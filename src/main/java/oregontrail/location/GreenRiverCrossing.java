package oregontrail.location;

import utils.CorneliusUtils;

public class GreenRiverCrossing extends Location implements River {
    private int length;
    private int width;
    private double depth;
    private double chance;

    public GreenRiverCrossing(int distance) {
        super(distance);
        this.length = CorneliusUtils.randomIntBetween(700, 1000);
        this.width = CorneliusUtils.randomIntBetween(1100, 1200);
        this.depth = CorneliusUtils.randomDoubleBetween(35, 100, 2);
        this.chance = width*depth/121000*100;
        setDescription();
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public double getDepth() {
        return depth;
    }

    public void setDepth(double depth) {
        this.depth = depth;
    }

    public double getChance() {
        return chance;
    }

    public void setChance(double chance) {
        this.chance = chance;
    }

    private void setDescription() {
        super.setDescription("The Green River is a tributary to the Colorado River, flowing south from the Continental Divide along a twisted, rugged path.\n" +
                "Known to be very deep and treacherous.\n" +
                "You must cross the river to continue.\n" +
                "The river at this point is currently " + width + " feet across,\n" +
                "and " + String.format("%.2f", depth) + " feet deep in the middle.");
    }

    @Override
    public String toString() {
        return "Green River Crossing";
    }
}
