package com.example.arkanoid.controllers;

import com.example.arkanoid.HighScores;
import com.example.arkanoid.MainApp;
import com.example.arkanoid.ScoreEntry;
import com.example.arkanoid.SoundManager;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;

import javafx.util.Duration;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

public class MenuController {

    @FXML private AnchorPane mainPane;
    @FXML private Button buttonStart;
    @FXML private Button buttonSetting;
    @FXML private Button buttonRanking;
    @FXML private Button buttonGuide;
    @FXML private Button backButtonGuide;
    @FXML private Button backButtonRanking;
    @FXML private Button backButtonSettings;

    @FXML private ImageView guideImageView;
    @FXML private ImageView startImage;
    @FXML private ImageView settingImage;
    @FXML private ImageView rankingImage;
    @FXML private ImageView guideImageBtn;
    @FXML private ImageView mainBackground;
    @FXML private ImageView rankingImageView;

    @FXML private Text rank1Text;
    @FXML private Text rank2Text;
    @FXML private Text rank3Text;

    @FXML private VBox settingsPane;
    @FXML private Slider volumeSlider;
    @FXML private ImageView settingsBackground;

    // STORY
    @FXML private ImageView storyImageView;
    @FXML private Text storyText1;
    @FXML private Text storyText2;
    @FXML private Text storyText3;

    // ✅ VIDEO INTRO
    @FXML private MediaView storyVideoView;
    private MediaPlayer introPlayer;

    private Image storyImage1;
    private Image storyImage2;

    private Timeline currentTimeline;
    private boolean isSkipped = false;

    private Font isabellaTitleFont;
    private Font isabellaBodyFont;

    private HighScores highScores;

