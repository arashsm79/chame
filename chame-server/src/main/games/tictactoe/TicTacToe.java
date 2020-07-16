package main.games.tictactoe;


import com.google.gson.Gson;
import main.ChameProtocol;
import main.ClientSession;
import main.games.ChameGame;
import main.games.ChameGameAction;
import main.games.ChamePlayer;
import main.games.GameHandler;
import main.games.tictactoe.skeletons.TicGameStartSkeleton;
import main.games.tictactoe.skeletons.TicPlaceMarkSkeleton;
import main.skeletons.ChameMessage;
import main.skeletons.ChatMessageSkeleton;
import main.skeletons.GameRoomSkeleton;

public class TicTacToe extends ChameGame {
    
    public static final int GRID_SIZE = 3;
    private Gson gson = new Gson();
    private TicBoard ticBoard;


    public TicTacToe(GameRoomSkeleton gameRoom){
        super(ChameProtocol.TICTACTOE, 2, gameRoom);
        ticBoard = new TicBoard();
        this.setGameStatus(GameStatus.WAITING);
        setUpGameGrid(ticBoard.getButtons());
    }

    @Override
    public synchronized void joinUser(ClientSession clientSession) {

        if(this.getPlayersCount() == 0)
            super.addPlayer(new TicPlayer(this.getPlayersCount(), clientSession, this, TicButton.States.X, TicBoard.Winner.PLAYER1));
        else if(this.getPlayersCount() == 1)
            super.addPlayer(new TicPlayer(this.getPlayersCount(), clientSession, this, TicButton.States.O, TicBoard.Winner.PLAYER2));

        if(this.getPlayersCount() == 2){
            sendStartSignal();
        }
    }

    @Override
    public synchronized void abruptLeave(ClientSession clientSession) {

        if(getGameStatus() == GameStatus.STARTED){
            TicPlayer winnerPlayer = getPlayer(0);
            if(winnerPlayer == clientSession.getCurrentGamePlayer())
                winnerPlayer = getPlayer(1);

            sendEndGameMessage(winnerPlayer.getWinningTag());
            this.setGameStatus(GameStatus.FINISHED);

        }else{
            removeGame();
        }
        synchronized (getPlayersList()){

            for(ChamePlayer player : getPlayersList()){
                if(player==null)
                    continue;
                player.getClientSession().setCurrentGame(null);
                player.getClientSession().setCurrentGamePlayer(null);
            }
        }
    }

    private void sendStartSignal() {

        TicGameStartSkeleton ticGameStartSkeleton = new TicGameStartSkeleton(
                this.getPlayer(0).getClientSession().getUsername(),
                this.getPlayer(0).getMarker().name(),
                this.getPlayer(1).getClientSession().getUsername(),
                this.getPlayer(1).getMarker().name()
        );

        String startSignal = gson.toJson(new ChameMessage(
                ChameProtocol.GAME_ACTION,
                gson.toJson(new ChameGameAction(ChameProtocol.TICTACTOE,
                        ChameProtocol.START_GAME,
                        gson.toJson(ticGameStartSkeleton)
                        )
                    )
                )
        );

        this.setGameStatus(GameStatus.STARTED);
        broadCast(startSignal);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        changeTurn();

    }

    public synchronized void doTurn(TicPlayer player, TicPoint point){

        if(ticBoard.getTurn() == player){

            TicButton ticButton = ticBoard.getButton(point.getX(), point.getY());

            if(ticButton.getState() != TicButton.States.EMPTY){
                //error
                return;
            }

            //do the move
            ticButton.setState(player.getMarker());

            sendUpdateToPlayers(point, player.getMarker().name());
            ticBoard.incrementTotalButtonsFilled();

            if(!checkForWinner()){
                changeTurn();
            }

        }else {
            //error
            return;
        }

    }

    private void sendUpdateToPlayers(TicPoint point, String markerName) {

        TicPlaceMarkSkeleton ticPlaceMarkSkeleton = new TicPlaceMarkSkeleton(
                point,
                markerName
        );

        String updateMessage = gson.toJson(new ChameMessage(
                ChameProtocol.GAME_ACTION,
                gson.toJson(new ChameGameAction(ChameProtocol.TICTACTOE,
                        TicProtocols.PLACE_MARK,
                        gson.toJson(ticPlaceMarkSkeleton)))
        ));

        broadCast(updateMessage);

    }

