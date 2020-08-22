package Chess;

public class Knight extends Piece {
    public Knight(boolean white)
    {
        super(white, "Knight");
        if (white) {
            this.setFilePath("src/main/java/Chess/assets/White_Knight.png");
        }
        else {
            this.setFilePath("src/main/java/Chess/assets/Black_Knight.png");
        }
    }

    @Override
    public boolean canMove(Board board, Spot start,
                           Spot end)
    {
        // we can't move the piece to a spot that has
        // a piece of the same colour
        if (end.getPiece().isWhite() == this.isWhite()) {
            return false;
        }

        return true;
    }
}