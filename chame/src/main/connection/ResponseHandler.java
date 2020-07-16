package main.connection;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import main.ControllerManager;
import main.auth.LogInController;
import main.Main;
import main.connection.skeletons.ChameMessage;
import main.mainview.Chat.ChatTabController;
import main.mainview.Contact.ContactsTabController;
import main.mainview.Game.GameTabController;


public class ResponseHandler implements Runnable {
    private ClientSession clientSession;
    private String message;
    private Gson gson = new Gson();

    public ResponseHandler(ClientSession clientSession) {
        this.clientSession = clientSession;
    }

    @Override
    public void run() {
        message = dequeueClientSessionMessage();
        if(message == null)
            return;
        ChameMessage chameMessage = gson.fromJson(message, ChameMessage.class);

        switch (chameMessage.getHeader()){
            case ChameProtocol.ERROR:{
                popupError(chameMessage.getBody());
                break;
            }

            case ChameProtocol.LOGIN_SUCCESS:{
                ControllerManager.getController(LogInController.class).handleServerResponse(chameMessage);
                break;
            }
            case ChameProtocol.RECOVERED_PASSWORD:{
                ControllerManager.getController(LogInController.class).handleServerResponse(chameMessage);;
                break;
            }

            case ChameProtocol.CONTACTS_LIST:{
                ControllerManager.getController(ContactsTabController.class).handleServerResponse(chameMessage);
                break;
            }
            case ChameProtocol.ROOMS_LIST:{
                ControllerManager.getController(ChatTabController.class).handleServerResponse(chameMessage);
                break;
            }
            case ChameProtocol.ROOM_MESSAGES:{
                ControllerManager.getController(ChatTabController.class).handleServerResponse(chameMessage);;
                break;
            }
            case ChameProtocol.ROOM_GAME_LOBBIES:{
                ControllerManager.getController(ChatTabController.class).handleServerResponse(chameMessage);;;
                break;
            }
            case ChameProtocol.GAME_ACTION:{
                ControllerManager.getController(GameTabController.class).handleServerResponse(chameMessage);
                break;
            }

        }

        return;
    }

    public String dequeueClientSessionMessage(){
        return clientSession.getChameMessageReader().getFullMessage();
    }

    private void popupError(String errorMessage) {
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
}