    public void broadCast(String msg){
        for(ChamePlayer pl : this.getPlayersList()){
            pl.getClientSession().sendMessage(msg);
        }
    }

    public boolean checkForWinner(){

        TicBoard.Winner winner = ticBoard.checkForWin(ticBoard.getButtons(), GRID_SIZE);
        if(winner != null) {

            sendEndGameMessage(winner);
            this.setGameStatus(GameStatus.FINISHED);
            return true;
        }
        return false;
    }

    private void sendEndGameMessage(TicBoard.Winner winner) {

        if(winner == null)
            return;

        String winnerUsername = "";
        switch (winner){
            case PLAYER1:{
                winnerUsername = this.getPlayer(0).getClientSession().getUsername();
                break;
            }
            case PLAYER2:{
                winnerUsername = this.getPlayer(1).getClientSession().getUsername();
                break;
            }
            case TIE:{
                winnerUsername = ChameProtocol.TIE;
            }
        }
        this.setGameStatus(GameStatus.FINISHED);
        String endMessage = gson.toJson(new ChameMessage(
                ChameProtocol.GAME_ACTION,
                gson.toJson(new ChameGameAction(ChameProtocol.TICTACTOE,
                        ChameProtocol.END_GAME,
                        winnerUsername))
        ));

        broadCast(endMessage);
        removeGame();

    }

    public synchronized void changeTurn(){
        String turnFalseMessage = gson.toJson(new ChameMessage(
                ChameProtocol.GAME_ACTION,
                gson.toJson(new ChameGameAction(ChameProtocol.TICTACTOE,
                        TicProtocols.CHANGE_TURN,
                        "false"))
        ));

        String turnTrueMessage = gson.toJson(new ChameMessage(
                ChameProtocol.GAME_ACTION,
                gson.toJson(new ChameGameAction(ChameProtocol.TICTACTOE,
                        TicProtocols.CHANGE_TURN,
                        "true"))
        ));

        if(ticBoard.getTurn() == this.getPlayer(0)){

            ticBoard.setTurn(getPlayer(1));
            this.getPlayer(0).getClientSession().sendMessage(turnFalseMessage);
            this.getPlayer(1).getClientSession().sendMessage(turnTrueMessage);

        } else {
            ticBoard.setTurn(this.getPlayer(0));
            this.getPlayer(0).getClientSession().sendMessage(turnTrueMessage);
            this.getPlayer(1).getClientSession().sendMessage(turnFalseMessage);
        }
    }


    @Override
    public TicPlayer getPlayer(int index){
        return (TicPlayer) super.getPlayer(index);
    }

        //sets up buttons
    public void setUpGameGrid(TicButton[][] buttons)
    {
        for(int col = 0; col < GRID_SIZE; col++)
        {
            for(int row = 0; row < GRID_SIZE; row++)
            { 
                buttons[col][row] = new TicButton();
                TicButton currentBtn = buttons[col][row];
                currentBtn.setPoint(new TicPoint(col, row));
            }
        }
    }


    @Override
    public void handleResponse(ChameGameAction chameGameAction, ClientSession clientSession) {
        if(
                chameGameAction.getGameName().equalsIgnoreCase(ChameProtocol.TICTACTOE) &&
                this.getGameStatus() == GameStatus.STARTED
        ){
                switch (chameGameAction.getActionType()){
                    case TicProtocols.PLACE_MARK:{

                        TicPlaceMarkSkeleton ticPlaceMarkSkeleton = gson.fromJson(chameGameAction.getBody(), TicPlaceMarkSkeleton.class);
                        TicPoint ticPoint = ticPlaceMarkSkeleton.getTicPoint();
                        if(ticPoint == null)
                            return;
                        doTurn((TicPlayer)clientSession.getCurrentGamePlayer(), ticPoint);
                        break;
                    }
                    case ChameProtocol.ABRUPT_LEAVE:{
                        abruptLeave(clientSession);
                        break;
                    }
                }

        }
    }
}






