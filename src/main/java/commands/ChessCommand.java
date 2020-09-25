package commands;

import chess.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;

public class ChessCommand {

    private static ChessGame chessGame;
    private static GameStatus gameState = GameStatus.INACTIVE;
    private static final String gameBoardImageLoc = "src/main/java/chess/gameState.png";

    public static boolean isRunning() {
        return chessGame != null && gameState != GameStatus.INACTIVE;
    }

    public static void execute(MessageReceivedEvent event, String message) {
        if (message.equals("q") || message.equals("quit")) {
            event.getChannel().sendMessage("Quitting `Chess`...\n\n").queue();
            chessGame = null;
            gameState = GameStatus.INACTIVE;
            return;
        }

        //If game is INACTIVE then a new game should be initalized
        if (gameState.equals(GameStatus.INACTIVE)) {
            chessGame = new ChessGame();
            chessGame.board.buildImage();
            gameState = GameStatus.START_UP;
        }

        //State machine game mechanics
        String reply = "";
        String belowMessage = null;
        File file = null;
        switch (gameState) {
            case START_UP:
                reply = "`New game started`\nMake a move (ex: `c2 c4`)";
                file = new File(gameBoardImageLoc);
                gameState = GameStatus.PROCESS_MOVE;
                break;
            case PROCESS_MOVE:
                reply = chessGame.processMove(message);
                if (reply.contains("Game Over!")) {
                    file = new File(gameBoardImageLoc);
                    belowMessage = "GG";
                    chessGame = null;
                    gameState = GameStatus.INACTIVE;
                } else if (reply.contains("is in check!") || reply.contains("Success!")) {
                    file = new File(gameBoardImageLoc);
                    reply = reply.replace("Success!", ""); //Remove success from the reply message
                    belowMessage = "Make a move (ex: `c2c4` or `help` or `helpb2` to see possible moves or `q` to quit the game)";
                }
                break;
            default:
                reply = "Game logic error. Ask Jay-Ar what happened";
        }

        event.getChannel().sendMessage(reply).queue();
        if (file != null) {
            event.getChannel().sendFile(file).queue();
        }
        if (belowMessage != null) {
            event.getChannel().sendMessage(belowMessage).queue();
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
