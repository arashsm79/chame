package main.games.tictactoe.skeletons;

import main.games.tictactoe.TicPoint;

public class TicPlaceMarkSkeleton {
    private TicPoint ticPoint;
    private String mark;

    public TicPlaceMarkSkeleton(TicPoint ticPoint, String mark) {
        this.ticPoint = ticPoint;
        this.mark = mark;
    }

    public TicPoint getTicPoint() {
        return ticPoint;
    }

    public void setTicPoint(TicPoint ticPoint) {
        this.ticPoint = ticPoint;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }
}
