package oregontrail.occupation;

public class Teacher extends Occupation {
    public Teacher() {
        super(400, 3.5);
    }

    @Override
    public String toString() {
        return "Teacher";
    }
}
