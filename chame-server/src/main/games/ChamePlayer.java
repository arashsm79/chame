package main.games;

import main.ClientSession;

public class ChamePlayer {
    private ClientSession clientSession;
    private int index;

    public ChamePlayer(int index, ClientSession clientSession, ChameGame game){
        this.index = index;
        this.clientSession = clientSession;
        clientSession.setCurrentGame(game);
        clientSession.setCurrentGamePlayer(this);
    }

    public ClientSession getClientSession() {
        return clientSession;
    }

    public void setClientSession(ClientSession clientSession) {
        this.clientSession = clientSession;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
