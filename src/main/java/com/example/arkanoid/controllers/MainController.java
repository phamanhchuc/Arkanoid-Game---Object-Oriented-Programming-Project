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

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import java.io.InputStream;

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

    @FXML private Text winText;

    private GameManager gameManager;
    private AnimationTimer gameLoop;
    private Set<KeyCode> activeKeys = new HashSet<>();
    private boolean isPaused = false;

    // --- SỬA BIẾN ---
    private Image storyImage2;
    private Image storyImage3;
    private Image storyImage4;
    private Image storyImage5;
    private Image storyImage6;
    private Font isabellaBodyFont;
    private Timeline currentWinTimeline;
    private boolean isWinSkipped = false;
    // --- KẾT THÚC SỬA ---

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

                // (Kiểm tra thắng trước khi update)
                if (gameManager.hasWonLevel()) {
                    // Dừng loop và gọi hàm xử lý cutscene
                    gameLoop.stop();
                    handleLevelWin(); // Hàm mới
                    return; // Không chạy update/render của frame này
                }

                gameManager.processInput(activeKeys);
                if (!isPaused) {
                    gameManager.update(delta);
                }
                gameManager.render(gc);
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

        // --- SỬA TẢI ẢNH VÀ FONT (DÙNG TÊN MỚI) ---
        try {
            // (Đảm bảo bạn đã đổi tên các file này thành .png hoặc .jpg cho đúng)
            storyImage2 = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/story_2.png"));
            storyImage3 = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/story_3.png"));
            storyImage4 = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/story_4.png"));
            storyImage5 = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/story_5.png"));
            storyImage6 = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/story_6.png"));

            // (Thêm kiểm tra null cho tất cả ảnh)
            if (storyImage2 == null) System.err.println("Lỗi: Không tìm thấy 'story_2.png'");
            if (storyImage3 == null) System.err.println("Lỗi: Không tìm thấy 'story_3.png'");
            if (storyImage4 == null) System.err.println("Lỗi: Không tìm thấy 'story_4.png'");
            if (storyImage5 == null) System.err.println("Lỗi: Không tìm thấy 'story_5.png'");
            if (storyImage6 == null) System.err.println("Lỗi: Không tìm thấy 'story_6.png'");

            // (Tải font)
            String fontPath = "/com/example/arkanoid/fonts/isabella.ttf";
            InputStream fontStreamBody = getClass().getResourceAsStream(fontPath);
            if (fontStreamBody != null) {
                isabellaBodyFont = Font.loadFont(fontStreamBody, 40.0);
            } else {
                System.err.println("Lỗi: Không tìm thấy file font 'isabella.ttf' tại: " + fontPath);
            }

        } catch (Exception e) {
            System.err.println("Lỗi nghiêm trọng khi tải ảnh cutscene hoặc font.");
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
            // (Đã sửa từ lần trước)
            if (!isPaused && !gameManager.isGameOver() && !gameManager.hasWonLevel() && !gameManager.isRunning()) {
                gameManager.startGame();
            }
        });
        gameCanvas.requestFocus();
    }


    private void togglePause(boolean pause) {
        if (gameManager.isGameOver() || gameManager.hasWonLevel()) return;
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


    // --- HÀM MỚI (THAY THẾ showWinScreen) ---
    private void handleLevelWin() {
        // Lấy màn vừa thắng (0-indexed)
        int levelWonIndex = gameManager.getCurrentLevelIndex();

        // Bảo GameManager chuẩn bị màn tiếp theo
        gameManager.nextLevel();

        // Kiểm tra xem đã thắng toàn bộ game chưa
        if (gameManager.hasWonGame()) {
            // Đã thắng màn 3 (màn cuối)
            runCutscene_WinLevel3(); // Chạy cutscene 3 (ảnh 6)
        }
        else if (levelWonIndex == 0) { // Vừa thắng màn 1
            runCutscene_WinLevel1(); // Chạy cutscene 1 (ảnh 2 + 3)
        }
        else if (levelWonIndex == 1) { // Vừa thắng màn 2
            runCutscene_WinLevel2(); // Chạy cutscene 2 (ảnh 4 + 5)
        }
        else {
            // Dự phòng: nếu có màn 4, 5... mà chưa định nghĩa cutscene
            loadNextLevelAndRestart();
        }
    }

    // --- (Hàm này là showWinScreen cũ, đã đổi tên) ---
    private void runCutscene_WinLevel1() {
        if (storyImage2 == null || storyImage3 == null || winImageView == null) {
            System.err.println("Lỗi ảnh 2 hoặc 3, tải màn tiếp theo ngay lập tức.");
            loadNextLevelAndRestart();
            return;
        }

        if (gameCanvas != null) gameCanvas.setVisible(false);
        isWinSkipped = false;

        if (isabellaBodyFont != null) winText.setFont(isabellaBodyFont);
        else winText.setFont(new Font("Arial", 40));

        winImageView.setOnMousePressed(event -> {
            if (isWinSkipped) return;
            isWinSkipped = true;
            System.out.println("Đã bỏ qua (skip) cutscene thắng!");
            if (currentWinTimeline != null) currentWinTimeline.stop();
            SoundManager.stopTypingLoop();
            loadNextLevelAndRestart();
        });
        winImageView.requestFocus();

        String scene1_Text = "Linh thạch sáng chói , bầu trời tỉnh dậy , mở mắt,.. ";
        String scene2_Text1 = "THÀNH PHỐ NỔI ACTHERIA\nCổng Trời mở ra\nKhông phải nơi Thượng đế ngự trị mà là công trình của những kẻ muốn thay thế ngài.";
        String scene2_Text2 = "Dưới bầu trời vàng Fim, bóng con muỗi khổng lồ uốn lượn như một vị thánh bị xiềng bằng thép. ConMel đứng lặng, nhìn đôi mắt vô hồn ấy, cảm thấy như bầu trời đang thở bằng nỗi đau của loài người.";

        Runnable onScene2Finished = () -> {
            PauseTransition pause = new PauseTransition(Duration.millis(3000));
            pause.setOnFinished(e -> {
                if(isWinSkipped) return;
                loadNextLevelAndRestart();
            });
            pause.play();
        };

        Runnable onScene1Finished = () -> {
            if(isWinSkipped) return;
            PauseTransition pause = new PauseTransition(Duration.millis(3000));
            pause.setOnFinished(e -> {
                if(isWinSkipped) return;
                winImageView.setImage(storyImage3);
                winText.setText("");
                winText.setVisible(true);
                winText.setTextAlignment(javafx.scene.text.TextAlignment.LEFT);
                winText.setLayoutX(50);
                winText.setLayoutY(100);
                startWinTypewriter(scene2_Text1, () -> {
                    PauseTransition pause2 = new PauseTransition(Duration.millis(1000));
                    pause2.setOnFinished(e2 -> {
                        if (isWinSkipped) return;
                        winText.setLayoutY(600);
                        startWinTypewriter(scene2_Text2, onScene2Finished);
                    });
                    pause2.play();
                });
            });
            pause.play();
        };

        System.out.println("Bắt đầu Cảnh 1 (Thắng màn 1)...");
        winImageView.setImage(storyImage2);
        winImageView.setVisible(true);
        winText.setText("");
        winText.setVisible(true);
        winText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        winText.setLayoutX(50);
        winText.setLayoutY(100);
        startWinTypewriter(scene1_Text, onScene1Finished);
    }

    // --- HÀM MỚI (cho Màn 2) ---
    private void runCutscene_WinLevel2() {
        if (storyImage4 == null || storyImage5 == null || winImageView == null) {
            System.err.println("Lỗi ảnh 4 hoặc 5, tải màn tiếp theo ngay.");
            loadNextLevelAndRestart();
            return;
        }
        if (gameCanvas != null) gameCanvas.setVisible(false);
        isWinSkipped = false;
        winText.setVisible(false); // Không có text

        winImageView.setOnMousePressed(event -> {
            if (isWinSkipped) return;
            isWinSkipped = true;
            loadNextLevelAndRestart(); // Tải màn 3
        });
        winImageView.requestFocus();

        // (Khi Cảnh 2 (ảnh 5) chạy xong)
        Runnable onScene2Finished = () -> {
            if(isWinSkipped) return;
            loadNextLevelAndRestart(); // Tải Màn 3
        };

        // (Khi Cảnh 1 (ảnh 4) chạy xong)
        Runnable onScene1Finished = () -> {
            if(isWinSkipped) return;
            winImageView.setImage(storyImage5); // Hiện ảnh 5
            PauseTransition pause = new PauseTransition(Duration.millis(3000)); // Chờ 3 giây
            pause.setOnFinished(e -> onScene2Finished.run());
            pause.play();
        };

        // Bắt đầu Cảnh 1 (Ảnh 4)
        System.out.println("Bắt đầu Cảnh 1 (Thắng màn 2)...");
        winImageView.setImage(storyImage4);
        winImageView.setVisible(true);
        PauseTransition pause = new PauseTransition(Duration.millis(3000)); // Chờ 3 giây
        pause.setOnFinished(e -> onScene1Finished.run());
        pause.play();
    }

    // --- HÀM MỚI (cho Màn 3 - Màn cuối) ---
    private void runCutscene_WinLevel3() {
        if (storyImage6 == null || winImageView == null) {
            System.err.println("Lỗi ảnh 6, quay về menu.");
            handleQuit(); // Thắng game, thoát về menu
            return;
        }
        if (gameCanvas != null) gameCanvas.setVisible(false);
        isWinSkipped = false;
        winText.setVisible(false); // Không có text

        winImageView.setOnMousePressed(event -> {
            if (isWinSkipped) return;
            isWinSkipped = true;
            handleQuit(); // Thoát
        });
        winImageView.requestFocus();

        // Bắt đầu Cảnh 1 (Ảnh 6)
        System.out.println("Bắt đầu Cảnh 1 (Thắng màn 3 - CUỐI)...");
        winImageView.setImage(storyImage6);
        winImageView.setVisible(true);

        PauseTransition pause = new PauseTransition(Duration.millis(5000)); // Chờ 5 giây
        pause.setOnFinished(e -> {
            if(isWinSkipped) return;
            handleQuit(); // Thoát về menu
        });
        pause.play();
    }


    private void startWinTypewriter(String fullText, Runnable onFinished) {
        final AtomicInteger charIndex = new AtomicInteger(0);
        winText.setText("");

        currentWinTimeline = new Timeline(new KeyFrame(Duration.millis(60), e -> {
            if (isWinSkipped) {
                currentWinTimeline.stop();
                return;
            }
            int index = charIndex.getAndIncrement();
            if (index < fullText.length()) {
                winText.setText(winText.getText() + fullText.charAt(index));
            } else {
                currentWinTimeline.stop();
                SoundManager.stopTypingLoop();
                if (onFinished != null) {
                    onFinished.run();
                }
            }
        }));

        currentWinTimeline.setCycleCount(fullText.length() + 1);
        SoundManager.startTypingLoop();
        currentWinTimeline.play();
    }


    private void loadNextLevelAndRestart() {
        // (Hàm này không reset gameManager, chỉ reset UI)
        restartGameAfterWin();
    }

    private void restartGameAfterWin() {
        if (winImageView != null) {
            winImageView.setVisible(false);
            winImageView.setOnMousePressed(null);
        }
        if (winText != null) {
            winText.setVisible(false);
        }

        if (gameCanvas != null) {
            gameCanvas.setVisible(true);
            gameCanvas.requestFocus();
        }
        if (gameLoop != null) {
            gameLoop.start();
        }
    }

    private void handleRestart() {
        System.out.println("Restarting game (về màn 1)...");
        if (isPaused) {
            togglePause(false);
        }

        if (currentWinTimeline != null) {
            currentWinTimeline.stop();
            SoundManager.stopTypingLoop();
        }
        if (winText != null) {
            winText.setVisible(false);
        }

        gameManager.initGame(); // Reset về màn 1
        restartGameAfterWin();
    }

    private void handleQuit() {
        System.out.println("Quitting game...");
        stopGameLoop();

        if (currentWinTimeline != null) {
            currentWinTimeline.stop();
        }
        SoundManager.shutdown();

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