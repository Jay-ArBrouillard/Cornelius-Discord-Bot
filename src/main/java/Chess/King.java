package Chess;

public class King extends Piece {
    private boolean castlingDone = false;

    public King(boolean white)
    {
        super(white, "King");
        if (white) {
            this.setFilePath("src/main/java/Chess/assets/White_King.png");
        }
        else {
            this.setFilePath("src/main/java/Chess/assets/Black_King.png");
        }
    }

    public boolean isCastlingDone()
    {
        return this.castlingDone == true;
    }

    public void setCastlingDone(boolean castlingDone)
    {
        this.castlingDone = castlingDone;
    }

    @Override
    public boolean canMove(Board board, Spot start, Spot end)
    {
        // we can't move the piece to a Spot that
        // has a piece of the same color
        if (end.getPiece().isWhite() == this.isWhite()) {
            return false;
        }

        return true;
    }

    private boolean isValidCastling(Board board,
                                    Spot start, Spot end)
    {

        if (this.isCastlingDone()) {
            return false;
        }

        // Logic for returning true or false
        return true;
    }

    public boolean isCastlingMove(Spot start, Spot end)
    {
        // check if the starting and
        // ending position are correct
        return true;
    }
}

