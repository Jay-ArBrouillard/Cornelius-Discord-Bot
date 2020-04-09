package Wumpus;

import java.util.ArrayList;

public class Cell {
    private int [] location;
    private int roomNumber;
    private Human human;
    private Wumpus wumpus;
    private boolean isPit;
    private boolean hasBats;

    public Cell (int [] location, int roomNumber) {
        this.location = location;
        this.roomNumber = roomNumber;
        isPit = false;
        hasBats = false;
    }

    public int[] getLocation() {
        return location;
    }

    public void setLocation(int[] location) {
        this.location = location;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public Human getHuman() {
        return human;
    }

    public void setHuman(Human human) {
        this.human = human;
    }

    public Wumpus getWumpus() {
        return wumpus;
    }

    public void setWumpus(Wumpus wumpus) {
        this.wumpus = wumpus;
    }

    public void setHasPit(boolean pit) {
        isPit = pit;
    }

    public boolean hasPit() {
        return isPit;
    }

    public boolean hasBats() {
        return hasBats;
    }

    public void setHasBats(boolean hasBats) {
        this.hasBats = hasBats;
    }

    public String toString() {
        return Integer.toString(roomNumber);
    }
}
