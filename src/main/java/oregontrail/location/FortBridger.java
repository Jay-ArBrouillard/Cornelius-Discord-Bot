package oregontrail.location;

public class FortBridger extends Location {
    public FortBridger(int distance) {
        super(distance);
        setDescription();
        setImageURL("https://lh3.googleusercontent.com/pw/ACtC-3cAjkTk2r-ucbMhOnNiKKy3JqYjv4_1tMSa207fD8qHYqEBKCm_19B0_40670Kk-oKtZUuyCPBnr1Ow8zm08iix6yr1-m7w78q87nDD8d1cecnbHDPp56p270j6XeTCV0IMGx5J6QNFY0aJbM0Vd46l=w562-h347-no?authuser=1");
    }

    private void setDescription() {
        super.setDescription("Fort Bridger is a U.S. army outpost, although it was founded in 1843 by fur trader and scout Jim Bridger as a trading post and way station."  +
                "There is a general store available to buy supplies.");
    }

    @Override
    public String toString() {
        return "Fort Bridger";
    }
}
