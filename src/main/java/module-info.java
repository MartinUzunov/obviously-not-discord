module com.ong.obviouslynotdiscord {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.fxmisc.richtext;


    opens com.ong to javafx.fxml;
    exports com.ong;
    exports com.ong.controllers;
    opens com.ong.controllers.HomepageElements to javafx.fxml;
    opens com.ong.controllers.GroupElements to javafx.fxml;
    opens com.ong.controllers to javafx.fxml;
    opens com.ong.controllers.PopUps to javafx.fxml;
}