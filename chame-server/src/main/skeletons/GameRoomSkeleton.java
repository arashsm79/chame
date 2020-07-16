package main.skeletons;

import main.games.ChameGame;

public class GameRoomSkeleton {
    private int gameRoomID;
    private int roomID;
    private String gameName;
    private int capacity;
    private int playersInside;
    private String ownerUsername;
    private transient ChameGame chameGame;

    public GameRoomSkeleton(int gameRoomID, int roomID, String gameName, int capacity, int playersInside, String ownerUsername, ChameGame chameGame) {
        this.gameRoomID = gameRoomID;
        this.roomID = roomID;
        this.gameName = gameName;
        this.capacity = capacity;
        this.playersInside = playersInside;
        this.ownerUsername = ownerUsername;
        this.chameGame = chameGame;
    }

    public ChameGame getChameGame() {
        return chameGame;
    }

    public void setChameGame(ChameGame chameGame) {
        this.chameGame = chameGame;
    }

    public int getGameRoomID() {
        return gameRoomID;
    }

    public GameRoomSkeleton setGameRoomID(int gameRoomID) {
        this.gameRoomID = gameRoomID;
        return this;
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
