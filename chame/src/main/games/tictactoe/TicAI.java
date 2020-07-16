package main.games.tictactoe;

import static main.games.tictactoe.TicTacToe.GRID_SIZE;

public class TicAI extends TicPlayer {

    TicAI(TicButton.States marker, TicBoard.Winner winningTag){
        super("AI", marker, winningTag);
    }

    //the heart of the minimax algorithm
    /////////// MINIMAX //////////////
    public TicPoint findTheBestPoint(TicBoard ticBoard) {

        TicButton[][] buttons = ticBoard.getButtons();
        int bestScore = Integer.MIN_VALUE; //-infinity
        TicPoint move = null;
        for(int i = 0; i < GRID_SIZE; i++)
        {
            for(int j = 0; j < GRID_SIZE; j++)
            {

                if(buttons[i][j].getState() == TicButton.States.EMPTY)
                {
                    buttons[i][j].setState(TicButton.States.O);
                    ticBoard.incrementTotalButtonsFilled();

                    int score = minimax(ticBoard, 0, false);

                    buttons[i][j].setState(TicButton.States.EMPTY);
                    ticBoard.decrementTotalButtonsFilled();
                    if(score > bestScore)
                    {
                        bestScore = score;
                        move = new TicPoint(i, j);
                    }


                }
            }
        }
        return move;
    }


    public int minimax(TicBoard ticBoard, int depth, boolean isMaximizing) {

        TicButton[][] buttons = ticBoard.getButtons();
        TicBoard.Winner result = ticBoard.checkForWin(buttons, GRID_SIZE);
        //base condition
        if(result != null)
        {
            return result.getScore();
        }

        if(isMaximizing)
        {
            int bestScore = Integer.MIN_VALUE; //-infinity
            for(int i = 0; i < GRID_SIZE; i++)
            {
                for(int j = 0; j < GRID_SIZE; j++)
                {
                    if(buttons[i][j].getState() == TicButton.States.EMPTY)
                    {
                        buttons[i][j].setState(TicButton.States.O); //AI
                        ticBoard.incrementTotalButtonsFilled();
                        int score = minimax(ticBoard, depth + 1, false);

                        buttons[i][j].setState(TicButton.States.EMPTY);
                        ticBoard.decrementTotalButtonsFilled();

                        if(score > bestScore)
                        {
                            bestScore = score;
                        }
                    }
                }
            }
            return bestScore;

        }else
        {
            int bestScore = Integer.MAX_VALUE; // +infinity
            for(int i = 0; i < GRID_SIZE; i++)
            {
                for(int j = 0; j < GRID_SIZE; j++)
                {
                    if(buttons[i][j].getState() == TicButton.States.EMPTY)
                    {
                        buttons[i][j].setState(TicButton.States.X); //Player
                        ticBoard.incrementTotalButtonsFilled();

                        int score = minimax(ticBoard, depth + 1, true);
                        buttons[i][j].setState(TicButton.States.EMPTY);
                        ticBoard.decrementTotalButtonsFilled();

                        if(score < bestScore)
                        {
                            bestScore = score;
                        }
                    }
                }
            }

            return bestScore;
        }

    }

}
