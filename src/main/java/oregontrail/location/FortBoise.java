package oregontrail.location;

public class FortBoise extends Location {
    public FortBoise(int distance) {
        super(distance);
        setDescription();
        setImageURL("https://lh3.googleusercontent.com/pw/ACtC-3d980Uaj6jZQ-E8kwEg0IHqwstxWhGQ7s5aqhxNXFpNenGxC5psnKnAMnRpGCGg5pixWBQU8kQudNaF8hwvB5K9u0qjDKsTAyg1ephLKMBltjeaA2YFh4noZqla76lZDWZd0FAJIamtXs7k8LBcqIrF=w561-h352-no?authuser=1");
    }

    private void setDescription() {
        super.setDescription("Fort Boise was built by the Hudson's Bay Company in 1834 as a fur-trading outpost.  Its name comes from the French word \"boise,\" meaning \"wooded.\n"  +
                "There is a general store available to buy supplies.");
    }

    @Override
    public String toString() {
        return "Fort Boise";
    }
}
