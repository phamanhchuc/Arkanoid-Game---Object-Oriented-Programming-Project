package com.example.arkanoid.controllers;

import com.example.arkanoid.core.GameData;
import com.example.arkanoid.core.GameManager;
import com.example.arkanoid.MainApp;
import com.example.arkanoid.core.SoundManager;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Đây là bộ não của gameplay. Nó chịu trách nhiệm:
 * 1. Chạy gameplay (game loop)
 * Vẽ game lên Canvas
 * Update ball/paddle/bricks
 * Nhận input từ người chơi
 * Kiểm tra thắng/thua
 *
 * 2. Điều khiển Pause Menu
 * Resume
 * Restart
 * Quit về Menu
 *
 * 3. Điều khiển Cutscene khi thắng (Level 1 → Level 2 → Level 3)
 * Hiển thị hình ảnh story
 * Chạy hiệu ứng typing text
 * Chạy video cinematic
 * Bấm skip
 *
 * 4. Chơi video cinematic
 * 5. Chuyển sang level mới hoặc quay về Main Menu
 */
public class MainController {

    // --- FXML INJECTIONS ---
    @FXML
    private Canvas gameCanvas; // nơi vẽ game (ball, paddle, bricks)
    @FXML
    private VBox pausePane; // bảng pause menu
    @FXML
    private Button resumeButton;
    @FXML
    private Button restartButton;
    @FXML
    private Button quitButton;
    @FXML
    private ImageView pauseBackground;
    @FXML
    private ImageView winImageView; // khung hiển thị cutscene/ story art

    @FXML
    private Text winText, winTitleText, winText1, winText2, winText3, winText4;
    // các đoạn text xuất hiện trong cutscene thắng
    private Text[] allWinTexts;

    // --- GAME LOGIC VARIABLES ---
    private GameManager gameManager; // lớp xử lý logic game (update, render, input, levels)
    private AnimationTimer gameLoop;
    private Set<KeyCode> activeKeys = new HashSet<>();
    // danh sách phím đang được nhấn cùng lúc (dùng cho di chuyển mượt)
    private boolean isPaused = false; // boolean kiểm tra trạng thái pause

    // --- RESOURCES & CUTSCENE VARIABLES ---
    private Image storyImage2, storyImage3, storyImage4, storyImage5, storyImage6;
    private Font isabellaBodyFont;

    private Timeline currentWinTimeline; // timeline typewriter đang chạy
    private boolean isWinSkipped = false;

    private Text currentTypingTarget;
    private String currentTypingContent;
    private Runnable currentTypingCallback;

    private MediaPlayer currentVideoPlayer;

