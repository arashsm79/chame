package main.connection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.concurrent.ConcurrentLinkedQueue;

//a NIO handler for writing messages to socketchannel
public class ChameMessageWriter {

    private ClientSession clientSession;
    private ConcurrentLinkedQueue<ByteBuffer> queue = new ConcurrentLinkedQueue<>();

    public ChameMessageWriter(ClientSession clientSession) {
        this.clientSession = clientSession;
    }

    //writes to the socket if there's no more message then deregisters the OP_WRITE
    public void dequeueAndWrite(SelectionKey key) throws IOException {

        ByteBuffer byteBuffer = queue.peek();

        //if the queue is empty then we no longer need to write to it
        if(byteBuffer == null){
            key.interestOps(SelectionKey.OP_READ);
            return;
        }

        //write as much as it can to the socketChannel
        clientSession.writeToSocket(byteBuffer);
        //if all of the message has been sent remove it from the queue
        if(!byteBuffer.hasRemaining()){
            queue.poll();
        }else {
            byteBuffer.compact();
        }

    }

    //enqueues a message to be written when ever possible
    public void enqueueMessage(String msg){
        ByteBuffer buffer = ByteBuffer.allocate(msg.length() + 1);
        buffer.put(msg.getBytes());
        buffer.put((byte) 0x00);
        buffer.flip();

        queue.add(buffer);
        clientSession.getMainSelector().wakeup();
    }

}
