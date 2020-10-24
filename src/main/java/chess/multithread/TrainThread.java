package chess.multithread;

import chess.ChessGame;
import chess.ChessGameState;
import chess.GameType;
import chess.tables.ChessPlayer;
import commands.ChessCommand;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.*;
import java.time.Instant;

import static chess.ChessConstants.*;

public class TrainThread extends Thread {
    private ChessGame game;
    private ChessGameState state;
    private MessageChannel mc;
    private int threadNum;
    public String whiteSidePlayerName;
    public String blackSidePlayerName;
    public String whiteSideId;
    public String blackSideId;
    public List<String> playersInGame;

    public TrainThread(String id1, String name1, String id2, String name2, int threadNum, MessageChannel mc, List<String> playersInGame) {
        state = new ChessGameState();
        game = new ChessGame(state);
        ChessPlayer whiteSidePlayer = game.addUser(id1, name1);
        ChessPlayer blackSidePlayer = game.addUser(id2, name2);
        game.setBlackSidePlayer(blackSidePlayer);
        game.setWhiteSidePlayer(whiteSidePlayer);
        game.setupStockfishClient();
        game.setupComputerClient(GameType.CVC);
        game.gameType = GameType.CVP;
        state.getPrevElo().put(whiteSidePlayer.discordId, whiteSidePlayer.elo);
        state.getPrevElo().put(blackSidePlayer.discordId, blackSidePlayer.elo);
        state.setMatchStartTime(Instant.now().toEpochMilli());
        this.whiteSidePlayerName = name1;
        this.blackSidePlayerName = name2;
        this.whiteSideId = id1;
        this.blackSideId = id2;
        this.threadNum = threadNum;
        this.mc = mc;
        this.playersInGame = playersInGame;
    }

    public void run() {
        String status;
        String reply;
        mc.sendMessage("Beginning match on Thread " + threadNum + ": " + whiteSidePlayerName + " vs " + blackSidePlayerName).queue();
        do {
            state = game.ai(null);
            reply = state.getMessage();
            status = state.getStatus();

            if (CHECKMATE.equals(status) || DRAW.equals(status) || COMPUTER_RESIGN.equals(status)) {
                String finalReply = reply;
                new Thread(() -> {
                    while (game != null && game.threadRunning) {
                        try {
                            Thread.sleep(1000);
                            //In the rare case that the same player plays in a matchup consecutively
                            //Wait to ensure that the new elo is saved in the database before the same player plays again
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        if (game.stockFishClient != null) game.stockFishClient.close();
                        if (game.client1 != null) game.client1.close();
                        if (game.client2 != null) game.client2.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    finally {
                        playersInGame.remove(whiteSideId);
                        playersInGame.remove(blackSideId);
                        state = null;
                        game = null;
                        whiteSidePlayerName = null;
                        blackSidePlayerName = null;
                        whiteSideId = null;
                        blackSideId = null;
                        System.gc(); //Attempt to call garbage collector to clear memory
                        mc.sendMessage("Completed match on Thread " + threadNum + " - " + finalReply).queue();
                        mc = null;
                    }
                }).start();
                break;
            }

        } while (true);
    }
}
