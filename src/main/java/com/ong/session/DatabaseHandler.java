package com.ong.session;

import com.ong.core.Log;
import com.ong.core.Message;
import com.ong.core.User;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Properties;

/**
 * Singleton class which handles all Database operations. The class loads the Database credentials from
 * database.properties file.
 * All methods must return a boolean value(which represents whether the operation is successful or not) or
 * appropriate type/class.
 */
@SuppressWarnings ("SqlResolve")
public final class DatabaseHandler {

    private static DatabaseHandler instance;
    private static DatabaseCredentials databaseCredentials;
    private static Connection connection;

    private DatabaseHandler() {
        loadCredentials();
    }

    /**
     * Initiate a connection to the Database, if one is not already present.
     *
     * @return DatabaseHandler
     */
    public static DatabaseHandler getInstance() {
        if (instance == null) {
            instance = new DatabaseHandler();
            try {
                connection = DriverManager.getConnection("jdbc:mysql://localhost:" + databaseCredentials.getPort() + "/" +
                        databaseCredentials.getDatabaseName(), databaseCredentials.getName(), databaseCredentials.getPassword());
            } catch (Exception e) {
                Log.error(e);
            }
        }
        return instance;
    }

    /**
     * Adds the user information to the DB(table 'users'), if the user does not already exist.
     *
     * @param user - the user that needs to be signed up
     * @return true: The sign-up is successful.
     *         false: The sign-up is not successful.
     */
    public boolean signUpUser(User user) {
        boolean successful = false;
        PreparedStatement psInsert = null;
        PreparedStatement psCheckUserExists = null;
        ResultSet rs = null;

        try {
            psCheckUserExists = connection.prepareStatement("SELECT * FROM users where username = ?");
            psCheckUserExists.setString(1, user.getUsername());
            rs = psCheckUserExists.executeQuery();

            if (rs.isBeforeFirst()) {
                Log.info("User already exists!");
            } else {
                psInsert = connection.prepareStatement("INSERT INTO users (username,email,password,birth_date) VALUES (?, ?, ?, ?)");
                psInsert.setString(1, user.getUsername());
                psInsert.setString(2, user.getEmail());
                psInsert.setString(3, user.getPassword());
                psInsert.setString(4, user.getBirthDate());
                psInsert.executeUpdate();
                successful = true;
            }
        } catch (Exception e) {
            Log.error("Could not Sign-up user " + user.getUsername(), e);
        } finally {
            closeAll(rs, psCheckUserExists, psInsert);
        }
        return successful;
    }

    /**
     *
     * Checks(in DB table 'users') if username, password pair exists in the DB and loads the user information to
     * User class.
     *
     * @param username
     * @param password
     * @return User: The user exists and the loading of the information is successful.
     *         null: The user does not exist or the loading of the information is not successful.
     */
    public User signInUser(String username, String password) {
        PreparedStatement psCheckUserExists = null;
        ResultSet rs = null;
        User user = null;

        try {
            psCheckUserExists = connection.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?");
            psCheckUserExists.setString(1, username);
            psCheckUserExists.setString(2, password);
            rs = psCheckUserExists.executeQuery();

            if (!rs.isBeforeFirst()) {
                Log.error("Invalid username or password!");
            }
            else if (rs.next()) {
                user = new User(rs.getString("username"), rs.getString("email"),
                        rs.getString("password"), rs.getString("birth_date"));

                Blob blob = rs.getBlob("profile_photo");
                byte b[] = blob.getBytes(1, (int) blob.length());
                ByteArrayInputStream bais = new ByteArrayInputStream(b);
                Image im = new Image(bais);
                user.setImage(im);

            }
        } catch (Exception e) {
            Log.error("The loading of the User is not successful!", e);
        } finally {
            closeAll(psCheckUserExists, rs);
        }
        return user;
    }



    /**
     * Retrieves the appropriate profile_photo column from DB table 'users'.
     * Loads the user image BLOB, converts it to Byte stream and creates Image from it.
     *
     * @param username
     * @return Image: the loading of the photo is successful.
     *          null: the loading of the photo is not successful.
     */
    public Image getUserPhoto(String username) {
        PreparedStatement psFindPhoto = null;
        ResultSet rs = null;
        Image image = null;

        try {
            psFindPhoto = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
            psFindPhoto.setString(1, username);
            rs = psFindPhoto.executeQuery();
            if (rs.next()) {
                Blob blob = rs.getBlob("profile_photo");
                byte b[] = blob.getBytes(1, (int) blob.length());
                ByteArrayInputStream bais = new ByteArrayInputStream(b);
                image = new Image(bais);
            }
        } catch (Exception e) {
            Log.error("The loading of the User photo is not successful!", e);
        } finally {
            closeAll(psFindPhoto, rs);
        }
        return image;
    }

    /**
     * DB table 'user_relationship'.
     * @param firstUsername
     * @param secondUsername
     * @return The current DM status between two users.
     */
    public int getDMStatus(String firstUsername, String secondUsername) {
        PreparedStatement psDmStatus = null;
        ResultSet rs = null;
        int status = 0;

        try {
            psDmStatus = connection.prepareStatement("SELECT open_dm FROM user_relationship WHERE " +
                    "(first_user_id = (SELECT user_id FROM users where username = ?) AND second_user_id = (SELECT " +
                    "user_id FROM users where username = ?))");
            psDmStatus.setString(1, firstUsername);
            psDmStatus.setString(2, secondUsername);
            rs = psDmStatus.executeQuery();

            if (rs.next()) {
                status = rs.getInt(1);
            }
        } catch (Exception e) {
            Log.error("Could not get DM status between " + firstUsername + " and " + secondUsername, e);
        } finally {
            closeAll(psDmStatus, rs);
        }
        return status;
    }

