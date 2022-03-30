package com.ong.controllers.HomepageElements;

import com.ong.controllers.GroupsMenuController;
import com.ong.controllers.ScreenController;
import com.ong.session.DatabaseHandler;
import com.ong.session.UserSession;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Callback;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * FXML File: FriendsRequests.fxml
 *
 * Represents the middle part of the Border Pane homepage view after the requests button is clicked.
 */
public class FriendRequestsController implements Initializable {

    private final Background hoverBackground = new Background(new BackgroundFill(Color.web("#393c42"), CornerRadii.EMPTY, Insets.EMPTY));

    @FXML
    private TextField addFriendField;

    @FXML
    private ListView requests;

    @FXML
    private Label infoLabel;

    @FXML
    private Button sendFriendRequestButton;

    private ObservableList<CustomRow> requestsData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        requestsData = FXCollections.observableArrayList();
        requests.setItems(requestsData);
        requests.setCellFactory((Callback<ListView<CustomRow>, ListCell<CustomRow>>) listView -> new CustomCell());

        Runnable runnable = () -> loadAllRequests();

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(runnable, 0, 5, TimeUnit.SECONDS);


        sendFriendRequestButton.setOnMouseClicked(event -> {
            String username = addFriendField.getText();
            if (!username.equals("")) {
                if (!DatabaseHandler.getInstance().checkIfUserExists(username)) {
                    infoLabel.setText("Invalid username!");
                    infoLabel.setTextFill(Color.RED);
                } else {
                    DatabaseHandler.getInstance().updateUserRelationship(UserSession.getInstance().getUser().getUsername(),
                            username, "request");
                    infoLabel.setText("Friend request sent!");
                    infoLabel.setTextFill(Color.GREEN);
                }
                infoLabel.setVisible(true);
            }
        });

        // don't change color if out of focus
        addFriendField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                addFriendField.getStyleClass().add("fx-text-fill: white;");
            }
        });
    }

    private void loadAllRequests() {
        requestsData.clear();
        HashMap<String, Image> friendRequests = null;
        HashMap<String, Image> groupRequests = null;
        friendRequests = DatabaseHandler.getInstance().loadAllFriends(UserSession.getInstance().getUser().
                getUsername(), false, "%requests%");
        groupRequests = DatabaseHandler.getInstance().loadAllGroups(UserSession.getInstance().getUser().getUsername(),
                "request");

        for (String s : friendRequests.keySet()) {
            requestsData.add(new CustomRow(s, friendRequests.get(s), false));
        }

        for (String s : groupRequests.keySet()) {
            requestsData.add(new CustomRow(s, groupRequests.get(s), true));
        }
    }

    private void accept(CustomRow row) {
        if (row.isGroup) {
            DatabaseHandler.getInstance().updateUserToGroupRelationship(UserSession.getInstance().getUser().
                    getUsername(), row.name, "member");

            GroupsMenuController groupsMenuController = (GroupsMenuController)
                    ScreenController.getInstance().getController("GroupsMenu");

            groupsMenuController.addGroup(row.name, DatabaseHandler.getInstance().getGroupPhoto(row.name));

        } else {
            DatabaseHandler.getInstance().updateUserRelationship(UserSession.getInstance().getUser().getUsername(),
                    row.name, "friends");

            AllFriendsController allFriendsController =
                    (AllFriendsController) ScreenController.getInstance().getController("AllFriends");

            allFriendsController.addFriend(row.name);
        }
        requestsData.remove(row);
    }

    private void decline(CustomRow row) {
        requestsData.remove(row);
    }

    private class CustomRow {
        private final String name;
        private final Image image;
        private final boolean isGroup;

        public CustomRow(String name, Image image, boolean isGroup) {
            super();
            this.name = name;
            this.image = image;
            this.isGroup = isGroup;
        }
    }

    public class CustomCell extends ListCell<CustomRow> {
        private final Label username;
        private final Circle circle;
        private HBox content;
        private VBox vBox;

        public CustomCell() {
            super();
            content = new HBox(10);
            vBox = new VBox();
            username = new Label();
            username.getStyleClass().add("text-fields");
            circle = new Circle(100, 85, 15);
        }

        @Override
        protected void updateItem(CustomRow item, boolean empty) {
            super.updateItem(item, empty);

            Platform.runLater(() -> {
                if (item == null) {
                    setGraphic(null);
                } else {
                    content = new HBox(10);
                    vBox = new VBox(-1);
                    circle.setFill(new ImagePattern(item.image));
                    content.getChildren().add(circle);

                    Label requester = new Label("User");
                    requester.setTextFill(Color.WHITE);
                    requester.setFont(new Font(12));

                    if (item.isGroup) {
                        requester.setText("Group");
                    }

                    username.setText(item.name);
                    vBox.getChildren().add(username);
                    vBox.getChildren().add(requester);

                    Rectangle accept = new Rectangle(25, 25);
                    accept.setFill(new ImagePattern(new Image("com/ong/images/green_tick.png")));

                    accept.setOnMouseClicked(event -> {
                        accept(item);
                        requestsData.remove(item);
                    });

                    accept.setOnMouseEntered(event -> accept.setBlendMode(BlendMode.DIFFERENCE));

                    accept.setOnMouseExited(event -> accept.setBlendMode(null));

                    Rectangle decline = new Rectangle(25, 25);
                    decline.setFill(new ImagePattern(new Image("com/ong/images/red_x.png")));

                    decline.setOnMouseClicked(event -> decline(item));

                    decline.setOnMouseEntered(event -> decline.setBlendMode(BlendMode.DIFFERENCE));

                    decline.setOnMouseExited(event -> decline.setBlendMode(null));

                    HBox left = new HBox(10);
                    left.setAlignment(Pos.CENTER_LEFT);
                    left.getChildren().add(circle);
                    left.getChildren().add(vBox);

                    HBox right = new HBox(10);
                    right.setAlignment(Pos.CENTER_RIGHT);
                    right.getChildren().add(accept);
                    right.getChildren().add(decline);
                    HBox.setHgrow(right, Priority.ALWAYS);

                    content.getChildren().add(left);
                    content.getChildren().add(right);
                    content.setAlignment(Pos.CENTER_LEFT);

                    setGraphic(content);
                }
            });


            content.setOnMouseEntered(event -> {
                content.setBackground(hoverBackground);
                content.requestFocus();
            });

            content.setOnMouseExited(event -> content.setBackground(null));

            setStyle("-fx-control-inner-background: transparent");
        }
    }
}
