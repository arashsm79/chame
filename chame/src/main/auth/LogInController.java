package main.auth;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.ChameController;
import main.ControllerManager;
import main.Main;
import main.auth.skeletons.ForgotPasswordSkeleton;
import main.auth.skeletons.RecoveredPasswordSkeleton;
import main.connection.ChameProtocol;
import main.connection.Connection;
import main.connection.skeletons.ChameMessage;
import main.auth.skeletons.LoginSkeleton;
import main.mainview.MainViewController;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ForkJoinPool;

//Controller for logging in
public class LogInController implements Initializable, ChameController {

    @FXML
    private TextField username_textField;

    @FXML
    private PasswordField password_textField;

    @FXML
    private Button signIn_button;

    @FXML
    private Hyperlink forgotPassword_hyper;

    @FXML
    private Hyperlink register_hyper;

    private Gson gson = new Gson();
    private Stage stage;
    private Scene scene;
    private Parent root;


    //creates a dialog for retrieving the forgotten password
    @FXML
    void OnForgotPasswordHyper_action(ActionEvent event) {
        forgotPasswordDialog();
    }

    private void forgotPasswordDialog() {
        final Stage dialog = new Stage();
        dialog.setTitle("Forgot Password");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);

        VBox dialogRoot = new VBox();
        dialogRoot.getStyleClass().addAll("pane");
        dialogRoot.setId("forgotpassword_pane");
        dialogRoot.setPadding(new Insets(40));

        VBox dialogVbox = new VBox();
        dialogVbox.getStyleClass().addAll("inner_box");
        dialogVbox.setPadding(new Insets(12));
        dialogVbox.setAlignment(Pos.CENTER_LEFT);
        dialogVbox.getStylesheets()
                .addAll(Main.class.getResource("resources/styles/gamesGridPane.css").toExternalForm());

        ComboBox<String> recoveryQuestionComboBox = new ComboBox<>();
        recoveryQuestionComboBox.setPromptText("Recovery Question");
        List<String> questions = Arrays.asList(
                "What was the name of your first pet?",
                "What is the name of the city your mom was born in?",
                "What is your favorite sports club?",
                "What was the name of your elementary school?",
                "What is the name of your favorite teacher?");
        recoveryQuestionComboBox.getItems().addAll(questions);
        recoveryQuestionComboBox.setMinSize(100, 30);

        Label emailLabel = new Label("Email: ");

        Label answerLabel = new Label("Answer: ");

        TextField emailTextField = new TextField();
        emailTextField.setPromptText("Email: ");
        emailTextField.setPrefSize(100, 30);

        TextField answerTextField = new TextField();
        answerTextField.setPrefSize(100, 30);
        answerTextField.setPromptText("Answer: ");

