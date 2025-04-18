module org.example.visualnovelapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;


    opens org.example.visualnovelapp to javafx.fxml;
    exports org.example.visualnovelapp;
}