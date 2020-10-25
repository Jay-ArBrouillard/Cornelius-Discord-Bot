package chess.player;

import chess.Alliance;
import chess.board.Board;
import chess.board.Move;
import chess.board.Move.*;
import chess.board.Tile;
import chess.pieces.King;
import chess.pieces.Piece;
import chess.pieces.Rook;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Player {
    protected final Board board;
    protected final King playerKing;
    protected final List<Move> legalMoves;
    protected final boolean isInCheck;

    protected Player(final Board board, final List<Move> legalMoves, final List<Move> opponentMoves) {
        this.board = board;
        this.playerKing = establishKing();
        this.legalMoves = Collections.unmodifiableList(Stream.concat(legalMoves.stream(),
                            calculateKingCastles(legalMoves, opponentMoves).stream()).collect(Collectors.toList()));
        this.isInCheck = !Player.calculateAttacksOnTile(this.playerKing.getPiecePosition(), opponentMoves).isEmpty();
    }

    protected static Collection<Move> calculateAttacksOnTile(int piecePosition, Collection<Move> opponentMoves) {
        final List<Move> attackMoves = new ArrayList<>();
        for (final Move move : opponentMoves) {
            if (piecePosition == move.getDestinationCoordinate()) {
                attackMoves.add(move);
            }
        }

        return Collections.unmodifiableList(attackMoves);
    }

    protected King establishKing() {
        for (final Piece piece : getActivePieces()) {
            if  (piece.getPieceType().isKing()) {
                return (King) piece;
            }
        }

        throw new RuntimeException("Invalid board! King does not exist");
    }

    public King getPlayerKing() {
        return this.playerKing;
    }

    public boolean isCastled() {
        return this.playerKing.isCastled();
    }


    public boolean isKingSideCastleCapable() {
        return this.playerKing.isKingSideCastleCapable();
    }

    public void setKingSideCastleCapable(boolean kingSideCastleCapable) {
        this.playerKing.setKingSideCastleCapable(kingSideCastleCapable);
    }

    public boolean isQueenSideCastleCapable() {
        return this.playerKing.isQueenSideCastleCapable();
    }

    public void setQueenSideCastleCapable(boolean queenSideCastleCapable) {
        this.playerKing.setQueenSideCastleCapable(queenSideCastleCapable);
    }

    public List<Move> getLegalMoves() {
        return this.legalMoves;
    }

    public boolean isMoveLegal(final Move move) {
        return this.legalMoves.contains(move);
    }

    public boolean isInCheck() {
        return this.isInCheck;
    }

    public boolean isInCheckMate() {
        return this.isInCheck && !hasEscapeMoves();
    }

    public boolean isInStaleMate() {
        return !this.isInCheck && !hasEscapeMoves();
    }

    public boolean hasEscapeMoves() {
        for (final Move move : this.legalMoves) {
            final MoveTransition transition = makeMove(move, true);
            if (transition.getMoveStatus().isDone()) {
                return true;
            }
        }

        return false;
    }

    public MoveTransition makeMove(final Move move) {
        return makeMove(move, false);
    }

    public MoveTransition makeMove(final Move move, final boolean isDummyMove) {
        if (!isMoveLegal(move)) {
            return new MoveTransition(this.board, move, MoveStatus.ILLEGAL_MOVE);
        }
        //Move is legal
        final Board transitionBoard = move.execute();
        final Collection<Move> kingAttacks = Player.calculateAttacksOnTile(transitionBoard.getCurrentPlayer().getOpponent().getPlayerKing().getPiecePosition(),
                transitionBoard.getCurrentPlayer().getLegalMoves());
        //The move will still leave the king in check
        if (!kingAttacks.isEmpty()) {
            return new MoveTransition(this.board, move, MoveStatus.LEAVES_PLAYER_CHECK);
        }
        if (!isDummyMove) {
            //Update is castle capable in the case that Rook is moved
            this.setKingSideCastleCapable(false); //Default these to false unless found
            this.setQueenSideCastleCapable(false);
            Collection<Move> castleMoveAvailable = calculateKingCastles(transitionBoard.getCurrentPlayer().getOpponent().getLegalMoves(),
                                                                        transitionBoard.getCurrentPlayer().getLegalMoves());
            for (Move m : castleMoveAvailable) {
                if (m.toString().contains("o-o")) { //This is hard coded in KingSideCastleMove class
                    this.setKingSideCastleCapable(true);
                }
                if (m.toString().contains("o-o-o")) { //This is hard coded in QueenSideCastleMove class
                    this.setQueenSideCastleCapable(true);
                }
            }

            //Add to moves played
            if (move instanceof PawnEnPassantAttackMove ||
                    move instanceof PawnMove ||
                    move instanceof PawnJump ||
                    move instanceof MajorAttackMove ||
                    move instanceof AttackMove ||
                    move instanceof PawnAttackMove)   {
                transitionBoard.getMovesPlayed().add(true);
            }
            else {
                transitionBoard.getMovesPlayed().add(false);
            }
        }

        return new MoveTransition(transitionBoard, move, MoveStatus.DONE);
    }

    protected boolean hasCastleOpportunities() {
        return !this.isInCheck && !this.playerKing.isCastled() &&
                (this.playerKing.isKingSideCastleCapable() || this.playerKing.isQueenSideCastleCapable());
    }

    public abstract Alliance getAlliance();
    public abstract Collection<Piece> getActivePieces();
    public abstract Player getOpponent();
    protected abstract Collection<Move> calculateKingCastles(Collection<Move> playerLegalMoves, Collection<Move> opponentLegalMoves);
}
