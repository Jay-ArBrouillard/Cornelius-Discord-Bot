package oregontrail.occupation;

public abstract class Occupation {
    public int startingCash;
    public double bonusMultipler = 1.0;
    public Occupation(int startingCash) {
        this.startingCash = startingCash;
    }

    public Occupation(int startingCash, double bonusMultipler) {
        this.startingCash = startingCash;
        this.bonusMultipler = bonusMultipler;
    }
}
