package main;

import com.google.gson.Gson;
import main.games.ChameGame;
import main.games.ChameGameAction;
import main.games.ChamePlayer;
import main.games.tictactoe.TicProtocols;
import main.skeletons.ChameMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;


//a client session associated with each channel
public class ClientSession {

    private SocketChannel clientSocket;
    private Selector mainSelector;
    private SelectionKey key;
    private ChameMessageWriter chameMessageWriter;
    private ChameMessageReader chameMessageReader;

    //holds the references to the current game that the client is playing
    private ChameGame currentGame = null;
    private ChamePlayer currentGamePlayer = null;

    //a set of all the "room client sets" that this client is part of
    private Set<Integer> associatedRoomIdsSet = Collections.synchronizedSet(new HashSet<Integer>());

    private int id;
    private String username;
    private boolean online;


    public ClientSession(SocketChannel clientSocket, Selector mainSelector, SelectionKey key) {
        this.clientSocket = clientSocket;
        this.mainSelector = mainSelector;
        this.key = key;
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
            disconnectClient();
            //end of stream reached
        }

        //process the buffer queue and construct the messages
        chameMessageReader.processBufferQueue();

    }

    //register interest in writing and add the message to the queue
    public void sendMessage(String msg){

        //indicates that we want to write to this channel

        clientSocket.keyFor(this.mainSelector).interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);

        //enqueue the message
        this.chameMessageWriter.enqueueMessage(msg);

    }

    //removes all the resources associated with this client and make it offline
    public void disconnectClient() throws IOException {
        //disconnect the socket
        clientSocket.close();
        ChameServer.workerPool.execute(()->{
            removeAssociatedRoomIds();
            setStatusAsOffline();
            removeFromGames();
        });
    }

    private void removeFromGames() {
        if(currentGame != null){

            currentGame.abruptLeave(this);
        }
    }

    private void setStatusAsOffline() {

        String sqlQuery = "UPDATE users SET is_online=0 WHERE id=?";
        try (Connection cn = DatabaseConnectionPool.getConnection();
             PreparedStatement pst = cn.prepareStatement(sqlQuery)) {
            pst.setInt(1, id);
            pst.execute();
        }catch (SQLException e){
            e.printStackTrace();
        }

    }

    //add associated room to the set
    public void addRoomId(int id){
        associatedRoomIdsSet.add(id);
    }

    //removes all the empty rooms associated with this client from the online room manager
    public void removeAssociatedRoomIds(){

        synchronized (associatedRoomIdsSet){

            Iterator<Integer> it = associatedRoomIdsSet.iterator();
            while(it.hasNext()){
                int roomID = it.next();
                Set<ClientSession> scs = OnlineRoomsManager.getInstance().roomsList.get(roomID);
                if(scs != null){
                    synchronized (scs){
                        scs.remove(this);
                        if(scs.isEmpty()){
                            OnlineRoomsManager.getInstance().roomsList.remove(scs);
                        }
                    }
                }
            }
        }
    }

    //getters and setters

    public Selector getMainSelector() {
        return mainSelector;
    }

    public ChamePlayer getCurrentGamePlayer() {
        return currentGamePlayer;
    }

    public void setCurrentGamePlayer(ChamePlayer currentGamePlayer) {
        this.currentGamePlayer = currentGamePlayer;
    }

    public ChameMessageReader getChameMessageReader() {
        return chameMessageReader;
    }

    public SocketChannel getClientSocket() {
        return clientSocket;
    }

    public void setClientSocket(SocketChannel clientSocket) {
        this.clientSocket = clientSocket;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public SelectionKey getKey() {
        return key;
    }

    public ChameGame getCurrentGame() {
        return currentGame;
    }

    public void setCurrentGame(ChameGame currentGame) {
        this.currentGame = currentGame;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
