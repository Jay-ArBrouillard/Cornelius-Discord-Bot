package Chess;


public class Queen extends Piece {
    public Queen(boolean white)
    {
        super(white, "Queen");
        if (white) {
            this.setFilePath("src/main/java/Chess/assets/White_Queen.png");
        }
        else {
            this.setFilePath("src/main/java/Chess/assets/Black_Queen.png");
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
