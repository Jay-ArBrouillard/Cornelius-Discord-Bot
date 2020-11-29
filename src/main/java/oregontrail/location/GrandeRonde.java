package oregontrail.location;

public class GrandeRonde extends Location {
    public GrandeRonde(int distance) {
        super(distance);
        setDescription();
        setImageURL("https://lh3.googleusercontent.com/pw/ACtC-3dWRG3kH6Cm9_OsHx8UkRKll6Nm9qmTS9vp2XYT-BCus7gnAPhHLo0WZ9WHRSRjZM6TxbVr6GeNqJhyrgftYZcx4rDJlRq5CXr--beG69OdJ5w_WR4hz21kX5bzIVm51eMsdR0J7NPjvyWgsfJiPcLT=w643-h369-no?authuser=1");
    }

    private void setDescription() {
        super.setDescription("The Grand Ronde (French for 'great ring') is a river that runs roughly\n" +
                "parallel to the Blue Mountains.  The Oregon Trail crosses through the Grande\n" +
                "Ronde river valley just before the mountains.  The Grande Ronde valley is noted\n" +
                "for its beauty and is greatly appreciated by emigrants as a sign that their\n" +
                "long journey is nearing its end.");
    }

    public String getGrandeRondeOptions() {
        StringBuilder optionsBuilder = new StringBuilder();
        optionsBuilder.append("You may:\n");
        optionsBuilder.append("\t1. Head to Fort Walla Walla\n");
        optionsBuilder.append("\t2. Take shortcut to Dalles");
        return optionsBuilder.toString();
    }

    @Override
    public String toString() {
        return "The Grande Ronde in the Blue Mountains";
    }
}
