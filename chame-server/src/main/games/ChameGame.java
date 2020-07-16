package main.games;

import main.ClientSession;
import main.games.tictactoe.TicPlayer;
import main.skeletons.ChameMessage;
import main.skeletons.GameRoomSkeleton;

//an abstract class for all the games
public abstract class ChameGame {

    private GameStatus gameStatus;
    private ChamePlayer[] players;
    private int playersCount = 0;
    private String gameName;
    private GameRoomSkeleton gameRoom;

    public ChameGame(String gameName, int numberOfPlayers, GameRoomSkeleton gameRoom){
        this.gameName = gameName;
        this.gameRoom = gameRoom;
        this.gameRoom.setCapacity(numberOfPlayers);
        players = new TicPlayer[numberOfPlayers];
    }

    public abstract void handleResponse(ChameGameAction chameGameAction, ClientSession clientSession);

    public void addPlayer(ChamePlayer player){
        players[playersCount] = player;
        playersCount += 1;
    }
    public abstract  void abruptLeave(ClientSession clientSession);

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void removeGame(){
        GameHandler.getInstance().removeGame(this.gameRoom);
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public ChamePlayer[] getPlayersList() {
        return players;
    }

    public void getPlayersList(TicPlayer[] players) {
        this.players = players;
    }

    public GameRoomSkeleton getGameRoom() {
        return gameRoom;
    }

    public void setGameRoom(GameRoomSkeleton gameRoom) {
        this.gameRoom = gameRoom;
    }

    public ChamePlayer getPlayer(int index){
        if(index < players.length)
            return players[index];
        else
            return null;
    }


    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public abstract void joinUser(ClientSession clientSession);

    public int getPlayersCount() {
        return playersCount;
    }

    public void setPlayersCount(int playersCount) {
        this.playersCount = playersCount;
    }


    public enum GameNames{
        TICTACTOE,
        SNAKESANDLADDERS;
    }
    public enum GameStatus{
        WAITING,
        STARTED,
        FINISHED;
    }
}
