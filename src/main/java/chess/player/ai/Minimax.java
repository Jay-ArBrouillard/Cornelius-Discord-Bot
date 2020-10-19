package chess.player.ai;

import chess.board.Board;
import chess.board.Move;
import chess.player.MoveTransition;

/**
 * Simple Minimax algorithm
 */
public class Minimax implements MoveStrategy {

    private final BoardEvaluator boardEvaluator;
    private final int searchDepth;

    public Minimax(final int searchDepth) {
        this.boardEvaluator = new StandardBoardEvaluator();
        this.searchDepth = searchDepth;
    }

    @Override
    public String toString() {
        return "Minimax";
    }

    @Override
    public long getNumBoardsEvaluated() {
        return 0;
    }

    @Override
    public Move execute(final Board board) {
        final long startTime = System.currentTimeMillis();
        Move bestMove = null;
        int highestSeenValue = Integer.MIN_VALUE;
        int lowestSeenValue = Integer.MAX_VALUE;
        int currentValue;

        System.out.printf("%s player THINKING with depth = %d\n", board.getCurrentPlayer(), searchDepth);

        int numMoves = board.getCurrentPlayer().getLegalMoves().size();

        for (final Move move : board.getCurrentPlayer().getLegalMoves()) {
            final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {
                currentValue = board.getCurrentPlayer().getAlliance().isWhite() ?
                        min(moveTransition.getTransitionBoard(), searchDepth - 1) :
                        max(moveTransition.getTransitionBoard(), searchDepth - 1);

                if (board.getCurrentPlayer().getAlliance().isWhite() && currentValue == highestSeenValue) {
                    if (Math.random() < 0.5) {
                        highestSeenValue = currentValue;
                        bestMove = move;
                    }
                }
                else if (board.getCurrentPlayer().getAlliance().isWhite() && currentValue > highestSeenValue) {
                    highestSeenValue = currentValue;
                    bestMove = move;
                }
                else if (board.getCurrentPlayer().getAlliance().isBlack() && currentValue == lowestSeenValue) {
                    if (Math.random() < 0.5) {
                        lowestSeenValue = currentValue;
                        bestMove = move;
                    }
                }
                else if (board.getCurrentPlayer().getAlliance().isBlack() && currentValue < lowestSeenValue) {
                    lowestSeenValue = currentValue;
                    bestMove = move;
                }
            }
        }

        final long executionTime = System.currentTimeMillis() - startTime;
        System.out.printf("%s Finished THINKING with depth = %d. Elapsed Time = %d seconds\n", board.getCurrentPlayer(), searchDepth, executionTime / 1000);
        return bestMove;
    }
    
    public int min(final Board board, final int depth) {
        if (depth == 0 || isEndGameScenario(board)) {
            return this.boardEvaluator.evaluate(board);
        }
        
        int lowestValueSeen = Integer.MAX_VALUE;
        for (final Move move : board.getCurrentPlayer().getLegalMoves()) {
            final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {
                final int currentValue = max(moveTransition.getTransitionBoard(), depth - 1);
                if (currentValue <= lowestValueSeen) {
                    lowestValueSeen = currentValue;
                }
            }
        }
        
        return lowestValueSeen;
    }
    
    public int max(final Board board, final int depth) {
        if (depth == 0 || isEndGameScenario(board)) {
            return this.boardEvaluator.evaluate(board);
        }

        int highestSeenValue = Integer.MIN_VALUE;
        for (final Move move : board.getCurrentPlayer().getLegalMoves()) {
            final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {
                final int currentValue = min(moveTransition.getTransitionBoard(), depth - 1);
                if (currentValue >= highestSeenValue) {
                    highestSeenValue = currentValue;
                }
            }
        }

        return highestSeenValue;
    }

    private boolean isEndGameScenario(Board board) {
        return board.getCurrentPlayer().isInCheckMate() || board.getCurrentPlayer().isInStaleMate();
    }
}