    /**
     * DB table 'users'.
     * The preferred statuses are:
     *  Online, Away, Do Not Disturb, Invisible.
     *
     * @param username
     * @return The preferred status of a user.
     */
    public String getPreferredStatus(String username) {
        PreparedStatement psGetPreferredStatus = null;
        ResultSet rs = null;
        String result = "";

        try {
            psGetPreferredStatus = connection.prepareStatement("SELECT preferred_status FROM users WHERE username = ?");
            psGetPreferredStatus.setString(1, username);
            rs = psGetPreferredStatus.executeQuery();

            if (rs.next()) {
                result = rs.getString(1);
            }
        } catch (Exception e) {
            Log.error("Could not get preferred status for user:" + username, e);
        } finally {
            closeAll(psGetPreferredStatus, rs);
        }
        return result;
    }

    /**
     * Retrieves all appropriate records from DB table 'user_relationship' and matches them to table 'users'.
     *
     * user_relationship / open_dm statuses:
     *  0 - no opened DM
     *  1 - opened for first user
     *  2 - opened for second user
     *  3 - opened for both users
     *
     * @param username
     * @param dm - Used to determine, if all friends should be added or only the friends with which the user has
     *             an open Direct Message.
     * @param type - represents the type of the relationship.(e.g. friends, first_requested(friend request) etc.)
     * @return If dm is false: HashMap of the Names and the profile Photos of all Friends.
     *         If dm is true:  HashMap of the names and the profile photos of the friends with which the User has an open
     *                         Direct message.
     */
    public HashMap<String, Image> loadAllFriends(String username, boolean dm, String type) {
        PreparedStatement psGetRelationship = null;
        PreparedStatement psGetUserId = null;
        ResultSet rs = null;
        ResultSet rsRelationship = null;
        HashMap<String, Image> result = new HashMap<>();
        int id = -1;

        try {
            // getting the UserId, so it can be used in the next statements
            psGetUserId = connection.prepareStatement("SELECT user_id FROM users WHERE username = ?");
            psGetUserId.setString(1, username);
            rs = psGetUserId.executeQuery();
            if (rs.next()) {
                id = rs.getInt(1);
            }

            if (!dm) { // get all friends
                psGetRelationship = connection.prepareStatement("SELECT * FROM user_relationship WHERE (" +
                        "                        (first_user_id = ? OR second_user_id = ?) AND type LIKE ?)");
            } else { // get all friends that have open DM
                psGetRelationship = connection.prepareStatement("SELECT * FROM user_relationship WHERE ((" +
                        "                        (first_user_id = ? AND open_dm IN (1,3)) OR " +
                        "                        (second_user_id = ? AND open_dm IN (2,3))) AND type LIKE ?)");
            }
            psGetRelationship.setInt(1, id);
            psGetRelationship.setInt(2, id);
            psGetRelationship.setString(3, type);
            rs = psGetRelationship.executeQuery();

            // statement to get a single friend
            psGetRelationship = connection.prepareStatement("SELECT * FROM users WHERE user_id = ?");

            // iterate all friends that meet the requirements
            while (rs.next()) {
                // determine which column from user_relationship to use
                int currentId = rs.getInt("first_user_id");
                if (id == currentId) {
                    currentId = rs.getInt("second_user_id");
                    if (rs.getString("type").equals("first_requests")) {
                        continue;
                    }
                } else if (rs.getString("type").equals("second_requests")) {
                    continue;
                }

                psGetRelationship.setInt(1, currentId);
                rsRelationship = psGetRelationship.executeQuery();

                // get the current friend information
                if (rsRelationship.next()) {
                    Blob blob = rsRelationship.getBlob("profile_photo");
                    byte b[] = blob.getBytes(1, (int) blob.length());
                    ByteArrayInputStream bais = new ByteArrayInputStream(b);
                    Image image = new Image(bais);
                    String friend = rsRelationship.getString("username");
                    if (!friend.equals(username)) {
                        result.put(friend, image);
                    }
                }
            }
        } catch (Exception e) {
            Log.error("Could not load all friends!", e);
        } finally {
            closeAll(psGetRelationship, psGetUserId, rs, rsRelationship);
        }
        return result;
    }

    /**
     * Loads all Direct Messages between two users(from DB table 'messages').
     *
     * @param firstUsername
     * @param secondUsername
     * @return ArrayList of all the Message's between two users.
     */
    public ArrayList<Message> loadDMMessages(String firstUsername, String secondUsername) {
        PreparedStatement psLoadMessages = null;
        ResultSet rs = null;
        ArrayList<Message> result = new ArrayList<>();

        try {
            psLoadMessages = connection.prepareStatement("SELECT * FROM messages WHERE (to_username = ? AND " +
                    "from_username = ?) OR (to_username = ? AND from_username = ?)");
            psLoadMessages.setString(1, firstUsername);
            psLoadMessages.setString(2, secondUsername);
            psLoadMessages.setString(3, secondUsername);
            psLoadMessages.setString(4, firstUsername);

            rs = psLoadMessages.executeQuery();

            while (rs.next()) {
                Timestamp timestamp = rs.getTimestamp("date_and_time");
//                LocalDateTime localDateTime = timestamp.toLocalDateTime();
                Message message = new Message(rs.getString("to_username"),
                        rs.getString("from_username"), rs.getString("message"), null);
                result.add(message);
            }
        } catch (Exception e) {
            Log.error("Could not load all direct messages between " + firstUsername + " and " + secondUsername, e);
        } finally {
            closeAll(psLoadMessages, rs);
        }
        return result;
    }

    /**
     * DB table 'users'.
     *
     * @param username
     * @return true: If the user exists.
     *        false: If the user does not exist.
     */
    public boolean checkIfUserExists(String username) {
        boolean result = false;
        PreparedStatement psCheckUserExists = null;
        ResultSet rs = null;

        try {
            psCheckUserExists = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
            psCheckUserExists.setString(1, username);
            rs = psCheckUserExists.executeQuery();

            if (rs.next()) {
                result = true;
            }
        } catch (Exception e) {
            Log.error("Could not check if user: " + username + " exists.", e);
        } finally {
            closeAll(psCheckUserExists, rs);
        }
        return result;
    }

