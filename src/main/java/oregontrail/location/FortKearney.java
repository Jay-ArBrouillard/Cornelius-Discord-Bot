package oregontrail.location;

public class FortKearney extends Location {
    public FortKearney(int distance) {
        super(distance);
        setDescription();
        setImageURL("https://lh3.googleusercontent.com/pw/ACtC-3ebaM9yP6x_Q1SqzriI9q_mQKENvtrkfzgJFlh61P4_6cWBr-lNpLadS1WNX1sWsQN02_aqMJZCGBc_q1FLabcOSsF3lHHQI2Ns9_-xoRayD69gASmxCJDu1jKdVEhqJZ33Njuqb_nezuvV9Pbmf9o_=w562-h355-no?authuser=1");
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
