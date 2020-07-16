package main.games.snakesandladders;

public class SlAI extends SlPlayer{

    public SlAI(SnakesAndLadders.Pieces piece) {
        super("AI", piece, new SlPoint(-1, -1), 0, true);
    }
}
