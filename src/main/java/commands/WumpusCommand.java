package commands;

import Wumpus.Game;
import Wumpus.Board;
import Wumpus.Human;
import Wumpus.Wumpus;
import Wumpus.State;
import Wumpus.WumpusUtils;
import Wumpus.Map;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.*;

public class WumpusCommand {

    private static Game game;
    private static State gameState = State.SETUP;
    private static String NEW_GAME = "!wumpus";

    public static boolean isRunning() {
        return game != null || gameState == State.RULES_INIT ||
                gameState == State.START_UP || gameState == State.CHOOSE_ACTION;
    }

    public static void execute(MessageReceivedEvent event, String message) {
        if (message.equals("q") || message.equals("quit")) {
            gameState = State.SETUP;
            game = null;
            event.getChannel().sendMessage("Quitting `Hunt the Wumpus`...\n\n").queue();
            return;
        }

        //Reset / Initialize Game
        if (message.equals(NEW_GAME)) {
            gameState = State.RULES_INIT;
            StringBuilder rules = new StringBuilder("Rules:\n");
            rules.append("1. There are 3 hazards:\n");
            rules.append("\t\ta. A bottomless pit (you will feel a breeze nearby)\n");
            rules.append("\t\tb. A colony of bats that will pick you up and drop you in a random space--including potentially deadly spaces (you will hear flapping nearby)\n");
            rules.append("\t\tc. A fearsome, hungry, and unbathed wumpus (you will smell it nearby\n");
            rules.append("2. The wumpus is heavy; bats cannot lift him.").append("\n");
            rules.append("3. The wumpus is covered in suckers; he won't fall down the bottomless pit.\n");
            rules.append("4. Firing an arrow that misses the wumpus may cause it to move.\n");
            rules.append("5. You have 5 wumpus-piercing arrows.\n");
            event.getChannel().sendMessage(rules.toString() + "\nStarting new `Hunt the Wumpus` game...\n\n" + "Provide the map length and width ex: `5 5`").queue();
            return;
        }
        //Only proceed with the game if a game has been started
        if (game == null && gameState == State.SETUP) {
            return;
        }

        //State machine game mechanics
        String reply = "";
        switch (gameState) {
            case RULES_INIT:
                reply = initialize(message);
                if (reply.equals("success")) {
                    gameState = State.START_UP;
                }
            case START_UP:
                if (game == null) return;
                reply = game.getStatus();
                gameState = State.CHOOSE_ACTION;
                break;
            case CHOOSE_ACTION:
                if (game == null) return;
                //Check for move or shoot inputs
                reply = game.human.moveOrShoot(message);
                if (reply.equals("success")) {
                    String [] inputs = message.split(" ");
                    String validateMsg = game.human.validate(inputs[1]);
                    //Handle move command
                    if (inputs[0].equalsIgnoreCase("m")) {
                        if (validateMsg.equals("success")) {
                            game.human.move(inputs[1], game.board);
                            reply = game.getStatus();
                            if (reply.contains("Game Over")) {
                                game = null;
                                gameState = State.SETUP;
                            }
                        }
                        else {
                            reply = validateMsg;
                        }
                    }
                    else { //Handle shoot command
                        if (validateMsg.equals("success")) {
                            game.human.shoot(inputs[1], game.board, event);
                            reply = game.getStatus();
                            if (reply.contains("Game Over")) {
                                game = null;
                                gameState = State.SETUP;
                            }
                        }
                        else {
                            reply = validateMsg;
                        }
                    }
                }
                break;
        }

        if (reply.contains("Game Over")) {
            gameState = State.SETUP;
            game = null;
        }
        event.getChannel().sendMessage(reply).queue();
    }

    public static String initialize(String inputs) {
        String [] input = inputs.split(" ");
        if (input.length != 2) {
            return "`" + inputs + "` is invalid please supply a length and width ex: `5 5`";
        }
        int length = 0;
        int width = 0;
        try {
            length = Integer.parseInt(input[0]);
            width = Integer.parseInt(input[1]);
            if (length <= 1 || width <= 1) {
                return "`" + inputs + "` length and width must be greater than 1 ex: `5 5`";
            }
            if (length > 100 || width > 100) {
                return "`" + inputs + "` length and width must be less than ex: `5 5`";
            }
        } catch (Exception e) {
            return "`" + inputs + "` is invalid please supply a length and width ex: `5 5`";
        }

        //Randomize player, wumpus, pits, bats
        int playerStartingRoom = WumpusUtils.getRandomNumber(0, length*width-1);
        int wumpusStartingRoom = playerStartingRoom;
        while (wumpusStartingRoom == playerStartingRoom) {
            wumpusStartingRoom = WumpusUtils.getRandomNumber(0, length*width-1);
        }

        int numPits = WumpusUtils.getRandomNumber(1, (long) ((length*width-1)*.10));
        int numBats = WumpusUtils.getRandomNumber(1, (long) ((length*width-1)*.30));
        int [][] pits = new int[numPits][2];
        int [][] bats = new int[numBats][2];
        int pitsCounter = 0;
        int batsCounter = 0;

        ArrayList<ArrayList<Integer>> availableCells = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < width; j++) {
                availableCells.add(new ArrayList<>(Arrays.asList(i,j)));
            }
        }
        Collections.shuffle(availableCells);
        for (int i = 0; i < availableCells.size(); i++) {
            int x = availableCells.get(i).get(0);
            int y = availableCells.get(i).get(1);
            if ((x != playerStartingRoom / length || y != playerStartingRoom % length) && pitsCounter < numPits) {
                pits[pitsCounter][0] = x;
                pits[pitsCounter][1] = y;
                pitsCounter++;
            } else if (batsCounter < numBats) {
                bats[batsCounter][0] = x;
                bats[batsCounter][1] = y;
                batsCounter++;
            }

            if (pitsCounter >= numPits && batsCounter >= numBats) break;
        }

        Human player = new Human(new int[]{playerStartingRoom / length,playerStartingRoom % length}, playerStartingRoom);
        Wumpus wumpus = new Wumpus(new int[]{wumpusStartingRoom / length,wumpusStartingRoom % length}, wumpusStartingRoom);

        game = new Game(
                new Board(length,width, pits, bats, player, wumpus),
                player,
                wumpus
        );

        new WumpusUtils(game); //Make sure to set the utils
        game.setMap(new Map(game.board.getGameBoard(), Integer.toString(game.board.getGameBoard()[length-1][width-1].getRoomNumber()).length()));    //Make sure to add map to game
        return "success";
    }

    public static class Help {
        static String name = "!wumpus";
        static String description = "starts a game of hunt the wumpus";
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
