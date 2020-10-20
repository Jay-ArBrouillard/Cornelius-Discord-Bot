package chess;

import Utils.GoogleSheets;
import chess.board.Board;
import chess.board.BoardUtils;
import chess.board.Move;
import chess.board.Tile;
import chess.pgn.FenUtils;
import chess.player.MoveTransition;
import chess.player.ai.IterativeDeepening;
import chess.player.ai.uci.*;
import chess.player.ai.uci.engine.enums.Option;
import chess.player.ai.uci.engine.enums.Query;
import chess.player.ai.uci.engine.enums.QueryType;
import chess.player.ai.uci.engine.enums.Variant;
import chess.tables.ChessPlayer;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.*;

public class ChessGame {
    public Board board;
    private ChessMessageHandler messageHandler;
    private GoogleSheets db;
    private ChessGameState state;
    private ChessPlayer whiteSidePlayer;
    private ChessPlayer blackSidePlayer;
    private IterativeDeepening id;
    public boolean threadRunning = false;
    public StockFishClient stockFishClient;
    public BaseAiClient client1;
    public BaseAiClient client2;

    public ChessGame(ChessGameState state) {
        db = new GoogleSheets();
        board = Board.createStandardBoard();
        messageHandler = new ChessMessageHandler();
        this.state = state;
    }

