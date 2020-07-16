package main.games.tictactoe.skeletons;

public class TicGameStartSkeleton {
    private String player1;
    private String player1Mark;
    private String player2;
    private String player2Mark;

    public TicGameStartSkeleton(String player1, String player1Mark, String player2, String player2Mark) {
        this.player1 = player1;
        this.player1Mark = player1Mark;
        this.player2 = player2;
        this.player2Mark = player2Mark;
    }

    public String getPlayer1() {
        return player1;
    }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }

    public String getPlayer1Mark() {
        return player1Mark;
    }

    public void setPlayer1Mark(String player1Mark) {
        this.player1Mark = player1Mark;
    }

    public String getPlayer2() {
        return player2;
    }

    public void setPlayer2(String player2) {
        this.player2 = player2;
    }

    public String getPlayer2Mark() {
        return player2Mark;
    }

    public void setPlayer2Mark(String player2Mark) {
        this.player2Mark = player2Mark;
    }
}
