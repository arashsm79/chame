package main.games.tictactoe;

import java.util.concurrent.atomic.AtomicBoolean;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;
import main.ControllerManager;
import main.Main;
import main.connection.ChameProtocol;
import main.connection.Connection;
import main.connection.skeletons.ChameMessage;
import main.games.ChameGame;
import main.games.ChameGameAction;
import main.games.tictactoe.skeletons.TicGameStartSkeleton;
import main.games.tictactoe.skeletons.TicPlaceMarkSkeleton;
import main.mainview.Game.GameTabController;
import main.mainview.MainViewController;

public class TicTacToe extends ChameGame{
    
    public static final int GRID_SIZE = 3;

    private Stage parentStage;
    private Stage gameStage;
    private Scene gameScene;
    private TicBoard ticBoard;
    private ChameGame.GameType gameType;
    private Gson gson = new Gson();
    private TicPlayer clientPlayerInstance;
    private AtomicBoolean clientTurn = new AtomicBoolean(false);
    private TicPlayer[] players = new TicPlayer[2];

    public TicTacToe(ChameGame.GameType gameType, Stage parentStage){
        super(ChameProtocol.TICTACTOE);
        this.gameType = gameType;
        this.parentStage = parentStage;

        GridPane root = new GridPane();
        root.getStylesheets().addAll(Main.class.getResource("resources/styles/tictactoe.css").toExternalForm());
        root.getStyleClass().add("tic_gridPane");
        ticBoard = new TicBoard();


        switch (this.gameType){
            case OFFLINE:{
                players[0] = new TicPlayer(MainViewController.clientUsername, TicButton.States.X, TicBoard.Winner.PLAYER1);
                players[1] = new TicAI(TicButton.States.O, TicBoard.Winner.PLAYER2);
                ticBoard.setTurn(players[0]);
                setUpGameGrid(root, ticBoard.getButtons(), offlineBoardButtonOnAction);
                startStage();

                break;
            }
            case ONLINE:{
                setUpGameGrid(root, ticBoard.getButtons(), onlineBoardButtonOnAction);
                break;
            }
        }




    }

    public void startStage(){
        //setup the scene
        gameStage = new Stage();
        gameStage.initModality(Modality.NONE);
        gameStage.initOwner(parentStage);
        gameStage.setScene(gameScene);
        gameStage.setTitle("Tic Tac Toe");
        gameStage.show();
        setGameStatus(GameStatus.STARTED);

        gameStage.setOnCloseRequest((e)->{
            if(getGameStatus() != GameStatus.FINISHED) {
                ChameMessage placeMarkerMesage = new ChameMessage(
                        ChameProtocol.GAME_ACTION,
                        gson.toJson(new ChameGameAction(ChameProtocol.TICTACTOE,
                                ChameProtocol.ABRUPT_LEAVE,
                                ""))
                );
                Connection.queueMessage(placeMarkerMesage);
            }
        });
    }

    public void doTurn(TicPlayer player, TicPoint point){

        if(gameType == GameType.OFFLINE && ticBoard.getTurn() != player)return;

            TicButton ticButton = ticBoard.getButton(point.getX(), point.getY());

            //do the move
            ticButton.setText(player.getMarker().toString().toUpperCase());
            ticButton.setState(player.getMarker());
            ticButton.setId(player.getMarker().toString().toLowerCase());
            ticButton.setDisable(true);

            if(this.gameType == GameType.OFFLINE){
                ticBoard.incrementTotalButtonsFilled();
                checkForWinner();
                changeTurn();
            }


    }

    public void checkForWinner(){

        TicBoard.Winner winner = ticBoard.checkForWin(ticBoard.getButtons(), GRID_SIZE);
        if(winner == null)
            return;

        TicPlayer winnerPlayer = null;
        for(TicPlayer p : players){
            if(p.getWinningTag() == winner){
                winnerPlayer = p;
                break;
            }
        }

        resultPopup(winner, winnerPlayer, gameStage);
    }

    public void changeTurn(){

        if(ticBoard.getTurn() == players[0])
            ticBoard.setTurn(players[1]);
        else
            ticBoard.setTurn(players[0]);
    }



    //sets up buttons
    public void setUpGameGrid(GridPane root, TicButton[][] buttons, EventHandler<ActionEvent> boardButtonOnAction)
    {
        for(int col = 0; col < GRID_SIZE; col++)
        {
            for(int row = 0; row < GRID_SIZE; row++)
            { 
                buttons[col][row] = new TicButton("");
                TicButton currentBtn = buttons[col][row];
                currentBtn.setOnAction(boardButtonOnAction);
                currentBtn.setPoint(new TicPoint(col, row));
                //Styling
                currentBtn.getStyleClass().add("tic_button");
                currentBtn.setPrefSize(100, 100);
                root.add(currentBtn, col, row);
            }
        }
        gameScene = new Scene(root, 300, 300);
    }


    private final EventHandler<ActionEvent> offlineBoardButtonOnAction = (ActionEvent event) -> {
        TicButton clickedButton = (TicButton) event.getSource();
        doTurn(players[0], clickedButton.getPoint());
        aiPlay();
    };

