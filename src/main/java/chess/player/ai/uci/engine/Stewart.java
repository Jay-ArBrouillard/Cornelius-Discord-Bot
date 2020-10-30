package chess.player.ai.uci.engine;

import chess.board.BoardUtils;
import chess.board.Move;
import chess.player.ai.Minimax;
import chess.player.ai.uci.engine.enums.Query;

import java.io.IOException;

public class Stewart extends UCIEngine {
    Minimax engine;
    public Stewart(final int searchDepth, final int searchTimeMillis) {
        super();
        engine = new Minimax(searchDepth, searchTimeMillis);
    }

    public String getBestMove(Query query) {
        Move bestMove = engine.execute(query.getBoard()); //Won't finish until it finds a non-null move
        String moveNotation = BoardUtils.getPositionAtCoordinate(bestMove.getCurrentCoordinate()) + BoardUtils.getPositionAtCoordinate(bestMove.getDestinationCoordinate());
        return moveNotation;
    }

    public void close() throws IOException {
        super.close();
    }
}
