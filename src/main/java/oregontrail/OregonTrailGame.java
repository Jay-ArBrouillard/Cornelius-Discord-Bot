package oregontrail;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import oregontrail.enums.DiseaseEnum;
import oregontrail.enums.RationsEnum;
import oregontrail.location.*;
import oregontrail.occupation.*;
import oregontrail.store.*;
import utils.CorneliusUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;


import static commands.OregonTrailCommand.getOptionsString;
import static oregontrail.OTGameStatus.*;

public class OregonTrailGame {

    public Double END_DISTANCE; //1807-2083 Total miles depending on path chosen

    public OregonTrailPlayer owner;
    public Wagon wagon;
    public GeneralStore store;
    public int distanceTraveled = 0;
    public int daysElapsed = 0;
    public RationsEnum rations = RationsEnum.FILLING;
    public int score = 0;
    private boolean rest = false; //Did you rest this turn
    public Queue<Location> landMarks;
    public Location currentLocation;
    public List<Location> splitPathsChoosen = new LinkedList<>();
    //Use for build progress image
    public int startX = 0;
    public int endX = 102;

    private MessageReceivedEvent event;
    private static DecimalFormat formatPercent = new DecimalFormat("##0.##");

    public OregonTrailGame(MessageReceivedEvent event) {
        this.event = event;
        owner = new OregonTrailPlayer(event.getAuthor().getId(), event.getAuthor().getName());
        wagon = new Wagon(owner, event);
        landMarks = new LinkedList<>();
        landMarks.add(new KansasRiverCrossing(102));
        landMarks.add(new BigBlueRiverCrossing(185));
        landMarks.add(new FortKearney(304));
        landMarks.add(new FortLaramie(640));
        landMarks.add(new SouthPass(932));
    }

    public OTGameStatus play(String optionNumber) {
        if (optionNumber == null || optionNumber.isEmpty()) {
            return INVALID_INPUT;
        }
        optionNumber = optionNumber.trim();
        String[] split = optionNumber.split(" ");
        rest = false;
        if (CorneliusUtils.isNumeric(split[0])) {
            switch (split[0]) {
                case "1": //Continue traveling
                    if (split.length >= 2 && split[1] != null && CorneliusUtils.isNumeric(split[1])) {
                        int days = Integer.parseInt(split[1]);
                        for (int i = 0; i < days; i++) {
                            boolean event1 = generateRandomEvents();
                            boolean event2 = wagon.nextDay(this, true, false);
                            if (event1 || event2 || isGameOver()) break;
                        }
                    }
                    else {
                        generateRandomEvents();
                        wagon.nextDay(this, true, false);
                    }
                    break;
                case "2": //Show inventory
                    event.getChannel().sendMessage(wagon.printInventory()).queue();
                    break;
                case "3": //Rest
                    if (split.length >= 2 && split[1] != null && CorneliusUtils.isNumeric(split[1])) {
                        int days = Integer.parseInt(split[1]);
                        for (int i = 0; i < days; i++) {
                            rest();
                            boolean event1 = generateRandomEvents();
                            boolean event2 = wagon.nextDay(this, false, false);
                            if (event1 || event2 || isGameOver()) break;
                        }
                    }
                    else {
                        rest();
                        generateRandomEvents();
                        wagon.nextDay(this, false, false);
                    }
                    rest = true;
                    break;
                case "4": //Hunt
                    hunt();
                    generateRandomEvents();
                    wagon.nextDay(this, false, false);
                    break;
                case "5": //Change pace
                    if (split.length >= 2) {
                        if (CorneliusUtils.isNumeric(split[1])) {
                            double temp = Double.parseDouble(split[1]);
                            if (temp >= 1.0 && temp <= 24.0) {
                                wagon.setPace(temp);
                                return RUNNING;
                            }
                        }
                    }
                    return INVALID_INPUT;
                case "6": //Change Rations
                    if (split.length >= 2) {
                        if (RationsEnum.FILLING.name.equals(split[1])) {
                            rations = RationsEnum.FILLING;
                            return RUNNING;
                        }
                        else if (RationsEnum.MEAGER.name.equals(split[1])) {
                            rations = RationsEnum.MEAGER;
                            return RUNNING;
                        }
                        else if (RationsEnum.STARVING.name.equals(split[1])) {
                            rations = RationsEnum.STARVING;
                            return RUNNING;
                        }
                    }
                    return INVALID_INPUT;
                case "7": //Kill Member
                    if (split.length >= 2) {
                        String nameToKill = optionNumber.substring(optionNumber.indexOf(" ") + 1);
                        nameToKill = nameToKill.trim();
                        for (OregonTrailPlayer player : wagon.getParty()) {
                            if (nameToKill.equalsIgnoreCase(player.name)) {
                                player.kill();
                                event.getChannel().sendMessage("You killed " + player.name + ". Why bro? Did he really deserve that?").queue();
                                return RUNNING;
                            }
                        }
                    }
                    return INVALID_INPUT;
                case "8": //Exit game
                    return QUIT;
            }
        }
        return gameOver();
    }

