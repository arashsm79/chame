package main.connection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;


//a session associated with the server channel
public class ClientSession {
    private SocketChannel clientSocket;
    private Selector mainSelector;
    private ChameMessageWriter chameMessageWriter;
    private ChameMessageReader chameMessageReader;


    public ClientSession(SocketChannel clientSocket, Selector mainSelector) {
        this.clientSocket = clientSocket;
        this.mainSelector = mainSelector;
        this.chameMessageWriter = new ChameMessageWriter(this);
        this.chameMessageReader = new ChameMessageReader(this);

    }

    //writes a message from the queue
    public void write(SelectionKey key) throws IOException {
        chameMessageWriter.dequeueAndWrite(key);
    }

    //writes as much as it can and returns the total bytes written
    public int writeToSocket(ByteBuffer byteBuffer) throws IOException {
        int bytesWritten      = this.clientSocket.write(byteBuffer);
        int totalBytesWritten = bytesWritten;

        while(bytesWritten > 0 && byteBuffer.hasRemaining()){
            bytesWritten = this.clientSocket.write(byteBuffer);
            totalBytesWritten += bytesWritten;
        }

        return totalBytesWritten;
    }


    //reads from the channel
    public void read(SelectionKey key) throws IOException {

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        int bytesRead = this.clientSocket.read(byteBuffer);
        int totalBytesRead = bytesRead;

        while(bytesRead > 0){

            if(!byteBuffer.hasRemaining()){
                //if the buffer is filled up, enqueue it and create a new one
                chameMessageReader.enqueueByteBuffer(byteBuffer);
                byteBuffer = ByteBuffer.allocate(1024);
            }
            bytesRead = this.clientSocket.read(byteBuffer);
            totalBytesRead += bytesRead;
        }

        //if it is not an empty buffer
        if(byteBuffer.position() != 0){
            chameMessageReader.enqueueByteBuffer(byteBuffer);
        }

        if(bytesRead == -1){
            //end of stream reached
            Connection.getInstance().closeConnection();
        }

        //process the buffer queue and construct the messages
        chameMessageReader.processBufferQueue();

    }

    //register interest in writing and add the message to the queue
    public void sendMessage(String msg){

        //indicates that we want to write to this channel

        clientSocket.keyFor(this.mainSelector).interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        //System.out.println("Queued message: " + msg);

        //enqueue the message
        this.chameMessageWriter.enqueueMessage(msg);
    }


    //getters and setters
    public ChameMessageReader getChameMessageReader() {
        return chameMessageReader;
    }
    public SocketChannel getClientSocket() {
        return clientSocket;
    }

    public Selector getMainSelector() {
        return mainSelector;
    }

    public void setClientSocket(SocketChannel clientSocket) {
        this.clientSocket = clientSocket;
    }


}
