package Chess;

public class Pawn extends Piece {
    private boolean firstMove = true;

    public Pawn(boolean white)
    {
        super(white, "Pawn");
        if (white) {
            this.setFilePath("src/main/java/Chess/assets/White_Pawn.png");
        }
        else {
            this.setFilePath("src/main/java/Chess/assets/Black_Pawn.png");
        }
    }

    @Override
    public boolean canMove(Board board, Spot start,
                           Spot end)
    {
        int x = Math.abs(start.getX() - end.getX());
        int y = Math.abs(start.getY() - end.getY());

        //Vertical move check
        if (this.isFirstMove() && y > 2) {
            return false;
        }
        if (!this.isFirstMove() && y > 1)
        {
            return false;
        }

        //Horizontal move check. Can't move side ways unless capturing a piece
        if (x > 1)
        {
            return false;
        }
        if (x == 1 && end.getPiece() == null)
        {
            return false;
        }

        if (start.getPiece().isWhite()) //White piece can only move up
        {
            if (end.getY() < start.getY())
            {
                return false;
            }
        }
        else //Black piece can only move down
        {
            if (end.getY() > start.getY())
            {
                return false;
            }
        }

        return true;
    }

    public boolean isFirstMove() {
        return firstMove;
    }

    public void setFirstMove(boolean firstMove) {
        this.firstMove = firstMove;
    }

    public String toString() {
        return getName();
    }
}
