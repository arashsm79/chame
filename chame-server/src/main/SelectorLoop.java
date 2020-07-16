package main;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

//the main selector thread
// all the IO operations are done in this thread
public class SelectorLoop implements Runnable {

    private final Selector mainSelector;
    public SelectorLoop(Selector selector)
    {
        this.mainSelector = selector;
    }
    @Override
    public void run() {
        while (mainSelector.isOpen()) {

            try {
                mainSelector.select();
            } catch (IOException ex) {
                ex.printStackTrace();
                break;
            }

            Set<SelectionKey> readyKeys = mainSelector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                if(!key.isValid())
                    continue;

                try {
                    if (key.isAcceptable()) {
                        handleAccept(key);
                    }

                    if(key.isValid() && key.isReadable()) {
                        handleRead(key);
                    }

                    if (key.isValid() && key.isWritable()) {
                        handleWrite(key);
                    }
                } catch (IOException e) {
                    key.cancel();
                    try {
                        key.channel().close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }

            }
        }
    }

    //handle accept of new connections
    private void handleAccept(SelectionKey key) throws IOException {

        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel client = server.accept();
        System.out.println("Accepted connection from " + client);
        client.configureBlocking(false);

        client.register(
                mainSelector,
                SelectionKey.OP_READ,
                new ClientSession(client, mainSelector, key)
        );

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


}
