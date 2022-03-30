package com.ong.client;

import com.ong.core.Log;
import com.ong.core.Message;

import java.io.ObjectOutputStream;
import java.util.Vector;

/**
 * Class responsible for sending messages to the Server.
 */
public class ClientSender extends Thread {
    private final Vector queue;
    private final ObjectOutputStream objectOutputStream;

    public ClientSender(ObjectOutputStream objectOutputStream) {
        this.objectOutputStream = objectOutputStream;
        this.queue = new Vector();
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) { // check if the thread is interrupted
                if (!queue.isEmpty()) {
                    Message message = (Message) queue.get(0);
                    objectOutputStream.writeObject(message);
                    queue.remove(0);
                }
            }
        } catch (Exception e) {
            Log.error("Lost connection to server.", e);
            System.exit(-1);
        }
    }

    public void addItem(Message item) {
        queue.add(item);
    }
}