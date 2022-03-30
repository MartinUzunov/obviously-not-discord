package com.ong.controllers.GroupElements;

import com.ong.client.Client;
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

/**
 * FXML File: GroupChat.fxml
 *
 * Represents the middle part of the Border Pane group view. The top part of the view contains the channel name. The
 * middle part contains Scroll pane which contains all messages. The bottom part contains Text Area for typing messages.
 */
public class GroupChatController implements Initializable {

    @FXML
    public Label channelNameLabel;

    @FXML
    private VBox chatMessagesVBox;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private TextArea sendMessageTextArea;

    private String lastMessage = "";
    public String group;
    public String category;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeSendMessagesTextArea();
        chatMessagesVBox.heightProperty().addListener((observable, oldValue, newValue) -> scrollPane.setVvalue((double) newValue));
    }

    /**
     * Creates and adds message view(HBox) to the Scroll Pane
     *
     * @param message
     */
    public void addMessageToView(Message message) {
        HBox cellHBox = new HBox(10);
        cellHBox.setFillHeight(true);

        VBox NameAndMessageVBox = new VBox(3);
        NameAndMessageVBox.setAlignment(Pos.CENTER_LEFT);
        NameAndMessageVBox.setFillWidth(true);

        final Circle userPhoto = new Circle(100, 85, 25);

        // adds the user photo to the cell, if the last message is sent by different user
        if (!lastMessage.equals(message.getFromUsername())) {
            Image image = DatabaseHandler.getInstance().getUserPhoto(message.getFromUsername());
            userPhoto.setFill(new ImagePattern(image));
            cellHBox.getChildren().add(userPhoto);
        } else {
            // padding when there is no photo added to the cell
            cellHBox.setPadding(new Insets(0, 0, 0, 61));
        }

        // adds username to the cell, if the last message is sent by different user
        TextExt username = new TextExt();
        username.setText(message.getFromUsername());
        username.setFill(Color.WHITE);
        username.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        if (!lastMessage.equals(message.getFromUsername())) {
            NameAndMessageVBox.getChildren().add(username);
        }

        // add the message node to the cell
        InlineCssTextArea messageTextArea = createMessageNode(message.getMessage());
        NameAndMessageVBox.getChildren().add(messageTextArea);

        cellHBox.getChildren().add(NameAndMessageVBox);

        setHoverEffect(cellHBox, messageTextArea);
        chatMessagesVBox.getChildren().add(cellHBox);

        lastMessage = message.getFromUsername();
    }

    /**
     * Loads all messages in the current channel from Database and adds them to the view.
     */
    public void loadMessages() {
        ArrayList<Message> messages = DatabaseHandler.getInstance().loadGroupChannelMessages(group, category,
                channelNameLabel.getText());

        for (Message m : messages) {
            addMessageToView(m);
        }
    }

    private InlineCssTextArea createMessageNode(String message) {
        InlineCssTextArea messageTextArea = new InlineCssTextArea();
        messageTextArea.setStyle("-fx-font-size: 15px;");
        messageTextArea.setBackground(new Background(new BackgroundFill(Color.web("#36393f"), CornerRadii.EMPTY, Insets.EMPTY)));

        // calculates the cell height, so the entire message is displayed
        int height = 35;
        int wrap = (int) Math.ceil((double) message.length() / 180.0);
        wrap += message.split("\r\n|\r|\n").length - 1;
        messageTextArea.setEditable(false);
        messageTextArea.setPrefSize(850, height * wrap);
        messageTextArea.setMinSize(850, height * wrap);
        messageTextArea.setWrapText(true);

        messageTextArea.appendText(message);
        messageTextArea.moveTo(0);
        messageTextArea.requestFollowCaret();

        messageTextArea.addEventFilter(ScrollEvent.ANY, event -> {
            scrollPane.setHvalue(scrollPane.getHvalue() + event.getDeltaX());
            event.consume();
        });
        return messageTextArea;
    }

    private void sendMessageToServer(Message message) {
        Client.clientSender.addItem(message);
    }

    private void initializeSendMessagesTextArea() {
        sendMessageTextArea.setStyle("-fx-font-size: 16px;");

        sendMessageTextArea.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER && keyEvent.isShiftDown()) { // new row on SHIFT + ENTER
                sendMessageTextArea.appendText("\n");
            } else if (keyEvent.getCode() == KeyCode.ENTER) {
                String text = sendMessageTextArea.getText();
                text = text.substring(0, text.length() - 1);
                if (text.length() > 1000) {
                    text = text.substring(0, 1000);
                }
                if (!text.isBlank()) {

                    Message message = new Message(group, category, channelNameLabel.getText(),
                            UserSession.getInstance().getUser().getUsername(), text, LocalDateTime.now());
                    addMessageToView(message);
                    sendMessageToServer(message);

                }
                sendMessageTextArea.setText("");
            }
        });

        // set out of focus color
        sendMessageTextArea.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                sendMessageTextArea.getStyleClass().add("fx-text-fill: white;");
            }
        });
    }

    /**
     * Changes the Background color of a HBox on a mouse entered and mouse exited.
     *
     * @param hBox
     */
    private void setHoverEffect(HBox hBox, InlineCssTextArea inlineCssTextArea) {
        hBox.setOnMouseEntered((mouseEvent) -> {
            hBox.setBackground(new Background(new BackgroundFill(Color.web("#393c42"), CornerRadii.EMPTY,
                    Insets.EMPTY)));
            inlineCssTextArea.setBackground(new Background(new BackgroundFill(Color.web("#393c42"), CornerRadii.EMPTY,
                    Insets.EMPTY)));
        });

        hBox.setOnMouseExited((mouseEvent) -> {
            hBox.setBackground(null);
            inlineCssTextArea.setBackground(new Background(new BackgroundFill(Color.web("#36393f"), CornerRadii.EMPTY,
                    Insets.EMPTY)));
        });
    }
}
