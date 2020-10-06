package chess.player.ai;

import chess.board.Board;
import chess.board.BoardUtils;
import chess.board.Move;
import chess.player.MoveTransition;
import com.google.common.collect.ComparisonChain;
import com.google.common.primitives.Ints;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.*;

import static chess.board.Move.NULL_MOVE;

/**
 * Minimax Algorithm with Iterative Deepening
 */
public class IterativeDeepening implements MoveStrategy {

    private final BoardEvaluator evaluator;
    private final int searchDepth;
    private long boardsEvaluated;
    private long executionTime;
    private int cutOffsProduced;

    private MessageChannel messageChannel;
    public int MAXIMUM_TIME = 10000; //millis

    private enum MoveSorter {

        SMART_SORT {
            @Override
            Collection<Move> sort(final Collection<Move> moves) {
                return com.google.common.collect.Ordering.from((Comparator<Move>) (move1, move2) -> ComparisonChain.start()
                        .compareTrueFirst(BoardUtils.isThreatenedBoardImmediate(move1.getBoard()), BoardUtils.isThreatenedBoardImmediate(move2.getBoard()))
                        .compareTrueFirst(move1.isAttack(), move2.isAttack())
                        .compare(move2.getMovedPiece().getPieceType().getPieceValue(), move1.getMovedPiece().getPieceType().getPieceValue())
                        .result()).immutableSortedCopy(moves);
            }
        },
        STANDARD {
            @Override
            Collection<Move> sort(final Collection<Move> moves) {
                return com.google.common.collect.Ordering.from((Comparator<Move>) (move1, move2) -> ComparisonChain.start()
                        .compareTrueFirst(move1.isCastlingMove(), move2.isCastlingMove())
                        .compare(BoardUtils.mvvlva(move2), BoardUtils.mvvlva(move1))
                        .result()).immutableSortedCopy(moves);
            }
        },
        EXPENSIVE {
            @Override
            Collection<Move> sort(final Collection<Move> moves) {
                return com.google.common.collect.Ordering.from((Comparator<Move>) (move1, move2) -> ComparisonChain.start()
                        .compareTrueFirst(BoardUtils.kingThreat(move1), BoardUtils.kingThreat(move2))
                        .compareTrueFirst(move1.isCastlingMove(), move2.isCastlingMove())
                        .compare(BoardUtils.mvvlva(move2), BoardUtils.mvvlva(move1))
                        .result()).immutableSortedCopy(moves);
            }
        };

        abstract  Collection<Move> sort(Collection<Move> moves);
    }

    public IterativeDeepening(final int searchDepth, MessageChannel messageChannel) {
        this.evaluator = new StandardBoardEvaluator();
        this.searchDepth = searchDepth;
        this.messageChannel = messageChannel;
        this.boardsEvaluated = 0;
        this.cutOffsProduced = 0;
    }

    @Override
    public String toString() {
        return "ID";
    }

    @Override
    public long getNumBoardsEvaluated() {
        return this.boardsEvaluated;
    }

