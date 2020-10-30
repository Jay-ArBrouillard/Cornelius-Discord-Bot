package chess.player.ai;

import chess.Alliance;
import chess.board.Board;
import chess.board.BoardUtils;
import chess.board.Move;
import chess.player.MoveTransition;
import chess.player.Player;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.Collection;
import java.util.Comparator;

/**
 * Minimax Algorithm with Alpha-Beta Pruning and Move Ordering
 */
public class AlphaBetaWithMoveOrdering implements MoveStrategy {

    private final BoardEvaluator evaluator;
    private final int searchDepth;
    private final int quiescenceFactor;
    private long boardsEvaluated;
    private long executionTime;
    private int quiescenceCount;
    private int cutOffsProduced;

    public int MAXIMUM_TIME = 10000; //millis
    private final MessageChannel messageChannel;

    private enum MoveSorter {

        SMART_SORT {
            @Override
            Collection<Move> sort(final Collection<Move> moves) {
                return Ordering.from((Comparator<Move>) (move1, move2) -> ComparisonChain.start()
                        .compareTrueFirst(isThreatenedBoardImmediate(move1.getBoard()), isThreatenedBoardImmediate(move2.getBoard()))
                        .compareTrueFirst(move1.isAttack(), move2.isAttack())
                        .compareTrueFirst(move1.isCastlingMove(), move2.isCastlingMove())
                        .compare(move2.getMovedPiece().getPieceType().getPieceValue(), move1.getMovedPiece().getPieceType().getPieceValue())
                        .result()).immutableSortedCopy(moves);
            }
        };

        abstract  Collection<Move> sort(Collection<Move> moves);
    }

    @Override
    public long getNumBoardsEvaluated() {
        return this.boardsEvaluated;
    }

    public AlphaBetaWithMoveOrdering(final int searchDepth,
                                     final int quiescenceFactor,
                                     MessageChannel messageChannel) {
        this.evaluator = new StandardBoardEvaluator();
        this.searchDepth = searchDepth;
        this.quiescenceFactor = quiescenceFactor;
        this.boardsEvaluated = 0;
        this.quiescenceCount = 0;
        this.cutOffsProduced = 0;
        this.messageChannel = messageChannel;
    }

    @Override
    public String toString() {
        return "AB+MO";
    }

    @Override
    public Move execute(final Board board) {
        final long startTime = System.currentTimeMillis();
        final Player currentPlayer = board.getCurrentPlayer();
        final Alliance alliance = currentPlayer.getAlliance();
        Move bestMove = Move.NULL_MOVE;
        int highestSeenValue = Integer.MIN_VALUE;
        int lowestSeenValue = Integer.MAX_VALUE;
        int currentValue;
//        int moveCounter = 1;
//        final int numMoves = this.moveSorter.sort(board.getCurrentPlayer().getLegalMoves()).size();
//        System.out.println(board.getCurrentPlayer() + " THINKING with depth = " + this.searchDepth);
//        System.out.println("\tOrdered moves! : " + this.moveSorter.sort(board.getCurrentPlayer().getLegalMoves()));
        for (final Move move : MoveSorter.SMART_SORT.sort((board.getCurrentPlayer().getLegalMoves()))) {
            final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            this.quiescenceCount = 0;
//            final String s;
            if (moveTransition.getMoveStatus().isDone()) {
//                final long candidateMoveStartTime = System.nanoTime();
                currentValue = alliance.isWhite() ?
                        min(moveTransition.getTransitionBoard(), this.searchDepth - 1, highestSeenValue, lowestSeenValue) :
                        max(moveTransition.getTransitionBoard(), this.searchDepth - 1, highestSeenValue, lowestSeenValue);
                if (alliance.isWhite() && currentValue > highestSeenValue) {
                    highestSeenValue = currentValue;
                    bestMove = move;
                    messageChannel.sendTyping().queue();
                }
                else if (alliance.isBlack() && currentValue < lowestSeenValue) {
                    lowestSeenValue = currentValue;
                    bestMove = move;
                    messageChannel.sendTyping().queue();
                }
//                final String quiescenceInfo = " [h: " +highestSeenValue+ " l: " +lowestSeenValue+ "] q: " +this.quiescenceCount;
//                s = "\t" + toString() + "(" +this.searchDepth+ "), m: (" +moveCounter+ "/" +numMoves+ ") " + move + ", best:  " + bestMove
//
//                        + quiescenceInfo + ", t: " +calculateTimeTaken(candidateMoveStartTime, System.nanoTime());
            } //else {
//                s = "\t" + toString() + ", m: (" +moveCounter+ "/" +numMoves+ ") " + move + " is illegal, best: " +bestMove;
//            }
//            System.out.println(s);
//            moveCounter++;
            if (System.currentTimeMillis() - startTime > MAXIMUM_TIME && bestMove != Move.NULL_MOVE) {
                break;
            }
        }
        this.executionTime = System.currentTimeMillis() - startTime;
        System.out.printf("%s SELECTS %s [#boards evaluated = %d, time taken = %d ms, eval rate = %.1f cutoffCount = %d prune percent = %.2f\n", board.getCurrentPlayer(),
                bestMove, this.boardsEvaluated, this.executionTime, (1000 * ((double)this.boardsEvaluated/this.executionTime)), this.cutOffsProduced, 100 * ((double)this.cutOffsProduced/this.boardsEvaluated));
        return bestMove;
    }

    public int max(final Board board,
                   final int depth,
                   final int highest,
                   final int lowest) {
        if (depth == 0 || BoardUtils.isEndGame(board)) {
            this.boardsEvaluated++;
            return this.evaluator.evaluate(board, depth);
        }
        int currentHighest = highest;
        for (final Move move : MoveSorter.SMART_SORT.sort((board.getCurrentPlayer().getLegalMoves()))) {
            final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {
                currentHighest = Math.max(currentHighest, min(moveTransition.getTransitionBoard(),
                        calculateQuiescenceDepth(board, move, depth), currentHighest, lowest));
                if (lowest <= currentHighest) {
                    this.cutOffsProduced++;
                    break;
                }
            }
        }
        return currentHighest;
    }

    public int min(final Board board,
                   final int depth,
                   final int highest,
                   final int lowest) {
        if (depth == 0 || BoardUtils.isEndGame(board)) {
            this.boardsEvaluated++;
            return this.evaluator.evaluate(board, depth);
        }
        int currentLowest = lowest;
        for (final Move move : MoveSorter.SMART_SORT.sort((board.getCurrentPlayer().getLegalMoves()))) {
            final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {
                currentLowest = Math.min(currentLowest, max(moveTransition.getTransitionBoard(),
                        calculateQuiescenceDepth(board, move, depth), highest, currentLowest));
                if (currentLowest <= highest) {
                    this.cutOffsProduced++;
                    break;
                }
            }
        }
        return currentLowest;
    }

    private int calculateQuiescenceDepth(final Board board,
                                         final Move move,
                                         final int depth) {
        return depth - 1;
    }

    public static boolean isThreatenedBoardImmediate(final Board board) {
        return board.getWhitePlayer().isInCheck() || board.getWhitePlayer().isInCheck();
    }
}
