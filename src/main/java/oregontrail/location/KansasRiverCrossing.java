package oregontrail.location;

import utils.CorneliusUtils;

public class KansasRiverCrossing extends Location implements River {
    private int length;
    private int width;
    private double depth;
    private double chance;

    public KansasRiverCrossing(int distance) {
        super(distance);
        this.length = CorneliusUtils.randomIntBetween(100, 300);
        this.width = CorneliusUtils.randomIntBetween(500, 650);
        this.depth = CorneliusUtils.randomDoubleBetween(1, 5, 2);
        this.chance = width*depth/3250*100;
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
        super.setDescription("The Kansas River is a tributary to the Missouri. You must cross the river to continue.\n" +
                "The river at this point is currently " + width + " feet across,\n" +
                "and " + String.format("%.2f", depth) + " feet deep in the middle.");
    }

    @Override
    public String toString() {
        return "Kansas River Crossing";
    }
}
