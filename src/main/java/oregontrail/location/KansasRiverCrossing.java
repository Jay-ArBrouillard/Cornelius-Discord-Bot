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
        setImageURL("https://lh3.googleusercontent.com/pw/ACtC-3dJ-1a23u2dwPqv3QjUpVf090Acey7reK7dCB3-_KQ8QA00_I1MT0e86I-Lh0dZFy-4rXMN_-3fcq_o6mZw5EheKr5PVDBCax-cqv_8SyZpfAFkTfJo0MUCgNPRg87U8ko9pL2K34Mryz4hkTmw2plE=w644-h325-no?authuser=1");
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
