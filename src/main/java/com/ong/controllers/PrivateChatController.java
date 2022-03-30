package com.ong.controllers;

import com.ong.client.Client;
import com.ong.core.Log;
import com.ong.core.Message;
import com.ong.session.DatabaseHandler;
import com.ong.session.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import org.fxmisc.richtext.InlineCssTextArea;
import org.fxmisc.richtext.TextExt;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class PrivateChatController implements Initializable {

    private final Background messageDefaultBackground = new Background(new BackgroundFill(Color.web("#36393f"), CornerRadii.EMPTY, Insets.EMPTY));
    private final Background messageHoverBackground = new Background(new BackgroundFill(Color.web("#393c42"), CornerRadii.EMPTY, Insets.EMPTY));

    @FXML
    public Label friendUsername;
    @FXML
    private VBox chatMessages;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private TextArea textArea;
    private String lastMessage = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        textArea.setStyle("-fx-font-size: 16px;");
        textArea.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER && keyEvent.isShiftDown()) {
                textArea.appendText("\n");
            } else if (keyEvent.getCode() == KeyCode.ENTER) {
                String text = textArea.getText();
                text = text.substring(0, text.length() - 1);

                if (!text.isBlank()) {
                    Message message = new Message(friendUsername.getText(), UserSession.getInstance().getUser().getUsername(), text, LocalDateTime.now());
                    addMessage(message);
                    sendMessage(message);
                }

                textArea.setText("");
            }
        });

        textArea.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                textArea.getStyleClass().add("fx-text-fill: white;");
            }
        });

        chatMessages.heightProperty().addListener((observable, oldValue, newValue) -> scrollPane.setVvalue((double) newValue));

    }

    public void addMessage(Message message) {
        HBox hBox = new HBox(10);
        VBox vBox = new VBox(3);
        final Circle circle = new Circle(100, 85, 25);

        if (!lastMessage.equals(message.getFromUsername())) {
            Image image = DatabaseHandler.getInstance().getUserPhoto(message.getFromUsername());
            circle.setFill(new ImagePattern(image));
            hBox.getChildren().add(circle);
        } else {
            circle.setFill(Color.web("#36393f"));
            hBox.setPadding(new Insets(0, 0, 0, 61));
        }

        TextExt username = new TextExt();
        username.setText(message.getFromUsername());
        username.setFill(Color.WHITE);
        username.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        InlineCssTextArea msg = new InlineCssTextArea();
        int height = 30;
        int wrap = (int) Math.ceil((double) message.getMessage().length() / 180.0);
        wrap += message.getMessage().split("\r\n|\r|\n").length - 1;
        msg.setEditable(false);
        msg.setPrefSize(1000, height * wrap);
        msg.setMinSize(1000, height * wrap);

        msg.setWrapText(true);
        msg.setStyle("-fx-font-size: 15px;");
        msg.setBackground(messageDefaultBackground);
        msg.appendText(message.getMessage());

        msg.moveTo(0);
        msg.requestFollowCaret();

        msg.addEventFilter(ScrollEvent.ANY, event -> {
            scrollPane.setHvalue(scrollPane.getHvalue() + event.getDeltaX());
            event.consume();
        });

        if (!lastMessage.equals(message.getFromUsername())) {
            vBox.getChildren().add(username);
        }
        vBox.getChildren().add(msg);
        vBox.setAlignment(Pos.CENTER_LEFT);
        vBox.setFillWidth(true);

        hBox.getChildren().add(vBox);
        hBox.setFillHeight(true);

        hBox.setOnMouseEntered(event -> {
            hBox.styleProperty().set("-fx-background-color: #393c42;");
            msg.setBackground(messageHoverBackground);
        });

        hBox.setOnMouseExited(event -> {
            hBox.styleProperty().set("-fx-background-color: #36393f;");
            msg.setBackground(messageDefaultBackground);
        });

        chatMessages.getChildren().add(hBox);

        lastMessage = message.getFromUsername();
    }

    public void sendMessage(Message message) {
        Client.clientSender.addItem(message);
    }

    public void loadMessages() {
        ArrayList<Message> messages = null;
        try {
            messages = DatabaseHandler.getInstance().loadDMMessages(UserSession.getInstance().getUser().getUsername(),
                    friendUsername.getText());
        } catch (Exception e) {
            Log.error(e);
        }

        for (Message m : messages) {
            addMessage(m);
        }
    }
}
