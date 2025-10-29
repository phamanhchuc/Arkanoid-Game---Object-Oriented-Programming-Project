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
    // --- THÊM BIẾN FXML CHO PAUSE MENU ---
    @FXML private VBox pausePane;
    @FXML private Button resumeButton;
    @FXML private Button restartButton;
    @FXML private Button quitButton;
    @FXML private ImageView pauseBackground;


    private GameManager gameManager;
    private AnimationTimer gameLoop;
    private Set<KeyCode> activeKeys = new HashSet<>();

    // --- CỜ ĐỂ THEO DÕI TRẠNG THÁI PAUSE ---
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

                // Chỉ xử lý input và update game nếu KHÔNG bị pause VÀ game chưa kết thúc
                if (!isPaused && !gameManager.isGameOver()) {
                    gameManager.processInput(activeKeys);
                    gameManager.update(delta);
                }

                // Luôn render (để vẽ game hoặc vẽ menu pause đè lên)
                gameManager.render(gc);
            }
        };

        gameLoop.start();
        setupInputHandlers();

        // --- GÁN SỰ KIỆN CHO CÁC NÚT PAUSE ---
        resumeButton.setOnAction(e -> togglePause(false));
        restartButton.setOnAction(e -> handleRestart());
        quitButton.setOnAction(e -> handleQuit());

        // Áp dụng hiệu ứng click cho nút pause (tùy chọn)
        addClickAnimation(resumeButton);
        addClickAnimation(restartButton);
        addClickAnimation(quitButton);
    }

    private void setupInputHandlers() {
        gameCanvas.setFocusTraversable(true);

        gameCanvas.setOnKeyPressed(e -> {
            // --- XỬ LÝ PHÍM P ---
            if (e.getCode() == KeyCode.P) {
                togglePause(!isPaused); // Đảo ngược trạng thái pause
            } else {
                // Chỉ xử lý các phím khác nếu game không bị pause
                if (!isPaused) {
                    activeKeys.add(e.getCode());
                    gameManager.setMouseControl(false);
                }
            }
        });

        gameCanvas.setOnKeyReleased(e -> {
            // Luôn xóa phím khỏi activeKeys khi thả ra
            activeKeys.remove(e.getCode());
        });

        gameCanvas.setOnMouseMoved(e -> {
            // Chỉ xử lý di chuyển chuột nếu game không bị pause
            if (!isPaused) {
                gameManager.setMouseControl(true);
                gameManager.processMouseMovement(e.getX());
            }
        });

        gameCanvas.setOnMousePressed(e -> {
            // Chỉ xử lý click chuột để bắt đầu nếu game không bị pause
            if (!isPaused) {
                gameManager.startGame();
            }
        });

        gameCanvas.requestFocus();
    }

    /**
     * Hàm để bật/tắt trạng thái Pause
     * @param pause true để pause, false để resume
     */
    private void togglePause(boolean pause) {
        if (gameManager.isGameOver()) {
            return;
        }

        isPaused = pause;
        pausePane.setVisible(isPaused);
        pauseBackground.setVisible(isPaused); // Hiện/ẩn hình nền pause

        if (isPaused) {
            gameManager.pauseGame();
            gameCanvas.setOpacity(0.5);
            SoundManager.stopMusic();
        } else {
            gameManager.resumeGame();
            gameCanvas.setOpacity(1.0);
            SoundManager.playMusic(SoundManager.Music.BACKGROUND_GAME);
            gameCanvas.requestFocus();
        }
        System.out.println("Game Paused: " + isPaused);
    }


    /**
     * Xử lý khi nhấn nút Restart trong menu Pause
     */
    private void handleRestart() {
        System.out.println("Restarting game...");
        togglePause(false); // Tắt menu pause
        gameManager.initGame(); // Khởi tạo lại game từ đầu
        // Không cần gọi gameManager.startGame() vì bóng sẽ tự dính vào paddle
    }

    /**
     * Xử lý khi nhấn nút Quit trong menu Pause
     */
    private void handleQuit() {
        System.out.println("Quitting game...");
        stopGameLoop(); // Dừng vòng lặp game hiện tại
        SoundManager.stopMusic(); // Dừng hẳn nhạc nền

        try {
            Stage stage = (Stage) quitButton.getScene().getWindow();

            // Tải lại main-menu.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/arkanoid/main-menu.fxml"));
            Region menuRoot = loader.load(); // Dùng Region

            // Tạo StackPane bọc như khi chuyển từ Login
            StackPane rootPane = new StackPane();
            rootPane.getChildren().add(menuRoot);
            rootPane.setStyle("-fx-background-color: black;");
            rootPane.setAlignment(Pos.CENTER);

            menuRoot.setMaxSize(MainApp.DESIGN_WIDTH, MainApp.DESIGN_HEIGHT);

            // Lấy lại Scene và đặt root mới
            Scene menuScene = stage.getScene();
            menuScene.setRoot(rootPane);

            // Áp dụng lại co giãn
            MainApp.scaleToFit(menuRoot, menuScene);

            stage.setTitle("Arkanoid - Main Menu");

        } catch (IOException ex) {
            System.err.println("Lỗi: Không thể quay về main-menu.fxml.");
            ex.printStackTrace();
        } catch (Exception ex) {
            System.err.println("Lỗi không xác định khi quay về menu.");
            ex.printStackTrace();
        }
    }

    // --- HÀM ANIMATION CLICK (Tùy chọn cho nút Pause) ---
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