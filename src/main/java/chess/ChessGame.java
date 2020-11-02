package chess;

import utils.EloRanking;
import utils.GoogleSheets;
import chess.board.Board;
import chess.board.Move;
import chess.board.Tile;
import chess.pgn.FenUtils;
import chess.pieces.Piece.PieceType;
import chess.player.MoveTransition;
import chess.player.ai.uci.client.*;
import chess.player.ai.uci.engine.enums.Option;
import chess.player.ai.uci.engine.enums.Query;
import chess.player.ai.uci.engine.enums.QueryType;
import chess.player.ai.uci.engine.enums.Variant;
import chess.tables.ChessPlayer;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ChessGame {
    public Board board;
    public ChessMessageHandler messageHandler;
    public GoogleSheets db;
    public ChessGameState state;
    public ChessPlayer whiteSidePlayer;
    public ChessPlayer blackSidePlayer;
    public boolean threadRunning = false;
    public StockFishClient stockFishClient;
    public BaseAiClient client1;
    public BaseAiClient client2;
    public GameType gameType;
    private boolean DEBUG = true;

    public ChessGame(ChessGameState state) {
        db = new GoogleSheets();
        board = Board.createStandardBoard();
        messageHandler = new ChessMessageHandler();
        this.state = state;
    }

    public void setupStockfishClient() {
        try {
            stockFishClient = new StockFishClient.Builder()
                                .setOption(Option.Hash, 2)
                                .setVariant(Variant.MODERN)  // BMI for windows, Modern for linux
                                .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setupComputerClient(GameType gameType) throws IOException {
        ChessPlayer [] players;
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
            client1 = null;
            client2 = null;
            return;
        }
        for (ChessPlayer p : players) {
            setClient(p);
        }
    }

    private void setClient(ChessPlayer p) throws IOException {
        if (p.name.contains("Cornelius")) { //Cornelius will default to use stockfish client
            setClient(new StockFishClient.Builder()
                    .setOption(Option.Hash, 32)
                    .setVariant(Variant.MODERN) // BMI for windows, Modern for linux
//                    .setOption(Option.Limit_Strength, Boolean.TRUE)
//                    .setOption(Option.Elo, p.name.split("Cornelius ")[1]) //Elo Skill level is in their name
                    .build(), p);
        }
        else if (p.discordId.endsWith("RO4")) { //Rodent IV
            String [] name = p.name.split(" ");
            setClient(new RodentClient.Builder()
                    .setOption(Option.Personality_File, name.length == 1 ? findPersonalityFileLocation(p.name) : findPersonalityFileLocation(name[1]))
                    .build(), p);
        }
        else if (p.name.contains("Cheng")) {
            setClient(new ChengClient.Builder()
                    .setOption(Option.Hash, 32)
//                    .setOption(Option.Limit_Strength, Boolean.TRUE)
//                    .setOption(Option.Elo, p.name.split("Cheng ")[1]) //Elo Skill level is in their name
                    .build(), p);
        }
        else if (p.name.contains("Fishnet")) {
            setClient(new FishnetClient.Builder()
                    .setOption(Option.Hash, 32)
//                    .setOption(Option.Limit_Strength, Boolean.TRUE)
//                    .setOption(Option.Elo, p.name.split("Fishnet ")[1]) //Elo Skill level is in their name
                    .build(), p);
        }
        else if (p.name.contains("CT800")) {
            setClient(new CT800Client.Builder()
                    .setOption(Option.Hash, 32)
//                    .setOption(Option.Limit_Strength, Boolean.TRUE)
//                    .setOption(Option.Elo, p.name.split("CT800 ")[1]) //Elo Skill level is in their name
                    .build(), p);
        }
        else if (p.name.contains("Xiphos")) {
            setClient(new XiphosClient.Builder()
                    .setVariant(Variant.SSE) //BMI for windows, SSE for linux
                    .setOption(Option.Hash, 32)
                    .build(), p);
        }
        else if (p.name.contains("Komodo")) {
            setClient(new KomodoClient.Builder()
                    //Komodo defaults table memory to 128mb. Not sure if this is problematic yet
                    .setOption(Option.Book_File, "/app/bin/books/elo-2650.bin")
                    .setOption(Option.Hash, 32)
                    .build(), p);
        }
        else if (p.name.contains("Cinnamon")) {
            setClient(new CinnamonClient.Builder()
                    .setOption(Option.Hash, 32)
                    .build(), p);
        }
        else if (p.name.contains("Laser")) {
            setClient(new LaserClient.Builder()
                    .setOption(Option.Hash, 32)
                    .build(), p);
        }
        else if (p.name.contains("Amoeba")) {
            setClient(new AmoebaClient.Builder()
                    .setOption(Option.Hash, 32)
                    .build(), p);
        }
        else if (p.name.contains("CounterGo")) {
            setClient(new CounterGoClient.Builder()
                    .setOption(Option.Hash, 32)
                    .build(), p);
        }
        else if (p.name.contains("Asymptote")) {
            setClient(new AsymptoteClient.Builder()
                    .setOption(Option.Hash, 32)
                    .build(), p);
        }
        else if (p.name.contains("Dumb")) {
            setClient(new DumbClient.Builder()
                    .setOption(Option.Hash, 32)
                    .build(), p);
        }
        else if (p.name.contains("Pigeon")) {
            setClient(new PigeonClient.Builder()
                    .setOption(Option.Hash, 32)
                    .build(), p);
        }
        else if (p.name.contains("Monolith")) {
            setClient(new MonolithClient.Builder()
                    .setOption(Option.Hash, 32)
                    .build(), p);
        }
        else if (p.name.contains("Floyd")) {
            setClient(new FloydClient.Builder()
                    .setOption(Option.Hash, 32)
                    .build(), p);
        }
        else if (p.name.contains("Fridolin")) {
            setClient(new FridolinClient.Builder()
                    .setOption(Option.Hash, 32)
                    .build(), p);
        }
        else if (p.name.contains("LittleWing")) {
            setClient(new LittleWingClient.Builder()
                    .setOption(Option.Hash, 32)
                    .build(), p);
        }
        else if (p.name.contains("Moustique")) {
            setClient(new MoustiqueClient.Builder()
                    //Don't have any uci options
                    .build(), p);
        }
        else if (p.name.contains("Ethereal")) {
            setClient(new EtherealClient.Builder()
                    .setOption(Option.Hash, 32)
                    .build(), p);
        }
        else if (p.name.contains("Claudia")) {
            setClient(new ClaudiaClient.Builder()
                    //Claudia only has Hash option and sets it 32 by default (min value aswell)
                    .build(), p);
        }
        else if (p.name.contains("Randy Random")) {
            setClient(new RandyRandomClient(), p);
        }
        else if (p.name.contains("Stewart")) {
            setClient(new StewartClient(4), p);
        }
        else if (p.name.contains("Bartholomew")) {
            setClient(new StewartClient(4), p);
        }
        else if (p.name.contains("Sugar")) {
            setClient(new SugarClient.Builder()
                    .setOption(Option.Hash, 32)
                    .build(), p);
        }
        else if (p.name.contains("Cheese")) {
            setClient(new CheeseClient.Builder()
                    .setOption(Option.Hash, 32)
                    .build(), p);
        }
        else if (p.name.contains("Fruit")) {
            setClient(new FruitClient.Builder()
                    .setOption(Option.Hash, 32)
                    .build(), p);
        }
        else if (p.name.contains("Donna")) {
            setClient(new DonnaClient.Builder()
                    .setOption(Option.Hash, 32)
                    .build(), p);
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

    private String findPersonalityFileLocation(String name) {
        File f = new File("/app/bin/personalities");
        String fileName = (name+".txt").toLowerCase(); //Force lowercase here
        Collection<File> allFiles = listFileTree(f);
        for (File file : allFiles) {
            if (fileName.equals(file.getName())) {
                return file.getPath();
            }
        }
        throw new RuntimeException("Could not find personality file for " + name);
    }

    public static Collection<File> listFileTree(File dir) {
        Set<File> fileTree = new HashSet<>();
        if(dir==null||dir.listFiles()==null){
            return fileTree;
        }
        for (File entry : dir.listFiles()) {
            if (entry.isFile()) fileTree.add(entry);
            else fileTree.addAll(listFileTree(entry));
        }
        return fileTree;
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

    public String setupPlayers(MessageChannel mc, String message, double elo, String id) {
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
            mc.sendTyping().queue();
            player = findOpponentSimilarElo(elo, id, 50); //Should never be null
        }
        else if (option.equals("2") || option.equals("3")){
            mc.sendTyping().queue();
            player = findUserByName(opponent);
            if (player == null) {
                return new StringBuilder("Opponent by the name of `").append(opponent).append("` does not exist in the database. Try again and/or check the chess record spreadsheets for exact name").toString();
            }
            //Ensure opponent is a bot
            if (!player.discordId.startsWith(System.getenv("OWNER_ID"))) {
                return new StringBuilder("Opponent by the name of `").append(opponent).append("` must be an AI such as `Cornelius 20`. Try again.").toString();
            }
        }

        if (message.startsWith("2")) { // Option 2
            return GameType.PVC.toString() + "-" + player.discordId + "-" + player.name;
        }
        else { //option 3
            return GameType.CVP.toString() + "-" + player.discordId + "-" + player.name;
        }
    }

    public ChessPlayer findOpponentSimilarElo(double elo, String id, double range) {
        return findUserBySimilarElo(elo, id, range);
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
        //Is castling notation?
        if (inputNoSpaces.equalsIgnoreCase("o-o") || inputNoSpaces.equalsIgnoreCase("o-o-o")) {
            return convertCastlingMove(inputNoSpaces, false);
        }

        messageHandler.validateInput(input);
        if (messageHandler.handleErrorMessage().equals(messageHandler.ERROR)) {
            state.setMessage(messageHandler.getLastErrorMessage());
            state.setStateError();
            return state;
        }

        String x1Str = Character.toString(inputNoSpaces.charAt(0)).toLowerCase();
        String y1Str = Character.toString(inputNoSpaces.charAt(1)).toLowerCase();
        String x2Str = Character.toString(inputNoSpaces.charAt(2)).toLowerCase();
        String y2Str = Character.toString(inputNoSpaces.charAt(3)).toLowerCase();
        String promotionType = null;
        if (inputNoSpaces.length() == 5) { //Example: e7e8q
            promotionType = Character.toString(inputNoSpaces.charAt(4)).toLowerCase();
        }

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

        return handleMove(startCoordinate, destinationCoordinate, inputNoSpaces, promotionType, false);
    }

    private ChessGameState convertCastlingMove(String moveCmd, boolean isComputer) {
        //Handle if player sends "O-O" or "O-O-O" for castling
        int startCoordinate;
        int destinationCoordinate;
        if (moveCmd.equalsIgnoreCase("o-o")) {
            if (this.board.getCurrentPlayer().getAlliance().isWhite()) {
                startCoordinate = 60;
                destinationCoordinate = 62;
            }
            else {
                startCoordinate = 4;
                destinationCoordinate = 6;
            }
        }
        else if (moveCmd.equalsIgnoreCase("o-o-o")) {
            if (this.board.getCurrentPlayer().getAlliance().isWhite()) {
                startCoordinate = 60;
                destinationCoordinate = 58;
            }
            else {
                startCoordinate = 4;
                destinationCoordinate = 2;
            }
        }
        else {
            throw new RuntimeException("Not a castle move");
        }
        return handleMove(startCoordinate, destinationCoordinate, moveCmd, null, isComputer);
    }

    private ChessGameState handleMove(int startCoordinate, int destinationCoordinate, String moveCmd, String promotionType, boolean isComputer) {
        Move move = Move.MoveFactory.createMove(this.board, startCoordinate, destinationCoordinate);
        if (promotionType != null)  {
            boolean success = setPromotionType(promotionType, move);
            if (!success) {
                state.setStateInvalidPawnPromotionType();
                state.setMessage("Invalid promotion type `" + promotionType + "`. Valid types include q (Queen), r (Rook), n (Knight), or b (Bishop).");
                return state;
            }
        }
        MoveTransition transition = this.board.getCurrentPlayer().makeMove(move);
        if (transition.getMoveStatus().isDone()) {
            state.setFullMoves(this.board.getNumFullMoves());
            this.board = null;
            this.board = transition.getTransitionBoard();
            if (!gameType.isComputerVsComputer()) this.board.buildImage(); //Only build board image when human player is playing

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
                e.printStackTrace();
                try {
                    if (stockFishClient != null) stockFishClient.close();
                } catch (IOException ie) {
                    //Do nothing close fails
                }
                finally {
                    stockFishClient = null;
                    setupStockfishClient();
                }
            }

            //Should computer resign?
            if (isComputer && state.getBoardEvaluationMessage() != null) {
                double evaluationScore = Double.parseDouble(state.getBoardEvaluationMessage().replace("(white side)", "").trim());
                if (didWhiteJustMove() && evaluationScore <= -12) {
                    state.setMessage(whiteSidePlayer.name + " has RESIGNED!");
                    state.setStateComputerResign();
                    state.setWinnerId(blackSidePlayer.discordId);
                    state.setPlayerForfeit();
                    updateDatabaseBlackSideWin(true);
                    return state;
                }
                if (didBlackJustMove() && evaluationScore >= 12) {
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
            if (DEBUG && isComputer) {
                System.out.println(String.format("Start coordinate: %d, Destination coordinate: %d, Move command: %s, Promotion Type: %s", startCoordinate, destinationCoordinate, moveCmd, promotionType));
                System.out.println(String.format("Current fen:%s", FenUtils.parseFEN(this.board)));
                if (isWhitePlayerTurn()) {
                    System.out.println(client1 + " just sent an move that leaves player in check or an illegal move");
                }
                else {
                    System.out.println(client2 + " just sent an move that leaves player in check or an illegal move");
                }
            }

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

    private boolean setPromotionType(String promotionType, Move move) {
        if (promotionType.equals("q")) {
            move.setPromotionType(PieceType.QUEEN);
        }
        else if (promotionType.equals("r")) {
            move.setPromotionType(PieceType.ROOK);
        }
        else if (promotionType.equals("n")) {
            move.setPromotionType(PieceType.KNIGHT);
        }
        else if (promotionType.equals("b")) {
            move.setPromotionType(PieceType.BISHOP);
        }
        else {
            return false;
        }
        return true;
    }

    public synchronized ChessPlayer findUserByClosestElo(double elo, String id) {
        return db.findUserClosestElo(elo, id);
    }

    public synchronized ChessPlayer findUserBySimilarElo(double elo, String id, double range) {
        return db.findUserSimilarElo(elo, id, range);
    }

    public synchronized ChessPlayer findUserByName(String name) {
        return db.findUserByName(name);
    }

    public synchronized ChessPlayer addUser(String id, String name) {
        return db.addUser(id, name);
    }

    public synchronized List<List<Object>> getAllUsers() {
        return db.getAllUsers();
    }

    private synchronized void updateDatabaseDraw() {
        threadRunning = true;
        whiteSidePlayer.incrementDraws();
        blackSidePlayer.incrementDraws();
        EloRanking.calculateChessElo(state, whiteSidePlayer, blackSidePlayer);
        db.updateUser(whiteSidePlayer);
        if (gameType.isComputerVsComputer()) sleep(3000);
        db.updateUser(blackSidePlayer);
        if (gameType.isComputerVsComputer()) sleep(3000);
        db.addMatch(whiteSidePlayer, blackSidePlayer, state);
        threadRunning = false;
    }

    public synchronized void updateDatabaseWhiteSideWin() {
        updateDatabaseWhiteSideWin(false);
    }

    /**
     * If no moves are made treat this as a draw as long as someone didn't forfeit
     */
    public synchronized void updateDatabaseWhiteSideWin(boolean isForfeit) {
        threadRunning = true;
        if (isForfeit || state.getFullMoves() > 1) {
            whiteSidePlayer.incrementWins();
            blackSidePlayer.incrementLosses();
            EloRanking.calculateChessElo(state, whiteSidePlayer, blackSidePlayer);
            db.updateUser(whiteSidePlayer); // Executes 3 requests
            if (gameType.isComputerVsComputer()) sleep(3000);
            db.updateUser(blackSidePlayer); // Executes 3 requests
            if (gameType.isComputerVsComputer()) sleep(3000);
            db.addMatch(whiteSidePlayer, blackSidePlayer, state); //Executes 5 requests
        }
        else { // This is should never happen when computer vs computer
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
        threadRunning = true;
        if (isForfeit || state.getFullMoves() > 1) {
            whiteSidePlayer.incrementLosses();
            blackSidePlayer.incrementWins();
            EloRanking.calculateChessElo(state, whiteSidePlayer, blackSidePlayer);
            db.updateUser(whiteSidePlayer);
            if (gameType.isComputerVsComputer()) sleep(3000);
            db.updateUser(blackSidePlayer);
            if (gameType.isComputerVsComputer()) sleep(3000);
            db.addMatch(whiteSidePlayer, blackSidePlayer, state);
        }
        else { // This is should never happen when computer vs computer
            state.setStateDraw();
            updateDatabaseDraw();
        }
        threadRunning = false;
    }

    public void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
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
        int randomThinkTime = mc == null ? 1000 : ThreadLocalRandom.current().nextInt(5000, 10000 + 1); //Between 5-10 seconds against human
        String bestMoveString = null;
        try {
            if (isWhitePlayerTurn()) {
                if (client1 instanceof RandyRandomClient || client1 instanceof StewartClient || client1 instanceof BartholomewClient) {
                    bestMoveString = client1.submit(new Query.Builder(QueryType.Best_Move)
                            .setMovetime(randomThinkTime)
                            .setBoard(this.board).build());
                }
                else {
                    bestMoveString = client1.submit(new Query.Builder(QueryType.Best_Move)
                            .setMovetime(randomThinkTime)
                            .setFen(FenUtils.parseFEN(this.board)).build());
                }
            }
            else if (isBlackPlayerTurn()) {
                if (client2 instanceof RandyRandomClient || client2 instanceof StewartClient || client2 instanceof BartholomewClient) {
                    bestMoveString = client2.submit(new Query.Builder(QueryType.Best_Move)
                            .setMovetime(randomThinkTime)
                            .setBoard(this.board).build());
                }
                else {
                    bestMoveString = client2.submit(new Query.Builder(QueryType.Best_Move)
                            .setMovetime(randomThinkTime)
                            .setFen(FenUtils.parseFEN(this.board)).build());
                }
            }
        } catch (Exception e) {
            //Do nothing here Handling outside of catch
        }

        boolean error1 = gameType.isComputerVsComputer() && (client1 == null || client2 == null);
        boolean error2 = gameType.isPlayerVsComputer() && client1 == null && client2 == null;
        if (error1 || error2 || bestMoveString == null || bestMoveString.isEmpty()) {
            if (isWhitePlayerTurn()) {
                System.out.println(String.format("client:%s, bestMoveString:%s, fen:%s", client1, bestMoveString, FenUtils.parseFEN(this.board)));
                state.setMessage("Error forcing game to end with no consequences. " + client1 + " was not able initialize or find a move");
            }
            else {
                System.out.println(String.format("client:%s, bestMoveString:%s, fen:%s", client2, bestMoveString, FenUtils.parseFEN(this.board)));
                state.setMessage("Error forcing game to end with no consequences. " + client2 + " was not able initialize or find a move");
            }
            client1 = null;
            client2 = null;
            state.setStateError();
            return state;
        }

        //Parse bestMoveString for taunt messages
        //Rodent Taunt example: "e5e7##tauntString"
        String tauntMsg = null;
        if (bestMoveString.contains("##")) {
            String[] split = bestMoveString.split("##");
            bestMoveString = split[0];
            if (split.length == 2) tauntMsg = split[1];
        }

        if (mc != null)  {
            mc.sendTyping().queue();
            //If computer is winning by 5 points there is a 50% chance he will send a taunt message
            if (tauntMsg != null && Math.random() >= 0.5 && state.getBoardEvaluationMessage() != null) {
                double evaluationScore = Double.parseDouble(state.getBoardEvaluationMessage().replace("(white side)", "").trim());
                if ((isWhitePlayerTurn() && evaluationScore <= -5.0) || (isBlackPlayerTurn() && evaluationScore >= 5.0)) {
                    String fullMessage = isWhitePlayerTurn() ? "`"+whiteSidePlayer.name+"` -" + tauntMsg : "`"+blackSidePlayer.name+"`: " + tauntMsg;
                    mc.sendMessage(fullMessage).queue();
                }
            }
        }
        //Is castling notation?
        bestMoveString = bestMoveString.trim().toLowerCase(); //Always convert best move to lowercase

        if (bestMoveString.equalsIgnoreCase("o-o") || bestMoveString.equalsIgnoreCase("o-o-o")) {
            return convertCastlingMove(bestMoveString, true);
        }

        String x1Str = Character.toString(bestMoveString.charAt(0));
        String y1Str = Character.toString(bestMoveString.charAt(1));
        String x2Str = Character.toString(bestMoveString.charAt(2));
        String y2Str = Character.toString(bestMoveString.charAt(3));
        String promotionType = null;
        if (bestMoveString.length() == 5) { //Example: e7e8q for queen promotion
            promotionType = Character.toString(bestMoveString.charAt(4)).toLowerCase();
        }

        ///////////////////////// Get board coordinates from input ////////////////////////////////
        int startCoordinate = convertInputToInteger(x1Str, y1Str);
        int destinationCoordinate = convertInputToInteger(x2Str, y2Str);

        return handleMove(startCoordinate, destinationCoordinate, bestMoveString, promotionType, true);
    }
}
