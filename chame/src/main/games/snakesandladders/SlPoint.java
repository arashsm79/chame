package main.games.snakesandladders;

public class SlPoint {
    private int x;
    private int y;
    public SlPoint(int x, int y)
    {
        this.x = x;
        this.y = y;
    }
    public SlPoint()
    {
        this.x = 0;
        this.y = 0;
    }

    @Override
    public String toString() {
        return this.getX() + " : " + this.getY();
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
