package oregontrail.enums;

public enum DiseaseEnum {
    MEASLES("Measles", 0.75, 1),
    CHOLERA("Cholera", 0.25, 5),
    DYSENTERY("Dysentery", 0.25, 3),
    TYPHOID("Typhoid", 0.25, 3);

    public final String name;
    public final double infectionRate;
    public final int sicknessPenalty;

    DiseaseEnum(String name, double infectionRate, int sicknessPenalty) {
        this.name = name;
        this.infectionRate = infectionRate;
        this.sicknessPenalty = sicknessPenalty;
    }

    @Override
    public String toString() {
        return name;
    }
}
