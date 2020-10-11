package chess;

import java.util.HashMap;
import java.util.Map;

public class ChessGameState {
    private String message;
    private String status;
    private Map<String, Integer> prevElo; //PlayerId -> Elo
    private long matchStartTime;
    private String boardEvaluationMessage;
    private double totalMoves = 0; //Half moves that's why we need a double
    private String winnerId;

    public ChessGameState() {
        prevElo = new HashMap<>();
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

    public Map<String, Integer> getPrevElo() {
        return prevElo;
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

    public String getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }

    public double getTotalMoves() {
        return totalMoves;
    }

    public void setTotalMoves(double totalMoves) {
        this.totalMoves = totalMoves;
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

    public void setStateWaitingAcceptChallenge() {
        this.status = ChessConstants.WAITING_ACCEPT_CHALLENGE;
    }

    public void setStateChallengeeDecline() {
        this.status = ChessConstants.CHALLENGEE_DECLINE;
    }
}
