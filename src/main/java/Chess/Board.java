package Chess;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Board {
    Spot[][] boxes;

    public Board()
    {
        boxes = new Spot[8][8];
    }

    public Spot getBox(int x, int y) {
        return boxes[x][y];
    }

    public void resetBoard()
    {
        // Starting from bottom left corner is (0,0)
        // initialize white pieces
        boxes[0][0] = new Spot(0, 0, new Rook(true));
        boxes[0][1] = new Spot(1, 0, new Knight(true));
        boxes[0][2] = new Spot(2, 0, new Bishop(true));
        boxes[0][3] = new Spot(3, 0, new Queen(true));
        boxes[0][4] = new Spot(4, 0, new King(true));
        boxes[0][5] = new Spot(5, 0, new Bishop(true));
        boxes[0][6] = new Spot(6, 0, new Knight(true));
        boxes[0][7] = new Spot(7, 0, new Rook(true));
        boxes[1][0] = new Spot(0, 1, new Pawn(true));
        boxes[1][1] = new Spot(1, 1, new Pawn(true));
        boxes[1][2] = new Spot(2, 1, new Pawn(true));
        boxes[1][3] = new Spot(3, 1, new Pawn(true));
        boxes[1][4] = new Spot(4, 1, new Pawn(true));
        boxes[1][5] = new Spot(5, 1, new Pawn(true));
        boxes[1][6] = new Spot(6, 1, new Pawn(true));
        boxes[1][7] = new Spot(7, 1, new Pawn(true));

        // initialize black pieces
        boxes[7][0] = new Spot(0, 7, new Rook(false));
        boxes[7][1] = new Spot(1, 7, new Knight(false));
        boxes[7][2] = new Spot(2, 7, new Bishop(false));
        boxes[7][3] = new Spot(3, 7, new Queen(false));
        boxes[7][4] = new Spot(4, 7, new King(false));
        boxes[7][5] = new Spot(5, 7, new Bishop(false));
        boxes[7][6] = new Spot(6, 7, new Knight(false));
        boxes[7][7] = new Spot(7, 7, new Rook(false));
        boxes[6][0] = new Spot(0, 6, new Pawn(false));
        boxes[6][1] = new Spot(1, 6, new Pawn(false));
        boxes[6][2] = new Spot(2, 6, new Pawn(false));
        boxes[6][3] = new Spot(3, 6, new Pawn(false));
        boxes[6][4] = new Spot(4, 6, new Pawn(false));
        boxes[6][5] = new Spot(5, 6, new Pawn(false));
        boxes[6][6] = new Spot(6, 6, new Pawn(false));
        boxes[6][7] = new Spot(7, 6, new Pawn(false));

        // initialize remaining boxes without any piece
        for (int i = 2; i < 6; i++) {
            for (int j = 0; j < 8; j++) {
                boxes[i][j] = new Spot(j, i,null);
            }
        }

        String [] letters = new String[] {"a", "b", "c", "d", "e", "f", "g", "h"};
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                boxes[i][j].setxPos(letters[j]);
                boxes[i][j].setyPos(""+(i+1));
            }
        }
    }

    public void resetBoardImage() throws IOException {
        //Begin with board image as background
        BufferedImage result = ImageIO.read(new File("src/main/java/Chess/assets/board.png"));
        Graphics g = result.getGraphics();

        //Overlay all the pieces onto the board
        BufferedImage blackRook1 = ImageIO.read(new File("src/main/java/Chess/assets/Black_Rook.png"));
        g.drawImage(blackRook1, 70, 43, 120, 120, null); //Black_Rook1

        BufferedImage blackKnight1 = ImageIO.read(new File("src/main/java/Chess/assets/Black_Knight.png"));
        g.drawImage(blackKnight1, 232, 43, 120, 120, null); //Black_Knight1

        BufferedImage blackBishop1 = ImageIO.read(new File("src/main/java/Chess/assets/Black_Bishop.png"));
        g.drawImage(blackBishop1, 394, 43, 120, 120, null); //Black_Bishop1

        BufferedImage blackQueen = ImageIO.read(new File("src/main/java/Chess/assets/Black_Queen.png"));
        g.drawImage(blackQueen, 556, 43, 120, 120, null); //Black_Queen

        BufferedImage blackKing = ImageIO.read(new File("src/main/java/Chess/assets/Black_King.png"));
        g.drawImage(blackKing, 718, 43, 120, 120, null); //Black_King

        BufferedImage blackBishop2 = ImageIO.read(new File("src/main/java/Chess/assets/Black_Bishop.png"));
        g.drawImage(blackBishop2, 880, 43, 120, 120, null); //Black_Bishop2

        BufferedImage blackKnight2 = ImageIO.read(new File("src/main/java/Chess/assets/Black_Knight.png"));
        g.drawImage(blackKnight2, 1042, 43, 120, 120, null); //Black_Knight2

        BufferedImage blackRook2 = ImageIO.read(new File("src/main/java/Chess/assets/Black_Rook.png"));
        g.drawImage(blackRook2, 1204, 43, 120, 120, null); //Black_Rook2

        //Black Pawns
        int x = 70;
        int y = 205;
        for (int i = 0; i < 8; i++) {
            BufferedImage blackPawn = ImageIO.read(new File("src/main/java/Chess/assets/Black_Pawn.png"));
            g.drawImage(blackPawn, x, y, 120, 120, null); //Black_Pawn
            x = x + 162;
        }

        //White Pawns
        x = 70;
        y = 1015;
        for (int i = 0; i < 8; i++) {
            BufferedImage whitePawn = ImageIO.read(new File("src/main/java/Chess/assets/White_Pawn.png"));
            g.drawImage(whitePawn, x, y, 120, 120, null); //White_Pawn
            x = x + 162;
        }

        BufferedImage whiteRook1 = ImageIO.read(new File("src/main/java/Chess/assets/White_Rook.png"));
        g.drawImage(whiteRook1, 70, 1177, 120, 120, null); //White_Rook1

        BufferedImage whiteKnight1 = ImageIO.read(new File("src/main/java/Chess/assets/White_Knight.png"));
        g.drawImage(whiteKnight1, 232, 1177, 120, 120, null); //White_Knight1

        BufferedImage whiteBishop1 = ImageIO.read(new File("src/main/java/Chess/assets/White_Bishop.png"));
        g.drawImage(whiteBishop1, 394, 1177, 120, 120, null); //White_Bishop1

        BufferedImage whiteQueen = ImageIO.read(new File("src/main/java/Chess/assets/White_Queen.png"));
        g.drawImage(whiteQueen, 556, 1177, 120, 120, null); //White_Queen

        BufferedImage whiteKing = ImageIO.read(new File("src/main/java/Chess/assets/White_King.png"));
        g.drawImage(whiteKing, 718, 1177, 120, 120, null); //White_King

        BufferedImage whiteBishop2 = ImageIO.read(new File("src/main/java/Chess/assets/White_Bishop.png"));
        g.drawImage(whiteBishop2, 880, 1177, 120, 120, null); //White_Bishop2

        BufferedImage whiteKnight2 = ImageIO.read(new File("src/main/java/Chess/assets/White_Knight.png"));
        g.drawImage(whiteKnight2, 1042, 1177, 120, 120, null); //White_Knight2

        BufferedImage whiteRook2 = ImageIO.read(new File("src/main/java/Chess/assets/White_Rook.png"));
        g.drawImage(whiteRook2, 1204, 1177, 120, 120, null); //White_Rook2

        ImageIO.write(result,"png",new File("src/main/java/Chess/gameState.png"));
        System.out.println("Successfully reset board image");
    }

    public boolean buildImage()
    {
        //Begin with board image as background
        try {
            BufferedImage result = ImageIO.read(new File("src/main/java/Chess/assets/board.png"));
            Graphics g = result.getGraphics();

            //Overlay all the pieces onto the board based on matrix
            int x = 1204;
            int y = 43;
            for (int i = 7; i >= 0; i--)
            {
                for (int j = 7; j >= 0; j--)
                {
                    if (boxes[i][j].getPiece() != null) //There is a piece here
                    {
                        //Overlay that piece
                        BufferedImage piece = ImageIO.read(new File(boxes[i][j].getPiece().getFilePath()));
                        g.drawImage(piece, x, y, 120, 120, null);
                    }
                    x -= 162;
                }
                x = 1204;
                y += 162;
            }

            ImageIO.write(result,"png",new File("src/main/java/Chess/gameState.png"));
            System.out.println("Successfully reset board image");
            return true;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}

