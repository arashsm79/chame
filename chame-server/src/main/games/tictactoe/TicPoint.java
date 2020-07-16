package main.games.tictactoe;

public class TicPoint {
    private int x;
    private int y;
    public TicPoint(int x, int y)
    {
        this.x = x;
        this.y = y;
    }
    /**
     * @param x the x to set
     */
    public void setX(int x) {
        this.x = x;
    }
    /**
     * @return the x
     */
    public int getX() {
        return x;
    }
    /**
     * @param y the y to set
     */
    public void setY(int y) {
        this.y = y;
    }/**
     * @return the y
     */
    public int getY() {
        return y;
    }
}
