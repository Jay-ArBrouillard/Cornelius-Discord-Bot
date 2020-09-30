package chess.player.ai;

import chess.board.Board;
import chess.board.Move;

public interface MoveStrategy {

    long getNumBoardsEvaluated();

    Move execute(Board board);
}