    /**
     * Return true for potentially game ending events such as illness, sudden death, oxen death, etc.
     * @return
     */
    private boolean generateRandomEvents() {
        int rand = CorneliusUtils.randomIntBetween(0, 102);
        if (rand < 4) { // Catch Sickness
            OregonTrailPlayer player = wagon.giveRandomSickness();
            if (player != null) {
                event.getChannel().sendMessage("`"+ player.name + "` has been diagnosed with **" + player.getIllnesses().get(player.getIllnesses().size()-1) + "**!").queue();
                return true;
            }
            return false;
        }
        else if (rand < 6) { // Random Part Breakdown
            Part part = wagon.breakPart();
            for (Part p : wagon.getSpareParts()) {
                if ((p instanceof Tongue && part instanceof Tongue) || (p instanceof Axle && part instanceof Axle) || (p instanceof Wheel && part instanceof Wheel)) {
                    wagon.getActiveParts().add(p);
                    wagon.getSpareParts().remove(p);
                    break;
                }
            }
            event.getChannel().sendMessage("Your " + part.toString() + " has broken down").queue();
            return true;
        }
        else if (rand < 7) {
            //Random party member death
            OregonTrailPlayer player = wagon.killRandomPartyMember();
            EmbedBuilder death = new EmbedBuilder();
            death.setColor(Color.GREEN);
            death.setTitle(player.name + " suddenly died");
            death.setImage("https://lh3.googleusercontent.com/pw/ACtC-3e2IneIrxRmTvhETM4ijxNi-g-cswNAre18wEH5R7NzoUqKae5aDOC5hsC2aT594KpwAWCoxVnuRYI2NqZJerF4VqMUbVYrRY3dVOngmD2jNkHTCPa3d5kynkfzuT5tkPa_yQ3mufUDljUcBUlPLxE=w1001-h350-no?authuser=1");
            death.setFooter("Noone knows what happened. But fuck that guy. -" + player.name);
            event.getChannel().sendMessage(death.build()).queue();
            return true;
        }
        else if (rand < 8){
            //Random Oxen Death
            if (wagon.getOxen() > 1){
                wagon.setOxen(wagon.getOxen() - 1);
                event.getChannel().sendMessage("1 of your oxen suddenly died!").queue();
            }
            return false;
        }
        else if (rand < 11) {
            // Theft
            String item = wagon.removeOneRandomItem();
            if (item != null) {
                event.getChannel().sendMessage("1 " + item + " has been stolen from you.").queue();
            }
            return false;
        }
        else if (rand < 12) {
            // Attacked
            event.getChannel().sendMessage("Raiders invaded your camp during the night!").queue();
            for (OregonTrailPlayer player : wagon.getLivingMembers()) {
                double wounded = CorneliusUtils.randomNumber01();
                if (wounded <= 0.33) {
                    int damage = CorneliusUtils.randomIntBetween(0, player.health);
                    wagon.decreaseHealth(player, damage);
                    event.getChannel().sendMessage(player.name + " suffered -" + damage + " damage").queue();
                    if (!player.isAlive()) {
                        return true;
                    }
                }
            }
            return false;
        }
        else if (rand < 17) {
            // Came across a farmer
            int gainedFood = CorneliusUtils.randomIntBetween(0, 25) + 25;
            wagon.setFood(wagon.getFood() + gainedFood);
            event.getChannel().sendMessage("You came across a generous farmer and are gifted " + Integer.valueOf(gainedFood) + "lbs of food!").queue();
            return false;
        }
        else if (rand < 19) {
            // Came across abandoned wagon
            if (wagon.getSpareParts().size() < 9) {
                int axleCount = wagon.countAxles();
                int tongueCount = wagon.countTongues();
                int wheelCount = wagon.countWheels();
                if (axleCount < 3) {
                    wagon.getSpareParts().add(new Axle());
                    event.getChannel().sendMessage("You find an abandoned wagon and gather the parts from the wagon.\n You've gained:\n1 Axle").queue();
                }
                else if (tongueCount < 3) {
                    wagon.getSpareParts().add(new Tongue());
                    event.getChannel().sendMessage("You find an abandoned wagon and gather the parts from the wagon.\n You've gained:\n1 Tongue.").queue();
                }
                else if (wheelCount < 3) {
                    wagon.getSpareParts().add(new Wheel());
                    event.getChannel().sendMessage("You find an abandoned wagon and gather the parts from the wagon.\n You've gained:\n1 Wheel.").queue();
                }
            }
            return false;
        }
        else if (rand < 20) {
            // Random Party member recovery (death, health, or sickness)
            List<OregonTrailPlayer> allMembers = wagon.getParty();
            int r = CorneliusUtils.randomIntBetween(0, allMembers.size()-1);
            OregonTrailPlayer selected = allMembers.get(r);
            boolean wasAlive = selected.isAlive();
            boolean hadIllnesses = selected.getIllnesses().size() > 0;
            int previousHp = selected.health;
            wagon.recoverMember(selected);
            if (!wasAlive && selected.isAlive()) {
                event.getChannel().sendMessage("By a miracle of god, " + selected.name + ", was brought back to life!").queue();
            }
            else if (hadIllnesses && selected.getIllnesses().size() == 0) {
                event.getChannel().sendMessage(selected.name + " ate some dino vitamins last night and recovered from all illnesses").queue();
            }
            else {
                event.getChannel().sendMessage(selected.name + " had extremely good sleep last night +" + (selected.health - previousHp) + " health").queue();
            }
            return false;
        }
        else if (rand < 23) {
            // Random player eats too much food
            List<OregonTrailPlayer> livingMembers = wagon.getLivingMembers();
            int r = CorneliusUtils.randomIntBetween(0, livingMembers.size()-1);
            OregonTrailPlayer selected = livingMembers.get(r);
            int foodEaten = CorneliusUtils.randomIntBetween(20, 200);
            wagon.setFood(wagon.getFood() - foodEaten);
            EmbedBuilder munchies = new EmbedBuilder();
            munchies.setColor(Color.YELLOW);
            munchies.setImage("https://lh3.googleusercontent.com/pw/ACtC-3ftTEk_a_9HdWzXpAsp6xPmjB64z0UzqVvyN8rpkUiGimG04UBZfrqItH8bnlXkbohz-jVWS-2BmslOuUcshvMXQYA_MTOmkquHyTSeRY3-DhoO1ZDa482w9h1c4QnzUIWAiRQTmfhGBXA8VE0mH5g=w760-h467-no?authuser=1");
            munchies.setFooter(selected.name + " got stupid stoned the night before and ate a lot of food when their munchies kicked in.\nYou lose " + foodEaten + " lbs of food");
            event.getChannel().sendMessage(munchies.build()).queue();
            return false;
        }
        else if (rand < 25) {
            //Wrong trail lose days
        }

        return false;
    }