    /**
     * DB table 'users'.
     * @param username
     * @param password
     * @return true: If Username and Password pair exists in users table.
     *        false: If Username and Password pair does not exist in users table.
     */
    public boolean validateUserPassword(String username, String password) {
        boolean successful = false;
        PreparedStatement psCheckPassword = null;
        ResultSet rs = null;

        try {
            psCheckPassword = connection.prepareStatement("SELECT * FROM users WHERE (username = ? AND password = ?)");
            psCheckPassword.setString(1, username);
            psCheckPassword.setString(2, password);
            rs = psCheckPassword.executeQuery();
            if (rs.next()) {
                successful = true;
            }
        } catch (Exception e) {
            Log.error("Could not validate password for user: " + username, e);
        } finally {
            closeAll(psCheckPassword, rs);
        }
        return successful;
    }

    /**
     * Updates the appropriate profile_photo column from DB table 'users'.
     *
     * @param username
     * @param newProfilePhoto
     * @return true: The updating of the profile photo is successful.
     *         false: The updating of the profile photo is not successful.
     */
    public boolean updateUserProfilePhoto(String username, String newProfilePhoto) {
        boolean successful = false;
        FileInputStream fs = null;
        PreparedStatement psUpdatePhoto = null;

        try {
            File f = new File(newProfilePhoto);
            fs = new FileInputStream(f);

            psUpdatePhoto = connection.prepareStatement("UPDATE users SET profile_photo = ? WHERE username = ?");
            psUpdatePhoto.setBinaryStream(1, fs, (int) f.length());
            psUpdatePhoto.setString(2, username);
            psUpdatePhoto.executeUpdate();
            successful = true;
        } catch (Exception e) {
            Log.error("Updating the the profile is not successful!", e);
        } finally {
            closeAll(psUpdatePhoto);
        }
        return successful;
    }

    /**
     * DB table 'users'.
     * @param username
     * @param newPassword
     * @return true: If the changing of the password is successful.
     *        false: If the changing of the password is not successful.
     */
    public boolean updateUserPassword(String username, String newPassword) {
        boolean successful = false;
        PreparedStatement psUpdatePassword = null;

        try {
            psUpdatePassword = connection.prepareStatement("UPDATE users SET password = ? WHERE username = ?");
            psUpdatePassword.setString(1, newPassword);
            psUpdatePassword.setString(2, username);
            psUpdatePassword.executeUpdate();
            successful = true;
        } catch (Exception e) {
            Log.error("Could not update password for user: " + username, e);
        } finally {
            closeAll(psUpdatePassword);
        }
        return successful;
    }

    /**
     * Changes the DM status( from DB table user_relationship) between two users.
     *
     * user_relationship / open_dm statuses:
     *  0 - no opened DM
     *  1 - opened for first user
     *  2 - opened for second user
     *  3 - opened for both users
     *
     * The function does not work for leveling down status (e.g. does not work from 3 to (2 or 1) or from (2 or 1) to 0)
     *
     * @param firstUsername
     * @param secondUsername
     * @param status - always the current status of the DM between the users must be passed
     * @return true: The updating of the DM status is successful.
     *         false: The updating of the DM status is not successful.
     */
    public boolean updateDMStatus(String firstUsername, String secondUsername, int status) {
        boolean successful = false;
        PreparedStatement psCheckFirstUser = null;
        PreparedStatement psUpdateStatus = null;
        ResultSet rs = null;

        try {
            // performs a test to check if the first or the second column of user_relationship should be used
            psCheckFirstUser = connection.prepareStatement("SELECT * FROM user_relationship WHERE " +
                    "(first_user_id = (SELECT user_id FROM users where username = ?) AND second_user_id = (SELECT " +
                    "user_id FROM users where username = ?))");
            psCheckFirstUser.setString(1, firstUsername);
            psCheckFirstUser.setString(2, secondUsername);
            rs = psCheckFirstUser.executeQuery();

            // updates the status depending on which column from user_relationship is used
            if (rs.next()) {  // using column first_user_id
                switch (status) {
                    case 0:
                        status = 1;
                        break;
                    case 2:
                        status = 3;
                        break;
                    default:
                        return false;
                }
            } else { // using column second_user_id
                switch (status) {
                    case 0:
                        status = 2;
                        break;
                    case 1:
                        status = 3;
                        break;
                    default:
                        return false;
                }
            }

            psUpdateStatus = connection.prepareStatement("UPDATE user_relationship SET open_dm = ? WHERE " +
                    "(first_user_id = (SELECT user_id FROM users where username = ?) AND second_user_id = (SELECT " +
                    "user_id FROM users where username = ?)) OR" +
                    "(first_user_id = (SELECT user_id FROM users where username = ?) AND second_user_id = (SELECT " +
                    "user_id FROM users where username = ?))");
            psUpdateStatus.setInt(1, status);
            psUpdateStatus.setString(2, firstUsername);
            psUpdateStatus.setString(3, secondUsername);
            psUpdateStatus.setString(4, secondUsername);
            psUpdateStatus.setString(5, firstUsername);
            psUpdateStatus.executeUpdate();
            successful = true;
        } catch (Exception e) {
            Log.error("Could not update DM status between " + firstUsername + " and " + secondUsername, e);
        } finally {
            closeAll(psUpdateStatus, rs, psCheckFirstUser);
        }
        return successful;
    }

