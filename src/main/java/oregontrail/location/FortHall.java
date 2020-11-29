package oregontrail.location;

public class FortHall extends Location {
    public FortHall(int distance) {
        super(distance);
        setDescription();
        setImageURL("https://lh3.googleusercontent.com/pw/ACtC-3feczRjGInpzr0dwb5V0a-aleEZtotViecOBPx4qEwjoZ0YTW7pPVC96wo-vkWRVw9x8srKkZ1I6GVjzGjHBTR7xgO0OQ9UAOg3gCrgsZOuoDZet_xxoHfei9JcdkbwU_1aAsL24WbVolSc748IGwWZ=w1070-h588-no?authuser=1");
    }

    private void setDescription() {
        super.setDescription("Fort Hall is an outpost on the banks of the Snake River. It was originally a fur-trading post, founded by Nathaniel Wyeth in 1834.  Later it was bought by the Hudson's Bay Company.");
    }

    @Override
    public String toString() {
        return "Fort Hall";
    }
}
