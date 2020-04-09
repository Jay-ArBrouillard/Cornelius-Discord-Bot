package Wumpus;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class Map {
    public String mapString;
    public Cell [][] board;

    public Map(Cell[][] board) {
        this.board = board;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                sb.append("[ ? ]");
            }
            sb.append("\n");
        }
        mapString = sb.toString();
    }

    public void update(Human human) {
        int roomNum = human.getCurrentRoom()+1;
        int firstBracket = findNthOccur(mapString, '[', roomNum);
        int secondBracket = findNthOccur(mapString, ']', roomNum);

        mapString = mapString.substring(0, firstBracket) + "[ X ]" + mapString.substring(secondBracket+1);
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
