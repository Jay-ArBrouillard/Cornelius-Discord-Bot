package Wumpus;

public class Wumpus extends Character {
    private boolean hit;

    public Wumpus(int[] location, int currentRoom) {
        super(location, currentRoom);
        this.hit = false;
    }

    public boolean isHit() {
        return hit;
    }

    public void setHit(boolean hit) {
        this.hit = hit;
    }

    public void move(Cell to, Cell[][] gameBoard) {
        gameBoard[to.getLocation()[0]][to.getLocation()[1]].setWumpus(this); //move wumpus to new room
        gameBoard[this.getLocation()[0]][this.getLocation()[1]].setWumpus(null); //remove wumpus from old room
        this.setCurrentRoom(to.getRoomNumber());    //Set new room number
        this.setLocation(new int[] {to.getLocation()[0], to.getLocation()[1]}); //set new location
    }
}
