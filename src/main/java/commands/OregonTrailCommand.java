package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import oregontrail.*;
import oregontrail.location.Location;
import oregontrail.store.MattGeneralStore;
import utils.CorneliusUtils;

import java.awt.*;
import java.util.Arrays;

import static oregontrail.OTGameState.*;
import static oregontrail.OTGameStatus.*;

public class OregonTrailCommand {
    private static OTGameState otGameState = MENU;
    private static OTGameStatus otGameStatus = RUNNING;
    private static OregonTrailGame oregonTrailGame;

    public static void execute(MessageReceivedEvent event, String message) {
        if (Arrays.asList(CorneliusUtils.QUIT).stream().anyMatch(x -> x.equals(message))) {
            endGame(event);
            return;
        }

        boolean success;
        String menuOptions;
        switch (otGameState) {
            case MENU:
                oregonTrailGame = new OregonTrailGame(event);
                EmbedBuilder titleEB = new EmbedBuilder();
                titleEB.setColor(Color.CYAN);
                titleEB.setDescription("Oregon Trail v3.0 alpha");
                titleEB.setImage("https://lh3.googleusercontent.com/pw/ACtC-3dDFtFhShqsAbNM_yksXXIAjWv0wvkA4MLh0gU_8CGQVsB838MNFvIloYPKP8a0Pchu9h7jp7fgjRF3VD_3-MFjdbkl8ZG2UJhqIBGJCVLBAlyz82XX_Xlb5PLO0MzxaPMPev5Y0sGZggKMsjOziZ4=w1737-h937-no?authuser=1");
                titleEB.setFooter("Objective: Get to Willamette Valley, Oregon to win.\n" +
                        "If all of your party dies, then you lose.\n" +
                        "If your wagon is unable to move due to a broken part and you do not have the spare part, then you will also lose.\n" +
                        "If all your oxen die, then you will also lose.\n" +
                        "Developed by Jay-Ar - https://github.com/Jay-ArBrouillard/Cornelius-Discord-Bot");
                event.getChannel().sendMessage(titleEB.build()).queue();

                menuOptions = "You may:\n" +
                        "\t1. Start a new game\n" +
                        "\t2. Resume an existing game (Not Working)\n" +
                        "\t3. See the rules (Not working)\n" +
                        "\t4. Exit";
                event.getChannel().sendMessage(menuOptions).queue();
                otGameState = START_GAME;
                break;
            case MAIN:
                otGameStatus = oregonTrailGame.play(message);
                if (RUNNING.equals(otGameStatus)) {
                    Location landMark = oregonTrailGame.checkLandMarks();
                    if (landMark != null) {
                        otGameState = LANDMARK;
                    } else {
                        oregonTrailGame.update();
                    }
                }
                else if (RULES.equals(otGameStatus)) {
                    //TODO
                }
                else if (QUIT.equals(otGameStatus)) {
                    endGame(event);
                } else {
                    event.getChannel().sendMessage("Invalid input: `" + message + "` - Please choose a valid option **(1-5)**").queue();
                    event.getChannel().sendMessage(getOptionsString()).queue();
                }
                break;
            case LANDMARK:
                otGameStatus = oregonTrailGame.playLandMark(message);
                if (RUNNING.equals(otGameStatus)) {
                    // Check game over
                    if (oregonTrailGame.isGameOver()) {
                        if (oregonTrailGame.isWin()) {
                            event.getChannel().sendMessage("Nice. You won!").queue();
                        } else {
                            event.getChannel().sendMessage("Rip. You lost!").queue();
                        }
                        endGame(event);
                    }
                    else {
                        oregonTrailGame.update();
                        otGameState = MAIN;
                    }
                } else if (STORE.equals(otGameStatus)) {
                    otGameState = GENERAL_STORE;
                }
                else if (KEEP_STATE.equals(otGameStatus));
                else if (WIN.equals(otGameStatus)) {
                    event.getChannel().sendMessage("Nice. You won!").queue();
                    endGame(event);
                } else if (LOSE.equals(otGameStatus)) {
                    event.getChannel().sendMessage("Rip. You lost!").queue();
                    endGame(event);
                }
                else {
                    event.getChannel().sendMessage("Invalid input: `" + message + "` - Please choose a valid option **(1-5)**").queue();
                    event.getChannel().sendMessage(getOptionsString()).queue();
                }
                break;
            case START_GAME:
                otGameStatus = oregonTrailGame.processMenuOption(message);
                if (START.equals(otGameStatus)) {
                    event.getChannel().sendMessage(getOccupationString()).queue();
                    otGameState = SELECT_OCCUPATION;
                } else if (RUNNING.equals(otGameStatus)) {
                    //TODO
                } else if (RULES.equals(otGameStatus)) {
                    //TODO
                } else if (QUIT.equals(otGameStatus)) {
                    endGame(event);
                } else {
                    event.getChannel().sendMessage("Invalid input: `" + message + "` - Please choose a valid option **(1-4)**").queue();
                    menuOptions = "You may:\n" +
                            "\t1. Start a new game\n" +
                            "\t2. Resume an existing game (Not Working)\n" +
                            "\t3. See the rules (Not working)\n" +
                            "\t4. Exit";
                    event.getChannel().sendMessage(menuOptions).queue();
                }
                break;
            case SELECT_OCCUPATION:
                success = oregonTrailGame.setOccupation(message);
                if (success) {
                    otGameState = GENERAL_STORE;
                    oregonTrailGame.store = new MattGeneralStore(event);
                    EmbedBuilder generalStoreEB = new EmbedBuilder();
                    generalStoreEB.setColor(oregonTrailGame.store.getColor());
                    generalStoreEB.setImage(oregonTrailGame.store.getStoreURL());
                    event.getChannel().sendMessage(generalStoreEB.build()).queue();
                    event.getChannel().sendMessage( oregonTrailGame.wagon.printInventory() + "\nWhich item and how many of that item would you like to buy ex: `1 2` or `7 2000`? Or type `leave` to leave store").queue();
                }
                break;
            case GENERAL_STORE:
                success = oregonTrailGame.store.canBuy(message, oregonTrailGame.wagon.getCash());
                if (success) {
                    oregonTrailGame.store.buy(message, oregonTrailGame.wagon);
                    event.getChannel().sendMessage( oregonTrailGame.wagon.printInventory() + "\nWhich item and how many of that item would you like to buy ex: `1 2` or `7 2000`? Or type `leave` to leave store").queue();
                }
                else if (!message.startsWith("leave")) {
                    event.getChannel().sendMessage("`" + message + "` is not a valid store option. Please enter **(1-7)** to buy an item or **leave** to leave store").queue();
                    EmbedBuilder generalStoreEB = new EmbedBuilder();
                    generalStoreEB.setColor(oregonTrailGame.store.getColor());
                    generalStoreEB.setImage(oregonTrailGame.store.getStoreURL());
                    event.getChannel().sendMessage(generalStoreEB.build()).queue();
                }
                if (message.startsWith("leave")) {
                    if (oregonTrailGame.wagon.getOxen() == 0) {
                        event.getChannel().sendMessage("You need atleast 1 oxen before beginning your travel...").queue();
                        EmbedBuilder generalStoreEB = new EmbedBuilder();
                        generalStoreEB.setColor(Color.RED);
                        generalStoreEB.setImage(oregonTrailGame.store.getStoreURL());
                        event.getChannel().sendMessage(generalStoreEB.build()).queue();
                    }
                    else {
                        otGameState = MAIN;
                        oregonTrailGame.update();
                    }
                }
                break;
            case RESUME_GAME:
                //TODO need to set values for game
                otGameState = MAIN;
                break;
            case SEE_RULES:
                //TODO
                break;
            default:
                event.getChannel().sendMessage("Logic error").queue();
        }


    }

