package main.games.tictactoe;

import javafx.scene.control.Button;

public class TicButton extends Button{

    private States state;
    private TicPoint point;
    public TicButton (String name)
    {
        super(name);
        this.state = States.EMPTY;
    }
    public States getState() {
        return state;
    }
    public void setState(States state) {
        this.state = state;
    }

    public TicPoint getPoint() {
        return point;
    }

    public void setPoint(TicPoint point) {
        this.point = point;
    }

    public enum States {
        X,
        // O is used for the AI
        O,
        EMPTY
    }
}