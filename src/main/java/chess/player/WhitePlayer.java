package chess.player;

import chess.Alliance;
import chess.board.Board;
import chess.board.BoardUtils;
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

import static chess.pieces.Piece.PieceType.ROOK;

public class WhitePlayer extends Player {
    public WhitePlayer(final Board board, final List<Move> whiteStandardLegalMoves, final List<Move> blackStandardLegalMoves) {
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
        if(!hasCastleOpportunities()) {
            return Collections.emptyList();
        }

        final List<Move> kingCastles = new ArrayList<>();

        if(this.playerKing.isFirstMove() && this.playerKing.getPiecePosition() == 60 && !this.isInCheck()) {
            //whites king side castle
            if(!this.board.getTile(61).isTileOccupied() && !this.board.getTile(62).isTileOccupied() ) {
                final Piece kingSideRook = this.board.getTile(63).getPiece();
                if(kingSideRook != null && kingSideRook.isFirstMove()) {
                    if(Player.calculateAttacksOnTile(61, opponentLegalMoves).isEmpty() &&
                            Player.calculateAttacksOnTile(62, opponentLegalMoves).isEmpty() &&
                            kingSideRook.getPieceType() == ROOK) {
                        if(!BoardUtils.isKingPawnTrap(this.board, this.playerKing, 52)) {
                            kingCastles.add(new KingSideCastleMove(this.board, this.playerKing, 62, (Rook) kingSideRook, kingSideRook.getPiecePosition(), 61));
                        }
                    }
                }
            }
            //whites queen side castle
            if(!this.board.getTile(59).isTileOccupied()  && !this.board.getTile(58).isTileOccupied()  &&
                    !this.board.getTile(57).isTileOccupied() ) {
                final Piece queenSideRook = this.board.getTile(56).getPiece();
                if(queenSideRook != null && queenSideRook.isFirstMove()) {
                    if(Player.calculateAttacksOnTile(58, opponentLegalMoves).isEmpty() &&
                            Player.calculateAttacksOnTile(59, opponentLegalMoves).isEmpty() && queenSideRook.getPieceType() == ROOK) {
                        if(!BoardUtils.isKingPawnTrap(this.board, this.playerKing, 52)) {
                            kingCastles.add(new QueenSideCastleMove(this.board, this.playerKing, 58, (Rook) queenSideRook, queenSideRook.getPiecePosition(), 59));
                        }
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
