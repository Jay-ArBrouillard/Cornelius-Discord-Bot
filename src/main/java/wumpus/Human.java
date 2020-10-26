package wumpus;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;

import static wumpus.WumpusUtils.game;
import static wumpus.WumpusUtils.getAdjacentCells;

public class Human extends Character {
    private int arrows = 5;
    private int[] previousLoc; //Needed for map

    public Human(int[] location, int currentRoom) {
        super(location, currentRoom);
        this.previousLoc = location;
    }

    /**
     * Returns an inquiry to move or shoot or a validation message.
     * @param input
     * @return
     */
    public String moveOrShoot(String input) {
        String [] split = input.split(" ");
        if (input == null || input.length() == 0) {
            return "Invalid move or shoot command (ex: `m 5`/`s 5`/`q`): `" + input + "`";
        }
        if (split.length != 2) {
            return "Invalid move or shoot command (ex: `m 5`/`s 5`/`q`): `" + input + "`";
        }
        String roomNumber = split[1];
        if (split[0].equalsIgnoreCase("m")) {
            if (!StringUtils.isNumeric(roomNumber)) {
                return "Invalid room number (ex: `m 5`/`s 5`/`q`): `" + split[0] + "`";
            }
        } else if (split[0].equalsIgnoreCase("s")) {
            if (!StringUtils.isNumeric(roomNumber)) {
                return "Invalid room number (ex: `m 5`/`s 5`/`q`): `" + split[0] + "`";
            }
        } else {
            return "Invalid move or shoot command (ex: `m 5`/`s 5`/`q`): `" + input + "`";
        }
        return "success";
    }

    /**
     * Move the player to another location using roomNumber
     * @param roomNumber
     * @param board
     */
    public void move(String roomNumber, Board board) {
        move(board.getCellByRoomNumber(Integer.valueOf(roomNumber)), board.getGameBoard());
    }

    private void move(Cell to, Cell[][] gameBoard) {
        previousLoc = this.getLocation(); //set previous location
        gameBoard[to.getLocation()[0]][to.getLocation()[1]].setHuman(this); //move human to new room
        gameBoard[this.getLocation()[0]][this.getLocation()[1]].setHuman(null); //remove human from old room
        this.setCurrentRoom(to.getRoomNumber());    //Set new room number
        this.setLocation(new int[] {to.getLocation()[0], to.getLocation()[1]}); //set new location
    }

    public String validate(String input) {
        ArrayList<Cell> adjacentCells = getAdjacentCells(new int[] {this.getLocation()[0], this.getLocation()[1]});

        int roomNumber = -1;
        try {
            roomNumber = Integer.parseInt(input);
        } catch (NumberFormatException ne) {
            return "Invalid move/shoot command (" + adjacentCells.toString() + "): `" + input + "`";
        }

        int finalRoomNumber = roomNumber;
        boolean valid = adjacentCells.stream().anyMatch(x -> x.getRoomNumber() == finalRoomNumber);
        if (valid) {
            return "success";
        }
        return "The room number `" + input + "` isn't a valid option please choose from " + adjacentCells.toString();
    }

    /**
     * Will shoot in the direction of the room passed. Also causes wumpus to move.
     * @param roomNumber
     * @param board
     */
    public void shoot(String roomNumber, Board board, MessageReceivedEvent event) {
        int x = this.getLocation()[0];
        int y = this.getLocation()[1];

        Cell targetCell = board.getCellByRoomNumber(Integer.valueOf(roomNumber));
        int targetX = targetCell.getLocation()[0];
        int targetY = targetCell.getLocation()[1];

        Cell [][] gameBoard = board.getGameBoard();
        String temp = "Arrow travels through rooms...";
        //Shooting up
        if (targetX == x - 1 && targetY == y) {
            for (int i = x; i >= 0; i--) {
                temp += gameBoard[i][y].getRoomNumber() + "-";
                if (gameBoard[i][y].getWumpus() != null) {
                    gameBoard[i][y].getWumpus().setHit(true);
                }
            }
        } else if (targetX == x && targetY == y + 1) { //Shooting right
            for (int i = y; i < board.getWidth(); i++) {
                temp += gameBoard[x][i].getRoomNumber() + "-";
                if (gameBoard[x][i].getWumpus() != null) {
                    gameBoard[x][i].getWumpus().setHit(true);
                }
            }
        } else if (targetX == x + 1 && targetY == y) { //Shooting down
            for (int i = x; i < board.getWidth(); i++) {
                temp += gameBoard[i][y].getRoomNumber() + "-";
                if (gameBoard[i][y].getWumpus() != null) {
                    gameBoard[i][y].getWumpus().setHit(true);
                }
            }
        } else { //Shooting left
            for (int i = y; i >= 0; i--) {
                temp += gameBoard[x][i].getRoomNumber() + "-";
                if (gameBoard[x][i].getWumpus() != null) {
                    gameBoard[x][i].getWumpus().setHit(true);
                }
            }
        }

        event.getChannel().sendMessage(temp).queue();
        //Decrease arrows by 1
        this.arrows = this.arrows - 1;

        //The Wumpus moves will move if you shoot and miss
        if (!game.wumpus.isHit()) {
            ArrayList<Cell> adjacentCells = WumpusUtils.getAdjacentCells(game.wumpus.getLocation());
            Collections.shuffle(adjacentCells);
            Cell moveTo = adjacentCells.get(0);
            //move
            game.wumpus.move(moveTo, gameBoard);
        }
    }

    public int getArrows() {
        return arrows;
    }

    public void setArrows(int arrows) {
        this.arrows = arrows;
    }

    public int[] getPreviousLoc() {
        return previousLoc;
    }

    public void setPreviousLoc(int[] previousLoc) {
        this.previousLoc = previousLoc;
    }
}
