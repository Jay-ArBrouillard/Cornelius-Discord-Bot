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

public class Queen extends Piece {
    private final static int[] CANDIDATE_MOVE_COORDINATES = { -9, -8, -7, -1, 1,
            7, 8, 9 };

    public Queen(final int piecePosition, final Alliance alliance) {
        super(PieceType.QUEEN, piecePosition, alliance, true);
    }

    public Queen(final int piecePosition, final Alliance alliance, final boolean isFirstMove) {
        super(PieceType.QUEEN, piecePosition, alliance, isFirstMove);
    }

    @Override
    public Collection<Move> getLegalMoves(final Board board) {
        final List<Move> legalMoves = new LinkedList<>();
        for (final int currentCandidateOffset : CANDIDATE_MOVE_COORDINATES) {
            int candidateDestinationCoordinate = this.piecePosition;
            while (true) {
                if (isFirstColumnExclusion(currentCandidateOffset, candidateDestinationCoordinate) ||
                        isEightColumnExclusion(currentCandidateOffset, candidateDestinationCoordinate)) {
                    break;
                }
                candidateDestinationCoordinate += currentCandidateOffset;
                if (!BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)) {
                    break;
                } else {
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
    public Queen movePiece(Move move) {
        return new Queen(move.getDestinationCoordinate(), move.getMovedPiece().getPieceAlliance());
    }

    @Override
    public String toString() {
        return this.pieceType.toString();
    }

    @Override
    public int locationBonus() {
        return this.pieceAlliance.queenBonus(this.piecePosition);
    }

    private static boolean isFirstColumnExclusion(final int currentPosition,
                                                  final int candidatePosition) {
        return BoardUtils.FIRST_COLUMN.get(candidatePosition) && ((currentPosition == -9)
                || (currentPosition == -1) || (currentPosition == 7));
    }

    private static boolean isEightColumnExclusion(final int currentPosition,
                                                  final int candidatePosition) {
        return BoardUtils.EIGHTH_COLUMN.get(candidatePosition) && ((currentPosition == -7)
                || (currentPosition == 1) || (currentPosition == 9));
    }
}
