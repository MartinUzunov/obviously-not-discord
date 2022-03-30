package com.ong.controllers;

import com.ong.Main;
import com.ong.controllers.HomepageElements.DirectMessagesController;
import com.ong.core.Group;
import com.ong.core.Log;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Pair;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class HomepageController implements Initializable {

    @FXML
    private BorderPane borderPane;

    @FXML
    private StackPane stackPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Parent parent = null;

        FXMLLoader allFriends = new FXMLLoader(Main.class.getResource("Views/HomepageElements/AllFriends.fxml"));
        try {
            parent = allFriends.load();
        } catch (Exception e) {
            Log.error(e);
        }
        borderPane.setRight(parent);
        ScreenController.getInstance().addView("AllFriends", parent);
        ScreenController.getInstance().addController("AllFriends", allFriends.getController());


        FXMLLoader directMessages = new FXMLLoader(Main.class.getResource("Views/HomepageElements/DirectMessages.fxml"));
        try {
            parent = directMessages.load();
        } catch (Exception e) {
            Log.error(e);
        }
        borderPane.setLeft(parent);
        ScreenController.getInstance().addView("DirectMessages", parent);
        DirectMessagesController directMessagesController = directMessages.getController();
        ScreenController.getInstance().addController("DirectMessages", directMessagesController);


        FXMLLoader groupsMenu = new FXMLLoader(Main.class.getResource("Views/HomepageElements/GroupsMenu.fxml"));
        try {
            parent = groupsMenu.load();
        } catch (Exception e) {
            Log.error(e);
        }
        borderPane.setTop(parent);
        ScreenController.getInstance().addView("GroupsMenu", parent);
        ScreenController.getInstance().addController("GroupsMenu", groupsMenu.getController());


        FXMLLoader onlineFriends = new FXMLLoader(Main.class.getResource("Views/HomepageElements/OnlineFriends.fxml"));
        try {
            parent = onlineFriends.load();
        } catch (Exception e) {
            Log.error(e);
        }
        borderPane.setCenter(parent);
        ScreenController.getInstance().addView("OnlineFriends", parent);
        ScreenController.getInstance().addController("OnlineFriends", onlineFriends.getController());
    }

    public void toDM(String name) {

        Parent root = ScreenController.getInstance().getView("DM_" + name);
        if (root == null) {
            DirectMessagesController directMessagesController = (DirectMessagesController)
                    ScreenController.getInstance().getController("DirectMessages");
            directMessagesController.createDirectMessage(name);
            root = ScreenController.getInstance().getView("DM_" + name);
        }
        PrivateChatController privateChatController = (PrivateChatController) ScreenController.getInstance().getController("DM_" + name);
        privateChatController.friendUsername.setText(name);

        Parent finalRoot = root;
        Platform.runLater(() -> {
            borderPane.setCenter(null);
            borderPane.setRight(null);
            borderPane.setCenter(finalRoot);
        });

    }

    public void fromDM() {
        borderPane.setCenter(ScreenController.getInstance().getView(
                "OnlineFriends"));

        borderPane.setRight(ScreenController.getInstance().getView(
                "AllFriends"));
    }

    public void toGroup(String name) {
        Group group = ScreenController.getInstance().getGroup(name);
        Parent left = group.getChannelsTab();
        Parent center = group.getCurrentChatTab();
        Parent right = group.getMembersTab();

        borderPane.setLeft(left);
        borderPane.setCenter(center);
        borderPane.setRight(right);
    }

    public void fromGroup() {
        Platform.runLater(() -> {
            borderPane.setLeft(ScreenController.getInstance().getView(
                    "DirectMessages"));

            borderPane.setCenter(ScreenController.getInstance().getView(
                    "OnlineFriends"));

            borderPane.setRight(ScreenController.getInstance().getView(
                    "AllFriends"));
        });

    }

    public void changeChatChannel(String groupName, String category, String channelName) {
        Pair<String, String> p = new Pair<>(category.substring(9), channelName);
        Group group = ScreenController.getInstance().getGroup(groupName);
        Parent root = group.getChatTabs().get(p).getKey();
        group.setCurrentChatTab(root);
        borderPane.setCenter(root);
    }

    public void blur() {
        ColorAdjust adj = new ColorAdjust(0, -0.9, -0.5, 0);
        GaussianBlur blur = new GaussianBlur(10);
        adj.setInput(blur);
        borderPane.setEffect(adj);
        borderPane.setDisable(true);
    }

    public void removeBlur() {
        borderPane.setEffect(null);
        borderPane.setDisable(false);
    }

    public void addToStackPane(Parent parent) {
        stackPane.getChildren().add(parent);
    }

    public void removeFromStackPane(Parent parent) {
        stackPane.getChildren().remove(parent);
    }

    public BorderPane getBorderPane() {
        return borderPane;
    }

    public interface CloseCallback {
        void close();
    }

    public static class CustomRow {
        private final String name;
        private final Image image;
        private final boolean closeButton;
        private CloseCallback closeCallback;

        public CustomRow(String name, Image image) {
            super();
            this.name = name;
            this.image = image;
            this.closeButton = false;
        }

        public CustomRow(String name, Image image, boolean closeButton, CloseCallback closeCallback) {
            super();
            this.name = name;
            this.image = image;
            this.closeButton = closeButton;
            this.closeCallback = closeCallback;
        }

        public String getName() {
            return name;
        }

        public Image getImage() {
            return image;
        }

        @Override
        public String toString() {
            return "CustomRow{" +
                    "name='" + name + '\'' + '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CustomRow customRow = (CustomRow) o;
            return Objects.equals(name, customRow.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, image);
        }
    }

    public static class CustomCell extends ListCell<HomepageController.CustomRow> {
        private final Label name;
        private final Circle circle;
        private final ContextMenu contextMenu;
        private HBox content;

        public CustomCell() {
            super();
            content = new HBox(10);
            name = new Label();
            name.getStyleClass().add("text");
            circle = new Circle(100, 85, 15);
            contextMenu = null;
        }

        public CustomCell(ContextMenu contextMenu) {
            super();
            content = new HBox(10);
            name = new Label();
            name.getStyleClass().add("text");
            circle = new Circle(100, 85, 15);
            this.contextMenu = contextMenu;
        }

        @Override
        protected void updateItem(HomepageController.CustomRow item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null && item.getName() != null && !item.getName().equals("")) {
                setContextMenu(contextMenu);
            }
            Platform.runLater(() -> {
                if (item == null) {
                    setGraphic(null);
                } else if (item.name.equals("Online:") || item.name.equals("Offline:")) {
                    content = new HBox(10);
                    name.setText(item.name);
                    name.getStyleClass().clear();
                    name.setTextFill(Color.web("#8e9297"));
                    name.getStyleClass().add("-fx-font-size: 50px");
                    content.getChildren().add(name);

                    content.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(content);
                    setDisable(true);
                    setFocusTraversable(false);
                } else if (item.closeButton) {
                    content = new HBox(10);
                    name.setText(item.name);
                    circle.setFill(new ImagePattern(item.getImage()));
                    Rectangle rectangle = new Rectangle(15, 15);
                    rectangle.setFill(new ImagePattern(new Image("com/ong/images/x.png")));

                    rectangle.setOnMouseClicked(event -> item.closeCallback.close());

                    rectangle.setOnMouseEntered(event -> rectangle.setBlendMode(BlendMode.DIFFERENCE));

                    rectangle.setOnMouseExited(event -> rectangle.setBlendMode(null));

                    HBox left = new HBox(10);
                    left.setAlignment(Pos.CENTER_LEFT);
                    left.getChildren().add(circle);
                    left.getChildren().add(name);

                    HBox right = new HBox();
                    right.setAlignment(Pos.CENTER_RIGHT);
                    right.getChildren().add(rectangle);
                    HBox.setHgrow(right, Priority.ALWAYS);
                    right.setVisible(false);

                    content.getChildren().add(left);
                    content.getChildren().add(right);
                    content.setAlignment(Pos.CENTER_LEFT);

                    content.setOnMouseEntered(event -> right.setVisible(true));

                    content.setOnMouseExited(event -> right.setVisible(false));

                    setGraphic(content);
                    setContextMenu(contextMenu);
                } else {
                    content = new HBox(10);
                    name.setText(item.name);
                    circle.setFill(new ImagePattern(item.getImage()));
                    content.getChildren().add(circle);
                    content.getChildren().add(name);

                    content.setAlignment(Pos.CENTER_LEFT);

                    setGraphic(content);
                }
            });


            setStyle("-fx-control-inner-background: transparent");
        }

    }
}
