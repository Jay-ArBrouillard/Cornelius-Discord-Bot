package chess;

import chess.board.Board;
import chess.board.BoardUtils;
import chess.board.Move;
import chess.board.Move.*;
import chess.board.Tile;
import chess.player.MoveTransition;

import java.util.ArrayList;
import java.util.Collection;

public class ChessGame {
    public Board board;
    private ChessMessageHandler messageHandler;

    public ChessGame() {
        board = Board.createStandardBoard();
        messageHandler = new ChessMessageHandler();
    }

    public String processMove(String input)
    {
        //////////////////////// Get all possible moves ////////////////////////////////
        if (messageHandler.showAllLegalMoves(input)) {
             return "`All Legal Moves (" + this.board.getCurrentPlayer().getAlliance() + ")`: " + this.board.getCurrentPlayer().getLegalMoves().toString();
        }
        if (messageHandler.handleErrorMessage().equals(messageHandler.ERROR)) {
            return messageHandler.getLastErrorMessage();
        }
        //////////////////////// Get all possible moves for specific tile ////////////////////////////////
        if (messageHandler.showAllLegalMovesForTile(input)) {
            String removeHelpString = input.replace("help", "");
            String filteredInput = removeHelpString.replaceAll("\\s+", "");
            String x1Str = Character.toString(filteredInput.charAt(0)).toLowerCase();
            String y1Str = Character.toString(filteredInput.charAt(1)).toLowerCase();

            int startCoordinate = convertInputToInteger(x1Str, y1Str);
            final Collection<Move> legalMovesForTile = new ArrayList<>();
            for (final Move move : this.board.getCurrentPlayer().getLegalMoves()) {
                if (move.getCurrentCoordinate() == startCoordinate)  {
                    legalMovesForTile.add(move);
                }
            }

            return "`Legal Moves for "  + filteredInput + "` on " + this.board.getCurrentPlayer().getAlliance() + " side`: " + legalMovesForTile.toString();
        }
        if (messageHandler.handleErrorMessage().equals(messageHandler.ERROR)) {
            return messageHandler.getLastErrorMessage();
        }
        //////////////////////// Special Case for castling //////////////////////////
        String filtered = input.replaceAll("\\s+", "");
        if (input.equals("o-o")) { //King side castle
            if (this.board.getCurrentPlayer().getAlliance().isBlack()) {
                return handleMove(3, 1, filtered);
            } else {
                return handleMove(59, 57, filtered);
            }
        }
        else if (input.equals("o-o-o")) { //Queen side castle
            if (this.board.getCurrentPlayer().getAlliance().isBlack()) {
                return handleMove(3, 5, filtered);
            } else {
                return handleMove(59, 61, filtered);
            }
        }

        //////////////////////// Validate Move Input ////////////////////////////////
        messageHandler.validateInputLengthFour(input);
        if (messageHandler.handleErrorMessage().equals(messageHandler.ERROR)) {
            return messageHandler.getLastErrorMessage();
        }

        String x1Str = Character.toString(filtered.charAt(0)).toLowerCase();
        String y1Str = Character.toString(filtered.charAt(1)).toLowerCase();
        String x2Str = Character.toString(filtered.charAt(2)).toLowerCase();
        String y2Str = Character.toString(filtered.charAt(3)).toLowerCase();

        messageHandler.validateRowAndColumn(x1Str+y1Str, x2Str+y2Str);
        if (messageHandler.handleErrorMessage().equals(messageHandler.ERROR)) {
            return messageHandler.getLastErrorMessage();
        }

        ///////////////////////// Get board coordinates from input ////////////////////////////////
        int startCoordinate = convertInputToInteger(x1Str, y1Str);
        int destinationCoordinate = convertInputToInteger(x2Str, y2Str);

        //////////////////////// Validate Move is correct Alliance ////////////////////////////////
        Tile tileAtStart = this.board.getTile(startCoordinate);
        messageHandler.validateIsLegalMove((x1Str+y1Str), tileAtStart, this.board.getCurrentPlayer());
        if (messageHandler.handleErrorMessage().equals(messageHandler.ERROR)) {
            return messageHandler.getLastErrorMessage();
        }

        return handleMove(startCoordinate, destinationCoordinate, filtered);
    }

