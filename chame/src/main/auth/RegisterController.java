package main.auth;

import com.google.gson.Gson;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import main.ChameController;
import main.ControllerManager;
import main.Main;
import main.connection.ChameProtocol;
import main.connection.skeletons.ChameMessage;
import main.connection.Connection;
import main.auth.skeletons.RegisterSkeleton;

import java.util.Arrays;
import java.util.List;

//re gister controller for sending register requests
public class RegisterController implements ChameController {

    @FXML
    private Button back_button;

    @FXML
    private TextField username_textField;

    @FXML
    private PasswordField password_textField;

    @FXML
    private TextField email_textField;

    @FXML
    private ComboBox<String> recoveryQuestion_comboBox;

    @FXML
    private TextField answer_TextField;

    @FXML
    private Button signUp_button;

    private Gson gson = new Gson();
    private Stage stage;
    private Scene scene;
    private Parent root;

    //go back to login controller
    @FXML
    void OnBack_button_action(ActionEvent event) {

        scene.setRoot(ControllerManager.getController(LogInController.class).getRoot());
    }


    //sign up
    @FXML
    void OnSignUp_button_action(ActionEvent event) {
        if(!enteredValidEntries())
            return;
        RegisterSkeleton rs = new RegisterSkeleton(
                username_textField.getText(),
                password_textField.getText(),
                recoveryQuestion_comboBox.getValue(),
                answer_TextField.getText(),
                email_textField.getText());
        ChameMessage chMsg = new ChameMessage(
                ChameProtocol.REGISTER,
                gson.toJson(rs)
        );
        Connection.queueMessage(chMsg);
        scene.setRoot(ControllerManager.getController(LogInController.class).getRoot());

    }

    //make sure entries are valid
    private boolean enteredValidEntries() {
        boolean isAcceptable = true;
        if(!username_textField.getText().matches("^[aA-zZ]\\w{5,29}$"))
            isAcceptable=false;
        if(!password_textField.getText().matches("^\\p{ASCII}{6,20}$"))
            isAcceptable=false;
        if(!email_textField.getText().matches("^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$"))
            isAcceptable=false;
        if(recoveryQuestion_comboBox.getValue() == null)
            isAcceptable=false;
        if(!answer_TextField.getText().matches("^\\p{ASCII}{6,20}$"))
            isAcceptable=false;

        if(!isAcceptable)
            Main.popupError("Please enter valid input.\nAll entries should also be more than 6 characters");

        return isAcceptable;
    }



    //initialize the controller
    @Override
    public void initController(Stage stage, Scene scene, Parent root)
    {
        this.scene = scene;
        this.stage = stage;
        this.root = root;
        startUp();
    }


    private void startUp()
    {
        addRecoveryQuestions();
    }

    //adds recovery questions to the combo box
    private void addRecoveryQuestions() {
        List<String> questions = Arrays.asList(
                "What was the name of your first pet?",
                "What is the name of the city your mom was born in?",
                "What is your favorite sports club?",
                "What was the name of your elementary school?",
                "What is the name of your favorite teacher?");
        recoveryQuestion_comboBox.getItems().addAll(questions);
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


}
