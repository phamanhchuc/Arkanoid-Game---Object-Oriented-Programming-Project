package com.example.arkanoid.controllers;

import com.example.arkanoid.GameData;
import com.example.arkanoid.GameManager;
import com.example.arkanoid.MainApp;
import com.example.arkanoid.SoundManager;
import javafx.animation.AnimationTimer;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
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
import javafx.scene.layout.Pane;
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

    // --- CÁC BIẾN THÊM ---
    private Image storyImage2;
    private Image storyImage3;
    private Image storyImage4;
    private Image storyImage5;
    private Image storyImage6;
    private Font isabellaBodyFont;
    private Timeline currentWinTimeline;
    private boolean isWinSkipped = false;

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

                if (gameManager.hasWonLevel()) {
                    gameLoop.stop();
                    handleLevelWin();
                    return;
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

        try {
            storyImage2 = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/story_2.png"));
            storyImage3 = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/story_3.png"));
            storyImage4 = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/story_4.png"));
            storyImage5 = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/story_5.png"));
            storyImage6 = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/story_6.png"));

            String fontPath = "/com/example/arkanoid/fonts/isabella.ttf";
            InputStream fontStreamBody = getClass().getResourceAsStream(fontPath);
            if (fontStreamBody != null) {
                isabellaBodyFont = Font.loadFont(fontStreamBody, 40.0);
            } else {
                System.err.println("Lỗi: Không tìm thấy font " + fontPath);
            }

        } catch (Exception e) {
            System.err.println("Lỗi khi tải ảnh hoặc font:");
            e.printStackTrace();
        }
    }

    private void setupInputHandlers() {
        gameCanvas.setFocusTraversable(true);
        gameCanvas.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.P) togglePause(!isPaused);
            else activeKeys.add(e.getCode());
        });
        gameCanvas.setOnKeyReleased(e -> activeKeys.remove(e.getCode()));
        gameCanvas.setOnMouseMoved(e -> {
            if (!isPaused && !gameManager.isGameOver()) {
                gameManager.setMouseControl(true);
                gameManager.processMouseMovement(e.getX());
            }
        });
        gameCanvas.setOnMousePressed(e -> {
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
    }

    private void handleLevelWin() {
        int levelWonIndex = gameManager.getCurrentLevelIndex();
        gameManager.nextLevel();

        if (gameManager.hasWonGame()) runCutscene_WinLevel3();
        else if (levelWonIndex == 0) runCutscene_WinLevel1();
        else if (levelWonIndex == 1) runCutscene_WinLevel2();
        else loadNextLevelAndRestart();
    }

    private void runCutscene_WinLevel1() {
        if (storyImage2 == null || storyImage3 == null || winImageView == null) {
            System.err.println("Thiếu ảnh cutscene 2 hoặc 3");
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
            if (currentWinTimeline != null) currentWinTimeline.stop();
            SoundManager.stopTypingLoop();
            loadNextLevelAndRestart();
        });

        String scene1_Text = "Linh thạch sáng chói. Bầu trời tỉnh dậy, mở mắt,... ";
        String scene2_Text1 = "THÀNH PHỐ NỔI AETHERION";
        String scene2_Text2 = "Cổng Trời mở ra, \nkhông phải nơi Thượng Đế \nngự trị, mà là công trình của \nnhững kẻ muốn thay thế Ngài.";
        String scene2_Text3 = "Dưới bầu trời vàng kim, bóng con muỗi khổng lồ uốn lượn như một vị thánh bị xiềng bằng thép. ConMel đứng lặng, nhìn đôi mắt vô hồn ấy, cảm thấy như bầu trời đang thở bằng nỗi đau của loài người.";

        Runnable onScene2Finished = () -> {
            PauseTransition pause = new PauseTransition(Duration.millis(3000));
            pause.setOnFinished(e -> {
                if (isWinSkipped) return;

                // 🧹 DỌN DẸP CÁC TEXT SAU CUTSCENE
                Platform.runLater(() -> {
                    if (winText != null && winText.getParent() instanceof Pane parent) {
                        parent.getChildren().removeIf(node ->
                                node instanceof Text &&
                                        node != winText // không xóa text gốc nếu bạn cần dùng lại
                        );
                    }
                });

                loadNextLevelAndRestart();
            });
            pause.play();
        };

        Runnable onScene1Finished = () -> {
            if (isWinSkipped) return;
            PauseTransition pause = new PauseTransition(Duration.millis(3000));
            pause.setOnFinished(e -> {
                if (isWinSkipped) return;

                winImageView.setImage(storyImage3);
                winText.setVisible(true);
                winText.setText("");

                Pane parent = (Pane) winText.getParent();

                Text winTitleText = new Text();
                winTitleText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
                winTitleText.setLayoutX(0);
                winTitleText.setLayoutY(90);
                winTitleText.setWrappingWidth(1200);
                winTitleText.setStyle("-fx-fill: white; -fx-stroke: #bd7244; -fx-stroke-width: 2;");
                winTitleText.setFont(isabellaBodyFont != null ? Font.font(isabellaBodyFont.getFamily(), 70) : new Font("Arial", 70));
                parent.getChildren().add(winTitleText);

                startWinTypewriter(winTitleText, scene2_Text1, () -> {
                    PauseTransition pause2 = new PauseTransition(Duration.millis(1000));
                    pause2.setOnFinished(e2 -> {
                        if (isWinSkipped) return;

                        Text winText1 = new Text();
                        winText1.setVisible(true);
                        winText1.setTextAlignment(javafx.scene.text.TextAlignment.LEFT);
                        winText1.setLayoutX(50);
                        winText1.setLayoutY(200);
                        winText1.setWrappingWidth(800);
                        winText1.setStyle("-fx-fill: white; -fx-stroke: #520d0d; -fx-stroke-width: 1.5;");
                        winText1.setFont(isabellaBodyFont != null ? Font.font(isabellaBodyFont.getFamily(), 50) : new Font("Arial", 42));
                        parent.getChildren().add(winText1);

                        startWinTypewriter(winText1, scene2_Text2, () -> {
                            PauseTransition pause3 = new PauseTransition(Duration.millis(800));
                            pause3.setOnFinished(e3 -> {
                                if (isWinSkipped) return;

                                Text winText2 = new Text();
                                winText2.setVisible(true);
                                winText2.setTextAlignment(javafx.scene.text.TextAlignment.LEFT);
                                winText2.setLayoutX(300);
                                winText2.setLayoutY(650);
                                winText2.setWrappingWidth(900);
                                winText2.setStyle("-fx-fill: white; -fx-stroke: #520d0d; -fx-stroke-width: 1.5;");
                                winText2.setFont(isabellaBodyFont != null ? Font.font(isabellaBodyFont.getFamily(), 50) : new Font("Arial", 38));
                                parent.getChildren().add(winText2);

                                startWinTypewriter(winText2, scene2_Text3, onScene2Finished);
                            });
                            pause3.play();
                        });
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
        winText.setStyle("-fx-fill: white; -fx-stroke: #723f3f; -fx-stroke-width: 1.5;");
        winText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        winText.setLayoutX(50);
        winText.setLayoutY(100);
        winText.setFont(isabellaBodyFont != null ? Font.font(isabellaBodyFont.getFamily(), 60) : new Font("Arial", 55));
        startWinTypewriter(scene1_Text, onScene1Finished);
    }

    private void runCutscene_WinLevel2() {
        if (storyImage4 == null || storyImage5 == null) {
            System.err.println("Thiếu ảnh cutscene 4 hoặc 5");
            loadNextLevelAndRestart();
            return;
        }

        if (gameCanvas != null) gameCanvas.setVisible(false);
        isWinSkipped = false;
        winText.setVisible(false);

        winImageView.setOnMousePressed(event -> {
            if (isWinSkipped) return;
            isWinSkipped = true;

            // Dọn dẹp text khi skip
            Platform.runLater(() -> {
                if (winText != null && winText.getParent() instanceof Pane parent) {
                    parent.getChildren().removeIf(node ->
                            node instanceof Text && node != winText
                    );
                }
            });

            if (currentWinTimeline != null) currentWinTimeline.stop();
            SoundManager.stopTypingLoop();
            loadNextLevelAndRestart();
        });

        // ---------- NỘI DUNG TEXT ----------
        // ending scene (storyImage4)
        String scene2_Text3 = "Ánh sáng vỡ tung, Cổng Trời sụp đổ trong \ntiếng cầu nguyện bị xé vụn.";
        String scene2_Text4 = "ConMel với tay về phía AnhChuc, \nnhưng chỉ còn khoảng không nuốt lấy anh.";
        String scene2_Text5 = "Trong tro sáng còn lại, \nQuanBew mỉm cười lặng lẽ. \nThiên Đàng cuối cùng đã mở ra.";

        // opening scene (storyImage5)
        String scene3_Text1 = "Cổng Trời không khép lại - nó nở rộ như \nmột vết thương. " +
                "Từ trái tim bị hiến tế \ncủa AnhChuc, bầu trời rách toạc, \nvà máu hóa thành lửa.";
        String scene3_Text2 = "QuanBew quan sát đống đổ nát, thì thầm lời cuối cùng của nghi lễ rồi mỉm cười trước vị thần mà hắn vừa sinh ra.\n" +
                "Từ đó, Địa Ngục bắt đầu treo giữa trời.";

        // 🧹 Dọn text helper
        Runnable clearTexts = () -> Platform.runLater(() -> {
            if (winText != null && winText.getParent() instanceof Pane p) {
                p.getChildren().removeIf(node -> node instanceof Text && node != winText);
            }
        });

        // ---------- Khi kết thúc toàn bộ cutscene ----------
        Runnable onAllFinished = () -> {
            if (isWinSkipped) return;
            clearTexts.run();
            loadNextLevelAndRestart();
        };

        // ---------- Cảnh mở đầu (storyImage5) ----------
        Runnable playScene3 = () -> {
            if (isWinSkipped) return;

            // Dọn text cũ trước khi vào cảnh mới
            clearTexts.run();

            winImageView.setImage(storyImage5);
            Pane parent = (Pane) winImageView.getParent();

            // Thêm delay ngắn để đảm bảo ảnh đã render xong
            PauseTransition waitForImage = new PauseTransition(Duration.millis(200));
            waitForImage.setOnFinished(e0 -> {
                if (isWinSkipped) return;

                // scene3_Text1
                Text t1 = createCutsceneText(60, 120, 700, 48,
                        "-fx-fill: white; -fx-stroke: #bd7244; -fx-stroke-width: 1.5;");
                parent.getChildren().add(t1);

                startWinTypewriter(t1, scene3_Text1, () -> {
                    PauseTransition p2 = new PauseTransition(Duration.millis(1000));
                    p2.setOnFinished(e -> {
                        if (isWinSkipped) return;

                        // scene3_Text2
                        Text t2 = createCutsceneText(580, 650, 570, 46,
                                "-fx-fill: white; -fx-stroke: #864c39; -fx-stroke-width: 1.5;");
                        parent.getChildren().add(t2);

                        startWinTypewriter(t2, scene3_Text2, () -> {
                            PauseTransition done = new PauseTransition(Duration.seconds(3));
                            done.setOnFinished(e2 -> onAllFinished.run());
                            done.play();
                        });
                    });
                    p2.play();
                });
            });
            waitForImage.play();
        };

        // ---------- Cảnh kết (storyImage4) ----------
        Runnable playScene2 = () -> {
            if (isWinSkipped) return;
            winImageView.setImage(storyImage4);
            Pane parent = (Pane) winImageView.getParent();

            Text t3 = createCutsceneText(60, 120, 700, 48, "-fx-fill: white; -fx-stroke: #bd7244; -fx-stroke-width: 1.5;");
            parent.getChildren().add(t3);
            startWinTypewriter(t3, scene2_Text3, () -> {
                PauseTransition p1 = new PauseTransition(Duration.millis(1000));
                p1.setOnFinished(e -> {
                    if (isWinSkipped) return;

                    Text t4 = createCutsceneText(60, 650, 700, 46, "-fx-fill: white; -fx-stroke: #821010; -fx-stroke-width: 1.5;");
                    parent.getChildren().add(t4);
                    startWinTypewriter(t4, scene2_Text4, () -> {
                        PauseTransition p2 = new PauseTransition(Duration.millis(800));
                        p2.setOnFinished(e2 -> {
                            if (isWinSkipped) return;

                            Text t5 = createCutsceneText(750, 750, 450, 44, "-fx-fill: white; -fx-stroke: #864c39; -fx-stroke-width: 1.5;");
                            t5.setTextAlignment(javafx.scene.text.TextAlignment.LEFT);
                            parent.getChildren().add(t5);
                            startWinTypewriter(t5, scene2_Text5, () -> {
                                // Sau khi xong ending → sang opening
                                PauseTransition nextScene = new PauseTransition(Duration.seconds(2));
                                nextScene.setOnFinished(e3 -> playScene3.run());
                                nextScene.play();
                            });
                        });
                        p2.play();
                    });
                });
                p1.play();
            });
        };

        // ---------- Bắt đầu toàn bộ ----------
        System.out.println("Bắt đầu Cutscene (Win Level 2)...");
        winImageView.setImage(storyImage4);
        winImageView.setVisible(true);

        PauseTransition startDelay = new PauseTransition(Duration.millis(2000));
        startDelay.setOnFinished(e -> playScene2.run());
        startDelay.play();
    }

    // Hàm helper tạo text với style chung
    private Text createCutsceneText(double x, double y, double wrap, double fontSize, String style) {
        Text text = new Text();
        text.setTextAlignment(javafx.scene.text.TextAlignment.LEFT);
        text.setLayoutX(x);
        text.setLayoutY(y);
        text.setWrappingWidth(wrap);
        text.setStyle(style);
        text.setFont(isabellaBodyFont != null ? Font.font(isabellaBodyFont.getFamily(), fontSize) : new Font("Arial", fontSize - 3));
        return text;
    }

    private void runCutscene_WinLevel3() {
        if (storyImage6 == null) {
            System.err.println("Thiếu ảnh 6");
            handleQuit();
            return;
        }

        if (gameCanvas != null) gameCanvas.setVisible(false);
        isWinSkipped = false;
        winText.setVisible(false);

        winImageView.setOnMousePressed(e -> {
            if (isWinSkipped) return;
            isWinSkipped = true;
            handleQuit();
        });

        // --- Nội dung kết ---
        String scene3_Text3 = "Ánh sáng cuối cùng bùng nổ, xé tan cả Thiên Đàng lẫn Địa Ngục. " +
                "Bạo Chúa Nem Chua gào thét, và bầu trời sụp đổ cùng nhịp tim nhân loại. \n" +
                "ConMel tan vào luồng sáng của lời hứa cuối cùng.";
        String scene3_Text4 = "Trên mặt đất nứt vỡ, một bàn tay gãy nát nằm lại giữa đống " +
                "tro tàn - như dấu chấm hết cho giấc mơ của nhân loại.";

        System.out.println("Bắt đầu Cảnh (Thắng màn 3 - cuối)...");

        // --- Đặt ảnh ---
        winImageView.setImage(storyImage6);
        winImageView.setVisible(true);

        // --- Lấy parent thật của ảnh ---
        Node parentNode = winImageView.getParent();
        if (!(parentNode instanceof Pane parent)) {
            System.err.println("Không tìm thấy Pane cha của winImageView!");
            handleQuit();
            return;
        }

        // 🧹 Dọn text cũ
        Platform.runLater(() -> parent.getChildren().removeIf(n -> n instanceof Text));

        // --- Đợi 0.3s để chắc chắn ảnh hiển thị ---
        PauseTransition waitImage = new PauseTransition(Duration.millis(300));
        waitImage.setOnFinished(ev -> {
            if (isWinSkipped) return;

            // --- Text 1 ---
            Text text1 = new Text();
            text1.setTextAlignment(javafx.scene.text.TextAlignment.LEFT);
            text1.setLayoutX(40);
            text1.setLayoutY(100);
            text1.setWrappingWidth(750);
            text1.setStyle("-fx-fill: white; -fx-stroke: #520d0d; -fx-stroke-width: 1.5;");
            text1.setFont(isabellaBodyFont != null
                    ? Font.font(isabellaBodyFont.getFamily(), 50)
                    : new Font("Arial", 45));
            parent.getChildren().add(text1);

            // 🔹 Đảm bảo text nằm trên ảnh
            text1.toFront();

            startWinTypewriter(text1, scene3_Text3, () -> {
                PauseTransition after1 = new PauseTransition(Duration.millis(1000));
                after1.setOnFinished(e -> {
                    if (isWinSkipped) return;

                    Text text2 = new Text();
                    text2.setTextAlignment(javafx.scene.text.TextAlignment.LEFT);
                    text2.setLayoutX(770);
                    text2.setLayoutY(550);
                    text2.setWrappingWidth(400);
                    text2.setStyle("-fx-fill: white; -fx-stroke: #520d0d; -fx-stroke-width: 1.5;");
                    text2.setFont(isabellaBodyFont != null
                            ? Font.font(isabellaBodyFont.getFamily(), 50)
                            : new Font("Arial", 42));
                    parent.getChildren().add(text2);
                    text2.toFront();

                    startWinTypewriter(text2, scene3_Text4, () -> {
                        PauseTransition done = new PauseTransition(Duration.seconds(4));
                        done.setOnFinished(e2 -> {
                            if (isWinSkipped) return;
                            handleQuit();
                        });
                        done.play();
                    });
                });
                after1.play();
            });
        });
        waitImage.play();
    }


    // --- TYPEWRITER: bản mới ---
    private void startWinTypewriter(Text target, String content, Runnable onFinished) {
        if (target == null || content == null) {
            if (onFinished != null) onFinished.run();
            return;
        }

        target.setText("");
        final int[] index = {0};

        // stop timeline trước đó nếu có
        if (currentWinTimeline != null) {
            currentWinTimeline.stop();
        }

        // khai báo timeline trước, sau đó tạo KeyFrame tham chiếu tới timeline
        Timeline timeline = new Timeline();

        KeyFrame kf = new KeyFrame(Duration.millis(60), evt -> {
            if (isWinSkipped) {
                // dừng chính timeline này
                timeline.stop();
                return;
            }
            if (index[0] < content.length()) {
                target.setText(target.getText() + content.charAt(index[0]++));
            } else {
                timeline.stop();
                SoundManager.stopTypingLoop();
                if (onFinished != null) onFinished.run();
            }
        });

        timeline.getKeyFrames().add(kf);
        timeline.setCycleCount(content.length() + 1);

        // lưu tham chiếu để có thể dừng từ nơi khác (ví dụ khi skip)
        currentWinTimeline = timeline;

        SoundManager.startTypingLoop();
        timeline.play();
    }


    // --- TYPEWRITER: bản cũ giữ nguyên API ---
    private void startWinTypewriter(String fullText, Runnable onFinished) {
        startWinTypewriter(winText, fullText, onFinished);
    }

    private void loadNextLevelAndRestart() {
        restartGameAfterWin();
    }

    private void restartGameAfterWin() {
        if (winImageView != null) {
            winImageView.setVisible(false);
            winImageView.setOnMousePressed(null);
        }
        if (winText != null) winText.setVisible(false);

        if (gameCanvas != null) {
            gameCanvas.setVisible(true);
            gameCanvas.requestFocus();
        }
        if (gameLoop != null) gameLoop.start();
    }

    private void handleRestart() {
        if (isPaused) togglePause(false);
        if (currentWinTimeline != null) {
            currentWinTimeline.stop();
            SoundManager.stopTypingLoop();
        }
        if (winText != null) winText.setVisible(false);
        gameManager.initGame();
        restartGameAfterWin();
    }

    private void handleQuit() {
        stopGameLoop();
        if (currentWinTimeline != null) currentWinTimeline.stop();
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
            ex.printStackTrace();
        }
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
