package chess.player.ai.uci.engine;

import chess.board.BoardUtils;
import chess.board.Move;
import chess.player.ai.MinimaxWithIterativeDeepening;
import chess.player.ai.uci.engine.enums.Query;

import java.io.IOException;

public class Bartholomew extends UCIEngine {
    MinimaxWithIterativeDeepening engine;
    public Bartholomew(final int searchDepth) {
        super(); //Empty constructor
        engine = new MinimaxWithIterativeDeepening(searchDepth);
    }

    public String getBestMove(Query query) {
        engine.setThinkTime(query.getMovetime());
        Move bestMove = engine.execute(query.getBoard()); //Won't finish until it finds a non-null move
        String moveNotation = BoardUtils.getPositionAtCoordinate(bestMove.getCurrentCoordinate()) + BoardUtils.getPositionAtCoordinate(bestMove.getDestinationCoordinate());
        return moveNotation;
    }

    public void close() throws IOException {
        super.close();
    }
}
