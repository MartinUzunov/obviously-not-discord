package com.ong.controllers;

import com.ong.Main;
import com.ong.controllers.PopUps.ChangePasswordPopupController;
import com.ong.controllers.PopUps.ChangePhotoPopupController;
import com.ong.core.Log;
import com.ong.session.DatabaseHandler;
import com.ong.session.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import org.fxmisc.richtext.InlineCssTextArea;

import java.net.URL;
import java.util.ResourceBundle;

public class UserSectionController implements Initializable {

    @FXML
    private HBox userInfo;

    private ContextMenu contextMenu;

    private Circle userPhotoCircle;

    private HBox status;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUserPhoto();
        setUsernameAndStatus();
        setIcons();
    }

    private void setUserPhoto() {
        userPhotoCircle = new Circle(100, 85, 25);
        userPhotoCircle.setFill(new ImagePattern(UserSession.getInstance().getUser().getImage()));

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Views/PopUps/ChangePhotoPopup.fxml"));
        Parent parentChangePhoto = null;
        try {
            parentChangePhoto = fxmlLoader.load();
        } catch (Exception e) {
            Log.error(e);
        }
        Parent finalParentChangePhoto = parentChangePhoto;

        ChangePhotoPopupController changePhotoPopupController = fxmlLoader.getController();
        changePhotoPopupController.setCloseAction(() -> {
            HomepageController homepageController = (HomepageController) ScreenController.getInstance().
                    getController("Homepage");

            homepageController.removeFromStackPane(finalParentChangePhoto);
            homepageController.removeBlur();
        });


        userPhotoCircle.setOnMousePressed(event -> {
            HomepageController homepageController = (HomepageController) ScreenController.getInstance().
                    getController("Homepage");

            homepageController.addToStackPane(finalParentChangePhoto);
            homepageController.blur();
        });

        setBlendEffect(userPhotoCircle, BlendMode.DARKEN);
        userInfo.getChildren().add(userPhotoCircle);
    }

    private void setUsernameAndStatus() {
        // username
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER_LEFT);
        vBox.setPadding(new Insets(5, 0, 0, 8));
        InlineCssTextArea inlineCssTextArea = new InlineCssTextArea();
        inlineCssTextArea.setEditable(false);
        inlineCssTextArea.setWrapText(false);
        inlineCssTextArea.setStyle("-fx-font-size: 15px; -fx-text-fill: white; -fx-font-weight: bold;");
        inlineCssTextArea.setMaxWidth(90);
        inlineCssTextArea.setBackground(new Background(new BackgroundFill(Color.web("#292b2f"), CornerRadii.EMPTY, Insets.EMPTY)));
        inlineCssTextArea.appendText(UserSession.getInstance().getUser().getUsername());

        inlineCssTextArea.totalHeightEstimateProperty();
        inlineCssTextArea.totalHeightEstimateProperty();
        vBox.getChildren().add(inlineCssTextArea);

        // status
        status = new HBox(3);
        status.setBackground(new Background(new BackgroundFill(Color.web("#292b2f"), CornerRadii.EMPTY, Insets.EMPTY)));
        status.setAlignment(Pos.TOP_LEFT);
        status.setPadding(new Insets(0, 0, 5, 0));

        ContextMenu statusMenu = setStatusMenu();
        status.setOnMouseClicked(event -> {
            Bounds boundsInScene = status.localToScene(status.getBoundsInLocal());
            statusMenu.show(status, boundsInScene.getCenterX() - status.getWidth(),
                    boundsInScene.getCenterY() - status.getHeight() * 2 - 10);
        });

        String preferredStatus = "";
        preferredStatus = DatabaseHandler.getInstance().getPreferredStatus(UserSession.getInstance().
                getUser().getUsername());

        if(preferredStatus == null) {
            preferredStatus = "Online";
        }

        InlineCssTextArea statusName = createStatusNameField(preferredStatus);
        statusName.setPadding(new Insets(0, 0, 5, 0));
        Circle circle = new Circle(0, 0, 5);

        switch (preferredStatus) {
            case "Away":
                circle.setFill(Color.YELLOW);
                break;
            case "Do Not Disturb":
                circle.setFill(Color.RED);
                break;
            case "Invisible":
                circle.setFill(Color.GRAY);
                break;
            default:
                circle.setFill(Color.GREEN);
                break;
        }

        status.getChildren().add(circle);
        status.getChildren().add(statusName);

        vBox.getChildren().add(status);
        userInfo.getChildren().add(vBox);
    }

    private void setIcons() {
        HBox icons = new HBox(3);
        icons.setAlignment(Pos.CENTER_LEFT);
        ImagePattern headphones = new ImagePattern(new Image("com/ong/images/headphones.png"));
        ImagePattern headphonesOff = new ImagePattern(new Image("com/ong/images/headphones_off.png"));
        ImagePattern microphone = new ImagePattern(new Image("com/ong/images/microphone.png"));
        ImagePattern microphoneOff = new ImagePattern(new Image("com/ong/images/microphone_off.png"));
        ImagePattern settings = new ImagePattern(new Image("com/ong/images/settings.png"));

        // headphones
        Rectangle headphonesRec = new Rectangle(25, 25);
        headphonesRec.setFill(headphones);
        setBlendEffect(headphonesRec, BlendMode.DIFFERENCE);
        headphonesRec.setOnMouseClicked(event -> {
            if (headphonesRec.getFill().equals(headphones)) {
                headphonesRec.setFill(headphonesOff);
            } else {
                headphonesRec.setFill(headphones);
            }
        });

        Rectangle microphoneRec = new Rectangle(25, 25);
        microphoneRec.setFill(microphone);
        setBlendEffect(microphoneRec, BlendMode.DIFFERENCE);
        microphoneRec.setOnMouseClicked(event -> {
            if (microphoneRec.getFill().equals(microphone)) {
                microphoneRec.setFill(microphoneOff);
            } else {
                microphoneRec.setFill(microphone);
            }
        });

        // settings
        Rectangle settingsRec = new Rectangle(25, 25);
        settingsRec.setFill(settings);
        setBlendEffect(settingsRec, BlendMode.DIFFERENCE);

        icons.getChildren().add(headphonesRec);
        icons.getChildren().add(microphoneRec);
        icons.getChildren().add(settingsRec);

        userInfo.getChildren().add(icons);

        setSettingsMenu(settingsRec);
    }

    private void setSettingsMenu(Rectangle settingsRec) {
        settingsRec.setOnMouseClicked(event -> {
            Bounds boundsInScene = settingsRec.localToScene(settingsRec.getBoundsInLocal());
            contextMenu.show(settingsRec, boundsInScene.getCenterX() - settingsRec.getWidth() * 4,
                    boundsInScene.getCenterY() - settingsRec.getHeight() * 2 - 10);
        });

        contextMenu = new ContextMenu();
        contextMenu.setStyle("-fx-background-color: #18191c;");
        MenuItem changePassword = new MenuItem("Change Password");
        MenuItem logout = new MenuItem("Logout");
        addStyleToMenuItems(changePassword, logout);
        contextMenu.getItems().addAll(changePassword, logout);

        setChangePasswordMenuItem(changePassword);

        logout.setOnAction(event -> {
            UserSession.getInstance().clearSession();
            ScreenController.getInstance().clearSession();
            Main.login(Main.mainStage);


        });
    }

    private void setChangePasswordMenuItem(MenuItem menuItem) {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Views/PopUps/ChangePasswordPopup.fxml"));
        Parent parentChangePassword = null;
        try {
            parentChangePassword = fxmlLoader.load();
        } catch (Exception e) {
            Log.error(e);
        }
        Parent finalParentChangePassword = parentChangePassword;

        ChangePasswordPopupController changePasswordPopupController = fxmlLoader.getController();
        changePasswordPopupController.setCloseAction(() -> {
            HomepageController homepageController = (HomepageController) ScreenController.getInstance().
                    getController("Homepage");

            homepageController.removeFromStackPane(finalParentChangePassword);
            homepageController.removeBlur();
        });

        menuItem.setOnAction(event -> {
            HomepageController homepageController = (HomepageController) ScreenController.getInstance().
                    getController("Homepage");
            homepageController.blur();
            homepageController.addToStackPane(finalParentChangePassword);
        });
    }

    private ContextMenu setStatusMenu() {
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setStyle("-fx-background-color: #18191c;");

        MenuItem online = new MenuItem("Online");
        Circle onlineGraphic = new Circle(5, Color.GREEN);
        online.setGraphic(onlineGraphic);
        online.setOnAction(event -> {
            MenuItem menuItem = (MenuItem) event.getSource();
            DatabaseHandler.getInstance().updatePreferredStatus(UserSession.getInstance().getUser().getUsername(),
                    menuItem.getText());

            changeCurrentStatus(menuItem.getText(), menuItem.getGraphic());
        });

        MenuItem away = new MenuItem("Away");
        Circle awayGraphic = new Circle(5, Color.YELLOW);
        away.setGraphic(awayGraphic);
        away.setOnAction(event -> {
            MenuItem menuItem = (MenuItem) event.getSource();

            DatabaseHandler.getInstance().updatePreferredStatus(UserSession.getInstance().getUser().getUsername(),
                    menuItem.getText());

            changeCurrentStatus(menuItem.getText(), menuItem.getGraphic());
        });

        MenuItem doNotDisturb = new MenuItem("Do Not Disturb");
        Circle doNotDisturbGraphic = new Circle(5, Color.RED);
        doNotDisturb.setGraphic(doNotDisturbGraphic);
        doNotDisturb.setOnAction(event -> {
            MenuItem menuItem = (MenuItem) event.getSource();

            DatabaseHandler.getInstance().updatePreferredStatus(UserSession.getInstance().getUser().getUsername(),
                    menuItem.getText());

            changeCurrentStatus(menuItem.getText(), menuItem.getGraphic());
        });

        MenuItem invisible = new MenuItem("Invisible");
        Circle invisibleGraphic = new Circle(5, Color.GREY);
        invisible.setGraphic(invisibleGraphic);
        invisible.setOnAction(event -> {
            MenuItem menuItem = (MenuItem) event.getSource();

            DatabaseHandler.getInstance().updatePreferredStatus(UserSession.getInstance().getUser().getUsername(),
                    menuItem.getText());

            changeCurrentStatus(menuItem.getText(), menuItem.getGraphic());
        });

        addStyleToMenuItems(online, away, doNotDisturb, invisible);
        contextMenu.getItems().addAll(online, away, doNotDisturb, invisible);

        return contextMenu;
    }

    private void addStyleToMenuItems(MenuItem... menuItem) {
        for (MenuItem mi : menuItem) {
            mi.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        }
    }

    private void changeCurrentStatus(String newStatusText, Node newGraphic) {
        status.getChildren().clear();
        Circle oldCircle = (Circle) newGraphic;
        Circle newCircle = new Circle(oldCircle.getRadius(), oldCircle.getFill());
        status.getChildren().add(newCircle);
        status.getChildren().add(createStatusNameField(newStatusText));
    }

    private InlineCssTextArea createStatusNameField(String text) {
        InlineCssTextArea statusName = new InlineCssTextArea();
        statusName.setEditable(false);
        statusName.setWrapText(false);
        statusName.getStyleClass().clear();
        statusName.setBackground(new Background(new BackgroundFill(Color.web("#292b2f"), CornerRadii.EMPTY, Insets.EMPTY)));
        statusName.appendText(text);
        statusName.totalHeightEstimateProperty();
        statusName.getStyleClass().add("online-status");
        //statusName.setPadding(new Insets(3,0,0,0));

        statusName.setOnMouseEntered(event -> {
            statusName.getStyleClass().clear();
            statusName.getStyleClass().add("online-status-hovered");
        });

        statusName.setOnMouseExited(event -> {
            statusName.getStyleClass().clear();
            statusName.getStyleClass().add("online-status");
        });

        return statusName;
    }

    private void setBlendEffect(Node node, BlendMode blendMode) {
        node.setOnMouseEntered(event -> node.setBlendMode(blendMode));

        node.setOnMouseExited(event -> node.setBlendMode(null));
    }

    public Circle getUserPhotoCircle() {
        return userPhotoCircle;
    }
}
