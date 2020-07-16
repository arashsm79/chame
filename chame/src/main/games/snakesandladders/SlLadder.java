package main.games.snakesandladders;

public class SlLadder implements SlPieceMover{
    private SlPoint begin;
    private SlPoint end;

    public SlLadder(SlPoint begin, SlPoint end) {
        this.begin = begin;
        this.end = end;
    }
    public SlLadder(int x1, int y1, int x2, int y2) {
        this.begin = new SlPoint(x1, y1);
        this.end = new SlPoint(x2, y2);
    }

    public SlPoint getBegin() {
        return begin;
    }

    public void setBegin(SlPoint begin) {
        this.begin = begin;
    }

    public SlPoint getEnd() {
        return end;
    }

    public void setEnd(SlPoint end) {
        this.end = end;
    }
}
