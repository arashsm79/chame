package main.mainview;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import main.ChameController;
import main.ControllerManager;
import main.mainview.Chat.ChatTabController;
import main.mainview.Contact.ContactsTabController;
import main.mainview.Game.GameTabController;

import java.net.URL;
import java.util.ResourceBundle;

public class MainViewController implements Initializable, ChameController {

    @FXML
    private Tab gameTab;
    @FXML
    private Tab chatTab;
    @FXML
    private Tab contactsTab;
    @FXML
    private TabPane main_tabPane;
    @FXML
    private GameTabController gameTabController;
    @FXML
    private ChatTabController chatTabController;
    @FXML
    private ContactsTabController contactsTabController;

    private Stage stage;
    private Scene scene;
    private Parent root;

    public static String clientUsername = "";

    //initialize the controller
    @Override
    public void initController(Stage stage, Scene scene, Parent root)
    {
        this.scene = scene;
        this.stage = stage;
        this.root = root;

        ControllerManager.getController(GameTabController.class).initController(stage, scene, root);
        ControllerManager.getController(ChatTabController.class).initController(stage, scene, root);
        ControllerManager.getController(ContactsTabController.class).initController(stage, scene, root);
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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @FXML
    public void refreshContact(ActionEvent actionEvent) {
        ControllerManager.getController(ContactsTabController.class).sendContactsListRequest();

    }

    @FXML
    public void refreshGroup(ActionEvent actionEvent) {
        ControllerManager.getController(ChatTabController.class).sendRoomsListRequest();


    }
}
