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

        // --- THAY ĐỔI Ở ĐÂY: Dùng getBounds() thay vì getVisualBounds() ---
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        gameCanvas.setWidth(screenBounds.getWidth());
        gameCanvas.setHeight(screenBounds.getHeight());

        // Khởi tạo GameManager với kích thước toàn màn hình
        gameManager = new GameManager((int) screenBounds.getWidth(), (int) screenBounds.getHeight(), GameData.playerName);

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
                gameManager.render(gc);
            }
        };

        gameLoop.start();
        setupInputHandlers();
    }

    private void setupInputHandlers() {
        gameCanvas.setFocusTraversable(true);
        gameCanvas.setOnKeyPressed(e -> activeKeys.add(e.getCode()));
        gameCanvas.setOnKeyReleased(e -> activeKeys.remove(e.getCode()));
        gameCanvas.requestFocus();
    }
}

