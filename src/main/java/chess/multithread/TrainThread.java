package chess.multithread;

import chess.ChessGame;
import chess.ChessGameState;
import chess.GameType;
import chess.tables.ChessPlayer;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.time.Instant;

import static chess.ChessConstants.*;

public class TrainThread extends Thread {
    private ChessGame game;
    private ChessGameState state;
    private MessageChannel mc;
    private int threadNum;
    private String whiteSidePlayerName;
    private String blackSidePlayerName;
    private int p1Index;
    private int p2Index;
    private int size;

    public TrainThread(String[][] players, int i, int j, int threadNum, MessageChannel mc) {
        state = new ChessGameState();
        game = new ChessGame(state);
        ChessPlayer whiteSidePlayer = game.addUser(players[i][0], players[i][1]);
        ChessPlayer blackSidePlayer = game.addUser(players[j][0], players[j][1]);
        game.setBlackSidePlayer(blackSidePlayer);
        game.setWhiteSidePlayer(whiteSidePlayer);
        game.setupStockfishClient();
        game.setupComputerClient(GameType.CVC);
        state.getPrevElo().put(whiteSidePlayer.discordId, whiteSidePlayer.elo);
        state.getPrevElo().put(blackSidePlayer.discordId, blackSidePlayer.elo);
        state.setMatchStartTime(Instant.now().toEpochMilli());
        this.p1Index = i;
        this.p2Index = j;
        this.size = players.length;
        this.threadNum = threadNum;
        this.whiteSidePlayerName = players[i][1];
        this.blackSidePlayerName = players[j][1];
        this.mc = mc;
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
                try {
                    if (game != null) {
                        if (game.stockFishClient != null) game.stockFishClient.close();
                        if (game.client1 != null) game.client1.close();
                        if (game.client2 != null) game.client2.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                state = null;
                game.stockFishClient = null;
                game.client1 = null;
                game.client2 = null;
                game.board = null;
                game.blackSidePlayer = null;
                game.whiteSidePlayer = null;
                game.state = null;
                game.messageHandler = null;
                game.db = null;
                game.id = null;
                game = null;
                System.gc(); //Attempt to call garbage collector to clear memory
                int gameNumber = p1Index * size + p2Index + 1;
                int totalGames = size * size;
                mc.sendMessage("Completed match ("+gameNumber + "/" + totalGames +") " + reply).queue();
                break;
            }

        } while (true);
    }
}