    public void setupStockfishClient() {
        try {
            stockFishClient = new StockFishClient.Builder()
                                .setOption(Option.Minimum_Thinking_Time, 500)
                                .setOption(Option.Hash, 16)
                                .setVariant(Variant.MODERN)  // BMI for windows, Modern for linux
                                .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setupComputerClient(GameType gameType) {
        try {
            ChessPlayer [] players = new ChessPlayer[0];
            if (gameType.isPlayerVsComputer()) {
                if (whiteSidePlayer.discordId.contains(System.getenv("OWNER_ID"))) { //White side is ai
                    players = new ChessPlayer[]{whiteSidePlayer};
                }
                else { //black side is ai
                    players = new ChessPlayer[]{blackSidePlayer};
                }
            } else if (gameType.isComputerVsComputer()) {
                players = new ChessPlayer[]{whiteSidePlayer, blackSidePlayer};
            } else { //Player vs player
                //Don't initialize clients
                return;
            }

            for (ChessPlayer p : players) {
                if (p.name.contains("Stockfish")) {
                    setClient(new StockFishClient.Builder()
                            .setOption(Option.Minimum_Thinking_Time, 500) // Minimum thinking time Stockfish will take
                            .setOption(Option.Skill_Level, 20)
                            .setOption(Option.Hash, 16)
                            .setVariant(Variant.MODERN) // BMI for windows, Modern for linux
                            .build(), p);
            }
                else if (p.name.contains("Xiphos")) {
                    setClient(new XiphosClient.Builder()
                            .setOption(Option.Minimum_Thinking_Time, 500)
                            .setVariant(Variant.SSE) //BMI or windows, SSE for linux
                            .setOption(Option.Hash, 16)
                            .build(), p);
                }
                else if (p.name.contains("Komodo")) {
                    setClient(new KomodoClient.Builder()
                            .setOption(Option.Minimum_Thinking_Time, 500)
                            .setVariant(Variant.DEFAULT) //Always set to Default for linux
                            .setOption(Option.Hash, 16)
                            .build(), p);
                }
                else if (p.name.contains("Cinnamon")) {
                    setClient(new CinnamonClient.Builder()
                            .setVariant(Variant.DEFAULT) //Always set to Default for linux
                            .setOption(Option.Hash, 16)
                            .build(), p);
                }
                else if (p.name.contains("Laser")) {
                    setClient(new LaserClient.Builder()
                            .setVariant(Variant.DEFAULT) //Always set to Default for linux
                            .setOption(Option.Hash, 16)
                            .build(), p);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setClient(BaseAiClient client, ChessPlayer p) {
        if (p.discordId.equals(whiteSidePlayer.discordId)) {
            client1 = client; //White side player will always be client1
        }
        else if (p.discordId.equals(blackSidePlayer.discordId)) {
            client2 = client; //Black side player will always be client 2
        }
    }

    public void setWhiteSidePlayer(ChessPlayer whiteSidePlayer) {
        this.whiteSidePlayer = whiteSidePlayer;
    }

    public void setBlackSidePlayer(ChessPlayer blackSidePlayer) {
        this.blackSidePlayer = blackSidePlayer;
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

    public String setupPlayers(String message, int elo) {
        String option = message.contains(" ") ? message.substring(0, message.indexOf(" ")).trim() : message;
        String opponent = message.contains(" ") ? message.substring(message.indexOf(" ")).trim() : null;

        if (!option.equals("1") && !option.equals("2") && !option.equals("3")) {
            return new StringBuilder("`").append(message).append("` is not a valid option. Please choose an option (1-3). For 2 or 3, optional add an opponent name ex: `3 Cornelius Stockfish 20`").toString();
        }

        if (message.startsWith("1")) { // Option 1
            return GameType.PVP.toString();
        }

        ChessPlayer player = null;
        if (opponent == null || (opponent != null && opponent.isEmpty())) { //Find a random opponent with a similar elo if possible
            player = findUserByElo(elo); //Should never be null
        }
        else if (option.equals("2") || option.equals("3")){
            player = findUserByName(opponent);
            if (player == null) {
                return new StringBuilder("Opponent by the name of `").append(opponent).append("` does not exist in the database. Try again and/or check the chess record spreadsheets for exact name").toString();
            }
            //Valid opponent
        }

        if (message.startsWith("2")) { // Option 2
            return GameType.PVC.toString() + " " + player.discordId + " " + player.name;
        }
        else { //option 3
            return GameType.CVP.toString() + " " + player.discordId + " " + player.name;
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
            this.board = null;
            this.board = transition.getTransitionBoard();
            this.board.buildImage();

            // Is someone in check mate?
            if (this.board.getCurrentPlayer().isInCheckMate()) {
                state.setStateCheckmate();
                if (didWhiteJustMove()) {
                    state.setMessage("`" + whiteSidePlayer.name + "` has CHECKMATED `" + blackSidePlayer.name + "`");
                    state.setWinnerId(whiteSidePlayer.discordId);
                    updateDatabaseWhiteSideWin();
                }
                else {
                    state.setMessage("`" + blackSidePlayer.name + "` has CHECKMATED `" + whiteSidePlayer.name + "`");
                    state.setWinnerId(blackSidePlayer.discordId);
                    updateDatabaseBlackSideWin();
                }
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
                    state.setMessage("`" + whiteSidePlayer.name + "` SELECTS " + moveCmd + " `" + blackSidePlayer.name + "` is in check!");
                }
                else {
                    state.setMessage("`" + blackSidePlayer.name + "` SELECTS " + moveCmd + " `" + whiteSidePlayer.name + "` is in check!");
                }
                state.setStateCheck();
                return state;
            }

            //Update eval score
            //+position eval is good for white, -negative eval is good for black
            try {
                state.setBoardEvaluationMessage(null);
                String evaluationMessage = stockFishClient.submit(new Query.Builder(QueryType.EVAL).setFen(FenUtils.parseFEN(this.board)).build());
                if (evaluationMessage != null) state.setBoardEvaluationMessage(evaluationMessage.substring(22));
            } catch (Exception e) {
                stockFishClient = null;
                setupStockfishClient();
                e.printStackTrace();
            }

            //Should computer resign?
            if (isComputer && state.getBoardEvaluationMessage() != null) {
                double evaluationScore = Double.parseDouble(state.getBoardEvaluationMessage().replace("(white side)", "").trim());
                if (didWhiteJustMove() && evaluationScore <= -10) {
                    state.setMessage(whiteSidePlayer.name + " has RESIGNED!");
                    state.setStateComputerResign();
                    state.setWinnerId(blackSidePlayer.discordId);
                    state.setPlayerForfeit();
                    updateDatabaseBlackSideWin(true);
                    return state;
                }
                if (didBlackJustMove() && evaluationScore >= 10) {
                    state.setMessage(blackSidePlayer.name + " has RESIGNED!");
                    state.setStateComputerResign();
                    state.setWinnerId(whiteSidePlayer.discordId);
                    state.setPlayerForfeit();
                    updateDatabaseWhiteSideWin(true);
                    return state;
                }
            }

            //If we get to this point then player made a legal move
            if (didWhiteJustMove()) {
                state.setMessage("`" + whiteSidePlayer.name + "` SELECTS " + move.toString());
            }
            else {
                state.setMessage("`" + blackSidePlayer.name + "` SELECTS " + move.toString());
            }
            state.setStateSuccessfulMove();
            transition = null;
            return state;
        }
        else {
            if (transition.getMoveStatus().leavesPlayerInCheck()) {
                state.setMessage("`"+moveCmd + "` leaves " + this.board.getCurrentPlayer().getAlliance() + " player in check");
                state.setStateLeavesPlayerInCheck();
                transition = null;
                return state;
            }
            else {
                state.setMessage("`"+moveCmd + "` is not a legal move for " + this.board.getCurrentPlayer().getAlliance());
                state.setStateIllegalMove();
                transition = null;
                return state;
            }
        }
    }

    public synchronized ChessPlayer findUserByElo(int elo) {
        return db.findUserClosestElo(elo);
    }

    public synchronized ChessPlayer findUserByName(String name) {
        return db.findUserByName(name);
    }

    public synchronized ChessPlayer addUser(String id, String name) {
        return db.addUser(id, name);
    }

    private synchronized void updateDatabaseDraw() {
        whiteSidePlayer.incrementDraws();
        whiteSidePlayer.calculateElo(true, false, blackSidePlayer);
        db.updateUser(whiteSidePlayer);

        blackSidePlayer.incrementDraws();
        blackSidePlayer.calculateElo(true, false, whiteSidePlayer);
        db.updateUser(blackSidePlayer);

        db.addMatch(whiteSidePlayer, blackSidePlayer, state);

        db.updateAvgGameLength(whiteSidePlayer.discordId);
        db.updateAvgGameLength(blackSidePlayer.discordId);
    }

    public synchronized void updateDatabaseWhiteSideWin() {
        updateDatabaseWhiteSideWin(false);
    }

    /**
     * If no moves are made treat this as a draw as long as someone didn't forfeit
     */
    public synchronized void updateDatabaseWhiteSideWin(boolean isForfeit) {
        if (isForfeit || state.getTotalMoves() > 0) {
            whiteSidePlayer.incrementWins();
            whiteSidePlayer.calculateElo(false, true, blackSidePlayer);
            db.updateUser(whiteSidePlayer);

            blackSidePlayer.incrementLosses();
            blackSidePlayer.calculateElo(false, false, whiteSidePlayer);
            db.updateUser(blackSidePlayer);

            db.addMatch(whiteSidePlayer, blackSidePlayer, state);

            db.updateAvgGameLength(whiteSidePlayer.discordId);
            db.updateAvgGameLength(blackSidePlayer.discordId);
        }
        else {
            state.setStateDraw();
            updateDatabaseDraw();
        }
        threadRunning = false;
    }


    public synchronized void updateDatabaseBlackSideWin() {
        updateDatabaseBlackSideWin(false);
    }
    /**
     * If no moves are made treat this as a draw as long as someone didn't forfeit
     */
    public synchronized void updateDatabaseBlackSideWin(boolean isForfeit) {
        if (isForfeit || state.getTotalMoves() > 0) {
            whiteSidePlayer.incrementLosses();
            whiteSidePlayer.calculateElo(false, false, blackSidePlayer);
            db.updateUser(whiteSidePlayer);

            blackSidePlayer.incrementWins();
            blackSidePlayer.calculateElo(false, true, whiteSidePlayer);
            db.updateUser(blackSidePlayer);

            db.addMatch(whiteSidePlayer, blackSidePlayer, state);

            db.updateAvgGameLength(whiteSidePlayer.discordId);
            db.updateAvgGameLength(blackSidePlayer.discordId);
        }
        else {
            state.setStateDraw();
            updateDatabaseDraw();
        }
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

    public ChessGameState ai(int difficulty) {
        long thinkTime = 500;
        String bestMoveString = null;
        do {
            try {
                if (isWhitePlayerTurn()) {
                    bestMoveString = client1.submit(new Query.Builder(QueryType.Best_Move)
                            .setMovetime(thinkTime)
                            .setDifficulty(difficulty)
                            .setFen(FenUtils.parseFEN(this.board)).build());
                }
                else if (isBlackPlayerTurn()) {
                    bestMoveString = client2.submit(new Query.Builder(QueryType.Best_Move)
                            .setMovetime(thinkTime)
                            .setDifficulty(difficulty)
                            .setFen(FenUtils.parseFEN(this.board)).build());
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    if (isWhitePlayerTurn()) {
                        if (client1 != null) client1.close();
                    }
                    else if (isBlackPlayerTurn()) {
                        if (client2 != null) client2.close();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                finally { //If ai breaks then rely on simple ai for rest of the game
                    System.out.println("Using iterative deeping");
                    if (id == null) id = new IterativeDeepening(6);
                    final Move bestMove = id.execute(this.board);
                    bestMoveString = BoardUtils.getPositionAtCoordinate(bestMove.getCurrentCoordinate()) + BoardUtils.getPositionAtCoordinate(bestMove.getDestinationCoordinate());
                    return handleMove(bestMove.getCurrentCoordinate(), bestMove.getDestinationCoordinate(), bestMoveString, true);
                }
            } finally {
                thinkTime *= 2;
            }
        } while (bestMoveString == null);

        String x1Str = Character.toString(bestMoveString.charAt(0));
        String y1Str = Character.toString(bestMoveString.charAt(1));
        String x2Str = Character.toString(bestMoveString.charAt(2));
        String y2Str = Character.toString(bestMoveString.charAt(3));

        ///////////////////////// Get board coordinates from input ////////////////////////////////
        int startCoordinate = convertInputToInteger(x1Str, y1Str);
        int destinationCoordinate = convertInputToInteger(x2Str, y2Str);

        return handleMove(startCoordinate, destinationCoordinate, bestMoveString, true);
    }

    public ChessGameState ai(MessageChannel mc) {
        int randomThinkTime = 5000;//ThreadLocalRandom.current().nextInt(5000, 10000 + 1); //Between 5-10 seconds
        String bestMoveString = null;
        do {
            System.out.println(randomThinkTime);
            try {
                System.out.println("Current player: " + this.board.getCurrentPlayer().getAlliance().toString());

                if (isWhitePlayerTurn()) {
                    System.out.println("White player is choosing their move");
                    bestMoveString = client1.submit(new Query.Builder(QueryType.Best_Move)
                            .setMovetime(randomThinkTime)
                            .setFen(FenUtils.parseFEN(this.board)).build());
                    System.out.println("White players best move is: " + bestMoveString);
                }
                else if (isBlackPlayerTurn()) {
                    System.out.println("Black player is choosing their move");
                    bestMoveString = client2.submit(new Query.Builder(QueryType.Best_Move)
                            .setMovetime(randomThinkTime)
                            .setFen(FenUtils.parseFEN(this.board)).build());
                    System.out.println("Black players best move is: " + bestMoveString);
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    if (isWhitePlayerTurn()) {
                        if (client1 != null) client1.close();
                    }
                    else if (isBlackPlayerTurn()) {
                        if (client2 != null) client2.close();

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                finally {
                    System.out.println("Using iterative deeping");
                    if (id == null) id = new IterativeDeepening(6);
                    final Move bestMove = id.execute(this.board);
                    bestMoveString = BoardUtils.getPositionAtCoordinate(bestMove.getCurrentCoordinate()) + BoardUtils.getPositionAtCoordinate(bestMove.getDestinationCoordinate());
                    return handleMove(bestMove.getCurrentCoordinate(), bestMove.getDestinationCoordinate(), bestMoveString, true);
                }
            } finally {
                randomThinkTime *= 2;
            }
        } while (bestMoveString == null);
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
