package com.ong.client;

import com.ong.core.Log;
import com.ong.core.Message;
import com.ong.core.MessageHandler;
import javafx.application.Platform;

import java.io.ObjectInputStream;

/**
 * Class responsible for receiving messages from the Server.
 */
public class ClientReceiver extends Thread {

    private final ObjectInputStream objectInputStream;

    public ClientReceiver(ObjectInputStream objectInputStream) {
        this.objectInputStream = objectInputStream;
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) { // check if the thread is interrupted
                Message message = (Message) objectInputStream.readObject();
                if (message != null) {
                    // runLater is used, because adding elements can be done only on the JavaFX Thread.
                    Platform.runLater(() -> {
                        MessageHandler messageHandler = new MessageHandler(message);
                        messageHandler.handle();
                    });
                }
            }
        } catch (Exception e) {
            Log.error("Lost connection to server.", e);
            System.exit(-1);
        }
    }
}
