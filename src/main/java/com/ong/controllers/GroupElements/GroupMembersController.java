package com.ong.controllers.GroupElements;

import com.ong.Main;
import com.ong.client.Client;
import com.ong.controllers.HomepageController;
import com.ong.controllers.PopUps.AddGroupMemberPopupController;
import com.ong.controllers.ScreenController;
import com.ong.core.Log;
import com.ong.core.Message;
import com.ong.core.User;
import com.ong.session.DatabaseHandler;
import com.ong.session.UserSession;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.util.Callback;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * FXML File: GroupMembers.fxml
 *
 * Represents the right part of the Border Pane group view. The top part of the view contains a List View which
 * contains information about Online and Offline users. The Bottom part contains a button for adding Group Members.
 * Only users with role = 'admin' can add members.
 */
public class GroupMembersController implements Initializable {

    @FXML
    private ListView membersListView;

    @FXML
    private Button addMemberButton;

    private ObservableList<HomepageController.CustomRow> data;
    private HashMap<String, Image> allMembers;
    private String groupName;
    private boolean running = true;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        requestOnlineScheduler(30);
        initializeListView();
    }

    public void initializeAddMemberButton() {
        if (DatabaseHandler.getInstance().getUserInGroupRole(groupName,
                UserSession.getInstance().getUser().getUsername()).equals("admin")) {

            addMemberButton.setOnAction(event -> {
                FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Views/PopUps/AddGroupMemberPopup.fxml"));
                Parent parent = null;
                try {
                    parent = fxmlLoader.load();
                } catch (Exception e) {
                    Log.error(e);
                }
                AddGroupMemberPopupController addGroupMemberPopupController = fxmlLoader.getController();
                addGroupMemberPopupController.setGroupName(groupName);
                HomepageController homepageController = (HomepageController) ScreenController.getInstance().
                        getController("Homepage");

                Parent finalParent = parent;
                addGroupMemberPopupController.setCloseAction(() -> {
                    homepageController.removeFromStackPane(finalParent);
                    homepageController.removeBlur();
                });

                homepageController.blur();
                homepageController.addToStackPane(parent);
            });
        } else {
            addMemberButton.setBlendMode(BlendMode.DIFFERENCE);
            Tooltip tooltip = new Tooltip("Only group admin can add members!");
            tooltip.setStyle("-fx-font-size: 22px; fx-font-weight: bold; "
                    + "-fx-base: #AE3522; "
                    + "-fx-text-fill: white;");
            tooltip.setShowDelay(Duration.seconds(0));
            Tooltip.install(addMemberButton, tooltip);
        }
    }

    /**
     * Loads all members of the group and distributes them to Online and Offline lists.
     * @param message
     */
    public void updateOnlineMembers(Message message) {
        loadAllMembers();
        data.clear();

        // add online users
        data.add(new HomepageController.CustomRow("Online:", null));
        ArrayList<String> onlineUsersAsString = new ArrayList<>();
        if (message != null) {
            ArrayList<User> onlineUsers = (ArrayList<User>) message.getFreeObject();
            for (User user : onlineUsers) {
                onlineUsersAsString.add(user.getUsername());
                data.add(new HomepageController.CustomRow(user.getUsername(),
                        DatabaseHandler.getInstance().getUserPhoto(user.getUsername())));

            }
        }

        // add offline users
        data.add(new HomepageController.CustomRow("Offline:", null));
        for (String user : allMembers.keySet()) {
            if (!onlineUsersAsString.contains(user)) {
                data.add(new HomepageController.CustomRow(user, DatabaseHandler.getInstance().getUserPhoto(user)));
            }
        }
    }

    public void loadAllMembers() {
        allMembers = DatabaseHandler.getInstance().loadAllGroupMembers(groupName);
        if (allMembers.size() > 0 && !allMembers.containsKey(UserSession.getInstance().getUser().getUsername()) && groupName != null) {
            GroupChannelsController channelsTabController = (GroupChannelsController)
                    ScreenController.getInstance().getGroup(groupName).getChannelsTabController();
            channelsTabController.leaveGroup();
        }
    }

    public void setGroupName(String name) {
        this.groupName = name;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    private void initializeListView() {
        data = FXCollections.observableArrayList();
        membersListView.setItems(data);

        membersListView.setCellFactory((Callback<ListView<HomepageController.CustomRow>, ListCell<HomepageController.CustomRow>>) listView -> {
            ContextMenu contextMenu = createMembersMenu();
            return new HomepageController.CustomCell(contextMenu);
        });

        membersListView.getSelectionModel().selectedItemProperty().addListener((ChangeListener<HomepageController.CustomRow>) (observable, oldValue, newValue) -> {
            if (newValue == null || newValue.getName().equals("Online:") || newValue.getName().equals("Offline:")) {
                return;
            }
        });
    }

    /**
     * Creates ContextMenu which contains all actions that can be performed on a Group member by the Admin.
     *
     * @return ContextMenu
     */
    private ContextMenu createMembersMenu() {
        ContextMenu membersMenu = new ContextMenu();
        membersMenu.setStyle("-fx-background-color: #18191c;");

        MenuItem kickUser = new MenuItem("Kick User");
        kickUser.setOnAction(event -> {
            HomepageController.CustomRow rowToRemove = (HomepageController.CustomRow)
                    membersListView.getSelectionModel().getSelectedItem();

            data.remove(new HomepageController.CustomRow(rowToRemove.getName(), null));
            DatabaseHandler.getInstance().deleteUserFromGroup(groupName, rowToRemove.getName());
        });
        addStyleToMenuItems(kickUser);
        membersMenu.getItems().add(kickUser);

        return membersMenu;
    }

    /**
     * Requests the online users from the Server every N seconds.
     */
    private void requestOnlineScheduler(int N) {
        final Message[] message = new Message[1];
        Runnable runnable = null;
        Object shutdownExecutor = null;

        try {
            Object finalShutdownExecutor = shutdownExecutor;
            runnable = () -> {
                if (!running) { // if the user is no longer member of the group, stop the executor
                    ScheduledExecutorService exec = (ScheduledExecutorService) finalShutdownExecutor;
                    exec.shutdown();
                }

                updateOnlineMembers(null);
                message[0] = new Message(null, null, null, groupName, null, null);
                message[0].setType(Message.MessageType.REQUEST_MEMBERS_ONLINE);
                Client.clientSender.addItem(message[0]);
            };
        } catch (Exception e) {
            Log.error(e);
        }

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(runnable, 0, N, TimeUnit.SECONDS);

        shutdownExecutor = executor;
    }

    /**
     * Applies style to the passed MenuItems.
     *
     * @param menuItem [0...*]
     */
    private void addStyleToMenuItems(MenuItem... menuItem) {
        for (MenuItem mi : menuItem) {
            mi.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        }
    }
}
