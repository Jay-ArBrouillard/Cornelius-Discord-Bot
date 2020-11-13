package commands;

import chess.*;
import chess.multithread.TrainThread;
import chess.pgn.FenUtils;
import chess.tables.ChessPlayer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import utils.EloRanking;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Instant;
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
    private static DecimalFormat formatPercent = new DecimalFormat("##0.00");

    public static boolean isRunning() {
        return chessGame != null && decision != INACTIVE;
    }

    public static boolean isMessageFromPlayer(String id) {
        return (whiteSidePlayer != null && id.equals(whiteSidePlayer.discordId)) || (blackSidePlayer != null && id.equals(blackSidePlayer.discordId));
    }

    public static void execute(MessageReceivedEvent event, String message) {
        if (Arrays.asList(ChessConstants.QUIT).stream().anyMatch(x -> x.equals(message))) {
            if (isMessageFromPlayer(event.getAuthor().getId())) {
                state.setPlayerForfeit();
                // If less than or equal to 3 full moves have been completed then game ends with no consequences
                if (state.getFullMoves() <= 3) {
                    event.getChannel().sendMessage("**NOTICE** Less than 4 moves were made in this game so no win or loss or draw is awarded").queue();
                }
                else if (gameType != null && gameType.isPlayerVsComputer() && (decision == Decision.PLAYER_MOVE || decision == COMPUTER_MOVE)) { //Player vs Computer game
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
                endGame(event.getChannel()); //Only end game if quit command is from player in the current game
            }
            return;
        }

        if (message.startsWith("!chess addAll")) { handleAddAll(event); return; }
        if (message.startsWith("!chess trainFair")) { handleTrainFairly(event, message); return; }
        if (message.startsWith("!chess trainRandom")) { handleTrainRandom(event, message); return; }
        if (message.startsWith("!chess trainAll")) { handleTrainAll(event); return; }
        if (message.startsWith("!chess trainUser")) { handleTrainUser(event, message); return; }

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
                    reply = String.format("It's `%s's` turn", whiteSidePlayer.name);
                    belowMessage = null;
                    boardImageFile = null;
                    break;
                }
                else if (chessGame.isBlackPlayerTurn() && !event.getAuthor().getId().equals(blackSidePlayer.discordId)) {
                    reply = String.format("It's `%s's` turn", blackSidePlayer.name);
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
                        belowMessage = chessGame.isWhitePlayerTurn() ? String.format("It's `%s's` turn", whiteSidePlayer.name) :
                                                                       String.format("It's `%s's` turn", blackSidePlayer.name) ;
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
                reply = "`Chess` Initialized. Please choose from game options `(1-3)`:\n1. Player (WHITE) vs. Player (BLACK)\n2. Player vs. Computer\n3. Computer vs. Player\n\n" +
                        "After choosing a game option you will make moves using algebraic long notation (ie: e5e7). When playing against the computer you can select your computer opponent by adding their name after the option for example `2 Komodo` or `3 Mikhail Tal`\n" +
                        "Otherwise a random computer that is +/- 50 elo from you will be chosen. If there are none, then the closest elo computer is chosen.";
                decision = Decision.SETUP_RESPONSE;
                break;
            case SETUP_RESPONSE:
                ChessPlayer humanPlayer = chessGame.addUser(event.getAuthor().getId(), event.getAuthor().getName());
                reply = chessGame.setupPlayers(event.getChannel(), message, humanPlayer.elo, humanPlayer.discordId);
                System.gc();
                if (reply.startsWith(GameType.PVP.toString())) {
                    reply = "`Player vs Player Chess Game`\nPlease challenge another player by entering their `userId` (Click on user and Copy ID)";
                    whiteSidePlayer = humanPlayer;
                    chessGame.setWhiteSidePlayer(whiteSidePlayer);
                    state.getPrevElo().put(whiteSidePlayer.discordId, whiteSidePlayer.elo);
                    decision = Decision.CHALLENGE_OPPONENT;
                    gameType = GameType.PVP;
                    chessGame.gameType = GameType.PVP;
                }
                else if (reply.startsWith(GameType.PVC.toString())) {
                    boardImageFile = new File(GAME_BOARD_IMAGE_LOCATION);
                    //Set up white side
                    whiteSidePlayer = humanPlayer;
                    chessGame.setWhiteSidePlayer(whiteSidePlayer);
                    state.getPrevElo().put(whiteSidePlayer.discordId, whiteSidePlayer.elo);
                    //Set up black side
                    String [] gameidName = reply.split("-");
                    blackSidePlayer = chessGame.addUser(gameidName[1], gameidName[2]);
                    state.getPrevElo().put(blackSidePlayer.discordId, blackSidePlayer.elo);
                    chessGame.setBlackSidePlayer(blackSidePlayer);
                    double whiteSideWinChance = EloRanking.calculateProbabilityOfWin(whiteSidePlayer.elo, blackSidePlayer.elo);
                    reply = String.format("`Starting Chess Match! %s (elo: %d, odds: %s%%) vs. %s (elo: %d, odds: %s%%)`\nMake a move (ex: `c2 c4`)", whiteSidePlayer.name,
                                                                                                                            (int)whiteSidePlayer.elo,
                                                                                                                            formatPercent.format(whiteSideWinChance*100),
                                                                                                                            blackSidePlayer.name,
                                                                                                                            (int)blackSidePlayer.elo,
                                                                                                                            formatPercent.format((1-whiteSideWinChance)*100));
                    gameType = GameType.PVC;
                    chessGame.gameType = GameType.PVC;
                    try {
                        chessGame.setupComputerClient(gameType);
                        chessGame.setupStockfishClient();
                        decision = PLAYER_MOVE;
                        state.setMatchStartTime(Instant.now().toEpochMilli());
                    } catch (IOException e) {
                        e.printStackTrace();
                        reply = e.getMessage();
                        state.setStateError();
                    }
                }
                else if (reply.startsWith(GameType.CVP.toString())) {
                    boardImageFile = new File(GAME_BOARD_IMAGE_LOCATION);
                    //Set up white side
                    String [] gameidName = reply.split("-");
                    whiteSidePlayer = chessGame.addUser(gameidName[1], gameidName[2]);   //Note: Difficulty value is appended to id and name
                    chessGame.setWhiteSidePlayer(whiteSidePlayer);
                    state.getPrevElo().put(whiteSidePlayer.discordId, whiteSidePlayer.elo);
                    //Set up black side
                    blackSidePlayer = humanPlayer;
                    chessGame.setBlackSidePlayer(blackSidePlayer);
                    state.getPrevElo().put(blackSidePlayer.discordId, blackSidePlayer.elo);
                    double whiteSideWinChance = EloRanking.calculateProbabilityOfWin(whiteSidePlayer.elo, blackSidePlayer.elo);
                    reply = String.format("`Starting Chess Match!` %s (elo: %d, odds: %s%%) vs. %s (elo: %d, odds: %s%%)\n%s will go first...", whiteSidePlayer.name,
                                                                                                                      whiteSidePlayer.elo,
                                                                                                                      formatPercent.format(whiteSideWinChance*100),
                                                                                                                      blackSidePlayer.name,
                                                                                                                      blackSidePlayer.elo,
                                                                                                                      formatPercent.format((1-whiteSideWinChance)*100),
                                                                                                                      whiteSidePlayer.name);
                    gameType = GameType.CVP;
                    chessGame.gameType = GameType.CVP;
                    try {
                        chessGame.setupComputerClient(gameType);
                        chessGame.setupStockfishClient();
                        decision = COMPUTER_MOVE;
                        state.setMatchStartTime(Instant.now().toEpochMilli());
                    } catch (IOException e) {
                        e.printStackTrace();
                        reply = e.getMessage();
                        state.setStateError();
                    }
                }
                break;
            case CHALLENGE_OPPONENT:
                //The player who sent the challenge must enter the challengee's  user id
                if (!event.getAuthor().getId().equals(whiteSidePlayer.discordId)) {
                    reply = String.format("`%s` must enter the Challengee user id", whiteSidePlayer.name);
                    belowMessage = null;
                    boardImageFile = null;
                    break;
                }
                Guild guild = event.getGuild();
                String discordId = message.trim();
                if (discordId == null || discordId.isEmpty()) {
                    reply = "Please enter a non-empty userId";
                }
                else {
                    Member member = guild.retrieveMemberById(discordId).complete();
                    if (member != null) {
                        blackSidePlayer = chessGame.addUser(discordId, member.getEffectiveName());
                        chessGame.setBlackSidePlayer(blackSidePlayer);
                        state.getPrevElo().put(blackSidePlayer.discordId, blackSidePlayer.elo);
                        reply = String.format("`%s` challenges <@%s> to a chess game. Challengee must reply `y` to this text chat to accept!", whiteSidePlayer.name, blackSidePlayer.discordId);
                        state.setStateWaitingAcceptChallenge();
                        decision = Decision.OPPONENT_ACCEPT_DECLINE;
                    }
                    else {
                        reply = "Opponent does not exist or is not in your discord server. Please reenter userId.";
                    }
                }
                break;
            case OPPONENT_ACCEPT_DECLINE:
                if (event.getAuthor().getId().equals(blackSidePlayer.discordId)) {
                    if (message.equalsIgnoreCase("y")) {
                        double whiteSideWinChance = EloRanking.calculateProbabilityOfWin(whiteSidePlayer.elo, blackSidePlayer.elo);
                        reply = String.format("`Starting Chess Match!` %s (elo: %d, odds: %s%%) vs. %s (elo: %d, odds: %s%%)", whiteSidePlayer.name,
                                                                                                                                whiteSidePlayer.elo,
                                                                                                                                formatPercent.format(whiteSideWinChance*100),
                                                                                                                                blackSidePlayer.name,
                                                                                                                                blackSidePlayer.elo,
                                                                                                                                formatPercent.format((1-whiteSideWinChance)*100),
                                                                                                                                whiteSidePlayer.name);
                        boardImageFile = new File(GAME_BOARD_IMAGE_LOCATION);
                        belowMessage = String.format("`%s` goes first. Make a move (ex: `c2 c4`)", whiteSidePlayer.name);
                        decision = Decision.PLAYER_MOVE;
                        chessGame.setupStockfishClient();
                        state.setMatchStartTime(Instant.now().toEpochMilli());
                    }
                    else {
                        reply = String.format("`%s` has declined the chess match", blackSidePlayer.name);
                        state.setStateChallengeeDecline();
                    }
                }
                else if (event.getAuthor().getId().equals(whiteSidePlayer.discordId)) {
                    if (message.equalsIgnoreCase("q")) {
                        reply = String.format("`%s` has declined the chess match", whiteSidePlayer.name);
                        state.setStateChallengeeDecline();
                    }
                    else {
                        reply = String.format("Waiting for <@%s> to accept `y` or decline the challenge. Only they can accept or type `q` to quit.", blackSidePlayer.discordId);
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

    private static void handleAddAll(MessageReceivedEvent event) {
        event.getChannel().sendMessage("Starting adding all computer players...").queue();
        String[][] players = getAIList();
        chessGame = new ChessGame(null);
        for (int i = 0; i < players.length; i++) {
            //google api limit is 1 per second
            ChessPlayer p = chessGame.addUser(players[i][0], players[i][1]); //This executes 3 requests
            event.getChannel().sendMessage("Added " + p.name + " - " + p.discordId).queue();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        chessGame = null;
        event.getChannel().sendMessage("Completed").queue();
    }

    private static void handleTrainAll(MessageReceivedEvent event) {
        event.getChannel().sendMessage("Starting training all matches...").queue();
        String[][] players = getAIList();
        randomizeList(players);

        TrainThread[] threads = new TrainThread[4];
        ArrayList<ArrayList<String>> allMatchups = new ArrayList<>();
        for (int i = 0; i < players.length; i++) {
            for (int j = 0; j < players.length; j++) {
                if (i == j) continue;
                allMatchups.add(new ArrayList<>(Arrays.asList(players[i][0], players[i][1], players[j][0], players[j][1])));
            }
        }

        List<String> playersInGame = new ArrayList<>();
        while (allMatchups.size() > 0) {
            boolean isThreadOpen = false;
            int threadIndex = 0;
            while (!isThreadOpen) {
                for (int k = 0; k < threads.length; k++) {
                    TrainThread currThread = threads[k];
                    if (currThread == null || !currThread.isAlive()) {
                        threadIndex = k;
                        threads[k] = null;
                        isThreadOpen = true;
                        System.gc();
                        break;
                    }
                }
            }
            List<String> matchup = null;

            // Find a match that is not running. Same player can't be in two games at the same time
            // Randomize list everytime so same players don't play in order
            Collections.shuffle(allMatchups);
            for (int i = 0; i < allMatchups.size(); i++) {
                List currMatchup = allMatchups.get(i);
                if (!playersInGame.contains(currMatchup.get(0)) && !playersInGame.contains(currMatchup.get(2))) {
                    //found a free matchup
                    matchup = allMatchups.remove(i);
                    System.gc();
                    break;
                }
            }

            if (matchup != null) {
                //matchup - id1, name1, id2, name2
                playersInGame.add(matchup.get(0)); // Add id1
                playersInGame.add(matchup.get(2)); // Add id2
                try {
                    threads[threadIndex] = new TrainThread(matchup.get(0), matchup.get(1), matchup.get(2), matchup.get(3), threadIndex, event.getChannel(), playersInGame);
                    threads[threadIndex].start();
                } catch (IOException ie) {
                    System.out.println(String.format("Error starting match on thread %d: %s", threadIndex, ie.getMessage()));
                    playersInGame.remove(matchup.get(0)); // Remove id1
                    playersInGame.remove(matchup.get(2)); // Remove id2
                } finally {
                    event.getChannel().sendMessage("Matches left to start: " + (allMatchups.size()+1)).queue();
                }
            }
        }

        event.getChannel().sendMessage("Completed Train All").queue();
        endGame(event.getChannel());
    }

    private static void randomizeList(String[][] players) {
        Random rand = new Random();
        for (int i = 0; i < players.length; i++) {
            int randomIndex = rand.nextInt(players.length);
            String temp = players[i][0];
            players[i][0] = players[randomIndex][0];
            players[randomIndex][0] = temp;

            String temp2 = players[i][1];
            players[i][1] = players[randomIndex][1];
            players[randomIndex][1] = temp2;
        }
    }

    private static void handleTrainFairly(MessageReceivedEvent event, String message) {
        //Ex: !chess trainFair range 50 10 - Every player would play 10 games against opponents in a +- 50 range
        String [] split = message.split("\\s+");
        if (split.length != 4) {
            event.getChannel().sendMessage("Incorrect format for `!chess trainFair`. Valid examples include `!chess trainFair 50 1`, `!chess trainFair 100 10`, etc...").queue();
            return;
        }

        event.getChannel().sendMessage("Train all computer players vs fair opponents...").queue();

        int range = Integer.parseInt(split[2]);
        int gamesPerPlayer = Integer.parseInt(split[3]);
        TrainThread[] threads = new TrainThread[3];
        ArrayList<ArrayList<String>> allMatchups = new ArrayList<>();
        List<List<Object>> userObjects = new ChessGame(null).getAllUsers();
        Map<String, Integer> playerGamesMap = new HashMap<>();
        userObjects.remove(0); // Remove header row
        event.getChannel().sendMessage(String.format("Attempting to find %d opponent(s) in a +/-%d range for each player...", gamesPerPlayer, range)).queue();
        for (List row : userObjects) {
            String id1 = (String) row.get(0);
            if (!id1.startsWith(System.getenv("OWNER_ID"))) continue; //Ensure player is a bot
            String name1 = (String) row.get(1);
            int elo1 = Integer.parseInt((String) row.get(2));
            int lowerBound = elo1 - range;
            int upperBound = elo1 + range;
            int gamesFoundForPlayer = playerGamesMap.containsKey(id1) ? playerGamesMap.get(id1) : 0;
            if (gamesFoundForPlayer < gamesPerPlayer) {
                for (List row2 : userObjects) {
                    String id2 = (String) row2.get(0);
                    if (id1.equals(id2)) continue; // Don't play itself
                    if (id2.startsWith(System.getenv("OWNER_ID"))) { //Ensure opponent is a bot
                        String name2 = (String) row2.get(1);
                        int elo2 = Integer.parseInt((String) row2.get(2));
                        if (elo2 >= lowerBound && elo2 <= upperBound) {
                            allMatchups.add(new ArrayList<>(Arrays.asList(id1, name1, id2, name2)));
                            gamesFoundForPlayer++;
                        }
                        if (elo2 < lowerBound) {
                            break;
                        }
                        if (gamesFoundForPlayer == gamesPerPlayer) {
                            break;
                        }
                    }
                }
                playerGamesMap.put(id1, gamesFoundForPlayer);
            }
        }
        event.getChannel().sendMessage(String.format("Found %d matches out of %d maximum matches", allMatchups.size(), userObjects.size() * gamesPerPlayer)).queue();
        userObjects = null;
        playerGamesMap = null;
        System.gc();

        List<String> playersInGame = new ArrayList<>();
        while (allMatchups.size() > 0) {
            boolean isThreadOpen = false;
            int threadIndex = 0;
            while (!isThreadOpen) {
                for (int k = 0; k < threads.length; k++) {
                    TrainThread currThread = threads[k];
                    if (currThread == null || !currThread.isAlive()) {
                        threadIndex = k;
                        threads[k] = null;
                        isThreadOpen = true;
                        break;
                    }
                }
            }
            List<String> matchup = null;

            // Find a match that is not running. Same player can't be in two games at the same time
            // Randomize list everytime so same players don't play in order
            Collections.shuffle(allMatchups);
            for (int i = 0; i < allMatchups.size(); i++) {
                List currMatchup = allMatchups.get(i);
                if (!playersInGame.contains(currMatchup.get(0)) && !playersInGame.contains(currMatchup.get(2))) {
                    //found a free matchup
                    matchup = allMatchups.remove(i);
                    System.gc();
                    break;
                }
            }

            if (matchup != null) {
                //matchup - id1, name1, id2, name2
                playersInGame.add(matchup.get(0)); // Add id1
                playersInGame.add(matchup.get(2)); // Add id2
                try {
                    threads[threadIndex] = new TrainThread(matchup.get(0), matchup.get(1), matchup.get(2), matchup.get(3), threadIndex, event.getChannel(), playersInGame);
                    threads[threadIndex].start();
                } catch (IOException ie) {
                    System.out.println(String.format("Error starting match on thread %d: %s", threadIndex, ie.getMessage()));
                    playersInGame.remove(matchup.get(0)); // Remove id1
                    playersInGame.remove(matchup.get(2)); // Remove id2
                } finally {
                    event.getChannel().sendMessage("Matches left to start: " + (allMatchups.size()+1)).queue();
                }
            }
        }

        event.getChannel().sendMessage("Completed Train Fairly").queue();
        endGame(event.getChannel());
    }

    private static void handleTrainRandom(MessageReceivedEvent event, String message) {
        //Ex: !chess train 10 - Every player would play 10 games against random opponents
        String [] split = message.split("\\s+");
        if (split.length != 3) {
            event.getChannel().sendMessage("Incorrect format for `!chess trainRandom`. Valid examples include `!chess trainRandom 1`, `!chess trainRandom 10`, etc...").queue();
            return;
        }

        event.getChannel().sendMessage("Train all computer players vs random opponents...").queue();
        Random rand = new Random();

        int gamesPerPlayer = Integer.parseInt(split[2]);
        TrainThread[] threads = new TrainThread[3];
        ArrayList<ArrayList<String>> allMatchups = new ArrayList<>();
        List<List<Object>> userObjects = new ChessGame(null).getAllUsers();
        userObjects.remove(0); // Remove header row
        for (int i = 0; i < userObjects.size(); i++) {
            List<Object> row = userObjects.get(i);
            String id1 = (String) row.get(0);
            String name1 = (String) row.get(1);
            if (!id1.contains(System.getenv("OWNER_ID"))) continue; //Ensure player is a bot
            for (int j = 0; j < gamesPerPlayer; j++) {
                int randomIndex = rand.nextInt(userObjects.size());
                while (randomIndex == i || !((String)userObjects.get(randomIndex).get(0)).contains(System.getenv("OWNER_ID"))) {
                    randomIndex = rand.nextInt(userObjects.size());
                }
                List<Object> oppRow = userObjects.get(randomIndex);
                allMatchups.add(new ArrayList<>(Arrays.asList(id1, name1, (String) oppRow.get(0), (String) oppRow.get(1))));
            }
        }
        userObjects = null;
        System.gc();
        event.getChannel().sendMessage(String.format("Found %d matches - %d matche(s) per player", allMatchups.size(), gamesPerPlayer)).queue();

        List<String> playersInGame = new ArrayList<>();
        while (allMatchups.size() > 0) {
            boolean isThreadOpen = false;
            int threadIndex = 0;
            while (!isThreadOpen) {
                for (int k = 0; k < threads.length; k++) {
                    TrainThread currThread = threads[k];
                    if (currThread == null || !currThread.isAlive()) {
                        threadIndex = k;
                        threads[k] = null;
                        isThreadOpen = true;
                        break;
                    }
                }
            }
            List<String> matchup = null;

            // Find a match that is not running. Same player can't be in two games at the same time
            // Randomize list everytime so same players don't play in order
            Collections.shuffle(allMatchups);
            for (int i = 0; i < allMatchups.size(); i++) {
                List currMatchup = allMatchups.get(i);
                if (!playersInGame.contains(currMatchup.get(0)) && !playersInGame.contains(currMatchup.get(2))) {
                    //found a free matchup
                    matchup = allMatchups.remove(i);
                    System.gc();
                    break;
                }
            }

            if (matchup != null) {
                //matchup - id1, name1, id2, name2
                playersInGame.add(matchup.get(0)); // Add id1
                playersInGame.add(matchup.get(2)); // Add id2
                try {
                    threads[threadIndex] = new TrainThread(matchup.get(0), matchup.get(1), matchup.get(2), matchup.get(3), threadIndex, event.getChannel(), playersInGame);
                    threads[threadIndex].start();
                } catch (IOException ie) {
                    System.out.println(String.format("Error starting match on thread %d: %s", threadIndex, ie.getMessage()));
                    playersInGame.remove(matchup.get(0)); // Remove id1
                    playersInGame.remove(matchup.get(2)); // Remove id2
                } finally {
                    event.getChannel().sendMessage("Matches left to start: " + (allMatchups.size()+1)).queue();
                }
            }
        }

        event.getChannel().sendMessage("Completed train Random").queue();
        endGame(event.getChannel());
    }

    private static void handleTrainUser(MessageReceivedEvent event, String message) {
        //Ex: !chess train discordId range 10 - Player with this id would play 10 games against similar elo opponents
        String [] split = message.split("\\s+");
        if (split.length != 5) {
            event.getChannel().sendMessage("Incorrect format for `!chess trainUser discordId range number`. Valid example `!chess trainUser 693282099167494225FN3000 50 10`").queue();
            return;
        }

        event.getChannel().sendMessage("Train user vs similar elo players...").queue();
        String[][] players = getAIList();

        int gamesCompleted = 0;
        String discordId = split[2];
        int range = Integer.parseInt(split[3]);
        int totalGames = Integer.parseInt(split[4]);
        int playerIndex = 0;
        for (int i = 0; i < players.length; i++) {
            if (players[i][0].equals(discordId)) {
                playerIndex = i;
                break;
            }
        }

        whiteSidePlayer = new ChessGame(null).addUser(players[playerIndex][0], players[playerIndex][1]);
        if (whiteSidePlayer == null) {
            totalGames--;
            event.getChannel().sendMessage(String.format("Error finding player with id %s from database.", players[playerIndex][0])).queue();
            endGame(event.getChannel());
            return;
        }

        while (gamesCompleted < totalGames) {
            state = new ChessGameState();
            chessGame = new ChessGame(state);
            blackSidePlayer = chessGame.findOpponentSimilarElo(whiteSidePlayer.elo, whiteSidePlayer.discordId, range);
            if (blackSidePlayer == null) {
                totalGames--;
                event.getChannel().sendMessage(String.format("Error finding opponent from database skipping match... Now %d total matches left", totalGames - gamesCompleted)).queue();
                continue;
            }

            chessGame.setBlackSidePlayer(blackSidePlayer);
            chessGame.setWhiteSidePlayer(whiteSidePlayer);
            gameType = GameType.CVC;
            chessGame.gameType = GameType.CVC;
            state.getPrevElo().put(whiteSidePlayer.discordId, whiteSidePlayer.elo);
            state.getPrevElo().put(blackSidePlayer.discordId, blackSidePlayer.elo);
            try {
                chessGame.setupComputerClient(gameType);
                chessGame.setupStockfishClient();
                decision = COMPUTER_MOVE;
                state.setMatchStartTime(Instant.now().toEpochMilli());
            } catch (IOException e) {
                e.printStackTrace();
                event.getChannel().sendMessage(e.toString()).queue();
                totalGames--;
                chessGame = null;
                state = null;
                System.gc(); //Attempt to call garbage collector to clear memory
                continue;
            }

            event.getChannel().sendMessage("Beginning match (" + (gamesCompleted+1) + "/" + totalGames + ") : " + whiteSidePlayer.name + " vs " + blackSidePlayer.name).queue();
            String status;
            do {
                state = chessGame.ai(null);
                reply = state.getMessage();
                status = state.getStatus();

                boolean isGameOver = CHECKMATE.equals(status) || DRAW.equals(status) || COMPUTER_RESIGN.equals(status) || ERROR.equals(status);
                long minutesElapsed = (Instant.now().toEpochMilli() - state.getMatchStartTime()) / 1000 / 60;
                if (minutesElapsed >= 3.5) { //3.5 minutes
                    if (chessGame.didWhiteJustMove()) {
                        System.out.println(String.format("client:%s, reply:%s, status:%s, fen:%s", chessGame.client1, reply, status, FenUtils.parseFEN(chessGame.board)));
                    }
                    else {
                        System.out.println(String.format("client:%s, reply:%s, status:%s, fen:%s", chessGame.client2, reply, status, FenUtils.parseFEN(chessGame.board)));
                    }
                    if (minutesElapsed >= 10 && !isGameOver) {
                        event.getChannel().sendMessage((String.format("Ending match for %s vs %s because match is taking longer than 10 minutes to complete", whiteSidePlayer.name, blackSidePlayer.name))).queue();
                        totalGames--;
                        break;
                    }
                }

                if (isGameOver) {
                    try {
                        if (chessGame != null) {
                            if (chessGame.stockFishClient != null) chessGame.stockFishClient.close();
                            if (chessGame.client1 != null) chessGame.client1.close();
                            if (chessGame.client2 != null) chessGame.client2.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    finally {
                        state = null;
                        chessGame.stockFishClient = null;
                        chessGame.client1 = null;
                        chessGame.client2 = null;
                        chessGame.board = null;
                        chessGame.blackSidePlayer = null;
                        chessGame.whiteSidePlayer = null;
                        chessGame.state = null;
                        chessGame.messageHandler = null;
                        chessGame.db = null;
                        chessGame = null;
                        blackSidePlayer = null;
                        event.getChannel().sendMessage(reply).queue();
                        gamesCompleted++;
                        break;
                    }
                }

            } while (true);

            System.gc(); //Attempt to call garbage collector to clear memory
        }

        event.getChannel().sendMessage("Completed TrainUser. Total Matches completed: " + gamesCompleted).queue();
        endGame(event.getChannel());
    }

    private static String[][] getAIList() {
        return new String[][]{
                {"693282099167494225JimmyRO4", "Jimmy"},
                {"693282099167494225Nemo6.0", "Nemorino"},
                {"693282099167494225rofchade2.3", "Rofchade"},
                {"693282099167494225LazyGull0.4", "LazyGull"},
                {"693282099167494225CavemanRO4", "Caveman"},
                {"693282099167494225MasterRO4", "Master"},
                {"693282099167494225BenRO4", "Ben"},
                {"693282099167494225BlaisRO4", "Blais"},
                {"693282099167494225ChrisRO4", "Chris"},
                {"693282099167494225LeaRO4", "Lea"},
                {"693282099167494225NoviceRO4", "Novice"},
                {"693282099167494225PamRO4", "Pam"},
                {"693282099167494225RitaRO4", "Rita"},
                {"693282099167494225ArthurRO4", "Arthur"},
                {"693282099167494225ExpertRO4", "Expert"},
                {"693282099167494225FrederickRO4", "Frederick"},
                {"693282099167494225GabrielRO4", "Gabriel"},
                {"693282099167494225MatthewRO4", "Matthew"},
                {"693282099167494225NerdRO4", "Nerd"},
                {"693282099167494225DorothyRO4", "Dorothy"},
                {"693282099167494225MikeRO4", "Mike"},
                {"693282099167494225NancyRO4", "Nancy"},
                {"693282099167494225SlothRO4", "Sloth"},
                {"693282099167494225TortoiseRO4", "Tortoise"},
                {"693282099167494225RemyRO4", "Remy"},
                {"693282099167494225KingHunterRO4", "Kinghunter"},
                {"693282099167494225SOPHY", "Sophy"},
                {"693282099167494225BarbarianRO4", "Barbarian"},
                {"693282099167494225ARASAN22.1", "Arasan"},
                {"693282099167494225AlbertRO4", "Albert"},
                {"693282099167494225FrankRO4", "Frank"},
                {"693282099167494225NoraRO4", "Nora"},
                {"693282099167494225AmyRO4", "Amy"},
                {"693282099167494225DrunkRO4", "Drunk"},
                {"693282099167494225HelplessRO4", "Helpless"},
                {"693282099167494225FRUIT2.1", "Fruit"},
                {"693282099167494225DONNA4.1", "Donna"},
                {"693282099167494225SUGAR", "Sugar"},
                {"693282099167494225DynamicRO4", "Dynamic"},
                {"693282099167494225MorphyRO4", "Paul Morphy"},
                {"693282099167494225SteinitzRO4", "Wilhelm Steinitz"},
                {"693282099167494225StranglerRO4", "Strangler"},
                {"693282099167494225SwapperRO4", "Swapper"},
                {"693282099167494225TarraschRO4", "Siegbert Tarrasch"},
                {"693282099167494225VincentRO4", "Vincent"},
                {"693282099167494225AnandRO4", "Viswanathan Anand"},
                {"693282099167494225AmandaRO4", "Amanda"},
                {"693282099167494225CloeRO4", "Cloe"},
                {"693282099167494225DeborahRO4", "Deborah"},
                {"693282099167494225DefenderRO4", "Defender"},
                {"693282099167494225GrumpyRO4", "Grumpy"},
                {"693282099167494225NimzowitschRO4", "Aron Nimzowitsch"},
                {"693282099167494225PartisanRO4", "Partisan"},
                {"693282099167494225PawnsackerRO4", "Pawnsacker"},
                {"693282099167494225PedritaRO4", "Pedrita"},
                {"693282099167494225PrestonRO4", "Preston"},
                {"693282099167494225SimpleRO4", "Mr. Simple"},
                {"693282099167494225SpitfireRO4", "Spitfire"},
                {"693282099167494225TalRO4", "Mikhail Tal"},
                {"693282099167494225ZeroRO4", "Zero"},
//                {"693282099167494225AnandRO4", "Viswanathan Anand"},
//                {"693282099167494225AnderssenRO4", "Adolf Anderssen"},
//                {"693282099167494225BotvinnikRO4", "Mikhail Botvinnik"},
//                {"693282099167494225FischerRO4", "Bobby Fischer"},
//                {"693282099167494225PetrosianRO4", "Tigran Petrosian"},
//                {"693282099167494225RetiRO4", "Richard Reti"},
//                {"693282099167494225RubinsteinRO4", "Akiba Rubinstein"},
//                {"693282099167494225SpasskyRO4", "Boris Spassky"},
//                {"693282099167494225TarraschRO4", "Siegbert Tarrasch"},
                {"693282099167494225CL0", "Claudia"},
                {"693282099167494225BARTH", "Bartholomew"},
                {"693282099167494225STEW", "Stewart"},
                {"693282099167494225RR", "Randy Random"},
                {"693282099167494225ET12", "Ethereal"},
            {"693282099167494225MO0.3", "Moustique"},
            {"693282099167494225LW0.6", "LittleWing"},
            {"693282099167494225FR3", "Fridolin"},
            {"693282099167494225FL0.9", "Floyd"},
            {"693282099167494225MO2", "Monolith"},
            {"693282099167494225PI1.5", "Pigeon"},
            {"693282099167494225DU1.4", "Dumb"},
            {"693282099167494225A0.8", "Asymptote"},
            {"693282099167494225CG3.6", "CounterGo"},
            {"693282099167494225A3.2", "Amoeba"},
            {"693282099167494225L1.7", "Laser"},
            {"693282099167494225C2.3", "Cinnamon"},
            {"693282099167494225X0.6", "Xiphos"},
            {"693282099167494225K12.1", "Komodo"},
//            {"693282099167494225CH800", "Cheng 800"},
//            {"693282099167494225CH900", "Cheng 900"},
//            {"693282099167494225CH1000", "Cheng 1000"},
//            {"693282099167494225CH1100", "Cheng 1100"},
//            {"693282099167494225CH1200", "Cheng 1200"},
//            {"693282099167494225CH1300", "Cheng 1300"},
//            {"693282099167494225CH1400", "Cheng 1400"},
//            {"693282099167494225CH1500", "Cheng 1500"},
//            {"693282099167494225CH1600", "Cheng 1600"},
//            {"693282099167494225CH1700", "Cheng 1700"},
//            {"693282099167494225CH1800", "Cheng 1800"},
//            {"693282099167494225CH1900", "Cheng 1900"},
//            {"693282099167494225CH2000", "Cheng 2000"},
//            {"693282099167494225CH2100", "Cheng 2100"},
//            {"693282099167494225CH2200", "Cheng 2200"},
//            {"693282099167494225CH2300", "Cheng 2300"},
//            {"693282099167494225CH2400", "Cheng 2400"},
            {"693282099167494225CH2500", "Cheng"},
//            {"693282099167494225CO1350", "Cornelius 1350"},
//            {"693282099167494225CO1450", "Cornelius 1450"},
//            {"693282099167494225CO1550", "Cornelius 1550"},
//            {"693282099167494225CO1650", "Cornelius 1650"},
//            {"693282099167494225CO1750", "Cornelius 1750"},
//            {"693282099167494225CO1850", "Cornelius 1850"},
//            {"693282099167494225CO1950", "Cornelius 1950"},
//            {"693282099167494225CO2050", "Cornelius 2050"},
//            {"693282099167494225CO2150", "Cornelius 2150"},
//            {"693282099167494225CO2250", "Cornelius 2250"},
//            {"693282099167494225CO2350", "Cornelius 2350"},
//            {"693282099167494225CO2450", "Cornelius 2450"},
//            {"693282099167494225CO2550", "Cornelius 2550"},
//            {"693282099167494225CO2650", "Cornelius 2650"},
//            {"693282099167494225CO2750", "Cornelius 2750"},
            {"693282099167494225CO2850", "Cornelius"},
//            {"693282099167494225FN0", "Fishnet 0"},
//            {"693282099167494225FN100", "Fishnet 100"},
//            {"693282099167494225FN200", "Fishnet 200"},
//            {"693282099167494225FN300", "Fishnet 300"},
//            {"693282099167494225FN400", "Fishnet 400"},
//            {"693282099167494225FN500", "Fishnet 500"},
//            {"693282099167494225FN600", "Fishnet 600"},
//            {"693282099167494225FN700", "Fishnet 700"},
//            {"693282099167494225FN800", "Fishnet 800"},
//            {"693282099167494225FN900", "Fishnet 900"},
//            {"693282099167494225FN1000", "Fishnet 1000"},
//            {"693282099167494225FN1100", "Fishnet 1100"},
//            {"693282099167494225FN1200", "Fishnet 1200"},
//            {"693282099167494225FN1300", "Fishnet 1300"},
//            {"693282099167494225FN1400", "Fishnet 1400"},
//            {"693282099167494225FN1500", "Fishnet 1500"},
//            {"693282099167494225FN1600", "Fishnet 1600"},
//            {"693282099167494225FN1700", "Fishnet 1700"},
//            {"693282099167494225FN1800", "Fishnet 1800"},
//            {"693282099167494225FN1900", "Fishnet 1900"},
//            {"693282099167494225FN2000", "Fishnet 2000"},
//            {"693282099167494225FN2100", "Fishnet 2100"},
//            {"693282099167494225FN2200", "Fishnet 2200"},
//            {"693282099167494225FN2300", "Fishnet 2300"},
//            {"693282099167494225FN2400", "Fishnet 2400"},
//            {"693282099167494225FN2500", "Fishnet 2500"},
//            {"693282099167494225FN2600", "Fishnet 2600"},
//            {"693282099167494225FN2700", "Fishnet 2700"},
//            {"693282099167494225FN2800", "Fishnet 2800"},
//            {"693282099167494225FN2900", "Fishnet 2900"},
            {"693282099167494225FN3000", "Fishnet"},
//            {"693282099167494225CT1000", "CT800 1000"},
//            {"693282099167494225CT1100", "CT800 1100"},
//            {"693282099167494225CT1200", "CT800 1200"},
//            {"693282099167494225CT1300", "CT800 1300"},
//            {"693282099167494225CT1400", "CT800 1400"},
//            {"693282099167494225CT1500", "CT800 1500"},
//            {"693282099167494225CT1600", "CT800 1600"},
//            {"693282099167494225CT1700", "CT800 1700"},
//            {"693282099167494225CT1800", "CT800 1800"},
//            {"693282099167494225CT1900", "CT800 1900"},
//            {"693282099167494225CT2000", "CT800 2000"},
//            {"693282099167494225CT2100", "CT800 2100"},
//            {"693282099167494225CT2200", "CT800 2200"},
//            {"693282099167494225CT2300", "CT800 2300"},
//            {"693282099167494225CT2400", "CT800 2400"},
//            {"693282099167494225CT2500", "CT800 2500"},
//            {"693282099167494225CT2600", "CT800 2600"},
//            {"693282099167494225CT2700", "CT800 2700"},
//            {"693282099167494225CT2800", "CT800 2800"},
//            {"693282099167494225CT2900", "CT800 2900"},
            {"693282099167494225CT3000", "CT800"},
        };
    }

    public static void sendMessages(MessageReceivedEvent event, String reply, File file, String belowMessage) {
        if (!oldMessageIds.isEmpty()) {
            event.getChannel().purgeMessagesById(oldMessageIds);
            oldMessageIds.clear();
        }
        if (reply == null || reply.isEmpty()) return;
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
        if (CHECKMATE.equals(status) || DRAW.equals(status) || COMPUTER_RESIGN.equals(status) || ERROR.equals(status)) {
            boardImageFile = new File(GAME_BOARD_IMAGE_LOCATION);
            belowMessage = "GG";
        } else if (CHECK.equals(status) || SUCCESSFUL_MOVE.equals(status)) {
            boardImageFile = new File(GAME_BOARD_IMAGE_LOCATION);
            if (chessGame.isWhitePlayerTurn()) {
                belowMessage = String.format("`%s's` turn. Make a move (ex: `c2c4` or `help` or `helpb2` to see possible moves or `q` to forfeit the game)", whiteSidePlayer.name);
            }
            if (chessGame.isBlackPlayerTurn()) {
                belowMessage = String.format("`%s's` turn. Make a move (ex: `c2c4` or `help` or `helpb2` to see possible moves or `q` to forfeit the game)", blackSidePlayer.name);
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
            chessGame = null;
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
