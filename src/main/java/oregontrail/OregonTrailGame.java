package oregontrail;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import oregontrail.enums.DiseaseEnum;
import oregontrail.enums.RationsEnum;
import oregontrail.occupation.*;
import oregontrail.store.GeneralStore;
import oregontrail.store.MattGeneralStore;
import utils.CorneliusUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;


import static commands.OregonTrailCommand.getOptionsString;
import static oregontrail.OTGameStatus.*;

public class OregonTrailGame {

    public final double END_DISTANCE = 2000.0; //Miles

    public OregonTrailPlayer owner;
    public Wagon wagon;
    public GeneralStore store;
    public int distanceTraveled = 0; //2000 miles total
    public int daysElapsed = 0;
    public RationsEnum rations = RationsEnum.FILLING;
    public int score = 0;

    private MessageReceivedEvent event;
    private static DecimalFormat formatPercent = new DecimalFormat("##0.##");

    public OregonTrailGame(MessageReceivedEvent event) {
        this.event = event;
        owner = new OregonTrailPlayer(event.getAuthor().getId(), event.getAuthor().getName());
        wagon = new Wagon(owner, event);
    }

    public OTGameStatus play(String optionNumber) {
        if (optionNumber == null || optionNumber.isEmpty()) {
            return INVALID_INPUT;
        }
        optionNumber = optionNumber.trim();
        String[] split = optionNumber.split(" ");
        if (CorneliusUtils.isNumeric(split[0])) {
            switch (split[0]) {
                case "1": //Continue traveling
                    if (split.length >= 2 && split[1] != null && CorneliusUtils.isNumeric(split[1])) {
                        int days = Integer.parseInt(split[1]);
                        for (int i = 0; i < days; i++) {
                            boolean event1 = generateRandomEvents();
                            boolean event2 = wagon.nextDay(this, true);
                            if (event1 || event2 || isGameOver()) break;
                        }
                    }
                    else {
                        generateRandomEvents();
                        wagon.nextDay(this, true);
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
                            boolean event2 = wagon.nextDay(this, false);
                            if (event1 || event2 || isGameOver()) break;
                        }
                    }
                    else {
                        rest();
                        generateRandomEvents();
                        wagon.nextDay(this, false);
                    }
                    break;
                case "4": //Hunt
                    hunt();
                    generateRandomEvents();
                    wagon.nextDay(this, false);
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

    private boolean generateRandomEvents() {
        int rand = CorneliusUtils.randomIntBetween(0, 100);
        if (rand < 4) { // Catch Sickness
            OregonTrailPlayer player = wagon.giveRandomSickness();
            event.getChannel().sendMessage(player.name + " has been diagnosed with " + player.getSickness()).queue();
            return true;
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
            death.setImage("https://lh3.googleusercontent.com/pw/ACtC-3dvu4OzHzM7vWpPOpvxhpN1apWQlGppVsiuSQ7bDLbRgd6rL2jbR0aAxwDXA6zEQyEyqa_LBa4pPXz8aR7KeJoZfKcWJlAheGbmY9bwhZWrxopvzufaOD3ksDFponrXhSNtZrSigzT7TiBTH80z7U8=w300-h207-no?authuser=1");
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
            return true;
        }
        else if (rand < 11) {
            // Theft
            String item = wagon.removeRandomItem();
            if (item != null) {
                event.getChannel().sendMessage("1 " + item + " has been stolen from you.").queue();
            }
            return true;
        }
        else if (rand < 12) {
            // Attacked
            event.getChannel().sendMessage("Raiders invaded your camp during the night!").queue();
            for (OregonTrailPlayer player : wagon.getParty()) {
                double wounded = CorneliusUtils.randomNumber01();
                if (wounded <= 0.33) {
                    int damage = CorneliusUtils.randomIntBetween(0, player.health);
                    wagon.decreaseHealth(player, damage);
                    event.getChannel().sendMessage(player.name + " suffered -" + damage + " damage").queue();
                }
            }
            return true;
        }
        else if (rand < 17) {
            // Came across a farmer
            int gainedFood = CorneliusUtils.randomIntBetween(0, 25) + 25;
            wagon.setFood(wagon.getFood() + gainedFood);
            event.getChannel().sendMessage("You came across a generous farmer and are gifted " + Integer.valueOf(gainedFood) + "lbs of food!").queue();
            return true;
        }
        else if (rand < 19) {
            // Came across abandoned wagon
            wagon.getSpareParts().add(new Axle());
            wagon.getSpareParts().add(new Tongue());
            wagon.getSpareParts().add(new Wheel());
            event.getChannel().sendMessage("You find an abandoned wagon and gather the parts from the wagon.\n You've gained:\n1 Wheel\n1 Axle\n1 Tongue.").queue();
            return true;
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
            return true;
        }
        else
            ;// Storm lose days

        return false;
    }


    /**
     * Return boolean if game is over
     * @return
     */
    private boolean isGameOver() {
        // All players dead - LOSE
        if (wagon.getParty().stream().allMatch(m -> m.health <= 0)) {
            return true;
        }

        // WIN
        if (distanceTraveled >= END_DISTANCE) {
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
     * Return OTGameStatus if game is over and send messages
     * @return
     */
    private OTGameStatus gameOver() {
        // All players dead - LOSE
        if (wagon.getParty().stream().allMatch(m -> m.health <= 0)) {
            buildProgressImage();
            event.getChannel().sendFile(new File("src/main/java/oregontrail/gameState.png")).queue();
            printTeamConditions();

            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Everyone died");
            eb.setImage("https://lh3.googleusercontent.com/pw/ACtC-3cUWgGW1FIIRRXE5l2X8YRK9hEIDi14YZPpEYgCsqa2QKWDGmL5R7S3qOMem3bVJ8JTadR8X2n7MD86V7igeFCZV1jOhCv0nb3ihUq5xQC9MxgnoxDXJhBMbPyDVHhj0h0vxbbkMzNSUsti3sUTAak=w1400-h744-no?authuser=1");
            eb.setFooter("Developed by Jay-Ar - https://github.com/Jay-ArBrouillard/Cornelius-Discord-Bot");
            event.getChannel().sendMessage(eb.build()).queue();
            return LOSE;
        }

        // WIN
        if (distanceTraveled >= END_DISTANCE) {
            //Formula for score
            EmbedBuilder eb = new EmbedBuilder();
            eb.setImage("https://lh3.googleusercontent.com/pw/ACtC-3cV-AT_mz5aa2SckZ4h4efAzepBYf8aPJTcgY2e5sXAAmUpco74KzW1jZiGFpDlERK914sZPVLyfAzDG3n2YTnOeNd7ExFs76hQg1vScdciZXqlDe24BtF7Bp7ppBGDpVfA5oqmbBzD-AcviVTeVcg=w800-h474-no?authuser=1");

            // 400 for each living member plus the value of their health
            int tempMemberScore = 0;
            for (OregonTrailPlayer member : wagon.getParty()) {
                if (member.health > 0) {
                    tempMemberScore += 400 + member.health;
                }
            }

            eb.addField(wagon.getParty().size() + " survived X 400 + (their health) =", String.valueOf(tempMemberScore), false);
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
            int tempSpareWagonParts = 0;
            // TODO 50 for working wagon
            for (Part part : wagon.getSpareParts()) {
                score += 2;
                tempSpareWagonPartsQuantity++;
                tempSpareWagonParts += 2;
            }

            eb.addField(tempSpareWagonParts + " spare wagon parts X 2", String.valueOf(tempSpareWagonPartsQuantity * 2), false);
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
                        event.getChannel().sendMessage(member.name + " healed from " + disease.name).queue();
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

    public void initalizeMattGeneralStore() {
        store = new MattGeneralStore(event);
    }

    public void buildProgressImage() {
        //Begin with mountains as background
        BufferedImage result = null;
        Graphics g = null;
        try {
            result = ImageIO.read(new File("src/main/java/oregontrail/assets/mountains.png"));
            g = result.getGraphics();

            double percentage = distanceTraveled / END_DISTANCE;
            int x = (int)Math.round(1800.0 - (percentage * 1800.0));
            //Overlay wagon
            BufferedImage piece = ImageIO.read(new File("src/main/java/oregontrail/assets/wagon.png"));
            g.drawImage(piece, x, 370, 200, 200, null);
            piece.flush();
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
        buildProgressImage();
        event.getChannel().sendFile(new File("src/main/java/oregontrail/gameState.png")).queue();
        printTeamConditions();
        event.getChannel().sendMessage(getOptionsString()).queue();
    }
}
