package chess.multithread;

import chess.ChessGame;
import chess.ChessGameState;
import chess.GameType;
import chess.pgn.FenUtils;
import chess.tables.ChessPlayer;
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
        game.setWhiteSidePlayer(whiteSidePlayer);
        game.setBlackSidePlayer(blackSidePlayer);
        game.gameType = GameType.CVC;
        state.getPrevElo().put(whiteSidePlayer.discordId, whiteSidePlayer.elo);
        state.getPrevElo().put(blackSidePlayer.discordId, blackSidePlayer.elo);
        this.whiteSidePlayerName = name1;
        this.blackSidePlayerName = name2;
        this.whiteSideId = id1;
        this.blackSideId = id2;
        this.threadNum = threadNum;
        this.mc = mc;
        this.playersInGame = playersInGame;
        try {
            game.setupComputerClient(GameType.CVC);
            game.setupStockfishClient();
            state.setMatchStartTime(Instant.now().toEpochMilli());
        }
        catch (Exception e) {
            e.printStackTrace();
            mc.sendMessage(e.getMessage()).queue();
        }
        finally {
            playersInGame.remove(whiteSideId);
            playersInGame.remove(blackSideId);
        }
    }

    public void run() {
        String status;
        String reply;
        if (state.getMatchStartTime() == null) {
            return;
        }
        mc.sendMessage("Beginning match on Thread " + threadNum + ": " + whiteSidePlayerName + " vs " + blackSidePlayerName).queue();
        do {
            state = game.ai(null);
            reply = state.getMessage();
            status = state.getStatus();
            boolean isGameOver = CHECKMATE.equals(status) || DRAW.equals(status) || COMPUTER_RESIGN.equals(status) || ERROR.equals(status);
            long minutesElapsed = (Instant.now().toEpochMilli() - state.getMatchStartTime()) / 1000 / 60;
            if (minutesElapsed >= 3.5) { //3.5 minutes
                if (game.didWhiteJustMove()) {
                    System.out.println(String.format("thread:%d, client:%s, reply:%s, status:%s, fen:%s", threadNum, game.client1, reply, status, FenUtils.parseFEN(game.board)));
                }
                else {
                    System.out.println(String.format("thread:%d, client:%s, reply:%s, status:%s, fen:%s", threadNum, game.client2, reply, status, FenUtils.parseFEN(game.board)));
                }
                if (minutesElapsed >= 10 && !isGameOver) {
                    mc.sendMessage((String.format("Ending match for %s vs %s because match is taking longer than 10 minutes to complete", whiteSidePlayerName, blackSidePlayerName))).queue();
                    break;
                }
            }

            if (isGameOver) {
                String finalReply = reply;
                while (game.threadRunning) {
                    try {
                        Thread.sleep(1000);
                        //In the rare case that the same player plays in a matchup consecutively
                        //Wait to ensure that the new elo is saved in the database before the same player plays again
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (game != null) {
                        if (game.stockFishClient != null) game.stockFishClient.close();
                        if (game.client1 != null) game.client1.close();
                        if (game.client2 != null) game.client2.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    playersInGame.remove(whiteSideId);
                    playersInGame.remove(blackSideId);
                    state = null;
                    game.stockFishClient = null;
                    game.client1 = null;
                    game.client2 = null;
                    game = null;
                    whiteSidePlayerName = null;
                    blackSidePlayerName = null;
                    whiteSideId = null;
                    blackSideId = null;
                    mc.sendMessage("Completed match on Thread " + threadNum + " - " + finalReply).queue();
                    mc = null;
                }
                break;
            }

        } while (true);

        System.gc(); //Attempt to call garbage collector to clear memory
    }
}
