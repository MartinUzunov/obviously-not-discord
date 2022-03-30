package com.server.core;

import com.ong.core.Log;
import com.ong.core.Message;
import com.ong.core.User;
import com.server.Server;
import com.server.util.ServerDatabaseHandler;

import java.util.ArrayList;
import java.util.Vector;

public class ServerDispatcher extends Thread {
    private final Vector queue = new Vector();
    private final Vector clients = new Vector();

    public synchronized void addClient(ServerClient serverClient) {
        clients.add(serverClient);
    }

    public synchronized void deleteClient(ServerClient serverClient) {
        try {
            serverClient.socket.close();
        } catch (Exception e) {
            Log.error(e);
        }
        int index = clients.indexOf(serverClient);
        if (index != -1) {
            clients.removeElementAt(index);
        }
    }

    public synchronized void dispatchMessage(Message message) {
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

    private void sendMessageToAll(Message message) {
        for (Object client : clients) {
            ServerClient serverClient = (ServerClient) client;
            Message currentMessage = new Message(message);

            if (currentMessage.getType() != null && currentMessage.getType().equals(Message.MessageType.REQUEST_FRIENDS_ONLINE) &&
                    currentMessage.getFromUsername().equals(serverClient.user.getUsername())) {

                currentMessage.setType(Message.MessageType.RESPONSE_FRIENDS_ONLINE);
                try {
                    currentMessage.setFreeObject(onlineFriends(Server.onlineUsers(), currentMessage.getFromUsername()));
                } catch (Exception e) {
                    Log.error(e);
                }
                serverClient.serverSender.sendMessage(currentMessage);

            } else {
                try {
                    if (currentMessage.getType() != null &&
                            currentMessage.getType().equals(Message.MessageType.REQUEST_MEMBERS_ONLINE) &&
                            ServerDatabaseHandler.getInstance().isGroupMember(serverClient.user.getUsername(),
                                    currentMessage.getFromUsername())) {

                        currentMessage.setType(Message.MessageType.RESPONSE_MEMBERS_ONLINE);
                        try {
                            currentMessage.setFreeObject(onlineMembers(Server.onlineUsers(), currentMessage.getFromUsername()));
                        } catch (Exception e) {
                            Log.error(e);
                        }
                        serverClient.serverSender.sendMessage(currentMessage);
                    } else if (currentMessage.getToGroup() != null && !currentMessage.getFromUsername().equals(serverClient.user.getUsername())) {
                        serverClient.serverSender.sendMessage(currentMessage);
                    } else if (currentMessage.getToUsername() != null && currentMessage.getToUsername().equals(serverClient.user.getUsername())) {
                        serverClient.serverSender.sendMessage(currentMessage);
                    }
                } catch (Exception e) {
                    Log.error(e);
                }
            }
        }
    }

    private ArrayList<User> onlineFriends(ArrayList<User> allOnline, String friendOf) {
        ArrayList<User> result = new ArrayList<>();
        ArrayList<String> allFriends = ServerDatabaseHandler.getInstance().loadAllFriends(friendOf);

        for (User u : allOnline) {
            if (allFriends.contains(u.getUsername()) && !u.getUsername().equals(friendOf)) {
                result.add(u);
            }
        }
        return result;
    }

    private ArrayList<User> onlineMembers(ArrayList<User> allOnline, String group) {
        ArrayList<User> result = new ArrayList<>();
        ArrayList<String> allFriends = ServerDatabaseHandler.getInstance().loadAllGroupMembers(group);

        for (User u : allOnline) {
            if (allFriends.contains(u.getUsername())) {
                result.add(u);
            }
        }
        return result;
    }

    public void run() {
        try {
            while (true) {
                Message message = getNextMessage();
                sendMessageToAll(message);
                if (message.getMessage() != null) {
                    ServerDatabaseHandler.getInstance().addMessage(message);
                }
            }
        } catch (Exception e) {
            Log.error(e);
        }
    }
}