    private void aiPlay() {

        TicPoint nextComputerMove = ((TicAI) players[1]).findTheBestPoint(ticBoard);
        //Pick a random move just incase the minimax failed (it never fails but just incase :D)
        if(nextComputerMove != null) {
            doTurn(players[1], nextComputerMove);
        }

    }

    private EventHandler<ActionEvent> onlineBoardButtonOnAction = (ActionEvent event) -> {
        TicButton clickedButton = (TicButton) event.getSource();
        if(clientTurn.get()){

            TicPlaceMarkSkeleton ticPlaceMarkSkeleton = new TicPlaceMarkSkeleton(
                    clickedButton.getPoint(),
                    clientPlayerInstance.getMarker().name()
            );

            ChameMessage placeMarkerMesage  = new ChameMessage(
                    ChameProtocol.GAME_ACTION,
                    gson.toJson(new ChameGameAction(ChameProtocol.TICTACTOE,
                            TicProtocols.PLACE_MARK,
                            gson.toJson(ticPlaceMarkSkeleton)))
            );
            Connection.queueMessage(placeMarkerMesage);

            clientTurn.set(false);
        }

    };



    //result popup dialog
    public void resultPopup(TicBoard.Winner winner, TicPlayer winnerPlayer, Stage stage)
    {
        String labelStr = "";

        if(this.gameType == GameType.OFFLINE && winner == null)
            return;
        else if(winner == TicBoard.Winner.TIE)
            labelStr = "No one won!";
        else if(winnerPlayer!= null){
            labelStr = "Player " + winnerPlayer.getMarker().name() + " won! " + "(" + winnerPlayer.getUsername() + ")";
        }





        Button lb = new Button(labelStr);
        lb.setDisable(true);
        lb.setAlignment(Pos.CENTER);

        lb.setStyle("-fx-font-size: 2em; -fx-background-color:white;-fx-font-weight: bold;-fx-opacity: 1; -fx-text-fill: #17142c; -fx-focus-color: transparent; -fx-background-insets: 0, 0, 1, 2;");

        Button quitBtn = new Button("Close");
        quitBtn.setStyle("-fx-font-size: 1em; -fx-font-weight: bold; -fx-text-fill: #17142c; -fx-focus-color: transparent; -fx-background-insets: 0, 0, 1, 2;");
        quitBtn.setPrefSize(400, 100);
        quitBtn.setAlignment(Pos.CENTER);
        quitBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {

                stage.close();
            }
        });

        final Stage dialog = new Stage();
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.initOwner(stage);
                dialog.setTitle("Winner winner! Chicken Dinner!");
                GridPane dialogVbox = new GridPane();
                dialogVbox.setAlignment(Pos.CENTER);
                dialogVbox.add(lb, 0, 0);
                dialogVbox.add(quitBtn, 0, 1);
                Scene dialogScene = new Scene(dialogVbox, 400, 100);
                dialog.setScene(dialogScene);
                dialog.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent arg0) {
                        stage.close();

                    }
                });
                dialog.showAndWait();
    }


    @Override
    public void handleServerResponse(ChameGameAction chameGameAction) {
        switch (chameGameAction.getActionType()){
            case ChameProtocol.START_GAME:{
                TicGameStartSkeleton ticGameStartSkeleton = gson.fromJson(chameGameAction.getBody(), TicGameStartSkeleton.class);
                players[0] = new TicPlayer(ticGameStartSkeleton.getPlayer1(), TicButton.States.valueOf(ticGameStartSkeleton.getPlayer1Mark()), TicBoard.Winner.PLAYER1);
                players[1] = new TicPlayer(ticGameStartSkeleton.getPlayer2(), TicButton.States.valueOf(ticGameStartSkeleton.getPlayer2Mark()), TicBoard.Winner.PLAYER2);
                clientPlayerInstance = (players[0].getUsername().equalsIgnoreCase(MainViewController.clientUsername)) ? players[0] : players[1];
                Platform.runLater(this::startStage);
                break;
            }
            case TicProtocols.CHANGE_TURN:{
                clientTurn.set(Boolean.parseBoolean(chameGameAction.getBody()));
                break;
            }
            case TicProtocols.PLACE_MARK:{
                TicPlaceMarkSkeleton ticPlaceMarkSkeleton = gson.fromJson(chameGameAction.getBody(), TicPlaceMarkSkeleton.class);
                for(TicPlayer p : players){
                    if(TicButton.States.valueOf(ticPlaceMarkSkeleton.getMark()) == p.getMarker()){
                        Platform.runLater(()->doTurn(p, ticPlaceMarkSkeleton.getTicPoint()));
                        break;
                    }
                }
                break;
            }
            case ChameProtocol.END_GAME:{
                setGameStatus(GameStatus.FINISHED);
                String winnerUsername = chameGameAction.getBody();
                if(winnerUsername.equalsIgnoreCase(ChameProtocol.TIE)){
                    Platform.runLater(()->resultPopup(TicBoard.Winner.TIE, null, gameStage));
                }else {
                    for(TicPlayer p : players){
                        if(p.getUsername().equalsIgnoreCase(winnerUsername)){
                            Platform.runLater(()->resultPopup(null, p, gameStage));
                            break;
                        }
                    }
                }
                Platform.runLater(this::removeGame);
                break;
            }
        }
    }

    private void removeGame() {
        ControllerManager.getController(GameTabController.class).removeGame();
    }
}






