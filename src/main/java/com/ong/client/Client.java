package com.ong.client;

import com.ong.core.Log;
import com.ong.core.User;
import com.ong.session.UserSession;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Entry Point to the Client side of the application.
 * Concurrently sending and receiving messages.
 */
public class Client {
    public static final String HOSTNAME = "localhost";
    public static final int PORT = 4999;
    public static ClientSender clientSender;
    public static ClientReceiver clientReceiver;

    public static void run() {
        try {
            User user = UserSession.getInstance().getUser();

            Socket socket = new Socket(HOSTNAME, PORT);

            OutputStream outputStream = socket.getOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            InputStream inputStream = socket.getInputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

            // send user information on connection
            objectOutputStream.writeObject(user);

            // thread for Sending messages
            clientSender = new ClientSender(objectOutputStream);
            clientSender.setDaemon(false);
            clientSender.start();

            // Thread for receiving messages
            clientReceiver = new ClientReceiver(objectInputStream);
            clientReceiver.setDaemon(false);
            clientReceiver.start();

            Log.info("Connected to server " + HOSTNAME + ":" + PORT);
        } catch (Exception e) {
            Log.error("Could not connect to " + HOSTNAME + ":" + PORT, e);
            System.exit(-1);
        }
    }
}