package commands;

import Utils.GoogleSheets;
import chess.*;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ChessCommand {

    private static ChessGame chessGame;
    private static GameMode gameMode;
    private static GameStatus gameState = GameStatus.INACTIVE;
    private static final String gameBoardImageLoc = "src/main/java/chess/gameState.png";
    private static List<String> currentMessageIds = new ArrayList<>();
    private static List<String> oldMessageIds = new ArrayList<>();
    private static GoogleSheets db;
    private static String whitePlayerName;
    private static String blackPlayerName;
    private static String whitePlayerId;
    private static String blackPlayerId;


    public static boolean isRunning() {
        return chessGame != null && gameState != GameStatus.INACTIVE;
    }

    public static void execute(MessageReceivedEvent event, String message) {
        if (message.equalsIgnoreCase("q") || message.equalsIgnoreCase("quit")) {
            if (gameMode.isPlayerVsComputer()) {
                db.updateUser(event.getAuthor().getId(), false, false);
            }
            else {
                if (whitePlayerId != null && blackPlayerId != null && gameState != GameStatus.CHALLENGE_OPPONENT) {
                    db.updateUser(event.getAuthor().getId(), false, false);
                }
            }
            endGame(event.getChannel());
            return;
        }

        if (message.equalsIgnoreCase("board")) {
            event.getChannel().sendFile(new File(gameBoardImageLoc)).queue();
            return;
        }

        //If game is INACTIVE then a new game should be initalized
        if (gameState.equals(GameStatus.INACTIVE)) {
            chessGame = new ChessGame(event.getChannel());
            chessGame.board.buildImage();
            gameState = GameStatus.SETUP;
            db = new GoogleSheets();
        }

        //State machine game mechanics
        String reply = "";
        String belowMessage = null;
        File boardImageFile = null;
        switch (gameState) {
            case PLAYER_MOVE:
                reply = chessGame.processMove(message);
                if (reply.contains("Game Over") || reply.contains("DRAW")) {
                    boardImageFile = new File(gameBoardImageLoc);
                    belowMessage = "GG";
                    if (reply.contains("DRAW")) {
                        db.updateUser(blackPlayerId, false, true);
                        db.updateUser(whitePlayerId, false, true);
                    }
                    if (chessGame.isWhitePlayerTurn()) {  //That means Black checkmated White
                        db.updateUser(blackPlayerId, true, false);
                        db.updateUser(whitePlayerId, false, false);
                    } else { // White checkmated Black
                        db.updateUser(blackPlayerId, false, false);
                        db.updateUser(whitePlayerId, true, false);
                    }
                    endGame(event.getChannel());
                } else if (reply.contains("is in check!") || reply.contains("Success!")) {
                    boardImageFile = new File(gameBoardImageLoc);
                    reply = reply.replace("Success!", ""); //Remove success from the reply message
                    if (chessGame.isWhitePlayerTurn()) {
                        belowMessage = "`" + whitePlayerName + "` turn. Make a move (ex: `c2c4` or `help` or `helpb2` to see possible moves or `q` to forfeit the game)";
                    }
                    else {
                        belowMessage = "`" + blackPlayerName + "` turn. Make a move (ex: `c2c4` or `help` or `helpb2` to see possible moves or `q` to forfeit the game)";
                    }
                    if (gameMode.isPlayerVsComputer()) { //Only change turn if move went through
                        gameState = GameStatus.COMPUTER_MOVE;
                    }
                }
                break;
            case COMPUTER_MOVE:
                reply = chessGame.ai();
                if (reply.contains("Game Over") || reply.contains("DRAW")) {
                    boardImageFile = new File(gameBoardImageLoc);
                    belowMessage = "GG";
                    if (reply.contains("DRAW")) {
                        db.updateUser(blackPlayerId, false, true);
                        db.updateUser(whitePlayerId, false, true);
                    }
                    if (chessGame.isWhitePlayerTurn()) {  //That means Black checkmated White
                        db.updateUser(blackPlayerId, true, false);
                        db.updateUser(whitePlayerId, false, false);
                    } else { // White checkmated Black
                        db.updateUser(blackPlayerId, false, false);
                        db.updateUser(whitePlayerId, true, false);
                    }
                    endGame(event.getChannel());
                } else if (reply.contains("is in check!") || reply.contains("Success!")) {
                    boardImageFile = new File(gameBoardImageLoc);
                    reply = reply.replace("Success!", ""); //Remove success from the reply message
                    if (chessGame.isWhitePlayerTurn()) {
                        belowMessage = "`" + whitePlayerName + "` turn. Make a move (ex: `c2c4` or `help` or `helpb2` to see possible moves or `q` to forfeit the game)";
                    }
                    else {
                        belowMessage = "`" + blackPlayerName + "` turn. Make a move (ex: `c2c4` or `help` or `helpb2` to see possible moves or `q` to forfeit the game)";
                    }
                    if (gameMode.isPlayerVsComputer()) { //Only change turn if move went through
                        gameState = GameStatus.PLAYER_MOVE;
                    }
                }
                break;
            case SETUP:
                reply = "New `Chess` game started. Please choose from player options `(1-4)`:\n1. Player (WHITE) vs. Player (BLACK)\n2. Player vs. Computer\n3. Computer vs. Player";
                gameState = GameStatus.SETUP_RESPONSE;
                break;
            case SETUP_RESPONSE:
                reply = chessGame.setupPlayers(message);
                if (reply.equals(GameMode.PVP.toString())) {
                    reply = "`Player vs Player Chess Game`\nPlease challenge another player by entering their `userId` (Click on user and Copy ID)";
                    gameState = GameStatus.CHALLENGE_OPPONENT;
                    gameMode = GameMode.PVP;
                }
                else if (reply.equals(GameMode.PVC.toString())) {
                    reply = "`Starting Player vs. Computer Game`\nMake a move (ex: `c2 c4`)";
                    boardImageFile = new File(gameBoardImageLoc);
                    whitePlayerName = event.getAuthor().getName();
                    blackPlayerName = "Cornelius";
                    gameState = GameStatus.PLAYER_MOVE;
                    gameMode = GameMode.PVC;
                }
                else if (reply.equals(GameMode.CVP.toString())) {
                    reply = "`Starting Computer vs. Player Game`\nCornelius will go first...";
                    boardImageFile = new File(gameBoardImageLoc);
                    whitePlayerName = "Cornelius";
                    blackPlayerName = event.getAuthor().getName();
                    gameState = GameStatus.COMPUTER_MOVE;
                    gameMode = GameMode.CVP;
                }
                else {
                    reply = "Please choose from player options `(1-4)`:";
                }
                break;
            case CHALLENGE_OPPONENT:
                Guild guild = event.getGuild();
                Member member = guild.getMemberById(message.trim());
                if (member == null || !guild.getMembers().contains(member)) {
                    reply = "Opponent does not exist or is not in your discord server. Please reenter userId.";
                }
                else { //Valid opponent found
                    db.addUser(event.getAuthor().getId(), event.getAuthor().getName());
                    db.addUser(message.trim(), member.getEffectiveName());
                    whitePlayerName = event.getAuthor().getName();
                    blackPlayerName = member.getEffectiveName();
                    whitePlayerId = event.getAuthor().getId();
                    blackPlayerId = member.getId();
                    reply = "`" + event.getAuthor().getName() + "` challenges `" + member.getEffectiveName() + "` to a chess game. Challengee must reply 'y' to this text chat to accept!";
                    gameState = GameStatus.OPPONENT_ACCEPT_DECLINE;
                }
                break;
            case OPPONENT_ACCEPT_DECLINE:
                if (message.equalsIgnoreCase("y")) {
                    reply = "`" + whitePlayerName + "` vs `" + blackPlayerName + "`";
                    boardImageFile = new File(gameBoardImageLoc);
                    belowMessage = "`"+ event.getAuthor().getName() + "` goes first. Make a move (ex: `c2 c4`)";
                    gameState = GameStatus.PLAYER_MOVE;
                }
                else {
                    reply = blackPlayerName + " has declined the chess match";
                    endGame(event.getChannel());
                }


                break;
            default:
                reply = "Game logic error. Ask Jay-Ar what happened";
        }

        sendMessages(event, reply, boardImageFile, belowMessage);
        if (gameState.equals(GameStatus.COMPUTER_MOVE)) {
            new Thread(() -> {
                try {
                    computerAction(event);
                } catch (Exception e) {
                    //Do nothing
                }
            }).start();
        }
    }

    public static void sendMessages(MessageReceivedEvent event, String reply, File file, String belowMessage) {
        if (!oldMessageIds.isEmpty()) {
            event.getChannel().purgeMessagesById(oldMessageIds);
            oldMessageIds.clear();
        }
        event.getChannel().sendMessage(reply).queue((msg) -> currentMessageIds.add(msg.getId()));
        if (file != null) {
            event.getChannel().sendFile(file).queue((msg) -> currentMessageIds.add(msg.getId()));
        }
        if (belowMessage != null) {
            event.getChannel().sendMessage(belowMessage).queue((msg) -> currentMessageIds.add(msg.getId()));
        }

        oldMessageIds.addAll(currentMessageIds);
        currentMessageIds.clear();
    }

    public static void computerAction(MessageReceivedEvent event) {
        String reply = "";
        String belowMessage = null;
        File file = null;

        if (gameMode.isPlayerVsComputer()) {
            event.getChannel().sendMessage("`Cornelius` is THINKING...").queue();
        }
        reply = chessGame.ai();
        if (reply.contains("Game Over!") || reply.contains("DRAW")) {
            file = new File(gameBoardImageLoc);
            belowMessage = "GG";
            endGame(event.getChannel());
        } else if (reply.contains("is in check!") || reply.contains("Success!")) {
            file = new File(gameBoardImageLoc);
            reply = reply.replace("Success!", ""); //Remove success from the reply message
            reply = "`Cornelius` SELECTS " + reply;
            belowMessage = "Make a move (ex: `c2c4` or `help` or `helpb2` to see possible moves or `q` to forfeit the game)";
            if (gameMode != null && gameMode.isPlayerVsComputer()) {
                gameState = GameStatus.PLAYER_MOVE;
            }
        }

        sendMessages(event, reply, file, belowMessage);
    }

    public static void endGame(MessageChannel messageChannel) {
        messageChannel.sendMessage("Quitting `Chess`...\n\n").queue();
        chessGame = null;
        gameState = GameStatus.INACTIVE;
        gameMode = null;
        currentMessageIds.clear();
        oldMessageIds.clear();
        whitePlayerName = null;
        blackPlayerName = null;
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