    /**
     * DB table 'groups'.
     * The preferred statuses are:
     * Online, Away, Do Not Disturb, Invisible.
     *
     * @param username
     * @param newStatus
     * @return true: If the update of the status is successful.
     *        false: If the update of the status is not successful.
     */
    public boolean updatePreferredStatus(String username, String newStatus) {
        boolean successful = false;
        PreparedStatement psUpdatePreferredStatus = null;

        try {
            psUpdatePreferredStatus = connection.prepareStatement("UPDATE users SET preferred_status = ? WHERE username = ?");
            psUpdatePreferredStatus.setString(1, newStatus);
            psUpdatePreferredStatus.setString(2, username);
            psUpdatePreferredStatus.executeUpdate();
            successful = true;
        } catch (Exception e) {
            Log.error("Could not update preferred status of " + username + " to " + newStatus, e);
        } finally {
            closeAll(psUpdatePreferredStatus);
        }
        return successful;
    }

    /**
     * Updates(or creates) the user relationship between two users(DB table 'user_relationship'.). If the type =
     * 'request', the text is changed to the appropriate column (if the first user(first_user_id) request the
     * relationship, the text should be changed to 'first_requests', else the text should be changed to
     * 'second_requests').
     *
     * The current supported types are: friends, first_requests, second_requests.
     *
     * @param firstUsername
     * @param secondUsername
     * @param newType
     * @return true: If the updating of the relationship is successful.
     *        false: If the updating of the relationship is not successful.
     */
    public boolean updateUserRelationship(String firstUsername, String secondUsername, String newType) {
        boolean successful = false;
        PreparedStatement psUpdateRelationship = null;
        PreparedStatement psGetUserId = null;
        PreparedStatement psCheckFirstUser = null;
        PreparedStatement psCheckRecordExists = null;
        ResultSet rs = null;

        try {
            // get first user id
            psGetUserId = connection.prepareStatement("SELECT user_id FROM users WHERE username = ?");
            psGetUserId.setString(1, firstUsername);
            rs = psGetUserId.executeQuery();
            int firstID = -1;
            if (rs.next()) {
                firstID = rs.getInt(1);
            }

            // get second user id
            psGetUserId.setString(1, secondUsername);
            rs = psGetUserId.executeQuery();
            int secondID = -1;
            if (rs.next()) {
                secondID = rs.getInt(1);
            }

            psCheckRecordExists = connection.prepareStatement("SELECT * FROM user_relationship WHERE " +
                    "((first_user_id = ? AND second_user_id = ?) OR (first_user_id = ? AND second_user_id = ?))");
            psCheckRecordExists.setInt(1, firstID);
            psCheckRecordExists.setInt(2, secondID);
            psCheckRecordExists.setInt(3, secondID);
            psCheckRecordExists.setInt(4, firstID);
            rs = psCheckRecordExists.executeQuery();

            if (!rs.next()) { // create the record if it does not exist
                psUpdateRelationship = connection.prepareStatement("INSERT INTO user_relationship (first_user_id, second_user_id, type," +
                        " open_dm) VALUES (?, ?, ?, ?)");
                psUpdateRelationship.setInt(1, firstID);
                psUpdateRelationship.setInt(2, secondID);
                psUpdateRelationship.setString(3, "first_requests");
                psUpdateRelationship.setInt(4, 0);

            } else { // update the record if it exists
                psCheckFirstUser = connection.prepareStatement("SELECT * FROM user_relationship WHERE " +
                        "(first_user_id = ? AND second_user_id = ?)");
                psCheckFirstUser.setInt(1, firstID);
                psCheckFirstUser.setInt(2, secondID);
                rs = psCheckFirstUser.executeQuery();

                // determine which column to use
                if (!rs.next()) { // using the second column
                    int temp = firstID;
                    firstID = secondID;
                    secondID = temp;
                    if (newType.equals("request")) {
                        newType = "second_requests";
                    }
                } else if (newType.equals("request")) {
                    newType = "first_requests";
                }

                psUpdateRelationship = connection.prepareStatement("UPDATE user_relationship SET type = ? WHERE " +
                        "(first_user_id = ? AND second_user_id = ?)");
                psUpdateRelationship.setString(1, newType);
                psUpdateRelationship.setInt(2, firstID);
                psUpdateRelationship.setInt(3, secondID);
            }
            psUpdateRelationship.executeUpdate();
            successful = true;
        } catch (Exception e) {
            Log.error("Could not update relationship between " + firstUsername + " and " + secondUsername +
                    " to " + newType, e);
        } finally {
            closeAll(psUpdateRelationship, psGetUserId, psCheckFirstUser, psCheckRecordExists, rs);
        }
        return successful;
    }

