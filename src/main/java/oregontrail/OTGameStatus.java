package oregontrail;

public enum OTGameStatus {
    START("START"),
    RUNNING("RUNNING"),
    RULES("RULES"),
    QUIT("QUIT"),
    WIN("WIN"),
    LOSE("LOSE"),
    INVALID_INPUT("INVALID_INPUT");

    public final String label;

    OTGameStatus(String label) {
        this.label = label;
    }
}
