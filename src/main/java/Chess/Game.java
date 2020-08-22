package Chess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Game {
    public Player[] players = new Player[2];
    private Board board = new Board();
    private Player currentTurn;
    private GameStatus status = GameStatus.INACTIVE;
    private List<Move> movesPlayed = new ArrayList<>();
    private final List<String> validXPositions = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h");
    private final List<String> validYPositions = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8");

    public void initialize(Player p1, Player p2) throws IOException {
        players[0] = p1;
        players[1] = p2;

        board.resetBoard();
//        board.resetBoardImage();
        board.buildImage();

        if (p1.isWhiteSide()) {
            this.currentTurn = p1;
        }
        else {
            this.currentTurn = p2;
        }

        movesPlayed.clear();
    }

    public boolean isEnd()
    {
        return true; //TODO FIX
    }

    public GameStatus getStatus()
    {
        return this.status;
    }

    public void setStatus(GameStatus status)
    {
        this.status = status;
    }

    public String processMoveInput(String input)
    {
        String filtered = input.replaceAll("\\s+", "");
        if (filtered.length() != 4) {
            return "Invalid move input format. Use format such as `c2 c4` or `c2c4`";
        }

        String x1Str = Character.toString(filtered.charAt(0)).toLowerCase();
        String y1Str = Character.toString(filtered.charAt(1)).toLowerCase();
        String x2Str = Character.toString(filtered.charAt(2)).toLowerCase();
        String y2Str = Character.toString(filtered.charAt(3)).toLowerCase();

        if (!validXPositions.contains(x1Str) || !validXPositions.contains(x2Str) || !validYPositions.contains(y1Str) || !validYPositions.contains(y2Str)) {
            return "Must use letters from a - h for x positions and numbers from 1 - 8 for y positions";
        }

        //Convert input
        int x1 = convertInputToInteger(x1Str);
        int y1 = convertInputToInteger(y1Str);
        int x2 = convertInputToInteger(x2Str);
        int y2 = convertInputToInteger(y2Str);

        Spot sourceSpot = board.getBox(y1, x1);
        Spot targetSpot = board.getBox(y2, x2);

        boolean valid = isValidMove(sourceSpot, targetSpot);

        if (valid)
        {
            //Move pieces on the board
            targetSpot.setPiece(sourceSpot.getPiece());
            if (sourceSpot.getPiece() instanceof Pawn)
            {
                ((Pawn) sourceSpot.getPiece()).setFirstMove(false);
            }

            //Build Success message
            StringBuilder sb = new StringBuilder("Moving `");
            sb.append(sourceSpot.getPiece().getName()).append("` to `");
            sb.append(x2Str).append(y2Str).append("`");

            sourceSpot.setPiece(null);

            //Update the image itself
            board.buildImage();
            return "Success!" + sb.toString();
        }
        else
        {
            //Build fail message
            StringBuilder sb = new StringBuilder("Invalid move. ");
            if (sourceSpot.getPiece() == null) {
                sb.append("No piece at `").append(x1Str).append(y1Str).append("`");
            }
            else {
                sb.append("Cannot move `");
                sb.append(sourceSpot.getPiece().getName()).append("` at `");
                sb.append(x1Str).append(y1Str).append("` to `");
                sb.append(x2Str).append(y2Str).append("`");
            }

            return sb.toString();
        }
    }

    private int convertInputToInteger(String value)
    {
        int intValue = -1;
        try {
            intValue = Integer.parseInt(value);
            //If we get here then the position on the 2d matrix is 1 less
            if (intValue >= 1 && intValue <= 8) {
                intValue -= 1;
            }
        } catch(NumberFormatException e){
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
        }

        return intValue;
    }

    public boolean isValidMove(Spot sourceSpot, Spot targetSpot) {
        Piece piece = sourceSpot.getPiece();

        boolean valid = false;
        if (piece instanceof Pawn) {
            Pawn curr = (Pawn) piece;
            valid = curr.canMove(board, sourceSpot, targetSpot);
        }
        else if (piece instanceof Rook) {
            Rook curr = (Rook) piece;
            valid = curr.canMove(board, sourceSpot, targetSpot);
        }
        else if (piece instanceof Knight) {
            Knight curr = (Knight) piece;
            valid = curr.canMove(board, sourceSpot, targetSpot);
        }
        else if (piece instanceof Bishop) {
            Bishop curr = (Bishop) piece;
            valid = curr.canMove(board, sourceSpot, targetSpot);
        }
        else if (piece instanceof Queen) {
            Queen curr = (Queen) piece;
            valid = curr.canMove(board, sourceSpot, targetSpot);
        }
        else if (piece instanceof King) {
            King curr = (King) piece;
            valid = curr.canMove(board, sourceSpot, targetSpot);
        }

        return valid;
    }

    public boolean playerMove(Player player, int startX,
                              int startY, int endX, int endY) throws Exception {
        Spot startBox = board.getBox(startX, startY);
        Spot endBox = board.getBox(startY, endY);
        Move move = new Move(player, startBox, endBox);
        return this.makeMove(move, player);
    }

    public boolean makeMove(Move move, Player player)
    {


        return true;
    }

    public Player getCurrentTurn()
    {
        return this.getCurrentTurn();
    }
}
