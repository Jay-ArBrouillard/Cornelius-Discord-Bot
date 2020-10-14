package commands;

import chess.*;
import chess.tables.ChessPlayer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static chess.ChessConstants.*;
import static commands.ChessCommand.Decision.*;

public class ChessCommand {

    public enum Decision {
        INACTIVE,
        PLAYER_MOVE,
        COMPUTER_MOVE,
        SETUP,
        SETUP_RESPONSE,
        CHALLENGE_OPPONENT,
        OPPONENT_ACCEPT_DECLINE,
        DATABASE_SAVING,
        SET_COMPUTER_DIFFICULTY
    }

    private static ChessGame chessGame; //Contains business logic - Model
    private static ChessGameState state;//Contains inbetween logic about players and the business logic
    private static ChessPlayer whiteSidePlayer;
    private static ChessPlayer blackSidePlayer;
    private static GameType gameType; //Player vs Player or Player vs Computer or Computer vs Player
    private static Decision decision = INACTIVE;
    private static List<String> currentMessageIds = new ArrayList<>();
    private static List<String> oldMessageIds = new ArrayList<>();
    private static String reply;
    private static String belowMessage;
    private static File boardImageFile;

    public static boolean isRunning() {
        return chessGame != null && decision != INACTIVE;
    }

    public static boolean isMessageFromPlayer(String id) {
        return (whiteSidePlayer != null && id.equals(whiteSidePlayer.discordId)) || (blackSidePlayer != null && id.equals(blackSidePlayer.discordId));
    }

