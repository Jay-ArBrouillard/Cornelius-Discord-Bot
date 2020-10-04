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
    private static int whitePlayerElo;
    private static int blackPlayerElo;
    private static String reply;
    private static String belowMessage;
    private static File boardImageFile;
    private static Long matchStartTime;

    public static boolean isRunning() {
        return chessGame != null && gameState != GameStatus.INACTIVE;
    }

    public static boolean isMessageFromPlayer(String id) {
        return id.equals(whitePlayerId) || id.equals(blackPlayerId);
    }

    public static void execute(MessageReceivedEvent event, String message) {
        if (message.equalsIgnoreCase("q") || message.equalsIgnoreCase("quit")) {
            if (isMessageFromPlayer(event.getAuthor().getId())) {
                if (gameMode != null && gameMode.isPlayerVsComputer() && (gameState == GameStatus.PLAYER_MOVE || gameState == GameStatus.COMPUTER_MOVE)) { //Player vs Computer game
                    if (event.getAuthor().getId().equals(blackPlayerId)) { // Black quit
                        db.updateUser(blackPlayerId, false, false, blackPlayerElo, whitePlayerElo);
                        db.updateUser(whitePlayerId, true, false, whitePlayerElo, blackPlayerElo);
                        db.addCompletedMatch(whitePlayerName, blackPlayerName, whitePlayerId, blackPlayerId, whitePlayerName, blackPlayerName, false, matchStartTime);
                    }
                    else if (event.getAuthor().getId().equals(whitePlayerId)) { // White quit
                        db.updateUser(blackPlayerId, true, false, blackPlayerElo, whitePlayerElo);
                        db.updateUser(whitePlayerId, false, false, whitePlayerElo, blackPlayerElo);
                        db.addCompletedMatch(whitePlayerName, blackPlayerName, whitePlayerId, blackPlayerId, blackPlayerName, whitePlayerName, false, matchStartTime);
                    }
                }
                else { //Player vs Player Game
                    if (whitePlayerId != null && blackPlayerId != null && gameState == GameStatus.PLAYER_MOVE) {
                        if (event.getAuthor().getId().equals(blackPlayerId)) { // Black quit
                            db.updateUser(blackPlayerId, false, false, blackPlayerElo, whitePlayerElo);
                            db.updateUser(whitePlayerId, true, false, whitePlayerElo, blackPlayerElo);
                            db.addCompletedMatch(whitePlayerName, blackPlayerName, whitePlayerId, blackPlayerId, whitePlayerName, blackPlayerName, false, matchStartTime);
                        }
                        else if (event.getAuthor().getId().equals(whitePlayerId)) { // White quit
                            db.updateUser(blackPlayerId, true, false, blackPlayerElo, whitePlayerElo);
                            db.updateUser(whitePlayerId, false, false, whitePlayerElo, blackPlayerElo);
                            db.addCompletedMatch(whitePlayerName, blackPlayerName, whitePlayerId, blackPlayerId, blackPlayerName, whitePlayerName, false, matchStartTime);
                        }
                    }
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
        switch (gameState) {
            case PLAYER_MOVE:
                //Check if it is their turn
                oldMessageIds.add(event.getMessageId());
                if (chessGame.isWhitePlayerTurn() && !event.getAuthor().getId().equals(whitePlayerId)) {
                    reply = "It's `" + whitePlayerName + "'s` turn";
                    belowMessage = null;
                    boardImageFile = null;
                    break;
                }
                else if (!chessGame.isWhitePlayerTurn() && !event.getAuthor().getId().equals(blackPlayerId)) {
                    reply = "It's `" + blackPlayerName + "'s` turn";
                    belowMessage = null;
                    boardImageFile = null;
                    break;
                }
                //Start move
                reply = chessGame.processMove(message);
                if (reply.contains("CHECKMATE") || reply.contains("DRAW")) {
                    boardImageFile = new File(gameBoardImageLoc);
                    belowMessage = "GG";
                    if (reply.contains("DRAW")) {
                        db.updateUser(blackPlayerId, false, true, blackPlayerElo, whitePlayerElo);
                        db.updateUser(whitePlayerId, false, true, whitePlayerElo, blackPlayerElo);
                        db.addCompletedMatch(whitePlayerName, blackPlayerName, whitePlayerId, blackPlayerId, "", "", true, matchStartTime);
                    }
                    else { //CHECKMATE
                        if (chessGame.isWhitePlayerTurn()) { // Black checkmated White
                            reply = "`" + blackPlayerName + "` has CHECKMATED `" + whitePlayerName + "`";
                            db.updateUser(blackPlayerId, true, false, blackPlayerElo, whitePlayerElo);
                            db.updateUser(whitePlayerId, false, false, whitePlayerElo, blackPlayerElo);
                            db.addCompletedMatch(whitePlayerName, blackPlayerName, whitePlayerId, blackPlayerId, blackPlayerName, whitePlayerName, false, matchStartTime);
                        }
                        else { // White checkmated Black
                            reply = "`" + whitePlayerName + "` has CHECKMATED `" + blackPlayerName + "`";
                            db.updateUser(blackPlayerId, false, false, blackPlayerElo, whitePlayerElo);
                            db.updateUser(whitePlayerId, true, false, whitePlayerElo, blackPlayerElo);
                            db.addCompletedMatch(whitePlayerName, blackPlayerName, whitePlayerId, blackPlayerId, whitePlayerName, blackPlayerName, false, matchStartTime);
                        }
                    }
                } else if (reply.contains("CHECK") || reply.contains("Success!")) {
                    boardImageFile = new File(gameBoardImageLoc);
                    if (reply.contains("Success!")) {
                        reply = reply.replace("Success!", ""); //Remove success from the reply message
                        if (chessGame.isWhitePlayerTurn()) {
                            reply = "`" + blackPlayerName + "` SELECTS " + reply;
                            belowMessage = "`" + whitePlayerName + "'s` turn. ";
                        }
                        else {
                            reply = "`" + whitePlayerName + "` SELECTS " + reply;
                            belowMessage = "`" + blackPlayerName + "'s` turn. ";
                        }
                    }
                    else { //CHECK
                        reply = reply.replace("CHECK", ""); //Remove CHECK from the reply message
                        if (chessGame.isWhitePlayerTurn()) {
                            reply = "`" + blackPlayerName + "` SELECTS " + reply + " `" + whitePlayerName + "` is in check!";
                            belowMessage = "`" + whitePlayerName + "'s` turn. ";
                        }
                        else {
                            reply = "`" + whitePlayerName + "` SELECTS " + reply + " `" + blackPlayerName + "` is in check!";
                            belowMessage = "`" + blackPlayerName + "'s` turn. ";
                        }
                    }
                    if (gameMode.isPlayerVsPlayer()) {
                        belowMessage += "Make a move (ex: `c2c4` or `help` or `helpb2` to see possible moves or `q` to forfeit the game)";
                    }
                    else { //Only change turn if move went through
                        gameState = GameStatus.COMPUTER_MOVE;
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
                    whitePlayerElo = db.addUser(event.getAuthor().getId(), event.getAuthor().getName());
                    whitePlayerName = event.getAuthor().getName();
                    whitePlayerId = event.getAuthor().getId();
                    gameState = GameStatus.CHALLENGE_OPPONENT;
                    gameMode = GameMode.PVP;
                }
                else if (reply.equals(GameMode.PVC.toString())) {
                    boardImageFile = new File(gameBoardImageLoc);
                    whitePlayerName = event.getAuthor().getName();
                    blackPlayerName = "Cornelius";
                    whitePlayerElo = db.addUser(event.getAuthor().getId(), event.getAuthor().getName());
                    blackPlayerElo = db.addUser("693282099167494225", "Cornelius");
                    whitePlayerId = event.getAuthor().getId();
                    blackPlayerId = "693282099167494225";//System.getenv("OWNER_ID"); //Cornelius Discord Id
                    gameState = GameStatus.PLAYER_MOVE;
                    gameMode = GameMode.PVC;
                    matchStartTime = System.currentTimeMillis();
                    reply = "`Starting " + whitePlayerName + " (" + whitePlayerElo + ")" + " vs. Cornelius (" + blackPlayerElo + ") Chess Game`\nMake a move (ex: `c2 c4`)";
                }
                else if (reply.equals(GameMode.CVP.toString())) {

                    boardImageFile = new File(gameBoardImageLoc);
                    whitePlayerName = "Cornelius";
                    blackPlayerName = event.getAuthor().getName();
                    whitePlayerElo = db.addUser("693282099167494225", "Cornelius");
                    blackPlayerElo = db.addUser(event.getAuthor().getId(), event.getAuthor().getName());
                    whitePlayerId = "693282099167494225";//System.getenv("OWNER_ID"); //Cornelius Discord Id
                    blackPlayerId = event.getAuthor().getId();
                    gameState = GameStatus.COMPUTER_MOVE;
                    gameMode = GameMode.CVP;
                    matchStartTime = System.currentTimeMillis();
                    reply = "`Starting Cornelius (" + whitePlayerElo + ")" + " vs. " + blackPlayerName + " (" + blackPlayerElo + ") Chess Game`\nCornelius will go first...";
                }
                else {
                    reply = "Please choose from player options `(1-4)`:";
                }
                break;
            case CHALLENGE_OPPONENT:
                //The player who sent the challenge must enter the challengee's  user id
                if (!event.getAuthor().getId().equals(whitePlayerId)) {
                    reply = "`" + whitePlayerName + "` must enter the Challengee user id";
                    belowMessage = null;
                    boardImageFile = null;
                    break;
                }
                Guild guild = event.getGuild();
                Member member = null;
                try {
                   member = guild.getMemberById(message.trim());
                }
                catch (Exception e) {
                    //Do nothing
                }
                if (member == null || !guild.getMembers().contains(member)) {
                    reply = "Opponent does not exist or is not in your discord server. Please reenter userId.";
                }
                else { //Valid opponent found
                    blackPlayerElo = db.addUser(message.trim(), member.getEffectiveName());
                    blackPlayerName = member.getEffectiveName();
                    blackPlayerId = member.getId();
                    reply = "`" + event.getAuthor().getName() + "` challenges <@" + blackPlayerId + "> to a chess game. Challengee must reply `y` to this text chat to accept!";
                    gameState = GameStatus.OPPONENT_ACCEPT_DECLINE;
                }
                break;
            case OPPONENT_ACCEPT_DECLINE:
                if (event.getAuthor().getId().equals(blackPlayerId)) {
                    if (message.equalsIgnoreCase("y")) {
                        reply = "<@" + whitePlayerId + "> ("+whitePlayerElo+") vs <@" + blackPlayerId + "> ("+blackPlayerElo+") Chess Game";
                        boardImageFile = new File(gameBoardImageLoc);
                        belowMessage = "`"+ whitePlayerName + "` goes first. Make a move (ex: `c2 c4`)";
                        gameState = GameStatus.PLAYER_MOVE;
                        matchStartTime = System.currentTimeMillis();
                    }
                    else {
                        reply = "`" + blackPlayerName + "` has declined the chess match";
                    }
                }
                else if (event.getAuthor().getId().equals(whitePlayerId)) {
                    if (message.equalsIgnoreCase("q")) {
                        reply = "`" + whitePlayerName + "` has declined the challenge";
                    }
                    else {
                        reply = "Waiting for <@"+blackPlayerId+"> to accept `y` or decline the challenge. Or type `q` to quit."; //No reply
                    }
                    //Do nothing waiting
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

        if (reply.contains("CHECK") || reply.contains("DRAW") || reply.contains("declined")) {
            endGame(event.getChannel());
        }
    }

    public static void computerAction(MessageReceivedEvent event) {
        reply = chessGame.ai();
        if (reply.contains("CHECKMATE") || reply.contains("DRAW")) {
            boardImageFile = new File(gameBoardImageLoc);
            belowMessage = "GG";
            if (reply.contains("DRAW")) {
                db.updateUser(blackPlayerId, false, true, blackPlayerElo, whitePlayerElo);
                db.updateUser(whitePlayerId, false, true, whitePlayerElo, blackPlayerElo);
                db.addCompletedMatch(whitePlayerName, blackPlayerName, whitePlayerId, blackPlayerId, "", "", true, matchStartTime);
            }
            else { //CHECKMATE
                if (chessGame.isWhitePlayerTurn()) { // Black checkmated White
                    reply = "`" + blackPlayerName + "` has CHECKMATED `" + whitePlayerName + "`";
                    db.updateUser(blackPlayerId, true, false, blackPlayerElo, whitePlayerElo);
                    db.updateUser(whitePlayerId, false, false, whitePlayerElo, blackPlayerElo);
                    db.addCompletedMatch(whitePlayerName, blackPlayerName, whitePlayerId, blackPlayerId, blackPlayerName, whitePlayerName, false, matchStartTime);
                }
                else { // White checkmated Black
                    reply = "`" + whitePlayerName + "` has CHECKMATED `" + blackPlayerName + "`";
                    db.updateUser(blackPlayerId, false, false, blackPlayerElo, whitePlayerElo);
                    db.updateUser(whitePlayerId, true, false, whitePlayerElo, blackPlayerElo);
                    db.addCompletedMatch(whitePlayerName, blackPlayerName, whitePlayerId, blackPlayerId, whitePlayerName, blackPlayerName, false, matchStartTime);
                }
            }
        } else if (reply.contains("CHECK") || reply.contains("Success!")) {
            boardImageFile = new File(gameBoardImageLoc);
            reply = reply.replace("Success!", ""); //Remove success from the reply message
            if (chessGame.isWhitePlayerTurn()) {
                if (reply.contains("CHECK")) {
                    reply = reply.replace("CHECK", ""); //Remove CHECK from the reply message
                    reply = "`" + blackPlayerName + "` SELECTS " + reply + " `" + whitePlayerName + "` is in check!";
                }
                else {
                    reply = "`" + blackPlayerName + "` SELECTS " + reply;
                }
                belowMessage = "`" + whitePlayerName + "'s` turn. Make a move (ex: `c2c4` or `help` or `helpb2` to see possible moves or `q` to forfeit the game)";
            }
            else {
                if (reply.contains("CHECK")) {
                    reply = reply.replace("CHECK", ""); //Remove CHECK from the reply message
                    reply = "`" + whitePlayerName + "` SELECTS " + reply + " `" + blackPlayerName + "` is in check!";
                }
                else {
                    reply = "`" + whitePlayerName + "` SELECTS " + reply;
                }
                belowMessage = "`" + blackPlayerName + "'s` turn. Make a move (ex: `c2c4` or `help` or `helpb2` to see possible moves or `q` to forfeit the game)";
            }
            gameState = GameStatus.PLAYER_MOVE;
        }

        sendMessages(event, reply, boardImageFile, belowMessage);
    }

    public static void endGame(MessageChannel messageChannel) {
        chessGame = null;
        gameState = GameStatus.INACTIVE;
        gameMode = null;
        currentMessageIds.clear();
        oldMessageIds.clear();
        whitePlayerName = null;
        blackPlayerName = null;
        whitePlayerId = null;
        blackPlayerId = null;
        reply = null;
        belowMessage = null;
        boardImageFile = null;
        matchStartTime = null;
        messageChannel.sendMessage("Quitting `Chess`\nCheck out the spreadsheet for game stats:\nhttps://docs.google.com/spreadsheets/d/1lSYTcv2Bucg5OBrGjLvPoi5kWoHos9GqORW9wATqzr4/edit?usp=sharing").queue();
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
