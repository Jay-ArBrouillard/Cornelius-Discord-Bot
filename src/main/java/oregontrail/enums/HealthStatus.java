package oregontrail.enums;

public enum HealthStatus {
    EXCELLENT("Excellent"),
    GOOD("Good"),
    FAIR("Fair"),
    POOR("Poor"),
    NEAR_DEATH("Near Death"),
    DEAD("Dead");

    public final String name;

    HealthStatus(String name) {
        this.name = name;
    }
}