    /**
     * Return boolean if game is over
     * @return
     */
    public boolean isGameOver() {
        // All players dead - LOSE
        if (wagon.getParty().stream().allMatch(m -> m.health <= 0)) {
            return true;
        }

        // WIN
        if (END_DISTANCE != null && distanceTraveled >= END_DISTANCE) {
            return true;
        }

        // No oxen alive - LOSE
        if (wagon.getOxen() == 0) {
            return true;
        }

        // Wagon broke down and no spare parts - LOSE
        if (wagon.getActiveParts().size() != 7) {
            return true;
        }
        return false;
    }

    /**
     * Return true for win, else false for loss
     * @return
     */
    public boolean isWin() {
        // All players dead - LOSE
        if (wagon.getParty().stream().allMatch(m -> m.health <= 0)) {
            return false;
        }

        // WIN
        if (END_DISTANCE != null && distanceTraveled >= END_DISTANCE) {
            return true;
        }
        return false;
    }

    /**
     * Return OTGameStatus if game is over and send messages
     * @return
     */
    public OTGameStatus gameOver() {
        // All players dead - LOSE
        if (wagon.getParty().stream().allMatch(m -> m.health <= 0)) {
            buildProgressImage();
            event.getChannel().sendFile(new File("src/main/java/oregontrail/gameState.png")).queue();
            printTeamConditions();

            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Everyone died");
            eb.setImage("https://lh3.googleusercontent.com/pw/ACtC-3e6dc8gd5Nkg0FPramKwXKHSZH3Igt28__-O-TEHqsnuBMx6A0TWbG9g6TjXdEC_U1p4s71EETG_75o99y1DB-BjmlMtSJYsbhHM2Gini5UpGNiSUZ1fdhxtAS80EqrFAMdv2RjVL0mfEavOGUyhvE=w1001-h350-no?authuser=1");
            eb.setFooter("Developed by Jay-Ar - https://github.com/Jay-ArBrouillard/Cornelius-Discord-Bot");
            event.getChannel().sendMessage(eb.build()).queue();
            return LOSE;
        }

        // WIN
        if (END_DISTANCE != null && distanceTraveled >= END_DISTANCE) {
            //Formula for score
            EmbedBuilder eb = new EmbedBuilder();
            eb.setImage("https://lh3.googleusercontent.com/pw/ACtC-3cV-AT_mz5aa2SckZ4h4efAzepBYf8aPJTcgY2e5sXAAmUpco74KzW1jZiGFpDlERK914sZPVLyfAzDG3n2YTnOeNd7ExFs76hQg1vScdciZXqlDe24BtF7Bp7ppBGDpVfA5oqmbBzD-AcviVTeVcg=w800-h474-no?authuser=1");

            // 400 for each living member plus the value of their health
            int tempMemberScore = 0;
            for (OregonTrailPlayer member : wagon.getParty()) {
                if (member.isAlive()) {
                    tempMemberScore += 400 + member.health;
                }
            }

            eb.addField(wagon.getLivingMembers().size() + " survived X 400 + (their health) =", String.valueOf(tempMemberScore), false);
            score += tempMemberScore;
            score += Math.round(wagon.getFood() / 25.0);
            eb.addField(wagon.getFood() + " pounds of food / 25", String.valueOf(Math.round(wagon.getFood() / 25.0)), false);
            score += Math.round(wagon.getCash() / 5.0);
            eb.addField(wagon.getCash() + " dollars / 5", String.valueOf(Math.round(wagon.getCash() / 5.0)), false);
            score += Math.round(wagon.getAmmo() / 50.0);
            eb.addField(wagon.getAmmo() + " ammo / 50", String.valueOf(Math.round(wagon.getAmmo() / 50.0)), false);
            score += wagon.getOxen() * 4;
            eb.addField(wagon.getOxen() + " oxen X 4", String.valueOf(wagon.getOxen() * 4), false);
            score += wagon.getClothes() * 2;
            eb.addField(wagon.getClothes() + " sets of clothing X 2", String.valueOf(wagon.getClothes() * 2), false);
            int tempSpareWagonPartsQuantity = 0;
            // TODO 50 for working wagon
            for (Part part : wagon.getSpareParts()) {
                tempSpareWagonPartsQuantity++;
            }

            eb.addField(tempSpareWagonPartsQuantity + " spare wagon parts X 2", String.valueOf(tempSpareWagonPartsQuantity * 2), false);
            score += tempSpareWagonPartsQuantity * 2;
            eb.addField("Final Bonus: " + score + " X " + owner.job.bonusMultipler, String.valueOf(score * owner.job.bonusMultipler), false);
            score *= owner.job.bonusMultipler;
            eb.setTitle("Score: " + score);
            eb.setFooter("Developed by Jay-Ar - https://github.com/Jay-ArBrouillard/Cornelius-Discord-Bot");

            buildProgressImage();
            event.getChannel().sendFile(new File("src/main/java/oregontrail/gameState.png")).queue();
            printTeamConditions();
            event.getChannel().sendMessage(eb.build()).queue();
            return WIN;
        }

        // No oxen alive - LOSE
        if (wagon.getOxen() == 0) {
            buildProgressImage();
            event.getChannel().sendFile(new File("src/main/java/oregontrail/gameState.png")).queue();
            printTeamConditions();

            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("All of your oxen are dead");
            eb.setImage("https://lh3.googleusercontent.com/pw/ACtC-3eZ6TXI3fTEFHzErgZISEgBqon4aZBeX7wyiLHuO7Tl1Kw2gVOosKgkWZQlLMsJUa5XlhL0Q1tdF7XI1VO2SulwHjuo872B06KQWuj8KA326QZANZFF8jH1yT2BvX1YPJubJLTUWzXQlO3zqO6aY5M=w208-h242-no?authuser=1");
            eb.setFooter("Developed by Jay-Ar - https://github.com/Jay-ArBrouillard/Cornelius-Discord-Bot");
            event.getChannel().sendMessage(eb.build()).queue();
            return LOSE;
        }

        // Wagon broke down and no spare parts - LOSE
        if (wagon.getActiveParts().size() != 7) {
            buildProgressImage();
            event.getChannel().sendFile(new File("src/main/java/oregontrail/gameState.png")).queue();
            printTeamConditions();

            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Your wagon broke down and you had no spare parts");
            eb.setImage("https://lh3.googleusercontent.com/pw/ACtC-3dtiQ8qV_8QioYwD1avO23D0Vg8T809WPSeJmnImR7jRyfeVVQriCI7t7LBY95MvFFVW4HUgheubIbCUC5SPmDgfCwrOHVmRkOl9JNMOK3Fu8IG5sg-2BPyfiIUX_hFSWe3C4US1UXHepNvwfRVUI8=w371-h280-no?authuser=1");
            eb.setFooter("Developed by Jay-Ar - https://github.com/Jay-ArBrouillard/Cornelius-Discord-Bot");
            event.getChannel().sendMessage(eb.build()).queue();
            return LOSE;
        }

        return RUNNING;
    }


