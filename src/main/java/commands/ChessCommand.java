package commands;

import chess.*;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import java.io.File;
import java.util.*;

import static chess.ChessConstants.*;
import static commands.ChessCommand.Decision.COMPUTER_MOVE;

public class ChessCommand {

    public enum Decision {
        INACTIVE,
        PLAYER_MOVE,
        COMPUTER_MOVE,
        SETUP,
        SETUP_RESPONSE,
        CHALLENGE_OPPONENT,
        OPPONENT_ACCEPT_DECLINE
    }

    private static ChessGame chessGame; //Contains business logic - Model
    private static ChessGameState state = new ChessGameState(); //Contains inbetween logic about players and the business logic
    private static GameType gameType; //Player vs Player or Player vs Computer or Computer vs Player
    private static Decision decision = Decision.INACTIVE;
    private static List<String> currentMessageIds = new ArrayList<>();
    private static List<String> oldMessageIds = new ArrayList<>();
    private static String reply;
    private static String belowMessage;
    private static File boardImageFile;

    public static boolean isRunning() {
        return chessGame != null && decision != Decision.INACTIVE;
    }

    public static boolean isMessageFromPlayer(String id) {
        return id.equals(state.getWhitePlayerId()) || id.equals(state.getBlackPlayerId());
    }

