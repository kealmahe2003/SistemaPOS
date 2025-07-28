package com.cafeteriapos;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cafeteriapos/views/LoginView.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setTitle("Inicio de Sesión - Sistema POS");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}