    /**
     * -----------------------------------------------
     * | ANIMAL                |    POUNDS OF FOOD   |
     * -----------------------------------------------
     * | Squirrel              | 1 lb.               |
     * | Buffalo               | 350-500 lbs.        |
     * | Rabbit                | 2 lbs.              |
     * | Deer                  | 50 lbs.             |
     * | Mallard Duck          | 1 lb.               |
     * | Canada Goose          | 2 lb.               |
     * | Gazelle (?)           | 35 lb.              |
     * | Caribou               | 300-350 lbs.        |
     * | Bear                  | 100 lbs.            |
     * -----------------------------------------------
     */
    private void hunt() {
        int animal = CorneliusUtils.randomIntBetween(0, 9);
        int rand = CorneliusUtils.randomIntBetween(0, 10);
        int ammoUsed = CorneliusUtils.randomIntBetween(1, 10);
        double chance = 0;
        int groupSize = wagon.getParty().size();
        if (groupSize >= 5) {
            chance = 9;
        }
        else if (groupSize >= 4) {
            chance = 8;
        }
        else if (groupSize >= 3) {
            chance = 7;
        }
        else if (groupSize >= 2) {
            chance = 6;
        }
        else if (groupSize >= 1) {
            chance = 5;
        }

        if (wagon.getAmmo() <= 0) {
            chance /= 2.0;
        }

        if (animal == 0) {
            if (rand < chance) {
                event.getChannel().sendMessage(String.format("Your crew killed a squirrel! Added %d food to your wagon", 1)).queue();
                wagon.setFood(wagon.getFood() + 1);
            }
            else {
                event.getChannel().sendMessage(String.format("While hunting your crew had a %s%% to kill a squirrel and failed. Feels bad man.", formatPercent.format(chance * 10))).queue();
            }
        } else if (animal == 1) {
            if (rand < chance) {
                event.getChannel().sendMessage(String.format("Your crew killed a buffalo! Added %d food to your wagon", 350)).queue();
                wagon.setFood(wagon.getFood() + 350);
            }
            else {
                event.getChannel().sendMessage(String.format("While hunting your crew had a %s%% to kill a buffalo and failed. Feels bad man.", formatPercent.format(chance * 10))).queue();
            }
        } else if (animal == 2) {
            if (rand < chance) {
                event.getChannel().sendMessage(String.format("Your crew killed a rabbit! Added %d food to your wagon", 2)).queue();
                wagon.setFood(wagon.getFood() + 2);
            }
            else {
                event.getChannel().sendMessage(String.format("While hunting your crew had a %s%% to kill a rabbit and failed. Feels bad man.", formatPercent.format(chance * 10))).queue();
            }
        } else if (animal == 3) {
            if (rand < chance) {
                event.getChannel().sendMessage(String.format("Your crew killed a deer! Added %d food to your wagon", 50)).queue();
                wagon.setFood(wagon.getFood() + 50);
            }
            else {
                event.getChannel().sendMessage(String.format("While hunting your crew had a %s%% to kill a deer and failed. Feels bad man.", formatPercent.format(chance * 10))).queue();
            }
        } else if (animal == 4) {
            if (rand < chance) {
                event.getChannel().sendMessage(String.format("Your crew killed a duck! Added %d food to your wagon", 1)).queue();
                wagon.setFood(wagon.getFood() + 1);
            }
            else {
                event.getChannel().sendMessage(String.format("While hunting your crew had %s%% to kill a duck and failed. Feels bad man.", formatPercent.format(chance * 10))).queue();
            }
        } else if (animal == 5) {
            if (rand < chance) {
                event.getChannel().sendMessage(String.format("Your crew killed a canadian goose! Added %d food to your wagon", 2)).queue();
                wagon.setFood(wagon.getFood() + 2);
            }
            else {
                event.getChannel().sendMessage(String.format("While hunting your crew had %s%% to kill a canadian goose and failed. Feels bad man.", formatPercent.format(chance * 10))).queue();
            }
        } else if (animal == 6) {
            if (rand < chance) {
                event.getChannel().sendMessage(String.format("Your crew killed a gazelle! Added %d food to your wagon", 35)).queue();
                wagon.setFood(wagon.getFood() + 35);
            }
            else {
                event.getChannel().sendMessage(String.format("While hunting your crew had %s%% to kill a canadian gazelle and failed. Feels bad man.", formatPercent.format(chance * 10))).queue();
            }
        } else if (animal == 7) {
            if (rand < chance) {
                event.getChannel().sendMessage(String.format("Your crew killed a caribou! Added %d food to your wagon", 300)).queue();
                wagon.setFood(wagon.getFood() + 300);
            }
            else {
                event.getChannel().sendMessage(String.format("While hunting your crew had %s%% to kill a caribou and failed. Feels bad man.", formatPercent.format(chance * 10))).queue();
            }
        } else if (animal == 8) {
            if (rand < chance) {
                event.getChannel().sendMessage(String.format("Your crew killed a black bear! Added %d food to your wagon", 10)).queue();
                wagon.setFood(wagon.getFood() + 100);
            }
            else {
                event.getChannel().sendMessage(String.format("While hunting your crew had %s%% to kill a black bear and failed. Feels bad man.", formatPercent.format(chance * 10))).queue();
            }
        } else {
            event.getChannel().sendMessage("After searching for hours you found no animals in the area...").queue();
        }

        // Use ammo whether you get a kill or not
        if (wagon.getAmmo() >= ammoUsed) {
            wagon.setAmmo(wagon.getAmmo() - ammoUsed);
        }
        else {
            wagon.setAmmo(0);
            event.getChannel().sendMessage("You're out of ammo. Having ammo will signifcantly increase success rate of hunting...").queue();
        }
    }

