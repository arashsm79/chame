package main.games.tictactoe;


public class TicButton {

    private States state;
    private TicPoint point;
    public TicButton ()
    {
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