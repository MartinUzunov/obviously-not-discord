package com.ong.controllers.PopUps;

import com.ong.controllers.HomepageController;
import com.ong.session.DatabaseHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.ResourceBundle;

public class AddGroupMemberPopupController implements Initializable {

    @FXML
    private HBox closeHBox;

    @FXML
    private Button addButton;

    @FXML
    private TextField usernameField;

    @FXML
    private Label invalidUsername;

    private HomepageController.CloseCallback closeCallback;

    private String groupName;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        addButton.setOnMouseClicked(event -> {
            String username = usernameField.getText();

            if (username == null || username.equals("") || !DatabaseHandler.getInstance().checkIfUserExists(username)) {
                invalidUsername.setVisible(true);
                return;
            }

            DatabaseHandler.getInstance().updateUserToGroupRelationship(username, groupName, "request");
            closeCallback.close();
        });
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setCloseAction(HomepageController.CloseCallback closeCallback) {
        this.closeCallback = closeCallback;
        closeHBox.getChildren().add(new CloseButton(closeCallback));
    }
}