    /**
     * Restore health and heal illnesses
     */
    private void rest() {
        int minHeal = owner.job instanceof Doctor ? 2 : 1;
        int maxHeal = 5;
        for (OregonTrailPlayer member : wagon.getParty()) {
            if (!member.isAlive()) continue;
            //Random heal
            member.health += CorneliusUtils.randomIntBetween(minHeal, maxHeal);

            if (member.getIllnesses().size() > 0) {
                List<DiseaseEnum> diseasesHealed = new LinkedList<>();
                for (DiseaseEnum disease : member.getIllnesses()) {
                    double healChance = CorneliusUtils.randomNumber01();
                    if (healChance <= 0.5) {
                        diseasesHealed.add(disease);
                        member.getImmunity().put(disease.name, 0);
                        event.getChannel().sendMessage(member.name + " healed from " + disease.name + " (Immune to " + disease.name + " 7 days)").queue();
                    }
                }
                //Remove healed diseases
                member.getIllnesses().removeAll(diseasesHealed);
            }
        }
    }

    public OTGameStatus processMenuOption(String optionNumber) {
        if (optionNumber != null && CorneliusUtils.isNumeric(optionNumber)) {
            optionNumber = optionNumber.trim();
            switch (optionNumber) {
                case "1": //Start new game
                    return START;
                case "2": //Resume existing game
                    return RUNNING;
                case "3": //See rules
                    return RULES;
                case "4": //Exit game
                    return QUIT;
                default:
                    throw new RuntimeException("Error processing menu option");
            }
        }
        return INVALID_INPUT;
    }

    public boolean setOccupation(String optionNumber) {
        if (optionNumber != null && CorneliusUtils.isNumeric(optionNumber)) {
            optionNumber = optionNumber.trim();
            switch (optionNumber) {
                case "1":
                    owner.job = new Banker();
                    break;
                case "2":
                    owner.job = new Doctor();
                    break;
                case "3":
                    owner.job = new Merchant();
                    break;
                case "4":
                    owner.job = new Blacksmith();
                    break;
                case "5":
                    owner.job = new Carpenter();
                    break;
                case "6":
                    owner.job = new Saddlemaker();
                    break;
                case "7":
                    owner.job = new Farmer();
                    break;
                case "8":
                    owner.job = new Teacher();
                    break;
                default:
                    return false;
            }
            //Set cash to starting cash based on occupation
            wagon.setCash(owner.job.startingCash);
        }

        if (owner.job != null) return true;
        return false;
    }

    public void buildProgressImage() {
        //Begin with mountains as background
        BufferedImage result = null;
        Graphics g = null;
        double DEFAULT_END_DISTANCE = 2083.0;
        try {
            result = ImageIO.read(new File("src/main/java/oregontrail/assets/mountains.png"));
            g = result.getGraphics();

            double percentage = distanceTraveled / (END_DISTANCE == null ? DEFAULT_END_DISTANCE : END_DISTANCE);
            int x = (int)Math.round(1800.0 - (percentage * 1800.0));

            //Overlay next landmark
            if (landMarks.size() > 0) {
                Location l = landMarks.peek();
                BufferedImage landmark = ImageIO.read(new URL(l.getImageURL()));
                int distanceBetweenLocationAndWagon = Math.max(l.getDistance() - distanceTraveled, 0);
                double lPercentage = 1 - (distanceBetweenLocationAndWagon / (double)endX);
                int temp = (int)Math.round(lPercentage * (1800 - distanceTraveled));
                g.drawImage(landmark, temp, 370, 200, 200, null);
                landmark.flush();
            }

            //Overlay wagon
            BufferedImage wagon = ImageIO.read(new File("src/main/java/oregontrail/assets/wagon.png"));
            g.drawImage(wagon, x, 370, 200, 200, null);
            wagon.flush();

            ImageIO.write(result, "png", new File("src/main/java/oregontrail/gameState.png"));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (result != null) {
                result.flush();
                result = null;
            }
            if (g != null) {
                g.dispose();
                g = null;
            }
        }
    }

