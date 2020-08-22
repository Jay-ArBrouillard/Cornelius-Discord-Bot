package Chess;

public abstract class Piece {
    private String name;
    private String filePath;
    private boolean killed = false;
    private boolean white = false;

    public Piece(boolean white, String name)
    {
        this.setWhite(white);
        this.setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isWhite()
    {
        return this.white == true;
    }

    public void setWhite(boolean white)
    {
        this.white = white;
    }

    public boolean isKilled()
    {
        return this.killed == true;
    }

    public void setKilled(boolean killed)
    {
        this.killed = killed;
    }

    public abstract boolean canMove(Board board,
                                    Spot start, Spot end);
}
