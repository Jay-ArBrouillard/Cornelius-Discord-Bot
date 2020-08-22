package Chess;

public abstract class Player {
    public boolean whiteSide;
    public boolean humanPlayer;
    public int x;
    public int y;

    public boolean isWhiteSide()
    {
        return this.whiteSide == true;
    }
    public boolean isHumanPlayer()
    {
        return this.humanPlayer == true;
    }
}