        Button recoverButton = new Button("Recover");
        recoverButton.setPrefSize(100,30);
        recoverButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {

                boolean isAcceptable = true;

                if(!emailTextField.getText().matches("^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$"))
                    isAcceptable=false;
                if(recoveryQuestionComboBox.getValue() == null)
                    isAcceptable=false;
                if(!answerTextField.getText().matches("^\\p{ASCII}{6,20}$"))
                    isAcceptable=false;

                if(!isAcceptable){
                    Main.popupError("Please enter valid input.\nAll entries should also be more than 6 characters");
                    return;
                }

                ForgotPasswordSkeleton forgotPasswordSkeleton = new ForgotPasswordSkeleton(
                        emailTextField.getText(),
                        recoveryQuestionComboBox.getValue(),
                        answerTextField.getText()
                );

                ChameMessage chMsg = new ChameMessage(
                        ChameProtocol.FORGOT_PASSWORD,
                        gson.toJson(forgotPasswordSkeleton)
                );

                Connection.queueMessage(chMsg);
                dialog.close();
            }
        });

        VBox.setMargin(emailTextField, new Insets(5));
        VBox.setMargin(answerTextField, new Insets(5));
        VBox.setMargin(recoveryQuestionComboBox, new Insets(5));
        VBox.setMargin(recoverButton, new Insets(5));
        VBox.setMargin(answerLabel, new Insets(5));
        VBox.setMargin(emailLabel, new Insets(5));


        dialogVbox.getChildren().addAll(emailLabel, emailTextField, recoveryQuestionComboBox, answerLabel, answerTextField, recoverButton);
        dialogRoot.getChildren().addAll(dialogVbox);

        Scene dialogScene = new Scene(dialogRoot, 400, 300);

        dialogScene.getStylesheets().addAll(Main.class.getResource("resources/styles/authentication.css").toExternalForm());
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    //transition to the register controller
    @FXML
    void OnRegisterHyperAction(ActionEvent event) {
        if(!ControllerManager.containsController(RegisterController.class))
        {
            try {
                FXMLLoader loader = new FXMLLoader(Main.class.getResource("resources/fxml/Register.fxml"));
                Parent registerRoot = loader.load();
                RegisterController registerController = (RegisterController)loader.getController();
                registerController.initController(this.stage, this.scene, registerRoot);
                ControllerManager.putController(RegisterController.class, registerController);
                scene.setRoot(registerRoot);

            } catch (IOException e) {
                //e.printStackTrace();
            }
        }else
        {
            scene.setRoot(ControllerManager.getController(RegisterController.class).getRoot());
        }
    }

    //send sign in request
    @FXML
    void OnSignIn_button_action(ActionEvent event) {
        if(!enteredValidEntries())
            return;

        LoginSkeleton loginSkeleton = new LoginSkeleton(
                username_textField.getText(),
                password_textField.getText()
        );
        ChameMessage chMsg = new ChameMessage(
                ChameProtocol.LOGIN,
                gson.toJson(loginSkeleton)
        );
        Connection.queueMessage(chMsg);
    }

    //handles the server response
    public synchronized void handleServerResponse(ChameMessage chameMessage){

        switch (chameMessage.getHeader()){
            case ChameProtocol.LOGIN_SUCCESS:{
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        loginAndChangeRoot();
                    }
                });
                break;
            }
            case ChameProtocol.RECOVERED_PASSWORD:{
                RecoveredPasswordSkeleton rps = gson.fromJson(chameMessage.getBody(), RecoveredPasswordSkeleton.class);

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        foundPasswordDialog(rps);
                    }
                });
                break;
            }
        }

    }

    //if the password is found, display it to the user
    private void foundPasswordDialog(RecoveredPasswordSkeleton rps) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Recovered Password");
        alert.setHeaderText(null);
        alert.setContentText("The password for the user registered with\nemail: " + rps.getEmail() + "\nis: " + rps.getPassword());
        alert.initOwner(Main.primaryStage);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.showAndWait();
    }

    //on login success change to the main view
    private void loginAndChangeRoot(){
        if(!ControllerManager.containsController(MainViewController.class))
        {
            try {
                FXMLLoader loader = new FXMLLoader(Main.class.getResource("resources/fxml/MainView.fxml"));
                Parent mainViewRoot = loader.load();
                MainViewController mainViewController = (MainViewController)loader.getController();
                mainViewController.initController(this.stage, this.scene, mainViewRoot);
                ControllerManager.putController(MainViewController.class, mainViewController);
                MainViewController.clientUsername = username_textField.getText();
                scene.setRoot(mainViewRoot);
                stage.sizeToScene();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }else
        {
            scene.setRoot(ControllerManager.getController(MainViewController.class).getRoot());
        }
    }


    //initialize the controller
    @Override
    public void initController(Stage stage, Scene scene, Parent root)
    {
        this.scene = scene;
        this.stage = stage;
        this.root = root;
    }



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    //check for valid entries
    private boolean enteredValidEntries() {
        boolean isAcceptable = true;

        if(!username_textField.getText().matches("^[aA-zZ]\\w{5,29}$"))
            isAcceptable=false;
        if(!password_textField.getText().matches("^\\p{ASCII}{6,20}$"))
            isAcceptable=false;

        if(!isAcceptable)
            Main.popupError("Please enter valid input.\nAll entries should also be more than 6 characters");

        return isAcceptable;
    }

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
