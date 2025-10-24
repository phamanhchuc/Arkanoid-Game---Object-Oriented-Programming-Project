package com.example.arkanoid;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
// Bỏ import StackPane

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/arkanoid/login.fxml"));
        Parent root = loader.load();

        // --- CODE CẬP NHẬT ---
        // Không bọc trong StackPane và không set kích thước 1200x955.5
        // Scene sẽ tự lấy kích thước từ 'root' (từ file FXML)
        Scene scene = new Scene(root);
        // --- KẾT THÚC CẬP NHẬT ---

        stage.setTitle("Arkanoid Login");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}