package com.server.core;


import com.ong.core.Log;
import com.ong.core.Message;

import java.io.*;

public class ServerListener extends Thread {
    private final ServerDispatcher serverDispatcher;
    private final ServerClient serverClient;
    private final ObjectInputStream inputStream;

    public ServerListener(ServerClient serverClient, ServerDispatcher serverDispatcher, ObjectInputStream objectInputStream) {
        this.serverClient = serverClient;
        this.serverDispatcher = serverDispatcher;
        inputStream = objectInputStream;
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                Message message = (Message) inputStream.readObject();
                if (message == null) {
                    break;
                }
                serverDispatcher.dispatchMessage(message);
            }
        }
        catch (Exception e) {
            Log.warning(serverClient.user.getUsername() + " Disconnected");
        }

        serverClient.serverSender.interrupt();
        serverDispatcher.deleteClient(serverClient);
    }
}
