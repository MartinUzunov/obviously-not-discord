package com.ong.controllers;

import com.ong.Main;
import com.ong.controllers.GroupElements.GroupChannelsController;
import com.ong.controllers.GroupElements.GroupMembersController;
import com.ong.controllers.PopUps.CreateGroupPopupController;
import com.ong.core.Group;
import com.ong.core.Log;
import com.ong.session.DatabaseHandler;
import com.ong.session.UserSession;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import javafx.util.Pair;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;

public class GroupsMenuController implements Initializable {

    private final int centerX = 100;
    private final int centerY = 85;
    private final int radius = 25;
    @FXML
    private ScrollPane groupsMenuScrollPane;
    private HashMap<String, Circle> groupNodes;
    private HBox groupContainer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        groupNodes = new HashMap<>();
        groupContainer = new HBox(12);
        groupContainer.setPadding(new Insets(5, 0, 0, 10));
        groupContainer.getStyleClass().add("background");

        addHome();
        addPlus();

        addAllGroups();

        groupsMenuScrollPane.setContent(groupContainer);
        groupsMenuScrollPane.setFitToHeight(true);
        groupsMenuScrollPane.setFitToWidth(true);
    }

    private void addHome() {
        Circle home = new Circle(centerX, centerY, radius);
        home.setFill(new ImagePattern(new Image("com/ong/Images/not_logo.png")));
        Tooltip.install(home, makeTooltip(new Tooltip("Home")));
        createRotationAnimation(home);
        home.setOnMousePressed(event -> {
            HomepageController homepageController = (HomepageController) ScreenController.getInstance().getController("Homepage");
            homepageController.fromGroup();
        });
        groupContainer.getChildren().add(home);

        Separator separator = new Separator(Orientation.VERTICAL);
        separator.getStyleClass().add("separator");
        separator.setPrefSize(10, 80);
        separator.setPadding(new Insets(2, 0, 2, 0));
        groupContainer.getChildren().add(separator);
    }

    private void addAllGroups() {
        LinkedHashMap<String, Image> groupPhotos = null;
        groupPhotos = DatabaseHandler.getInstance().loadAllGroups(UserSession.getInstance().getUser().
                getUsername(), "member");

        for (String s : groupPhotos.keySet()) {
            addGroup(s, groupPhotos.get(s));
        }
    }

    public void addGroup(String name, Image image) {
        final Circle circle = new Circle(centerX, centerY, radius);
        circle.setFill(new ImagePattern(image));
        Tooltip.install(circle, makeTooltip(new Tooltip(name)));
        createRotationAnimation(circle);

        createGroup(name);

        circle.setOnMousePressed(event -> {
            HomepageController homepageController = (HomepageController) ScreenController.getInstance().
                    getController("Homepage");
            homepageController.toGroup(name);
        });

        if (groupContainer.getChildren().size() < 2) {
            groupContainer.getChildren().add(groupContainer.getChildren().size(), circle);
        } else {
            groupContainer.getChildren().add(groupContainer.getChildren().size() - 1, circle);
        }

        groupNodes.put(name, circle);
    }

    public void createGroup(String name) {
        Parent left = null;
        Parent center = null;
        Parent right = null;

        FXMLLoader leftLoader = new FXMLLoader(Main.class.getResource("Views" + "/GroupElements/GroupChannels.fxml"));
        try {
            left = leftLoader.load();
        } catch (Exception e) {
            Log.error(e);
        }
        GroupChannelsController groupChannelsController = leftLoader.getController();

        FXMLLoader rightLoader = new FXMLLoader(Main.class.getResource("Views" + "/GroupElements/GroupMembers.fxml"));
        try {
            right = rightLoader.load();
        } catch (Exception e) {
            Log.error(e);
        }
        GroupMembersController groupMembersController = rightLoader.getController();
        groupMembersController.setGroupName(name);
        groupMembersController.initializeAddMemberButton();
        groupMembersController.loadAllMembers();

        groupChannelsController.init(name);

        Image image = DatabaseHandler.getInstance().getGroupPhoto(name);

        Group group = new Group(name, image, left, center, right);
        group.setChannelsTabController(groupChannelsController);
        group.setMembersTabController(groupMembersController);

        Pair<String, String> pair = groupChannelsController.getDefaultChannel();
        group.createChatTab(pair.getKey(), pair.getValue());

        ScreenController.getInstance().addGroup(group);
    }

    public Circle getGroupNode(String groupName) {
        return groupNodes.get(groupName);
    }

    public void removeGroup(String groupName) {
        Platform.runLater(() -> groupContainer.getChildren().remove(groupNodes.get(groupName)));
    }

    private void addPlus() {
        Circle plus = new Circle(centerX, centerY, radius);
        plus.setFill(new ImagePattern(new Image("com/ong/Images/plus.png")));
        Tooltip.install(plus, makeTooltip(new Tooltip("Create new group")));
        createRotationAnimation(plus);
        plus.setOnMousePressed(event -> {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Views/PopUps/CreateGroupPopup.fxml"));
            Parent parent = null;
            try {
                parent = fxmlLoader.load();
            } catch (Exception e) {
                Log.error(e);
            }
            CreateGroupPopupController createGroupPopupController = fxmlLoader.getController();
            Parent finalParent = parent;
            createGroupPopupController.setCloseAction(() -> {
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
        groupContainer.getChildren().add(plus);
    }

    private void createRotationAnimation(Circle circle) {
        circle.setOnMouseEntered(event -> {
            RotateTransition rt = new RotateTransition(Duration.millis(500), circle);
            rt.setFromAngle(0);
            rt.setToAngle(360);
            rt.setCycleCount(1);
            rt.play();
        });
    }

    private Tooltip makeTooltip(Tooltip tooltip) {
        tooltip.setStyle("-fx-font-size: 16px; fx-font-weight: bold; "
                + "-fx-base: #AE3522; "
                + "-fx-text-fill: white;");
        tooltip.setShowDelay(Duration.millis(0));
        return tooltip;
    }
}
