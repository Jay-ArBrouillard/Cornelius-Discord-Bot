package chess.board;

import chess.*;
import chess.board.Move.*;
import chess.pieces.*;
import chess.player.BlackPlayer;
import chess.player.Player;
import chess.player.WhitePlayer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class Board {
    private final List<Tile> gameBoard;
    private final List<Piece> whitePieces; //Active (non-captured) white pieces
    private final List<Piece> blackPieces; //Active (non-captured) black pieces

    private final WhitePlayer whitePlayer;
    private final BlackPlayer blackPlayer;
    private final Player currentPlayer;

    private final Pawn enPassantPawn;
    private final List<Boolean> movesPlayed; //If a move captures a piece or moves a pawn value is true else false

    private static final int START_X_COORDINATE = 70;
    private static final int START_Y_COORDINATE = 43;
    private static final int X_OFFSET = 162;
    private static final int Y_OFFSET = 162;

    public Board(Builder builder, List<Boolean> movesPlayed) {
        this.gameBoard = createGameBoard(builder);
        this.whitePieces = calculateActivePieces(this.gameBoard, Alliance.WHITE);
        this.blackPieces = calculateActivePieces(this.gameBoard, Alliance.BLACK);
        this.enPassantPawn = builder.enPassantPawn;

        final List<Move> whiteStandardLegalMoves = calculateLegalMoves(this.whitePieces);
        final List<Move> blackStandardLegalMoves = calculateLegalMoves(this.blackPieces);

        this.whitePlayer = new WhitePlayer(this, whiteStandardLegalMoves, blackStandardLegalMoves);
        this.blackPlayer = new BlackPlayer(this, whiteStandardLegalMoves, blackStandardLegalMoves);
        this.currentPlayer = builder.nextMoveMaker.choosePlayer(this.whitePlayer, this.blackPlayer);

        this.movesPlayed = movesPlayed;
    }

    public Collection<Piece> getWhitePieces() {
        return this.whitePieces;
    }

    public Collection<Piece> getBlackPieces() {
        return this.blackPieces;
    }

    public Player getWhitePlayer() {
        return this.whitePlayer;
    }

    public Player getBlackPlayer() {
        return this.blackPlayer;
    }

    public Player getCurrentPlayer() {
        return this.currentPlayer;
    }

    public Pawn getEnPassantPawn() {
        return this.enPassantPawn;
    }

    public List<Boolean> getMovesPlayed() {
        return movesPlayed;
    }

    public boolean isDraw50MoveRule() {
        if (movesPlayed.size() < 50) {
            return false;
        }
        return movesPlayed.stream().allMatch(x -> x.booleanValue() == false);
    }

    public boolean isDrawImpossibleToCheckMate() {
        //king versus king
        if (this.blackPieces.size() == 1 && this.whitePieces.size() == 1) {
            if (this.blackPieces.get(0) instanceof King && this.whitePieces.get(0) instanceof King) {
                return true;
            }
        }
        //king and bishop versus king
        if (this.blackPieces.size() == 2 && this.whitePieces.size() == 1) {
            boolean hasBlackKing = false;
            boolean hasBlackBishop = false;
            for (final Piece piece : this.blackPieces) {
                if (piece instanceof King) hasBlackKing = true;
                if (piece instanceof Bishop) hasBlackBishop = true;
            }
            if (hasBlackKing && hasBlackBishop && this.whitePieces.get(0) instanceof King) {
                return true;
            }
        }
        if (this.blackPieces.size() == 1 && this.whitePieces.size() == 2) {
            boolean hasWhiteKing = false;
            boolean hasWhiteBishop = false;
            for (final Piece piece : this.whitePieces) {
                if (piece instanceof King) hasWhiteKing = true;
                if (piece instanceof Bishop) hasWhiteBishop = true;
            }
            if (hasWhiteKing && hasWhiteBishop && this.blackPieces.get(0) instanceof King) {
                return true;
            }
        }
        //king and knight versus king
        if (this.blackPieces.size() == 2 && this.whitePieces.size() == 1) {
            boolean hasBlackKing = false;
            boolean hasBlackKnight = false;
            for (final Piece piece : this.blackPieces) {
                if (piece instanceof King) hasBlackKing = true;
                if (piece instanceof Knight) hasBlackKnight = true;
            }
            if (hasBlackKing && hasBlackKnight && this.whitePieces.get(0) instanceof King) {
                return true;
            }
        }
        if (this.blackPieces.size() == 1 && this.whitePieces.size() == 2) {
            boolean hasWhiteKing = false;
            boolean hasWhiteKnight = false;
            for (final Piece piece : this.whitePieces) {
                if (piece instanceof King) hasWhiteKing = true;
                if (piece instanceof Knight) hasWhiteKnight = true;
            }
            if (hasWhiteKing && hasWhiteKnight && this.blackPieces.get(0) instanceof King) {
                return true;
            }
        }
        //king and bishop versus king and bishop with the bishops on the same color.
        if (this.blackPieces.size() == 2 && this.whitePieces.size() == 2) {
            boolean hasBlackKing = false;
            boolean hasBlackBishop = false;
            Boolean blackBishopTileColor = null;//false is dark, true is light
            for (final Piece piece : this.blackPieces) {
                if (piece instanceof King) hasBlackKing = true;
                if (piece instanceof Bishop) {
                    hasBlackBishop = true;
                    blackBishopTileColor = BoardUtils.getColorAtCoordinate(piece.getPiecePosition()) == 1 ? true : false;
                }
            }
            boolean hasWhiteKing = false;
            boolean hasWhiteBishop = false;
            Boolean whiteBishopTileColor = null; //false is dark, true is light
            for (final Piece piece : this.whitePieces) {
                if (piece instanceof King) hasWhiteKing = true;
                if (piece instanceof Bishop) {
                    hasWhiteBishop = true;
                    whiteBishopTileColor = BoardUtils.getColorAtCoordinate(piece.getPiecePosition()) == 1 ? true : false;
                }
            }

            if (hasBlackKing && hasBlackBishop && hasWhiteKing && hasWhiteBishop && blackBishopTileColor == whiteBishopTileColor) {
                System.out.println("Draw: " + blackBishopTileColor + ", " + whiteBishopTileColor);
                return true;
            }
        }
        return false;
    }

    private List<Move> calculateLegalMoves(Collection<Piece> pieces) {
        final List<Move> legalMovesForAlliance = new ArrayList<>();
        for (final Piece piece : pieces) {
            legalMovesForAlliance.addAll(piece.getLegalMoves(this));
        }
        return Collections.unmodifiableList(legalMovesForAlliance);
    }

    private static List<Piece> calculateActivePieces(List<Tile> gameBoard, Alliance alliance) {
        final List<Piece> activePieces = new ArrayList<>();
        for (final Tile tile : gameBoard) {
            if (tile.isTileOccupied()) {
                final Piece piece = tile.getPiece();
                if (piece.getPieceAlliance() == alliance) {
                    activePieces.add(piece);
                }
            }
        }

        return Collections.unmodifiableList(activePieces);
    }

    private List<Tile> createGameBoard(final Builder builder) {
        final List<Tile> tiles = new ArrayList<>(BoardUtils.NUM_TILES);
        for(int i = 0; i < BoardUtils.NUM_TILES; i++) {
            tiles.add(i, Tile.createTile(i, builder.boardConfig.get(i)));
        }
        return Collections.unmodifiableList(tiles);
    }

    public Tile getTile(final int tileCoordinate) {
        return gameBoard.get(tileCoordinate);
    }

    public void buildImage() {
        //Begin with board image as background
        BufferedImage result = null;
        Graphics g = null;
        try {
            result = ImageIO.read(new File("src/main/java/chess/assets/board.png"));
            g = result.getGraphics();

            //Overlay all the pieces onto the board based on matrix top bottom, left to right
            int x = START_X_COORDINATE;
            int y = START_Y_COORDINATE;

            for (int i = 0; i < BoardUtils.NUM_TILES; i++) {
                Tile curr = this.gameBoard.get(i);
                if (curr.isTileOccupied()) {
                    //Overlay that piece
                    BufferedImage piece = ImageIO.read(new File(curr.getPiece().getFilePath()));
                    g.drawImage(piece, x, y, 120, 120, null);
                    piece.flush();
                }
                if ((i+1) % BoardUtils.NUM_TILES_PER_ROW == 0) {
                    x = START_X_COORDINATE;
                    y += Y_OFFSET;
                }
                else {
                    x += X_OFFSET;
                }
            }
            ImageIO.write(result, "png", new File("src/main/java/chess/gameState.png"));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (result != null) {
                result.flush();
                result = null;
            }
            if (g != null) {
                g.dispose();
                g = null;
            }
        }
    }

    public static Board createStandardBoard() {
        final Builder builder = new Builder();
        // Black Layout
        builder.setPiece(new Rook(0, Alliance.BLACK));
        builder.setPiece(new Knight(1, Alliance.BLACK));
        builder.setPiece(new Bishop(2, Alliance.BLACK));
        builder.setPiece(new Queen(3, Alliance.BLACK));
        builder.setPiece(new King(4, Alliance.BLACK, true, true));
        builder.setPiece(new Bishop(5, Alliance.BLACK));
        builder.setPiece(new Knight(6, Alliance.BLACK));
        builder.setPiece(new Rook(7, Alliance.BLACK));
        builder.setPiece(new Pawn(8, Alliance.BLACK));
        builder.setPiece(new Pawn(9, Alliance.BLACK));
        builder.setPiece(new Pawn(10, Alliance.BLACK));
        builder.setPiece(new Pawn(11, Alliance.BLACK));
        builder.setPiece(new Pawn(12, Alliance.BLACK));
        builder.setPiece(new Pawn(13, Alliance.BLACK));
        builder.setPiece(new Pawn(14, Alliance.BLACK));
        builder.setPiece(new Pawn(15, Alliance.BLACK));
        // White Layout
        builder.setPiece(new Pawn(48, Alliance.WHITE));
        builder.setPiece(new Pawn(49, Alliance.WHITE));
        builder.setPiece(new Pawn(50, Alliance.WHITE));
        builder.setPiece(new Pawn(51, Alliance.WHITE));
        builder.setPiece(new Pawn(52, Alliance.WHITE));
        builder.setPiece(new Pawn(53, Alliance.WHITE));
        builder.setPiece(new Pawn(54, Alliance.WHITE));
        builder.setPiece(new Pawn(55, Alliance.WHITE));
        builder.setPiece(new Rook(56, Alliance.WHITE));
        builder.setPiece(new Knight(57, Alliance.WHITE));
        builder.setPiece(new Bishop(58, Alliance.WHITE));
        builder.setPiece(new Queen(59, Alliance.WHITE));
        builder.setPiece(new King(60, Alliance.WHITE, true, true));
        builder.setPiece(new Bishop(61, Alliance.WHITE));
        builder.setPiece(new Knight(62, Alliance.WHITE));
        builder.setPiece(new Rook(63, Alliance.WHITE));
        //white to move
        builder.setMoveMaker(Alliance.WHITE);
        //build the board
        return builder.build(new LinkedList<>());
    }

    public static class Builder {

        Map<Integer, Piece> boardConfig;
        Alliance nextMoveMaker;
        Pawn enPassantPawn;

        public Builder() {
            boardConfig = new HashMap<>();
        }

        public Builder setPiece(final Piece piece) {
            this.boardConfig.put(piece.getPiecePosition(), piece);
            return this;
        }

        public Builder setMoveMaker(final Alliance nextMoveMaker) {
            this.nextMoveMaker = nextMoveMaker;
            return this;
        }

        public Board build(List<Boolean> movesPlayed) {
            if (movesPlayed.size() >= 51) {
                movesPlayed.remove(0); //IMPORTANT: never let the size exceed 50 moves. Remove the first move in the list
            }
            return new Board(this, movesPlayed);
        }

        public void setEnPassantPawn(Pawn movedPawn) {
            this.enPassantPawn = movedPawn;
        }
    }
}

