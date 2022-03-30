package com.ong.session;

import com.ong.core.Log;
import com.ong.core.User;

/**
 * Singleton class which represents the User session throughout the whole application.
 */
public final class UserSession {
    private static UserSession instance;
    private User user;

    private UserSession(User _user) {
        user = _user;
    }

    public static UserSession getInstance() {
        try {
            if (instance == null) {
                throw new Exception("No user session!");
            }
        } catch (Exception e) {
            Log.error(e);
        }

        return instance;
    }

    public static UserSession getInstance(User _user) {
        if (instance == null) {
            instance = new UserSession(_user);
        }
        return instance;
    }

    public User getUser() {
        return user;
    }

    public void clearSession() {
        user = null;
        instance = null;
    }
}
