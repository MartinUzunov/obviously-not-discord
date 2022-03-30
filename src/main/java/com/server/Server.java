package com.server;

import com.ong.core.Log;
import com.ong.core.User;
import com.server.core.ServerClient;
import com.server.core.ServerDispatcher;
import com.server.core.ServerListener;
import com.server.core.ServerSender;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Server {
    public static final int PORT = 4999;
    private static ServerSocket serverSocket;
    private static ServerDispatcher serverDispatcher;

    public static Set<ServerClient> activeServerClients = new HashSet<>();

    public static void main(String args[]) {
        Log.init();
        bindServerSocket();

        serverDispatcher = new ServerDispatcher();
        serverDispatcher.start();

        handleClientConnections();
    }

    private static void bindServerSocket() {
        try {
            serverSocket = new ServerSocket(PORT);
            Log.info("The Server started on " + "port " + PORT);
        }
        catch (IOException e) {
            Log.error("Can't start listening on " + "port " + PORT);
            Log.error(e);
            System.exit(-1);
        }
    }

    private static void handleClientConnections() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();

                ServerClient serverClient = new ServerClient();

                OutputStream outputStream = socket.getOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

                InputStream inputStream = socket.getInputStream();
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

                serverClient.user = (User) objectInputStream.readObject();
                serverClient.socket = socket;
                objectOutputStream.reset();

                ServerSender serverSender = new ServerSender(serverClient, serverDispatcher, objectOutputStream);
                ServerListener serverListener = new ServerListener(serverClient, serverDispatcher, objectInputStream);

                serverClient.serverListener = serverListener;
                serverListener.start();

                serverClient.serverSender = serverSender;
                serverSender.start();

                serverDispatcher.addClient(serverClient);
                if(!activeServerClients.contains(serverClient)){
                    activeServerClients.add(serverClient);
                    Log.info(serverClient.user.getUsername() + " Connected!");
                }
            }
            catch (Exception e) {
                Log.error(e);
            }
        }
    }

    public static ArrayList<User> onlineUsers(){
        ArrayList<User> result = new ArrayList<>();
        for(ServerClient sc : activeServerClients){
            result.add(sc.user);
        }
        return result;
    }
}
