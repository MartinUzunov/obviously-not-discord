package com.ong.controllers;

import com.ong.Main;
import com.ong.client.Client;
import com.ong.core.Log;
import com.ong.core.User;
import com.ong.session.DatabaseHandler;
import com.ong.session.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;

import java.net.URL;
import java.util.ResourceBundle;

public class SignInController implements Initializable {
    @FXML
    private TextField loginUsername;

    @FXML
    private PasswordField loginPassword;

    @FXML
    private Button loginButton;

    @FXML
    private Label invalidLabel;

    @FXML
    private Hyperlink loginNeedAnAccount;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        loginButton.setOnAction(event -> login());

        loginNeedAnAccount.setOnAction(event -> {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Views/SignUp.fxml"));

            ScreenController.getInstance().addScreen("Register", fxmlLoader);
            ScreenController.getInstance().activate("Register");
            ScreenController.getInstance().setMinimumDimensions(452, 536);
        });

        loginPassword.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                login();
            }
        });
    }

    private void login() {
        User user = null;
        user = DatabaseHandler.getInstance().signInUser(loginUsername.getText(), loginPassword.getText());

        if (user != null) {
            UserSession.getInstance(user);
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Views/Homepage.fxml"));
            ScreenController.getInstance().addScreen("Homepage", fxmlLoader);
            ScreenController.getInstance().activate("Homepage");
            ScreenController.getInstance().setMaximized(true);
            new Thread(() -> {
                Client.run();
            }).start();
        } else {
            invalidLabel.setVisible(true);
        }
    }
}
