package oregontrail.location;

public class FortWallaWalla extends Location {
    public FortWallaWalla(int distance) {
        super(distance);
        setDescription();
        setImageURL("https://lh3.googleusercontent.com/pw/ACtC-3ddtVDqPTiMKG4QgZAM8oknTBnvi5Jcd2SUIOSrOIe1TkAagS6JZREueUbUzXiZfznYojMfZX8Wcbvh49RSLo3yCM_9ZOf-iE67CxafCP_8T4SBuSphweEEYMtKzn8pRXqZH-XlOUzMn_SpJj-f5vx0=w638-h384-no?authuser=1");
    }

    private void setDescription() {
        super.setDescription("Fort Walla Walla was established in 1818 as a fur-trading post at the juncture of the Columbia and Walla Walla Rivers.  It later became a military fort.\n"  +
                "There is a general store available to buy supplies.");
    }

    @Override
    public String toString() {
        return "Fort Walla Walla";
    }
}
