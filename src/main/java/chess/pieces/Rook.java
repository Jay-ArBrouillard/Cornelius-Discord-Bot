package chess.pieces;

import chess.Alliance;
import chess.board.Board;
import chess.board.BoardUtils;
import chess.board.Move;
import chess.board.Move.AttackMove;
import chess.board.Move.MajorAttackMove;
import chess.board.Move.MajorMove;
import chess.board.Tile;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Rook extends Piece {
    private final static int[] CANDIDATE_MOVE_COORDINATES = { -8, -1, 1, 8 };

    public Rook(final int piecePosition, final Alliance alliance) {
        super(PieceType.ROOK, piecePosition, alliance, true);
    }

    public Rook(final int piecePosition, final Alliance alliance, final boolean isFirstMove) {
        super(PieceType.ROOK, piecePosition, alliance, isFirstMove);
    }

    @Override
    public Collection<Move> getLegalMoves(final Board board) {
        final List<Move> legalMoves = new LinkedList<>();
        for (final int currentCandidateOffset : CANDIDATE_MOVE_COORDINATES) {
            int candidateDestinationCoordinate = this.piecePosition;
            while (BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)) {
                if (isColumnExclusion(currentCandidateOffset, candidateDestinationCoordinate)) {
                    break;
                }
                candidateDestinationCoordinate += currentCandidateOffset;
                if (BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)) {
                    final Tile candidateDestinationTile = board.getTile(candidateDestinationCoordinate);
                    final Piece pieceAtDestination = candidateDestinationTile.getPiece();
                    if (pieceAtDestination == null) {
                        legalMoves.add(new MajorMove(board, this, candidateDestinationCoordinate));
                    } else {
                        final Alliance pieceAtDestinationAllegiance = pieceAtDestination.getPieceAlliance();
                        if (this.pieceAlliance != pieceAtDestinationAllegiance) {
                            legalMoves.add(new MajorAttackMove(board, this, candidateDestinationCoordinate,
                                    pieceAtDestination));
                        }
                        break;
                    }
                }
            }
        }
        return Collections.unmodifiableList(legalMoves);
    }

    @Override
    public Rook movePiece(Move move) {
        return new Rook(move.getDestinationCoordinate(), move.getMovedPiece().getPieceAlliance());
    }

    @Override
    public String toString() {
        return this.pieceType.toString();
    }

    @Override
    public int locationBonus() {
        return this.pieceAlliance.rookBonus(this.piecePosition);
    }

    private static boolean isColumnExclusion(final int currentCandidate,
                                             final int candidateDestinationCoordinate) {
        return (BoardUtils.FIRST_COLUMN.get(candidateDestinationCoordinate) && (currentCandidate == -1)) ||
                (BoardUtils.EIGHTH_COLUMN.get(candidateDestinationCoordinate) && (currentCandidate == 1));
    }
}
