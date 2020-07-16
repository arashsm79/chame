package main.mainview.Contact;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.ChameController;
import main.ControllerManager;
import main.Main;
import main.ServerResponseHandler;
import main.connection.ChameProtocol;
import main.connection.Connection;
import main.connection.skeletons.ChameMessage;
import main.mainview.Contact.skeletons.ContactSkeleton;
import main.mainview.Chat.skeletons.RoomSkeleton;
import main.connection.skeletons.UserSkeleton;
import main.mainview.Chat.ChatTabController;
import main.mainview.Game.skeletons.GameRoomSkeleton;
import main.mainview.MainViewController;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

//Controller for contacts tab
public class ContactsTabController implements Initializable, ChameController, ServerResponseHandler {

    @FXML
    private Button joinGroup_button;
    @FXML
    private Button stats_button;
    @FXML
    private Button createGroup_button;
    @FXML
    private TextField search_textField;
    @FXML
    private Button addContact_button;
    @FXML
    private ListView<ContactSkeleton> contacts_listView;
    @FXML
    private Label profileUsername_label;
    @FXML
    private ImageView contactPreviewAvatar_imageView;
    @FXML
    private Label contactPreviewUsername_label;
    @FXML
    private Circle contactPreviewOnline_circle;
    @FXML
    private Label contactPreviewStatus_label;
    @FXML
    private Button contactPreviewChat_button;
    @FXML
    private Button contactPreviewAccept_button;
    @FXML
    private Button contactPreviewDeny_button;


    private ObservableList<ContactSkeleton> contactObservableList = FXCollections.observableArrayList();

    private Stage stage;
    private Scene scene;
    private Parent root;

    private Gson gson = new Gson();


    //initializes the controller
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ControllerManager.putController(ContactsTabController.class, this);

        contacts_listView.setItems(contactObservableList);
        contacts_listView.setCellFactory(contactObservableList -> new ContactListViewCell());
        contacts_listView.getSelectionModel().selectedItemProperty().addListener(contactListItemSelectedChangeListener);
        sendContactsListRequest();

