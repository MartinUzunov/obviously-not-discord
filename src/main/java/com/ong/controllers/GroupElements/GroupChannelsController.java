package com.ong.controllers.GroupElements;

import com.ong.Main;
import com.ong.controllers.GroupsMenuController;
import com.ong.controllers.HomepageController;
import com.ong.controllers.PopUps.ChangePhotoPopupController;
import com.ong.controllers.PopUps.CreateCategoryAndChannelPopupController;
import com.ong.controllers.PopUps.CreateGroupPopupController;
import com.ong.controllers.ScreenController;
import com.ong.controllers.UserSectionController;
import com.ong.core.Group;
import com.ong.core.Log;
import com.ong.session.DatabaseHandler;
import com.ong.session.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.util.Pair;

import java.net.URL;
import java.util.*;

/**
 * FXML File: GroupChannels.fxml
 *
 * Represents the left part of the Border Pane group view. The top part of the view contains the Group name. The middle
 * part contains all group categories and channels. The bottom part contains the user information.
 *
 */
public class GroupChannelsController implements Initializable {

    @FXML
    private VBox mainContainerVBox;

    @FXML
    private HBox groupNameHBox;

    @FXML
    private Label groupNameLabel;

    @FXML
    private TreeView treeView;

    private TreeItem<String> root;
    private HashMap<String, TreeItem> categories;
    private LinkedHashMap<String, ArrayList<String>> channels;
    private Image hashtag;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        categories = new HashMap<>();
        channels = new LinkedHashMap<>();
        hashtag = new Image("com/ong/images/hashtag.png");

        root = new TreeItem<>("AllCategories");
        root.setExpanded(true);

