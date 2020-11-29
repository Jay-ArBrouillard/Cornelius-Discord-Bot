package oregontrail.location;

public class FortLaramie extends Location {
    public FortLaramie(int distance) {
        super(distance);
        setDescription();
        setImageURL("https://lh3.googleusercontent.com/pw/ACtC-3cdtiF7weDskn2ekVMSnUxCL7m_dh4bAYUzawTNym0dPpmEJ44Ka4Dw92aAA_NK2JDsL7AqB0bN96MehN6Nkj3SB3KRRk-yLtJOx2j4zpVbtoKCUUDhznmPBMBm0cc7pCmL6dD48ByAQC0gcAfVQpPc=w815-h513-no?authuser=1");
    }

    private void setDescription() {
        super.setDescription("Fort Laramie is a US Army post near the junction of the North Platte and" +
                "Laramie Rivers.  Originally called Fort William, it was founded as a" +
                "fur-trading post in 1834.  It was renamed for Jacques Laramie, a French trapper" +
                "who worked in the region earlier in the century." +
                "There is a general store available to buy supplies.");
    }

    @Override
    public String toString() {
        return "Fort Laramie";
    }
}
