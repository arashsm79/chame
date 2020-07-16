package main.games.snakesandladders;

import javafx.scene.layout.GridPane;
import main.games.tictactoe.TicButton;
import main.games.tictactoe.TicPoint;

public class SlBoard {
    public static final int GRID_SIZE = 10;

    private SlButton[][] buttons = new SlButton[GRID_SIZE][GRID_SIZE];

    SlBoard(GridPane boardGridPane){
        for(int row = 0; row < GRID_SIZE; row++)
        {
            for(int col = 0; col < GRID_SIZE; col++)
            {
                buttons[col][row] = new SlButton();
                SlButton currentBtn = buttons[col][row];
                currentBtn.setPoint(new SlPoint(col, row));
                //Styling
                currentBtn.getStyleClass().add("piece");
                currentBtn.setType(SlButton.Type.EMPTY);
                currentBtn.setPrefSize(60, 60);
                boardGridPane.add(currentBtn, col, row);
            }
        }

        setUpSnakesAndLadders();
    }

    private void setUpSnakesAndLadders() {
        getButton(1, 9).addPieceMover(SlButton.Type.LADDER, new SlLadder(1, 9, 2, 8));
        getButton(2, 7).addPieceMover(SlButton.Type.SNAKE, new SlSnake(2, 7, 5, 9));
    }

    public SlButton getButton(SlPoint point){
        return buttons[point.getX()][point.getY()];
    }
    public SlButton getButton(int x, int y){
        return buttons[x][y];
    }

}
