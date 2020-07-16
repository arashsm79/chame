package main.mainview.Chat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import main.ChameController;
import main.ControllerManager;
import main.Main;
import main.ServerResponseHandler;
import main.connection.ChameProtocol;
import main.connection.Connection;
import main.connection.skeletons.*;
import main.mainview.Chat.skeletons.ChatMessageSkeleton;
import main.mainview.Chat.skeletons.RoomMessageRequestSkeleton;
import main.mainview.Chat.skeletons.RoomMessageResponseSkeleton;
import main.mainview.Chat.skeletons.RoomSkeleton;
import main.mainview.Game.skeletons.GameRoomSkeleton;
import main.mainview.MainViewController;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.ResourceBundle;

//controller for chat tab
public class ChatTabController implements Initializable, ChameController, ServerResponseHandler {

    private Stage stage;
    private Scene scene;
    private Parent root;

    private Gson gson = new Gson();

    @FXML
    private Button deleteMessage_button;

    @FXML
    private Button refreshLobbies_button;

    @FXML
    private ListView<RoomSkeleton> rooms_listView;

    @FXML
    private Label roomName_label;

    @FXML
    private ListView<GameRoomSkeleton> gameLobbies_listView;

    @FXML
    private ListView<ChatMessageSkeleton> chat_listView;

    @FXML
    private TextArea chat_textArea;

    @FXML
    private Button send_button;

    private ObservableList<RoomSkeleton> roomsObservableList = FXCollections.observableArrayList();

    //initializes the listviews and sends a request for room list
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ControllerManager.putController(ChatTabController.class, this);

        rooms_listView.setItems(roomsObservableList);
        rooms_listView.setCellFactory(roomsObservableList -> new RoomListViewCell());
        rooms_listView.getSelectionModel().selectedItemProperty().addListener(roomsListItemSelectedChangeListener);

        gameLobbies_listView.setCellFactory(gameLobbiesLisView -> new GameLobbiesListViewCell());
        gameLobbies_listView.getStylesheets().addAll(Main.class.getResource("resources/styles/fancylistview.css").toExternalForm());
        gameLobbies_listView.getStylesheets().addAll(Main.class.getResource("resources/styles/gamesGridPane.css").toExternalForm());
        gameLobbies_listView.getSelectionModel().selectedItemProperty().addListener(gameLobbiesListItemSelectedChangeListener);

        chat_listView.setCellFactory( chatMessageSkeletonListView -> new ChatMessageListViewCell());

