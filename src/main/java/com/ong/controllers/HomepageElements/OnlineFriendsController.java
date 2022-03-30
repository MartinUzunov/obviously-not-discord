package com.ong.controllers.HomepageElements;

import com.ong.client.Client;
import com.ong.controllers.HomepageController;
import com.ong.controllers.ScreenController;
import com.ong.core.Message;
import com.ong.core.User;
import com.ong.session.DatabaseHandler;
import com.ong.session.UserSession;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.util.Callback;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OnlineFriendsController implements Initializable {

    @FXML
    private ListView onlineFriendsListView;

    @FXML
    private Label numberOfOnlineUsers;

    private ObservableList<CustomRow> onlineData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        final Message[] m = new Message[1];
        Runnable runnable = null;

        runnable = () -> {
            m[0] = new Message(null, null, null, UserSession.getInstance().getUser().
                    getUsername(), null, null);
            m[0].setType(Message.MessageType.REQUEST_FRIENDS_ONLINE);
            Client.clientSender.addItem(m[0]);
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(runnable, 0, 5, TimeUnit.SECONDS);

        onlineData = FXCollections.observableArrayList();

        onlineFriendsListView.setItems(onlineData);

        onlineFriendsListView.setCellFactory((Callback<ListView<CustomRow>, ListCell<CustomRow>>) listView -> new CustomCell());

        onlineFriendsListView.getSelectionModel().selectedItemProperty().addListener((ChangeListener<CustomRow>) (observable, oldValue, newValue) -> {
            HomepageController homepageController = (HomepageController) ScreenController.getInstance().getController("Homepage");
            homepageController.toDM(newValue.getName());
        });

    }

    public void updateOnline(Message message) {
        ArrayList<User> onlineUsers = (ArrayList<User>) message.getFreeObject();
        onlineData.clear();
        AllFriendsController allFriendsController = (AllFriendsController) ScreenController.getInstance().
                getController("AllFriends");
        for (User user : onlineUsers) {
            if (!DatabaseHandler.getInstance().getPreferredStatus(user.getUsername()).equals("Invisible") &&
                    allFriendsController.getAllUsers().containsKey(user.getUsername())) {
                onlineData.add(new CustomRow((user.getUsername()), allFriendsController.getUserImage(user.getUsername())));
            }
        }
        numberOfOnlineUsers.setText(String.valueOf(onlineData.size()));
    }

    private class CustomRow {
        private final String name;
        private final Image image;

        public CustomRow(String name, Image image) {
            super();
            this.name = name;
            this.image = image;
        }

        public String getName() {
            return name;
        }

        public Image getImage() {
            return image;
        }
    }

    public class CustomCell extends ListCell<CustomRow> {
        private final Label name;
        private final Circle circle;
        private HBox content;
        private VBox vBox;

        public CustomCell() {
            super();
            content = new HBox(10);
            vBox = new VBox();
            name = new Label();
            name.getStyleClass().add("text");
            circle = new Circle(100, 85, 15);
        }

        @Override
        protected void updateItem(CustomRow item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null) {
                setGraphic(null);
            } else {
                content = new HBox(10);
                vBox = new VBox();
                name.setText(item.name);
                if (item.getImage() != null) {
                    circle.setFill(new ImagePattern(item.getImage()));
                }
                content.getChildren().add(circle);

                vBox.getChildren().add(name);
                HBox statusLine = new HBox(3);
                statusLine.setAlignment(Pos.CENTER_LEFT);
                String status = "";
                status = DatabaseHandler.getInstance().getPreferredStatus(name.getText());

                Circle statusColor = new Circle(6);

                switch (status) {
                    case "Away":
                        statusColor.setFill(Color.YELLOW);
                        break;
                    case "Do Not Disturb":
                        statusColor.setFill(Color.RED);
                        break;
                    case "Invisible":
                        statusColor.setFill(Color.GRAY);
                        break;
                    default:
                        statusColor.setFill(Color.GREEN);
                        break;
                }

                Label statusName = new Label(status);
                statusName.setFont(new Font(10));
                statusName.setTextFill(Color.GRAY);

                statusLine.getChildren().add(statusColor);
                statusLine.getChildren().add(statusName);

                vBox.getChildren().add(statusLine);

                content.getChildren().add(vBox);

                content.setAlignment(Pos.CENTER_LEFT);

                setGraphic(content);
            }

            content.setOnMouseEntered(event -> content.setBackground(new Background(new BackgroundFill(Color.web("#393c42"), CornerRadii.EMPTY, Insets.EMPTY))));

            content.setOnMouseExited(event -> content.setBackground(null));

            setStyle("-fx-control-inner-background: transparent");
        }
    }
}