    /**
     * Handles the closing of an DM between two users. Deletes the messages(DB table 'messages'.) between the users only
     * if, both users have the DM closed after the current request. Updates the user_relationship table appropriately.
     * The closing request must always be requested by firstUsername.
     *
     * @param firstUsername
     * @param secondUsername
     * @return true: The updating of the DM status is successful.
     */
    public boolean deleteDirectMessages(String firstUsername, String secondUsername) {
        boolean successful = false;

        PreparedStatement psUpdateRelationship = null;
        PreparedStatement psDeleteMessages = null;
        PreparedStatement psCheckFirstUser = null;
        ResultSet rs = null;

        try {
            int status = getDMStatus(firstUsername, secondUsername);

            if (status == 3) { // close DM only on one side
                psCheckFirstUser = connection.prepareStatement("SELECT * FROM user_relationship WHERE " +
                        "(first_user_id = (SELECT user_id FROM users WHERE username = ?) AND second_user_id = (SELECT" +
                        " " +
                        "user_id FROM users WHERE username = ?))");
                psCheckFirstUser.setString(1, firstUsername);
                psCheckFirstUser.setString(2, secondUsername);
                rs = psCheckFirstUser.executeQuery();

                if (rs.next()) { // the closing request came from the user in the first column
                    status = 2;
                } else { // the closing request came from the user in the second column
                    status = 1;
                }
            } else { // delete messages and close DM, because the DM is opened only on one side
                psDeleteMessages = connection.prepareStatement("DELETE FROM messages WHERE ((to_username = ? AND " +
                        "from_username = ?) OR (to_username = ? AND from_username = ?))");
                psDeleteMessages.setString(1, firstUsername);
                psDeleteMessages.setString(2, secondUsername);
                psDeleteMessages.setString(3, secondUsername);
                psDeleteMessages.setString(4, firstUsername);
                psDeleteMessages.executeUpdate();
                status = 0;
            }
            psUpdateRelationship = connection.prepareStatement("UPDATE user_relationship SET open_dm = ? WHERE " +
                    "(first_user_id = (SELECT user_id FROM users WHERE username = ?) AND second_user_id = (SELECT " +
                    "user_id FROM users WHERE username = ?)) OR" +
                    "(first_user_id = (SELECT user_id FROM users WHERE username = ?) AND second_user_id = (SELECT " +
                    "user_id FROM users WHERE username = ?))");
            psUpdateRelationship.setInt(1, status);
            psUpdateRelationship.setString(2, firstUsername);
            psUpdateRelationship.setString(3, secondUsername);
            psUpdateRelationship.setString(4, secondUsername);
            psUpdateRelationship.setString(5, firstUsername);
            psUpdateRelationship.executeUpdate();

            successful = true;
        } catch (Exception e) {
            Log.error("Couldn't delete messages between " + firstUsername + " and " + secondUsername, e);
        } finally {
            closeAll(psUpdateRelationship, psDeleteMessages, psCheckFirstUser, rs);
        }

        return successful;
    }



    /**
     * Loads the group image BLOB(from DB table 'groups'.), converts it to Byte stream and creates Image from it.
     *
     * @param groupName
     * @return Image: the loading of the photo is successful.
     *          null: the loading of the photo is not successful.
     */
    public Image getGroupPhoto(String groupName) {
        PreparedStatement psGetPhoto = null;
        ResultSet rs = null;
        Image groupPhoto = null;

        try {
            psGetPhoto = connection.prepareStatement("SELECT * FROM `groups` WHERE group_name = ?");
            psGetPhoto.setString(1, groupName);
            rs = psGetPhoto.executeQuery();
            rs.next();

            Blob blob = rs.getBlob("group_photo");
            byte b[] = blob.getBytes(1, (int) blob.length());
            ByteArrayInputStream bais = new ByteArrayInputStream(b);
            groupPhoto = new Image(bais);
        } catch (Exception e) {
            Log.error("Could not load the Group(" + groupName + ") photo!", e);
        } finally {
            closeAll(psGetPhoto, rs);
        }
        return groupPhoto;
    }

    /**
     * DB table 'user_in_group'.
     *
     * @param groupName
     * @param username
     * @return The role of the given user in the group.
     */
    public String getUserInGroupRole(String groupName, String username) {
        PreparedStatement psUserInGroup = null;
        ResultSet rs = null;
        String result = "";

        try {
            psUserInGroup = connection.prepareStatement("SELECT role FROM user_in_group WHERE group_id = (SELECT " +
                    "group_id FROM `groups` WHERE group_name = ?) AND user_id = (SELECT user_id FROM users WHERE " +
                    "username = ?)");
            psUserInGroup.setString(1, groupName);
            psUserInGroup.setString(2, username);
            rs = psUserInGroup.executeQuery();

            if (rs.next()) {
                result = rs.getString(1);
            }
        } catch (Exception e) {
            Log.error("Could not get user: " + username + " role in group: " + groupName, e);
        } finally {
            closeAll(psUserInGroup, rs);
        }
        return result;
    }

    /**
     * From DB table 'user_in_group'.
     *
     * @param groupName
     * @return HashMap of the Names and the profile Photos of all Group members.
     */
    public HashMap<String, Image> loadAllGroupMembers(String groupName) {
        PreparedStatement psGetMembers = null;
        ResultSet rs = null;
        HashMap<String, Image> result = new HashMap<>();

        try {
            psGetMembers = connection.prepareStatement("SELECT * FROM users WHERE user_id IN (SELECT user_id FROM user_in_group" +
                    " WHERE group_id = (SELECT `group_id` FROM `groups` WHERE group_name = ?))");
            psGetMembers.setString(1, groupName);
            rs = psGetMembers.executeQuery();

            while (rs.next()) {
                Blob blob = rs.getBlob("profile_photo");
                byte b[] = blob.getBytes(1, (int) blob.length());
                ByteArrayInputStream bais = new ByteArrayInputStream(b);
                Image image = new Image(bais);
                result.put(rs.getString("username"), image);
            }
        } catch (Exception e) {
            Log.error("Could not load all " + groupName + " members!", e);
        } finally {
            closeAll(psGetMembers, rs);
        }
        return result;
    }

