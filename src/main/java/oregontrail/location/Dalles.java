package oregontrail.location;

public class Dalles extends Location {
    private final boolean canOnlyRaft;
    public Dalles(int distance, boolean canOnlyRaft) {
        super(distance);
        this.canOnlyRaft = canOnlyRaft;
        setDescription();
        setImageURL("https://lh3.googleusercontent.com/pw/ACtC-3eqbcyfAwOdnoG17vvQD3XTcohMHDsBigrAc4t32mYN5QyZaoBtSB6bqXHHhow7cOEYySU0NZfudb-t2TFlRIvvby6AE49IpB-oWv9ctOwicCGPZMK_ES0WCIFXsDTM9l14jeBZ6QWvfi5FofnFfzrt=w562-h351-no?authuser=1");
    }

    public boolean isCanOnlyRaft() {
        return canOnlyRaft;
    }

    private void setDescription() {
        super.setDescription("The Dalles is the chief embarkation point for rafts heading down the Columbia\n" +
                "River toward the Willamette Valley.  It was named by French fur-trappers, who\n" +
                "likened the deep, stony river gorge to a huge gutter.  (In French, the word\n" +
                "'dalles' can refer to 'gutters' or 'flagstones.') Emigrants to Oregon often\n" +
                "stop here to rest and trade before rafting down the Columbia.");
    }

    public String getDallesOptions() {
        StringBuilder optionsBuilder = new StringBuilder();
        optionsBuilder.append("You may:\n");
        optionsBuilder.append("\t1. Raft down the river (90 Miles - More Dangerous)\n");
        optionsBuilder.append("\t2. Take the Barlow Toll Road (100 miles - Safer)");
        return optionsBuilder.toString();
    }

    @Override
    public String toString() {
        return "Dalles Splits";
    }
}
