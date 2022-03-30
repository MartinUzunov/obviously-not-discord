package com.ong.controllers.PopUps;

import com.ong.controllers.GroupsMenuController;
import com.ong.controllers.HomepageController;
import com.ong.controllers.ScreenController;
import com.ong.controllers.UserSectionController;
import com.ong.session.DatabaseHandler;
import com.ong.session.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class ChangePhotoPopupController implements Initializable {

    @FXML
    private HBox closeHBox;

    @FXML
    private Label headerLabel;

    @FXML
    private Button choosePhotoButton;

    @FXML
    private Button saveChangesButton;

    @FXML
    private ImageView loadedImage;

    private String imageUrl;

    private HomepageController.CloseCallback closeCallback;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        choosePhotoButton.setOnMouseClicked(event -> {
            FileChooser fileChooser = new FileChooser();

            //Set extension filter
            FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
            FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
            fileChooser.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);

            //Show open file dialog
            File file = fileChooser.showOpenDialog(ScreenController.getInstance().getStage());
            if (file != null) {
                imageUrl = file.getAbsolutePath();
                Image image = new Image("file:///" + imageUrl);
                loadedImage.setImage(image);
            }
        });

        saveChangesButton.setOnMouseClicked(event -> {
            if (loadedImage == null || imageUrl == null) {
                return;
            }

            DatabaseHandler.getInstance().updateUserProfilePhoto(UserSession.getInstance().getUser().getUsername(), imageUrl);

            UserSectionController userSectionController = (UserSectionController)
                    ScreenController.getInstance().getController("UserSection");
            userSectionController.getUserPhotoCircle().setFill(new ImagePattern(loadedImage.getImage()));
            closeCallback.close();
        });
    }

    public void changeGroupPhoto(String groupName) {
        headerLabel.setText("Change Group Photo");
        saveChangesButton.setOnMouseClicked(mouseEvent -> {
            if (loadedImage == null || imageUrl == null) {
                return;
            }

            DatabaseHandler.getInstance().updateGroupProfilePhoto(groupName, imageUrl);

            GroupsMenuController groupsMenuController = (GroupsMenuController)
                    ScreenController.getInstance().getController("GroupsMenu");
            groupsMenuController.getGroupNode(groupName).setFill(new ImagePattern(loadedImage.getImage()));
            closeCallback.close();
        });
    }

    public void setCloseAction(HomepageController.CloseCallback closeCallback) {
        this.closeCallback = closeCallback;
        closeHBox.getChildren().add(new CloseButton(closeCallback));
    }
}
