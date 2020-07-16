package main;

import javafx.application.Platform;
import main.connection.ChameProtocol;
import main.connection.skeletons.ChameMessage;

public interface ServerResponseHandler {
    
    void handleServerResponse(ChameMessage chameMessage);
}
