package oregontrail.location;

public abstract class Location {
    private final int distance; //The distance this location is at
    private String description;
    private String imageURL;

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

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
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

    public static String getWagonSunkURL() {
        return "https://lh3.googleusercontent.com/pw/ACtC-3e4Htbc9YHbuFBGFoAUBxQw_N5em2aWHwU3pJJJkyl1cs7lRmNe13Mvz84VMPKC_hGnPuPynY2tnWMQ4fmyYXfO0R3xENo514Jjbn35233cbps_Kh_Nh2QFEz-CsyA0OnFQu3Y5iR8EQv02ZzlTD2LJ=w300-h213-no?authuser=1";
    }

    public static String getRestURL() {
        return "https://lh3.googleusercontent.com/pw/ACtC-3cwxpDU5rZpu5G0P5ogsJXz_IkEqOuHtFrSyqdAWCjzBYsocH6tQFgyxaL3LvYnGaubaMSH2SKwtpRZ71BlHDsGlusZ9pvskg8X7aZqgBt-lGohox1h1qtEakShP3SqRCwfFCFzuWtNHxPFx_F_Hic=w640-h332-no?authuser=1";
    }
}
