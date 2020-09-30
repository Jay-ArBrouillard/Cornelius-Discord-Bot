package chess.board;

import chess.pieces.King;
import chess.pieces.Piece;
import chess.player.MoveTransition;

import java.util.*;

import static chess.board.Move.NULL_MOVE;

public class BoardUtils {

    //NOTE board is read top to bottom left to right
    public static final List<Boolean> FIRST_COLUMN = initColumn(0);
    public static final List<Boolean> SECOND_COLUMN = initColumn(1);
    public static final List<Boolean> THIRD_COLUMN = initColumn(2);
    public static final List<Boolean> FOURTH_COLUMN = initColumn(3);
    public static final List<Boolean> FIFTH_COLUMN = initColumn(4);
    public static final List<Boolean> SIXTH_COLUMN = initColumn(5);
    public static final List<Boolean> SEVENTH_COLUMN = initColumn(6);
    public static final List<Boolean> EIGHTH_COLUMN = initColumn(7);
    public static final List<Boolean> FIRST_ROW = initRow(0);
    public static final List<Boolean> SECOND_ROW = initRow(8);
    public static final List<Boolean> THIRD_ROW = initRow(16);
    public static final List<Boolean> FOURTH_ROW = initRow(24);
    public static final List<Boolean> FIFTH_ROW = initRow(32);
    public static final List<Boolean> SIXTH_ROW = initRow(40);
    public static final List<Boolean> SEVENTH_ROW = initRow(48);
    public static final List<Boolean> EIGHTH_ROW = initRow(56);
    public static final int START_TILE_INDEX = 0;
    public static final int NUM_TILES_PER_ROW = 8;
    public static final int NUM_TILES = 64;
    public static final List<String> ALGEBRAIC_NOTATION = initializeAlgebraicNotation();
    public static final Map<String, Integer> POSITION_TO_COORDINATE = initializePositionToCoordinateMap();

    private BoardUtils() {
        throw new RuntimeException("Do not instantiate BoardUtils");
    }

    private static List<Boolean> initColumn(int columnNumber) {
        final Boolean[] column = new Boolean[NUM_TILES];
        for(int i = 0; i < column.length; i++) {
            column[i] = false;
        }
        do {
            column[columnNumber] = true;
            columnNumber += NUM_TILES_PER_ROW;
        } while(columnNumber < NUM_TILES);
        return Collections.unmodifiableList(Arrays.asList((column)));
    }

    private static List<Boolean> initRow(int rowNumber) {
        final Boolean[] row = new Boolean[NUM_TILES];
        for(int i = 0; i < row.length; i++) {
            row[i] = false;
        }
        do {
            row[rowNumber] = true;
            rowNumber++;
        } while(rowNumber % NUM_TILES_PER_ROW != 0);
        return Collections.unmodifiableList(Arrays.asList(row));
    }

    private static Map<String, Integer> initializePositionToCoordinateMap() {
        final Map<String, Integer> positionToCoordinate = new HashMap<>();
        for (int i = START_TILE_INDEX; i < NUM_TILES; i++) {
            positionToCoordinate.put(ALGEBRAIC_NOTATION.get(i), i);
        }
        return Collections.unmodifiableMap(positionToCoordinate);
    }

    private static List<String> initializeAlgebraicNotation() {
        return Collections.unmodifiableList(Arrays.asList(
                "a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8",
                "a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7",
                "a6", "b6", "c6", "d6", "e6", "f6", "g6", "h6",
                "a5", "b5", "c5", "d5", "e5", "f5", "g5", "h5",
                "a4", "b4", "c4", "d4", "e4", "f4", "g4", "h4",
                "a3", "b3", "c3", "d3", "e3", "f3", "g3", "h3",
                "a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2",
                "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1"));
    }

