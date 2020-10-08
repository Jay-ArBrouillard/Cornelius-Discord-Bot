package chess;

public class ChessGameState {
    private String message;
    private String status;
    private String whitePlayerName;
    private String blackPlayerName;
    private String whitePlayerId;
    private String blackPlayerId;
    private int whitePlayerElo;
    private int blackPlayerElo;
    private long matchStartTime;
    private String boardEvaluationMessage;

    ChessGameState () {
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public String getStatus() {
        return this.status;
    }

    public String getWhitePlayerName() {
        return whitePlayerName;
    }

    public void setWhitePlayerName(String whitePlayerName) {
        this.whitePlayerName = whitePlayerName;
    }

    public String getBlackPlayerName() {
        return blackPlayerName;
    }

    public void setBlackPlayerName(String blackPlayerName) {
        this.blackPlayerName = blackPlayerName;
    }

    public String getWhitePlayerId() {
        return whitePlayerId;
    }

    public void setWhitePlayerId(String whitePlayerId) {
        this.whitePlayerId = whitePlayerId;
    }

    public String getBlackPlayerId() {
        return blackPlayerId;
    }

    public void setBlackPlayerId(String blackPlayerId) {
        this.blackPlayerId = blackPlayerId;
    }

    public int getWhitePlayerElo() {
        return whitePlayerElo;
    }

    public void setWhitePlayerElo(int whitePlayerElo) {
        this.whitePlayerElo = whitePlayerElo;
    }

    public int getBlackPlayerElo() {
        return blackPlayerElo;
    }

    public void setBlackPlayerElo(int blackPlayerElo) {
        this.blackPlayerElo = blackPlayerElo;
    }

    public long getMatchStartTime() {
        return matchStartTime;
    }

    public void setMatchStartTime(long matchStartTime) {
        this.matchStartTime = matchStartTime;
    }

    public String getBoardEvaluationMessage() {
        return boardEvaluationMessage;
    }

    public void setBoardEvaluationMessage(String boardEvaluationMessage) {
        this.boardEvaluationMessage = boardEvaluationMessage;
    }

    public void setStateShowAllLegalMoves() {
        this.status = ChessConstants.SHOW_ALL_LEGAL_MOVES;
    }

    public void setStateShowAllLegalMovesForTile() {
        this.status = ChessConstants.SHOW_ALL_LEGAL_MOVES_FOR_TILE;
    }

    public void setStateError() {
        this.status = ChessConstants.ERROR;
    }

    public void setStateCheck() {
        this.status = ChessConstants.CHECK;
    }

    public void setStateCheckmate() {
        this.status = ChessConstants.CHECKMATE;
    }

    public void setStateShowBoard() {
        this.status = ChessConstants.BOARD;
    }

    public void setStateDraw() {
        this.status = ChessConstants.DRAW;
    }

    public void setStateSuccessfulMove() {
        this.status = ChessConstants.SUCCESSFUL_MOVE;
    }

    public void setStateComputerResign() {
        this.status = ChessConstants.COMPUTER_RESIGN;
    }

    public void setStateLeavesPlayerInCheck() {
        this.status = ChessConstants.MOVE_LEAVES_PLAYER_IN_CHECK;
    }

    public void setStateIllegalMove() {
        this.status = ChessConstants.ILLEGAL_MOVE;
    }
}
