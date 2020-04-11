package Wumpus;

import java.util.ArrayList;

public class Game {
    public Board board;
    public Human human;
    public Wumpus wumpus;
    public Map map;

    public Game(Board board, Human human, Wumpus wumpus) {
        this.board = board;
        this.human = human;
        this.wumpus = wumpus;
    }

    public void setMap(Map map) {
        this.map = map;
    }

    public String getStatus() {
        StringBuilder status = new StringBuilder();
        String bats = resolveBats();

        if (bats == null) {
            status.append("You are in room ");
            status.append(human.getCurrentRoom()).append("\n");
        }
        else {
            status.append(bats).append("\n");
        }

        String gameOverString = WumpusUtils.resolve();
        if (gameOverString != null) {
            return gameOverString;
        }

        status.append("Arrows: ");
        for (int i = 0; i < this.human.getArrows(); i++) {
            status.append("+--> ");
        }
        status.append("\n");
        status.append(WumpusUtils.getNearbyHazards(human.getLocation()));
        status.append("\n");
        status.append("Where would you like to move or shoot or quit? (ex: `m 5`/`s 5`/`q`)\n");
        map.update(human);
        status.append(map.getMapString());
        return status.toString();
    }

    public String resolveBats() {
        int x = this.human.getLocation()[0];
        int y = this.human.getLocation()[1];

        if (this.board.getGameBoard()[x][y].hasBats()) {
            //Remove the 'x' from the current spot since they are being moved from that spot
            this.map.updateCurrent(this.human);
            int newRoom = this.human.getCurrentRoom();
            while (newRoom == this.human.getCurrentRoom()) {
                newRoom = WumpusUtils.getRandomNumber(0, this.board.getLength() * this.board.getWidth() - 1);
            }
            this.human.move(Integer.toString(newRoom), this.board);
            return "Uh oh! Bats have moved you to `room " + newRoom + "`";
        }
        return null;
    }
}
