package oregontrail.location;

public class SouthPass extends Location {
    public SouthPass(int distance) {
        super(distance);
        setDescription();
    }

    private void setDescription() {
        super.setDescription("South Pass is a valley that cuts through the Rocky Mountains at their highest\n" +
                "point, the Continental Divide.  It marks the halfway point on your journey to\n" +
                "Oregon.  After South Pass, the trail splits.  If you're short on supplies, you\n" +
                "should head to Fort Bridger.  But if you don't need supplies, you may want to\n" +
                "take the shorter route and go directly to the Green River.");
    }

    @Override
    public String toString() {
        return "South Pass";
    }
}
