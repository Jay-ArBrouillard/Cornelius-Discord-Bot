package oregontrail.location;

public class SouthPass extends Location {
    public SouthPass(int distance) {
        super(distance);
        setDescription();
        setImageURL("https://lh3.googleusercontent.com/pw/ACtC-3eMBlCmBYjQuVNgQ68AEAZIcOV9zF8rxe94hO5mZBz2WDBt7CFpW21KxI2NwkLzW8YnMV5Xowj_fCTNC14VYuxKzaBQFm19NyA9iHhXN7YnqiONVSrd4zfSgxxe69wYCdSLqiavlQmCB7rRCXfoFl-l=w639-h393-no?authuser=1");
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
