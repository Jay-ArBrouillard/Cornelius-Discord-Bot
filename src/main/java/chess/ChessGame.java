package chess;

import Utils.GoogleSheets;
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
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

public class ChessGame {
    public Board board;
    private ChessMessageHandler messageHandler;
    private StockFishClient client;
    private GoogleSheets db;
    private ChessGameState state;
    public boolean threadRunning;


    public ChessGame(ChessGameState state) {
        db = new GoogleSheets();
        board = Board.createStandardBoard();
        messageHandler = new ChessMessageHandler();
        this.state = state;

        try {
            client = new StockFishClient.Builder()
                    .setOption(Option.Minimum_Thinking_Time, 1000) // Minimum thinking time Stockfish will take
                    .setOption(Option.Skill_Level, 20) // Stockfish skill level 0-20
                    .setVariant(Variant.MODERN) // As of 10/8/2020 Modern is the fastest variant that works on Heroku
                    .build();                   // on Local Windows BMI2 is the festest
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean didWhiteJustMove() {
        return this.board.getCurrentPlayer().getOpponent().getAlliance().isWhite();
    }

    public boolean didBlackJustMove() {
        return this.board.getCurrentPlayer().getOpponent().getAlliance().isBlack();
    }

    public boolean isWhitePlayerTurn() {
        return this.board.getCurrentPlayer().getAlliance().isWhite();
    }

    public boolean isBlackPlayerTurn() {
        return this.board.getCurrentPlayer().getAlliance().isBlack();
    }

    public String setupPlayers(MessageReceivedEvent event, String message) {
        if (message.equals("1")) {
            board.getWhitePlayer().setIsRobot(false);
            board.getBlackPlayer().setIsRobot(false);
            return GameType.PVP.toString();
        }
        else if (message.equals("2")) {
            board.getWhitePlayer().setIsRobot(false);
            board.getBlackPlayer().setIsRobot(true);
            return GameType.PVC.toString();
        }
        else if (message.equals("3")) {
            board.getWhitePlayer().setIsRobot(true);
            board.getBlackPlayer().setIsRobot(false);
            return GameType.CVP.toString();
        }
        else {
            return new StringBuilder("`").append(message).append("` is not a valid option. Please choose an option (1-3)").toString();
        }
    }

    public ChessGameState processMove(String input)
    {
        //////////////////////// Get all possible moves ////////////////////////////////
        if (messageHandler.showAllLegalMoves(input)) {
            state.setMessage("`All Legal Moves (" + this.board.getCurrentPlayer().getAlliance() + ")`: " + this.board.getCurrentPlayer().getLegalMoves().toString());
            state.setStateShowAllLegalMoves();
            return state;
        }
        if (messageHandler.handleErrorMessage().equals(messageHandler.ERROR)) {
            state.setMessage(messageHandler.getLastErrorMessage());
            state.setStateError();
            return state;
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

            state.setMessage("`Legal Moves for "  + filteredInput + " on " + this.board.getCurrentPlayer().getAlliance() + " side`: " + legalMovesForTile.toString());
            state.setStateShowAllLegalMovesForTile();
            return state;
        }
        if (messageHandler.handleErrorMessage().equals(messageHandler.ERROR)) {
            state.setMessage(messageHandler.getLastErrorMessage());
            state.setStateError();
            return state;
        }
        //////////////////////// Validate Move Input ////////////////////////////////
        String inputNoSpaces = input.replaceAll("\\s+", "");
        messageHandler.validateInputLengthFour(input);
        if (messageHandler.handleErrorMessage().equals(messageHandler.ERROR)) {
            state.setMessage(messageHandler.getLastErrorMessage());
            state.setStateError();
            return state;
        }

        String x1Str = Character.toString(inputNoSpaces.charAt(0)).toLowerCase();
        String y1Str = Character.toString(inputNoSpaces.charAt(1)).toLowerCase();
        String x2Str = Character.toString(inputNoSpaces.charAt(2)).toLowerCase();
        String y2Str = Character.toString(inputNoSpaces.charAt(3)).toLowerCase();

        messageHandler.validateRowAndColumn(x1Str+y1Str, x2Str+y2Str);
        if (messageHandler.handleErrorMessage().equals(messageHandler.ERROR)) {
            state.setMessage(messageHandler.getLastErrorMessage());
            state.setStateError();
            return state;
        }

        ///////////////////////// Get board coordinates from input ////////////////////////////////
        int startCoordinate = convertInputToInteger(x1Str, y1Str);
        int destinationCoordinate = convertInputToInteger(x2Str, y2Str);

        //////////////////////// Validate Move is correct Alliance ////////////////////////////////
        Tile tileAtStart = this.board.getTile(startCoordinate);
        messageHandler.validateIsLegalMove((x1Str+y1Str), tileAtStart, this.board.getCurrentPlayer());
        if (messageHandler.handleErrorMessage().equals(messageHandler.ERROR)) {
            state.setMessage(messageHandler.getLastErrorMessage());
            state.setStateError();
            return state;
        }

        return handleMove(startCoordinate, destinationCoordinate, inputNoSpaces, false);
    }

    private ChessGameState handleMove(int startCoordinate, int destinationCoordinate, String moveCmd, boolean isComputer) {
        final Move move = Move.MoveFactory.createMove(this.board, startCoordinate, destinationCoordinate);
        MoveTransition transition = this.board.getCurrentPlayer().makeMove(move);
        if (transition.getMoveStatus().isDone()) {
            state.setTotalMoves(state.getTotalMoves() + 0.5);
            this.board = transition.getTransitionBoard();
            this.board.buildImage();

            // Is someone in check mate?
            if (this.board.getCurrentPlayer().isInCheckMate()) {
                if (didWhiteJustMove()) {
                    state.setMessage("`" + state.getWhitePlayerName() + "` has CHECKMATED `" + state.getBlackPlayerName() + "`");
                    updateDatabaseWhiteSideWin();
                }
                else {
                    state.setMessage("`" + state.getBlackPlayerName() + "` has CHECKMATED `" + state.getWhitePlayerName() + "`");
                    updateDatabaseBlackSideWin();
                }
                state.setStateCheckmate();
                return state;
            }

            // Is game in a draw?
            if (this.board.isDraw50MoveRule()) {
                state.setMessage("DRAW (50 move rule)! The previous 50 moves resulted in no captures or pawn movements.");
                state.setStateDraw();
                updateDatabaseDraw();
                return state;
            }

            if (this.board.isDrawImpossibleToCheckMate()) {
                state.setMessage("DRAW! Neither player can reach checkmate in the current game state... " +
                        "This occurred from one of the following combinations:\n1.King versus King\n2.King and Bishop versus king\n3.King and Knight versus King\n4.King and Bishop versus King and Bishop with the bishops on the same color.");
                state.setStateDraw();
                updateDatabaseDraw();
                return state;
            }

            // Is game in stalement?
            if (this.board.getCurrentPlayer().isInStaleMate()) {
                state.setMessage("DRAW (Stalement)! " + this.board.getCurrentPlayer().getAlliance() + " is not in check and has no legal moves they can make. Game Over!");
                state.setStateDraw();
                updateDatabaseDraw();
                return state;
            }

            // Is someone in check?
            if (this.board.getCurrentPlayer().isInCheck()) {
                if (didWhiteJustMove()) {
                    state.setMessage("`" + state.getWhitePlayerName() + "` SELECTS " + moveCmd + " `" + state.getBlackPlayerName() + "` is in check!");
                }
                else {
                    state.setMessage("`" + state.getBlackPlayerName() + "` SELECTS " + moveCmd + " `" + state.getWhitePlayerName() + "` is in check!");
                }
                state.setStateCheck();
                return state;
            }

            //Update eval score
            //+position eval is good for white, -negative eval is good for black
            state.setBoardEvaluationMessage(client.submit(new Query.Builder(QueryType.EVAL).setFen(FenUtils.parseFEN(this.board)).build()).substring(22));
            //Should computer resign?
            if (isComputer) {
                double evaluationScore = Double.parseDouble(state.getBoardEvaluationMessage().replace("(white side)", "").trim());
                if (didWhiteJustMove() && evaluationScore <= -10.0) {
                    state.setMessage("Cornelius has RESIGNED!");
                    state.setStateComputerResign();
                    updateDatabaseBlackSideWin();
                    return state;
                }
                if (didBlackJustMove() && evaluationScore >= 10.0) {
                    state.setMessage("Cornelius has RESIGNED!");
                    state.setStateComputerResign();
                    updateDatabaseWhiteSideWin();
                    return state;
                }
            }

            //If we get to this point then player made a legal move
            if (didWhiteJustMove()) {
                state.setMessage("`" + state.getWhitePlayerName() + "` SELECTS " + move.toString());
            }
            else {
                state.setMessage("`" + state.getBlackPlayerName() + "` SELECTS " + move.toString());
            }
            state.setStateSuccessfulMove();
            return state;
        }
        else {
            if (transition.getMoveStatus().leavesPlayerInCheck()) {
                state.setMessage("`"+moveCmd + "` leaves " + this.board.getCurrentPlayer().getAlliance() + " player in check");
                state.setStateLeavesPlayerInCheck();
                return state;
            }
            else {
                state.setMessage("`"+moveCmd + "` is not a legal move for " + this.board.getCurrentPlayer().getAlliance());
                state.setStateIllegalMove();
                return state;
            }
        }
    }

    public synchronized int addUser(String id, String name) {
        return db.addUser(id, name);
    }

    private synchronized void updateDatabaseDraw() {
        db.updateUser(state.getBlackPlayerId(), false, true, state.getBlackPlayerElo(), state.getWhitePlayerElo());
        db.updateUser(state.getWhitePlayerId(), false, true, state.getWhitePlayerElo(), state.getBlackPlayerElo());
        db.addCompletedMatch(state.getWhitePlayerName(), state.getBlackPlayerName(), state.getWhitePlayerId(), state.getBlackPlayerId(), state.getWhitePlayerElo(), state.getBlackPlayerElo(),"", "", true, state.getMatchStartTime(), state.getTotalMoves());
        db.updateAvgGameLength(state.getBlackPlayerId());
        db.updateAvgGameLength(state.getWhitePlayerId());
    }

    public synchronized void updateDatabaseWhiteSideWin() {
        updateDatabaseWhiteSideWin(state);
    }

    public synchronized void updateDatabaseWhiteSideWin(ChessGameState state) {
        db.updateUser(state.getBlackPlayerId(), false, false, state.getBlackPlayerElo(), state.getWhitePlayerElo());
        db.updateUser(state.getWhitePlayerId(), true, false, state.getWhitePlayerElo(), state.getBlackPlayerElo());
        db.addCompletedMatch(state.getWhitePlayerName(), state.getBlackPlayerName(), state.getWhitePlayerId(), state.getBlackPlayerId(), state.getWhitePlayerElo(), state.getBlackPlayerElo(), state.getWhitePlayerName(), state.getBlackPlayerName(), false, state.getMatchStartTime(), state.getTotalMoves());
        db.updateAvgGameLength(state.getBlackPlayerId());
        db.updateAvgGameLength(state.getWhitePlayerId());
        threadRunning = false;
    }

    public synchronized void updateDatabaseBlackSideWin() {
        updateDatabaseBlackSideWin(state);
    }

    public synchronized void updateDatabaseBlackSideWin(ChessGameState state) {
        db.updateUser(state.getBlackPlayerId(), true, false, state.getBlackPlayerElo(), state.getWhitePlayerElo());
        db.updateUser(state.getWhitePlayerId(), false, false, state.getWhitePlayerElo(), state.getBlackPlayerElo());
        db.addCompletedMatch(state.getWhitePlayerName(), state.getBlackPlayerName(), state.getWhitePlayerId(), state.getBlackPlayerId(), state.getWhitePlayerElo(), state.getBlackPlayerElo(), state.getBlackPlayerName(), state.getWhitePlayerName(), false, state.getMatchStartTime(), state.getTotalMoves());
        db.updateAvgGameLength(state.getBlackPlayerId());
        db.updateAvgGameLength(state.getWhitePlayerId());
        threadRunning = false;
    }

    public int convertInputToInteger(String column, String row) {
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

    public ChessGameState ai(MessageChannel mc) {
        int randomThinkTime = ThreadLocalRandom.current().nextInt(5000, 10000 + 1); //Between 5-10 seconds
        String bestMoveString = client.submit(new Query.Builder(QueryType.Best_Move).setMovetime(randomThinkTime).setFen(FenUtils.parseFEN(this.board)).build());
        mc.sendTyping().queue();

        String x1Str = Character.toString(bestMoveString.charAt(0));
        String y1Str = Character.toString(bestMoveString.charAt(1));
        String x2Str = Character.toString(bestMoveString.charAt(2));
        String y2Str = Character.toString(bestMoveString.charAt(3));

        ///////////////////////// Get board coordinates from input ////////////////////////////////
        int startCoordinate = convertInputToInteger(x1Str, y1Str);
        int destinationCoordinate = convertInputToInteger(x2Str, y2Str);

        return handleMove(startCoordinate, destinationCoordinate, bestMoveString, true);
    }
}
