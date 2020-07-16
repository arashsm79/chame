package main.connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import main.Main;
import main.auth.LogInController;
import main.auth.RegisterController;
import main.connection.skeletons.ChameMessage;

//The service that handles the requests and response communications to/from the server
//this is the main selector thread
public class Connection extends Service<HashMap<String, String>> {


    private String serverIP;
    private int serverPort;
    private SocketChannel socket;
    private Selector mainSelector;
    private SocketChannel client;
    private static ClientSession mainClientSession = null;
    private static Connection connection_instance = null;
    private static Gson gson = new Gson();
    public static final ExecutorService workerPool = Executors.newCachedThreadPool();

    //a reference to the controllers for future callbacks
    private LogInController logInController;
    private RegisterController registerController;


    public Connection(String ip, int port) {
        this.serverIP = ip;
        this.serverPort = port;
    }

    //pretty straight forward
    //listens for responses from the server and calls back to the controller

    @Override
    protected Task<HashMap<String, String>> createTask() {

        return new Task<HashMap<String, String>>() {

            @Override
            protected HashMap<String, String> call() throws Exception {

                HashMap<String, String> msg = new HashMap<>();

                mainSelector = Selector.open();
                client = SocketChannel.open();
                client.configureBlocking(false);

                mainClientSession = new ClientSession(client, mainSelector);

                if (client.connect(new InetSocketAddress(serverIP, serverPort))) {
                    //connected
                    client.register(mainSelector, SelectionKey.OP_READ, mainClientSession);

                } else {
                    //finish connecting later
                    client.register(mainSelector, SelectionKey.OP_CONNECT, mainClientSession);
                }

                while (true) {

                    try {

                        if (mainSelector.select() == 0)
                            continue;

                        Set<SelectionKey> readyKeys = mainSelector.selectedKeys();
                        Iterator<SelectionKey> iterator = readyKeys.iterator();

                        while (iterator.hasNext()) {
                            SelectionKey key = iterator.next();
                            iterator.remove();
                            if (!key.isValid())
                                continue;
                            if (key.isConnectable()) {
                                handleConnect(key);
                            }

                            if (key.isReadable()) {
                                handleRead(key);
                            }

                            if (key.isWritable()) {

                                handleWrite(key);
                            }
                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (isCancelled())
                        break;

                }
                return msg;
            }

        };

    }

    //handle writing to channels
    private void handleWrite(SelectionKey key) throws IOException {

        ClientSession clientSession = (ClientSession) key.attachment();
        clientSession.write(key);
    }

    //handle reading from the channels
    private void handleRead(SelectionKey key) throws IOException {

        ClientSession clientSession = (ClientSession) key.attachment();
        clientSession.read(key);
    }

    //handle connection to the server
    private void handleConnect(SelectionKey key) {

        SocketChannel socketChannel = (SocketChannel) key.channel();

        // Finish the connection.
        try {
            socketChannel.finishConnect();
        } catch (IOException e) {
            // Cancel the channel's registration with our selector
            System.out.println(e);
            key.cancel();
            return;
        }

        // Register an interest in reading on this channel
        key.interestOps(SelectionKey.OP_READ);
        System.out.println(key);

    }


    //create the singleton instance
    public static Connection createConnection(String ip, int port) {
        if (connection_instance == null) {
            connection_instance = new Connection(ip, port);
        }

        return connection_instance;
    }


    //queue a message to be written to the server
    public static void queueMessage(ChameMessage chMsg) {

        if (mainClientSession == null)
            return;

        mainClientSession.sendMessage(gson.toJson(chMsg));
    }

    //dispatches a new thread to handle the respond from server
    public static void handleMessage(ClientSession clientSession) {
        workerPool.execute(new ResponseHandler(clientSession));
    }

    //gets the singleton instance
    public static Connection getInstance(){
        if(connection_instance == null)
            return new Connection("localhost", Main.DEFAULT_PORT);
        else
            return connection_instance;
    }


    //closes the connection
    public void closeConnection() {

        if(client != null && client.isOpen()){
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}