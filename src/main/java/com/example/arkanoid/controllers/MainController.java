package com.example.arkanoid.controllers;

import com.example.arkanoid.GameData;
import com.example.arkanoid.GameManager;
import com.example.arkanoid.MainApp; // Cần để quay về menu
import com.example.arkanoid.SoundManager; // Cần để dừng nhạc
import javafx.animation.AnimationTimer;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos; // Cần để quay về menu
import javafx.scene.Node; // Cần để quay về menu
import javafx.scene.Parent; // Cần để quay về menu
import javafx.scene.Scene; // Cần để quay về menu
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button; // <-- Import Button
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region; // Cần để quay về menu
import javafx.scene.layout.StackPane; // Cần để quay về menu
import javafx.scene.layout.VBox; // <-- Import VBox
import javafx.stage.Stage; // Cần để quay về menu
import javafx.util.Duration;

import javafx.scene.image.ImageView;

import java.io.IOException; // Cần để quay về menu
import java.util.HashSet;
import java.util.Set;

public class MainController {

    @FXML private Canvas gameCanvas;
    @FXML private VBox pausePane;
    @FXML private Button resumeButton;
    @FXML private Button restartButton;
    @FXML private Button quitButton;
    @FXML private ImageView pauseBackground;


    private GameManager gameManager;
    private AnimationTimer gameLoop;
    private Set<KeyCode> activeKeys = new HashSet<>();
    private boolean isPaused = false;

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

                // --- SỬA LỖI Ở ĐÂY ---
                // 1. Luôn xử lý input TRƯỚC TIÊN.
                // GameManager.processInput sẽ tự xử lý trường hợp gameOver.
                gameManager.processInput(activeKeys);

                // 2. Chỉ update logic game nếu KHÔNG bị pause.
                // GameManager.update sẽ tự xử lý trường hợp !running hoặc gameOver bên trong nó.
                if (!isPaused) {
                    gameManager.update(delta);
                }
                // --- KẾT THÚC SỬA ---

                // Luôn render
                gameManager.render(gc);
            }
        };

        gameLoop.start();
        setupInputHandlers();

        // GÁN SỰ KIỆN CHO CÁC NÚT PAUSE
        resumeButton.setOnAction(e -> togglePause(false));
        restartButton.setOnAction(e -> handleRestart());
        quitButton.setOnAction(e -> handleQuit());

        // Áp dụng hiệu ứng click cho nút pause (tùy chọn)
        addClickAnimation(resumeButton);
        addClickAnimation(restartButton);
        addClickAnimation(quitButton);
    }

    // --- HÀM setupInputHandlers ĐÃ SỬA ---
    private void setupInputHandlers() {
        gameCanvas.setFocusTraversable(true);

        gameCanvas.setOnKeyPressed(e -> {
            // Xử lý phím P trước tiên
            if (e.getCode() == KeyCode.P) {
                togglePause(!isPaused); // Đảo ngược trạng thái pause
            } else {
                // LUÔN thêm các phím khác vào activeKeys
                activeKeys.add(e.getCode());
                // Tắt mouse control chỉ khi game đang chạy và không pause
                if (!isPaused && gameManager.isRunning()) {
                    gameManager.setMouseControl(false);
                }
                // Giờ đây, khi gameOver=true, phím R/Space sẽ được thêm vào
                // activeKeys và được GameManager.processInput xử lý ở frame tiếp theo.
            }
        });

        gameCanvas.setOnKeyReleased(e -> {
            // Luôn xóa phím khi thả ra
            activeKeys.remove(e.getCode());
        });

        gameCanvas.setOnMouseMoved(e -> {
            // Chỉ xử lý di chuyển chuột nếu game không bị pause VÀ CHƯA KẾT THÚC
            if (!isPaused && !gameManager.isGameOver()) {
                gameManager.setMouseControl(true);
                gameManager.processMouseMovement(e.getX());
            }
        });

        gameCanvas.setOnMousePressed(e -> {
            // Chỉ xử lý click chuột để bắt đầu nếu game không bị pause VÀ CHƯA KẾT THÚC
            if (!isPaused && !gameManager.isGameOver()) {
                gameManager.startGame();
            }
        });

        gameCanvas.requestFocus();
    }
    // --- KẾT THÚC SỬA ---


    /**
     * Hàm để bật/tắt trạng thái Pause
     */
    private void togglePause(boolean pause) {
        if (gameManager.isGameOver()) return;

        isPaused = pause;
        if(pausePane != null) pausePane.setVisible(isPaused);
        if(pauseBackground != null) pauseBackground.setVisible(isPaused); // Hiện/ẩn hình nền pause

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


    /**
     * Xử lý khi nhấn nút Restart
     */
    private void handleRestart() {
        System.out.println("Restarting game...");
        if (isPaused) {
            togglePause(false); // Tắt menu pause nếu đang bật
        }
        gameManager.initGame(); // Khởi tạo lại game
        if (gameCanvas != null) gameCanvas.requestFocus(); // Focus lại canvas
    }

    /**
     * Xử lý khi nhấn nút Quit
     */
    private void handleQuit() {
        System.out.println("Quitting game...");
        stopGameLoop();
        SoundManager.stopMusic();

        try {
            Stage stage = (Stage) quitButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/arkanoid/main-menu.fxml"));
            Region menuRoot = loader.load();
            StackPane rootPane = new StackPane(menuRoot); // Cách viết gọn hơn
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