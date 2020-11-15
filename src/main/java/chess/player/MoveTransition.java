package chess.player;

import chess.board.Board;
import chess.board.Move;

public class MoveTransition {
    private Board originalBoard;
    private final Board transitionBoard;
    private final Move move;
    private final MoveStatus moveStatus; //Was move allowed?

    public MoveTransition(final Board transitionBoard, final Move move, final MoveStatus moveStatus) {
        this.transitionBoard = transitionBoard;
        this.move = move;
        this.moveStatus = moveStatus;
    }

    public MoveTransition(Board originalBoard, final Board transitionBoard, final Move move, final MoveStatus moveStatus) {
        this.originalBoard = originalBoard;
        this.transitionBoard = transitionBoard;
        this.move = move;
        this.moveStatus = moveStatus;
    }

    public MoveStatus getMoveStatus() {
        return this.moveStatus;
    }

    public Board getTransitionBoard() {
        return this.transitionBoard;
    }

    public Board getOriginalBoard() {
        return originalBoard;
    }
}
