package Chess;

public class Rook extends Piece {
    public Rook(boolean white)
    {
        super(white, "Rook");
        if (white) {
            this.setFilePath("src/main/java/Chess/assets/White_Rook.png");
        }
        else {
            this.setFilePath("src/main/java/Chess/assets/Black_Rook.png");
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
