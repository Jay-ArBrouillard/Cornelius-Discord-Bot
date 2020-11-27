package oregontrail.location;

public abstract class Location {
    private final int distance; //The distance this location is at
    private String description;

    public Location(int distance) {
        this.distance = distance;
    }

    public int getDistance() {
        return distance;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRiverOptions() {
        StringBuilder optionsBuilder = new StringBuilder();
        optionsBuilder.append("You may:\n");
        optionsBuilder.append("\t1. Attempt to ford the river (Same day)\n");
        optionsBuilder.append("\t2. Caulk the wagon and float it across (1 day)\n");
        optionsBuilder.append("\t3. Take the ferry across ($5 - 5 days)\n");
        optionsBuilder.append("\t4. Wait a day to see if conditions improve (1 day)\n");
        return optionsBuilder.toString();
    }

    public String getFortOptions() {
        StringBuilder optionsBuilder = new StringBuilder();
        optionsBuilder.append("You may:\n");
        optionsBuilder.append("\t1. Continue on the trail\n");
        optionsBuilder.append("\t2. Buy supplies");
        return optionsBuilder.toString();
    }

    public String getSouthPassOptions() {
        StringBuilder optionsBuilder = new StringBuilder();
        optionsBuilder.append("You may:\n");
        optionsBuilder.append("\t1. Head to Fort Bridger\n");
        optionsBuilder.append("\t2. Take shortcut to Green River Crossing");
        return optionsBuilder.toString();
    }
}