    @Override
    public Move execute(final Board board) {

        final long startTime = System.currentTimeMillis();
//        System.out.println(board.getCurrentPlayer() + " THINKING with depth = " + this.searchDepth);

        MoveOrderingBuilder builder = new MoveOrderingBuilder();
        builder.setOrder(board.getCurrentPlayer().getAlliance().isWhite() ? Ordering.DESC : Ordering.ASC);
        for(final Move move : board.getCurrentPlayer().getLegalMoves()) {
            builder.addMoveOrderingRecord(move, 0);
        }

        Move bestMove = NULL_MOVE;
        int currentDepth = 1;

        int highestSeenValue = Integer.MIN_VALUE;
        int lowestSeenValue = Integer.MAX_VALUE;

        while (currentDepth <= this.searchDepth) {
            if (System.currentTimeMillis() - startTime > MAXIMUM_TIME && bestMove != NULL_MOVE) {
                break;
            }
//            final long subTimeStart = System.currentTimeMillis();
            //int highestSeenValue = Integer.MIN_VALUE;
            //int lowestSeenValue = Integer.MAX_VALUE;
            int currentValue;
//            final List<MoveScoreRecord> records = builder.build();
//            builder = new MoveOrderingBuilder();
//            builder.setOrder(board.getCurrentPlayer().getAlliance().isWhite() ? Ordering.DESC : Ordering.ASC);
//            for (final MoveScoreRecord record : records) {
//                final Move move = record.getMove();
            for (final Move move : MoveSorter.EXPENSIVE.sort(board.getCurrentPlayer().getLegalMoves())) {
                final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
                if (moveTransition.getMoveStatus().isDone()) {
                    currentValue = board.getCurrentPlayer().getAlliance().isWhite() ?
                            min(moveTransition.getTransitionBoard(), currentDepth - 1, highestSeenValue, lowestSeenValue) :
                            max(moveTransition.getTransitionBoard(), currentDepth - 1, highestSeenValue, lowestSeenValue);
                    builder.addMoveOrderingRecord(move, currentValue);
                    if (board.getCurrentPlayer().getAlliance().isWhite() && currentValue > highestSeenValue) {
                        highestSeenValue = currentValue;
                        bestMove = move;
                         messageChannel.sendTyping().queue();
                    } else if (board.getCurrentPlayer().getAlliance().isBlack() && currentValue < lowestSeenValue) {
                        lowestSeenValue = currentValue;
                        bestMove = move;
                        messageChannel.sendTyping().queue();
                    }
                }

                if (System.currentTimeMillis() - startTime > MAXIMUM_TIME && bestMove != Move.NULL_MOVE) {
                    break;
                }
            }
//            final long subTime = System.currentTimeMillis()- subTimeStart;
//            System.out.println("\t" +toString()+ " bestMove = " +bestMove+ " Depth = " +currentDepth+ " took " +(subTime) + " ms, ordered moves : " +records);
            currentDepth++;
        }
        this.executionTime = System.currentTimeMillis() - startTime;
//        System.out.printf("%s SELECTS %s [#boards evaluated = %d, time taken = %d ms, eval rate = %.1f cutoffCount = %d prune percent = %.2f\n", board.getCurrentPlayer(),
//                bestMove, this.boardsEvaluated, this.executionTime, (1000 * ((double)this.boardsEvaluated/this.executionTime)), this.cutOffsProduced, 100 * ((double)this.cutOffsProduced/this.boardsEvaluated));
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
        for (final Move move : MoveSorter.STANDARD.sort((board.getCurrentPlayer().getLegalMoves()))) {
            final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {
                currentHighest = Math.max(currentHighest, min(moveTransition.getTransitionBoard(),
                        depth - 1, currentHighest, lowest));
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
        for (final Move move : MoveSorter.STANDARD.sort((board.getCurrentPlayer().getLegalMoves()))) {
            final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {
                currentLowest = Math.min(currentLowest, max(moveTransition.getTransitionBoard(),
                        depth - 1, highest, currentLowest));
                if (currentLowest <= highest) {
                    this.cutOffsProduced++;
                    break;
                }
            }
        }
        return currentLowest;
    }

    private static class MoveScoreRecord implements Comparable<MoveScoreRecord> {
        final Move move;
        final int score;

        MoveScoreRecord(final Move move, final int score) {
            this.move = move;
            this.score = score;
        }

        Move getMove() {
            return this.move;
        }

        int getScore() {
            return this.score;
        }

        @Override
        public int compareTo(MoveScoreRecord o) {
            return Integer.compare(this.score, o.score);
        }

        @Override
        public String toString() {
            return this.move + " : " +this.score;
        }
    }

    enum Ordering {
        ASC {
            @Override
            List<MoveScoreRecord> order(final List<MoveScoreRecord> moveScoreRecords) {
                Collections.sort(moveScoreRecords, new Comparator<MoveScoreRecord>() {
                    @Override
                    public int compare(final MoveScoreRecord o1,
                                       final MoveScoreRecord o2) {
                        return Ints.compare(o1.getScore(), o2.getScore());
                    }
                });
                return moveScoreRecords;
            }
        },
        DESC {
            @Override
            List<MoveScoreRecord> order(final List<MoveScoreRecord> moveScoreRecords) {
                Collections.sort(moveScoreRecords, (o1, o2) -> Ints.compare(o2.getScore(), o1.getScore()));
                return moveScoreRecords;
            }
        };

        abstract List<MoveScoreRecord> order(final List<MoveScoreRecord> moveScoreRecords);
    }


    private static class MoveOrderingBuilder {
        List<MoveScoreRecord> moveScoreRecords;
        Ordering ordering;

        MoveOrderingBuilder() {
            this.moveScoreRecords = new ArrayList<>();
        }

        void addMoveOrderingRecord(final Move move,
                                   final int score) {
            this.moveScoreRecords.add(new MoveScoreRecord(move, score));
        }

        void setOrder(final Ordering order) {
            this.ordering = order;
        }

        List<MoveScoreRecord> build() {
            return this.ordering.order(moveScoreRecords);
        }
    }


}