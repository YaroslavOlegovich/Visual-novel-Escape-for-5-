package org.example.visualnovelapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class VisualNovelApp extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {


        GameController controller = new GameController();
        Parent root = controller.createUI(); // Метод в контроллере для создания UI
        // --------------------------------------------------------------------

        Scene scene = new Scene(root, 800, 600); // Размер окна



        primaryStage.setTitle("Визуальная новелла: Тюрьма");
        primaryStage.setScene(scene);
        primaryStage.show();


    }

    public static void main(String[] args) {
        launch(args);
    }
}