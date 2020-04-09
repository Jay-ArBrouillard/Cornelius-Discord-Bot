package Wumpus;

import java.util.ArrayList;

public final class WumpusUtils {
    public static Game game;

    public WumpusUtils (Game game) {
        this.game = game;
    }

    public static int getRandomNumber(long min, long max) {
        return (int) (min + Math.random() * (max - min + 1));
    }

    public static String getNearbyHazards (int [] location) {
        StringBuilder hazards = new StringBuilder();
        ArrayList<Cell> adjacentCells = getAdjacentCells(location);
        Cell [][] gameBoard = game.board.getGameBoard();
        for (Cell cell : adjacentCells) {
            if (cell.getWumpus() != null) {
                hazards.append("\t\t\t`You smell a Wumpus nearby`\n");
            }
            if (gameBoard[cell.getLocation()[0]][cell.getLocation()[1]].hasPit()) {
                hazards.append("\t\t`You feel a draft`\n");
            }
            if (gameBoard[cell.getLocation()[0]][cell.getLocation()[1]].hasBats()) {
                hazards.append("\t`You hear flapping nearby`\n");
            }
        }
        return hazards.toString();
    }

    public static ArrayList<Cell> getAdjacentCells(int[] location) {
        ArrayList<Cell> cells = new ArrayList<Cell>();
        Cell [][] gameBoard = game.board.getGameBoard();
        if (location[0] + 1 < game.board.getLength()) {
            cells.add(gameBoard[location[0]+1][location[1]]);
        }
        if (location[0] - 1 >= 0) {
            cells.add(gameBoard[location[0]-1][location[1]]);
        }
        if (location[1] + 1 < game.board.getWidth()) {
            cells.add(gameBoard[location[0]][location[1]+1]);
        }
        if (location[1] - 1 >= 0) {
            cells.add(gameBoard[location[0]][location[1]-1]);
        }
        return cells;
    }

    public static String resolve() {
        int playerRoom = game.human.getCurrentRoom();
        int playerX = game.human.getLocation()[0];
        int playerY = game.human.getLocation()[1];
        if (playerRoom == game.wumpus.getCurrentRoom()) {
            return "YOU FOUND THE WUMPUS. You lose\n`Game Over`";
        }
        if (game.board.getGameBoard()[playerX][playerY].hasPit()) {
            return "YYYYOOOOOEEEE...Oh no you fell in a pit and brutally died!\n`Game Over`";
        }
        if (game.human.getArrows() == 0) {
            return "You lost because you ran out of arrows...what a shame.\n`Game Over`";
        }
        if (game.wumpus.isHit()) {
            return "YOU CLAPPED THE WUMPUS WITH AN ARROW. You Win\n`Game Over`";
        }
        return null;
    }
}
