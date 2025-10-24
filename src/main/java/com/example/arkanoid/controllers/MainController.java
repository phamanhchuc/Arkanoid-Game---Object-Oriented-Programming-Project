package com.example.arkanoid.controllers;

import com.example.arkanoid.GameData;
import com.example.arkanoid.GameManager;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.stage.Screen;

import java.util.HashSet;
import java.util.Set;

public class MainController {

    @FXML
    private Canvas gameCanvas;

    private GameManager gameManager;
    private AnimationTimer gameLoop;

    private Set<KeyCode> activeKeys = new HashSet<>();

    @FXML
    private void initialize() {
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();

        // Kích thước của Canvas (khớp với FXML)
        gameCanvas.setWidth(1200);
        gameCanvas.setHeight(955.5);

        gameManager = new GameManager(1200, 956, GameData.playerName);

        gameLoop = new AnimationTimer() {
            private long lastTime = 0;

            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }
                double delta = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                gameManager.processInput(activeKeys);
                gameManager.update(delta);

                // --- SỬA LỖI Ở ĐÂY ---
                // Xóa bỏ câu lệnh 'if' sai.
                // Luôn luôn gọi render() để GameManager tự quyết định
                // vẽ game đang chạy hay vẽ màn hình "GAME OVER".
                gameManager.render(gc);
                // --- KẾT THÚC SỬA LỖI ---
            }
        };

        gameLoop.start();
        setupInputHandlers();
    }

    private void setupInputHandlers() {
        gameCanvas.setFocusTraversable(true);

        // Khi nhấn phím, tắt chế độ chuột
        gameCanvas.setOnKeyPressed(e -> {
            activeKeys.add(e.getCode());
            gameManager.setMouseControl(false);
        });

        gameCanvas.setOnKeyReleased(e -> activeKeys.remove(e.getCode()));

        // Lắng nghe di chuyển chuột
        gameCanvas.setOnMouseMoved(e -> {
            gameManager.setMouseControl(true); // Bật chế độ chuột
            gameManager.processMouseMovement(e.getX()); // Gửi tọa độ X
        });

        // Lắng nghe click chuột để bắt đầu
        gameCanvas.setOnMousePressed(e -> {
            gameManager.startGame();
        });

        // Đảm bảo canvas được focus để nhận phím
        gameCanvas.requestFocus();
    }

    /**
     * Phương thức này có thể được gọi từ MenuController
     * nếu bạn muốn dừng game khi quay lại Menu (để tối ưu)
     */
    public void stopGameLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }
}