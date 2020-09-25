package chess;

import chess.pieces.Piece;

public class Spot {
    private Piece piece;
    private int x;
    private int y;
    //Use these to reference a spot using letter and number
    private String xPos; //a, b, c, d, e, f, g, h
    private String yPos; //1, 2, 3, 4, 5, 6, 7, 8

    public Spot(int x, int y, Piece piece)
    {
        this.setPiece(piece);
        this.setX(x);
        this.setY(y);
    }

    public Piece getPiece()
    {
        return this.piece;
    }

    public void setPiece(Piece p)
    {
        this.piece = p;
    }

    public int getX()
    {
        return this.x;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public int getY()
    {
        return this.y;
    }

    public void setY(int y)
    {
        this.y = y;
    }

    public String getxPos() {
        return xPos;
    }

    public void setxPos(String xPos) {
        this.xPos = xPos;
    }

    public String getyPos() {
        return yPos;
    }

    public void setyPos(String yPos) {
        this.yPos = yPos;
    }

    public boolean isOccupied() {
        return this.piece != null;
    }

    public String toString() {
        return xPos + yPos;
    }
}

