package chess.player.ai;

import chess.board.Board;
import chess.board.Move;
import chess.pieces.Piece;
import chess.player.Player;
import chess.player.ai.KingSafetyAnalyzer.KingDistance;

import static chess.pieces.Piece.PieceType.BISHOP;

public class StandardBoardEvaluator implements BoardEvaluator {
    private final static int CHECK_MATE_BONUS = 10000;
    private final static int CHECK_BONUS = 45;
    private final static int CASTLE_BONUS = 25;
    private final static int MOBILITY_MULTIPLIER = 5;
    private final static int ATTACK_MULTIPLIER = 1;
    private final static int TWO_BISHOPS_BONUS = 25;

    @Override
    public int evaluate(final Board board) {
        return score(board.getWhitePlayer()) - score(board.getBlackPlayer());
    }

    public String evaluationDetails(final Board board, final int depth) {
        return
                ("White Mobility : " + mobility(board.getWhitePlayer()) + "\n") +
                        "White kingThreats : " + kingThreats(board.getWhitePlayer()) + "\n" +
                        "White attacks : " + attacks(board.getWhitePlayer()) + "\n" +
                        "White castle : " + castle(board.getWhitePlayer()) + "\n" +
                        "White pieceEval : " + pieceEvaluations(board.getWhitePlayer()) + "\n" +
                        "White pawnStructure : " + pawnStructure(board.getWhitePlayer()) + "\n" +
                        "---------------------\n" +
                        "Black Mobility : " + mobility(board.getBlackPlayer()) + "\n" +
                        "Black kingThreats : " + kingThreats(board.getBlackPlayer()) + "\n" +
                        "Black attacks : " + attacks(board.getBlackPlayer()) + "\n" +
                        "Black castle : " + castle(board.getBlackPlayer()) + "\n" +
                        "Black pieceEval : " + pieceEvaluations(board.getBlackPlayer()) + "\n" +
                        "Black pawnStructure : " + pawnStructure(board.getBlackPlayer()) + "\n\n" +
                        "Final Score = " + evaluate(board);
    }

    private static int score(final Player player) {
        return mobility(player) +
                kingThreats(player) +
                attacks(player) +
                castle(player) +
                pieceEvaluations(player) +
                pawnStructure(player) +
                kingSafety(player);
    }

    private static int attacks(final Player player) {
        int attackScore = 0;
        for(final Move move : player.getLegalMoves()) {
            if(move.isAttack()) {
                final Piece movedPiece = move.getMovedPiece();
                final Piece attackedPiece = move.getAttackedPiece();
                if(movedPiece.getPieceType().getPieceValue() <= attackedPiece.getPieceType().getPieceValue()) {
                    attackScore++;
                }
            }
        }
        return attackScore * ATTACK_MULTIPLIER;
    }

    private static int pieceEvaluations(final Player player) {
        int pieceValuationScore = 0;
        int numBishops = 0;
        for (final Piece piece : player.getActivePieces()) {
            pieceValuationScore += piece.getPieceType().getPieceValue() + piece.locationBonus();
            if(piece.getPieceType() == BISHOP) {
                numBishops++;
            }
        }
        return pieceValuationScore + (numBishops == 2 ? TWO_BISHOPS_BONUS : 0);
    }

    private static int mobility(final Player player) {
        return MOBILITY_MULTIPLIER * mobilityRatio(player);
    }

    private static int mobilityRatio(final Player player) {
        return (int)((player.getLegalMoves().size() * 10.0f) / player.getOpponent().getLegalMoves().size());
    }

    private static int kingThreats(final Player player) {
        return player.getOpponent().isInCheckMate() ? CHECK_MATE_BONUS : check(player);
    }

    private static int check(final Player player) {
        return player.getOpponent().isInCheck() ? CHECK_BONUS : 0;
    }

    private static int depthBonus(final int depth) {
        return depth == 0 ? 1 : 100 * depth;
    }

    private static int castle(final Player player) {
        return player.isCastled() ? CASTLE_BONUS : 0;
    }

    private static int pawnStructure(final Player player) {
        return PawnStructureAnalyzer.get().pawnStructureScore(player);
    }

    private static int kingSafety(final Player player) {
        final KingDistance kingDistance = KingSafetyAnalyzer.get().calculateKingTropism(player);
        return ((kingDistance.getEnemyPiece().getPieceType().getPieceValue() / 100) * kingDistance.getDistance());
    }

}