        treeView.setRoot(root);
        treeView.setShowRoot(false);
    }

    /**
     * Initializes all tabs. The group name MUST be set before all tabs are initialized. If the function is not called
     * right after the loading of the FXML, the class functionality will be corrupted.
     *
     * @param groupName
     */
    public void init(String groupName) {
        setGroupNameLabel(groupName);
        initializeGroupNameTab();
        initializeChannelsTab();
        initializeUserInfoTab();
    }

    private void initializeGroupNameTab() {
        setHoverEffect(groupNameHBox);

        ContextMenu groupNameMenu = createGroupActionsMenu();
        groupNameHBox.setOnMouseClicked(event -> {
            // needed for the menu position
            Bounds boundsInScene = groupNameHBox.localToScene(groupNameHBox.getBoundsInLocal());
            groupNameMenu.show(groupNameHBox, boundsInScene.getCenterX() - groupNameHBox.getWidth(),
                    boundsInScene.getCenterY() + groupNameHBox.getHeight());
        });
    }

    private void initializeChannelsTab() {
        treeView.setCellFactory(p -> new CustomTreeCell());

        treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            TreeItem<String> newCell = (TreeItem<String>) newValue;

            if (newCell.getParent().getValue().equals("AllCategories")) { // root
                return;
            }

            // change chat view to the clicked Channel
            HomepageController homepageController = (HomepageController) ScreenController.getInstance().
                    getController("Homepage");
            homepageController.changeChatChannel(groupNameLabel.getText(), newCell.getParent().getValue(),
                    newCell.getValue());
        });

        channels = DatabaseHandler.getInstance().loadAllGroupChannels(groupNameLabel.getText());

        // create all categories and their channels
        for (String category : channels.keySet()) {
            if (!categories.containsKey(category)) {
                createCategory(category);
            }

            for (String channelName : channels.get(category)) {
                addTextChannelToCategory(category, channelName);
            }
        }
    }

    private void initializeUserInfoTab() {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Views/UserSection.fxml"));
        Parent userInfoTab = null;
        try {
            userInfoTab = fxmlLoader.load();
        } catch (Exception e) {
            Log.error(e);
        }

        ScreenController.getInstance().addView("UserSectionGroups", userInfoTab);
        UserSectionController userSectionController = (UserSectionController) ScreenController.getInstance().
                getController("UserSectionGroups");

        ScreenController.getInstance().addController("UserSectionGroups", userSectionController);

        mainContainerVBox.getChildren().add(userInfoTab);
    }

    public boolean createCategory(String name) {
        if (categories.containsKey(name)) {
            return false;
        }
        TreeItem<String> newCategory = new TreeItem<>("Category_" + name);
        root.getChildren().add(newCategory);
        categories.put(name, newCategory);
        return true;
    }

    public boolean addTextChannelToCategory(String category, String channelName) {
        if (!categories.containsKey(category) || channelName.toLowerCase(Locale.ROOT).contains("category_")) {
            return false;
        }

        TreeItem<String> current = categories.get(category);
        current.setExpanded(true);
        current.getChildren().add(new TreeItem<>(channelName));
        categories.put(category, current);
        return true;
    }

    public void changeGroupName(String newGroupName) {
        DatabaseHandler.getInstance().updateGroupName(groupNameLabel.getText(), newGroupName);

        Group group = ScreenController.getInstance().getGroup(groupNameLabel.getText());
        ScreenController.getInstance().removeGroup(groupNameLabel.getText());
        group.changeGroupName(newGroupName);
        ScreenController.getInstance().addGroup(group);

        groupNameLabel.setText(newGroupName);
    }

    /**
     * Forces the current user to leave the current group. Removes the group from the view and the Database.
     */
    public void leaveGroup() {
        ScreenController.getInstance().getGroup(groupNameLabel.getText()).stopGroupSchedulers();
        ScreenController.getInstance().removeGroup(groupNameLabel.getText());
        HomepageController homepageController = (HomepageController)
                ScreenController.getInstance().getController("Homepage");

        DatabaseHandler.getInstance().deleteUserFromGroup(groupNameLabel.getText(),
                UserSession.getInstance().getUser().getUsername());

        homepageController.fromGroup();

        GroupsMenuController groupsMenuController = (GroupsMenuController)
                ScreenController.getInstance().getController("GroupsMenu");
        groupsMenuController.removeGroup(groupNameLabel.getText());
    }

    /**
     * @return the current open chat channel of the group.
     */
    public Pair<String, String> getDefaultChannel() {
        Map.Entry<String, ArrayList<String>> entry = channels.entrySet().iterator().next();
        String category = entry.getKey();
        String channel = entry.getValue().get(0);
        return new Pair<>(category, channel);
    }

    public void setGroupNameLabel(String name) {
        groupNameLabel.setText(name);
    }

    /**
     * Creates ContextMenu which contains all actions that can be performed on a Group.
     *
     * @return ContextMenu
     */
    private ContextMenu createGroupActionsMenu() {
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setStyle("-fx-background-color: #18191c;");

        MenuItem changeGroupName = new MenuItem("Change Group Name");
        changeGroupName.setOnAction(event -> {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Views/PopUps/CreateGroupPopup.fxml"));
            Parent parent = null;

            try {
                parent = fxmlLoader.load();
            } catch (Exception e) {
                Log.error(e);
            }

            CreateGroupPopupController changeGroupNamePopupController = fxmlLoader.getController();
            changeGroupNamePopupController.changeGroupName(groupNameLabel.getText());

            Parent finalParent = parent;
            changeGroupNamePopupController.setCloseAction(() -> {
                HomepageController homepageController = (HomepageController) ScreenController.getInstance().
                        getController("Homepage");

                homepageController.removeFromStackPane(finalParent);
                homepageController.removeBlur();
            });
            HomepageController homepageController = (HomepageController) ScreenController.getInstance().
                    getController("Homepage");

            homepageController.addToStackPane(parent);
            homepageController.blur();
        });

        MenuItem changeGroupPhoto = new MenuItem("Change Group Photo");
        changeGroupPhoto.setOnAction(event -> {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Views/PopUps/ChangePhotoPopup.fxml"));
            Parent parent = null;

            try {
                parent = fxmlLoader.load();
            } catch (Exception e) {
                Log.error(e);
            }

            ChangePhotoPopupController changeGroupNamePopupController = fxmlLoader.getController();
            changeGroupNamePopupController.changeGroupPhoto(groupNameLabel.getText());

            Parent finalParent = parent;
            changeGroupNamePopupController.setCloseAction(() -> {
                HomepageController homepageController = (HomepageController) ScreenController.getInstance().
                        getController("Homepage");

                homepageController.removeFromStackPane(finalParent);
                homepageController.removeBlur();
            });
            HomepageController homepageController = (HomepageController) ScreenController.getInstance().
                    getController("Homepage");

            homepageController.addToStackPane(parent);
            homepageController.blur();
        });

        MenuItem leaveGroup = new MenuItem("Leave Group");
        leaveGroup.setOnAction(event -> leaveGroup());

        addStyleToMenuItems(changeGroupName, changeGroupPhoto, leaveGroup);
        contextMenu.getItems().addAll(changeGroupName, changeGroupPhoto, leaveGroup);

        return contextMenu;
    }

    /**
     * Creates ContextMenu which contains all actions that can be performed on a TreeView cell.
     *
     * @param cellType - used to determine the type of the clicked cell.
     * @return ContextMenu
     */
    private ContextMenu createTreeViewMenu(String cellType) {
        ContextMenu treeViewMenu = new ContextMenu();
        treeViewMenu.setStyle("-fx-background-color: #18191c;");

        // appears on all types of cells
        MenuItem createCategoryAndChannel = new MenuItem("Create Category and Channel");
        createCategoryAndChannel.setOnAction(event -> {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Views/PopUps/CreateCategoryAndChannel" +
                    "Popup.fxml"));
            Parent parent = null;
            try {
                parent = fxmlLoader.load();
            } catch (Exception e) {
                Log.error(e);
            }
            HomepageController homepageController = (HomepageController) ScreenController.getInstance().
                    getController("Homepage");

            CreateCategoryAndChannelPopupController createCategoryAndChannelPopupController = fxmlLoader.getController();
            createCategoryAndChannelPopupController.setGroupName(groupNameLabel.getText());
            Parent finalParent = parent;
            createCategoryAndChannelPopupController.setCloseAction(() -> {
                homepageController.removeFromStackPane(finalParent);
                homepageController.removeBlur();
            });

            homepageController.addToStackPane(parent);
            homepageController.blur();

        });

        switch (cellType) {
            case "category":
                MenuItem deleteCategory = new MenuItem("Delete Category");

                deleteCategory.setOnAction(event -> {
                    TreeItem treeItem = (TreeItem) treeView.getSelectionModel().getSelectedItem();

                    // the categories are prefixed with 'Category_' and because of that the substring of
                    // the category after the 9th symbol is passed as category
                    DatabaseHandler.getInstance().deleteCategoryAndChannel(groupNameLabel.getText(),
                            ((String) treeItem.getValue()).substring(9), null);

                    treeItem.getParent().getChildren().remove(treeItem);
                    categories.remove((String) treeItem.getValue());
                    channels.remove((String) treeItem.getValue());
                });

                addStyleToMenuItems(createCategoryAndChannel, deleteCategory);
                treeViewMenu.getItems().addAll(createCategoryAndChannel, deleteCategory);
                break;
            case "channel":
                MenuItem deleteChannel = new MenuItem("Delete Channel");
                deleteChannel.setOnAction(event -> {
                    TreeItem treeItem = (TreeItem) treeView.getSelectionModel().getSelectedItem();
                    // the categories are prefixed with 'Category_' and because of that the substring of
                    // the category after the 9th symbol is passed as category
                    DatabaseHandler.getInstance().deleteCategoryAndChannel(groupNameLabel.getText(),
                            ((String) treeItem.getParent().getValue()).substring(9), (String) treeItem.getValue());

                    treeItem.getParent().getChildren().remove(treeItem);
                    categories.remove((String) treeItem.getValue());
                    channels.remove((String) treeItem.getValue());
                });

                addStyleToMenuItems(createCategoryAndChannel, deleteChannel);
                treeViewMenu.getItems().addAll(createCategoryAndChannel, deleteChannel);
                break;
            case "empty":
                addStyleToMenuItems(createCategoryAndChannel);
                treeViewMenu.getItems().addAll(createCategoryAndChannel);
                break;
        }
        return treeViewMenu;
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

    /**
     * Changes the Background color of a HBox on a mouse entered and mouse exited.
     *
     * @param hBox
     */
    private void setHoverEffect(HBox hBox) {
        hBox.setOnMouseEntered(event -> hBox.setBackground(new Background(new BackgroundFill(Color.web("#393c42"), CornerRadii.EMPTY,
                Insets.EMPTY))));

        hBox.setOnMouseExited(event -> hBox.setBackground(null));
    }

    private Tooltip makeTooltip(Tooltip tooltip) {
        tooltip.setStyle("-fx-font-size: 16px; fx-font-weight: bold; "
                + "-fx-base: #AE3522; "
                + "-fx-text-fill: white;");
        tooltip.setShowDelay(Duration.seconds(1.5));
        return tooltip;
    }

    /**
     * Represents a single TreeView cell of type String. Responsible for updating the text, applying the graphic and
     * adding Context Menus and Tooltips to the cell.
     */
    private class CustomTreeCell extends TextFieldTreeCell<String> {
        final Tooltip tooltip;

        public CustomTreeCell() {
            tooltip = makeTooltip(new Tooltip());
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            ContextMenu menu;
            if (empty) {
                setText(null);
                setTooltip(null);
                setGraphic(null);
                menu = createTreeViewMenu("empty");
                setContextMenu(menu);

            } else if (!item.contains("Category_")) {
                setText(item);
                tooltip.setText(item);
                setTooltip(tooltip);
                ImageView icon = new ImageView(hashtag);
                icon.setFitWidth(20);
                icon.setFitHeight(20);
                setGraphic(icon);
                menu = createTreeViewMenu("channel");
                setContextMenu(menu);
            } else {
                item = item.substring(9);
                setText(item);
                tooltip.setText(item);
                setTooltip(tooltip);
                setGraphic(null);
                menu = createTreeViewMenu("category");
                setContextMenu(menu);
            }
        }
    }
}