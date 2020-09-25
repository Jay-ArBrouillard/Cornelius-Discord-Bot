package chess.player;

import chess.Alliance;
import chess.board.Board;
import chess.board.Move;
import chess.board.Move.KingSideCastleMove;
import chess.board.Move.QueenSideCastleMove;
import chess.board.Tile;
import chess.pieces.Piece;
import chess.pieces.Rook;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class WhitePlayer extends Player {
    public WhitePlayer(final Board board, final Collection<Move> whiteStandardLegalMoves, final Collection<Move> blackStandardLegalMoves) {
        super(board, whiteStandardLegalMoves, blackStandardLegalMoves);
    }

    @Override
    public Alliance getAlliance() {
        return Alliance.WHITE;
    }

    @Override
    public Collection<Piece> getActivePieces() {
        return this.board.getWhitePieces();
    }

    @Override
    public Player getOpponent() {
        return this.board.getBlackPlayer();
    }

    @Override
    protected Collection<Move> calculateKingCastles(final Collection<Move> playerLegalMoves, final Collection<Move> opponentLegalMoves) {
        final List<Move> kingCastles = new ArrayList<>();

        if (this.playerKing.isFirstMove() && !this.isInCheck()) {
            // White King Side Castle
            if (!this.board.getTile(57).isTileOccupied() && !this.board.getTile(58).isTileOccupied()) {
               final Tile rookTile = this.board.getTile(56);
               if (rookTile.isTileOccupied() && rookTile.getPiece().isFirstMove()) {
                   if (Player.calculateAttacksOnTile(57, opponentLegalMoves).isEmpty() &&
                           Player.calculateAttacksOnTile(58, opponentLegalMoves).isEmpty() &&
                           rookTile.getPiece().getPieceType().isRook()) {
                       kingCastles.add(new KingSideCastleMove(this.board, this.playerKing, 57,
                                                                   (Rook)rookTile.getPiece(), rookTile.getTileCoordinate(), 58));
                   }
               }
            }
            // White Queen Side Castle
            if (!this.board.getTile(60).isTileOccupied() &&
                    !this.board.getTile(61).isTileOccupied() &&
                    !this.board.getTile(62).isTileOccupied()) {
                final Tile rookTile = this.board.getTile(63);
                if (rookTile.isTileOccupied() && rookTile.getPiece().isFirstMove()) {
                    if (Player.calculateAttacksOnTile(60, opponentLegalMoves).isEmpty() &&
                            Player.calculateAttacksOnTile(61, opponentLegalMoves).isEmpty() &&
                            Player.calculateAttacksOnTile(62, opponentLegalMoves).isEmpty() &&
                            rookTile.getPiece().getPieceType().isRook()) {
                        kingCastles.add(new QueenSideCastleMove(this.board, this.playerKing, 61,
                                                               (Rook)rookTile.getPiece(), rookTile.getTileCoordinate(), 60));
                    }
                }
            }
        }

        return Collections.unmodifiableList(kingCastles);
    }

    @Override
    public String toString() {
        return Alliance.WHITE.toString();
    }
}
