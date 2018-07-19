package client;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Comms class for the clients
 * Only connects to one server at a time, but can handle multiple messages at once
 * Made by Alex Lockwood
 */
public class CommsClient {
    private CommsServerConn cccServerConn;
    private LinkedBlockingQueue<Object> qMessages;
    private Socket sClientSocket;

    //constructor
    public CommsClient() throws IOException {
        this.qMessages = new LinkedBlockingQueue<>();
        this.sClientSocket = new Socket("localhost", 60010);
        this.cccServerConn = new CommsServerConn(this.sClientSocket, qMessages);

        //when the connection ends, close the socket
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                sClientSocket.close();
            }
            catch (IOException io) {
                io.printStackTrace();
            }
        }));
    }

    //sending a message to the server
    public void sendMessage(Object[] oMessage) {
        cccServerConn.writeMessage(oMessage);
    }

    //get the most recent message from the queue
    public Object getMessage() {
        try {
            return this.qMessages.take();
        }
        catch(InterruptedException ie) {
            ie.printStackTrace();
        }
        return null;
    }
}
