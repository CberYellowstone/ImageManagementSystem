package edu.scau.imagemanagementsystem;

import java.io.IOException;
import java.util.Objects;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader
                .load(Objects
                        .requireNonNull(getClass().getResource("/edu/scau/imagemanagementsystem/fxml/MainView.fxml")));
        primaryStage.setTitle("电子图片管理程序");
        primaryStage.setScene(new Scene(root, 1200, 800));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}