    public static void execute(MessageReceivedEvent event, String message) {
        if (Arrays.asList(ChessConstants.QUIT).contains(message)) {
            if (isMessageFromPlayer(event.getAuthor().getId())) {
                ChessGameState copy = state.clone();
                if (gameType != null && gameType.isPlayerVsComputer() && (decision == Decision.PLAYER_MOVE || decision == COMPUTER_MOVE)) { //Player vs Computer game
                    if (event.getAuthor().getId().equals(state.getBlackPlayerId())) { // Black quit
                        Thread t = new Thread(() -> chessGame.updateDatabaseWhiteSideWin(copy));
                        chessGame.threadRunning = true;
                        t.start();
                    }
                    else if (event.getAuthor().getId().equals(state.getWhitePlayerId())) { // White quit
                        Thread t = new Thread(() -> chessGame.updateDatabaseBlackSideWin(copy));
                        chessGame.threadRunning = true;
                        t.start();
                    }
                }
                else { //Player vs Player Game
                    if (state.getWhitePlayerId() != null && state.getBlackPlayerId() != null && decision == Decision.PLAYER_MOVE) {
                        if (event.getAuthor().getId().equals(state.getBlackPlayerId())) { // Black quit
                            Thread t = new Thread(() -> chessGame.updateDatabaseWhiteSideWin(copy));
                            chessGame.threadRunning = true;
                            t.start();
                        }
                        else if (event.getAuthor().getId().equals(state.getWhitePlayerId())) { // White quit
                            Thread t = new Thread(() -> chessGame.updateDatabaseBlackSideWin(copy));
                            chessGame.threadRunning = true;
                            t.start();
                        }
                    }
                }
            }

            endGame(event.getChannel());
            return;
        }

        //If game is INACTIVE then a new game should be initalized
        if (decision.equals(Decision.INACTIVE)) {
            state = new ChessGameState();
            chessGame = new ChessGame(state);
            chessGame.board.buildImage();
            decision = Decision.SETUP;
        }

        if (ChessConstants.BOARD.equals(state.getStatus())) {
            event.getChannel().sendFile(new File(GAME_BOARD_IMAGE_LOCATION)).queue();
            return;
        }

        reply = null;
        belowMessage = null;
        boardImageFile = null;
        //State machine game mechanics
        switch (decision) {
            case PLAYER_MOVE:
                //Check if it is their turn
                oldMessageIds.add(event.getMessageId());
                if (chessGame.isWhitePlayerTurn() && !event.getAuthor().getId().equals(state.getWhitePlayerId())) {
                    reply = "It's `" + state.getWhitePlayerName() + "'s` turn";
                    belowMessage = null;
                    boardImageFile = null;
                    break;
                }
                else if (chessGame.isBlackPlayerTurn() && !event.getAuthor().getId().equals(state.getBlackPlayerId())) {
                    reply = "It's `" + state.getBlackPlayerName() + "'s` turn";
                    belowMessage = null;
                    boardImageFile = null;
                    break;
                }
                //Start move
                state = chessGame.processMove(message);
                reply = state.getMessage();
                String status = state.getStatus();
                if (CHECKMATE.equals(status) || DRAW.equals(status)) {
                    boardImageFile = new File(GAME_BOARD_IMAGE_LOCATION);
                    belowMessage = "GG";
                } else if (CHECK.equals(status) || SUCCESSFUL_MOVE.equals(status)) {
                    boardImageFile = new File(GAME_BOARD_IMAGE_LOCATION);
                    if (state.getBoardEvaluationMessage() != null) reply += " : " + state.getBoardEvaluationMessage();
                    if (gameType.isPlayerVsPlayer()) {
                        belowMessage = "Make a move (ex: `c2c4` or `help` or `helpb2` to see possible moves or `q` to forfeit the game)";
                    }
                    if (gameType.isPlayerVsComputer()) {
                        decision = COMPUTER_MOVE;
                        belowMessage = "It's `Cornelius`'s turn";
                    }
                }
                break;
            case SETUP:
                reply = "New `Chess` game started. Please choose from player options `(1-3)`:\n1. Player (WHITE) vs. Player (BLACK)\n2. Player vs. Computer\n3. Computer vs. Player";
                decision = Decision.SETUP_RESPONSE;
                break;
            case SETUP_RESPONSE:
                reply = chessGame.setupPlayers(event, message);
                if (reply.equals(GameType.PVP.toString())) {
                    reply = "`Player vs Player Chess Game`\nPlease challenge another player by entering their `userId` (Click on user and Copy ID)";
                    state.setWhitePlayerElo(chessGame.addUser(event.getAuthor().getId(), event.getAuthor().getName()));
                    state.setWhitePlayerName(event.getAuthor().getName());
                    state.setWhitePlayerId(event.getAuthor().getId());
                    decision = Decision.CHALLENGE_OPPONENT;
                    gameType = GameType.PVP;
                }
                else if (reply.equals(GameType.PVC.toString())) {
                    boardImageFile = new File(GAME_BOARD_IMAGE_LOCATION);
                    state.setWhitePlayerName(event.getAuthor().getName());
                    state.setBlackPlayerName("Cornelius");
                    state.setWhitePlayerElo(chessGame.addUser(event.getAuthor().getId(), event.getAuthor().getName()));
                    state.setBlackPlayerElo(chessGame.addUser(System.getenv("OWNER_ID"), "Cornelius"));
                    state.setWhitePlayerId(event.getAuthor().getId());
                    state.setBlackPlayerId(System.getenv("OWNER_ID"));
                    decision = Decision.PLAYER_MOVE;
                    gameType = GameType.PVC;
                    state.setMatchStartTime(System.currentTimeMillis());
                    reply = "`Starting " + state.getWhitePlayerName() + " (" + state.getWhitePlayerElo() + ")" + " vs. Cornelius (" + state.getBlackPlayerElo() + ") Chess Game`\nMake a move (ex: `c2 c4`)";
                }
                else if (reply.equals(GameType.CVP.toString())) {
                    boardImageFile = new File(GAME_BOARD_IMAGE_LOCATION);
                    state.setWhitePlayerName("Cornelius");
                    state.setBlackPlayerName(event.getAuthor().getName());
                    state.setWhitePlayerElo(chessGame.addUser(System.getenv("OWNER_ID"), "Cornelius"));
                    state.setBlackPlayerElo(chessGame.addUser(event.getAuthor().getId(), event.getAuthor().getName()));
                    state.setWhitePlayerId(System.getenv("OWNER_ID")); //Cornelius Discord Id
                    state.setBlackPlayerId(event.getAuthor().getId());
                    decision = COMPUTER_MOVE;
                    gameType = GameType.CVP;
                    state.setMatchStartTime(System.currentTimeMillis());
                    reply = "`Starting Cornelius (" + state.getWhitePlayerElo() + ")" + " vs. " + state.getBlackPlayerName() + " (" + state.getBlackPlayerElo() + ") Chess Game`\nCornelius will go first...";
                }
                else {
                    reply = "Please choose from player options `(1-3)`:";
                }
                break;
            case CHALLENGE_OPPONENT:
                //The player who sent the challenge must enter the challengee's  user id
                if (!event.getAuthor().getId().equals(state.getWhitePlayerId())) {
                    reply = "`" + state.getWhitePlayerName() + "` must enter the Challengee user id";
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
                    state.setBlackPlayerElo(chessGame.addUser(message.trim(), member.getEffectiveName()));
                    state.setBlackPlayerName(member.getEffectiveName());
                    state.setBlackPlayerId(member.getId());
                    reply = "`" + event.getAuthor().getName() + "` challenges <@" + state.getBlackPlayerId() + "> to a chess game. Challengee must reply `y` to this text chat to accept!";
                    decision = Decision.OPPONENT_ACCEPT_DECLINE;
                }
                break;
            case OPPONENT_ACCEPT_DECLINE:
                if (event.getAuthor().getId().equals(state.getBlackPlayerId())) {
                    if (message.equalsIgnoreCase("y")) {
                        reply = "<@" + state.getWhitePlayerId() + "> ("+state.getWhitePlayerElo()+") vs <@" + state.getBlackPlayerId() + "> ("+state.getBlackPlayerElo()+") Chess Game";
                        boardImageFile = new File(GAME_BOARD_IMAGE_LOCATION);
                        belowMessage = "`"+ state.getWhitePlayerName() + "` goes first. Make a move (ex: `c2 c4`)";
                        decision = Decision.PLAYER_MOVE;
                        state.setMatchStartTime(System.currentTimeMillis());
                    }
                    else {
                        reply = "`" + state.getBlackPlayerName() + "` has declined the chess match";
                    }
                }
                else if (event.getAuthor().getId().equals(state.getWhitePlayerId())) {
                    if (message.equalsIgnoreCase("q")) {
                        reply = "`" + state.getWhitePlayerName() + "` has declined the challenge";
                    }
                    else {
                        reply = "Waiting for <@"+state.getBlackPlayerId()+"> to accept `y` or decline the challenge. Or type `q` to quit."; //No reply
                    }
                    //Do nothing waiting
                }

                break;
            default:
                reply = "Game logic error. Ask Jay-Ar what happened";
        }

        sendMessages(event, reply, boardImageFile, belowMessage);
        if (decision.equals(COMPUTER_MOVE)) {
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
        if ((reply != null) && reply.contains("null")) return;
        event.getChannel().sendMessage(reply).queue((msg) -> currentMessageIds.add(msg.getId()));
        if (file != null) {
            event.getChannel().sendFile(file).queue((msg) -> currentMessageIds.add(msg.getId()));
        }
        if (belowMessage != null) {
            event.getChannel().sendMessage(belowMessage).queue((msg) -> currentMessageIds.add(msg.getId()));
        }

        oldMessageIds.addAll(currentMessageIds);
        currentMessageIds.clear();

        String status = state.getStatus();
        if (CHECKMATE.equals(status) || DRAW.equals(status) || COMPUTER_RESIGN.equals(status) || reply.contains("decline")) {
            endGame(event.getChannel());
        }
    }

    public static void computerAction(MessageReceivedEvent event) {
        state = chessGame.ai(event.getChannel());
        reply = state.getMessage();
        String status = state.getStatus();
        if (CHECKMATE.equals(status) || DRAW.equals(status) || COMPUTER_RESIGN.equals(status)) {
            boardImageFile = new File(GAME_BOARD_IMAGE_LOCATION);
            belowMessage = "GG";
        } else if (CHECK.equals(status) || SUCCESSFUL_MOVE.equals(status)) {
            boardImageFile = new File(GAME_BOARD_IMAGE_LOCATION);
            if (chessGame.isWhitePlayerTurn()) {
                belowMessage = "`" + state.getWhitePlayerName() + "'s` turn. Make a move (ex: `c2c4` or `help` or `helpb2` to see possible moves or `q` to forfeit the game)";
            }
            if (chessGame.isBlackPlayerTurn()) {
                belowMessage = "`" + state.getBlackPlayerName() + "'s` turn. Make a move (ex: `c2c4` or `help` or `helpb2` to see possible moves or `q` to forfeit the game)";
            }
            decision = Decision.PLAYER_MOVE;
        }
        if (state.getBoardEvaluationMessage() != null) reply += " : " + state.getBoardEvaluationMessage();
        sendMessages(event, reply, boardImageFile, belowMessage);
    }

    public static void endGame(MessageChannel messageChannel) {
        new Thread(() -> {
            while (chessGame.threadRunning) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    messageChannel.sendMessage("Error saving Chess Stats to GoogleSheets: " + e).queue();
                }
            }
            chessGame = null;
        }).start();
        decision = Decision.INACTIVE;
        gameType = null;
        currentMessageIds.clear();
        oldMessageIds.clear();
        state.setWhitePlayerName(null);
        state.setBlackPlayerName(null);
        state.setWhitePlayerId(null);
        state.setBlackPlayerId(null);
//        state.setMatchStartTime(null);
        reply = null;
        belowMessage = null;
        boardImageFile = null;
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
