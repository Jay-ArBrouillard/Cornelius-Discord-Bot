package chess;

import chess.board.Board;
import chess.board.Move;
import chess.board.Tile;
import chess.pgn.FenUtils;
import chess.player.MoveTransition;
import chess.player.ai.stockfish.StockFishClient;
import chess.player.ai.stockfish.engine.enums.Option;
import chess.player.ai.stockfish.engine.enums.Query;
import chess.player.ai.stockfish.engine.enums.QueryType;
import chess.player.ai.stockfish.engine.enums.Variant;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

public class ChessGame {
    public Board board;
    private ChessMessageHandler messageHandler;
    public String evalScore;
    private StockFishClient client;

    public ChessGame() {
        try {
            client = new StockFishClient.Builder()
//                    .setInstances(4) //Only 1 chess game can be played at any time so I don't think increase instances will make any difference
                    .setOption(Option.Minimum_Thinking_Time, 1000) // Minimum thinking time Stockfish will take
                    .setOption(Option.Skill_Level, 0) // Stockfish skill level 0-20
                    .setVariant(Variant.MODERN) // As of 10/8/2020 Modern is the fastest variant that works on Heroku
                    .build();                   // on Local Windows BMI2 is the festest
        } catch (Exception e) {
            e.printStackTrace();
        }

        board = Board.createStandardBoard();
        messageHandler = new ChessMessageHandler();
    }

    public boolean isWhitePlayerTurn() {
        return board.getCurrentPlayer().getAlliance().isWhite();
    }

    public String setupPlayers(String input) {
        if (input.equals("1")) {
            board.getWhitePlayer().setIsRobot(false);
            board.getBlackPlayer().setIsRobot(false);
            return GameMode.PVP.toString();
        }
        else if (input.equals("2")) {
            board.getWhitePlayer().setIsRobot(false);
            board.getBlackPlayer().setIsRobot(true);
            return GameMode.PVC.toString();
        }
        else if (input.equals("3")) {
            board.getWhitePlayer().setIsRobot(true);
            board.getBlackPlayer().setIsRobot(false);
            return GameMode.CVP.toString();
        }
        else {
            return new StringBuilder("`").append(input).append("` is not a valid option. Please choose an option (1-3)").toString();
        }
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

            return "`Legal Moves for "  + filteredInput + " on " + this.board.getCurrentPlayer().getAlliance() + " side`: " + legalMovesForTile.toString();
        }
        if (messageHandler.handleErrorMessage().equals(messageHandler.ERROR)) {
            return messageHandler.getLastErrorMessage();
        }
        //////////////////////// Special Case for castling //////////////////////////
        String inputNoSpaces = input.replaceAll("\\s+", "");
        if (input.equals("o-o")) { //King side castle
            if (this.board.getCurrentPlayer().getAlliance().isBlack()) {
                return handleMove(3, 1, inputNoSpaces, false);
            } else {
                return handleMove(59, 57, inputNoSpaces, false);
            }
        }
        else if (input.equals("o-o-o")) { //Queen side castle
            if (this.board.getCurrentPlayer().getAlliance().isBlack()) {
                return handleMove(3, 5, inputNoSpaces, false);
            } else {
                return handleMove(59, 61, inputNoSpaces, false);
            }
        }

        //////////////////////// Validate Move Input ////////////////////////////////
        messageHandler.validateInputLengthFour(input);
        if (messageHandler.handleErrorMessage().equals(messageHandler.ERROR)) {
            return messageHandler.getLastErrorMessage();
        }

        String x1Str = Character.toString(inputNoSpaces.charAt(0)).toLowerCase();
        String y1Str = Character.toString(inputNoSpaces.charAt(1)).toLowerCase();
        String x2Str = Character.toString(inputNoSpaces.charAt(2)).toLowerCase();
        String y2Str = Character.toString(inputNoSpaces.charAt(3)).toLowerCase();

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

        return handleMove(startCoordinate, destinationCoordinate, inputNoSpaces, false);
    }

    private String handleMove(int startCoordinate, int destinationCoordinate, String filtered, boolean isComputer) {
        final Move move = Move.MoveFactory.createMove(this.board, startCoordinate, destinationCoordinate);
        MoveTransition transition = this.board.getCurrentPlayer().makeMove(move);
        if (transition.getMoveStatus().isDone()) {
            this.board = null;
            this.board = transition.getTransitionBoard();
            this.board.buildImage();

            // Is someone in check mate?
            if (this.board.getCurrentPlayer().isInCheckMate()) {
                return "CHECKMATE";
            }

            // Is game in a draw?
            if (this.board.isDraw50MoveRule()) {
                return "DRAW (50 move rule)! The previous 50 moves resulted in no captures or pawn movements.";
            }

            if (this.board.isDrawImpossibleToCheckMate()) {
                return "DRAW! Neither player can reach checkmate in the current game state... " +
                        "This occurred from one of the following combinations:\n1.King versus King\n2.King and Bishop versus king\n3.King and Knight versus King\n4.King and Bishop versus King and Bishop with the bishops on the same color.";
            }

            // Is game in stalement?
            if (this.board.getCurrentPlayer().isInStaleMate()) {
                return "DRAW (Stalement)! " + this.board.getCurrentPlayer().getAlliance() + " is not in check and has no legal moves they can make. Game Over!";
            }

            // Is someone in check?
            if (this.board.getCurrentPlayer().isInCheck()) {
                return "CHECK" + move.toString();
            }

            //Update eval score
            //+position eval is good for white, -negative eval is good for black
            Query query = new Query.Builder(QueryType.EVAL).setFen(FenUtils.parseFEN(this.board)).build();
            final String[] temp = new String[1];
            synchronized(this){
                client.submit(query, result -> temp[0] = result);
            }
            evalScore = temp[0];
            //Should computer resign?
            if (isComputer) {
                double evaluationScore = Double.parseDouble(evalScore.replaceAll("(white side)", "").trim());
                if ((this.board.getCurrentPlayer().getAlliance().isWhite() && evaluationScore <= -10.0) ||
                        (this.board.getCurrentPlayer().getAlliance().isBlack() && evaluationScore >= 10.0)) {
                    return "Cornelius has RESIGNED!";
                }
            }

            return "Success!" + move.toString();
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

    public String ai(MessageChannel mc) {
        int randomThinkTime = ThreadLocalRandom.current().nextInt(5000, 10000 + 1); //Between 5-10 seconds
        mc.sendTyping().queue();

        Query query = new Query.Builder(QueryType.Best_Move).setMovetime(randomThinkTime).setFen(FenUtils.parseFEN(this.board)).build();
        String bestMoveString = null;
        final String[] reply = new String[1];
        synchronized(this){
            client.submit(query, result -> {
                mc.sendTyping().queue();

                String x1Str = Character.toString(bestMoveString.charAt(0));
                String y1Str = Character.toString(bestMoveString.charAt(1));
                String x2Str = Character.toString(bestMoveString.charAt(2));
                String y2Str = Character.toString(bestMoveString.charAt(3));

                ///////////////////////// Get board coordinates from input ////////////////////////////////
                int startCoordinate = convertInputToInteger(x1Str, y1Str);
                int destinationCoordinate = convertInputToInteger(x2Str, y2Str);

                reply[0] = handleMove(startCoordinate, destinationCoordinate, null, true);
            });
        }

        return reply[0];
    }
}
