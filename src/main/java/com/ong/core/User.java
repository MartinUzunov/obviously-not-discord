package com.ong.core;

import javafx.scene.image.Image;

import java.io.Serializable;

/**
 * Represents a single user in the application. The class implements Serializable, so it can be passed between the
 * Client and the Server as an object (using Object(Input/Output)Stream).
 */
public class User implements Serializable {
    private final String username;
    private final String email;
    private final String password;
    private final String birthDate;
    private String profilePhotoURL;
    private transient Image image; // marked as transient, because it can't be serialized.

    public User(String username, String email, String password, String birthDate) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.birthDate = birthDate;
        profilePhotoURL = "./src/main/resources/com/ong/images/no_profile.png";
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getProfilePhotoURL() {
        return profilePhotoURL;
    }

    public Image getImage() {
        return image;
    }

    public void setProfilePhotoURL(String profilePhotoURL) {
        this.profilePhotoURL = profilePhotoURL;
    }

    public void setImage(Image image) {
        this.image = image;
    }
}
