package chess.multithread;

import chess.ChessGame;
import chess.ChessGameState;
import chess.tables.ChessPlayer;
import net.dv8tion.jda.api.entities.MessageChannel;

import static chess.ChessConstants.*;

public class TrainThread extends Thread {
    private ChessGame game;
    private ChessPlayer whiteSidePlayer;
    private ChessPlayer blackSidePlayer;
    private ChessGameState state;
    private MessageChannel mc;

    public TrainThread(ChessGame game, ChessPlayer whiteSidePlayer, ChessPlayer blackSidePlayer, ChessGameState state, MessageChannel mc) {
        this.game = game;
        this.whiteSidePlayer = whiteSidePlayer;
        this.blackSidePlayer = blackSidePlayer;
        this.state = state;
        this.mc = mc;
    }

    public void run() {
        String status;
        String reply;
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
                whiteSidePlayer = null;
                blackSidePlayer = null;
                System.gc(); //Attempt to call garbage collector to clear memory
                mc.sendMessage(reply).queue();
                break;
            }

        } while (true);
    }
}