    private String handleMove(int startCoordinate, int destinationCoordinate, String filtered) {
        final Move move = Move.MoveFactory.createMove(this.board, startCoordinate, destinationCoordinate);
        final MoveTransition transition = this.board.getCurrentPlayer().makeMove(move);
        if (transition.getMoveStatus().isDone()) {
            this.board = transition.getTransitionBoard();
            this.board.buildImage();

            // Is someone in check mate?
            if (this.board.getCurrentPlayer().isInCheckMate()) {
                return this.board.getCurrentPlayer().getOpponent().getAlliance() + " has checkmated " + this.board.getCurrentPlayer().getAlliance() + "! Game Over!";
            }

            // Is game in stalement?
            if (this.board.getCurrentPlayer().isInStaleMate()) {
                return "DRAW! Game Over!";
            }

            // Is someone in check?
            if (this.board.getCurrentPlayer().isInCheck()) {
                return this.board.getCurrentPlayer().getAlliance() + " is in check!";
            }

            String alliance = this.board.getCurrentPlayer().getOpponent().getAlliance().toString();
            if (move instanceof PawnEnPassantAttackMove) {
                return "Success!" + alliance + " " + move.getMovedPiece().getPieceType() + " en passant " + BoardUtils.getPositionAtCoordinate(move.getMovedPiece().getPiecePosition());
            }
            else if (move instanceof PawnPromotion) {
                return "Success!Pawn at " + BoardUtils.getPositionAtCoordinate(move.getCurrentCoordinate()) + " promoted to " + move.getMovedPiece().getPieceType();
            }
            else if (move instanceof KingSideCastleMove) {
                return "Success!" + alliance + " " + move.toString() + " (KingSideCastleMove)";
            }
            else if (move instanceof QueenSideCastleMove) {
                return "Success!" + alliance + " " + move.toString() + " (QueenSideCastleMove)";
            }
            else if (move instanceof AttackMove || move instanceof MajorAttackMove) {
                return "Success!" + alliance + " " + move.getMovedPiece().getPieceType() + " captured " + BoardUtils.getPositionAtCoordinate(move.getMovedPiece().getPiecePosition());
            }
            else if (move instanceof PawnMove || move instanceof MajorMove) {
                return "Success!" + alliance + " " + move.getMovedPiece().getPieceType() + " moved to " + BoardUtils.getPositionAtCoordinate(move.getMovedPiece().getPiecePosition());
            }
            else if (move instanceof PawnJump) {
                return "Success!" + alliance + " " + move.getMovedPiece().getPieceType() + " jumped to " + BoardUtils.getPositionAtCoordinate(move.getMovedPiece().getPiecePosition());
            }
            else {
                return "Fail: " + move;
            }

        }
        else {
            if (transition.getMoveStatus().leavesPlayerInCheck()) {
                return "`"+filtered + "` leaves " + this.board.getCurrentPlayer().getAlliance() + " player in check";
            }
            else {
                return "`"+filtered + "` is not a legal move for " + this.board.getCurrentPlayer().getAlliance();
            }
        }
    }

    private int convertInputToInteger(String column, String row) {
        int r = transformRowNumber(row);
        int c = convertStringLetterToNumber(column);
        return r + c;
    }

    private int transformRowNumber(String rowNumber) {
        int rowValue = Integer.parseInt(rowNumber);
        switch (rowValue) {
            case 1:
                rowValue = 56;
                break;
            case 2:
                rowValue = 48;
                break;
            case 3:
                rowValue = 40;
                break;
            case 4:
                rowValue = 32;
                break;
            case 5:
                rowValue = 24;
                break;
            case 6:
                rowValue = 16;
                break;
            case 7:
                rowValue = 8;
                break;
            case 8:
                rowValue = 0;
                break;
            default:
                throw new RuntimeException("transformRowNumber Error. Should not happen.");
        }

        return rowValue;
    }

    private int convertStringLetterToNumber(String value)
    {
        int intValue;
        if (value.equals("a")) {
            intValue = 0;
        }
        else if (value.equals("b")) {
            intValue = 1;
        }
        else if (value.equals("c")) {
            intValue = 2;
        }
        else if (value.equals("d")) {
            intValue = 3;
        }
        else if (value.equals("e")) {
            intValue = 4;
        }
        else if (value.equals("f")) {
            intValue = 5;
        }
        else if (value.equals("g")) {
            intValue = 6;
        }
        else if (value.equals("h")) {
            intValue = 7;
        }
        else {
            throw new RuntimeException("convertStringLetterToNumber error. Should not happen");
        }

        return intValue;
    }

}
