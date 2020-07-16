package main.games.snakesandladders;

public class SlPlayer{

    private SnakesAndLadders.Pieces piece;
    private String username;
    private SlPoint position;
    private int index;

    private boolean out;

    public SlPlayer(String username, SnakesAndLadders.Pieces piece,  SlPoint position, int index, boolean out) {
        this.piece = piece;
        this.username = username;
        this.position = position;
        this.index = index;
        this.out = out;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPosition(SlPoint position) {
        this.position.setX(position.getX());
        this.position.setY(position.getY());
    }

    public void setPosition(int x, int y){
        this.position.setX(x);
        this.position.setY(y);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isOut() {
        return out;
    }

    public void setOut(boolean out) {
        this.out = out;
    }

    public SlPoint getPosition(){
        return this.position;
    }
    public SnakesAndLadders.Pieces getPiece() {
        return piece;
    }

    public void setPiece(SnakesAndLadders.Pieces piece) {
        this.piece = piece;
    }
}
