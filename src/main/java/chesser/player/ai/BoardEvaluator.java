package chess.player.ai;

import chess.board.Board;

public interface BoardEvaluator {

    /*
     * Positive number is good for White and Negative number good for Black
     */
    int evaluate(Board board, int depth);
}
