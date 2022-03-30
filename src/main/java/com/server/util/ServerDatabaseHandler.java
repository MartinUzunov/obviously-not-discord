package com.server.util;

import com.ong.core.Log;
import com.ong.core.Message;

import java.io.FileInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

@SuppressWarnings ("SqlResolve")
public class ServerDatabaseHandler {
    private static ServerDatabaseHandler instance;
    private static ServerDatabaseHandler.DatabaseCredentials databaseCredentials;
    private static Connection connection;

    private ServerDatabaseHandler() {
        loadCredentials();
    }

    public static ServerDatabaseHandler getInstance() {
        if (instance == null) {
            instance = new ServerDatabaseHandler();
            try {
                connection = DriverManager.getConnection("jdbc:mysql://localhost:" + databaseCredentials.getPort() + "/" +
                        databaseCredentials.getDatabaseName(), databaseCredentials.getName(), databaseCredentials.getPassword());
            } catch (Exception e) {
                Log.error(e);
            }
        }

        return instance;
    }

    private void loadCredentials() {
        if (databaseCredentials == null) {
            Properties prop = new Properties();
            try {
                prop.load(new FileInputStream("./src/main/java/com/ong/session/database.properties"));
            } catch (Exception e) {
                Log.error(e);
            }

            String databaseName = prop.getProperty("database-name");
            String username = prop.getProperty("username");
            String password = prop.getProperty("password");
            String port = prop.getProperty("port");
            databaseCredentials = new ServerDatabaseHandler.DatabaseCredentials(databaseName, username, password, port);
        }
    }

    public void addMessage(Message message) {
        PreparedStatement psInsert = null;

        try {
            psInsert = connection.prepareStatement("INSERT INTO messages " +
                    "(to_username,to_group,to_group_category," +
                    "to_group_channel," +
                    "from_username,message) VALUES (?, ?, ?, ?, ?, ?)");
            psInsert.setString(1, message.getToUsername());
            psInsert.setString(2, message.getToGroup());
            psInsert.setString(3, message.getToCategory());
            psInsert.setString(4, message.getToChannel());
            psInsert.setString(5, message.getFromUsername());
            psInsert.setString(6, message.getMessage());
            psInsert.executeUpdate();
        } catch (Exception e) {
            Log.error(e);
        } finally {
            closeAll(psInsert);
        }
    }

    public ArrayList<String> loadAllFriends(String name) {
        PreparedStatement ps = null;
        PreparedStatement findPhoto = null;
        PreparedStatement userId = null;
        ResultSet rs = null;
        ResultSet rs1 = null;
        ResultSet photoSet = null;
        ArrayList<String> result = new ArrayList<>();
        int id;

        try {
            userId = connection.prepareStatement("select * from users WHERE username = ?");
            userId.setString(1, name);
            rs1 = userId.executeQuery();
            rs1.next();
            id = rs1.getInt("user_id");

            ps = connection.prepareStatement("select * from user_relationship WHERE first_user_id = ?");
            ps.setInt(1, id);

            rs = ps.executeQuery();

            findPhoto = connection.prepareStatement("select * from users WHERE user_id = ?");

            while (rs.next()) {
                id = rs.getInt("second_user_id");
                findPhoto.setInt(1, id);
                photoSet = findPhoto.executeQuery();
                photoSet.next();

                String username = photoSet.getString("username");
                if (!username.equals(name)) {
                    result.add(username);
                }
            }

            ps = connection.prepareStatement("select * from user_relationship WHERE second_user_id = ?");
            ps.setInt(1, id);

            rs = ps.executeQuery();

            findPhoto = connection.prepareStatement("select * from users WHERE user_id = ?");

            while (rs.next()) {
                id = rs.getInt("first_user_id");
                findPhoto.setInt(1, id);
                photoSet = findPhoto.executeQuery();
                photoSet.next();

                String username = photoSet.getString("username");
                if (!username.equals(name)) {
                    result.add(username);
                }
            }
        } catch (Exception e) {
            Log.error(e);
        } finally {
            closeAll(ps, findPhoto, userId, rs, rs1, photoSet);
        }

        return result;
    }

    public ArrayList<String> loadAllGroupMembers(String name) {
        ArrayList<String> result = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = connection.prepareStatement("SELECT * FROM users WHERE user_id IN (SELECT user_id FROM user_in_group" +
                    " WHERE group_id = (SELECT `group_id` FROM `groups` WHERE group_name = ?))");
            ps.setString(1, name);
            rs = ps.executeQuery();

            while (rs.next()) {
                result.add(rs.getString("username"));
            }
        } catch (Exception e) {
            Log.error(e);
        } finally {
            closeAll(ps, rs);
        }

        return result;
    }

    public boolean isGroupMember(String username, String group) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean result = false;

        try {
            ps = connection.prepareStatement("SELECT * FROM user_in_group where user_id = (SELECT user_id FROM users " +
                    "WHERE username = ?) AND group_id = (SELECT group_id from `groups` WHERE group_name = ?)");
            ps.setString(1, username);
            ps.setString(2, group);
            rs = ps.executeQuery();

            if (rs.next()) {
                result = true;
            }
        } catch (Exception e) {
            Log.error(e);
        } finally {
            closeAll(ps, rs);
        }
        return result;
    }

    /**
     * Quietly closes all SQL structures that are passed as argument.
     * @param args - unknown number of SQL structures.
     */
    private void closeAll(AutoCloseable... args) {
        for (AutoCloseable arg : args) {
            if (arg != null) {
                try {
                    arg.close();
                } catch (Exception e) {
                    Log.error(e);
                }
            }
        }
    }

    private final class DatabaseCredentials {
        private final String databaseName;
        private final String name;
        private final String password;
        private final String port;

        public DatabaseCredentials(String _databaseName, String _name, String _password, String _port) {
            this.databaseName = _databaseName;
            this.name = _name;
            this.password = _password;
            this.port = _port;
        }

        public String getDatabaseName() {
            return databaseName;
        }

        public String getName() {
            return name;
        }

        public String getPassword() {
            return password;
        }

        public String getPort() {
            return port;
        }
    }
}
