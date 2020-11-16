package chess.player.ai.uci.engine;

import chess.board.Board;
import chess.board.BoardUtils;
import chess.board.Move;
import chess.pgn.FenUtils;
import chess.player.MoveTransition;
import chess.player.Player;
import chess.player.ai.uci.engine.enums.Query;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * CCCP will always prefer moves in this order: Checkmate, Check, Capture, and then Push
 * If there are multiple moves in a category then a random move from those is chosen
 */
public class CCCP extends UCIEngine {
    Random rand;
    public CCCP() {
        super();
        rand = new Random();
    }

    public String getBestMove(Query query) {
        List<Move> checkMates = new LinkedList<>();
        List<Move> checks = new LinkedList<>();
        List<Move> captures = new LinkedList<>();
        List<Move> pushes = new LinkedList<>();
        List<Move> allMoves = new LinkedList<>();
        Board board = query.getBoard();
        for (Move move : board.getCurrentPlayer().getLegalMoves()) {
            MoveTransition transition = board.getCurrentPlayer().makeMove(move, true);
            if (transition.getMoveStatus().isDone()) {
                if (transition.getTransitionBoard().getCurrentPlayer().isInCheckMate()) {
                    checkMates.add(move);
                }
                else if (transition.getTransitionBoard().getCurrentPlayer().isInCheck()) {
                    checks.add(move);
                }
                else if (move instanceof Move.MajorAttackMove || move instanceof Move.AttackMove || move instanceof Move.PawnAttackMove || move instanceof Move.PawnEnPassantAttackMove) {
                    captures.add(move);
                }
                else {
                    allMoves.add(move);
                    int currentCoordinate = move.getCurrentCoordinate();
                    if (transition.getTransitionBoard().getCurrentPlayer().getOpponent().getAlliance().isWhite()) {
                        int countToNextRow = currentCoordinate % 8 + 1;
                        if (move.getDestinationCoordinate() <= (currentCoordinate - countToNextRow)) {
                            pushes.add(move);
                        }
                    }
                    else {
                        int countToNextRow = 8 - (currentCoordinate % 8);
                        if (move.getDestinationCoordinate() >= (currentCoordinate + countToNextRow)) {
                            pushes.add(move);
                        }
                    }
                }
            }
            if (transition.getOriginalBoard() != null) {
                board = transition.getOriginalBoard();
            }
        }

        Move selection;
        // Checkmate, Check, Capture, Push
        if (!checkMates.isEmpty()) {
            selection = checkMates.get(rand.nextInt(checkMates.size()));
        }
        else if (!checks.isEmpty()) {
            selection = checks.get(rand.nextInt(checks.size()));
        }
        else if (!captures.isEmpty()) {
            selection = captures.get(rand.nextInt(captures.size()));
        }
        else if (!pushes.isEmpty()) {
            selection = pushes.get(rand.nextInt(pushes.size()));
        }
        else { //In the case we have no moves that push a piece forward then choose random from all moves
            selection = allMoves.get(rand.nextInt(allMoves.size()));
        }

        String moveNotation = BoardUtils.getPositionAtCoordinate(selection.getCurrentCoordinate()) + BoardUtils.getPositionAtCoordinate(selection.getDestinationCoordinate());
        if (selection instanceof Move.PawnPromotion) {
            String[] promotionTypes = new String[]{"q", "r", "n", "b"};
            moveNotation = moveNotation + promotionTypes[rand.nextInt(promotionTypes.length)];
        }
        return moveNotation;
    }

    public void close() throws IOException {
        super.close();
    }
}
