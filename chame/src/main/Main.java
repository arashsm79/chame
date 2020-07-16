package main;

import com.sun.javafx.css.StyleManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.auth.LogInController;
import main.connection.Connection;


public class Main extends Application {


    public static final int DEFAULT_PORT = 43121;
    public static Stage primaryStage;

    //get an instance of the singleton connection
    private Connection connection = Connection.createConnection("localhost", DEFAULT_PORT);

    @Override
    public void start(Stage stage) throws Exception{


        FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/fxml/LogIn.fxml"));
        Parent logInRoot = loader.load();
        LogInController logInController = loader.<LogInController>getController();
        Scene logInScene = new Scene(logInRoot);

        connection.start();
        logInController.initController(stage, logInScene, logInRoot);

        ControllerManager.putController(LogInController.class, logInController);

        Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);
        StyleManager.getInstance().addUserAgentStylesheet(getClass().getResource("resources/styles/main.css").toString());

        stage.setTitle("Chame - LogIn");
        stage.setScene(logInScene);
        primaryStage = stage;
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        Connection.getInstance().closeConnection();
        super.stop();
    }

    public static void popupError(String errorMessage) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error!");
                alert.setHeaderText(null);
                alert.setContentText(errorMessage);
                alert.initOwner(Main.primaryStage);
                alert.initModality(Modality.WINDOW_MODAL);
                alert.showAndWait();
            }
        });
    }
    public static void popupInfo(String infoMessage) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Info!");
                alert.setHeaderText(null);
                alert.setContentText(infoMessage);
                alert.initOwner(Main.primaryStage);
                alert.initModality(Modality.WINDOW_MODAL);
                alert.showAndWait();
            }
        });
    }
    public static void main(String[] args) {
        launch(args);
    }
}
