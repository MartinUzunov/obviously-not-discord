package com.ong.controllers.PopUps;

import com.ong.controllers.GroupElements.GroupChannelsController;
import com.ong.controllers.GroupsMenuController;
import com.ong.controllers.HomepageController;
import com.ong.controllers.ScreenController;
import com.ong.session.DatabaseHandler;
import com.ong.session.UserSession;
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

public class CreateGroupPopupController implements Initializable {

    @FXML
    private HBox closeHBox;

    @FXML
    private Button createButton;

    @FXML
    private TextField groupNameField;

    @FXML
    private Label headerLabel;

    @FXML
    private Label invalidGroupText;

    private HomepageController.CloseCallback closeCallback;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        createButton.setOnMouseClicked(event -> {
            String groupName = groupNameField.getText();
            if (groupName.equals("") || DatabaseHandler.getInstance().checkIfGroupExists(groupName)) {
                invalidGroupText.setVisible(true);
            } else {
                DatabaseHandler.getInstance().createGroup(groupName,
                        UserSession.getInstance().getUser().getUsername());

                GroupsMenuController groupsMenuController = (GroupsMenuController)
                        ScreenController.getInstance().getController("GroupsMenu");
                groupsMenuController.createGroup(groupName);
                groupsMenuController.addGroup(groupName, DatabaseHandler.getInstance().getGroupPhoto(groupName));

                closeCallback.close();
            }
        });
    }

    public void changeGroupName(String oldName) {
        headerLabel.setText("Change Group Name");
        createButton.setText("Change");

        createButton.setOnMouseClicked(event -> {
            GroupChannelsController channelsTabController = (GroupChannelsController)
                    ScreenController.getInstance().getGroup(oldName).getChannelsTabController();
            String newName = groupNameField.getText();

            if (newName != null && !newName.equals("") && !DatabaseHandler.getInstance().checkIfGroupExists(newName)) {
                channelsTabController.changeGroupName(newName);
                closeCallback.close();
            } else {
                invalidGroupText.setVisible(true);
            }
        });
    }

    public void setCloseAction(HomepageController.CloseCallback closeCallback) {
        this.closeCallback = closeCallback;
        closeHBox.getChildren().add(new CloseButton(closeCallback));
    }
}
