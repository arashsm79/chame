package main.games.snakesandladders;

import javafx.scene.control.Button;

public class SlButton extends Button {

    private SlPoint point;
    private Type type;
    private SlPieceMover pieceMover;
    private SlPlayer standingPlayer = null;
    public Type getType() {
        return type;
    }

    public SlPieceMover getPieceMover() {
        return pieceMover;
    }

    public void setPieceMover(SlPieceMover pieceMover) {
        this.pieceMover = pieceMover;
    }

    public SlPlayer getStandingPlayer() {
        return standingPlayer;
    }

    public void setStandingPlayer(SlPlayer standingPlayer) {
        this.standingPlayer = standingPlayer;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public SlPoint getPoint() {
        return point;
    }

    public void setPoint(SlPoint point) {
        this.point = point;
    }

    public void addPieceMover(Type type, SlPieceMover pieceMover) {
        this.type = type;
        this.pieceMover = pieceMover;
    }

    public enum Type{
        SNAKE,
        LADDER,
        EMPTY;
    }
}
