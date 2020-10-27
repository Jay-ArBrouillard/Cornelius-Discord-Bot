package chess.player.ai.uci.engine;

import chess.board.BoardUtils;
import chess.board.Move;
import chess.player.ai.uci.engine.enums.Query;

import java.util.*;
import java.io.IOException;

public class RandyRandom extends UCIEngine {
    Random rand;
    public RandyRandom() {
        super();
        rand = new Random();
    }

    public String getBestMove(Query query) {
         List<Move> moves = query.getBoard().getCurrentPlayer().getLegalMoves();
         System.out.println(moves);
         Move selection = moves.get(rand.nextInt(moves.size()));
         String v = BoardUtils.getPositionAtCoordinate(selection.getCurrentCoordinate()) + BoardUtils.getPositionAtCoordinate(selection.getDestinationCoordinate());
         System.out.println(v);
        return v;
    }

    public void close() throws IOException {
        super.close();
    }
}