    public static boolean gameInProgress() {
        return !MENU.equals(otGameState.toString()) && oregonTrailGame != null;
    }

    public static String getOccupationString() {
        StringBuilder occupationBuilder = new StringBuilder("Choose your occupation **(1-8)**. Each occupation has a starting cash and may have an extra advantage and final bonus multiplier:\n");
        occupationBuilder.append("1. Banker **$1600**\n");
        occupationBuilder.append("2. Doctor **$1,200** and sick/injured are less likely to die\n");
        occupationBuilder.append("3. Merchant **$1,200** and x1.5 final bonus\n");
        occupationBuilder.append("4. Blacksmith **$800** and more likely to repair broken wagon parts and x2 final bonus\n");
        occupationBuilder.append("5. Carpenter **$800** and more likely to repair broken wagon parts and x2 final bonus\n");
        occupationBuilder.append("6. Saddlemaker **$800** and x2.5 final bonus\n");
        occupationBuilder.append("7. Farmer **$400** and oxen are less likely to get sick and die and x3 final bonus\n");
        occupationBuilder.append("8. Teacher **$400** and x3.5 final bonus\n");
        occupationBuilder.append("Final Bonus = amount that your final point total will be multiplied by if you make it to the end");
        return occupationBuilder.toString();
    }

    public static String getOptionsString() {
        StringBuilder optionsBuilder = new StringBuilder();
        optionsBuilder.append("1. Travel (Optionally provide number of days to travel (automatically stops at significant events) usage: `1` or `1 5`)\n");
        optionsBuilder.append("2. Show Inventory\n");
        optionsBuilder.append("3. Rest (Optionally provide number of days to rest usage: `3 10`)\n");
        optionsBuilder.append("4. Hunt\n");
        optionsBuilder.append("5. Change Pace (Provide value between `1-24` usage: `5 12.5`)\n");
        optionsBuilder.append("6. Change Rations (Valid options: `Filling`, `Meager`, or `Starving` usage: `6 Meager`)\n");
        optionsBuilder.append("7. Kill Member (Provide name of member usage: `7 Wild Bill`)\n");
        optionsBuilder.append("8. Exit Game");
        return optionsBuilder.toString();
    }

    public static void endGame(MessageReceivedEvent event) {
        otGameState = MENU;
        oregonTrailGame = null;
        System.gc();
        event.getChannel().sendMessage("Quitting Oregon Trail...").queue();
    }

    public static class Help {
        static String name = "!oregontrail";
        static String description = "start a game of oregon trail";
        static String arguments = "";
        static boolean guildOnly = false;

        public static String getName() {
            return name;
        }

        public static String getDescription() {
            return description;
        }

        public static String getArguments() {
            return arguments;
        }

        public boolean isGuildOnly() {
            return guildOnly;
        }
    }
}
