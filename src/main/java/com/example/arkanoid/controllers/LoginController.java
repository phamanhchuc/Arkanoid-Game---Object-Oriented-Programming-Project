package com.example.arkanoid.controllers;

import com.example.arkanoid.GameData;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
// Bỏ import StackPane
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField userNameText;

    @FXML
    private Button buttonLogin;

    @FXML
    private void initialize() {
        buttonLogin.setOnAction(e -> handleLogin());
        addClickAnimation(buttonLogin);
    }

    private void handleLogin() {
        String playerName = userNameText.getText().trim();
        if (playerName.isEmpty()) {
            userNameText.setPromptText("Please enter your name!");
            return;
        }

        GameData.playerName = playerName;

        try {
            Stage stage = (Stage) buttonLogin.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/arkanoid/main-menu.fxml"));
            Parent menuRoot = loader.load();

            // --- CODE CẬP NHẬT ---
            // Không bọc trong StackPane và không set kích thước 1200x955.5
            // Scene sẽ tự lấy kích thước từ 'menuRoot' (từ file FXML)
            Scene menuScene = new Scene(menuRoot);
            // --- KẾT THÚC CẬP NHẬT ---

            stage.setScene(menuScene);
            stage.setTitle("Arkanoid - Main Menu");

        } catch (IOException ex) {
            System.err.println("Lỗi: Không thể tải file main-menu.fxml.");
            ex.printStackTrace();
        }
    }

    // ... (Hàm addClickAnimation giữ nguyên)
    private void addClickAnimation(Button button) {
        ScaleTransition pressTransition = new ScaleTransition(Duration.millis(100), button);
        pressTransition.setToX(0.9);
        pressTransition.setToY(0.9);

        ScaleTransition releaseTransition = new ScaleTransition(Duration.millis(100), button);
        releaseTransition.setToX(1.0);
        releaseTransition.setToY(1.0);

        button.setOnMousePressed(event -> pressTransition.playFromStart());
        button.setOnMouseReleased(event -> releaseTransition.playFromStart());
    }
}