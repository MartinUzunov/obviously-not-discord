package com.ong.controllers.PopUps;

import com.ong.controllers.HomepageController;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

/**
 * Represents the close button of the PopUps.
 */
public class CloseButton extends VBox {
    public CloseButton(HomepageController.CloseCallback closeCallback){
        Rectangle rectangle = new Rectangle(15, 15);
        rectangle.setFill(new ImagePattern(new Image("com/ong/images/x.png")));

        rectangle.setOnMouseClicked(event -> closeCallback.close());

        rectangle.setOnMouseEntered(event -> rectangle.setBlendMode(BlendMode.DIFFERENCE));

        rectangle.setOnMouseExited(event -> rectangle.setBlendMode(null));

        getChildren().add(rectangle);
    }
}
