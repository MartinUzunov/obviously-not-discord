package com.ong.controllers.HomepageElements;

import com.ong.Main;
import com.ong.controllers.HomepageController;
import com.ong.controllers.PrivateChatController;
import com.ong.controllers.ScreenController;
import com.ong.core.Log;
import com.ong.session.DatabaseHandler;
import com.ong.session.UserSession;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

/**
 * FXML File: DirectMessages.fxml
 *
 * Represents the left part of the Border Pane homepage view. The tab contains all opened Direct Messages with users.
 */
public class DirectMessagesController implements Initializable {

    @FXML
    private VBox vBox;

    @FXML
    private ListView DMsListView;

    @FXML
    private ListView friendsTab;

    private HashMap<String, Image> openDms;
    private ObservableList<HomepageController.CustomRow> dmData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        openDms = DatabaseHandler.getInstance().loadAllFriends(UserSession.getInstance().getUser().getUsername(),
                true, "friends");
        setFriendsTab();
        setDMsTab();
        setUserInfoTab();
    }

    private void setFriendsTab() {
        Image friendProfile = new Image("com/ong/Images/friends_tab.png");

        ObservableList<HomepageController.CustomRow> friendsTabData = FXCollections.observableArrayList();
        friendsTabData.addAll(new HomepageController.CustomRow("Friends", friendProfile));

        friendsTab.setItems(friendsTabData);

        friendsTab.setCellFactory((Callback<ListView<HomepageController.CustomRow>, ListCell<HomepageController.CustomRow>>) listView -> new HomepageController.CustomCell());

        friendsTab.getSelectionModel().selectedItemProperty().addListener((ChangeListener<HomepageController.CustomRow>) (observable, oldValue, newValue) -> {
            HomepageController homepageController = (HomepageController) ScreenController.getInstance().getController("Homepage");
            homepageController.fromDM();
        });

        friendsTab.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                friendsTab.getSelectionModel().clearSelection();
            }
        });
    }

    private void setDMsTab() {
        dmData = FXCollections.observableArrayList();

        for (String s : openDms.keySet()) {
            createDirectMessageEntry(s);
        }

        DMsListView.setItems(dmData);

        DMsListView.setCellFactory((Callback<ListView<HomepageController.CustomRow>, ListCell<HomepageController.CustomRow>>) listView -> new HomepageController.CustomCell());

        DMsListView.getSelectionModel().selectedItemProperty().addListener((ChangeListener<HomepageController.CustomRow>) (observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }

            HomepageController homepageController = (HomepageController) ScreenController.getInstance().
                    getController("Homepage");
            homepageController.toDM(newValue.getName());
        });

        DMsListView.setOnMouseEntered(event -> DMsListView.requestFocus());

        DMsListView.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && oldVal != newVal) {
                DMsListView.getSelectionModel().clearSelection();
            }
        });
    }

    public void createDirectMessageEntry(String username) {
        HomepageController.CloseCallback closeCallback = () -> {
            dmData.remove(new HomepageController.CustomRow(username, openDms.get(username)));
            openDms.remove(username);
            ScreenController.getInstance().removeController("DM_" + username);
            ScreenController.getInstance().removeView("DM_" + username);

            DatabaseHandler.getInstance().deleteDirectMessages(UserSession.getInstance().getUser().getUsername(), username);
        };

        dmData.add(new HomepageController.CustomRow(username, openDms.get(username), true, closeCallback));
        createDirectMessage(username);
    }

    public void createDirectMessage(String username) {
        FXMLLoader cent = new FXMLLoader(Main.class.getResource("Views/PrivateChat.fxml"));
        Parent root = null;
        try {
            root = cent.load();
        } catch (Exception e) {
            Log.error(e);
        }
        ScreenController.getInstance().addView("DM_" + username, root);
        PrivateChatController privateChatController = cent.getController();
        ScreenController.getInstance().addController("DM_" + username, privateChatController);
        privateChatController.friendUsername.setText(username);

        privateChatController.loadMessages();

        HomepageController.CloseCallback closeCallback = () -> {
            dmData.remove(new HomepageController.CustomRow(username, openDms.get(username)));
            openDms.remove(username);
            ScreenController.getInstance().removeController("DM_" + username);
            ScreenController.getInstance().removeView("DM_" + username);

            DatabaseHandler.getInstance().deleteDirectMessages(UserSession.getInstance().getUser().getUsername(), username);
        };

        HomepageController.CustomRow newRow = new HomepageController.CustomRow(username,
                DatabaseHandler.getInstance().getUserPhoto(username), true, closeCallback);
        if (!dmData.contains(newRow)) {
            dmData.add(newRow);
        }

        int status = 0;
        status = DatabaseHandler.getInstance().getDMStatus(UserSession.getInstance().getUser().getUsername(), username);
        DatabaseHandler.getInstance().updateDMStatus(UserSession.getInstance().getUser().getUsername(), username, status);
    }

    private void setUserInfoTab() {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Views/UserSection.fxml"));
        Parent parent = null;
        try {
            parent = fxmlLoader.load();
        } catch (Exception e) {
            Log.error(e);
        }

        ScreenController.getInstance().addView("UserSection", parent);
        ScreenController.getInstance().addController("UserSection", fxmlLoader.getController());
        vBox.getChildren().add(parent);
    }
}