        profileUsername_label.setText(MainViewController.clientUsername);

    }

    //join a group given the ID
    @FXML
    public void joinGroupButtonOnAction(ActionEvent actionEvent) {
        final Stage joinGroup = new Stage();

        VBox joinGroupRoot = new VBox();


        Label howToLabel = new Label("Enter the id of your group: ");
        TextField groupID = new TextField();
        groupID.setPromptText("Group ID");
        Button joinGroupButton = new Button("Join Group");

        joinGroupButton.setOnAction((event) ->{

//            if(!groupID.getText().matches("^\\d{0,29}$"))
//                return;

            ChameMessage chameMessage = new ChameMessage(
                    ChameProtocol.JOIN_ROOM,
                    groupID.getText()
            );

            Connection.queueMessage(chameMessage);
            joinGroup.close();

        });
        Insets t = new Insets(10);
        VBox.setMargin(howToLabel, t);
        VBox.setMargin(groupID, t);
        VBox.setMargin(joinGroupButton, t);

        joinGroupRoot.getChildren().addAll(howToLabel, groupID, joinGroupButton);
        joinGroupRoot.getStyleClass().addAll("inner_box");
        joinGroup.setTitle("Join Room: ");
        joinGroup.initModality(Modality.WINDOW_MODAL);
        joinGroup.initOwner(stage);

        VBox chooserParent = new VBox();
        chooserParent.getStyleClass().addAll("pane");
        chooserParent.setId("game-pane");
        chooserParent.getChildren().addAll(joinGroupRoot);
        VBox.setMargin(joinGroupRoot, new Insets(20));
        Scene memberChooserDialogScene = new Scene(chooserParent, 400, 400);
        joinGroup.setScene(memberChooserDialogScene);
        joinGroup.showAndWait();
    }

    @FXML
    public void statsButtonOnAction(ActionEvent actionEvent) {

    }


    //accept the pending contact
    @FXML
    void contactPreviewAcceptButtonOnAction(ActionEvent event) {

        boolean isAcceptable = true;

        if(!contactPreviewUsername_label.getText().matches("^[aA-zZ]\\w{5,29}$"))
            isAcceptable=false;

        if(!isAcceptable){
            Main.popupError("Please enter valid input.\nAll entries should also be more than 6 characters");
            return;
        }

        ChameMessage chameMessage = new ChameMessage(
                ChameProtocol.FRIEND_REQUEST,
                contactPreviewUsername_label.getText()
        );

        Connection.queueMessage(chameMessage);
    }

    //remove the contact
    @FXML
    void contactPreviewDenyButtonOnAction(ActionEvent event) {
        boolean isAcceptable = true;

        if(!contactPreviewUsername_label.getText().matches("^[aA-zZ]\\w{5,29}$"))
            isAcceptable=false;

        if(!isAcceptable){
            Main.popupError("Please enter valid input.\nAll entries should also be more than 6 characters");
            return;
        }
        ChameMessage chameMessage = new ChameMessage(
                ChameProtocol.FRIEND_REQUEST_DENY,
                contactPreviewUsername_label.getText()
        );

        Connection.queueMessage(chameMessage);

        contactPreviewUsername_label.setText("Username");
        contactPreviewAvatar_imageView.setImage(null);
        contactPreviewOnline_circle.setVisible(false);
        contactPreviewStatus_label.setText("Status");
    }

    //create a new group
    @FXML
    void createGroupButtonOnAction(ActionEvent actionEvent) {
        final Stage memberChooser = new Stage();

        VBox chooseContactRoot = new VBox();

        ListView<ContactSkeleton> chooseGroupContactListView = new ListView<>();
        chooseGroupContactListView.getStylesheets().addAll(Main.class.getResource("resources/styles/fancylistview.css").toExternalForm());
        chooseGroupContactListView.getItems().addAll(contactObservableList);
        chooseGroupContactListView.setCellFactory(k -> new ContactListViewCell());

        chooseGroupContactListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        Label howToLabel = new Label("Hold down Ctrl to select members: ");
        TextField groupName = new TextField();
        groupName.setPromptText("Group Name");
        Button createGroupButton = new Button("Create Group");

        createGroupButton.setOnAction((event) ->{

            boolean isAcceptable = true;

            if(!groupName.getText().matches("^[aA-zZ]\\w{5,29}$"))
                isAcceptable=false;

            if(!isAcceptable){
                Main.popupError("Please enter valid input.\nAll entries should also be more than 6 characters");
                return;
            }


            List<ContactSkeleton> csList = chooseGroupContactListView.getSelectionModel().getSelectedItems();
            if(csList.size() <= 1){
                //error
                System.out.println("too little people");
                return;
            }

            List<UserSkeleton> usrList = new ArrayList<>();
            for(ContactSkeleton cs : csList){
                usrList.add(new UserSkeleton(cs.getUsername()));
            }
            usrList.add(new UserSkeleton(MainViewController.clientUsername));
            RoomSkeleton room = new RoomSkeleton();
            room.setMemberList(usrList);
            room.setPrivate(false);
            room.setName(groupName.getText());
            ControllerManager.getController(ChatTabController.class).sendRoomCreationRequest(room);
            memberChooser.close();
        });
        Insets t = new Insets(10);
        VBox.setMargin(howToLabel, t);
        VBox.setMargin(groupName, t);
        VBox.setMargin(createGroupButton, t);
        VBox.setMargin(chooseGroupContactListView, t);

        chooseContactRoot.getChildren().addAll(howToLabel, groupName, createGroupButton, chooseGroupContactListView);
        chooseContactRoot.getStyleClass().addAll("inner_box");
        memberChooser.setTitle("Choose Members: ");
        memberChooser.initModality(Modality.WINDOW_MODAL);
        memberChooser.initOwner(stage);

        VBox chooserParent = new VBox();
        chooserParent.getStyleClass().addAll("pane");
        chooserParent.setId("game-pane");
        chooserParent.getChildren().addAll(chooseContactRoot);
        VBox.setMargin(chooseContactRoot, new Insets(20));
        Scene memberChooserDialogScene = new Scene(chooserParent, 400, 700);
        memberChooser.setScene(memberChooserDialogScene);
        memberChooser.showAndWait();
    }

    //start a new chat room with the selected contact
    @FXML
    void contactPreviewChatButtonOnAction(ActionEvent event) {

        ContactSkeleton selectedContact = contacts_listView.getSelectionModel().getSelectedItem();

        if(roomAlreadyExistsInRoomList(selectedContact.getUsername())){

        }else{

            RoomSkeleton room = new RoomSkeleton();
            room.setMemberList(Arrays.asList(new UserSkeleton(MainViewController.clientUsername),
                    new UserSkeleton(selectedContact.getUsername())));
            room.setPrivate(true);
            room.setName(selectedContact.getUsername());
            ControllerManager.getController(ChatTabController.class).sendRoomCreationRequest(room);
        }
    }

    private boolean roomAlreadyExistsInRoomList(String username) {
        return false;
    }


    @FXML
    void addContactButtonOnAction(ActionEvent actionEvent) {

        boolean isAcceptable = true;

        if(!search_textField.getText().matches("^[aA-zZ]\\w{5,29}$"))
            isAcceptable=false;

        if(!isAcceptable){
            Main.popupError("Please enter valid input.\nAll entries should also be more than 6 characters");
            return;
        }

        ChameMessage chameMessage = new ChameMessage(
                ChameProtocol.FRIEND_REQUEST,
                search_textField.getText()
        );

        Connection.queueMessage(chameMessage);

        search_textField.setText("");

    }

    //handler server response
    public synchronized void handleServerResponse(ChameMessage chameMessage){

        switch (chameMessage.getHeader()){
            case ChameProtocol.CONTACTS_LIST:{
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        populateListView(chameMessage);
                    }
                });
            }
            break;
        }

    }

    //change the contact preview windows according to the selected contact
    private final ChangeListener<ContactSkeleton> contactListItemSelectedChangeListener =
            (ObservableValue<? extends ContactSkeleton> ov,
             ContactSkeleton old_val, ContactSkeleton new_val) -> {
        if(new_val == null)
            return;

        contactPreviewUsername_label.setText(new_val.getUsername());
        contactPreviewOnline_circle.setVisible(new_val.isOnline());
        contactPreviewStatus_label.setText(new_val.getRelationType());

        if(!new_val.getRelationType().toLowerCase().equals("pending")){
            contactPreviewAccept_button.setVisible(false);
        }else{
            contactPreviewAccept_button.setVisible(true);
        }

        if(new_val.getRelationType().toLowerCase().equals("friend"))
            contactPreviewAvatar_imageView.setImage(ContactListViewCell.friendImage);
        else
            contactPreviewAvatar_imageView.setImage(ContactListViewCell.pendingImage);


    };



    //send a request to retrieve the contact list
    public void sendContactsListRequest() {
        ChameMessage chameMessage = new ChameMessage(
                ChameProtocol.GET_CONTACTS,
                ""
        );

        Connection.queueMessage(chameMessage);
    }

    //populate the list view of contacts
    private void populateListView(ChameMessage chameMessage) {
        Type collectionType = new TypeToken<ArrayList<ContactSkeleton>>() {}.getType();
        //parse the json
        ArrayList<ContactSkeleton> parsedContactList = gson.fromJson(chameMessage.getBody(), collectionType);
        contactObservableList.clear();
        contactObservableList.addAll(parsedContactList);
    }


    //initialize controller
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



    private static class ContactListViewCell extends ListCell<ContactSkeleton> {

        public static Image friendImage;
        public static Image pendingImage;
        static {
            try {
                friendImage = new Image(new FileInputStream("src/main/resources/icons/friend.png"));
                pendingImage = new Image(new FileInputStream("src/main/resources/icons/request_pending.png"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        @Override
        protected void updateItem(ContactSkeleton contactSkeleton, boolean empty) {
            super.updateItem(contactSkeleton, empty);

            if(empty || contactSkeleton == null) {

                setText(null);
                setGraphic(null);

            } else {

                HBox root = new HBox();

                ImageView im = new ImageView();
                root.getChildren().add(im);
                im.setFitHeight(50);
                im.setFitWidth(50);
                im.setPreserveRatio(true);

                if(contactSkeleton.getRelationType().toLowerCase().equals("friend"))
                    im.setImage(friendImage);
                else
                    im.setImage(pendingImage);

                root.setMinSize(Double.MIN_VALUE, 70);
                Label lb = new Label(contactSkeleton.getUsername());
                HBox.setMargin(lb, new Insets(0, 20, 0, 10));

                Circle onlineCircle = new Circle(8);
                onlineCircle.setStrokeWidth(0);
                onlineCircle.setFill(Color.rgb(11, 196, 2));
                if(!contactSkeleton.isOnline()){
                    onlineCircle.setVisible(false);
                }
                root.setAlignment(Pos.CENTER_LEFT);
                root.getChildren().add(lb);
                setText(null);
                setGraphic(root);
            }
        }
    }
}

