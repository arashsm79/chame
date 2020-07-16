package main.games.tictactoe;

public class TicPlayer {

    private TicButton.States marker;
    private TicBoard.Winner winningTag;
    private String username;

    public TicPlayer(String username, TicButton.States marker, TicBoard.Winner winningTag) {
        this.marker = marker;
        this.winningTag = winningTag;
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public TicBoard.Winner getWinningTag() {
        return winningTag;
    }

    public void setWinningTag(TicBoard.Winner winningTag) {
        this.winningTag = winningTag;
    }

    public TicButton.States getMarker() {
        return marker;
    }

    public void setMarker(TicButton.States marker) {
        this.marker = marker;
    }
}
