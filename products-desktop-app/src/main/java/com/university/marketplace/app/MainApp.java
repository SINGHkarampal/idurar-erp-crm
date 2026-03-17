package com.university.marketplace.app;

import com.university.marketplace.util.DatabaseInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        DatabaseInitializer.initializeDatabase();
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/university/marketplace/fxml/MainView.fxml"));
        Scene scene = new Scene(loader.load(), 1280, 760);
        scene.getStylesheets().add("org/kordamp/bootstrapfx/bootstrapfx.css");
        scene.getStylesheets().add(MainApp.class.getResource("/com/university/marketplace/css/app.css").toExternalForm());
        stage.setTitle("Marketplace Products Management");
        stage.setMinWidth(1120);
        stage.setMinHeight(680);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
