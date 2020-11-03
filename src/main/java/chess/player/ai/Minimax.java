package chess.player.ai;

import chess.board.Board;
import chess.board.BoardUtils;
import chess.board.Move;
import chess.player.MoveTransition;

import java.util.concurrent.atomic.AtomicLong;

import static chess.board.Move.*;

/**
 * Simple Minimax algorithm
 */
public class Minimax implements MoveStrategy {

    private BoardEvaluator evaluator;
    private int searchDepth;
    private long boardsEvaluated;
    private FreqTableRow[] freqTable;
    private int freqTableIndex;
    private long thinkTime = 10000; //In millis

    public Minimax(final int searchDepth) {
        this.evaluator = new StandardBoardEvaluator();
        this.boardsEvaluated = 0;
        this.searchDepth = searchDepth;
    }

    public void setThinkTime(long thinkTime) {
        this.thinkTime = thinkTime;
    }

    @Override
    public String toString() {
        return "MiniMax";
    }

    @Override
    public long getNumBoardsEvaluated() {
        return this.boardsEvaluated;
    }

    public Move execute(final Board board) {
        final long startTime = System.currentTimeMillis();
        Move bestMove = NULL_MOVE;
        int highestSeenValue = Integer.MIN_VALUE;
        int lowestSeenValue = Integer.MAX_VALUE;
        int currentValue;
        this.freqTable = new FreqTableRow[board.getCurrentPlayer().getLegalMoves().size()];
        this.freqTableIndex = 0;
        for (final Move move : board.getCurrentPlayer().getLegalMoves()) {
            final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {
                final FreqTableRow row = new FreqTableRow(move);
                this.freqTable[this.freqTableIndex] = row;
                currentValue = board.getCurrentPlayer().getAlliance().isWhite() ?
                        min(moveTransition.getTransitionBoard(), this.searchDepth - 1, bestMove, startTime) :
                        max(moveTransition.getTransitionBoard(), this.searchDepth - 1, bestMove, startTime);
                this.freqTableIndex++;
                if (board.getCurrentPlayer().getAlliance().isWhite() &&
                        currentValue >= highestSeenValue) {
                    if (currentValue == highestSeenValue && Math.random() < 0.5) continue; //50% chance to choose equal moves
                    highestSeenValue = currentValue;
                    bestMove = move;
                } else if (board.getCurrentPlayer().getAlliance().isBlack() &&
                        currentValue <= lowestSeenValue) {
                    if (currentValue == lowestSeenValue && Math.random() < 0.5) continue;
                    lowestSeenValue = currentValue;
                    bestMove = move;
                }
            }

            if (bestMove != NULL_MOVE && (System.currentTimeMillis() - startTime) >= thinkTime) {
                break;
            }
        }
        return bestMove;
    }

    private int min(final Board board,
                    final int depth,
                    final Move bestMove,
                    final long startTime) {
        if(depth == 0) {
            this.boardsEvaluated++;
            this.freqTable[this.freqTableIndex].increment();
            return this.evaluator.evaluate(board, depth);
        }
        if(isEndGameScenario(board)) {
            return this.evaluator.evaluate(board, depth);
        }
        int lowestSeenValue = Integer.MAX_VALUE;
        for (final Move move : board.getCurrentPlayer().getLegalMoves()) {
            final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {
                final int currentValue = max(moveTransition.getTransitionBoard(), depth - 1, bestMove, startTime);
                if (currentValue <= lowestSeenValue) {
                    lowestSeenValue = currentValue;
                }
            }
            if (bestMove != NULL_MOVE && (System.currentTimeMillis() - startTime) >= startTime) {
                break;
            }
        }
        return lowestSeenValue;
    }

    private int max(final Board board,
                    final int depth,
                    final Move bestMove,
                    final long startTime) {
        if(depth == 0) {
            this.boardsEvaluated++;
            this.freqTable[this.freqTableIndex].increment();
            return this.evaluator.evaluate(board, depth);
        }
        if(isEndGameScenario(board)) {
            return this.evaluator.evaluate(board, depth);
        }
        int highestSeenValue = Integer.MIN_VALUE;
        for (final Move move : board.getCurrentPlayer().getLegalMoves()) {
            final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {
                final int currentValue = min(moveTransition.getTransitionBoard(), depth - 1, bestMove, startTime);
                if (currentValue >= highestSeenValue) {
                    highestSeenValue = currentValue;
                }
            }
            if (bestMove != NULL_MOVE && (System.currentTimeMillis() - startTime) >= startTime) {
                break;
            }
        }
        return highestSeenValue;
    }

    private static boolean isEndGameScenario(final Board board) {
        return board.getCurrentPlayer().isInCheckMate() || board.getCurrentPlayer().isInStaleMate();
    }

    private static class FreqTableRow {

        private final Move move;
        private final AtomicLong count;

        FreqTableRow(final Move move) {
            this.count = new AtomicLong();
            this.move = move;
        }

        void increment() {
            this.count.incrementAndGet();
        }

        @Override
        public String toString() {
            return BoardUtils.getPositionAtCoordinate(this.move.getCurrentCoordinate()) +
                    BoardUtils.getPositionAtCoordinate(this.move.getDestinationCoordinate()) + " : " +this.count;
        }
    }
}