    @FXML
    /**
     * Hàm khởi tạo gameplay khi màn Game được load.
     * Chức năng:
     * Lấy GraphicsContext để vẽ game.
     * Tạo GameManager.
     * Tạo vòng lặp game (AnimationTimer).
     * Gắn listener cho bàn phím + chuột.
     * Gắn sự kiện cho nút Resume / Restart / Quit.
     * Tải hình ảnh + font chữ cho cutscene.
     * Bắt đầu gameLoop.
     */
    private void initialize() {
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        gameCanvas.setWidth(1200);
        gameCanvas.setHeight(955.5);

        // GameManager chịu nhiệm vụ xử lý logic vật thể, va chạm, điểm, levels.
        // Truyền kích thước và tên người chơi vào GameManager
        gameManager = new GameManager(1200, 956, GameData.playerName);
        allWinTexts = new Text[]{winText, winTitleText, winText1, winText2, winText3, winText4};

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

                /**
                 Nếu level đã thắng (ví dụ tất cả bricks bị phá hoặc boss die):
                 dừng gameLoop: ngăn update/render tiếp (tránh tranh chấp trạng thái).
                 gọi handleLevelWin() để bật cutscene/qua level/ xử lý tiếp.
                 return để không thực hiện các bước sau.
                 */
                if (gameManager.hasWonLevel()) {
                    gameLoop.stop();
                    handleLevelWin();
                    return;
                }

                /**
                 * processInput xử lí:
                 * di chuyển paddle
                 * start ball
                 * trigger skill, powerup, v.v.
                 */
                gameManager.processInput(activeKeys);

                /**
                 * Chỉ update khi game không bị pause.
                 * GameManager.update(delta) sẽ:
                 * Di chuyển paddle, Di chuyển ball, Kiểm tra va chạm, Giảm máu brick,
                 * Thêm điểm, Kiểm tra rơi ball, Spawn powerups, Update hiệu ứng
                 */
                if (!isPaused) gameManager.update(delta);

                /**
                 * Dù pause hay không, render vẫn chạy.
                 * Có lý do:
                 * Khi pause, canvas mờ đi và hiển menu
                 * Nhưng game screen vẫn cần vẽ để thấy background bị mờ
                 * Render vẽ: Nền, Brick, Paddle, Ball, Particles, HUD (score, lives)
                 */
                gameManager.render(gc);
            }
        };

        gameLoop.start();
        setupInputHandlers();

        // Setup nút Pause Menu
        resumeButton.setOnAction(e -> togglePause(false));
        restartButton.setOnAction(e -> handleRestart());
        quitButton.setOnAction(e -> handleQuit());

        addClickAnimation(resumeButton);
        addClickAnimation(restartButton);
        addClickAnimation(quitButton);

        loadResources();
    }

    private void loadResources() {
        try {
            storyImage2 = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/story_2.png"));
            storyImage3 = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/story_3.png"));
            storyImage4 = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/story_4.png"));
            storyImage5 = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/story_5.png"));
            storyImage6 = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/story_6.png"));

            InputStream fontStream = getClass().getResourceAsStream("/com/example/arkanoid/fonts/isabella.ttf");
            if (fontStream != null) isabellaBodyFont = Font.loadFont(fontStream, 40.0);
        } catch (Exception e) {
            System.err.println("Lỗi tải resources: " + e.getMessage());
        }
    }

    // --- SỬA ĐỔI: CHUYỂN SANG ĐIỀU KHIỂN BẰNG PHÍM ---
    private void setupInputHandlers() {
        gameCanvas.setFocusTraversable(true);

        // Xử lý bàn phím
        gameCanvas.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.P) togglePause(!isPaused);
            else {
                activeKeys.add(e.getCode());
                // Đảm bảo tắt chế độ chuột khi người chơi bấm phím
                gameManager.setMouseControl(false);
            }
        });
        gameCanvas.setOnKeyReleased(e -> activeKeys.remove(e.getCode()));

        // --- ĐÃ VÔ HIỆU HÓA DI CHUYỂN BẰNG CHUỘT ---
        /*
        gameCanvas.setOnMouseMoved(e -> {
            if (!isPaused && !gameManager.isGameOver()) {
                gameManager.setMouseControl(true);
                gameManager.processMouseMovement(e.getX());
            }
        });
        */

        // Vẫn giữ click chuột để Bắt đầu game (Launch Ball) cho tiện
        gameCanvas.setOnMousePressed(e -> {
            if (!isPaused && !gameManager.isGameOver() && !gameManager.hasWonLevel() && !gameManager.isRunning()) {
                gameManager.startGame();
            }
        });
        gameCanvas.requestFocus();
    }

    /**
     * Đổi trạng thái Pause ↔ Resume của game.
     * Chức năng:
     * Tạm dừng hoặc tiếp tục game.
     * Giảm độ sáng canvas khi pause.
     * Hiện/ẩn Pause Menu.
     * Bật/tắt nhạc nền tương ứng.
     */
    private void togglePause(boolean pause) {
        if (gameManager.isGameOver() || gameManager.hasWonLevel()) return;
        isPaused = pause;
        pausePane.setVisible(isPaused);
        pauseBackground.setVisible(isPaused);

        if (isPaused) {
            gameManager.pauseGame();
            gameCanvas.setOpacity(0.5);
            SoundManager.stopMusic();
        } else {
            gameManager.resumeGame();
            gameCanvas.setOpacity(1.0);
            gameCanvas.requestFocus();
        }
    }

    // --- VIDEO PLAYER ---

    /**
     * Phát video cinematic trong cutscene.
     * Chức năng:
     * Tạo MediaPlayer để phát video.
     * Hiển thị video đè lên giao diện game.
     * Cho phép click chuột để skip.
     * Khi video kết thúc → gọi callback onFinished.
     * Tự dọn dẹp video khỏi giao diện.
     */
    private void playVideo(String videoName, Runnable onFinished) {
        try {
            String path = "/com/example/arkanoid/videos/" + videoName;
            Media media = new Media(getClass().getResource(path).toExternalForm());
            if (currentVideoPlayer != null) currentVideoPlayer.dispose();

            currentVideoPlayer = new MediaPlayer(media);
            MediaView mediaView = new MediaView(currentVideoPlayer);
            mediaView.setFitWidth(1280);
            mediaView.setFitHeight(800);
            mediaView.setPreserveRatio(false);

            StackPane overlayPane = new StackPane(mediaView);
            overlayPane.setAlignment(Pos.CENTER);
            overlayPane.setStyle("-fx-background-color: black;");

            Pane root = (Pane) winImageView.getScene().getRoot();
            root.getChildren().add(overlayPane);

            Runnable cleanup = () -> {
                currentVideoPlayer.stop();
                root.getChildren().remove(overlayPane);
                currentVideoPlayer.dispose();
                currentVideoPlayer = null;
                if (onFinished != null) onFinished.run();
            };

            currentVideoPlayer.setOnEndOfMedia(cleanup); // khi video phát xong, gọi cleanup
            currentVideoPlayer.setOnError(() -> {
                System.err.println("Lỗi video: " + videoName);
                cleanup.run();
            });
            overlayPane.setOnMousePressed(e -> cleanup.run()); // click -> skip video
            currentVideoPlayer.play();

        } catch (Exception e) {
            e.printStackTrace();
            if (onFinished != null) onFinished.run();
        }
    }

    private void playVideoMan2(Runnable onFinished) {
        playVideo("man2.mp4", onFinished);
    }

    private void playVideoMan3(Runnable onFinished) {
        playVideo("man3.mp4", onFinished);
    }

    private void playVideoKetCuoi(Runnable onFinished) {
        playVideo("ketcuoi.mp4", onFinished);
    }

    private void playVideoAfterCredit(Runnable onFinished) {
        playVideo("aftercredit.mp4", onFinished);
    }

    // --- LEVEL WIN ---
    /**
     * Xử lý khi người chơi thắng một level.
     * Chức năng:
     * Dừng gameLoop.
     * Tăng level.
     * Lựa chọn cutscene phù hợp:
     * Level 1 → cutscene 1
     * Level 2 → cutscene 2
     * Level 3 → cutscene ending
     * Nếu không có cutscene → load level mới.
     */
    private void handleLevelWin() {
        activeKeys.clear();
        SoundManager.stopMusic();
        int levelWonIndex = gameManager.getCurrentLevelIndex();
        gameManager.nextLevel();

        winImageView.setOnMousePressed(event -> skipCurrentTextStep());
        winImageView.requestFocus();

        if (gameManager.hasWonGame()) runCutscene_WinLevel3();
        else if (levelWonIndex == 0) runCutscene_WinLevel1();
        else if (levelWonIndex == 1) runCutscene_WinLevel2();
        else loadNextLevelAndRestart();
    }

    /**
     * Bỏ qua phần text đang chạy.
     * Chức năng:
     * Dừng typewriter timeline.
     * Hiện toàn bộ câu ngay lập tức.
     * Chạy tiếp cutscene.
     */
    private void skipCurrentTextStep() {
        if (currentWinTimeline != null && currentWinTimeline.getStatus() == Timeline.Status.RUNNING) {
            currentWinTimeline.stop();
            SoundManager.stopTypingLoop();
            if (currentTypingTarget != null && currentTypingContent != null)
                currentTypingTarget.setText(currentTypingContent);
            if (currentTypingCallback != null) {
                Runnable cb = currentTypingCallback;
                currentTypingCallback = null;
                cb.run();
            }
        }
    }

    // --- CUTSCENES ---
    private void runCutscene_WinLevel1() {
        prepareCutsceneUI(); // tắt game canvas, reset state cutscene
        winImageView.setImage(storyImage2);
        winImageView.setVisible(true);

        winText.setFont(isabellaBodyFont != null ? Font.font(isabellaBodyFont.getFamily(), 60) : new Font("Arial", 55));
        winText.setStyle("-fx-fill: white; -fx-stroke: #723f3f; -fx-stroke-width: 1.5;");
        winText.setTextAlignment(TextAlignment.CENTER);
        winText.setLayoutX(50);
        winText.setLayoutY(100);
        winText.setVisible(true);

        String t1 = "Linh thạch sáng chói. Bầu trời tỉnh dậy, mở mắt,...";
        String t2 = "THÀNH PHỐ NỔI AETHERION";
        String t3 = "Cổng Trời mở ra, \nkhông phải nơi Thượng Đế \nngự trị, mà là công trình của \nnhững kẻ muốn thay thế Ngài.";
        String t4 = "Dưới bầu trời vàng kim, bóng con muỗi khổng lồ uốn lượn như một vị thánh bị xiềng bằng thép. ConMel đứng lặng, nhìn đôi mắt vô hồn ấy, cảm thấy như bầu trời đang thở bằng nỗi đau của loài người.";

        startWinTypewriter(winText, t1, () -> {
            if (isWinSkipped) return;
            playVideoMan2(() -> {
                if (isWinSkipped) return;
                winImageView.setImage(storyImage3);
                winText.setVisible(false);

                winTitleText.setFont(isabellaBodyFont != null ? Font.font(isabellaBodyFont.getFamily(), 70) : new Font("Arial", 70));
                winTitleText.setTextAlignment(TextAlignment.CENTER);
                winTitleText.setVisible(true);

                startWinTypewriter(winTitleText, t2, () -> {
                    delay(1000, () -> {
                        if (isWinSkipped) return;
                        winText1.setFont(isabellaBodyFont != null ? Font.font(isabellaBodyFont.getFamily(), 50) : new Font("Arial", 42));
                        winText1.setTextAlignment(TextAlignment.LEFT);
                        winText1.setVisible(true);

                        startWinTypewriter(winText1, t3, () -> {
                            delay(800, () -> {
                                if (isWinSkipped) return;
                                winText2.setFont(isabellaBodyFont != null ? Font.font(isabellaBodyFont.getFamily(), 50) : new Font("Arial", 38));
                                winText2.setTextAlignment(TextAlignment.LEFT);
                                winText2.setVisible(true);
                                startWinTypewriter(winText2, t4, () -> delay(3000, this::loadNextLevelAndRestart));
                            });
                        });
                    });
                });
            });
        });
    }

    private void runCutscene_WinLevel2() {
        prepareCutsceneUI();
        winImageView.setImage(storyImage4);
        winImageView.setVisible(true);

        String t1 = "Ánh sáng vỡ tung, Cổng Trời sụp đổ trong \ntiếng cầu nguyện bị xé vụn.";
        String t2 = "ConMel với tay về phía AnhChuc, \nnhưng chỉ còn khoảng không nuốt lấy anh.";
        String t3 = "Trong tro sáng còn lại, \nQuanBew mỉm cười lặng lẽ. \nThiên Đàng cuối cùng đã mở ra.";
        String t4 = "Cổng Trời không khép lại - nó nở rộ như \nmột vết thương. Từ trái tim bị hiến tế \ncủa AnhChuc, bầu trời rách toạc, \nvà máu hóa thành lửa.";
        String t5 = "QuanBew quan sát đống đổ nát, thì thầm lời cuối cùng của nghi lễ rồi mỉm cười trước vị thần mà hắn vừa sinh ra.\nTừ đó, Địa Ngục bắt đầu treo giữa trời.";

        delay(2000, () -> {
            if (isWinSkipped) return;
            winText1.setFont(isabellaBodyFont != null ? Font.font(isabellaBodyFont.getFamily(), 48) : new Font("Arial", 45));
            winText1.setStyle("-fx-fill: white; -fx-stroke: #bd7244; -fx-stroke-width: 1.5;");
            winText1.setLayoutX(80);
            winText1.setLayoutY(150);
            winText1.setWrappingWidth(700);
            winText1.setTextAlignment(TextAlignment.LEFT);
            winText1.setVisible(true);

            startWinTypewriter(winText1, t1, () -> {
                delay(1000, () -> {
                    if (isWinSkipped) return;
                    winText2.setFont(isabellaBodyFont != null ? Font.font(isabellaBodyFont.getFamily(), 46) : new Font("Arial", 43));
                    winText2.setStyle("-fx-fill: white; -fx-stroke: #821010; -fx-stroke-width: 1.5;");
                    winText2.setLayoutX(50);
                    winText2.setLayoutY(600);
                    winText2.setWrappingWidth(700);
                    winText2.setTextAlignment(TextAlignment.LEFT);
                    winText2.setVisible(true);

                    startWinTypewriter(winText2, t2, () -> {
                        delay(800, () -> {
                            if (isWinSkipped) return;
                            winText3.setFont(isabellaBodyFont != null ? Font.font(isabellaBodyFont.getFamily(), 44) : new Font("Arial", 41));
                            winText3.setStyle("-fx-fill: white; -fx-stroke: #864c39; -fx-stroke-width: 1.5;");
                            winText3.setLayoutX(750);
                            winText3.setLayoutY(750);
                            winText3.setWrappingWidth(450);
                            winText3.setTextAlignment(TextAlignment.LEFT);
                            winText3.setVisible(true);

                            startWinTypewriter(winText3, t3, () -> {
                                playVideoMan3(() -> {
                                    if (isWinSkipped) return;
                                    winImageView.setImage(storyImage5);
                                    winText1.setVisible(false);
                                    winText2.setVisible(false);
                                    winText3.setVisible(false);

                                    delay(200, () -> {
                                        winText1.setFont(isabellaBodyFont != null ? Font.font(isabellaBodyFont.getFamily(), 48) : new Font("Arial", 45));
                                        winText1.setStyle("-fx-fill: white; -fx-stroke: #bd7244; -fx-stroke-width: 1.5;");
                                        winText1.setLayoutX(50);
                                        winText1.setLayoutY(100);
                                        winText1.setWrappingWidth(800);
                                        winText1.setVisible(true);

                                        startWinTypewriter(winText1, t4, () -> {
                                            delay(1000, () -> {
                                                winText2.setFont(isabellaBodyFont != null ? Font.font(isabellaBodyFont.getFamily(), 46) : new Font("Arial", 43));
                                                winText2.setStyle("-fx-fill: white; -fx-stroke: #864c39; -fx-stroke-width: 1.5;");
                                                winText2.setLayoutX(600);
                                                winText2.setLayoutY(650);
                                                winText2.setWrappingWidth(550);
                                                winText2.setVisible(true);
                                                startWinTypewriter(winText2, t5, () -> delay(3000, this::loadNextLevelAndRestart));
                                            });
                                        });
                                    });
                                });
                            });
                        });
                    });
                });
            });
        });
    }

    private void runCutscene_WinLevel3() {
        prepareCutsceneUI();
        winImageView.setImage(storyImage6);
        winImageView.setVisible(true);

        String t1 = "Ánh sáng cuối cùng bùng nổ, xé tan cả Thiên Đàng lẫn Địa Ngục. Bạo Chúa Nem Chua gào thét, và bầu trời sụp đổ cùng nhịp tim nhân loại. ConMel tan vào luồng sáng của lời hứa cuối cùng.";
        String t2 = "Trên mặt đất nứt vỡ, một bàn tay gãy nát nằm lại giữa đống tro tàn - như dấu chấm hết cho giấc mơ của nhân loại.";

        delay(300, () -> {
            if (isWinSkipped) return;
            winText3.setFont(isabellaBodyFont != null ? Font.font(isabellaBodyFont.getFamily(), 50) : new Font("Arial", 45));
            winText3.setStyle("-fx-fill: white; -fx-stroke: #520d0d; -fx-stroke-width: 1.5;");
            winText3.setLayoutX(50);
            winText3.setLayoutY(100);
            winText3.setWrappingWidth(750);
            winText3.setTextAlignment(TextAlignment.LEFT);
            winText3.setVisible(true);

            startWinTypewriter(winText3, t1, () -> {
                delay(1000, () -> {
                    if (isWinSkipped) return;
                    winText4.setFont(isabellaBodyFont != null ? Font.font(isabellaBodyFont.getFamily(), 50) : new Font("Arial", 42));
                    winText4.setStyle("-fx-fill: white; -fx-stroke: #520d0d; -fx-stroke-width: 1.5;");
                    winText4.setLayoutX(760);
                    winText4.setLayoutY(600);
                    winText4.setWrappingWidth(450);
                    winText4.setTextAlignment(TextAlignment.LEFT);
                    winText4.setVisible(true);

                    startWinTypewriter(winText4, t2, () -> {
                        playVideoKetCuoi(() -> playVideoAfterCredit(() -> {
                            if (isWinSkipped) return;
                            handleQuit();
                        }));
                    });
                });
            });
        });
    }

    private void prepareCutsceneUI() {
        gameCanvas.setVisible(false);
        isWinSkipped = false;
        for (Text t : allWinTexts) if (t != null) t.setVisible(false);
    }

    private void delay(int millis, Runnable action) {
        PauseTransition p = new PauseTransition(Duration.millis(millis));
        p.setOnFinished(e -> action.run());
        p.play();
    }

    private void startWinTypewriter(Text target, String content, Runnable onFinished) {
        if (target == null || content == null) {
            if (onFinished != null) onFinished.run();
            return;
        }

        target.setText("");
        if (currentWinTimeline != null) currentWinTimeline.stop();

        currentTypingTarget = target;
        currentTypingContent = content;
        currentTypingCallback = onFinished;

        final int[] index = {0};
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(60), evt -> {
            if (isWinSkipped) {
                timeline.stop();
                return;
            }
            if (index[0] < content.length()) target.setText(target.getText() + content.charAt(index[0]++));
            else {
                timeline.stop();
                SoundManager.stopTypingLoop();
                currentTypingTarget = null;
                if (onFinished != null) onFinished.run();
            }
        }));
        timeline.setCycleCount(content.length() + 1);
        currentWinTimeline = timeline;
        SoundManager.startTypingLoop();
        timeline.play();
    }

    /**
     * Dùng sau khi kết thúc cutscene hoặc thắng level.
     * Chức năng:
     * Reset trạng thái game.
     * Gọi gameManager.initGame().
     * Bật lại gameLoop và chơi level mới.
     */
    private void loadNextLevelAndRestart() {
        isWinSkipped = true;
        if (currentWinTimeline != null) currentWinTimeline.stop();
        restartGameAfterWin();
    }

    private void restartGameAfterWin() {
        winImageView.setVisible(false);
        winImageView.setOnMousePressed(null);
        for (Text t : allWinTexts)
            if (t != null) {
                t.setText("");
                t.setVisible(false);
            }

        gameCanvas.setVisible(true);
        gameCanvas.requestFocus();

        int levelIndex = gameManager.getCurrentLevelIndex();
        if (!gameManager.hasWonGame()) {
            if (levelIndex == 1) SoundManager.playMusic(SoundManager.Music.LEVEL2);
            else if (levelIndex == 2) SoundManager.playMusic(SoundManager.Music.LEVEL3);
        }
        gameLoop.start();
    }

    /**
     * Restart lại level hiện tại.
     * Chức năng:
     * Xóa trạng thái pause.
     * Reset GameManager.
     * Bắt đầu lại gameLoop.
     */
    private void handleRestart() {
        if (isPaused) togglePause(false);
        cleanupCutscenes();
        gameManager.initGame();
        restartGameAfterWin();
    }

    /**
     * Thoát về màn Main Menu.
     * Chức năng:
     * Load lại file main-menu.fxml.
     * Dừng gameLoop.
     * Trở về menu.
     */
    private void handleQuit() {
        stopGameLoop();
        cleanupCutscenes();
        SoundManager.shutdown();
        if (currentVideoPlayer != null) currentVideoPlayer.dispose();

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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void cleanupCutscenes() {
        if (currentWinTimeline != null) currentWinTimeline.stop();
        SoundManager.stopTypingLoop();
        isWinSkipped = true;
        currentTypingTarget = null;
        for (Text t : allWinTexts) if (t != null) t.setVisible(false);
    }

    private void addClickAnimation(Button button) {
        if (button == null) return;
        ScaleTransition press = new ScaleTransition(Duration.millis(100), button);
        press.setToX(0.9);
        press.setToY(0.9);
        ScaleTransition release = new ScaleTransition(Duration.millis(100), button);
        release.setToX(1.0);
        release.setToY(1.0);
        button.setOnMousePressed(e -> press.playFromStart());
        button.setOnMouseReleased(e -> release.playFromStart());
    }

    public void stopGameLoop() {
        if (gameLoop != null) gameLoop.stop();
    }
}