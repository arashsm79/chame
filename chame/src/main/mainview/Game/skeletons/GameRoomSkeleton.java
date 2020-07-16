package main.mainview.Game.skeletons;

public class GameRoomSkeleton {
    private int gameRoomID;
    private int roomID;
    private String gameName;
    private int capacity;
    private int playersInside;
    private String ownerUsername;

    public GameRoomSkeleton() {
    }

    public GameRoomSkeleton(int gameRoomID, int roomID, String gameName, int capacity, int playersInside, String ownerUsername) {
        this.gameRoomID = gameRoomID;
        this.roomID = roomID;
        this.gameName = gameName;
        this.capacity = capacity;
        this.playersInside = playersInside;
        this.ownerUsername = ownerUsername;
    }

    public int getGameRoomID() {
        return gameRoomID;
    }

    public void setGameRoomID(int gameRoomID) {
        this.gameRoomID = gameRoomID;
    }

    public int getRoomID() {
        return roomID;
    }

    public void setRoomID(int roomID) {
        this.roomID = roomID;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getPlayersInside() {
        return playersInside;
    }

    public void setPlayersInside(int playersInside) {
        this.playersInside = playersInside;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }
}
