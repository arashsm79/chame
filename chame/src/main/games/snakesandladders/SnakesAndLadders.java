package main.games.snakesandladders;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import main.connection.ChameProtocol;
import main.games.ChameGame;
import main.games.ChameGameAction;
import main.games.tictactoe.TicBoard;
import main.games.tictactoe.TicPlayer;
import main.mainview.MainViewController;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class SnakesAndLadders extends ChameGame implements Initializable {

    public static final int GRID_SIZE = 10;
    @FXML
    private Label turn_label;

    ExecutorService animationThreadPool = Executors.newCachedThreadPool();
    @FXML
    private GridPane slBoard_gridPane;

    @FXML
    private ImageView die_imageView;

    @FXML
    private Button roll_button;

    @FXML
    private GridPane outsidePieces_gridPane;

    private IntegerProperty dieNumberProperty = new SimpleIntegerProperty(0);

    private IntegerProperty turnIndexProperty = new SimpleIntegerProperty(0);

    private Stage parentStage;
    private Stage gameStage;
    private Scene gameScene;
    private SlBoard slBoard;
    private ChameGame.GameType gameType;
    private Gson gson = new Gson();

    private AtomicBoolean clientTurn = new AtomicBoolean(false);
    private SlPlayer[] players;
    private int playerCount;
    private int numberOfPlayers;


    public SnakesAndLadders() {
        super(ChameProtocol.SNAKES_AND_LADDERS);

    }
    public void init(Parent root, GameType gameType, Stage parentStage, int numberOfPlayers){
        this.gameType = gameType;
        this.parentStage = parentStage;
        this.numberOfPlayers = numberOfPlayers;
        players = new SlPlayer[numberOfPlayers];

        switch (gameType){
            case OFFLINE:{
                startStage(root);
                setupBoard(numberOfPlayers);
                turnIndexProperty.set(0);
                    break;
            }
            case ONLINE:{

                break;
            }
        }

    }

    public void startStage(Parent root) {
        gameScene = new Scene(root);
        gameStage = new Stage();
        gameStage.initModality(Modality.NONE);
        gameStage.initOwner(parentStage);
        gameStage.setScene(gameScene);
        gameStage.setTitle("Snakes and Ladders");
        gameStage.show();
        setGameStatus(GameStatus.STARTED);
    }

    public void setupBoard(int numberOfPlayers){

        slBoard = new SlBoard(slBoard_gridPane);
        for(int i = 0; i < numberOfPlayers; i++){
            addPlayer();
        }


    }

    public void addPlayer(){
        if(playerCount == 0){
            players[0] = new SlPlayer(MainViewController.clientUsername, Pieces.RED, new SlPoint(-1, -1), 0, true);
            addPieceToSide(Pieces.RED);
            playerCount++;
        }else if(playerCount == 1){
            players[1] = new SlAI(Pieces.GREEN);
            addPieceToSide(Pieces.GREEN);
            playerCount++;
        }else if(playerCount == 2){
            players[2] = new SlAI(Pieces.YELLOW);
            addPieceToSide(Pieces.YELLOW);
            playerCount++;
        }else if(playerCount == 3){
            players[3] = new SlAI(Pieces.BLUE);
            addPieceToSide(Pieces.BLUE);
            playerCount++;
        }
    }

    private void addPieceToSide(Pieces piece){
        switch (piece){
            case RED:{
                Label l = (Label) outsidePieces_gridPane.getChildren().get(0);
                l.setId("red-piece");
                break;
            }
            case GREEN:{
                Label l = (Label) outsidePieces_gridPane.getChildren().get(2);
                l.setId("green-piece");
                break;
            }
            case YELLOW:{
                Label l = (Label) outsidePieces_gridPane.getChildren().get(1);
                l.setId("yellow-piece");
                break;
            }
            case BLUE:{
                Label l = (Label) outsidePieces_gridPane.getChildren().get(3);
                l.setId("blue-piece");
                break;
            }
        }
    }
    private void removePieceToSide(Pieces piece){

        switch (piece){
            case RED:{
                Label l = (Label) outsidePieces_gridPane.getChildren().get(0);
                l.setId("");
                break;
            }
            case GREEN:{
                Label l = (Label) outsidePieces_gridPane.getChildren().get(2);
                l.setId("");
                break;
            }
            case YELLOW:{
                Label l = (Label) outsidePieces_gridPane.getChildren().get(1);
                l.setId("");
                break;
            }
            case BLUE:{
                Label l = (Label) outsidePieces_gridPane.getChildren().get(3);
                l.setId("");
                break;
            }
        }
    }

    @FXML
    void rollButtonOnAction(ActionEvent event) {

//        if(gameType == GameType.OFFLINE){
//            if(getPlayer(turnIndexProperty.intValue()) != players[0])
//                return;
//        }

        animationThreadPool.execute(()->{
            int dieNumber = 0;
            try {
                for(int i = 0; i < 10; i++){
                    dieNumber = (((int)(Math.random() * 10) % 6) + 1);
                    dieNumberProperty.setValue(dieNumber);
                    Thread.sleep(100);
                }
                Platform.runLater(()->doTurn(getPlayer(turnIndexProperty.intValue()), dieNumberProperty.getValue()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });
    }

    private void doTurn(SlPlayer slPlayer, int dieNum){

        if(getGameStatus() != GameStatus.STARTED)
            return;

        if(slPlayer.isOut() && (dieNum % 2 == 0 )){
            start(slPlayer);
            slPlayer.setOut(false);
            return;
        }else if(slPlayer.isOut()){
            changeTurn();
            return;
        }else if(!slPlayer.isOut()){
            move(slPlayer, dieNum);
        }


    }

    private void changeTurn() {
        int nextTurnIndex = (turnIndexProperty.intValue()+1) % 4;
        Platform.runLater(()->
        turnIndexProperty.set(nextTurnIndex));

    }

    private void move(SlPlayer slPlayer, int dieNum){
        animationThreadPool.execute(()->{

            SlPoint nextPos = new SlPoint();
            for(int i = 0; i < dieNum; i++){

                    int pX = slPlayer.getPosition().getX();
                    int pY = slPlayer.getPosition().getY();

                    nextPos.setX(pX);
                    nextPos.setY(pY);

                    //move to left
                    if(pY % 2 == 0){
                        //makes sure if it is going to exceed from left move up if possible
                        if(pX == 0){
                            //check if this is the 100th block
                            if(pY == 0){
                                endGame(slPlayer);
                                return;
                            }

                            //makes sure that it can go up
                            if(pY > 0){
                                nextPos.setY(pY - 1);
                            }
                        //if it has space move to left
                        }else if(pX > 0) {
                            nextPos.setX(pX - 1);
                        }
                    //move to right
                    }else if(pY % 2 == 1){

                        //makes sure if it is going to exceed from right move up if possible
                        if(pX == GRID_SIZE - 1){
                            //makes sure that it can go up
                            if(pY > 0){
                                nextPos.setY(pY - 1);
                            }
                        //if has space move to right
                        }else if(pX < GRID_SIZE - 1){
                            nextPos.setX(pX + 1);
                        }
                    }

                    try {
                        Thread.sleep(200);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if(i == dieNum-1)
                        finalMove(slPlayer, nextPos);
                    else
                        singleMove(slPlayer, nextPos);

            }
            changeTurn();
        });

    }

    private void endGame(SlPlayer slPlayer) {
        setGameStatus(GameStatus.FINISHED);
        resultPopup(slPlayer, gameStage);
    }

    //result popup dialog
    public void resultPopup(SlPlayer winnerPlayer, Stage stage)
    {
        String labelStr = winnerPlayer.getPiece().name() + " (" + winnerPlayer.getUsername() + ") won!";

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

    private void finalMove(SlPlayer slPlayer, SlPoint nextPos) {

        SlButton nextButton = slBoard.getButton(nextPos);
        singleMove(slPlayer, nextPos);

        if(nextButton.getStandingPlayer() != null){
            SlPlayer hitPlayer = nextButton.getStandingPlayer();
            nextButton.setStandingPlayer(null);
            hitPlayer.setOut(true);
            hitPlayer.setPosition(-1, -1);
            nextButton.setId(slPlayer.getPiece().getId());
            addPieceToSide(hitPlayer.getPiece());
        }

        switch (nextButton.getType()){
            case EMPTY:{
                nextButton.setStandingPlayer(slPlayer);
                break;
            }
            case SNAKE:{
                SlSnake snake = (SlSnake) nextButton.getPieceMover();
                try {
                    Thread.sleep(200);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finalMove(slPlayer, snake.getEnd());
                break;
            }
            case LADDER:{
                SlLadder ladder = (SlLadder) nextButton.getPieceMover();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finalMove(slPlayer, ladder.getEnd());
                break;
            }
        }

    }

    private void singleMove(SlPlayer slPlayer, SlPoint point){

        if(point.getY() < 0 || point.getX() < 0)
            return;
        if(slPlayer.getPosition().getY() < 0 || slPlayer.getPosition().getX() < 0)
            return;

        SlButton previousButton = slBoard.getButton(slPlayer.getPosition());
        SlButton nextButton = slBoard.getButton(point);


        if(previousButton.getStandingPlayer() == slPlayer || previousButton.getStandingPlayer() == null){
            previousButton.setStandingPlayer(null);
            previousButton.setId("");
        }

        if(nextButton.getStandingPlayer() == null)
            nextButton.setId(slPlayer.getPiece().getId());

        slPlayer.setPosition(point);
    }

    private void start(SlPlayer slPlayer) {

        SlButton nextButton = slBoard.getButton(new SlPoint(0, 9));

        if(nextButton.getStandingPlayer() != null){
            SlPlayer hitPlayer = nextButton.getStandingPlayer();
            hitPlayer.setOut(true);
            hitPlayer.setPosition(-1, -1);
            addPieceToSide(hitPlayer.getPiece());
        }

        nextButton.setId(slPlayer.getPiece().getId());
        slPlayer.setPosition(0, 9);
        nextButton.setStandingPlayer(slPlayer);

        removePieceToSide(slPlayer.getPiece());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dieNumberProperty.addListener((ob, oldV, newV)->{
            die_imageView.setId("die"+newV.intValue());
        });

        turnIndexProperty.addListener((ob, oldV, newV)->{
            turn_label.setId(getPlayer(newV.intValue()).getPiece().getId());
            turn_label.setText(getPlayer(newV.intValue()).getPiece().name());
        });

        turn_label.setId(Pieces.RED.getId());
        turn_label.setText("RED");

        for(int i = 0; i < 2; i++){
            for(int j = 0; j < 2; j++){

                Label l = new Label();
                l.getStyleClass().addAll("piece");
                l.setPrefSize(60, 60);
                outsidePieces_gridPane.add(l, i, j);
            }
        }

    }

    private SlPlayer getPlayer(int i ){
        return players[i];
    }

    @Override
    public void handleServerResponse(ChameGameAction chameGameAction) {

    }

    //an enum for pieces
    public enum Pieces{
        RED(0, "red-piece"),
        BLUE(1, "blue-piece"),
        YELLOW(2, "yellow-piece"),
        GREEN(3, "green-piece");

        private final int value;
        private final String id;

        Pieces(final int newValue, final String newID) {
            id = newID;
            value = newValue;
        }
        public int getValue() { return value; }

        public String getId() {
            return id;
        }
    }
}
