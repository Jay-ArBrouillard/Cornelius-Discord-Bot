package Wumpus;

public class Board {
    private int length;
    private int width;
    private Cell [][] gameBoard;
    private int[][] bottomlessPits; //[[1,2],[0,0]]
    private int[][] bats; //[[3,0]]

    /**
     * Initialize the game board of size length by width
     * @param length
     * @param width
     */
    public Board (int length, int width, int[][] pits, int[][] bats, Human human, Wumpus wumpus) {
        this.length = length;
        this.width = width;
        this.gameBoard = new Cell[length][width];
        //Add a Cell (x,y coordinate and roomNumber) to each spot on the board
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < width; j++) {
                int roomNumber = i*length+j;
                Cell cell = new Cell(new int[]{i,j}, roomNumber);
                gameBoard[i][j] = cell;
            }
        }

        //Add human to board
        gameBoard[human.getLocation()[0]][human.getLocation()[1]].setHuman(human);
        //Add wumpus to the board
        gameBoard[wumpus.getLocation()[0]][wumpus.getLocation()[1]].setWumpus(wumpus);

        //Add bottomlessPits to board
        for (int i = 0; i < pits.length; i++) {
            gameBoard[pits[i][0]][pits[i][1]].setHasPit(true);
        }

        //Add bats to board
        for (int i = 0; i < bats.length; i++) {
            gameBoard[bats[i][0]][bats[i][1]].setHasBats(true);
        }
    }

    public int getLength() {
        return length;
    }

    public int getWidth() {
        return width;
    }

    public Cell[][] getGameBoard() {
        return gameBoard;
    }

    public int[][] getBottomlessPits() {
        return bottomlessPits;
    }

    public int[][] getBats() {
        return bats;
    }

    public Cell getCellByRoomNumber(int roomNumber) {
        for (int i = 0; i < gameBoard.length; i++) {
            for (int j = 0; j < gameBoard[i].length; j++) {
                if (gameBoard[i][j].getRoomNumber() == roomNumber) {
                    return gameBoard[i][j];
                }
            }
        }
        return null;
    }
}
