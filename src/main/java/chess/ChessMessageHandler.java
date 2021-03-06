package chess;

import chess.board.BoardUtils;
import chess.board.Tile;
import chess.player.Player;

public class ChessMessageHandler {

    public StringBuilder lastErrorMessage;
    public final String NO_ERROR = "NO_ERROR";
    public final String ERROR = "ERROR";
    public final String PVP = "PVP";
    public String [] promotionTypes = {"q", "r", "b", "n"};


    public ChessMessageHandler() {
        lastErrorMessage = new StringBuilder();
    }

    public String getLastErrorMessage() {
        String saveMsg = lastErrorMessage.toString();
        lastErrorMessage = null;
        return saveMsg;
    }

    public boolean showAllLegalMoves(String input) {
        if (input.equalsIgnoreCase("help")) {
            return true;
        }
        return false;
    }

    public boolean showAllLegalMovesForTile(String input) {
        if (input.contains("help")) {
            String removeHelpString = input.replace("help", "");
            String filteredInput = removeHelpString.replaceAll("\\s+", "");
            if (filteredInput.length() != 2) {
                lastErrorMessage = new StringBuilder("Invalid format! To get possible moves for specified tile. Try `help` followed by tile position. Ex: `helpc2` or `help c2`");
                return false;
            }
            if (BoardUtils.POSITION_TO_COORDINATE.get(filteredInput) == null) {
                lastErrorMessage = new StringBuilder("Must use letters from (a - h) for column and numbers from (1 - 8) for row");
                return false;
            }
            return true;
        }
        return false;
    }

    public void validateComputerDifficulty(String input) {
        try {
            int num = Integer.parseInt(input);
            if (num < 0 || num > 20) {
                lastErrorMessage = new StringBuilder("Difficulty level must be between 0 and 20.");
            }
        } catch (NumberFormatException e) {
            lastErrorMessage = new StringBuilder("Invalid difficulty format. Re-enter a difficulty level between 0 and 20.");
        }
    }

    public void validateInput(String input) {
        if (input.equalsIgnoreCase("o-o") || input.equalsIgnoreCase("o-o-o")) {
            return;
        }
        if (input.length() == 5) {
            String promotionType = Character.toString(input.charAt(4));
            boolean success = false;
            for (String e : promotionTypes) {
                if (e.equals(promotionType)) {
                    success = true;
                    break;
                }
            }
            if (!success) {
                lastErrorMessage = new StringBuilder("Invalid move input format for promotion. Promotion type must be one of " + promotionTypes.toString() + " Ex: `e7e8q`");
                return;
            }
        } else if (input.length() != 4) {
            lastErrorMessage = new StringBuilder("Invalid move input format. Use format such as `c2 c4` or `c2c4` or `e7e8q` for promotion or castling notation");
        }
    }

    public void validateRowAndColumn(String firstPosition, String secondPosition) {
        if (BoardUtils.POSITION_TO_COORDINATE.get(firstPosition) == null || BoardUtils.POSITION_TO_COORDINATE.get(secondPosition) == null) {
            lastErrorMessage = new StringBuilder("Must use letters from (a - h) for column and numbers from (1 - 8) for row");
        }
    }

    public void validateIsLegalMove(String position, Tile tileAtStart, Player currentPlayer) {
        if (tileAtStart.isTileOccupied() && !tileAtStart.getPiece().getPieceAlliance().equals(currentPlayer.getAlliance())) {
            lastErrorMessage = new StringBuilder("It is `").append(currentPlayer.getAlliance()).
                    append("` player's turn. Please move a ").
                    append(currentPlayer.getAlliance()).
                    append(" piece");
        }
        else if (!tileAtStart.isTileOccupied()) {
            lastErrorMessage = new StringBuilder("No piece at `").append(position).append("`");
        }
    }

    public String handleErrorMessage() {
        if (lastErrorMessage != null && !lastErrorMessage.toString().isEmpty()) {
            return ERROR;
        }
        return NO_ERROR;
    }
}
