package main.games.tictactoe;

import main.ClientSession;
import main.games.ChameGame;
import main.games.ChamePlayer;

public class TicPlayer extends ChamePlayer {

    private TicButton.States marker;
    private TicBoard.Winner winningTag;

    TicPlayer(int index, ClientSession clientSession, ChameGame game, TicButton.States marker, TicBoard.Winner winningTag){
        super(index, clientSession, game);
        this.marker = marker;
        this.winningTag = winningTag;
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
