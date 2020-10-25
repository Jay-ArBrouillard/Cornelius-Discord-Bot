package chess.pgn;

import chess.board.Board;
import chess.board.BoardUtils;
import chess.pieces.*;
import java.util.*;

public class FenUtils {

    private FenUtils() {
        throw new RuntimeException("Not instantiable");
    }

    //TODO implement for when user want to start a game from a specific position
//    public static Board createGameFromFEN(final String fenString) {
//        return parseFEN(fenString);
//    }

    public static String parseFEN(final Board board) {
        return calculateBoardText(board) + " " +
                calculateCurrentPlayerText(board) + " " +
                calculateCastleText(board) + " " +
                calculateEnPassantSquare(board) + " " +
                calculateMoves(board);
    }

    private static String calculateMoves(Board board) {
        int halfMove = 0;
        int fullMoves = board.getNumFullMoves();
        List<Boolean> moveHistory = board.getMovesPlayed();
        for (int i = moveHistory.size() - 1; i >= 0; i--) {
            if (moveHistory.get(i) == true) {
                break;
            }
            halfMove++;
        }
        return String.format("%d %d", halfMove, fullMoves);
    }

//    private static Board parseFEN(final String fenString) {
//        final String[] fenPartitions = fenString.trim().split(" ");
//        final Board.Builder builder = new Board.Builder();
//        final boolean whiteKingSideCastle = whiteKingSideCastle(fenPartitions[2]);
//        final boolean whiteQueenSideCastle = whiteQueenSideCastle(fenPartitions[2]);
//        final boolean blackKingSideCastle = blackKingSideCastle(fenPartitions[2]);
//        final boolean blackQueenSideCastle = blackQueenSideCastle(fenPartitions[2]);
//        final String gameConfiguration = fenPartitions[0];
//        final char[] boardTiles = gameConfiguration.replaceAll("/", "")
//                .replaceAll("8", "--------")
//                .replaceAll("7", "-------")
//                .replaceAll("6", "------")
//                .replaceAll("5", "-----")
//                .replaceAll("4", "----")
//                .replaceAll("3", "---")
//                .replaceAll("2", "--")
//                .replaceAll("1", "-")
//                .toCharArray();
//        int i = 0;
//        while (i < boardTiles.length) {
//            switch (boardTiles[i]) {
//                case 'r':
//                    builder.setPiece(new Rook(i, Alliance.BLACK));
//                    i++;
//                    break;
//                case 'n':
//                    builder.setPiece(new Knight(i, Alliance.BLACK));
//                    i++;
//                    break;
//                case 'b':
//                    builder.setPiece(new Bishop(i, Alliance.BLACK));
//                    i++;
//                    break;
//                case 'q':
//                    builder.setPiece(new Queen(i, Alliance.BLACK));
//                    i++;
//                    break;
//                case 'k':
//                    builder.setPiece(new King(i, Alliance.BLACK, blackKingSideCastle, blackQueenSideCastle));
//                    i++;
//                    break;
//                case 'p':
//                    builder.setPiece(new Pawn(i, Alliance.BLACK));
//                    i++;
//                    break;
//                case 'R':
//                    builder.setPiece(new Rook(i, Alliance.WHITE));
//                    i++;
//                    break;
//                case 'N':
//                    builder.setPiece(new Knight(i, Alliance.WHITE));
//                    i++;
//                    break;
//                case 'B':
//                    builder.setPiece(new Bishop(i, Alliance.WHITE));
//                    i++;
//                    break;
//                case 'Q':
//                    builder.setPiece(new Queen(i, Alliance.WHITE));
//                    i++;
//                    break;
//                case 'K':
//                    builder.setPiece(new King(i, Alliance.WHITE, whiteKingSideCastle, whiteQueenSideCastle));
//                    i++;
//                    break;
//                case 'P':
//                    builder.setPiece(new Pawn(i, Alliance.WHITE));
//                    i++;
//                    break;
//                case '-':
//                    i++;
//                    break;
//                default:
//                    throw new RuntimeException("Invalid FEN String " +gameConfiguration);
//            }
//        }
//        builder.setMoveMaker(moveMaker(fenPartitions[1]));
//        return builder.build();
//    }

    private static String calculateBoardText(final Board board) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < BoardUtils.NUM_TILES; i++) {
            final String tileText = board.getTile(i).getPiece() == null ? "-" :
                    board.getTile(i).getPiece().getPieceAlliance().isWhite() ? board.getTile(i).getPiece().getPieceType().getShortPieceName().toUpperCase() :
                            board.getTile(i).getPiece().getPieceType().getShortPieceName().toLowerCase();
            builder.append(tileText);
        }
        builder.insert(8, "/");
        builder.insert(17, "/");
        builder.insert(26, "/");
        builder.insert(35, "/");
        builder.insert(44, "/");
        builder.insert(53, "/");
        builder.insert(62, "/");

        return builder.toString().replaceAll("--------", "8")
                                 .replaceAll("-------", "7")
                                 .replaceAll("------", "6")
                                 .replaceAll("-----", "5")
                                 .replaceAll("----", "4")
                                 .replaceAll("---", "3")
                                 .replaceAll("--", "2")
                                 .replaceAll("-", "1");
    }

    private static String calculateCurrentPlayerText(final Board board) {
        return board.getCurrentPlayer().toString().substring(0, 1).toLowerCase(); //b or w
    }

    private static String calculateEnPassantSquare(final Board board) {
        final Pawn enPassantPawn = board.getEnPassantPawn();

        if (enPassantPawn != null) {
            return BoardUtils.getPositionAtCoordinate(enPassantPawn.getPiecePosition() + (8) * enPassantPawn.getPieceAlliance().getOppositeDirection());
        }
        return "-";
    }

    private static String calculateCastleText(final Board board) {
        final StringBuilder builder = new StringBuilder();
        if (board.getWhitePlayer().isKingSideCastleCapable()) {
            builder.append("K");
        }
        if (board.getWhitePlayer().isQueenSideCastleCapable()) {
            builder.append("Q");
        }
        if (board.getBlackPlayer().isKingSideCastleCapable()) {
            builder.append("k");
        }
        if (board.getBlackPlayer().isQueenSideCastleCapable()) {
            builder.append("q");
        }
        final String result = builder.toString();
        return result.isEmpty() ? "-" : result;
    }
}
