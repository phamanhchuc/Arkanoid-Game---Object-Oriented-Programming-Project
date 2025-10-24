package com.example.arkanoid.controllers;

import com.example.arkanoid.GameData;
import javafx.animation.FadeTransition; // <-- IMPORT MỚI
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
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
    private void initialize() {
        buttonLogin.setOnAction(e -> handleLogin());

        // Chúng ta sẽ áp dụng hiệu ứng cho ảnh bên trong nút (trong FXML)
        // nên không cần gọi addClickAnimation(buttonLogin) ở đây nữa,
        // trừ khi bạn dùng Cách 1 (Nút đè lên Ảnh)
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

            // --- CODE MỚI: THÊM HIỆU ỨNG FADE IN ---

            // 1. Đặt nội dung menu thành trong suốt
            menuRoot.setOpacity(0);

            // 2. Tạo StackPane nền đen (để giữ kích thước 1200x955.5)
            StackPane centeringPane = new StackPane();
            centeringPane.setStyle("-fx-background-color: black;");
            centeringPane.getChildren().add(menuRoot);

            // 3. Tạo Scene mới
            Scene menuScene = new Scene(centeringPane, 1200, 955.5);

            // 4. ĐẶT SCENE MỚI LÊN SÂN KHẤU (QUAN TRỌNG)
            stage.setScene(menuScene);
            stage.setTitle("Arkanoid - Main Menu");

            // 5. Tạo hiệu ứng mờ dần (trong 0.5 giây)
            FadeTransition ft = new FadeTransition(Duration.millis(500), menuRoot);
            ft.setFromValue(0.0); // Bắt đầu từ trong suốt
            ft.setToValue(1.0);   // Kết thúc ở rõ nét
            ft.play(); // Chạy hiệu ứng

            // --- KẾT THÚC CODE MỚI ---

        } catch (IOException ex) {
            System.err.println("Lỗi: Không thể tải file main-menu.fxml.");
            ex.printStackTrace();
        }
    }

    // Hàm này có thể giữ lại nếu bạn dùng cho các nút khác,
    // nhưng hiện tại nó không được gọi vì nút Login đã dùng <graphic>
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