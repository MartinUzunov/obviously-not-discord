package com.ong;

import com.ong.controllers.ScreenController;
import com.ong.core.Log;
import com.ong.core.User;
import com.ong.session.UserSession;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Defines the Entry point of the JavaFX Application. Login screen set as the starting view.
 */
public class Main extends Application {

    public static Stage mainStage;

    public static void main(String[] args) {
        launch();
    }

    public static void login(Stage stage) {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Views/SignIn.fxml"));
        ScreenController screenController = ScreenController.getInstance(stage);
        screenController.addScreen("Login", fxmlLoader);
        ScreenController.getInstance().addController("Login", fxmlLoader.getController());
        screenController.activate("Login");
        screenController.setMaximized(true);
        ScreenController.getInstance().setMinimumDimensions(645, 440);

    }

    @Override
    public void start(Stage stage) {
        mainStage = stage;
        initialize(stage);
        login(stage);
        stage.getScene().getStylesheets().add(getClass().getResource("CSS/global.css").toExternalForm());

    }

    private void initialize(Stage stage) {
        Log.init();
        Log.info("Start Session");
        stage.setOnCloseRequest(event -> System.exit(0));
    }
}