    public void printTeamConditions() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Team Conditions:");
        eb.addField("Days Elapsed: ", String.valueOf(daysElapsed), true);
        eb.addField("Distance Traveled: ", String.valueOf(distanceTraveled), true);
        if (landMarks.size() > 0)
        eb.addField("Next Landmark (" + landMarks.peek().toString() + "): ", String.valueOf(landMarks.peek().getDistance() - distanceTraveled), true);
        eb.addField("Pace: ", wagon.getPace() + " hours / day", true);
        eb.addField("Rations: ", String.format("%s (%d food per person / day)", rations.name, rations.quantity), true);
        for (OregonTrailPlayer player : wagon.getParty()) {
            StringBuilder health = new StringBuilder();
            health.append(player.health);
            health.append(" - ").append(player.getHealthStatus().name);
            health.append(" - ").append(player.getSickness());
            if (player.job != null) {
                eb.addField("**" + player.name + "** (owner): ", health.toString(), false);
            } else {
                eb.addField("**" + player.name + "**: ", health.toString(), false);
            }
        }

        // Display Food, Bullets, and Cash
        eb.addField("**Food**: ", ""+wagon.getFood(), true);
        eb.addField("**Ammo**: ", ""+wagon.getAmmo(), true);
        eb.addField("**Cash**: ", "$"+wagon.getCash(), true);

