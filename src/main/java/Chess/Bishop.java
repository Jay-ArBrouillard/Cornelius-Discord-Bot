package Chess;

public class Bishop extends Piece {
    public Bishop(boolean white)
    {
        super(white, "Bishop");
        if (white) {
            this.setFilePath("src/main/java/Chess/assets/White_Bishop.png");
        }
        else {
            this.setFilePath("src/main/java/Chess/assets/Black_Bishop.png");
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
