package oregontrail;

public enum OTGameStatus {
    START("START"),
    RUNNING("RUNNING"),
    RULES("RULES"),
    QUIT("QUIT"),
    WIN("WIN"),
    LOSE("LOSE"),
    INVALID_INPUT("INVALID_INPUT"),
    KEEP_STATE("KEEP_STATE"),
    STORE("STORE");

    public final String label;

    OTGameStatus(String label) {
        this.label = label;
    }
}
