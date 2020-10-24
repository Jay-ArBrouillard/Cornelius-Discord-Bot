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
    public String whiteSidePlayerName;
    public String blackSidePlayerName;

    public TrainThread(String id1, String name1, String id2, String name2, int threadNum, MessageChannel mc) {
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
        this.threadNum = threadNum;
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
                mc.sendMessage("Completed match on Thread " + threadNum + " - " + reply).queue();
                break;
            }

        } while (true);
    }
}
