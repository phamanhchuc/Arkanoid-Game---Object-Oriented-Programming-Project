package com.example.arkanoid.controllers;

import com.example.arkanoid.core.GameData;
import com.example.arkanoid.MainApp;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region; // <-- THÊM IMPORT NÀY
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField userNameText;
    @FXML
    private Button buttonLogin;

    @FXML
    // Gán sự kiện Login cho nút
    private void initialize() {
        buttonLogin.setOnAction(e -> handleLogin());
    }

    // Hàm xử lý toàn bộ việc đăng nhập + chuyển sang menu
    // Lấy tên người chơi -> load menu -> tạo hiệu ứng chuyển cảnh
    private void handleLogin() {
        // Check player name
        String playerName = userNameText.getText().trim();
        if (playerName.isEmpty()) {
            userNameText.setPromptText("Please enter your name!");
            return;
        }
        GameData.playerName = playerName;

        try {
            // Lấy Stage hiện tại (cửa sổ đang hiển thị màn Login).
            Stage stage = (Stage) buttonLogin.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/arkanoid/main-menu.fxml"));

            // --- SỬA LỖI Ở ĐÂY: Đổi 'Parent' thành 'Region' vì setMaxSize() chỉ tồn tại ở lớp Region trở lên.  ---
            Region menuRoot = loader.load();

            StackPane rootPane = new StackPane();
            rootPane.getChildren().add(menuRoot);
            rootPane.setStyle("-fx-background-color: black;");
            rootPane.setAlignment(Pos.CENTER);

            // Dòng này giờ sẽ hoạt động
            menuRoot.setMaxSize(MainApp.DESIGN_WIDTH, MainApp.DESIGN_HEIGHT);

            Scene menuScene = stage.getScene();
            menuScene.setRoot(rootPane);

            MainApp.scaleToFit(menuRoot, menuScene);

            stage.setTitle("Arkanoid - Main Menu");

            // Hiệu ứng Fade-in: Giao diện bắt đầu trong suốt → hiện dần trong 0.5 giây.
            menuRoot.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(500), menuRoot);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.play();

        } catch (IOException ex) {
            System.err.println("Lỗi: Không thể tải file main-menu.fxml.");
            ex.printStackTrace();
        }
    }

    // Tạo hiệu ứng thu/ phóng khi nhấn nút
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