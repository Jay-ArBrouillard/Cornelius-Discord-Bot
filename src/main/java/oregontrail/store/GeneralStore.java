package oregontrail.store;

import oregontrail.Wagon;

public abstract class GeneralStore {
    private int numOxenAvailable = 0;
    private int numSetsClothingAvailable = 0;
    private int numAmmoAvailable = 0;
    private int numWagonWheelsAvailable = 0;
    private int numWagonAxlesAvailable = 0;
    private int numWagonTonguesAvailable = 0;
    private int numFoodAvailable = 0;
    private double clothingPrice = 0;
    private double oxenPrice = 0;
    private double ammoPrice = 0;
    private double wheelPrice = 0;
    private double axlePrice = 0;
    private double tonguePrice = 0;
    private double foodPrice = 0;
    private String imageURL;

    public abstract boolean canBuy(String input, double cash);
    public abstract double buy(String item, Wagon wagon);

    public int getNumOxenAvailable() {
        return numOxenAvailable;
    }

    public void setNumOxenAvailable(int numOxenAvailable) {
        this.numOxenAvailable = numOxenAvailable;
    }

    public int getNumSetsClothingAvailable() {
        return numSetsClothingAvailable;
    }

    public void setNumSetsClothingAvailable(int numSetsClothingAvailable) {
        this.numSetsClothingAvailable = numSetsClothingAvailable;
    }

    public int getNumAmmoAvailable() {
        return numAmmoAvailable;
    }

    public void setNumAmmoAvailable(int numAmmoAvailable) {
        this.numAmmoAvailable = numAmmoAvailable;
    }

    public int getNumWagonWheelsAvailable() {
        return numWagonWheelsAvailable;
    }

    public void setNumWagonWheelsAvailable(int numWagonWheelsAvailable) {
        this.numWagonWheelsAvailable = numWagonWheelsAvailable;
    }

    public int getNumWagonAxlesAvailable() {
        return numWagonAxlesAvailable;
    }

    public void setNumWagonAxlesAvailable(int numWagonAxlesAvailable) {
        this.numWagonAxlesAvailable = numWagonAxlesAvailable;
    }

    public int getNumWagonTonguesAvailable() {
        return numWagonTonguesAvailable;
    }

    public void setNumWagonTonguesAvailable(int numWagonTonguesAvailable) {
        this.numWagonTonguesAvailable = numWagonTonguesAvailable;
    }

    public int getNumFoodAvailable() {
        return numFoodAvailable;
    }

    public void setNumFoodAvailable(int numFoodAvailable) {
        this.numFoodAvailable = numFoodAvailable;
    }

    public double getClothingPrice() {
        return clothingPrice;
    }

    public void setClothingPrice(double clothingPrice) {
        this.clothingPrice = clothingPrice;
    }

    public double getOxenPrice() {
        return oxenPrice;
    }

    public void setOxenPrice(double oxenPrice) {
        this.oxenPrice = oxenPrice;
    }

    public double getAmmoPrice() {
        return ammoPrice;
    }

    public void setAmmoPrice(double ammoPrice) {
        this.ammoPrice = ammoPrice;
    }

    public double getWheelPrice() {
        return wheelPrice;
    }

    public void setWheelPrice(double wheelPrice) {
        this.wheelPrice = wheelPrice;
    }

    public double getAxlePrice() {
        return axlePrice;
    }

    public void setAxlePrice(double axlePrice) {
        this.axlePrice = axlePrice;
    }

    public double getTonguePrice() {
        return tonguePrice;
    }

    public void setTonguePrice(double tonguePrice) {
        this.tonguePrice = tonguePrice;
    }

    public double getFoodPrice() {
        return foodPrice;
    }

    public void setFoodPrice(double foodPrice) {
        this.foodPrice = foodPrice;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
