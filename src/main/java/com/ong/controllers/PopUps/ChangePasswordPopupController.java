package com.ong.controllers.PopUps;

import com.ong.controllers.HomepageController;
import com.ong.session.DatabaseHandler;
import com.ong.session.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class ChangePasswordPopupController implements Initializable {

    @FXML
    private HBox closeHBox;

    @FXML
    private Label wrongOldPassword;

    @FXML
    private Label wrongNewPassword;

    @FXML
    private Button savePasswordButton;

    @FXML
    private PasswordField oldPassword;

    @FXML
    private PasswordField newPassword;

    private HomepageController.CloseCallback closeCallback;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        savePasswordButton.setOnAction(event -> {
            if (!DatabaseHandler.getInstance().validateUserPassword(UserSession.getInstance().getUser().getUsername(),
                    oldPassword.getText())) {
                wrongOldPassword.setVisible(true);
            } else if (!validatePassword(newPassword.getText())) {
                wrongOldPassword.setVisible(false);
                wrongNewPassword.setVisible(true);
            } else {
                DatabaseHandler.getInstance().updateUserPassword(UserSession.
                        getInstance().getUser().getUsername(), newPassword.getText());
                closeCallback.close();
            }

        });
    }

    public void setCloseAction(HomepageController.CloseCallback closeCallback) {
        this.closeCallback = closeCallback;
        closeHBox.getChildren().add(new CloseButton(closeCallback));
    }

    private boolean validatePassword(String password) {
        return Pattern.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.])[A-Za-z\\d@$!%*?&.]{8,}$", password);
    }
}