    //0 for DARK and 1 for LIGHT
    private final static int[] TILE_COLORS = {
            1,  0,  1,  0,  1,  0,  1,  0,
            0,  1,  0,  1,  0,  1,  0,  1,
            1,  0,  1,  0,  1,  0,  1,  0,
            0,  1,  0,  1,  0,  1,  0,  1,
            1,  0,  1,  0,  1,  0,  1,  0,
            0,  1,  0,  1,  0,  1,  0,  0,
            1,  0,  1,  0,  1,  0,  1,  0,
            0,  1,  0,  1,  0,  1,  0,  1,
    };

    private final static int[] WHITE_FORWARD_MOVE = {
            -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,
            7,  7,  7,  7,  7,  7,  7,  7,
            15,  15,  15,  15,  15,  15,  15,  15,
            23,  23,  23,  23,  23,  23,  23,  23,
            31,  31,  31,  31,  31,  31,  31,  31,
            39,  39,  39,  39,  39,  39,  39,  39,
            47,  47,  47,  47,  47,  47,  47,  47,
            55,  55,  55,  55,  55,  55,  55,  55,
    };

    private final static int[] BLACK_FORWARD_MOVE = {
            8,  8,  8,  8,  8,  8,  8,  8,
            16,  16,  16,  16,  16,  16,  16,  16,
            24,  24,  24,  24,  24,  24,  24,  24,
            32,  32,  32,  32,  32,  32,  32,  32,
            40,  40,  40,  40,  40,  40,  40,  40,
            48,  48,  48,  48,  48,  48,  48,  48,
            56,  56,  56,  56,  56,  56,  56,  56,
            -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,
    };


    public static int getWhiteForwardMoveCoordinate(final int coordinate) {
        return WHITE_FORWARD_MOVE[coordinate];
    }

    public static int getBlackForwardMoveCoordinate(final int coordinate) {
        return BLACK_FORWARD_MOVE[coordinate];
    }

    public static int getColorAtCoordinate(final int coordinate) {
        return TILE_COLORS[coordinate];
    }

    public static int getCoordinateAtPosition(final String position) {
        return POSITION_TO_COORDINATE.get(position);
    }

    public static String getPositionAtCoordinate(final int coordinate) {
        return ALGEBRAIC_NOTATION.get(coordinate);
    }

    public static boolean isValidTileCoordinate(int coordinate) {
        return coordinate >=0 && coordinate < 64;
    }

    public static boolean isThreatenedBoardImmediate(final Board board) {
        return board.getWhitePlayer().isInCheck() || board.getBlackPlayer().isInCheck();
    }

    public static boolean kingThreat(final Move move) {
        final Board board = move.getBoard();
        final MoveTransition transition = board.getCurrentPlayer().makeMove(move);
        return transition.getTransitionBoard().getCurrentPlayer().isInCheck();
    }

    public static boolean isKingPawnTrap(final Board board,
                                         final King king,
                                         final int frontTile) {
        final Piece piece = board.getTile(frontTile).getPiece();
        return piece != null &&
                piece.getPieceType() == Piece.PieceType.PAWN &&
                piece.getPieceAlliance() != king.getPieceAlliance();
    }

    public static int mvvlva(final Move move) {
        final Piece movingPiece = move.getMovedPiece();
        if(move.isAttack()) {
            final Piece attackedPiece = move.getAttackedPiece();
            return (attackedPiece.getPieceType().getPieceValue() - movingPiece.getPieceType().getPieceValue() +  Piece.PieceType.KING.getPieceValue()) * 100;
        }
        return Piece.PieceType.KING.getPieceValue() - movingPiece.getPieceType().getPieceValue();
    }

    public static List<Move> lastNMoves(final Board board, int N) {
        final List<Move> moveHistory = board.getMovesPlayed();
        final List<Move> lastNMoves = new ArrayList<>();
        for (int i = moveHistory.size()-1; i >= N; i--) {
            Move move = moveHistory.get(i);
            lastNMoves.add(move);
        }
        return Collections.unmodifiableList(lastNMoves);
    }

    public static boolean isEndGame(final Board board) {
        return board.getCurrentPlayer().isInCheckMate() ||
                board.getCurrentPlayer().isInStaleMate();
    }
}
