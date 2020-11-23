package oregontrail.enums;

public enum RationsEnum {
    FILLING("Filling", 5),
    MEAGER("Meager", 3),
    STARVING("Starving", 1);

    public final String name;
    public final int quantity;

    RationsEnum(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }
}
