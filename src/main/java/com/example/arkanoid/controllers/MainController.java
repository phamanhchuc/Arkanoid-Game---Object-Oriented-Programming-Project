package com.example.arkanoid.controllers;

import com.example.arkanoid.GameData;
import com.example.arkanoid.GameManager;
import com.example.arkanoid.MainApp;
import com.example.arkanoid.SoundManager;
import javafx.animation.AnimationTimer;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import javafx.scene.image.Image; // <-- Import
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class MainController {

    @FXML private Canvas gameCanvas;
    @FXML private VBox pausePane;
    @FXML private Button resumeButton;
    @FXML private Button restartButton;
    @FXML private Button quitButton;
    @FXML private ImageView pauseBackground;

    @FXML private ImageView winImageView;

    private GameManager gameManager;
    private AnimationTimer gameLoop;
    private Set<KeyCode> activeKeys = new HashSet<>();
    private boolean isPaused = false;

    private Image storyImage2;

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
                if (lastTime == 0) { lastTime = now; return; }
                double delta = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                gameManager.processInput(activeKeys);

                if (!isPaused) {
                    gameManager.update(delta);
                }

                gameManager.render(gc);

                if (gameManager.hasWonLevel()) {
                    showWinScreen();
                }
            }
        };

        gameLoop.start();
        setupInputHandlers();

        resumeButton.setOnAction(e -> togglePause(false));
        restartButton.setOnAction(e -> handleRestart());
        quitButton.setOnAction(e -> handleQuit());

        addClickAnimation(resumeButton);
        addClickAnimation(restartButton);
        addClickAnimation(quitButton);

        // --- SỬA LỖI ĐƯỜNG DẪN Ở ĐÂY ---
        try {
            // Lỗi ở dòng này: com.example... đã được sửa thành com/example...
            storyImage2 = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/ảnh 2.png"));
            if (storyImage2 == null || storyImage2.isError()) {
                System.err.println("Lỗi: Không tìm thấy 'ảnh 2.png' trong MainController.");
                storyImage2 = null;
            }
        } catch (Exception e) {
            System.err.println("Lỗi nghiêm trọng khi tải ảnh 2.");
            e.printStackTrace();
        }
        // --- KẾT THÚC SỬA ---
    }

    private void setupInputHandlers() {
        gameCanvas.setFocusTraversable(true);

        gameCanvas.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.P) {
                togglePause(!isPaused);
            } else {
                activeKeys.add(e.getCode());
                if (!isPaused && gameManager.isRunning()) {
                    gameManager.setMouseControl(false);
                }
            }
        });

        gameCanvas.setOnKeyReleased(e -> {
            activeKeys.remove(e.getCode());
        });

        gameCanvas.setOnMouseMoved(e -> {
            if (!isPaused && !gameManager.isGameOver()) {
                gameManager.setMouseControl(true);
                gameManager.processMouseMovement(e.getX());
            }
        });

        gameCanvas.setOnMousePressed(e -> {
            if (!isPaused && !gameManager.isGameOver()) {
                gameManager.startGame();
            }
        });

        gameCanvas.requestFocus();
    }


    private void togglePause(boolean pause) {
        if (gameManager.isGameOver()) return;

        isPaused = pause;
        if(pausePane != null) pausePane.setVisible(isPaused);
        if(pauseBackground != null) pauseBackground.setVisible(isPaused);

        if (isPaused) {
            gameManager.pauseGame();
            if(gameCanvas != null) gameCanvas.setOpacity(0.5);
            SoundManager.stopMusic();
        } else {
            gameManager.resumeGame();
            if(gameCanvas != null) gameCanvas.setOpacity(1.0);
            SoundManager.playMusic(SoundManager.Music.BACKGROUND_GAME);
            if(gameCanvas != null) gameCanvas.requestFocus();
        }
        System.out.println("Game Paused: " + isPaused);
    }


    private void showWinScreen() {
        if (gameLoop != null) {
            gameLoop.stop(); // Dừng game
        }

        if (storyImage2 != null && winImageView != null) {
            winImageView.setImage(storyImage2);
            winImageView.setVisible(true); // Hiện ảnh 2
            if (gameCanvas != null) {
                gameCanvas.setVisible(false); // Ẩn game
            }

            // Thêm sự kiện click để đi tiếp (ví dụ: quay về menu)
            winImageView.setOnMousePressed(event -> {
                System.out.println("Đã xem ảnh thắng. Quay về menu.");
                winImageView.setVisible(false);
                gameCanvas.setVisible(true); // Hiện lại canvas
                // (Quan trọng) Vô hiệu hóa sự kiện này để không bị lặp
                winImageView.setOnMousePressed(null);
                // Quay về menu (dùng lại hàm quit)
                handleQuit();
            });
            winImageView.requestFocus(); // Nhận focus

        } else {
            // Nếu không có ảnh, quay về menu luôn
            System.err.println("Không có ảnh 2, quay về menu.");
            handleQuit();
        }
    }

    private void handleRestart() {
        System.out.println("Restarting game...");
        if (isPaused) {
            togglePause(false);
        }

        if (winImageView != null) {
            winImageView.setVisible(false);
            winImageView.setOnMousePressed(null); // Xóa sự kiện click
        }
        if (gameCanvas != null) {
            gameCanvas.setVisible(true); // Hiện lại canvas
            gameCanvas.requestFocus();
        }

        gameManager.initGame(); // Khởi tạo lại game
        gameLoop.start(); // <-- PHẢI START LẠI GAMELOOP
    }

    private void handleQuit() {
        System.out.println("Quitting game...");
        stopGameLoop();
        SoundManager.stopMusic();

        try {
            Stage stage = (Stage) quitButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/arkanoid/main-menu.fxml"));
            Region menuRoot = loader.load();
            StackPane rootPane = new StackPane(menuRoot);
            rootPane.setStyle("-fx-background-color: black;");
            rootPane.setAlignment(Pos.CENTER);
            menuRoot.setMaxSize(MainApp.DESIGN_WIDTH, MainApp.DESIGN_HEIGHT);
            Scene menuScene = stage.getScene();
            menuScene.setRoot(rootPane);
            MainApp.scaleToFit(menuRoot, menuScene);
            stage.setTitle("Arkanoid - Main Menu");
        } catch (Exception ex) {
            System.err.println("Lỗi khi quay về main-menu.fxml: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void addClickAnimation(Button button) {
        if (button == null) return;
        ScaleTransition pressTransition = new ScaleTransition(Duration.millis(100), button);
        pressTransition.setToX(0.9);
        pressTransition.setToY(0.9);
        ScaleTransition releaseTransition = new ScaleTransition(Duration.millis(100), button);
        releaseTransition.setToX(1.0);
        releaseTransition.setToY(1.0);
        button.setOnMousePressed(event -> pressTransition.playFromStart());
        button.setOnMouseReleased(event -> releaseTransition.playFromStart());
    }

    public void stopGameLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }
}