package com.example.arkanoid.controllers;

import com.example.arkanoid.GameData;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
// Bỏ import Screen và Rectangle2D
// import javafx.stage.Screen;
// import javafx.geometry.Rectangle2D;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField userNameText;

    @FXML
    private Button buttonLogin;

    @FXML
    private void initialize() {
        buttonLogin.setOnAction(e -> handleLogin());
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

            // --- THAY ĐỔI Ở ĐÂY: Tạo Scene với kích thước mặc định từ FXML ---
            // Bỏ Rectangle2D screenBounds = Screen.getPrimary().getBounds();
            // Scene menuScene = new Scene(menuRoot, screenBounds.getWidth(), screenBounds.getHeight());
            Scene menuScene = new Scene(menuRoot); // Scene sẽ tự lấy kích thước từ FXML (800x637)

            stage.setScene(menuScene);
            stage.setTitle("Arkanoid - Main Menu");
            // --- XÓA LỆNH setFullScreen Ở ĐÂY ---
            // stage.setFullScreen(true);

        } catch (IOException ex) {
            System.err.println("Lỗi: Không thể tải file main-menu.fxml.");
            ex.printStackTrace();
        }
    }
}

