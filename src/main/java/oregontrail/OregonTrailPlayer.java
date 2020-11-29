package oregontrail;

import oregontrail.enums.DiseaseEnum;
import oregontrail.enums.HealthStatus;
import oregontrail.occupation.Occupation;
import utils.CorneliusUtils;

import java.util.*;

public class OregonTrailPlayer {
    public final String id;
    public final String name;

    public Occupation job;
    public int health = 100;
    public LinkedList<Integer> sustenance; //Store food eaten in the last 3 days

    private List<DiseaseEnum> illnesses;
    private Map<String, Integer> immunity;
    private HealthStatus healthStatus;

    public OregonTrailPlayer(String id, String name) {
        this.id = id;
        this.name = name;
        this.sustenance = new LinkedList<>();
        this.illnesses = new ArrayList<>();
        this.immunity = new HashMap<>();
    }

    public HealthStatus getHealthStatus() {
        if (health >= 100) {
            healthStatus = HealthStatus.EXCELLENT;
        }
        else if (health >= 75) {
            healthStatus = HealthStatus.GOOD;
        }
        else if (health >= 50) {
            healthStatus = HealthStatus.FAIR;
        }
        else if (health >= 25) {
            healthStatus = HealthStatus.POOR;
        }
        else if (health > 0) {
            healthStatus = HealthStatus.NEAR_DEATH;
        }
        else {
            healthStatus = HealthStatus.DEAD;
        }

        return  healthStatus;
    }

    public void setHealthStatus(HealthStatus healthStatus) {
        this.healthStatus = healthStatus;
    }

    public List<DiseaseEnum> getIllnesses() {
        return illnesses;
    }

    public void setIllnesses(List<DiseaseEnum> illnesses) {
        this.illnesses = illnesses;
    }

    public Map<String, Integer> getImmunity() {
        return immunity;
    }

    public void setImmunity(Map<String, Integer> immunity) {
        this.immunity = immunity;
    }

    public boolean isAlive() {
        return this.health > 0;
    }

    public String getSickness() {
        return illnesses.size() == 0 ? "None" : illnesses.toString();
    }

    public void consume(int amount) {
        sustenance.add(amount);
        if (sustenance.size() > 3) {
            sustenance.removeFirst();
        }
    }

    public boolean becomeSick() {
        DiseaseEnum[] allDiseases = DiseaseEnum.class.getEnumConstants();
        int rand = CorneliusUtils.randomIntBetween(0, allDiseases.length-1);
        if (this.illnesses.contains(allDiseases[rand])) {
            return false;
        }
        this.illnesses.add(allDiseases[rand]);
        this.immunity.put(allDiseases[rand].name, 0);
        return true;
    }

    public void becomeSick(DiseaseEnum disease) {
        this.illnesses.add(disease);
        this.immunity.put(disease.name, 0);
    }

    public void kill() {
        this.health = 0;
    }
}
