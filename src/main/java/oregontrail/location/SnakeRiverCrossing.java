package oregontrail.location;

import utils.CorneliusUtils;

public class SnakeRiverCrossing extends Location implements River {
    private int length;
    private int width;
    private double depth;
    private double chance;

    public SnakeRiverCrossing(int distance) {
        super(distance);
        this.length = CorneliusUtils.randomIntBetween(2000, 3000);
        this.width = CorneliusUtils.randomIntBetween(200, 600);
        this.depth = CorneliusUtils.randomDoubleBetween(1, 20, 2);
        this.chance = width*depth/13000*100;
        setDescription();
        setImageURL("https://lh3.googleusercontent.com/pw/ACtC-3cJ3Dsq0DCTM7riV68Uxc9YNJxU5wzj-i3s7FodmoAxCbtaakBurRMDfmRHCg8KT5GqmdIEyR0_u3-v9V4GMMfZKQcZN7v7oRYfamccVsvEdiqu-ZygI772qxkknUW1-_ftak_cz7rY_Xgw_kn1PdgC=w640-h402-no?authuser=1");
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
        super.setDescription("The Snake River gets its name from the way it twists and turns through this ruffed country, sometimes through steep gorges.\n" +
                "You must cross the river to continue.\n" +
                "The river at this point is currently " + width + " feet across,\n" +
                "and " + String.format("%.2f", depth) + " feet deep in the middle.");
    }

    public String getSnakeRiverOptions() {
        StringBuilder optionsBuilder = new StringBuilder();
        optionsBuilder.append("You may:\n");
        optionsBuilder.append("\t1. Attempt to ford the river (Same day)\n");
        optionsBuilder.append("\t2. Caulk the wagon and float it across (1 day)\n");
        optionsBuilder.append("\t3. Hire an Indian as a guide (3 sets of clothing - 3 days)\n");
        optionsBuilder.append("\t4. Wait a day to see if conditions improve (1 day)\n");
        return optionsBuilder.toString();
    }

    @Override
    public String toString() {
        return "Snake River Crossing";
    }
}
