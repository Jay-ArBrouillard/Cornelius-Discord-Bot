package oregontrail;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import oregontrail.enums.DiseaseEnum;
import oregontrail.enums.RationsEnum;
import utils.CorneliusUtils;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Wagon {

    private int oxen;
    private int food;
    private int clothes;
    private int ammo;
    private double pace = 8.0; //Hours traveled per day (1-24)
    private int SPEED = 3; //Miles per hour traveled
    private final List<Part> activeParts;
    private final List<Part> spareParts;
    private final List<OregonTrailPlayer> party;
    private double cash;
    private int wagonWeight;
    private MessageReceivedEvent event;
    public static final int MAX_WEIGHT = 2000;

    public Wagon(OregonTrailPlayer owner, MessageReceivedEvent event) {
        this.event = event;
        party = new ArrayList<>();
        this.party.add(owner);
        populatePartyFromDiscord();

        activeParts = new ArrayList<>();
        activeParts.add(new Tongue());
        activeParts.add(new Axle());
        activeParts.add(new Axle());
        activeParts.add(new Wheel());
        activeParts.add(new Wheel());
        activeParts.add(new Wheel());
        activeParts.add(new Wheel());
        spareParts = new ArrayList<>();
        ammo = 0;
        clothes = 0;
        food = 0;
        oxen = 0;
        wagonWeight = 0;
    }

    public int getOxen() {
        return oxen;
    }

    public void setOxen(int oxen) {
        this.oxen = oxen;
    }

    public int getFood() {
        return food;
    }

    public void setFood(int food) {
        this.food = food;
    }

    public int getClothes() {
        return clothes;
    }

    public void setClothes(int clothes) {
        this.clothes = clothes;
    }

    public int getAmmo() {
        return ammo;
    }

    public void setAmmo(int ammo) {
        this.ammo = ammo;
    }

    public double getPace() {
        return pace;
    }

    public void setPace(double pace) {
        this.pace = pace;
    }

    public List<Part> getActiveParts() {
        return activeParts;
    }

    public List<Part> getSpareParts() {
        return spareParts;
    }

    public List<OregonTrailPlayer> getParty() {
        return party;
    }

    public double getCash() {
        return cash;
    }

    public void setCash(double cash) {
        this.cash = cash;
    }

    public int getWagonWeight() {
        return wagonWeight;
    }

    public String printInventory() {
        StringBuilder sb = new StringBuilder();
        sb.append("**Cash**: $").append(cash).append("\n");
        sb.append("**Oxen**: ").append(oxen).append("\n");
        sb.append("**Clothes**: ").append(clothes).append("\n");
        sb.append("**Ammo**: ").append(ammo).append("\n");
        sb.append("**Wheel** (spare): ").append(getWagonPartCount(Wheel.class.getSimpleName())).append("\n");
        sb.append("**Axle** (spare): ").append(getWagonPartCount(Axle.class.getSimpleName())).append("\n");
        sb.append("**Tongue** (spare): ").append(getWagonPartCount(Tongue.class.getSimpleName())).append("\n");
        sb.append("**Food**: ").append(food);
        return sb.toString();
    }

    private int getWagonPartCount(String partName) {
        int count = 0;
        for (Part part : spareParts) {
            if (part.toString().equals(partName)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Decrease quantity of food base on rations and set the sustenance values for each member that eats.
     */
    public void consumeFood(RationsEnum rations) {
        for (OregonTrailPlayer member : party) {
            if (!member.isAlive()) continue;
            if (food >= rations.quantity) {
                member.consume(rations.quantity);
                food -= rations.quantity;
            }
            else { // If food quantity of less than rations then give everyone except this member 0 for their sustentance
                // Then break from the loop
                int lastOfFood = food;
                for (OregonTrailPlayer m : party) {
                    if (m.name.equals(member.name)) {
                        m.consume(lastOfFood);
                    }
                    else {
                        m.consume(0);
                    }
                }
                food = 0;
                break;
            }
        }
    }


    /**
     * Progress a day in elapsed days. Decrease food and calculate health. Returns true if an event occurred
     * @param game
     * @param traveled
     * @return
     */
    public boolean nextDay(OregonTrailGame game, boolean traveled, boolean onRiver) {
        game.daysElapsed++;
        //Only increase distance traveled when traveled is true and onRiver is false
        if (traveled && !onRiver) game.distanceTraveled += pace * SPEED;
        boolean reachedLandMark = game.landMarks.size() == 0 ? false : game.distanceTraveled >= game.landMarks.peek().getDistance();
        consumeFood(game.rations);
        boolean healthDeath = calculateHealth(traveled, onRiver);
        return reachedLandMark || healthDeath;
    }

    /**
     * Decrease players health based on factors - food, pace, and diseases
     * Returns true if a player dies
     * @param traveled
     * @return
     */
    public boolean calculateHealth(boolean traveled, boolean onRiver) {
        //Local variables used on exponential function
        double A = 0.9;
        double B = 1.132;
        double C = 0.5;

        boolean playerDeath = false;

        for (OregonTrailPlayer member : party) {
            if (!member.isAlive()) continue;
            // Daily Penalty for food
            if (member.sustenance.size() > 0) {
                if (member.sustenance.getLast() == RationsEnum.FILLING.quantity) {
                    decreaseHealth(member, 1);
                }
                else if (member.sustenance.getLast() == RationsEnum.MEAGER.quantity) {
                    decreaseHealth(member, 2);
                }
                else if (member.sustenance.getLast() <= RationsEnum.STARVING.quantity) {
                    decreaseHealth(member, 3);
                }
            }

            // Penalty for amount of food in past 3 days
            if (member.sustenance.size() == 3) {
                int foodConsumeLast3Days = member.sustenance.stream().mapToInt(i -> i.intValue()).sum();
                if (foodConsumeLast3Days <= 1) {
                    decreaseHealth(member, 8);
                }
                else if (foodConsumeLast3Days <= 3) {
                    decreaseHealth(member, 5);
                }
                else if (foodConsumeLast3Days <= 5) {
                    decreaseHealth(member, 3);
                }
                else if (foodConsumeLast3Days <= 7) {
                    decreaseHealth(member, 2);
                }
                else if (foodConsumeLast3Days <= 9) {
                    decreaseHealth(member, 1);
                }
            }

            // Daily Reward and Penalty for pace
            if (traveled && !onRiver && pace > 8) {
                double penalty = A * Math.pow(B, pace - 8) + C; // Exponential function for increased pace
                decreaseHealth(member, (int)Math.round(penalty));
            }

            // Penalty for Diseases
            if (member.getIllnesses().size() > 0) {
                for (DiseaseEnum disease : member.getIllnesses()) {
                    decreaseHealth(member, disease.sicknessPenalty);
                    double infectOther = CorneliusUtils.randomNumber01();
                    // Spread disease to other members
                    if (infectOther <= disease.infectionRate) {
                        // Find members that don't have this disease
                        List<OregonTrailPlayer> eligibleMembers = new ArrayList<>();
                        for (OregonTrailPlayer player : party) {
                            if (!player.getIllnesses().stream().anyMatch(d -> disease.name.equals(d.name))) {
                                eligibleMembers.add(player);
                            }
                        }
                        if (eligibleMembers.size() > 0) {
                            int rand = CorneliusUtils.randomIntBetween(0, eligibleMembers.size()-1);
                            OregonTrailPlayer other = eligibleMembers.get(rand);
                            other.becomeSick(disease);
                            event.getChannel().sendMessage("`" + member.name + "` has spread **" + disease.name + "** to **" + other.name + "**!").queue();
                        }
                    }
                }
            }

            // If member just died
            if (!member.isAlive()) {
                if (member.getIllnesses().size() > 0) {
                    int rand = CorneliusUtils.randomIntBetween(0, member.getIllnesses().size()-1);
                    DiseaseEnum selectedDisease = member.getIllnesses().get(rand);
                    EmbedBuilder diseaseDeath = new EmbedBuilder();
                    diseaseDeath.setColor(Color.GREEN);
                    if (DiseaseEnum.CHOLERA.name.equals(selectedDisease.name)) {
                        diseaseDeath.setImage("https://lh3.googleusercontent.com/pw/ACtC-3dAe-WEOmwOWsnJXfhQIr2_OdbkPgv9yzpkB7-GIkD6uMOMsaRvbVQNHp8cXNmeUztXbImmTNCab7nuu0BlpFZMCul54Zh4-8kpLwUQs7irtrya4DX5qUtbAMX1orHrG_2Sl8WziGbFZ4JqnpL8qhA=w300-h207-no?authuser=1");
                    }
                    else if (DiseaseEnum.DYSENTERY.name.equals(selectedDisease.name)) {
                        diseaseDeath.setImage("https://lh3.googleusercontent.com/pw/ACtC-3ezpwOJhpkZddW3K98ksrVbaooEgeGsVGDPZc_7Amo3wYng3GCWHVr4p-Quu5ujgA2aTEWyNxqWGVDBZ8DYj3l_5ysTiWFHQvXI8WlVVlCh0k9Q8USL96RuTtUYgjV5XTSINV3na5KtZnycHDaWdrU=w300-h207-no?authuser=1");
                    }
                    else if (DiseaseEnum.MEASLES.name.equals(selectedDisease.name)) {
                        diseaseDeath.setImage("https://lh3.googleusercontent.com/pw/ACtC-3e0lhlKwNziL6o0eaYMjgoYyoDzTU5pVExLTTSCLgb95OSo7_IWOAMGtsduaXx54P9Wq9Y5AUjW7aJoSL053DjmifdnC3FsOBh2KJJEWiOj0PODG3OF89l8fgY2hQlpQyjy4CM5PKCR8NBOySEsKuM=w300-h207-no?authuser=1");
                    }
                    else if (DiseaseEnum.TYPHOID.name.equals(selectedDisease.name)) {
                        diseaseDeath.setImage("https://lh3.googleusercontent.com/pw/ACtC-3csGbPzpgKAe_TJgCgo-QDYavrUIzI8tlXOJYcwalZoKJecccNKUDpIK0V2H3yW1Ih6pgZRLMBIBx9XgrtSROTnAxM_yOeZBptEaEBqymoPd8ZXthIPokB7jPTfY9wBkKSzaWzhDRR8yz3erpt4MoY=w300-h207-no?authuser=1");
                    }
                    diseaseDeath.setFooter("R.I.P. -" + member.name);
                    event.getChannel().sendMessage(diseaseDeath.build()).queue();

                }
                else {
                    event.getChannel().sendMessage(member.name + " died an average death of inadequate living").queue();
                }
                playerDeath = true;
            }
        }

        return playerDeath;
    }

    /**
     * Decreases this members health by passed value. Kills member if value is greater than or equal to health
     * @param member
     * @param value
     */
    public void decreaseHealth(OregonTrailPlayer member, int value) {
        int health = member.health;
        if ((health - value) > 0) {
            member.health -= value;
        }
        else {
            member.kill();
        }
    }

    /**
     * Adds 5 random names from this discord guild to party. If fail, then add hard coded names
     * {"Wild Walter", "Hitlin Clinton", "Cowpoke Clyde", "Flap-jack Frances"}
     */
    private void populatePartyFromDiscord() {
        //Chooses random members from discord
        this.event.getGuild().loadMembers().onSuccess(membersList -> {
            Collections.shuffle(membersList);
            for (Member member : membersList) {
                if (party.size() == 5) break;
                if (!event.getAuthor().getName().equals(member.getEffectiveName())) {
                    party.add(new OregonTrailPlayer(null, member.getEffectiveName()));
                }
            }
            if (party.size() < 5) {
                String [] names = new String[] {"Wild Walter", "Hitlin Clinton", "Cowpoke Clyde", "Flap-jack Frances"};
                for (int i = 0; party.size() < 5; i++) {
                    party.add(new OregonTrailPlayer(null, names[i]));
                }
            }
        }).onError(x -> {
            if (party.size() < 5) {
                String [] names = new String[] {"Wild Walter", "Hitlin Clinton", "Cowpoke Clyde", "Flap-jack Frances"};
                for (int i = 0; party.size() < 5; i++) {
                    party.add(new OregonTrailPlayer(null, names[i]));
                }
            }
        });
    }

    /**
     * Random member in this party becomes sick with a random disease.
     * @return
     */
    public OregonTrailPlayer giveRandomSickness() {
        List<OregonTrailPlayer> livingMembers = getLivingMembers();
        Collections.shuffle(livingMembers);
        for (OregonTrailPlayer player : livingMembers) {
            boolean effected = player.becomeSick();
            if (effected) {
                return player;
            }
        }
        return null;
    }

    /**
     * Returns all members in party that are alive
     * @return
     */
    public List<OregonTrailPlayer> getLivingMembers() {
        List<OregonTrailPlayer> livingMembers = new ArrayList<>();
        for (OregonTrailPlayer p : party) {
            if (p.isAlive()) {
                livingMembers.add(p);
            }
        }
        return livingMembers;
    }

    /**
     * Removes a random item from active parts
     * @return
     */
    public Part breakPart() {
        int rand = CorneliusUtils.randomIntBetween(0, activeParts.size()-1);
        return activeParts.remove(rand);
    }

    /**
     * Sets a random member in party to 0 health
     * @return
     */
    public OregonTrailPlayer killRandomPartyMember() {
        List<OregonTrailPlayer> livingMembers = getLivingMembers();
        int rand = CorneliusUtils.randomIntBetween(0, livingMembers.size()-1);
        OregonTrailPlayer deadMember = livingMembers.get(rand);
        deadMember.kill();
        return deadMember;
    }

    /**
     * Removes one random item from the wagon
     *
     * @return String what was removed
     */
    public String removeRandomItem() {
        final int itemCt = countItems();
        final int foodRatio = food;
        final int clothesRatio = foodRatio + clothes;
        final int bulletRatio = clothesRatio + ammo;
        final int wheelRatio = bulletRatio + countWheels();
        final int axleRatio = wheelRatio + countAxles();

        if (itemCt > 0) {
            final int i = CorneliusUtils.randomIntBetween(0, itemCt);
            if (i < foodRatio) {
                food -= 1;
                return "food";
            } else if (i < clothesRatio) {
                clothes -= 1;
                return "clothes";
            } else if (i < bulletRatio) {
                ammo -= 1;
                return "bullet";
            } else if (i < wheelRatio) {
                spareParts.remove(new Wheel());
                return "wheel";
            } else if (i < axleRatio) {
                spareParts.remove(new Axle());
                return "axle";
            } else if (spareParts.contains(new Tongue())) {
                spareParts.remove(new Tongue());
                return "tongue";
            }
        }
        return null;
    }

    /**
     * Count all the items in the wagon. Not including cash
     * @return
     */
    private int countItems() {
        return food + clothes + ammo + spareParts.size();
    }

    /**
     * Count how many spare axles are being carried
     *
     *
     * @return Count of spare axles
     */
    public int countAxles() {

        int ret = 0;
        for (Part part : spareParts) {
            if (part instanceof Axle)
                ret++;
        }
        return ret;
    }

    /**
     * Count how many spare wheels are being carried
     *
     *
     * @return Count of spare wheels
     */
    public int countWheels() {
        int ret = 0;
        for (Part part : spareParts) {
            if (part instanceof Wheel)
                ret++;
        }
        return ret;
    }

    /**
     * Count how many spare tongues are being carried
     *
     *
     * @return Count of spare tongues
     */
    public int countTongues() {

        int ret = 0;
        for (Part part : spareParts) {
            if (part instanceof Tongue)
                ret++;
        }
        return ret;
    }

    /**
     * Given a player, will bring them back to life with 50 health, or cure all illnesses, or heal a random value between 1 and 50
     * @param player
     */
    public void recoverMember(OregonTrailPlayer player) {
        if (!player.isAlive()) {
            player.sustenance.clear();
            player.getIllnesses().clear();
            player.health = 50;
        }
        else { // Player is alive
            if (player.getIllnesses().size() > 0) {
                // Heal all their illnesses
                player.getIllnesses().clear();
            }
            else { // Has no illnesses
                // Increase their health
                player.health += CorneliusUtils.randomIntBetween(1, 50);
            }
        }
    }

    /**
     * Picks 3 random items or people to lose
     */
    public void failedRiverCrossing() {
        StringBuilder message = new StringBuilder();
        message.append("You lose:\n");
        for (int i = 0; i < 3; i++) {
            if (CorneliusUtils.randomNumber01() <= 0.20) {
                List<OregonTrailPlayer> livingMembers = getLivingMembers();
                OregonTrailPlayer drown = livingMembers.get(CorneliusUtils.randomIntBetween(0, livingMembers.size()-1));
                drown.kill();
                message.append("\t").append(drown.name).append(" (drowned)\n");
            }
            else {
                String item = removeRandomItem();
                if (item == null) {
                    oxen = oxen - 1 < 0 ? 0 : oxen - 1;
                    message.append("\t").append("1 oxen\n");
                }
                else {
                    if (item.equals("food")) {
                        int foodLose = CorneliusUtils.randomIntBetween(0, food / 3);
                        food -= foodLose;
                        message.append("\t").append(foodLose).append(" lbs of food\n");
                    }
                    else if (item.equals("clothes")) {
                        int clothesLose = CorneliusUtils.randomIntBetween(0, clothes);
                        clothes -= clothesLose;
                        message.append("\t").append(clothesLose).append(" sets of clothing\n");
                    }
                    else if (item.equals("bullet")) {
                        int bulletLose = CorneliusUtils.randomIntBetween(0, ammo);
                        ammo -= bulletLose;
                        message.append("\t").append(bulletLose).append(" bullets\n");
                    }
                    else {
                        message.append("\t").append("1 ").append(item).append("\n");
                    }
                }
            }
        }
        event.getChannel().sendMessage(message.toString()).queue();
    }
}
