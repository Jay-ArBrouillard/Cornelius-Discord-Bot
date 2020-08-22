package commands;

import Chess.*;
import Wumpus.State;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ChessCommand {

    private static Game game = new Game();
    private static GameStatus gameState = GameStatus.INACTIVE;
    private static final String gameBoardImageLoc = "src/main/java/Chess/gameState.png";

    public static boolean isRunning() {
        return game != null && gameState != GameStatus.INACTIVE;
    }

    public static void execute(MessageReceivedEvent event, String message) {
        if (message.equals("q") || message.equals("quit")) {
            event.getChannel().sendMessage("Quitting `Chess`...\n\n").queue();
            gameState = GameStatus.INACTIVE;
            return;
        }

        //If game is INACTIVE then a new game should be initalized
        if (gameState.equals(GameStatus.INACTIVE)) {
            try {
                game.initialize(new HumanPlayer(true), new ComputerPlayer(false));
                gameState = GameStatus.START_UP;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //State machine game mechanics
        String reply = "";
        File file = null;
        switch (gameState) {
            case START_UP:
                reply = "`New game started`\nChoose a piece and spot to move (ex: `c2 c4`)";
                file = new File(gameBoardImageLoc);
                gameState = GameStatus.PROCESS_MOVE;
                break;
            case CHOOSE_MOVE:
                reply = "Choose a piece and spot to move (ex: `c2 c4`)";
                gameState = GameStatus.PROCESS_MOVE;
                break;
            case PROCESS_MOVE:
                reply = game.processMoveInput(message);
                if (reply.contains("Success!")) {
                    file = new File(gameBoardImageLoc);
                    reply = reply.replace("Success!", ""); //Remove success from the reply message
                }
                break;
            default:
                System.out.println("Error got into default case");
        }

        event.getChannel().sendMessage(reply).queue();
        if (file != null) {
            event.getChannel().sendFile(file).queue();
        }
    }

    public static class Help {
        static String name = "!chess";
        static String description = "starts a game of chess";
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
