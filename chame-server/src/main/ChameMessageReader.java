package main;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

//a NIO message reader handler
public class ChameMessageReader {

    private ClientSession clientSession;
    //a queue for partial buffers
    private LinkedList<ByteBuffer> partialBufferQueue = new LinkedList<>();

    //when a full message is received it is added to this queue
    private ConcurrentLinkedQueue<String> fullMessages = new ConcurrentLinkedQueue<>();
    private StringBuilder partialMessageString = null;

    public ChameMessageReader(ClientSession clientSession) {
        this.clientSession = clientSession;
    }


    //adds the buffer to the queue
    public void enqueueByteBuffer(ByteBuffer byteBuffer) {

        partialBufferQueue.add(byteBuffer);

    }

    //processes the buffer queue
    public void processBufferQueue(){

        Iterator<ByteBuffer> it = partialBufferQueue.iterator();

        while(it.hasNext()){

            ByteBuffer byteBuffer = it.next();
            StringBuilder stringBuilder;

            //if there is already a partial message, append to that
            if(partialMessageString == null)
                stringBuilder = new StringBuilder();
            else
                stringBuilder = partialMessageString;

            //the end of message is reached when we encounter a null  character
            char byteRead = 0x00;
            byteBuffer.flip();
            while (byteBuffer.hasRemaining()) {
                byteRead = (char) byteBuffer.get();
                if (byteRead == 0x00) {
                    //a full message has been read
                    partialMessageString = null;
                    fullMessages.add(stringBuilder.toString());
                    ChameServer.handleMessage(this.clientSession);
                    //System.out.println("received: " + stringBuilder.toString());
                    stringBuilder = new StringBuilder();
                }else {
                    stringBuilder.append(byteRead);
                }
            }

            it.remove();
            //its a partial message
            if(stringBuilder.length() != 0){
                partialMessageString = stringBuilder;
            }
        }
    }

    //gets the full message
    public String getFullMessage(){
        return fullMessages.poll();
    }

}
