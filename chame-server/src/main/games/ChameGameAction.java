package main.games;

import main.ClientSession;

public class ChameGameAction {

    private String gameName;
    private String actionType;
    private String body;


    public ChameGameAction(String gameName, String actionType, String body) {
        this.gameName = gameName;
        this.actionType = actionType;
        this.body = body;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
