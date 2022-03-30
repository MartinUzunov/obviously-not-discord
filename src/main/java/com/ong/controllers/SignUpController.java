package com.ong.controllers;

import com.ong.Main;
import com.ong.client.Client;
import com.ong.core.User;
import com.ong.session.DatabaseHandler;
import com.ong.session.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class SignUpController implements Initializable {

    @FXML
    private TextField registerEmail;

    @FXML
    private Label invalidEmail;

    @FXML
    private TextField registerUsername;

    @FXML
    private Label invalidUsername;

    @FXML
    private PasswordField registerPassword;

    @FXML
    private Label invalidPassword;

    @FXML
    private DatePicker registerDate;

    @FXML
    private Label invalidDate;

    @FXML
    private Button registerButton;

    @FXML
    private Hyperlink alreadyHaveAccount;

    private boolean validateUsername(String username) {
        return Pattern.matches("^(?=[a-zA-Z0-9._\\-]{3,32}$)(?!.*[_.]{2})[^_.].*[^_.\\-]$", username);
    }

    private boolean validateEmail(String email) {
        return Pattern.matches("^[\\w.]{1,64}@[a-zA-z]{1,255}\\.[a-zA-z]{2,8}$", email);
    }

    private boolean validatePassword(String password) {
        return Pattern.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.])[A-Za-z\\d@$!%*?&.]{8,}$", password);
    }

    private String convertDate(LocalDate value) {
        String result;
        try {
            result = String.valueOf(value.getDayOfMonth()) + '-' + value.getMonthValue() + '-' +
                    value.getYear();
        } catch (Exception e) {
            return "";
        }
        return result;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        registerButton.setOnAction(event -> register());

        alreadyHaveAccount.setOnAction(event -> {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Views/SignIn.fxml"));
            ScreenController.getInstance().addScreen("Login", fxmlLoader);
            ScreenController.getInstance().activate("Login");
            ScreenController.getInstance().setMinimumDimensions(645, 440);
        });
    }

    private void register() {
        User user = null;
        String date = convertDate(registerDate.getValue());

        boolean abort = false;

        if (date.equals("")) {
            invalidDate.setVisible(true);
            abort = true;
        } else {
            invalidDate.setVisible(false);
        }

        if (!validateUsername(registerUsername.getText())) {
            invalidUsername.setVisible(true);
            abort = true;
        } else {
            invalidUsername.setVisible(false);
        }

        if (!validateEmail(registerEmail.getText())) {
            invalidEmail.setVisible(true);
            abort = true;
        } else {
            invalidEmail.setVisible(false);
        }

        if (!validatePassword(registerPassword.getText())) {
            invalidPassword.setVisible(true);
            abort = true;
        } else {
            invalidPassword.setVisible(false);
        }


        if (!abort) {
            user = new User(registerUsername.getText(), registerEmail.getText(), registerPassword.getText(), date);
            DatabaseHandler.getInstance().signUpUser(user);
            DatabaseHandler.getInstance().updateUserProfilePhoto(user.getUsername(), user.getProfilePhotoURL());
            user.setImage(new Image("com/ong/Images/no_profile.png"));

            UserSession.getInstance(user);
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Views/Homepage.fxml"));
            ScreenController.getInstance().addScreen("Homepage", fxmlLoader);
            ScreenController.getInstance().activate("Homepage");
            ScreenController.getInstance().setMaximized(true);
            new Thread(() -> {
                Client.run();
            }).start();
        }
    }
}
