package Wumpus;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class Map {
    public String mapString;
    public Cell [][] board;
    public int lastRoomNumLength;

    public Map(Cell[][] board, int lastRoomNumLength) {
        this.board = board;
        this.lastRoomNumLength = lastRoomNumLength;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                sb.append("[ ").append(StringUtils.leftPad("?", lastRoomNumLength, " ")).append(" ]");
            }
            sb.append("\n");
        }
        mapString = sb.toString();
    }

    /**
     * Updates the map with an 'x' for the humans location and also addings room numbers to adjacent locations
     * @param human
     */
    public void update(Human human) {
        int roomNum = human.getCurrentRoom();
        int firstBracket = findNthOccur(mapString, '[', roomNum+1);
        int secondBracket = findNthOccur(mapString, ']', roomNum+1);
        mapString = mapString.substring(0, firstBracket) + "[ " + StringUtils.leftPad("x", lastRoomNumLength, " ") + " ]" + mapString.substring(secondBracket+1);

        ArrayList<Cell> adjacentCells = WumpusUtils.getAdjacentCells(human.getLocation());
        for (Cell c : adjacentCells) {
            roomNum = c.getRoomNumber();
            firstBracket = findNthOccur(mapString, '[', roomNum+1);
            secondBracket = findNthOccur(mapString, ']', roomNum+1);
            mapString = mapString.substring(0, firstBracket) + "[ " +
                    StringUtils.leftPad(Integer.toString(roomNum), lastRoomNumLength, " ") +
                    " ]" +
                    mapString.substring(secondBracket+1);
        }
    }

    /**
     * Will only update the previous location human is standing on to that room number instead of an 'x'
     * @param human
     */
    public void updateCurrent(Human human) {
        int roomNum = board[human.getPreviousLoc()[0]][human.getPreviousLoc()[1]].getRoomNumber();
        int firstBracket = findNthOccur(mapString, '[', roomNum+1);
        int secondBracket = findNthOccur(mapString, ']', roomNum+1);
        mapString = mapString.substring(0, firstBracket) + "[ " + StringUtils.leftPad(Integer.toString(roomNum), lastRoomNumLength, " ") + " ]" + mapString.substring(secondBracket+1);
    }

    public int findNthOccur(String str, char ch, int N)
    {
        int occur = 0;
        // Loop to find the Nth
        // occurence of the character
        for (int i = 0; i < str.length(); i++)
        {
            if (str.charAt(i) == ch)
            {
                occur += 1;
            }
            if (occur == N)
                return i;
        }
        return -1;
    }

    public String getMapString() {
        return mapString;
    }
}
