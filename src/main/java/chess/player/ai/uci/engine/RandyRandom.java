package chess.player.ai.uci.engine;

import chess.board.BoardUtils;
import chess.board.Move;
import chess.board.Move.PawnPromotion;
import chess.player.MoveTransition;
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
         List<Move> legalMoves = new LinkedList<>();
         for (Move move : query.getBoard().getCurrentPlayer().getLegalMoves()) {
             MoveTransition transition = query.getBoard().getCurrentPlayer().makeMove(move, true);
             if (transition.getMoveStatus().isDone()) {
                 legalMoves.add(move);
             }
         }
         //Legal Moves should always have atleast 1 move in it other the game would be over before we got to this method
         Move selection = legalMoves.get(rand.nextInt(legalMoves.size()));
         String moveNotation = BoardUtils.getPositionAtCoordinate(selection.getCurrentCoordinate()) + BoardUtils.getPositionAtCoordinate(selection.getDestinationCoordinate());
         if (selection instanceof PawnPromotion) {
             String[] promotionTypes = new String[]{"q", "r", "n", "b"};
             moveNotation = moveNotation + promotionTypes[rand.nextInt(promotionTypes.length)];
         }
        return moveNotation;
    }

    public void close() throws IOException {
        super.close();
    }
}
