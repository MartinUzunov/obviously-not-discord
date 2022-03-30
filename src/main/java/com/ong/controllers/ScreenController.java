package com.ong.controllers;

import com.ong.Main;
import com.ong.core.Group;
import com.ong.core.Log;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.HashMap;

/**
 *
 */
public final class ScreenController {
    public boolean running;
    private static ScreenController instance;
    private HashMap<String, Parent> screenMap;
    private HashMap<String, Parent> views;
    private HashMap<String, Object> controllers;
    private HashMap<String, Group> groups;
    private Stage main;
    private final Image logo = new Image(Main.class.getResource("Images/not_logo.png").toString());

    private ScreenController() {
        screenMap = new HashMap<>();
        controllers = new HashMap<>();
        views = new HashMap<>();
        groups = new HashMap<>();
        main = new Stage();
        running = true;
    }

    private ScreenController(Stage _main) {
        screenMap = new HashMap<>();
        controllers = new HashMap<>();
        views = new HashMap<>();
        groups = new HashMap<>();
        main = _main;
        running = true;
    }

    public static ScreenController getInstance() {
        if (instance == null) {
            instance = new ScreenController();
        }
        return instance;
    }

    public static ScreenController getInstance(Stage _main) {
        if (instance == null) {
            instance = new ScreenController(_main);
        }
        return instance;
    }

    public void addScreen(String name, FXMLLoader fxmlLoader) {
        Parent parent = null;
        try {
            parent = fxmlLoader.load();
        } catch (Exception e) {
            Log.error(e);
        }
        screenMap.put(name, parent);
        controllers.put(name, fxmlLoader.getController());
    }

    public Parent getScreen(String name){
        return screenMap.get(name);
    }

    public void setMinimumDimensions(int width, int height) {
        main.setMinWidth(width);
        main.setMinHeight(height);
    }

    public void setMaximized(boolean full) {
        main.setMaximized(full);
    }

    public void addController(String name, Object o) {
        controllers.put(name, o);
    }

    public Object getController(String name) {
        return controllers.get(name);
    }

    public void addView(String name, Parent parent) {
        views.put(name, parent);
    }

    public Parent getView(String name) {
        return views.get(name);
    }

    public Stage getStage(){
        return main;
    }

    public void addGroup(Group group){
        groups.put(group.getName(),group);
    }

    public Group getGroup(String name){
        return groups.get(name);
    }

    public void removeView(String name){
        views.remove(name);
    }

    public void removeController(String name){
        controllers.remove(name);
    }

    public void activate(String name) {
        Scene scene = new Scene(screenMap.get(name));
        scene.getRoot().setStyle("-fx-font-family: 'Times New Roman'");
        main.setScene(scene);
        main.getIcons().add(logo);
        main.setTitle(name);
        main.show();
    }

    public void removeGroup(String groupName){
        groups.remove(groupName);
    }

    public void clearSession(){
        instance = null;
        screenMap = null;
        views = null;
        controllers = null;
        groups= null;
        main= null;
        running = false;
    }
}
