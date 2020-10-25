package chess.pieces;

import chess.Alliance;
import chess.board.Board;
import chess.board.Move;

import java.util.Collection;
import java.util.Objects;

public abstract class Piece {
    protected final PieceType pieceType;
    protected final int piecePosition; //Think of board as 1D array
    protected final Alliance pieceAlliance;
    protected final String filePath;
    protected final boolean isFirstMove;
    private final int cachedHashCode;

    protected Piece(final PieceType pieceType, final int piecePosition, final Alliance pieceAlliance, final boolean isFirstMove) {
        this.pieceType = pieceType;
        this.piecePosition = piecePosition;
        this.pieceAlliance = pieceAlliance;
        this.filePath = generateImagePath(pieceAlliance);
        this.isFirstMove = isFirstMove;
        this.cachedHashCode = hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Piece)) {
            return false;
        }
        final Piece otherPiece = (Piece) other;
        return this.piecePosition == otherPiece.piecePosition && this.pieceType == otherPiece.pieceType &&
                this.pieceAlliance == otherPiece.pieceAlliance && this.isFirstMove == otherPiece.isFirstMove;
    }

    @Override
    public int hashCode() {
        return this.cachedHashCode;
    }

    public Alliance getPieceAlliance() {
        return pieceAlliance;
    }

    public boolean isFirstMove() {
        return isFirstMove;
    }

    public int getPiecePosition() {
        return this.piecePosition;
    }

    public PieceType getPieceType() {
        return pieceType;
    }

    public String getFilePath() {
        return filePath;
    }

    public String generateImagePath(Alliance alliance) {
        StringBuilder pathbuilder = new StringBuilder("src/main/java/chess/assets/");
        if (alliance.isBlack()) {
            pathbuilder.append("Black_");
        }
        else {
            pathbuilder.append("White_");
        }
        pathbuilder.append(this.pieceType.fullPieceName);
        pathbuilder.append(".png");
        return pathbuilder.toString();
    }

    public abstract Collection<Move> getLegalMoves (final Board board);
    public abstract Piece movePiece(Move move);
    public abstract int locationBonus();

    public enum PieceType {

        PAWN(100, "P", "Pawn") {
            @Override
            public boolean isKing() {
                return false;
            }

            @Override
            public boolean isRook() {
                return false;
            }

            @Override
            public boolean isPawn() {
                return true;
            }

            @Override
            public boolean isBishop() {
                return false;
            }

            @Override
            public boolean isKnight() {
                return false;
            }

            @Override
            public boolean isQueen() {
                return false;
            }
        },
        KNIGHT(300, "N", "Knight") {
            @Override
            public boolean isKing() {
                return false;
            }

            @Override
            public boolean isRook() {
                return false;
            }

            @Override
            public boolean isPawn() {
                return false;
            }

            @Override
            public boolean isBishop() {
                return false;
            }

            @Override
            public boolean isKnight() {
                return true;
            }

            @Override
            public boolean isQueen() {
                return false;
            }
        },
        BISHOP(330, "B", "Bishop") {
            @Override
            public boolean isKing() {
                return false;
            }

            @Override
            public boolean isRook() {
                return false;
            }

            @Override
            public boolean isPawn() {
                return false;
            }

            @Override
            public boolean isBishop() {
                return true;
            }

            @Override
            public boolean isKnight() {
                return false;
            }

            @Override
            public boolean isQueen() {
                return false;
            }
        },
        ROOK(500, "R", "Rook") {
            @Override
            public boolean isKing() {
                return false;
            }

            @Override
            public boolean isRook() {
                return true;
            }

            @Override
            public boolean isPawn() {
                return false;
            }

            @Override
            public boolean isBishop() {
                return false;
            }

            @Override
            public boolean isKnight() {
                return false;
            }

            @Override
            public boolean isQueen() {
                return false;
            }
        },
        QUEEN(900, "Q", "Queen") {
            @Override
            public boolean isKing() {
                return false;
            }

            @Override
            public boolean isRook() {
                return false;
            }

            @Override
            public boolean isPawn() {
                return false;
            }

            @Override
            public boolean isBishop() {
                return false;
            }

            @Override
            public boolean isKnight() {
                return false;
            }

            @Override
            public boolean isQueen() {
                return true;
            }
        },
        KING(10000, "K", "King") {
            @Override
            public boolean isKing() {
                return true;
            }

            @Override
            public boolean isRook() {
                return false;
            }

            @Override
            public boolean isPawn() {
                return false;
            }

            @Override
            public boolean isBishop() {
                return false;
            }

            @Override
            public boolean isKnight() {
                return false;
            }

            @Override
            public boolean isQueen() {
                return false;
            }
        };

        private final int value;
        private final String pieceName;
        private final String fullPieceName;


        public int getPieceValue() {
            return this.value;
        }

        public String getShortPieceName() {
            return this.pieceName;
        }

        public String getFullPieceName() {
            return this.fullPieceName;
        }

        @Override
        public String toString() {
            return this.pieceName;
        }

        PieceType(final int val,
                  final String pieceName,
                  final String fullPieceName) {
            this.value = val;
            this.pieceName = pieceName;
            this.fullPieceName = fullPieceName;
        }

        public abstract boolean isKing();
        public abstract boolean isRook();
        public abstract boolean isPawn();
        public abstract boolean isBishop();
        public abstract boolean isKnight();
        public abstract boolean isQueen();
    }
}
