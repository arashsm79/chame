package main;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public interface ChameController {

    //returns the scne eof this controller
    Scene getScene();

    Parent getRoot();

    Stage getStage();

    void initController(Stage stage, Scene scene, Parent root);
    

}
