package main;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChameServer {

    //set the default port
    public static final int DEFAULT_PORT = 43121;

    //create a pool for workers threads (these get dispatched  when a full message is received)
    public static final ExecutorService workerPool = Executors.newCachedThreadPool();

    //starts the main selector loop
    public static void main(String[] args) {

        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (RuntimeException ex) {
            port = DEFAULT_PORT;
        }
        System.out.println("Listening for connections on port " + port);

        ServerSocketChannel serverChannel;
        Selector selector;
        try {
            serverChannel = ServerSocketChannel.open();
            InetSocketAddress address = new InetSocketAddress(port);
            serverChannel.bind(address);
            serverChannel.configureBlocking(false);
            selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        //start the main selector loop
        new Thread(new SelectorLoop(selector)).start();
    }


    //dispatches a new thread for handling the response from clients
    public static void handleMessage(ClientSession clientSession){
        workerPool.execute(new ResponseHandler(clientSession));
    }
}