    public static void execute(MessageReceivedEvent event, String message) {
        if (Arrays.asList(ChessConstants.QUIT).contains(message)) {
            if (isMessageFromPlayer(event.getAuthor().getId())) {
                state.setPlayerForfeit();
                if (gameType != null && gameType.isPlayerVsComputer() && (decision == Decision.PLAYER_MOVE || decision == COMPUTER_MOVE)) { //Player vs Computer game
                    chessGame.threadRunning = true;
                    if (event.getAuthor().getId().equals(blackSidePlayer.discordId)) { // Black quit
                        state.setWinnerId(whiteSidePlayer.discordId);
                        new Thread(() -> chessGame.updateDatabaseWhiteSideWin(true)).start();
                    }
                    else if (event.getAuthor().getId().equals(whiteSidePlayer.discordId)) { // White quit
                        state.setWinnerId(blackSidePlayer.discordId);
                        new Thread(() -> chessGame.updateDatabaseBlackSideWin(true)).start();
                    }
                }
                else { //Player vs Player Game
                    if (whiteSidePlayer != null && blackSidePlayer != null && decision == Decision.PLAYER_MOVE) {
                        if (event.getAuthor().getId().equals(blackSidePlayer.discordId)) { // Black quit
                            state.setWinnerId(whiteSidePlayer.discordId);
                            new Thread(() -> chessGame.updateDatabaseWhiteSideWin(true)).start();
                        }
                        else if (event.getAuthor().getId().equals(whiteSidePlayer.discordId)) { // White quit
                            state.setWinnerId(blackSidePlayer.discordId);
                            new Thread(() -> chessGame.updateDatabaseBlackSideWin(true)).start();
                        }
                    }
                }
            }

            endGame(event.getChannel());
            return;
        }

        //TODO fix this method
        if (message.startsWith("!chess") && message.contains("train")) {

            String[][] players = {{"693282099167494225SF"+"0", "Cornelius Stockfish 0"},
                    {"693282099167494225SF"+"1", "Cornelius Stockfish 1"},
                    {"693282099167494225SF"+"2", "Cornelius Stockfish 2"},
                    {"693282099167494225SF"+"3", "Cornelius Stockfish 3"},
                    {"693282099167494225SF"+"4", "Cornelius Stockfish 4"},
                    {"693282099167494225SF"+"5", "Cornelius Stockfish 5"},
                    {"693282099167494225SF"+"6", "Cornelius Stockfish 6"},
                    {"693282099167494225SF"+"7", "Cornelius Stockfish 7"},
                    {"693282099167494225SF"+"8", "Cornelius Stockfish 8"},
                    {"693282099167494225SF"+"9", "Cornelius Stockfish 9"},
                    {"693282099167494225SF"+"10", "Cornelius Stockfish 10"},
                    {"693282099167494225SF"+"11", "Cornelius Stockfish 11"},
                    {"693282099167494225SF"+"12", "Cornelius Stockfish 12"},
                    {"693282099167494225SF"+"13", "Cornelius Stockfish 13"},
                    {"693282099167494225SF"+"14", "Cornelius Stockfish 14"},
                    {"693282099167494225SF"+"15", "Cornelius Stockfish 15"},
                    {"693282099167494225SF"+"16", "Cornelius Stockfish 16"},
                    {"693282099167494225SF"+"17", "Cornelius Stockfish 17"},
                    {"693282099167494225SF"+"18", "Cornelius Stockfish 18"},
                    {"693282099167494225SF"+"19", "Cornelius Stockfish 19"},
                    {"693282099167494225SF"+"20", "Cornelius Stockfish 20"}};

            int gamesCompleted = 0;
            int totalGames = players.length * players.length;

            for (int i = 0; i < players.length; i++) {
                for (int j = 0; j < players.length; j++) {
                    if (i == j) continue; //Don't play itself
                    state = new ChessGameState();
                    chessGame = new ChessGame(state);
                    chessGame.setupComputerClient();
                    whiteSidePlayer = chessGame.addUser(players[i][0], players[i][1]);
                    blackSidePlayer = chessGame.addUser(players[j][0], players[j][1]);
                    chessGame.setBlackSidePlayer(blackSidePlayer);
                    chessGame.setWhiteSidePlayer(whiteSidePlayer);
                    state.getPrevElo().put(whiteSidePlayer.discordId, whiteSidePlayer.elo);
                    state.getPrevElo().put(blackSidePlayer.discordId, blackSidePlayer.elo);
                    state.setMatchStartTime(System.currentTimeMillis());
                    decision = COMPUTER_MOVE;

                    event.getChannel().sendMessage("Beginning match (" + gamesCompleted + "/" + totalGames + ") : " + whiteSidePlayer.name + " vs " + blackSidePlayer.name).queue();
                    String status;
                    do {
                        if (chessGame.board.getCurrentPlayer().getAlliance().isWhite()) {
                            state = chessGame.ai(i);
                        }
                        else {
                            state = chessGame.ai(j);
                        }
                        reply = state.getMessage();
                        status = state.getStatus();

                        if (CHECKMATE.equals(status) || DRAW.equals(status) || COMPUTER_RESIGN.equals(status)) {
                            if (chessGame.client != null) {
                                try {
                                    chessGame.client.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            chessGame = null;
                            whiteSidePlayer = null;
                            blackSidePlayer = null;
                            state = null;
                            System.gc(); //Attempt to call garbage collector to clear memory
                            event.getChannel().sendMessage(reply).queue();
                            gamesCompleted++;
                            break;
                        }

                    } while (true);
                }
            }

            endGame(event.getChannel());
            return;
        }

        // Don't allow a new game to be started until the previous game has saved
        if (decision.equals(DATABASE_SAVING)) {
            event.getChannel().sendMessage("Previous chess match is still saving stats please retry later...").queue();
            return;
        }

        //If game is INACTIVE then a new game should be initalized
        if (decision.equals(INACTIVE)) {
            state = new ChessGameState();
            chessGame = new ChessGame(state);
            chessGame.board.buildImage();
            decision = Decision.SETUP;
        }

        if (BOARD.equals(message)) {
            boardImageFile = new File(GAME_BOARD_IMAGE_LOCATION);
            event.getChannel().sendFile(boardImageFile).queue();
            return;
        }

        reply = null;
        belowMessage = null;
        boardImageFile = null;
        boolean executeComputerMove = true;
        //State machine game mechanics
        switch (decision) {
            case PLAYER_MOVE:
                //Check if it is their turn
                oldMessageIds.add(event.getMessageId());
                if (chessGame.isWhitePlayerTurn() && !event.getAuthor().getId().equals(whiteSidePlayer.discordId)) {
                    reply = "It's `" + whiteSidePlayer.name + "'s` turn";
                    belowMessage = null;
                    boardImageFile = null;
                    break;
                }
                else if (chessGame.isBlackPlayerTurn() && !event.getAuthor().getId().equals(blackSidePlayer.discordId)) {
                    reply = "It's `" + blackSidePlayer.name + "'s` turn";
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
            case COMPUTER_MOVE:
                //Game logic Handled outside of switch
                if (chessGame.isWhitePlayerTurn() && !event.getAuthor().getId().equals(whiteSidePlayer.discordId)) {
                    reply = "It's `" + whiteSidePlayer.name + "'s` turn";
                    belowMessage = null;
                    boardImageFile = null;
                    executeComputerMove = false;
                    break;
                }
                else if (chessGame.isBlackPlayerTurn() && !event.getAuthor().getId().equals(blackSidePlayer.discordId)) {
                    reply = "It's `" + blackSidePlayer.name + "'s` turn";
                    belowMessage = null;
                    boardImageFile = null;
                    executeComputerMove = false;
                    break;
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
                    whiteSidePlayer = chessGame.addUser(event.getAuthor().getId(), event.getAuthor().getName());
                    chessGame.setWhiteSidePlayer(whiteSidePlayer);
                    state.getPrevElo().put(whiteSidePlayer.discordId, whiteSidePlayer.elo);
                    decision = Decision.CHALLENGE_OPPONENT;
                    gameType = GameType.PVP;
                    chessGame.setupComputerClient();
                }
                else if (reply.equals(GameType.PVC.toString())) {
                    whiteSidePlayer = chessGame.addUser(event.getAuthor().getId(), event.getAuthor().getName());
                    chessGame.setWhiteSidePlayer(whiteSidePlayer);
                    state.getPrevElo().put(whiteSidePlayer.discordId, whiteSidePlayer.elo);
                    gameType = GameType.PVC;
                    reply = "Set a difficulty level for Cornelius (0 - 20)";
                    decision = Decision.SET_COMPUTER_DIFFICULTY;
                }
                else if (reply.equals(GameType.CVP.toString())) {
                    blackSidePlayer = chessGame.addUser(event.getAuthor().getId(), event.getAuthor().getName());
                    chessGame.setBlackSidePlayer(blackSidePlayer);
                    state.getPrevElo().put(blackSidePlayer.discordId, blackSidePlayer.elo);
                    gameType = GameType.CVP;
                    reply = "Set a difficulty level Cornelius (0 - 20)";
                    decision = Decision.SET_COMPUTER_DIFFICULTY;
                }
                else {
                    reply = "Please choose from player options `(1-3)`:";
                }
                break;
            case SET_COMPUTER_DIFFICULTY:
                state = chessGame.processDifficulty(message);
                if (INVALID_DIFFICULTY.equals(state.getStatus())) {
                    reply = state.getMessage();
                }
                else {
                    if (GameType.PVC.toString().equals(gameType.name())) {
                        boardImageFile = new File(GAME_BOARD_IMAGE_LOCATION);
                        blackSidePlayer = chessGame.addUser("693282099167494225SF"+message.trim(), "Cornelius Stockfish "+message.trim());  //Note: Difficulty value is appended to id and name
                        state.getPrevElo().put(blackSidePlayer.discordId, blackSidePlayer.elo);
                        chessGame.setBlackSidePlayer(blackSidePlayer);
                        decision = Decision.PLAYER_MOVE;
                        state.setMatchStartTime(System.currentTimeMillis());
                        reply = "`Starting " + whiteSidePlayer.name + " (" + whiteSidePlayer.elo + ")" + " vs. Cornelius Stockfish " + message.trim() + " (" + blackSidePlayer.elo + ") Chess Game`\nMake a move (ex: `c2 c4`)";

                    }
                    else if (GameType.CVP.toString().equals(gameType.name())) {
                        boardImageFile = new File(GAME_BOARD_IMAGE_LOCATION);
                        whiteSidePlayer = chessGame.addUser("693282099167494225SF"+message.trim(), "Cornelius Stockfish "+message.trim());  //Note: Difficulty value is appended to id and name
                        chessGame.setWhiteSidePlayer(whiteSidePlayer);
                        state.getPrevElo().put(whiteSidePlayer.discordId, whiteSidePlayer.elo);
                        decision = COMPUTER_MOVE;
                        state.setMatchStartTime(System.currentTimeMillis());
                        reply = "`Starting Cornelius Stockfish " + message.trim() + " (" + whiteSidePlayer.elo + ")" + " vs. " + blackSidePlayer.name + " (" + blackSidePlayer.elo + ") Chess Game`\nCornelius will go first...";
                    }
                }
                break;
            case CHALLENGE_OPPONENT:
                //The player who sent the challenge must enter the challengee's  user id
                if (!event.getAuthor().getId().equals(whiteSidePlayer.discordId)) {
                    reply = "`" + whiteSidePlayer.name + "` must enter the Challengee user id";
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
                    blackSidePlayer = chessGame.addUser(message.trim(), member.getEffectiveName());
                    chessGame.setBlackSidePlayer(blackSidePlayer);
                    state.getPrevElo().put(blackSidePlayer.discordId, blackSidePlayer.elo);
                    reply = "`" + whiteSidePlayer.name + "` challenges <@" + blackSidePlayer.discordId + "> to a chess game. Challengee must reply `y` to this text chat to accept!";
                    state.setStateWaitingAcceptChallenge();
                    decision = Decision.OPPONENT_ACCEPT_DECLINE;
                }
                break;
            case OPPONENT_ACCEPT_DECLINE:
                if (event.getAuthor().getId().equals(blackSidePlayer.discordId)) {
                    if (message.equalsIgnoreCase("y")) {
                        reply = "<@" + whiteSidePlayer.discordId + "> ("+whiteSidePlayer.elo+") vs <@" + blackSidePlayer.discordId + "> ("+blackSidePlayer.elo+") Chess Game";
                        boardImageFile = new File(GAME_BOARD_IMAGE_LOCATION);
                        belowMessage = "`"+ whiteSidePlayer.name + "` goes first. Make a move (ex: `c2 c4`)";
                        decision = Decision.PLAYER_MOVE;
                        state.setMatchStartTime(System.currentTimeMillis());
                    }
                    else {
                        reply = "`" + blackSidePlayer.name + "` has declined the chess match";
                        state.setStateChallengeeDecline();
                    }
                }
                else if (event.getAuthor().getId().equals(whiteSidePlayer.discordId)) {
                    if (message.equalsIgnoreCase("q")) {
                        reply = "`" + whiteSidePlayer.name + "` has declined the challenge";
                        state.setStateChallengeeDecline();
                    }
                    else {
                        reply = "Waiting for <@"+blackSidePlayer.discordId+"> to accept `y` or decline the challenge. Only they can accept or type `q` to quit."; //No reply
                    }
                    //Do nothing waiting
                }

                break;
            default:
                reply = "Game logic error. Ask Jay-Ar what happened";
        }
        sendMessages(event, reply, boardImageFile, belowMessage);
        if (decision.equals(COMPUTER_MOVE) && executeComputerMove) {
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
        if (CHECKMATE.equals(status) || DRAW.equals(status) || COMPUTER_RESIGN.equals(status) || CHALLENGEE_DECLINE.equals(status)) {
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
                belowMessage = "`" + whiteSidePlayer.name + "'s` turn. Make a move (ex: `c2c4` or `help` or `helpb2` to see possible moves or `q` to forfeit the game)";
            }
            if (chessGame.isBlackPlayerTurn()) {
                belowMessage = "`" + blackSidePlayer.name + "'s` turn. Make a move (ex: `c2c4` or `help` or `helpb2` to see possible moves or `q` to forfeit the game)";
            }
            decision = Decision.PLAYER_MOVE;
        }
        if (state.getBoardEvaluationMessage() != null) reply += " : " + state.getBoardEvaluationMessage();
        sendMessages(event, reply, boardImageFile, belowMessage);
    }

    public static void endGame(MessageChannel messageChannel) {
        new Thread(() -> {
            while (chessGame != null && chessGame.threadRunning) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    messageChannel.sendMessage("Error saving Chess Stats to GoogleSheets: " + e).queue();
                }
            }
            gameType = null;
            decision = INACTIVE;

        }).start();
        decision = DATABASE_SAVING;
        currentMessageIds.clear();
        oldMessageIds.clear();
        whiteSidePlayer = null;
        blackSidePlayer = null;
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

        public static String getName(){
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
