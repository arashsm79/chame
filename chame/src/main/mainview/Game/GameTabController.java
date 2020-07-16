package main.mainview.Game;

import com.google.gson.Gson;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.ChameController;
import main.ControllerManager;
import main.Main;
import main.ServerResponseHandler;
import main.auth.LogInController;
import main.connection.ChameProtocol;
import main.connection.Connection;
import main.connection.skeletons.ChameMessage;
import main.games.snakesandladders.SnakesAndLadders;
import main.mainview.Game.skeletons.GameRoomSkeleton;
import main.mainview.Chat.skeletons.RoomSkeleton;
import main.games.ChameGame;
import main.games.ChameGameAction;
import main.games.tictactoe.TicTacToe;
import main.mainview.Chat.ChatTabController;
import main.mainview.MainViewController;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

// a controller for games tab
public class GameTabController implements Initializable, ChameController, ServerResponseHandler {

    @FXML
    private GridPane games_gridPane;
    @FXML
    private Parent root;

    private Stage stage;
    private Scene scene;

    private ChameGame currentPlayingGame;

    private final Gson gson = new Gson();
    //initialize the controller
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ControllerManager.putController(GameTabController.class, this);
        populateGameGrid();

    }

    //populate the game grid with available games
    private void populateGameGrid() {
        games_gridPane.getStylesheets()
                .addAll(Main.class.getResource("resources/styles/gamesGridPane.css").toExternalForm());
        games_gridPane.getStyleClass().add("tic_gridPane");
        Button tictactoeButton = new Button("Tic Tac Toe");
        tictactoeButton.setPrefSize(200, 200);
        tictactoeButton.getStyleClass().addAll("game");
        tictactoeButton.setId("tictactoe");
        tictactoeButton.setOnAction(gameButtonOnAction);
        games_gridPane.add(tictactoeButton, 0, 0);

        Button snakesandladdersButton = new Button("Snakes and Ladders");
        //snakesandladdersButton.setDisable(true);
        snakesandladdersButton.setPrefSize(200, 200);
        snakesandladdersButton.getStyleClass().addAll("game");
        snakesandladdersButton.setId("snakesandladders");
        snakesandladdersButton.setOnAction(gameButtonOnAction);
        games_gridPane.add(snakesandladdersButton, 1, 0);


    }

    //on game click
    private final EventHandler<ActionEvent> gameButtonOnAction = (ActionEvent event) -> {

        Button gameButton = (Button) event.getSource();

        final Stage dialog = new Stage();
        dialog.setTitle(gameButton.getText());
        dialog.initModality(Modality.NONE);
        dialog.initOwner(stage);

        VBox dialogVbox = new VBox();
        dialogVbox.setAlignment(Pos.CENTER);
        dialogVbox.getStylesheets()
                .addAll(Main.class.getResource("resources/styles/gamesGridPane.css").toExternalForm());

        Label gameLabel = new Label(gameButton.getText());
        gameLabel.setPrefSize(200, 200);
        gameLabel.getStyleClass().addAll("game");
        gameLabel.setId(gameButton.getId());


        Button computerBtn = new Button("Play Against Computer");
        computerBtn.setPrefSize(200,60);
        computerBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                try {

                switch (gameButton.getId()){
                    case "tictactoe":
                        currentPlayingGame = new TicTacToe(ChameGame.GameType.OFFLINE, stage);
                        break;
                    case "snakesandladders":{
                        FXMLLoader loader = new FXMLLoader(Main.class.getResource("resources/fxml/SlBoard.fxml"));
                        Parent slRoot = loader.load();
                        SnakesAndLadders snakesAndLadders = loader.getController();
                        snakesAndLadders.init(slRoot, ChameGame.GameType.OFFLINE, stage, 4);
                        currentPlayingGame = snakesAndLadders;
                        break;
                    }
                }
                }catch (IOException e){
                    e.printStackTrace();
                }
                dialog.close();
            }
        });

        Button createNewGameBtn = new Button("Play With Friends");
        createNewGameBtn.setPrefSize(200,60);
        createNewGameBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {

                final Stage roomChooser = new Stage();

                ListView<RoomSkeleton> availableRooms = new ListView<>();
                availableRooms.getStylesheets().addAll(Main.class.getResource("resources/styles/fancylistview.css").toExternalForm());
                availableRooms.getItems().addAll(
                        ControllerManager.getController(ChatTabController.class).getRooms_listView().getItems()
                );

                int[] selectedRoomID = {-1};
                availableRooms.setCellFactory(roomsObservableList -> new ChatTabController.RoomListViewCell());
                availableRooms.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends RoomSkeleton> ov,
                                                                                        RoomSkeleton old_val, RoomSkeleton new_val)->{
                    selectedRoomID[0] = new_val.getId();
                    roomChooser.close();
                    dialog.close();
                });

                availableRooms.getStyleClass().addAll("inner_box");


                VBox chooserParent = new VBox();
                VBox.setMargin(availableRooms, new Insets(20));
                chooserParent.getStyleClass().addAll("pane");
                chooserParent.setId("game-pane");
                chooserParent.getChildren().addAll(availableRooms);

                roomChooser.setTitle(gameButton.getText());
                roomChooser.initModality(Modality.NONE);
                roomChooser.initOwner(stage);

                Scene roomChooserDialogScene = new Scene(chooserParent, 400, 700);
                roomChooser.setScene(roomChooserDialogScene);
                roomChooser.showAndWait();

                if(selectedRoomID[0] != -1){
                    GameRoomSkeleton grs = new GameRoomSkeleton();
                    grs.setGameName(gameButton.getId());
                    grs.setRoomID(selectedRoomID[0]);
                    grs.setOwnerUsername(MainViewController.clientUsername);
                    ChameMessage chameMessage = new ChameMessage(
                            ChameProtocol.CREATE_GAME,
                            gson.toJson(grs)
                    );
                    Connection.queueMessage(chameMessage);
                }
            }
        });

        Insets i = new Insets(10);
        VBox.setMargin(computerBtn, i);
        VBox.setMargin(createNewGameBtn, i);
        VBox.setMargin(gameLabel, i);
        dialogVbox.getChildren().addAll(gameLabel, computerBtn, createNewGameBtn);
        dialogVbox.getStyleClass().addAll("pane");
        dialogVbox.setId("game-pane");
        Scene dialogScene = new Scene(dialogVbox, 300, 400);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    };


    //initialize the controller
    @Override
    public void initController(Stage stage, Scene scene, Parent root)
    {
        this.scene = scene;
        this.stage = stage;
        this.root = root;
    }

    //getters
    @Override
    public Scene getScene() {
        return this.scene;
    }

    @Override
    public Parent getRoot() {
        return this.root;
    }

    @Override
    public Stage getStage() {
        return this.stage;
    }

    //remove the current game
    public void removeGame(){
        this.currentPlayingGame = null;
    }

    //handle server response
    @Override
    public void handleServerResponse(ChameMessage chameMessage) {
        switch (chameMessage.getHeader()){
            case ChameProtocol.GAME_ACTION:{
                ChameGameAction gameAction = gson.fromJson(chameMessage.getBody(), ChameGameAction.class);
                if(gameAction.getActionType().equalsIgnoreCase(ChameProtocol.START_GAME)){
                    switch (gameAction.getGameName()){
                        case ChameProtocol.TICTACTOE:{
                            if(currentPlayingGame == null){
                                currentPlayingGame = new TicTacToe(ChameGame.GameType.ONLINE, stage);
                            }
                            break;
                        }
                    }
                }
                if (currentPlayingGame!=null && gameAction.getGameName().equalsIgnoreCase(currentPlayingGame.getGameName())){
                    currentPlayingGame.handleServerResponse(gameAction);
                }
                break;
            }
        }
    }


}
