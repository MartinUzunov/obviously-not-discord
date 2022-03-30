package com.ong.controllers.HomepageElements;

import com.ong.Main;
import com.ong.client.Client;
import com.ong.controllers.HomepageController;
import com.ong.controllers.ScreenController;
import com.ong.core.Log;
import com.ong.core.Message;
import com.ong.session.DatabaseHandler;
import com.ong.session.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.util.Callback;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * FXML File: AllFriends.fxml
 *
 * Represents the right part of the Border Pane homepage view. The top part of the view contains a List View which
 * contains all friends of the user. The Bottom part contains a button which when pressed, changes the middle of the
 * homepage view to the Requests tab.
 */
public class AllFriendsController implements Initializable {

    @FXML
    private ListView allFriendsListView;

    @FXML
    private Button friendRequestsButton;

    private HashMap<String, Image> allUsers;
    private ObservableList<HomepageController.CustomRow> allFriendsData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadAllFriends();
        requestAllFriendsScheduler(30);
        initializeListView();
    }

    public void addFriend(String username) {
        allFriendsData.add(new HomepageController.CustomRow(username, DatabaseHandler.getInstance().getUserPhoto(username)));
    }

    public HashMap<String, Image> getAllUsers() {
        return allUsers;
    }

    public Image getUserImage(String username) {
        return allUsers.get(username);
    }

    private void loadAllFriends() {
        allFriendsData = FXCollections.observableArrayList();
        allUsers = DatabaseHandler.getInstance().loadAllFriends(UserSession.getInstance().getUser().getUsername(),
                    false, "friends");

        for (Map.Entry<String, Image> entry : allUsers.entrySet()) {
            allFriendsData.add(new HomepageController.CustomRow(entry.getKey(), entry.getValue()));
        }
        allFriendsListView.setItems(allFriendsData);
    }

    /**
     * Requests all user friends from the DB every N seconds.
     */
    private void requestAllFriendsScheduler(int N){
        Runnable runnable = () -> {
            if (ScreenController.getInstance().running) {
                loadAllFriends();
            }
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(runnable, 0, N, TimeUnit.SECONDS);
    }

    private void initializeListView(){
        allFriendsListView.setItems(allFriendsData);

        allFriendsListView.setCellFactory((Callback<ListView<HomepageController.CustomRow>, ListCell<HomepageController.CustomRow>>) listView -> {
            HomepageController.CustomCell customCell = new HomepageController.CustomCell();

            customCell.setOnMousePressed(event -> {
                HomepageController homepageController = (HomepageController)
                        ScreenController.getInstance().getController("Homepage");
                if (customCell.getItem() != null) {
                    homepageController.toDM(customCell.getItem().getName());
                }
            });
            return customCell;
        });

        allFriendsListView.setOnMouseEntered(event -> allFriendsListView.requestFocus());

        friendRequestsButton.setOnAction(event -> {
            HomepageController homepageController = (HomepageController)
                    ScreenController.getInstance().getController("Homepage");

            Parent view = ScreenController.getInstance().getView("FriendRequests");

            if (view == null) {
                FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Views/HomepageElements/" +
                        "FriendRequests.fxml"));
                Parent parent = null;
                try {
                    parent = fxmlLoader.load();
                } catch (Exception e) {
                    Log.error(e);
                }
                ScreenController.getInstance().addView("FriendRequests", parent);
                homepageController.getBorderPane().setCenter(parent);
            } else {
                homepageController.getBorderPane().setCenter(view);
            }
        });
    }
}
