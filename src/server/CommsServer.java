package server;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Comms client for the server
 * Handles all the client connections and can handle multiple messages at once
 * Made by Alex Lockwood
 */
public class CommsServer {

    private ArrayList<CommsClientConn> alClientList;
    private ServerSocket ssSocket;
    private LinkedBlockingQueue<Object[]> qMessages;

    /**
     * Class that handles the server's connection to each client
     */
    public class CommsClientConn {
        Socket sClientSocket;
        ObjectInputStream oisInput;
        ObjectOutputStream oosOutput;

        //constructor
        public CommsClientConn(Socket sClientSocket) throws IOException {
            this.sClientSocket = sClientSocket;
            oosOutput = new ObjectOutputStream(sClientSocket.getOutputStream());

            //new thread that listens for messages from clients
            Runnable rReadMessage = () -> {
                try {
                    oisInput = new ObjectInputStream(sClientSocket.getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                while(true) {
                    try {
                        Object[] oMessage = (Object[]) oisInput.readObject();
                        qMessages.put(oMessage);
                        synchronized (CommsServer.this) {
                            CommsServer.this.notify();
                        }
                    }
                    catch (EOFException eof) {
                        return;
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

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

        //write a message to a specific client
        public void writeMessage(Object oMessage) {
            try {
                oosOutput.writeObject(oMessage);
            }
            catch (IOException io) {
                io.printStackTrace();
            }
        }
    }

    //constructor for the comms server
    public CommsServer() throws IOException {
        this.alClientList = new ArrayList<>();
        this.ssSocket = new ServerSocket(60010);
        this.qMessages = new LinkedBlockingQueue<>();

        //new thread that listens for socket connections and sends their index to them
        Runnable rAcceptSocket = () -> {
            while(true) {
                try {
                    Socket sClientSocket = ssSocket.accept();
                    CommsClientConn ccc = new CommsClientConn(sClientSocket);
                    alClientList.add(ccc);
                    CommsServer.this.sendMessage(alClientList.indexOf(ccc), "CONNECTED:" + alClientList.indexOf(ccc));
                }
                catch (IOException io) {
                    io.printStackTrace();
                }
            }
        };

        Thread tAcceptSocket = new Thread(rAcceptSocket);
        tAcceptSocket.setDaemon(true);
        tAcceptSocket.start();
    }

    public LinkedBlockingQueue<Object[]> getBlockingQueue() {
        return this.qMessages;
    }

    //get a message from the queue
    public Object[] getMessage() {
        try {
            return this.getBlockingQueue().take();
        }
        catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }

        return null;
    }

    //send a message to a specific client, based upon their index in the list
    public void sendMessage(int iIndex, Object oMessage) {
        synchronized (this.alClientList.get(iIndex)) {
            try {
                this.alClientList.get(iIndex).writeMessage(oMessage);
                this.alClientList.get(iIndex).notifyAll();
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
    }

}
