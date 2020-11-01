package chess;

import java.util.HashMap;
import java.util.Map;

public class ChessGameState {
    private String message;
    private String status;
    private Map<String, Double> prevElo; //PlayerId -> Elo
    private Long matchStartTime;
    private String boardEvaluationMessage;
    private int fullMoves = 1; //The number of the full move. It starts at 1, and is incremented after Black's move.
    private String winnerId;
    private boolean playerForfeited = false;

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

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, Double> getPrevElo() {
        return prevElo;
    }

    public Long getMatchStartTime() {
        return matchStartTime;
    }

    public void setMatchStartTime(Long matchStartTime) {
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

    public int getFullMoves() {
        return fullMoves;
    }

    public void setFullMoves(int fullMoves) {
        this.fullMoves = fullMoves;
    }

    public boolean isPlayerForfeited() {
        return playerForfeited;
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

    public void setStateInvalidPawnPromotionType() { this.status = ChessConstants.INVALID_PAWN_PROMOTION_TYPE; }

    public void setPlayerForfeit() {
        this.playerForfeited = true;
    }

}
