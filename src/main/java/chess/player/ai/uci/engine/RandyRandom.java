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
         Move selection = moves.get(rand.nextInt(moves.size()));
        return BoardUtils.getPositionAtCoordinate(selection.getCurrentCoordinate()) + BoardUtils.getPositionAtCoordinate(selection.getDestinationCoordinate());
    }

    public void close() throws IOException {
        super.close();
    }
}
