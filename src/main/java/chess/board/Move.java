package chess.board;

import chess.board.Board.Builder;
import chess.pieces.Pawn;
import chess.pieces.Piece;
import chess.pieces.Piece.PieceType;
import chess.pieces.Queen;
import chess.pieces.Rook;

public abstract class Move {

    protected final Board board;
    protected final Piece movedPiece;
    protected final int destinationCoordinate;
    protected final boolean isFirstMove;
    protected PieceType promotionType = PieceType.QUEEN; //Default to queen

    public static final Move NULL_MOVE = new NullMove();

    public Move(final Board board, final Piece movedPiece, int destinationCoordinate) {
        this.board = board;
        this.movedPiece = movedPiece;
        this.destinationCoordinate = destinationCoordinate;
        this.isFirstMove = movedPiece.isFirstMove();;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + this.destinationCoordinate;
        result = 31 * result + this.movedPiece.hashCode();
        result = 31 * result + this.movedPiece.getPiecePosition();
        result = result + (this.isFirstMove ? 1 : 0);
        return result;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Move)) {
            return false;
        }
        final Move otherMove = (Move) other;
        return getDestinationCoordinate() == otherMove.getDestinationCoordinate() &&
                getMovedPiece().equals(otherMove.getMovedPiece());
    }

    public void setPromotionType(PieceType promotionType) {
        this.promotionType = promotionType;
    }

    public int getDestinationCoordinate() {
        return this.destinationCoordinate;
    }

    public Piece getMovedPiece() {
        return this.movedPiece;
    }

    public Board getBoard() {
        return this.board;
    }

    public int getCurrentCoordinate() {
        return this.getMovedPiece().getPiecePosition();
    }

    public boolean isAttack() {
        return false;
    }

    public boolean isCastlingMove() {
        return false;
    }

    public Piece getAttackedPiece() {
        return null;
    }

    public boolean isForwardMove() {
        if (this.board.getCurrentPlayer().getAlliance().isWhite()) {
            int value = BoardUtils.getWhiteForwardMoveCoordinate(getCurrentCoordinate());
            if (value != -1 && value <= getDestinationCoordinate()) {
                return true;
            }
        }
        else {
            int value = BoardUtils.getBlackForwardMoveCoordinate(getCurrentCoordinate());
            if (value != -1 && value >= getDestinationCoordinate()) {
                return true;
            }
        }
        return false;
    }

    /*
     * Execute method for Normal Move
     */
    public Board execute() {
        final Builder builder = new Builder();

        for (final Piece piece : this.board.getCurrentPlayer().getActivePieces()) {
            if (!this.movedPiece.equals(piece)) {
                builder.setPiece(piece);
            }
        }
        for (final Piece piece : this.board.getCurrentPlayer().getOpponent().getActivePieces()) {
            builder.setPiece(piece);
        }
        builder.setPiece(this.movedPiece.movePiece(this));
        builder.setMoveMaker(this.board.getCurrentPlayer().getOpponent().getAlliance());
        int numFullMoves = this.board.getCurrentPlayer().getAlliance().isBlack() ? this.board.getNumFullMoves() + 1 : this.board.getNumFullMoves();
        return builder.build(this.board.getMovesPlayed(), numFullMoves); //Return a new board
    }

    /*
     * Non-Pawn Move that captures a piece
     */
    public static class MajorAttackMove extends AttackMove {

        public MajorAttackMove(final Board board, final Piece movedPiece, final int destinationCoordinate, final Piece attackedPiece) {
            super(board, movedPiece, destinationCoordinate, attackedPiece);
        }

        @Override
        public boolean equals(final Object o) {
            return this == o || o instanceof MajorAttackMove && super.equals(o);
        }

        @Override
        public String toString() {
            return this.movedPiece.getPieceType().getFullPieceName() + " Major Attack `" + BoardUtils.getPositionAtCoordinate(this.getCurrentCoordinate()) + BoardUtils.getPositionAtCoordinate(this.destinationCoordinate) + "`";
        }
    }

    /*
     * Non-Pawn Move that moves to an empty space
     */
    public static final class MajorMove extends Move {

        public MajorMove(final Board board, final Piece movedPiece, final int destinationCoordinate) {
            super(board, movedPiece, destinationCoordinate);
        }

        @Override
        public boolean equals(final Object other) {
            return this == other || other instanceof MajorMove && super.equals(other);
        }


        @Override
        public String toString() {
            return this.movedPiece.getPieceType().getFullPieceName() + " Major Move `" + BoardUtils.getPositionAtCoordinate(this.getCurrentCoordinate()) + BoardUtils.getPositionAtCoordinate(this.destinationCoordinate) + "`";
        }
    }

    /*
     * Move that captures a piece
     */
    public static class AttackMove extends Move {

        final Piece attackedPiece;

        public AttackMove(final Board board, final Piece movedPiece, final int destinationCoordinate, final Piece attackedPiece) {
            super(board, movedPiece, destinationCoordinate);
            this.attackedPiece = attackedPiece;
        }

        @Override
        public int hashCode() {
            return this.attackedPiece.hashCode() + super.hashCode();
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof AttackMove)) {
                return false;
            }
            final AttackMove otherAttackMove = (AttackMove) other;
            return super.equals(otherAttackMove) && getAttackedPiece().equals(otherAttackMove.getAttackedPiece());
        }

        @Override
        public boolean isAttack() {
            return true;
        }

        @Override
        public Piece getAttackedPiece() {
            return this.attackedPiece;
        }
    }

    /*
     * Pawn Move 1 tile
     */
    public static final class PawnMove extends Move {

        public PawnMove(final Board board, final Piece movedPiece, final int destinationCoordinate) {
            super(board, movedPiece, destinationCoordinate);
        }

        @Override
        public boolean equals(final Object o) {
            return this == o || o instanceof PawnMove && super.equals(o);
        }

        @Override
        public String toString() {
            return "Pawn Move `" + BoardUtils.getPositionAtCoordinate(this.getCurrentCoordinate()) + BoardUtils.getPositionAtCoordinate(this.destinationCoordinate) + "`";
        }
    }

    /*
     * Pawn Move 2 tiles on first move
     */
    public static final class PawnJump extends Move {

        public PawnJump(final Board board, final Piece movedPiece, final int destinationCoordinate) {
            super(board, movedPiece, destinationCoordinate);
        }

        @Override
        public boolean equals(Object o) {
            return this == o || o instanceof PawnJump && super.equals(o);
        }

        @Override
        public Board execute() {
            final Builder builder = new Builder();
            for (final Piece piece : this.board.getCurrentPlayer().getActivePieces()) {
                if (!this.movedPiece.equals(piece)) {
                    builder.setPiece(piece);
                }
            }

            for (final Piece piece : this.board.getCurrentPlayer().getOpponent().getActivePieces()) {
                builder.setPiece(piece);
            }
            final Pawn movedPawn = (Pawn) this.movedPiece.movePiece(this);
            builder.setPiece(movedPawn);
            builder.setEnPassantPawn(movedPawn);
            builder.setMoveMaker(this.board.getCurrentPlayer().getOpponent().getAlliance());
            int numFullMoves = this.board.getCurrentPlayer().getAlliance().isBlack() ? this.board.getNumFullMoves() + 1 : this.board.getNumFullMoves();
            return builder.build(this.board.getMovesPlayed(), numFullMoves); //Return a new board
        }

        @Override
        public String toString() {
            return "Pawn Jump `" + BoardUtils.getPositionAtCoordinate(this.getCurrentCoordinate()) + BoardUtils.getPositionAtCoordinate(this.destinationCoordinate) + "`";
        }
    }

    /*
     * Pawn Captures Piece
     */
    public static class PawnAttackMove extends AttackMove {

        public PawnAttackMove(final Board board, final Piece movedPiece, final int destinationCoordinate, final Piece attackedPiece) {
            super(board, movedPiece, destinationCoordinate, attackedPiece);
        }

        @Override
        public boolean equals(Object o) {
            return this == o || o instanceof PawnAttackMove && super.equals(o);
        }

        @Override
        public String toString() {
            return "Pawn Attack `" + BoardUtils.getPositionAtCoordinate(this.getCurrentCoordinate()) + BoardUtils.getPositionAtCoordinate(this.destinationCoordinate) + "`";
        }
    }

    /*
     * Pawn Captures Piece in passing
     */
    public static final class PawnEnPassantAttackMove extends PawnAttackMove {

        public PawnEnPassantAttackMove(final Board board, final Piece movedPiece, final int destinationCoordinate, final Piece attackedPiece) {
            super(board, movedPiece, destinationCoordinate, attackedPiece);
        }

        @Override
        public boolean equals(final Object o) {
            return this == o || o instanceof PawnEnPassantAttackMove && super.equals(o);
        }

        @Override
        public Board execute() {
            final Builder builder = new Builder();
            for(final Piece piece : this.board.getCurrentPlayer().getActivePieces()) {
                if (!this.movedPiece.equals(piece)) {
                    builder.setPiece(piece);
                }
            }

            for(final Piece piece : this.board.getCurrentPlayer().getOpponent().getActivePieces()) {
                if (!piece.equals(this.getAttackedPiece())) {
                    builder.setPiece(piece);
                }
            }
            builder.setPiece(this.movedPiece.movePiece(this));
            builder.setMoveMaker(this.board.getCurrentPlayer().getOpponent().getAlliance());
            int numFullMoves = this.board.getCurrentPlayer().getAlliance().isBlack() ? this.board.getNumFullMoves() + 1 : this.board.getNumFullMoves();
            return builder.build(this.board.getMovesPlayed(), numFullMoves); //Return a new board
        }

        @Override
        public String toString() {
            return "Pawn EnPassant `" + BoardUtils.getPositionAtCoordinate(this.getCurrentCoordinate()) + BoardUtils.getPositionAtCoordinate(this.destinationCoordinate) + "`";
        }
    }

    public static class PawnPromotion extends Move {

        final Move decoratedMove;
        final Pawn promotedPawn;
        final Piece promotionPiece;

        public PawnPromotion(final Move decoratedMove,
                             final Piece promotionPiece) {
            super(decoratedMove.getBoard(), decoratedMove.getMovedPiece(), decoratedMove.getDestinationCoordinate());
            this.decoratedMove = decoratedMove;
            this.promotedPawn = (Pawn) decoratedMove.getMovedPiece();
            this.promotionPiece = promotionPiece;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + this.decoratedMove.hashCode();
            result = prime * result + this.destinationCoordinate;
            return result;
        }

        @Override
        public boolean equals(Object o) {
            return this == o || o instanceof PawnPromotion && super.equals(o);
        }

        @Override
        public Board execute() {
            final Board pawnMovedBoard = this.decoratedMove.execute();
            final Builder builder = new Builder();
            for (final Piece piece : pawnMovedBoard.getCurrentPlayer().getActivePieces()) {
                if (!this.promotedPawn.equals(piece)) {
                    builder.setPiece(piece);
                }
            }
            for (final Piece piece : pawnMovedBoard.getCurrentPlayer().getOpponent().getActivePieces()) {
                builder.setPiece(piece);
            }
            builder.setPiece(this.promotedPawn.getPromotionPiece(promotionType).movePiece(this));
            builder.setMoveMaker(this.board.getCurrentPlayer().getOpponent().getAlliance());
            int numFullMoves = this.board.getCurrentPlayer().getAlliance().isBlack() ? this.board.getNumFullMoves() + 1 : this.board.getNumFullMoves();
            return builder.build(this.board.getMovesPlayed(), numFullMoves); //Return a new board
        }

        @Override
        public boolean isAttack() {
            return this.decoratedMove.isAttack();
        }

        @Override
        public Piece getAttackedPiece() {
            return this.decoratedMove.getAttackedPiece();
        }

        @Override
        public String toString() {
            return "Pawn Promotion `" + BoardUtils.getPositionAtCoordinate(this.getCurrentCoordinate()) + BoardUtils.getPositionAtCoordinate(this.destinationCoordinate) + "`";
        }
    }

    static abstract class CastleMove extends Move {

        protected final Rook castleRook;
        protected final int castleRookStart;
        protected final int castleRookDestination;

        public CastleMove(final Board board, final Piece movedPiece, final int destinationCoordinate,
                          final Rook castleRook, final int castleRookStart, final int castleRookDestination) {
            super(board, movedPiece, destinationCoordinate);
            this.castleRook = castleRook;
            this.castleRookStart = castleRookStart;
            this.castleRookDestination = castleRookDestination;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + this.castleRook.hashCode();
            result = prime * result + this.castleRookDestination;
            return result;
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof CastleMove)) {
                return false;
            }
            final CastleMove otherCastleMove = (CastleMove) other;
            return super.equals(otherCastleMove) && this.castleRook.equals(otherCastleMove.getCastleRook());
        }

        public Rook getCastleRook() {
            return this.castleRook;
        }

        @Override
        public boolean isCastlingMove() {
            return true;
        }

        @Override
        public Board execute() {
            final Builder builder = new Builder();
            for (final Piece piece : this.board.getCurrentPlayer().getActivePieces()) {
                if (!this.movedPiece.equals(piece) && !this.castleRook.equals(piece)) {
                    builder.setPiece(piece);
                }
            }

            for (final Piece piece : this.board.getCurrentPlayer().getOpponent().getActivePieces()) {
                builder.setPiece(piece);
            }
            builder.setPiece(this.movedPiece.movePiece(this));
            builder.setPiece(new Rook(this.castleRookDestination, this.castleRook.getPieceAlliance(), false));
            builder.setMoveMaker(this.board.getCurrentPlayer().getOpponent().getAlliance());
            int numFullMoves = this.board.getCurrentPlayer().getAlliance().isBlack() ? this.board.getNumFullMoves() + 1 : this.board.getNumFullMoves();
            return builder.build(this.board.getMovesPlayed(), numFullMoves); //Return a new board
        }

        @Override
        public String toString() {
            return "CastleMove";
        }

    }

    public static final class KingSideCastleMove extends CastleMove {

        public KingSideCastleMove(final Board board, final Piece movedPiece, final int destinationCoordinate,
                                  final Rook castleRook, final int castleRookStart, final int castleRookDestination) {
            super(board, movedPiece, destinationCoordinate, castleRook, castleRookStart, castleRookDestination);
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof KingSideCastleMove)) {
                return false;
            }
            final KingSideCastleMove otherKingSideCastleMove = (KingSideCastleMove) other;
            return super.equals(otherKingSideCastleMove) && this.castleRook.equals(otherKingSideCastleMove.getCastleRook());
        }

        @Override
        public String toString() {
            return "King Side Castle `o-o`";
        }
    }

    public static final class QueenSideCastleMove extends CastleMove {

        public QueenSideCastleMove(final Board board, final Piece movedPiece, final int destinationCoordinate,
                                   final Rook castleRook, final int castleRookStart, final int castleRookDestination) {
            super(board, movedPiece, destinationCoordinate, castleRook, castleRookStart, castleRookDestination);
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof QueenSideCastleMove)) {
                return false;
            }
            final QueenSideCastleMove otherQueenSideCastleMove = (QueenSideCastleMove) other;
            return super.equals(otherQueenSideCastleMove) && this.castleRook.equals(otherQueenSideCastleMove.getCastleRook());
        }

        @Override
        public String toString() {
            return "Queen Side Castle `o-o-o`";
        }
    }

    /*
     * Invalid move
     */
    public static final class NullMove extends Move {

        public NullMove() {
            super(null,null, -1);
        }

        @Override
        public Board execute() {
            throw new RuntimeException("Can not execute a Null Move");
        }
    }

    public static class MoveFactory {

        private MoveFactory() {
            throw new RuntimeException("Do not instantiate Move Factory");
        }

        public static Move createMove(final Board board, final int currentCoordinate, final int destinationCoordinate) {
            for (final Move move : board.getCurrentPlayer().getLegalMoves()) {
                if(move.getCurrentCoordinate() == currentCoordinate && move.getDestinationCoordinate() == destinationCoordinate) {
                    return move;
                }
            }

            return NULL_MOVE;
        }
    }
}