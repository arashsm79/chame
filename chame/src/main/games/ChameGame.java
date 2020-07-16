package main.games;

//An abstract class for all the games
public abstract class ChameGame {

    private String gameName;
    private GameStatus gameStatus = GameStatus.WAITING;
    public ChameGame(String gameName){
        this.gameName = gameName;
    }

    public abstract void handleServerResponse(ChameGameAction chameGameAction);

    public String getGameName() {
        return gameName;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public enum GameType {
        OFFLINE,
        ONLINE;
    }

    public enum GameStatus{
        WAITING,
        STARTED,
        FINISHED;
    }
}