        sendRoomsListRequest();

    }

    //send a request for the list of rooms
    public void sendRoomsListRequest() {
        ChameMessage chameMessage = new ChameMessage(
                ChameProtocol.GET_ROOMS,
                ""
        );

        Connection.queueMessage(chameMessage);
    }


    //send the message (if valid) to server
    @FXML
    void sendButtonOnAction(ActionEvent event) {

        if(!validInputText())
            return;

        RoomSkeleton rs = rooms_listView.getSelectionModel().getSelectedItem();
        ChatMessageSkeleton cms = new ChatMessageSkeleton();
        cms.setBody(chat_textArea.getText());
        cms.setRoom_id(rs.getId());
        cms.setType("text");


        ChameMessage chameMessage = new ChameMessage(
                ChameProtocol.CHAT_MESSAGE,
                gson.toJson(cms)
        );

        Connection.queueMessage(chameMessage);
        chat_textArea.setText("");
    }

    //validate sent message
    private boolean validInputText() {

        boolean isAcceptable = true;

        if(!chat_textArea.getText().matches("^\\p{ASCII}{1,150}$"))
            isAcceptable=false;

        if(!isAcceptable)
            Main.popupError("Please enter valid input.\nAll entries should also be more than 6 characters");

        return isAcceptable;    }

    @Override
    public void initController(Stage stage, Scene scene, Parent root)
    {
        this.scene = scene;
        this.stage = stage;
        this.root = root;
    }


    //creates a new room
    public void sendRoomCreationRequest(RoomSkeleton roomSkeleton){

        ChameMessage chameMessage = new ChameMessage(
                ChameProtocol.CREATE_ROOM,
                gson.toJson(roomSkeleton)
        );

        Connection.queueMessage(chameMessage);

    }

    //join the game after client clicks on a lobby
    private ChangeListener<GameRoomSkeleton> gameLobbiesListItemSelectedChangeListener =
            (ObservableValue<? extends GameRoomSkeleton> ov,
             GameRoomSkeleton old_val, GameRoomSkeleton new_val) -> {
        if(new_val == null)
            return;
        ChameMessage chameMessage = new ChameMessage(
                ChameProtocol.JOIN_GAME,
                gson.toJson(new_val)
        );
        Connection.queueMessage(chameMessage);

    };

    //updates the list views related to rooms like chats and game lobbies
    private ChangeListener<RoomSkeleton> roomsListItemSelectedChangeListener =
            (ObservableValue<? extends RoomSkeleton> ov,
             RoomSkeleton old_val, RoomSkeleton new_val) -> {
        if(new_val == null)
            return;
        new_val.setUnreadMessages(0);
        //if its the first time (get all the messages
        //send a request to server for new messages, they'll populate the obeservable list when they arrive
        if(new_val.getChatsObservableList() == null){
            requestRoomMessages(new_val, 0);
            new_val.setChatsObservableList(FXCollections.observableArrayList());
            chat_listView.setItems(new_val.getChatsObservableList());
        }else{
            //else only get the messages that have been created after the last message in the list
            long lastMessageTime = 0;
            if(!new_val.getChatsObservableList().isEmpty()){
                lastMessageTime = new_val.getChatsObservableList().get(new_val.getChatsObservableList().size() - 1).getCreateDate();
            }
            requestRoomMessages(new_val, lastMessageTime);
            chat_listView.setItems(new_val.getChatsObservableList());
        }

        roomName_label.setText(new_val.getName());

        //game lobbies
        if(new_val.getGameLobbiesObservableList() == null){
            requestRoomLobbies(new_val);
            new_val.setGameLobbiesObservableList(FXCollections.observableArrayList());
            gameLobbies_listView.setItems(new_val.getGameLobbiesObservableList());

        }else{
            requestRoomLobbies(new_val);
            //refresh the lobbies and wait for more
            new_val.getGameLobbiesObservableList().clear();
            gameLobbies_listView.setItems(new_val.getGameLobbiesObservableList());
        }

    };

    //request all lobbies in this room
    private void requestRoomLobbies(RoomSkeleton new_val) {

        ChameMessage chameMessage = new ChameMessage(
                ChameProtocol.GET_ROOM_GAME_LOBBIES,
                Integer.toString(new_val.getId())
        );
        Connection.queueMessage(chameMessage);

    }

    //requests all the messages after the given time in milisecs
    private void requestRoomMessages(RoomSkeleton new_val, long afterTime) {

        ChameMessage chameMessage = new ChameMessage(
                ChameProtocol.GET_ROOM_MESSAGES,
                gson.toJson(new RoomMessageRequestSkeleton(new_val, afterTime))
        );
        Connection.queueMessage(chameMessage);

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

    //handle server response
    @Override
    public void handleServerResponse(ChameMessage chameMessage) {
        switch (chameMessage.getHeader()){
            case ChameProtocol.ROOMS_LIST:{
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        populateRoomsListView(chameMessage);
                    }
                });
                break;
            }
            case ChameProtocol.ROOM_MESSAGES:{
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        populateChatListView(chameMessage);
                    }
                });
                break;
            }
            case ChameProtocol.ROOM_GAME_LOBBIES:{
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        populateGameLobbyView(chameMessage);
                    }
                });
                break;
            }
        }
    }

    //add games to game lobby list view
    private void populateGameLobbyView(ChameMessage chameMessage) {
        Type collectionType = new TypeToken<HashSet<GameRoomSkeleton>>() {}.getType();

        HashSet<GameRoomSkeleton> rs = gson.fromJson(chameMessage.getBody(), collectionType);

        ArrayList<GameRoomSkeleton> rsArrayList = null;

        if(rs == null)
            rsArrayList = new ArrayList<>();
        else
            rsArrayList = new ArrayList<>(rs);

        int room_id = -1;

        if(rsArrayList.size() != 0){
            room_id = rsArrayList.get(0).getRoomID();
            for(RoomSkeleton curRoom : roomsObservableList){
                if(curRoom.getId() == room_id){
                    curRoom.getGameLobbiesObservableList().setAll(rsArrayList);
                    break;
                }
            }
        }

    }

    //add chats to chat lobby list view
    private void populateChatListView(ChameMessage chameMessage) {
        RoomMessageResponseSkeleton rs = gson.fromJson(chameMessage.getBody(), RoomMessageResponseSkeleton.class);
        int room_id = rs.getRoom_id();
        ArrayList<ChatMessageSkeleton> roomMessageList = rs.getRoomMessageList();
        for(RoomSkeleton curRoom : roomsObservableList){
            if(curRoom.getId() == room_id){
                if(curRoom.getChatsObservableList() == null){
                    curRoom.setChatsObservableList(FXCollections.observableArrayList());
                }
                curRoom.getChatsObservableList().addAll(roomMessageList);
                break;
            }
        }
    }

    //add rooms to rooms list view
    private void populateRoomsListView(ChameMessage chameMessage) {

        Type collectionType = new TypeToken<ArrayList<RoomSkeleton>>() {}.getType();
        //parse the json
        ArrayList<RoomSkeleton> parsedContactList = gson.fromJson(chameMessage.getBody(), collectionType);
        roomsObservableList.clear();
        roomsObservableList.addAll(parsedContactList);
    }

    public ListView<RoomSkeleton> getRooms_listView() {
        return rooms_listView;
    }


    //refresh the game lobbies for the selected room
    @FXML
    private void refreshLobbiesButtonOnAction(ActionEvent actionEvent) {

        RoomSkeleton new_val = rooms_listView.getSelectionModel().getSelectedItem();
        if(new_val == null)
            return;
        //game lobbies
        if(new_val.getGameLobbiesObservableList() == null){
            requestRoomLobbies(new_val);
            new_val.setGameLobbiesObservableList(FXCollections.observableArrayList());
            gameLobbies_listView.setItems(new_val.getGameLobbiesObservableList());

        }else{
            requestRoomLobbies(new_val);
            //refresh the lobbies and wait for more
            new_val.getGameLobbiesObservableList().clear();
            gameLobbies_listView.setItems(new_val.getGameLobbiesObservableList());
        }
    }

    public void deleteMessageButtonOnAction(ActionEvent actionEvent) {

        ChatMessageSkeleton cms = chat_listView.getSelectionModel().getSelectedItem();
        if(!cms.getSenderUsername().equalsIgnoreCase(MainViewController.clientUsername)){
            Main.popupError("You can only delete your own messages.");
            return;
        }


        ChameMessage chameMessage = new ChameMessage(
                ChameProtocol.DELETE_MESSAGE,
                gson.toJson(cms)
        );
        Connection.queueMessage(chameMessage);

        Main.popupInfo("Message has been deleted in the database.\nIt won't be shown here on next logins");


    }


    public static class RoomListViewCell extends ListCell<RoomSkeleton> {

        public static Image groupImage;
        public static Image privateImage;
        static {
            try {
                groupImage = new Image(new FileInputStream("src/main/resources/icons/group_room.png"));
                privateImage = new Image(new FileInputStream("src/main/resources/icons/private_room.png"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        @Override
        protected void updateItem(RoomSkeleton roomSkeleton, boolean empty) {
            super.updateItem(roomSkeleton, empty);
            if(empty || roomSkeleton== null) {

                setText(null);
                setGraphic(null);

            } else {

                HBox root = new HBox();
                ImageView im = new ImageView();

                im.setFitHeight(60);
                im.setFitWidth(60);
                im.setPreserveRatio(true);

                if(roomSkeleton.isPrivate())
                    im.setImage(privateImage);
                else
                    im.setImage(groupImage);

                root.setMinSize(Double.MIN_VALUE, 70);
                Label lb = new Label(roomSkeleton.getName() + "\n#" + Integer.toString(roomSkeleton.getId()));
                HBox.setMargin(lb, new Insets(0, 20, 0, 10));
                StackPane unreadStackPane = new StackPane();
                Circle unreadCircle = new Circle(10);
                unreadCircle.setStrokeWidth(0);
                unreadCircle.setFill(Color.rgb(200, 200, 200));

                //sets the value of unread messages string property
                roomSkeleton.getUnreadMessagesString().setValue(Integer.toString(roomSkeleton.getUnreadMessages()));
                Label unreadLabel = new Label(Integer.toString(roomSkeleton.getUnreadMessages()));
                unreadLabel.textProperty().bind(roomSkeleton.getUnreadMessagesString());
                unreadStackPane.getChildren().addAll(unreadCircle, unreadLabel);



                root.setAlignment(Pos.CENTER_LEFT);
                root.getChildren().addAll(im, lb, unreadStackPane);
                setText(null);
                setGraphic(root);
            }
        }
    }

    private static class ChatMessageListViewCell extends ListCell<ChatMessageSkeleton> {

        public static Image avatar;
        static {
            try {
                avatar = new Image(new FileInputStream("src/main/resources/icons/friend.png"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        @Override
        protected void updateItem(ChatMessageSkeleton chatMessageSkeleton, boolean empty) {
            super.updateItem(chatMessageSkeleton, empty);

            if(empty || chatMessageSkeleton== null) {

                setText(null);
                setGraphic(null);

            } else {

                HBox root = new HBox();
                root.setPrefWidth(300);

                ImageView im = new ImageView();
                im.setImage(avatar);
                im.setFitHeight(60);
                im.setFitWidth(60);
                im.setPreserveRatio(true);

                VBox labelsVbox = new VBox();
                HBox.setHgrow(labelsVbox, Priority.ALWAYS);

                Label username = new Label(chatMessageSkeleton.getSenderUsername());
                username.setStyle("-fx-font-weight: bold;");
                Label messageBody = new Label(chatMessageSkeleton.getBody());

                HBox dateHbox = new HBox();
                GregorianCalendar gc = new GregorianCalendar();
                gc.setTimeInMillis(chatMessageSkeleton.getCreateDate());
                Label date = new Label(gc.getTime().toString());
                dateHbox.getChildren().add(date);

                labelsVbox.getChildren().addAll(username, messageBody, dateHbox);
                //labelsVbox.getStyleClass().addAll("pane");
                if(chatMessageSkeleton.getSenderUsername().equalsIgnoreCase(MainViewController.clientUsername)){
                    labelsVbox.setId("clientchat");
                    root.getChildren().addAll(labelsVbox, im);
                    labelsVbox.setPadding(new Insets(5, 30, 5, 5));
                    dateHbox.setAlignment(Pos.BOTTOM_LEFT);
                } else{
                    labelsVbox.setId("otherschat");
                    root.getChildren().addAll(im, labelsVbox);
                    dateHbox.setAlignment(Pos.BOTTOM_RIGHT);
                    labelsVbox.setPadding(new Insets(5, 5, 5, 30));
                }


                setText(null);
                setGraphic(root);
            }
        }
    }

    private static class GameLobbiesListViewCell extends ListCell<GameRoomSkeleton> {

        @Override
        protected void updateItem(GameRoomSkeleton gameRoomSkeleton, boolean empty) {
            super.updateItem(gameRoomSkeleton, empty);

            if(empty || gameRoomSkeleton == null) {

                setText(null);
                setGraphic(null);

            } else {

                HBox root = new HBox();
                root.setPrefWidth(300);

                Label gameLabel = new Label(gameRoomSkeleton.getGameName());
                gameLabel.setPrefSize(32, 32);
                gameLabel.getStyleClass().addAll("game");
                gameLabel.setId(gameRoomSkeleton.getGameName().toLowerCase());
                Insets i = new Insets(5);
                Label capacity = new Label(gameRoomSkeleton.getPlayersInside() + "/" + gameRoomSkeleton.getCapacity());
                Label ownerUsername = new Label(gameRoomSkeleton.getOwnerUsername());

                HBox.setMargin(gameLabel, i);
                HBox.setMargin(capacity, i);
                HBox.setMargin(ownerUsername, i);
                root.setAlignment(Pos.CENTER_LEFT);
                root.getChildren().addAll(gameLabel, capacity, ownerUsername);
                setText(null);
                setGraphic(root);
            }
        }
    }

}
