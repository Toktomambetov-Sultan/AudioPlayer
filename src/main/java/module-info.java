module java_fx.audioeditor {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.desktop;


    opens java_fx.audioeditor to javafx.fxml;
    exports java_fx.audioeditor;
}