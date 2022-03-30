package com.ong.core;

import com.ong.Main;
import com.ong.controllers.GroupElements.GroupChannelsController;
import com.ong.controllers.GroupElements.GroupChatController;
import com.ong.controllers.GroupElements.GroupMembersController;
import com.ong.session.DatabaseHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Represents a Group Chat in the application.
 *
 * The class contains the Views and their respective Controllers needed for every single group:
 *      Left part - ChannelsTab view which represents the window where all Group categories and Group channels are
 *                  displayed.
 *      Center part - ChatTab view which displays the Chat window for every channel.
 *      Right part - MembersTab view which represents the window where all Group members are displayed.
 *
 * When a Group is displayed, all of these Views(parts) are set as Root in the appropriate Border Pane place.
 */
public class Group {
    private final Parent channelsTab; // left
    private final Parent membersTab; // right
    /**
     * Keeps track of all Chat views. Populated in method createChatTab.
     *
     * All Chat views must be created on Group creation(before any operations are performed with the group).
     *
     * Key = <Category name, Channel name>, Value = <FXML View, FXML Controller>
     */
    private final HashMap<Pair<String, String>, Pair<Parent, GroupChatController>> chatTabs;
    private String name;
    private Image photo;
    private Object channelsTabController; // left controller
    private Parent currentChatTab; // center
    private Object chatTabController; // center controller
    private Object membersTabController; // right controller

    /**
     *
     * @param name
     * @param photo
     * @param channelsTab - FXML view representing the left view of the Border Pane (GroupChannels.fxml)
     * @param currentChatTab - FXML view representing the center view of the Border Pane (GroupChat.fxml)
     * @param membersTab - FXML view representing the right view of the Border Pane (GroupMembers.fxml)
     */
    public Group(String name, Image photo, Parent channelsTab, Parent currentChatTab, Parent membersTab) {
        this.name = name;
        this.photo = photo;
        this.chatTabs = new HashMap<>();
        this.channelsTab = channelsTab;
        this.currentChatTab = currentChatTab;
        this.membersTab = membersTab;

        // loading all Categories, Channels and creating a Chat Tab for every channel.
        LinkedHashMap<String, ArrayList<String>> channels =
                DatabaseHandler.getInstance().loadAllGroupChannels(this.name);
        for (String category : channels.keySet()) {
            for (String channel : channels.get(category)) {
                Pair<String, String> p = new Pair<>(category, channel);
                if (!chatTabs.containsKey(p)) {
                    createChatTab(category, channel);
                }
            }
        }
    }

    /**
     * Creates a single chat tab, based on the provided category and channel and adds it to the chatTabs HashMap.
     * Responsible for calling the loadMessages method on groupChatController, AFTER populating the controller
     * variables for channel name, category and channel(the messages CANNOT be populated if these variables are null).
     *
     * @param category
     * @param channel
     * @return FXML view of the created Chat Tab.
     */
    public Parent createChatTab(String category, String channel) {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(
                "Views" + "/GroupElements/GroupChat.fxml"));
        Parent root = null;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            Log.error(e);
        }
        GroupChatController groupChatController = fxmlLoader.getController();

        groupChatController.channelNameLabel.setText(channel);
        groupChatController.group = name;
        groupChatController.category = category;
        groupChatController.loadMessages();

        currentChatTab = root;
        this.chatTabController = groupChatController;

        chatTabs.put(new Pair<>(category, channel), new Pair<>(root, groupChatController));

        return root;
    }

    /**
     * Changes the group name in all group tabs controllers.
     *
     * @param groupName
     */
    public void changeGroupName(String groupName) {
        this.name = groupName;
        GroupChatController chatTabController = (GroupChatController) this.chatTabController;
        chatTabController.group = groupName;
        GroupMembersController membersTabController = (GroupMembersController) this.membersTabController;
        membersTabController.setGroupName(groupName);
        GroupChannelsController channelsTabController = (GroupChannelsController) this.channelsTabController;
        channelsTabController.setGroupNameLabel(groupName);
    }

    /**
     * Shutdowns all Schedulers(function that is called every N seconds) that are running. Must be called after the
     * group deletion.
     */
    public void stopGroupSchedulers() {
        GroupMembersController membersTabController = (GroupMembersController) this.membersTabController;
        membersTabController.setRunning(false);
    }

    public String getName() {
        return name;
    }

    public Image getPhoto() {
        return photo;
    }

    public Parent getChannelsTab() {
        return channelsTab;
    }

    public Object getChannelsTabController() {
        return channelsTabController;
    }

    public Parent getCurrentChatTab() {
        return currentChatTab;
    }

    public Parent getMembersTab() {
        return membersTab;
    }

    public Object getMembersTabController() {
        return membersTabController;
    }

    public HashMap<Pair<String, String>, Pair<Parent, GroupChatController>> getChatTabs() {
        return chatTabs;
    }

    public void setPhoto(Image photo) {
        this.photo = photo;
    }

    public void setCurrentChatTab(Parent currentChatTab) {
        this.currentChatTab = currentChatTab;
    }

    public void setMembersTabController(Object membersTabController) {
        this.membersTabController = membersTabController;
    }

    public void setChannelsTabController(Object channelsTabController) {
        this.channelsTabController = channelsTabController;
    }
}
