package wumpus;

public abstract class Character {
    private int [] location;
    private int currentRoom;

    public Character (int[] location, int currentRoom) {
        this.location = location;
        this.currentRoom = currentRoom;
    }

    public void setLocation(int[] location) {
        this.location = location;
    }

    public int[] getLocation() {
        return location;
    }

    public int getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(int currentRoom) {
        this.currentRoom = currentRoom;
    }
}
