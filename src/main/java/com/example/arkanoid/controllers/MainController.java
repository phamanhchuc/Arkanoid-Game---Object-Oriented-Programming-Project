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

                gameManager.processInput(activeKeys); // Xử lý phím (hoặc bỏ qua nếu chuột di chuyển)
                gameManager.update(delta);

                // Chỉ render nếu game chưa dừng
                // (tránh lỗi nếu người dùng quay lại menu)
                if (gameManager.isRunning() || !gameManager.isGameOver()) {
                    gameManager.render(gc);
                }
            }
        };

        gameLoop.start();
        setupInputHandlers();
    }

    // --- CẬP NHẬT SETUPINPUTHANDLERS ---
    // Trong file MainController.java

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

        // --- CODE MỚI: LẮNG NGHE CLICK CHUỘT ĐỂ BẮT ĐẦU ---
        gameCanvas.setOnMousePressed(e -> {
            gameManager.startGame(); //
        });
        // --- KẾT THÚC CODE MỚI ---

        gameCanvas.requestFocus();
    }

    // (Hàm này có thể được gọi từ MenuController nếu bạn muốn dừng game)
    public void stopGameLoop() {
        gameLoop.stop();
    }
}