    @FXML
    private void initialize() {

        // --- BUTTON EVENTS ---
        buttonStart.setOnAction(e -> handleStartGame());
        buttonSetting.setOnAction(e -> handleSettings());
        buttonRanking.setOnAction(e -> handleRanking());
        buttonGuide.setOnAction(e -> handleGuide());
        backButtonGuide.setOnAction(e -> handleBackFromGuide());
        backButtonRanking.setOnAction(e -> handleBackFromRanking());
        backButtonSettings.setOnAction(e -> handleBackFromSettings());

        // --- ANIMATIONS ---
        addScaleAnimation(buttonStart, startImage);
        addJiggleAnimation(buttonSetting, settingImage);
        addJiggleAnimation(buttonRanking, rankingImage);
        addJiggleAnimation(buttonGuide, guideImageBtn);
        addClickAnimation(backButtonGuide);
        addClickAnimation(backButtonRanking);
        addClickAnimation(backButtonSettings);

        highScores = new HighScores();

        // --- VOLUME ---
        if (volumeSlider != null) {
            volumeSlider.setValue(SoundManager.getMasterVolume());
            volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
                SoundManager.setMasterVolume(newValue.doubleValue());
            });
        }

        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double val = newVal.doubleValue();
            String color;
            if (val <= 0.2) color = "#6ab04c";
            else if (val <= 0.4) color = "#2ecc71";
            else if (val <= 0.6) color = "#f1c40f";
            else if (val <= 0.8) color = "#e67e22";
            else color = "#e74c3c";

            Node thumb = volumeSlider.lookup(".thumb");
            if (thumb != null) thumb.setStyle("-fx-background-color: " + color + ";");
        });

        // --- LOAD IMAGES + FONTS ---
        try {
            storyImage1 = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/story_1.png"));
            storyImage2 = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/story_2.png"));

            String fontPath = "/com/example/arkanoid/fonts/isabella.ttf";
            InputStream f1 = getClass().getResourceAsStream(fontPath);
            InputStream f2 = getClass().getResourceAsStream(fontPath);

            if (f1 != null && f2 != null) {
                isabellaTitleFont = Font.loadFont(f1, 80);
                isabellaBodyFont = Font.loadFont(f2, 40);
            }

        } catch (Exception e) {
            System.err.println("Lỗi khi load ảnh/font");
            e.printStackTrace();
        }
    }

    // -------------------------------------------------------
    // PHẦN MỚI: PHÁT VIDEO "man1.mp4"
    // -------------------------------------------------------
    private void playIntroVideo(Runnable onFinished) {
        try {
            String path = "/com/example/arkanoid/videos/man1.mp4";

            Media media = new Media(getClass().getResource(path).toExternalForm());
            introPlayer = new MediaPlayer(media);

            storyVideoView.setMediaPlayer(introPlayer);
            storyVideoView.setVisible(true);

            introPlayer.setOnEndOfMedia(() -> {
                storyVideoView.setVisible(false);
                introPlayer.dispose();
                if (onFinished != null) onFinished.run();
            });

            introPlayer.setOnError(() -> {
                System.err.println("Video man1 lỗi: " + introPlayer.getError());
                storyVideoView.setVisible(false);
                if (onFinished != null) onFinished.run();
            });

            introPlayer.play();

            // Skip video khi click chuột
            storyVideoView.setOnMousePressed(e -> {
                if (introPlayer != null) {
                    introPlayer.stop();
                    introPlayer.dispose();
                }
                storyVideoView.setVisible(false);
                if (onFinished != null) onFinished.run();
            });

        } catch (Exception e) {
            System.err.println("Không thể phát video man1");
            e.printStackTrace();
            if (onFinished != null) onFinished.run();
        }
    }

    // -------------------------------------------------------
    // ✅ BẮT ĐẦU GAME → GỌI VIDEO → SAU ĐÓ CHẠY STORY CŨ
    // -------------------------------------------------------
    private void handleStartGame() {

        final Scene currentScene = mainPane.getScene();
        final Stage currentStage = (Stage) currentScene.getWindow();

        if (currentScene == null || currentStage == null) {
            System.err.println("Lỗi nghiêm trọng: không lấy được Scene/Stage");
            return;
        }

        setMainMenuVisible(false);
        isSkipped = false;

        // ✅ CHẠY VIDEO MAN1 TRƯỚC
        playIntroVideo(() -> startStorySequence(currentStage, currentScene));
    }

    // -------------------------------------------------------
    // ✅ CHUYỂN ĐOẠN STORY TEXT/IMAGE CŨ SANG HÀM RIÊNG
    // -------------------------------------------------------
    private void startStorySequence(Stage currentStage, Scene currentScene) {

        if (storyImage1 == null) {
            System.err.println("Lỗi: không có story_1.png → vào game luôn.");
            loadGameSceneAndMusic(currentStage, currentScene);
            return;
        }

        // Hiển thị image và text
        storyImageView.setImage(storyImage1);
        storyImageView.setOpacity(1);
        storyImageView.setVisible(true);

        storyText1.setText("");
        storyText2.setText("");
        storyText3.setText("");

        // Bật visible + đặt layer lên trên
        storyText1.setVisible(true); storyText1.setOpacity(1); storyText1.toFront();
        storyText2.setVisible(true); storyText2.setOpacity(1); storyText2.toFront();
        storyText3.setVisible(true); storyText3.setOpacity(1); storyText3.toFront();

        // --- Cấu hình vị trí, font, màu sắc ---
        storyText1.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        storyText1.setWrappingWidth(storyImageView.getFitWidth());
        storyText1.setLayoutX(0);
        storyText1.setLayoutY(80.0);
        storyText1.setFont(isabellaTitleFont != null ? isabellaTitleFont : new Font("Arial", 40.0));
        storyText1.setStyle("-fx-fill: white; -fx-stroke: #723f3f; -fx-stroke-width: 1.5;");

        storyText2.setLayoutX(50.0);
        storyText2.setLayoutY(140.0);
        storyText2.setWrappingWidth(550.0);
        storyText2.setFont(isabellaBodyFont != null ? isabellaBodyFont : new Font("Arial", 28.0));
        storyText2.setStyle("-fx-fill: white; -fx-stroke: #864c39; -fx-stroke-width: 1.0;");

        storyText3.setLayoutX(620.0);
        storyText3.setLayoutY(780.0);
        storyText3.setWrappingWidth(550.0);
        storyText3.setFont(isabellaBodyFont != null ? isabellaBodyFont : new Font("Arial", 28.0));
        storyText3.setStyle("-fx-fill: white; -fx-stroke: #864c39; -fx-stroke-width: 1.0;");

        isSkipped = false;

        // Click để skip story
        storyImageView.setOnMousePressed(event -> {
            if (isSkipped) return;
            isSkipped = true;

            if (currentTimeline != null) currentTimeline.stop();
            SoundManager.stopTypingLoop();

            // Hiển thị full text khi skip
            storyText1.setText("NHÀ NGUYỆN CÁT TRẮNG");
            storyText2.setText("Nơi tiếng chuông yên nghỉ ngàn năm, ConMel gặp tu sĩ mất đức tin - LongDe.\nNgài chỉ điểm cho ConMel về tung tích cháu gái AnhChuc...");
            storyText3.setText("...Trong gạch đá đổ vỡ, ConMel theo lời LongDe tìm cách mở ra Trục Thăng Thiên.");

            PauseTransition p = new PauseTransition(Duration.millis(500));
            p.setOnFinished(e -> fadeOutAndLoadGame(currentStage, currentScene));
            p.play();
        });

        // Typewriter story text
        String title = "NHÀ NGUYỆN CÁT TRẮNG";
        String text1 = "Nơi tiếng chuông yên nghỉ ngàn năm, ConMel gặp tu sĩ mất đức tin - LongDe.\nNgài chỉ điểm cho ConMel về tung tích cháu gái AnhChuc...";
        String text2 = "...Trong gạch đá đổ vỡ, ConMel theo lời LongDe tìm cách mở ra Trục Thăng Thiên.";

        startTypewriter(storyText1, title, () -> {
            PauseTransition p = new PauseTransition(Duration.millis(1500));
            p.setOnFinished(e -> startTypewriter(storyText2, text1, () -> {
                startTypewriter(storyText3, text2, () -> {
                    PauseTransition finalPause = new PauseTransition(Duration.millis(2000));
                    finalPause.setOnFinished(e2 -> fadeOutAndLoadGame(currentStage, currentScene));
                    finalPause.play();
                }, 50);
            }, 60));
            p.play();
        }, 80);
    }

    // -------------------------------------------------------
    // (CÁC HÀM CŨ GIỮ NGUYÊN)
    // -------------------------------------------------------

    private void startTypewriter(Text textNode, String fullText, Runnable onFinished, int delayMillis) {
        if (textNode == null || fullText == null) {
            if (onFinished != null) onFinished.run();
            return;
        }

        // Nếu đã skip, hiển thị toàn bộ ngay
        if (isSkipped) {
            textNode.setText(fullText);
            if (onFinished != null) onFinished.run();
            return;
        }

        // Reset text và đảm bảo visible + layer trên cùng
        textNode.setText("");
        textNode.setVisible(true);
        textNode.setOpacity(1);
        textNode.toFront();

        final AtomicInteger charIndex = new AtomicInteger(0);

        currentTimeline = new Timeline(new KeyFrame(Duration.millis(delayMillis), e -> {
            if (isSkipped) {
                currentTimeline.stop();
                textNode.setText(fullText);
                SoundManager.stopTypingLoop();
                if (onFinished != null) onFinished.run();
                return;
            }

            int index = charIndex.getAndIncrement();
            if (index < fullText.length()) {
                textNode.setText(textNode.getText() + fullText.charAt(index));
            } else {
                currentTimeline.stop();
                SoundManager.stopTypingLoop();
                if (onFinished != null) onFinished.run();
            }
        }));

        currentTimeline.setCycleCount(fullText.length() + 1);
        SoundManager.startTypingLoop();
        currentTimeline.play();
    }

    private void fadeOutAndLoadGame(Stage stage, Scene scene) {
        if (isSkipped) { loadGameSceneAndMusic(stage, scene); return; }

        FadeTransition f1 = new FadeTransition(Duration.millis(1000), storyImageView); f1.setToValue(0);
        FadeTransition f2 = new FadeTransition(Duration.millis(1000), storyText1); f2.setToValue(0);
        FadeTransition f3 = new FadeTransition(Duration.millis(1000), storyText2); f3.setToValue(0);
        FadeTransition f4 = new FadeTransition(Duration.millis(1000), storyText3); f4.setToValue(0);

        ParallelTransition fadeOut = new ParallelTransition(f1, f2, f3, f4);
        fadeOut.setOnFinished(e -> {
            storyImageView.setVisible(false);
            storyText1.setVisible(false);
            storyText2.setVisible(false);
            storyText3.setVisible(false);
            loadGameSceneAndMusic(stage, scene);
        });
        fadeOut.play();
    }

    private void loadGameSceneAndMusic(Stage stage, Scene scene) {
        loadGameScene(stage, scene);
    }

    private void loadGameScene(Stage stage, Scene gameScene) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/arkanoid/main-view.fxml"));
            Region gameRoot = loader.load();

            StackPane rootPane = new StackPane();
            rootPane.getChildren().add(gameRoot);
            rootPane.setStyle("-fx-background-color: black;");
            rootPane.setAlignment(Pos.CENTER);

            gameRoot.setMaxSize(MainApp.DESIGN_WIDTH, MainApp.DESIGN_HEIGHT);

            gameScene.setRoot(rootPane);
            MainApp.scaleToFit(gameRoot, gameScene);

            stage.setTitle("Arkanoid Game");

            Node canvas = gameRoot.lookup("#gameCanvas");
            if (canvas != null) canvas.requestFocus();
            else gameRoot.requestFocus();

        } catch (IOException ex) {
            System.err.println("Lỗi: Không thể tải main-view.fxml");
            ex.printStackTrace();
        }
    }

    // OTHER BUTTON HANDLERS (giữ nguyên)
    private void handleSettings() { showPanel(settingsPane); }
    private void handleRanking() {
        try {
            showPanel(rankingImageView);

            List<ScoreEntry> topScores = highScores.getScores();
            rank1Text.setText(getRankText(topScores, 0));
            rank2Text.setText(getRankText(topScores, 1));
            rank3Text.setText(getRankText(topScores, 2));
        } catch (Exception e) {
            e.printStackTrace();
            showPanel(null);
        }
    }

    private String getRankText(List<ScoreEntry> scores, int index) {
        if (scores != null && index >= 0 && index < scores.size()) {
            ScoreEntry entry = scores.get(index);
            return (index + 1) + ". " + entry.getPlayerName() + ": " + entry.getScore();
        }
        return (index + 1) + ". N/A";
    }

    private void handleGuide() { showPanel(guideImageView); }
    private void handleBackFromGuide() { showPanel(null); }
    private void handleBackFromRanking() { showPanel(null); }
    private void handleBackFromSettings() { showPanel(null); }

    private void showPanel(Node panel) {
        boolean showGuide = (panel == guideImageView);
        boolean showRanking = (panel == rankingImageView);
        boolean showSettings = (panel == settingsPane);
        boolean showMainMenu = (panel == null);

        if (guideImageView != null) guideImageView.setVisible(showGuide);
        if (backButtonGuide != null) backButtonGuide.setVisible(showGuide);

        if (rankingImageView != null) rankingImageView.setVisible(showRanking);
        if (rank1Text != null) rank1Text.setVisible(showRanking);
        if (rank2Text != null) rank2Text.setVisible(showRanking);
        if (rank3Text != null) rank3Text.setVisible(showRanking);
        if (backButtonRanking != null) backButtonRanking.setVisible(showRanking);

        if (settingsPane != null) settingsPane.setVisible(showSettings);
        if (settingsBackground != null) settingsBackground.setVisible(showSettings);

        setMainMenuVisible(showMainMenu);
    }

    private void setMainMenuVisible(boolean isVisible) {
        if (buttonStart != null) buttonStart.setVisible(isVisible);
        if (buttonSetting != null) buttonSetting.setVisible(isVisible);
        if (buttonRanking != null) buttonRanking.setVisible(isVisible);
        if (buttonGuide != null) buttonGuide.setVisible(isVisible);

        if (startImage != null) startImage.setVisible(isVisible);
        if (settingImage != null) settingImage.setVisible(isVisible);
        if (rankingImage != null) rankingImage.setVisible(isVisible);
        if (guideImageBtn != null) guideImageBtn.setVisible(isVisible);
    }

    private void addScaleAnimation(Button button, ImageView image) {
        if (button == null || image == null) return;

        ScaleTransition press = new ScaleTransition(Duration.millis(100), image);
        press.setToX(0.9); press.setToY(0.9);

        ScaleTransition release = new ScaleTransition(Duration.millis(100), image);
        release.setToX(1.0); release.setToY(1.0);

        button.setOnMousePressed(e -> press.playFromStart());
        button.setOnMouseReleased(e -> release.playFromStart());
    }

    private void addJiggleAnimation(Button button, ImageView image) {
        if (button == null || image == null) return;

        ScaleTransition pressScale = new ScaleTransition(Duration.millis(100), image);
        pressScale.setToX(0.9); pressScale.setToY(0.9);

        RotateTransition pressRotate = new RotateTransition(Duration.millis(100), image);
        pressRotate.setToAngle(-5);

        ParallelTransition press = new ParallelTransition(pressScale, pressRotate);

        ScaleTransition releaseScale = new ScaleTransition(Duration.millis(100), image);
        releaseScale.setToX(1.0); releaseScale.setToY(1.0);

        RotateTransition releaseRotate = new RotateTransition(Duration.millis(100), image);
        releaseRotate.setToAngle(0);

        ParallelTransition release = new ParallelTransition(releaseScale, releaseRotate);

        button.setOnMousePressed(e -> press.playFromStart());
        button.setOnMouseReleased(e -> release.playFromStart());
    }

    private void addClickAnimation(Button button) {
        if (button == null) return;

        ScaleTransition press = new ScaleTransition(Duration.millis(100), button);
        press.setToX(0.9); press.setToY(0.9);

        ScaleTransition release = new ScaleTransition(Duration.millis(100), button);
        release.setToX(1.0); release.setToY(1.0);

        button.setOnMousePressed(e -> press.playFromStart());
        button.setOnMouseReleased(e -> release.playFromStart());
    }
}
