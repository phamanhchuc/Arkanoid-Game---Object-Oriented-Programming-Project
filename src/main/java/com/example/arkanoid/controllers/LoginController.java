package com.example.arkanoid.controllers;

import com.example.arkanoid.GameData;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/arkanoid/main-view.fxml"));
            Parent gameRoot = loader.load();
            Scene gameScene = new Scene(gameRoot);
            stage.setScene(gameScene);
            stage.setTitle("Arkanoid Game");

            // --- THAY ĐỔI Ở ĐÂY: Chuyển cửa sổ sang chế độ toàn màn hình ---
            stage.setFullScreen(true);

        } catch (IOException ex) {
            System.err.println("Lỗi: Không thể tải file main-view.fxml.");
            ex.printStackTrace();
        }
    }
}

