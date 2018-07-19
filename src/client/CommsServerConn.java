package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Class that handles the client's connection to the server
 * Made by Alex Lockwood
 */
public class CommsServerConn {
    private Socket sClientSocket;
    private LinkedBlockingQueue<Object> qMessages;
    private ObjectInputStream oisInput;
    private ObjectOutputStream oosOutput;

    //constructor
    public CommsServerConn(Socket sClientSocket, LinkedBlockingQueue<Object> qMessages) throws IOException {
        this.sClientSocket = sClientSocket;
        this.qMessages = qMessages;
        //create the object streams
        oisInput = new ObjectInputStream(sClientSocket.getInputStream());
        oosOutput = new ObjectOutputStream(sClientSocket.getOutputStream());

        //create a new thread that listens for new objects
        Runnable rReadMessage = (() -> {
            while(true) {
                synchronized (CommsServerConn.this) {
                    try {
                        Object oMessage = oisInput.readObject();
                        qMessages.put(oMessage);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        Thread tReadMessage = new Thread(rReadMessage);
        tReadMessage.setDaemon(true);
        tReadMessage.start();

        //when the connection closes, close the streams
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                oisInput.close();
                oosOutput.close();
            }
            catch (IOException io) {
                io.printStackTrace();
            }
        }));
    }

    //write an object to the output stream
    public void writeMessage(Object[] oMessage) {
        try {
            oosOutput.writeObject(oMessage);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }
}