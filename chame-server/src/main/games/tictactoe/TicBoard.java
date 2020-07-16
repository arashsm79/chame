package main.games.tictactoe;

import static main.games.tictactoe.TicTacToe.GRID_SIZE;

public class TicBoard {

    private TicButton[][] buttons = new TicButton[GRID_SIZE][GRID_SIZE];
    private boolean isPlayingWithComputer = false;
    private int totalButtonsFilled = 0;
    private TicPlayer turn = null;

    TicBoard(){

    }

    public Winner checkForWin(TicButton[][] buttons, int GridSize) {
        //check vertical

        for (int i = 0; i < GRID_SIZE; i++){

            if(buttons[i][0].getState() != TicButton.States.EMPTY &&
                    buttons[i][0].getState() == buttons[i][1].getState() &&
                    buttons[i][1].getState() == buttons[i][2].getState())

                if(buttons[i][0].getState() == TicButton.States.X) {
                    return Winner.PLAYER1;
                }else {
                    return Winner.PLAYER2;
                }

            //check horizontal
            if(buttons[0][i].getState() != TicButton.States.EMPTY &&
                    buttons[0][i].getState() == buttons[1][i].getState() &&
                    buttons[1][i].getState() == buttons[2][i].getState())

                if(buttons[0][i].getState() == TicButton.States.X) {
                    return Winner.PLAYER1;
                }else {
                    return Winner.PLAYER2;
                }
        }

        //check diagonally top left to bottom right
        if(buttons[0][0].getState() != TicButton.States.EMPTY &&
                buttons[0][0].getState() == buttons[1][1].getState() &&
                buttons[1][1].getState() == buttons[2][2].getState())

            if(buttons[0][0].getState() == TicButton.States.X) {
                return Winner.PLAYER1;
            }else {
                return Winner.PLAYER2;
            }

        //check diagonally top right to bottom left
        if(buttons[2][0].getState() != TicButton.States.EMPTY &&
                buttons[2][0].getState() == buttons[1][1].getState() &&
                buttons[1][1].getState() == buttons[0][2].getState())

            if(buttons[2][0].getState() == TicButton.States.X) {
                return Winner.PLAYER1;
            }else {
                return Winner.PLAYER2;
            }

        if(totalButtonsFilled == GRID_SIZE * GRID_SIZE) {
            return Winner.TIE;
        }else {
            return null;
        }
    }

    public enum Winner {
        PLAYER1(-1),
        PLAYER2(1),
        TIE(0);

        public final int score;
        private Winner(int score)
        {
            this.score = score;
        }
        public int getScore() {
            return score;
        }
    }

    public void setButtons(TicButton[][] buttons) {
        this.buttons = buttons;
    }


    public boolean isPlayingWithComputer() {
        return isPlayingWithComputer;
    }

    public void setPlayingWithComputer(boolean playingWithComputer) {
        isPlayingWithComputer = playingWithComputer;
    }

    public int getTotalButtonsFilled() {
        return totalButtonsFilled;
    }
    public int incrementTotalButtonsFilled(){
        return ++totalButtonsFilled;
    }
    public int decrementTotalButtonsFilled(){
        return --totalButtonsFilled;
    }

    public void setTotalButtonsFilled(int totalButtonsFilled) {
        this.totalButtonsFilled = totalButtonsFilled;
    }

    public TicPlayer getTurn() {
        return turn;
    }
    public TicButton getButton(int x, int y){
        return buttons[x][y];
    }

    public void setTurn(TicPlayer turn) {
        this.turn = turn;
    }

    public TicButton[][] getButtons() {
        return buttons;
    }
}