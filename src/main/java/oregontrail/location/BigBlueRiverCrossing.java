package oregontrail.location;

import utils.CorneliusUtils;

public class BigBlueRiverCrossing extends Location implements River {
    private int length;
    private int width;
    private double depth;
    private double chance;

    public BigBlueRiverCrossing(int distance) {
        super(distance);
        this.length = CorneliusUtils.randomIntBetween(200, 400);
        this.width = CorneliusUtils.randomIntBetween(600, 1000);
        this.depth = CorneliusUtils.randomDoubleBetween(2, 6, 2);
        this.chance = width*depth/6000*100;
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
        super.setDescription("The Big Blue River is a tributary to the Kansas River, which is in turn a tributary to the Missouri.\n" +
                "You must cross the river to continue.\n" +
                "The river at this point is currently " + width + " feet across,\n" +
                "and " + String.format("%.2f", depth) + " feet deep in the middle.");
    }

    @Override
    public String toString() {
        return "Big Blue River Crossing";
    }
}
