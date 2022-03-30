package com.ong.controllers.PopUps;

import com.ong.controllers.GroupElements.GroupChannelsController;
import com.ong.controllers.HomepageController;
import com.ong.controllers.ScreenController;
import com.ong.core.Group;
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

public class CreateCategoryAndChannelPopupController implements Initializable {

    @FXML
    private HBox closeHBox;

    @FXML
    private Button submitButton;

    @FXML
    private Label invalidCategory;

    @FXML
    private Label invalidChannel;

    @FXML
    private TextField categoryTextField;

    @FXML
    private TextField channelTextField;

    private HomepageController.CloseCallback closeCallback;

    private String groupName;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        submitButton.setOnAction(event -> {
            String categoryName = categoryTextField.getText();
            String channelName = channelTextField.getText();
            Group group = ScreenController.getInstance().getGroup(groupName);
            GroupChannelsController groupChannelsController =
                    (GroupChannelsController) group.getChannelsTabController();

            if (channelName.equals("")) {
                channelName = "general";
            }

            if (categoryName.equals("")) {
                invalidCategory.setVisible(true);
                return;
            }

            if (DatabaseHandler.getInstance().checkIfChannelExists(groupName, categoryName, channelName)) {
                invalidChannel.setVisible(true);
                return;
            }

            if (DatabaseHandler.getInstance().checkIfCategoryExists(groupName, categoryName)) {
                groupChannelsController.addTextChannelToCategory(categoryName, channelName);
            } else {
                if (groupChannelsController.createCategory(categoryName)) {
                    groupChannelsController.addTextChannelToCategory(categoryName, channelName);
                }
            }
            group.createChatTab(categoryName, channelName);
            DatabaseHandler.getInstance().createCategoryAndChannel(groupName, categoryName, channelName);
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