        event.getChannel().sendMessage(eb.build()).queue();
    }

    public void update() {
        if (rest) {
            EmbedBuilder restImage = new EmbedBuilder();
            restImage.setImage(Location.getRestURL());
            event.getChannel().sendMessage(restImage.build()).queue();
        }
        else {
            buildProgressImage();
            event.getChannel().sendFile(new File("src/main/java/oregontrail/gameState.png")).queue();
        }
        printTeamConditions();
        event.getChannel().sendMessage(getOptionsString()).queue();
    }

    /**
     * Return true if we reached the next landmark
     * @return
     */
    public Location checkLandMarks() {
        Location location = landMarks.peek();
        if (landMarks.size() == 0) return null;
        if (location.toString().startsWith("Willamette") && END_DISTANCE == null) {
            END_DISTANCE = (double) location.getDistance();
        }
        if (distanceTraveled >= location.getDistance()) {
            distanceTraveled = location.getDistance(); //Once we reach landmark set distance traveled to it
            currentLocation = location;
            if (location.toString().contains("River")){
                EmbedBuilder locationEB = new EmbedBuilder();
                locationEB.setTitle("You made it to the " + location.toString() + " (day " + daysElapsed + ") - mile " + distanceTraveled);
                if (location.toString().contains("Big Blue River")) {
                    locationEB.setImage(currentLocation.getImageURL());
                }
                else if (location.toString().contains("Kansas River")) {
                    locationEB.setImage(currentLocation.getImageURL());
                }
                else if (location.toString().contains("Green River")) {
                    locationEB.setImage(currentLocation.getImageURL());
                }
                else if (location.toString().contains("Snake River")) {
                    locationEB.setImage(currentLocation.getImageURL());
                }
                locationEB.setFooter(location.getDescription());
                event.getChannel().sendMessage(locationEB.build()).queue();
                if (location.toString().contains("Snake River")) {
                    event.getChannel().sendMessage(((SnakeRiverCrossing)location).getSnakeRiverOptions()).queue();
                }
                else {
                    event.getChannel().sendMessage(location.getRiverOptions()).queue();
                }
            }
            else if (location.toString().contains("Fort")) {
                EmbedBuilder locationEB = new EmbedBuilder();
                locationEB.setTitle("You made it to the " + location.toString() + " (day " + daysElapsed + ") - mile " + distanceTraveled);
                if (location.toString().contains("Kearney")) {
                    // Initialize Kearney store
                    store = new KearneyGeneralStore(event);
                    // Fort Kearney building
                    locationEB.setImage(store.getBuildingURL());
                }
                else if (location.toString().contains("Laramie")) {
                    // Initialize Laramie store
                    store = new LaramieGeneralStore(event);
                    // Fort laramie building
                    locationEB.setImage(store.getBuildingURL());
                }
                else if (location.toString().contains("Bridger")) {
                    // Initialize Bridger store
                    store = new BridgerGeneralStore(event);
                    // Fort Bridger building
                    locationEB.setImage(store.getBuildingURL());
                }
                else if (location.toString().contains("Hall")) {
                    // Initialize Hall store
                    store = new HallGeneralStore(event);
                    // Fort Hall building
                    locationEB.setImage(store.getBuildingURL());
                }
                else if (location.toString().contains("Boise")) {
                    // Initialize Boise store
                    store = new BoiseGeneralStore(event);
                    // Fort Boise building
                    locationEB.setImage(store.getBuildingURL());
                }
                else if (location.toString().contains("Walla")) {
                    // Initialize Walla Walla store
                    store = new WallaWallaGeneralStore(event);
                    // Fort Walla Walla building
                    locationEB.setImage(store.getBuildingURL());
                }
                locationEB.setFooter(location.getDescription());
                event.getChannel().sendMessage(locationEB.build()).queue();
                event.getChannel().sendMessage(location.getFortOptions()).queue();
            }
            else if (location.toString().contains("South Pass")) {
                EmbedBuilder locationEB = new EmbedBuilder();
                locationEB.setTitle("Half way point! " + location.toString() + " (day " + daysElapsed + ") - mile " + distanceTraveled);
                locationEB.setImage(currentLocation.getImageURL());
                locationEB.setFooter(location.getDescription());
                event.getChannel().sendMessage(locationEB.build()).queue();
                event.getChannel().sendMessage(location.getSouthPassOptions()).queue();
            }
            else if (location.toString().contains("Grande Ronde")) {
                EmbedBuilder locationEB = new EmbedBuilder();
                locationEB.setTitle("You're almost to the end! " + location.toString() + " (day " + daysElapsed + ") - mile " + distanceTraveled);
                locationEB.setImage(currentLocation.getImageURL());
                locationEB.setFooter(location.getDescription());
                event.getChannel().sendMessage(locationEB.build()).queue();
                event.getChannel().sendMessage(((GrandeRonde)location).getGrandeRondeOptions()).queue();
            }
            else if (location.toString().contains("Dalles")) {
                EmbedBuilder locationEB = new EmbedBuilder();
                locationEB.setTitle(location.toString() + " (day " + daysElapsed + ") - " + distanceTraveled);
                locationEB.setImage(currentLocation.getImageURL());
                locationEB.setFooter(location.getDescription());
                event.getChannel().sendMessage(locationEB.build()).queue();
                event.getChannel().sendMessage(((Dalles)location).getDallesOptions()).queue();
            }
            Location tempL = landMarks.poll();
            startX = distanceTraveled;
            endX = landMarks.size() > 0 ? landMarks.peek().getDistance() : startX + 125;
            return tempL;
        }
        return null;
    }

    public OTGameStatus playLandMark(String optionNumber) {
        if (optionNumber == null || optionNumber.isEmpty()) {
            return INVALID_INPUT;
        }
        optionNumber = optionNumber.trim();
        String[] split = optionNumber.split(" ");
        rest = false;
        if (CorneliusUtils.isNumeric(split[0])) {
            OTGameStatus tempStatus;
            if (currentLocation.getClass().getSimpleName().contains("River")) {
                 tempStatus = riverCrossing(split[0]);
                 if (tempStatus != RUNNING) return tempStatus;
            }
            else if (currentLocation.getClass().getSimpleName().contains("Fort")) {
                tempStatus = fortStop(split[0]);
                if (tempStatus != RUNNING) return tempStatus;
            }
            else if (currentLocation.getClass().getSimpleName().contains("SouthPass")) {
                tempStatus = southPathStop(split[0]);
                if (tempStatus != RUNNING) return tempStatus;
            }
            else if (currentLocation.getClass().getSimpleName().contains("GrandeRonde")) {
                tempStatus = grandeRondeStop(split[0]);
                if (tempStatus != RUNNING) return tempStatus;
            }
            else if (currentLocation.getClass().getSimpleName().contains("Dalles")) {
                tempStatus = dallesStop(split[0]);
                if (tempStatus != RUNNING) return tempStatus;
            }
        }
        return gameOver();
    }

    /**
     * Logic for selected choice at a Dalles
     * @param selectedOption
     */
    private OTGameStatus dallesStop(String selectedOption) {
        switch (selectedOption) {
            case "1": //Take raft down the river (Shorter but more dangerous)
                if (splitPathsChoosen.get(0).getClass().getSimpleName().contains("GreenRiver")) { // Took Green River Crossing
                    if (splitPathsChoosen.get(1).getClass().getSimpleName().contains("Dalles")) { // Took Shortcut to Dalles
                        landMarks.add(new WillametteValley(1807));
                    }
                    else { // Went to Walla Walla
                        landMarks.add(new WillametteValley(1979));
                    }
                }
                else { // Went to Fort Bridger
                    if (splitPathsChoosen.get(1).getClass().getSimpleName().contains("Dalles")) { // Took Shortcut to Dalles
                        landMarks.add(new WillametteValley(1898));
                    }
                    else { // Went to Walla Walla
                        landMarks.add(new WillametteValley(2073));
                    }
                }
                wagon.raft(this);
                break;
            case "2": //Take the Barlow Toll Road (Safer but 10 miles longer)
                if (splitPathsChoosen.get(0).getClass().getSimpleName().contains("GreenRiver")) { // Took Green River Crossing
                    if (splitPathsChoosen.get(1).getClass().getSimpleName().contains("Dalles")) { // Took Shortcut to Dalles
                        landMarks.add(new WillametteValley(1817));
                    }
                    else { // Went to Walla Walla
                        landMarks.add(new WillametteValley(1989));
                    }
                }
                else { // Went to Fort Bridger
                    if (splitPathsChoosen.get(1).getClass().getSimpleName().contains("Dalles")) { // Took Shortcut to Dalles
                        landMarks.add(new WillametteValley(1908));
                    }
                    else { // Went to Walla Walla
                        landMarks.add(new WillametteValley(2083));
                    }
                }
                break;
        }
        return RUNNING;
    }

    /**
     * Logic for selected choice at a South Pass
     * @param selectedOption
     */
    private OTGameStatus southPathStop(String selectedOption) {
        switch (selectedOption) {
            case "1": //Go to Fort Bridger
                splitPathsChoosen.add(new FortBridger(-1));
                landMarks.add(new FortBridger(989));
                landMarks.add(new GreenRiverCrossing(1151));
                landMarks.add(new FortHall(1395));
                landMarks.add(new SnakeRiverCrossing(1534));
                landMarks.add(new FortBoise(1648));
                landMarks.add(new GrandeRonde(1798));
                break;
            case "2": //Take shortcut to Green River Crossing and skip Fort Bridger
                splitPathsChoosen.add(new GreenRiverCrossing(-1));
                landMarks.add(new GreenRiverCrossing(1057));
                landMarks.add(new FortHall(1258));
                landMarks.add(new SnakeRiverCrossing(1440));
                landMarks.add(new FortBoise(1554));
                landMarks.add(new GrandeRonde(1714));
                break;
        }
        return RUNNING;
    }

    /**
     * Logic for selected choice at Grande Ronde in the Blue Mountains
     * @param selectedOption
     */
    private OTGameStatus grandeRondeStop(String selectedOption) {
        switch (selectedOption) {
            case "1": //Go to Fort Walla Walla
                splitPathsChoosen.add(new FortWallaWalla(-1));
                if (splitPathsChoosen.get(0).getClass().getSimpleName().contains("GreenRiver")) { // Took Green River Crossing
                    landMarks.add(new FortWallaWalla(1769));
                    landMarks.add(new Dalles(1889, false));
                }
                else { // Went to Fort Bridger
                    landMarks.add(new FortWallaWalla(1863));
                    landMarks.add(new Dalles(1983, false));
                }
                break;
            case "2": //Take shortcut to Dalles and skip Fort Walla Walla
                splitPathsChoosen.add(new Dalles(-1, true));
                if (splitPathsChoosen.get(0).getClass().getSimpleName().contains("GreenRiver")) { // Took Green River Crossing
                    landMarks.add(new Dalles(1717, true));
                }
                else { // Went to Fort Bridger
                    landMarks.add(new Dalles(1808, true));
                }
                break;
        }
        return RUNNING;
    }

    /**
     * Logic for selected choice at a fort
     * @param selectedOption
     */
    private OTGameStatus fortStop(String selectedOption) {
        switch (selectedOption) {
            case "1": //Continue on trail
                return RUNNING;
            case "2": //Buy Supplies
                EmbedBuilder generalStoreEB = new EmbedBuilder();
                if (currentLocation.getClass().getSimpleName().contains("Kearney")) {
                    generalStoreEB.setColor(store.getColor());
                }
                else if (currentLocation.getClass().getSimpleName().contains("Laramie")) {
                    generalStoreEB.setColor(store.getColor());
                }
                else if (currentLocation.getClass().getSimpleName().contains("Bridger")) {
                    generalStoreEB.setColor(store.getColor());
                }
                else if (currentLocation.getClass().getSimpleName().contains("Hall")) {
                    generalStoreEB.setColor(store.getColor());
                }
                else if (currentLocation.getClass().getSimpleName().contains("Walla")) {
                    generalStoreEB.setColor(store.getColor());
                }
                generalStoreEB.setImage(store.getStoreURL());
                event.getChannel().sendMessage(generalStoreEB.build()).queue();
                event.getChannel().sendMessage( wagon.printInventory() + "\nWhich item and how many of that item would you like to buy ex: `1 2` or `7 1500`? Or type `leave` to leave store").queue();
                return STORE;
        }
        return RUNNING;
    }

    /**
     * Logic for selected choice at a river.
     * @param selectedOption
     */
    private OTGameStatus riverCrossing(String selectedOption) {
        River riverCrossing = (River) currentLocation;
        double success;
        EmbedBuilder riverEB;
        switch (selectedOption) {
            case "1": //Ford the river
                success = CorneliusUtils.randomDoubleBetween(1, 100, 2);
                riverEB = new EmbedBuilder();
                boolean fail = false;
                if (riverCrossing.getDepth() >= 3) { //Too deep
                    riverEB.setImage(Location.getWagonSunkURL());
                    riverEB.setTitle("River is too deep to cross!");
                    event.getChannel().sendMessage(riverEB.build()).queue();
                    fail = true;
                }
                else if (success >= riverCrossing.getChance()) {
                    event.getChannel().sendMessage("You successfully crossed the river!").queue();
                }
                else {
                    riverEB.setImage(Location.getWagonSunkURL());
                    riverEB.setTitle("You failed to cross the river!");
                    event.getChannel().sendMessage(riverEB.build()).queue();
                    fail = true;
                }
                if (fail) {
                    wagon.failedRiverCrossing();
                }
                break;
            case "2": //Caulk the wagon
                wagon.nextDay(this, false, true);
                success = CorneliusUtils.randomDoubleBetween(1, 100, 2);
                riverEB = new EmbedBuilder();
                success *= 1.5; //Increase success rate by 50%
                if (success >= riverCrossing.getChance()) {
                    event.getChannel().sendMessage("You spend a day caulking the wagon and successfully cross the river!").queue();
                }
                else {
                    riverEB.setImage(Location.getWagonSunkURL());
                    riverEB.setTitle("You failed to cross the river!");
                    event.getChannel().sendMessage(riverEB.build()).queue();
                    wagon.failedRiverCrossing();
                }
                break;
            case "3": //Take the ferry across or Hire Indian
                if (currentLocation.toString().contains("Snake River")) {
                    if (wagon.getClothes() < 3) {
                        event.getChannel().sendMessage("You do not have enough clothing to part with").queue();
                        return KEEP_STATE;
                    }
                    else {
                        for (int i = 0; i < 3; i++) {
                            wagon.nextDay(this, false, true);
                        }
                        wagon.setClothes(wagon.getClothes() - 3);
                        event.getChannel().sendMessage("A Shoshoni Indian guides you through the Snake River Crossing for a cost of 3 sets of clothing...").queue();
                    }
                }
                else {
                    for (int i = 0; i < 5; i++) {
                        rest();
                        wagon.nextDay(this, false, true);
                    }
                    wagon.setCash(wagon.getCash() - 5);
                    event.getChannel().sendMessage("You take the ferry across for a fee of $5. The trip takes 5 days...").queue();
                }
                break;
            case "4": //Wait a day
                rest();
                wagon.nextDay(this, false, true);
                EmbedBuilder locationEB = new EmbedBuilder();
                locationEB.setTitle( "Day " + daysElapsed);
                locationEB.setImage(Location.getRestURL());
                locationEB.setFooter(currentLocation.getDescription());
                event.getChannel().sendMessage(locationEB.build()).queue();
                event.getChannel().sendMessage(currentLocation.getRiverOptions()).queue();
                return KEEP_STATE;
            default:
                return INVALID_INPUT;
        }
        return RUNNING;
    }
}
