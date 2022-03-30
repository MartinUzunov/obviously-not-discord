package com.server.core;

import com.ong.core.Message;
import com.server.Server;

import java.io.ObjectOutputStream;
import java.util.Vector;

public class ServerSender extends Thread {
    private final Vector queue = new Vector();

    private final ServerDispatcher serverDispatcher;
    private final ServerClient serverClient;
    private final ObjectOutputStream outputStream;

    public ServerSender(ServerClient serverClient, ServerDispatcher serverDispatcher, ObjectOutputStream objectOutputStream) {
        this.serverClient = serverClient;
        this.serverDispatcher = serverDispatcher;
        this.outputStream = objectOutputStream;
    }

    public synchronized void sendMessage(Message message) {
        queue.add(message);
        notify();
    }

    private synchronized Message getNextMessage() throws Exception {
        while (queue.size() == 0) {
            wait();
        }
        Message message = (Message) queue.get(0);
        queue.removeElementAt(0);
        return message;
    }

    private void sendMessageToClient(Message message) throws Exception {
        outputStream.writeObject(message);
        outputStream.flush();
        outputStream.reset();
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                Message message = getNextMessage();
                sendMessageToClient(message);
            }
        } catch (Exception e) {
            Server.activeServerClients.remove(serverClient);
        }

        serverClient.serverListener.interrupt();
        serverDispatcher.deleteClient(serverClient);
    }
}
