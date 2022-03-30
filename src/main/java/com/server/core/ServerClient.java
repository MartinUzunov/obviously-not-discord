package com.server.core;


import com.ong.core.User;

import java.net.Socket;
import java.util.Objects;

public class ServerClient {
    public User user;
    public Socket socket = null;
    public ServerListener serverListener = null;
    public ServerSender serverSender = null;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerClient serverClient = (ServerClient) o;
        return Objects.equals(socket, serverClient.socket);
    }

    @Override
    public int hashCode() {
        return Objects.hash(socket, serverListener, serverSender);
    }
}