    /**
     * From DB table 'messages'.
     *
     * @param groupName
     * @param category
     * @param channel
     * @return ArrayList of all Message's in a specific Group Channel in the given category.
     */
    public ArrayList<Message> loadGroupChannelMessages(String groupName, String category, String channel) {
        ArrayList<Message> result = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = connection.prepareStatement("SELECT * FROM messages WHERE (to_group = ? AND to_group_category = ? AND " +
                    "to_group_channel = ?)");
            ps.setString(1, groupName);
            ps.setString(2, category);
            ps.setString(3, channel);

            rs = ps.executeQuery();

            while (rs.next()) {
                Timestamp timestamp = rs.getTimestamp("date_and_time");
//                LocalDateTime localDateTime = timestamp.toLocalDateTime();
                Message message = new Message(rs.getString("to_group"), rs.getString(
                        "to_group_category"),
                        rs.getString("to_group_channel"),
                        rs.getString("from_username"), rs.getString("message"), null);
                result.add(message);
            }
        } catch (Exception e) {
            Log.error("Could not load Group(" + groupName + ")/Channel(" + channel + ") messages!", e);
        } finally {
            closeAll(ps, rs);
        }
        return result;
    }

    /**
     * Loads all groups(from DB table 'groups'.) in which the user type is equal to the given type.
     *
     * @param username
     * @param type - the user status in the group (e.g. member, banned etc.). Ensures that only appropriate groups are
     * loaded.
     * @return HashMap of Group names and Group photos.
     */
    public LinkedHashMap<String, Image> loadAllGroups(String username, String type) {
        PreparedStatement psGetAllGroups = null;
        PreparedStatement psGetGroup = null;
        ResultSet rsGetAllGroups = null;
        ResultSet rsGroup = null;
        LinkedHashMap<String, Image> result = new LinkedHashMap<>();

        try {
            psGetAllGroups = connection.prepareStatement("SELECT * FROM user_in_group WHERE (user_id = (SELECT user_id FROM users" +
                    " WHERE username = ?) AND type = ?)");
            psGetAllGroups.setString(1, username);
            psGetAllGroups.setString(2, type);
            rsGetAllGroups = psGetAllGroups.executeQuery();

            psGetGroup = connection.prepareStatement("SELECT * FROM `groups` WHERE group_id = ?");

            while (rsGetAllGroups.next()) {
                int id = rsGetAllGroups.getInt("group_id");
                psGetGroup.setInt(1, id);
                rsGroup = psGetGroup.executeQuery();

                if (rsGroup.next()) {
                    String groupName = rsGroup.getString("group_name");
                    Blob blob = rsGroup.getBlob("group_photo");
                    byte b[] = blob.getBytes(1, (int) blob.length());
                    ByteArrayInputStream bais = new ByteArrayInputStream(b);
                    Image image = new Image(bais);
                    result.put(groupName, image);
                }
            }
        } catch (Exception e) {
            Log.error("Could not load all Groups for " + username, e);
        } finally {
            closeAll(psGetAllGroups, psGetGroup, rsGetAllGroups, rsGroup);
        }
        return result;
    }

    /**
     * Retrieves all Categories(from DB table 'group_channels'.) in a group and all channels in that category.
     *
     * @param groupName
     * @return HashMap of key: categories name, value: ArrayList of all channels in the category.
     */
    public LinkedHashMap<String, ArrayList<String>> loadAllGroupChannels(String groupName) {
        PreparedStatement psLoadChannels = null;
        ResultSet rs = null;
        LinkedHashMap<String, ArrayList<String>> result = new LinkedHashMap<>();

        try {
            psLoadChannels = connection.prepareStatement("SELECT * FROM group_channels WHERE group_name = ?");
            psLoadChannels.setString(1, groupName);

            rs = psLoadChannels.executeQuery();

            while (rs.next()) {
                String category = rs.getString("category");
                String channel = rs.getString("channel_name");

                ArrayList<String> channels;
                if (result.containsKey(category)) {
                    channels = result.get(category);
                } else {
                    channels = new ArrayList<>();
                }
                channels.add(channel);
                result.put(category, channels);
            }
        } catch (Exception e) {
            Log.error("Could not load all Group(" + groupName + ") channels!", e);
        } finally {
            closeAll(psLoadChannels, rs);
        }
        return result;
    }

    /**
     * DB table 'groups'.
     *
     * @param groupName
     * @return true: If the group exists.
     *        false: If the group does not exist.
     */
    public boolean checkIfGroupExists(String groupName) {
        boolean result = false;
        PreparedStatement psCheckGroupExists = null;
        ResultSet rs = null;

        try {
            psCheckGroupExists = connection.prepareStatement("SELECT * FROM `groups` WHERE group_name = ?");
            psCheckGroupExists.setString(1, groupName);
            rs = psCheckGroupExists.executeQuery();

            if (rs.next()) {
                result = true;
            }
        } catch (Exception e) {
            Log.error("Could not check if group: " + groupName + " exists.", e);
        } finally {
            closeAll(psCheckGroupExists, rs);
        }
        return result;
    }

    /**
     * DB table 'group_channels'.
     *
     * @param groupName
     * @param category
     * @return true: If the category exists.
     *        false: If the category does not exist.
     */
    public boolean checkIfCategoryExists(String groupName, String category) {
        boolean result = false;
        PreparedStatement psCheckCategoryExists = null;
        ResultSet rs = null;

        try {
            psCheckCategoryExists = connection.prepareStatement("SELECT * FROM `group_channels` WHERE group_name = ? " +
                    "AND category = ?");
            psCheckCategoryExists.setString(1, groupName);
            psCheckCategoryExists.setString(2, category);
            rs = psCheckCategoryExists.executeQuery();

            if (rs.next()) {
                result = true;
            }
        } catch (Exception e) {
            Log.error("Could not check if category: " + category + " exists in group " + groupName, e);
        } finally {
            closeAll(psCheckCategoryExists, rs);
        }
        return result;
    }

    /**
     * DB table 'group_channels'.
     *
     * @param groupName
     * @param category
     * @param channel
     * @return true: If the channel exists.
     *        false: If the channel does not exist.
     */
    public boolean checkIfChannelExists(String groupName, String category, String channel) {
        boolean result = false;
        PreparedStatement psCheckChannelExists = null;
        ResultSet rs = null;

        try {
            psCheckChannelExists = connection.prepareStatement("SELECT * FROM `group_channels` WHERE group_name = ? " +
                    "AND category = ? AND channel_name = ?");
            psCheckChannelExists.setString(1, groupName);
            psCheckChannelExists.setString(2, category);
            psCheckChannelExists.setString(3, channel);
            rs = psCheckChannelExists.executeQuery();

            if (rs.next()) {
                result = true;
            }
        } catch (Exception e) {
            Log.error("Could not check if channel: " + channel + " exists in category " + category +
                    " in group " + groupName, e);
        } finally {
            closeAll(psCheckChannelExists, rs);
        }
        return result;
    }

    /**
     * Changes records in DB tables 'groups', 'messages' and 'group_channels'.
     *
     * @param oldGroupName
     * @param newGroupName
     * @return true: If the change is successful.
     *        false: If the change is not successful.
     */
    public boolean updateGroupName(String oldGroupName, String newGroupName) {
        boolean successful = false;
        PreparedStatement psUpdateGroupChannels = null;
        PreparedStatement psUpdateMessages = null;
        PreparedStatement psUpdateGroups = null;

        try {
            psUpdateGroupChannels = connection.prepareStatement("UPDATE group_channels SET group_name = ? WHERE " +
                    "group_name = ?");
            psUpdateGroupChannels.setString(1, newGroupName);
            psUpdateGroupChannels.setString(2, oldGroupName);

            psUpdateMessages = connection.prepareStatement("UPDATE messages SET to_group = ? WHERE " +
                    "to_group = ?");
            psUpdateMessages.setString(1, newGroupName);
            psUpdateMessages.setString(2, oldGroupName);

            psUpdateGroups = connection.prepareStatement("UPDATE `groups` SET group_name = ? WHERE " +
                    "group_name = ?");
            psUpdateGroups.setString(1, newGroupName);
            psUpdateGroups.setString(2, oldGroupName);

            psUpdateGroupChannels.executeUpdate();
            psUpdateMessages.executeUpdate();
            psUpdateGroups.executeUpdate();

            successful = true;
        } catch (Exception e) {
            Log.error("Could not change group: " + oldGroupName + " name to " + newGroupName, e);
        } finally {
            closeAll(psUpdateGroupChannels, psUpdateMessages, psUpdateGroups);
        }
        return successful;
    }

    /**
     * DB table 'groups'.
     *
     * @param groupName
     * @param newImagePath - the image path on the local machine
     * @return true: The updating of the profile photo is successful.
     *         false: The updating of the profile photo is not successful.
     */
    public boolean updateGroupProfilePhoto(String groupName, String newImagePath) {
        boolean successful = false;
        PreparedStatement psUpdatePhoto = null;

        try {
            File photo = new File(newImagePath);
            FileInputStream photoAsStream = new FileInputStream(photo);

            psUpdatePhoto = connection.prepareStatement("UPDATE `groups` SET group_photo = ? WHERE group_name = ?");
            psUpdatePhoto.setBinaryStream(1, photoAsStream, (int) photo.length());
            psUpdatePhoto.setString(2, groupName);
            psUpdatePhoto.executeUpdate();
            successful = true;
        } catch (Exception e) {
            Log.error("Could not update group:" + groupName + " photo!", e);
        } finally {
            closeAll(psUpdatePhoto);
        }
        return successful;
    }

    /**
     * Updated the relationship between user and group(DB table 'user_in_group').
     * The current available types are: member, request.
     *
     * @param username
     * @param groupName
     * @param newType
     * @return true: If the updating of the relationship is successful.
     *        false: If the updating of the relationship is not successful.
     */
    public boolean updateUserToGroupRelationship(String username, String groupName, String newType) {
        boolean successful = false;
        PreparedStatement psUpdateGroupRelationship = null;
        PreparedStatement psGetID = null;
        PreparedStatement psCheckRecordExists = null;
        ResultSet rs = null;

        try {
            // get user id
            psGetID = connection.prepareStatement("SELECT user_id FROM users WHERE username = ?");
            psGetID.setString(1, username);
            rs = psGetID.executeQuery();
            int userID = -1;
            if (rs.next()) {
                userID = rs.getInt(1);
            }

            // get group id
            psGetID = connection.prepareStatement("SELECT group_id FROM `groups` WHERE group_name = ?");
            psGetID.setString(1, groupName);
            rs = psGetID.executeQuery();
            int groupID = -1;
            if (rs.next()) {
                groupID = rs.getInt(1);
            }

            psCheckRecordExists = connection.prepareStatement("SELECT * FROM user_in_group WHERE user_id = ? AND " +
                    "group_id = ?");
            psCheckRecordExists.setInt(1, userID);
            psCheckRecordExists.setInt(2, groupID);
            rs = psCheckRecordExists.executeQuery();

            if (!rs.next()) { // create record
                psUpdateGroupRelationship = connection.prepareStatement("INSERT INTO user_in_group (user_id, group_id, role, type)" +
                        " VALUES (?, ?, ?, ?)");
                psUpdateGroupRelationship.setInt(1, userID);
                psUpdateGroupRelationship.setInt(2, groupID);
                psUpdateGroupRelationship.setString(3, "No Role");
                psUpdateGroupRelationship.setString(4, newType);
            } else { // update record
                psUpdateGroupRelationship = connection.prepareStatement("UPDATE user_in_group SET type = ? WHERE user_id = ? AND group_id = ?");
                psUpdateGroupRelationship.setString(1, newType);
                psUpdateGroupRelationship.setInt(2, userID);
                psUpdateGroupRelationship.setInt(3, groupID);
            }
            psUpdateGroupRelationship.executeUpdate();
            successful = true;
        } catch (Exception e) {
            Log.error("Could not update relationship between group " + groupName + " and user " + username +
                    " to " + newType, e);
        } finally {
            closeAll(psUpdateGroupRelationship, psGetID, psCheckRecordExists, rs);
        }
        return successful;
    }

    /**
     * Creates new group by updating DB tables: groups, group_channels(inserts default category and channel),
     * user_in_group(inserts the user as role = 'admin' and type = 'member').
     *
     * @param groupName
     * @param groupCreator
     * @return true: If the creation is successful.
     *        false: If the creation is not successful.
     */
    public boolean createGroup(String groupName, String groupCreator) {
        boolean successful = false;
        PreparedStatement psInsertInGroups = null;
        PreparedStatement psInsertInGroupChannels = null;
        PreparedStatement psInsertUserInGroup = null;

        try {
            psInsertInGroups = connection.prepareStatement("INSERT INTO `groups` (group_name) VALUES (?)");
            psInsertInGroups.setString(1, groupName);
            psInsertInGroups.executeUpdate();

            psInsertInGroupChannels = connection.prepareStatement("INSERT INTO group_channels (group_name, category, " +
                    "channel_name) VALUES (?, ?, ?)");
            psInsertInGroupChannels.setString(1, groupName);
            psInsertInGroupChannels.setString(2, "Channels");
            psInsertInGroupChannels.setString(3, "General");
            psInsertInGroupChannels.executeUpdate();

            psInsertUserInGroup = connection.prepareStatement("INSERT INTO user_in_group (user_id, group_id, role, " +
                    "type) VALUES ((SELECT user_id FROM users WHERE username = ?), (SELECT group_id from `groups` " +
                    "WHERE group_name = ?), ?, ?)");
            psInsertUserInGroup.setString(1, groupCreator);
            psInsertUserInGroup.setString(2, groupName);
            psInsertUserInGroup.setString(3, "admin");
            psInsertUserInGroup.setString(4, "member");
            psInsertUserInGroup.executeUpdate();
            Image image = new Image("com/ong/images/groups_default.png");
            updateGroupProfilePhoto(groupName, image.getUrl().substring(5));

            successful = true;
        } catch (Exception e) {
            Log.error("Could not create new group:" + groupName + " with creator " + groupCreator, e);
        } finally {
            closeAll(psInsertInGroups, psInsertInGroupChannels, psInsertUserInGroup);
        }
        return successful;
    }

    /**
     * DB table 'group_channels'.
     *
     * @param groupName
     * @param category
     * @param channel
     * @return true: If the creation is successful.
     *        false: If the creation is not successful.
     */
    public boolean createCategoryAndChannel(String groupName, String category, String channel) {
        boolean successful = false;
        PreparedStatement psCreateCategoryAndChannel = null;

        try {
            psCreateCategoryAndChannel = connection.prepareStatement("INSERT INTO group_channels (group_name, category," +
                    " channel_name) VALUES (?, ?, ?)");
            psCreateCategoryAndChannel.setString(1, groupName);
            psCreateCategoryAndChannel.setString(2, category);
            psCreateCategoryAndChannel.setString(3, channel);

            psCreateCategoryAndChannel.executeUpdate();
            successful = true;
        } catch (Exception e) {
            Log.error("Could not create category/channel " + groupName + "/" + category + "/" + channel, e);
        } finally {
            closeAll(psCreateCategoryAndChannel);
        }
        return successful;
    }

    /**
     * DB table 'group_channels'.
     *
     * @param groupName
     * @param category
     * @param channel - if null, then the whole category is deleted, else only the channel is deleted.
     * @return true: If the deletion is successful.
     *        false: If the deletion is not successful.
     */
    public boolean deleteCategoryAndChannel(String groupName, String category, String channel) {
        boolean successful = false;
        PreparedStatement psDeleteGroupChannels = null;
        PreparedStatement psDeleteMessages = null;

        if (channel == null) {
            channel = "%%";
        }

        try {

            psDeleteGroupChannels = connection.prepareStatement("DELETE FROM group_channels WHERE group_name = ? " +
                    "AND category = ? AND channel_name LIKE ?");
            psDeleteGroupChannels.setString(1, groupName);
            psDeleteGroupChannels.setString(2, category);
            psDeleteGroupChannels.setString(3, channel);

            psDeleteMessages = connection.prepareStatement("DELETE FROM messages WHERE to_group = ? AND " +
                    "to_group_category = ? AND to_group_channel LIKE ?");
            psDeleteMessages.setString(1, groupName);
            psDeleteMessages.setString(2, category);
            psDeleteMessages.setString(3, channel);

            psDeleteGroupChannels.executeUpdate();
            psDeleteMessages.executeUpdate();

            successful = true;
        } catch (Exception e) {
            Log.error("Could not delete category/channel " + groupName + "/" + category + "/" + channel, e);
        } finally {
            closeAll(psDeleteGroupChannels, psDeleteMessages);
        }
        return successful;
    }

    /**
     * DB table 'user_in_group'.
     *
     * @param groupName
     * @param username
     * @return true: If the deletion is successful.
     *        false: If the deletion is not successful.
     */
    public boolean deleteUserFromGroup(String groupName, String username) {
        boolean successful = false;
        PreparedStatement psRemoveUserFromGroup = null;

        try {
            psRemoveUserFromGroup = connection.prepareStatement("DELETE FROM user_in_group WHERE group_id = (SELECT " +
                    "group_id FROM `groups` WHERE group_name = ?) AND user_id = (SELECT user_id FROM users WHERE " +
                    "username = ?)");
            psRemoveUserFromGroup.setString(1, groupName);
            psRemoveUserFromGroup.setString(2, username);

            psRemoveUserFromGroup.executeUpdate();
            successful = true;
        } catch (Exception e) {
            Log.error("Could not remove user: " + username + " from group: " + groupName, e);
        } finally {
            closeAll(psRemoveUserFromGroup);
        }
        return successful;
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

    /**
     * Loads the Database credentials from file database.properties to DatabaseCredentials class and assigns it to
     * the static DatabaseHandler variable databaseCredentials.
     *
     * The file must have the following structure:
     *      database-name ='string'
     *      username      ='string'
     *      password      ='string'
     *      port          ='int'
     */
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
            databaseCredentials = new DatabaseCredentials(databaseName, username, password, port);
        }
    }

    /**
     * Helper class to keep track of the Database credentials.
     */
    private static final class DatabaseCredentials {
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
