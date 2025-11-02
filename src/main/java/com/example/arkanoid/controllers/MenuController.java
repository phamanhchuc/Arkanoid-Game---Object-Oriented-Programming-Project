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
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MenuController {

    @FXML private AnchorPane mainPane;
    @FXML private Button buttonStart;
    @FXML private Button buttonSetting;
    @FXML private Button buttonRanking;
    @FXML private Button buttonGuide;
    @FXML private Button backButtonGuide;
    @FXML private Button backButtonRanking;
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
    @FXML private Button backButtonSettings;
    @FXML private ImageView settingsBackground;
    @FXML private ImageView storyImageView;

    @FXML private Text storyText1;
    @FXML private Text storyText2;
    @FXML private Text storyText3;

    private Image storyImage1;
    private Image storyImage2;

    private Timeline currentTimeline;
    private boolean isSkipped = false;

    private HighScores highScores;

    @FXML
    private void initialize() {
        buttonStart.setOnAction(e -> handleStartGame());
        buttonSetting.setOnAction(e -> handleSettings());
        buttonRanking.setOnAction(e -> handleRanking());
        buttonGuide.setOnAction(e -> handleGuide());
        backButtonGuide.setOnAction(e -> handleBackFromGuide());
        backButtonRanking.setOnAction(e -> handleBackFromRanking());
        backButtonSettings.setOnAction(e -> handleBackFromSettings());

        addScaleAnimation(buttonStart, startImage);
        addJiggleAnimation(buttonSetting, settingImage);
        addJiggleAnimation(buttonRanking, rankingImage);
        addJiggleAnimation(buttonGuide, guideImageBtn);
        addClickAnimation(backButtonGuide);
        addClickAnimation(backButtonRanking);
        addClickAnimation(backButtonSettings);

        highScores = new HighScores();

        if (volumeSlider != null) {
            volumeSlider.setValue(SoundManager.getMasterVolume());
            volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
                SoundManager.setMasterVolume(newValue.doubleValue());
            });
        }

        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double val = newVal.doubleValue();
            String color;
            if (val <= 0.2) { color = "#6ab04c"; } else if (val <= 0.4) { color = "#2ecc71"; } else if (val <= 0.6) { color = "#f1c40f"; } else if (val <= 0.8) { color = "#e67e22"; } else { color = "#e74c3c"; }
            Node thumb = volumeSlider.lookup(".thumb");
            if (thumb != null) {
                thumb.setStyle("-fx-background-color: " + color + ";");
            }
        });

        // Tải ảnh cốt truyện
        try {
            // (Đã sửa từ lần trước)
            storyImage1 = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/ảnh 1.png")); // Giả sử bạn đã đổi tên file
            storyImage2 = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/ảnh 2.png"));

            if (storyImage1 == null || storyImage1.isError()) {
                System.err.println("Lỗi: Không tìm thấy 'story_1.png'");
                storyImage1 = null;
            }
            if (storyImage2 == null || storyImage2.isError()) {
                System.err.println("Lỗi: Không tìm thấy 'ảnh 2.png'");
                storyImage2 = null;
            }

        } catch (Exception e) {
            System.err.println("Lỗi nghiêm trọng khi tải ảnh cốt truyện.");
            e.printStackTrace();
        }
    }

    private void handleStartGame() {
        if (storyImageView == null || storyImage1 == null) {
            System.err.println("Không thể bắt đầu cốt truyện (ảnh 1 lỗi), tải game luôn.");
            loadGameSceneAndMusic();
            return;
        }

        setMainMenuVisible(false);
        isSkipped = false;

        storyImageView.setImage(storyImage1);
        storyImageView.setOpacity(1.0);
        storyImageView.setVisible(true);

        storyText1.setLayoutX(50.0);
        storyText1.setLayoutY(80.0);
        storyText1.setWrappingWidth(1100.0);
        storyText1.setFont(new javafx.scene.text.Font("Arial", 40.0));
        storyText1.setStyle("-fx-font-family: 'Arial'; -fx-font-weight: bold; -fx-fill: white; -fx-stroke: #A14DA1; -fx-stroke-width: 1.5;");
        storyText1.setText("");
        storyText1.setVisible(true);

        storyText2.setLayoutX(50.0);
        storyText2.setLayoutY(140.0);
        storyText2.setWrappingWidth(550.0);
        storyText2.setFont(new javafx.scene.text.Font("Arial", 28.0));
        storyText2.setStyle("-fx-font-family: 'Arial'; -fx-font-weight: bold; -fx-fill: white; -fx-stroke: #A14DA1; -fx-stroke-width: 1.0;");
        storyText2.setText("");
        storyText2.setVisible(true);

        storyText3.setLayoutX(620.0);
        storyText3.setLayoutY(780.0);
        storyText3.setWrappingWidth(550.0);
        storyText3.setFont(new javafx.scene.text.Font("Arial", 28.0));
        storyText3.setStyle("-fx-font-family: 'Arial'; -fx-font-weight: bold; -fx-fill: white; -fx-stroke: #A14DA1; -fx-stroke-width: 1.0;");
        storyText3.setText("");
        storyText3.setVisible(true);

        storyImageView.setOnMousePressed(event -> {
            if (isSkipped) return;
            isSkipped = true;

            System.out.println("Đã bỏ qua (skip) cốt truyện!");
            if (currentTimeline != null) {
                currentTimeline.stop();
            }

            // --- THÊM DÒNG DỪNG ÂM THANH KHI SKIP ---
            SoundManager.stopTypingLoop();

            storyText1.setText("Nhà nguyện các trắng");
            storyText2.setText("Nơi tiếng chuông yên nghỉ ngàn năm , ConMel gặp tu sĩ mấtức tin -LongDe\nNgai chỉ điểm cho ConMel về tung tích cháu gái AnhChuc...");
            storyText3.setText("...Trong gạch đổ vỡ, ConMel theo lời LongDe tìm cách mở ra Trục Thăng Thiên");

            PauseTransition pause = new PauseTransition(Duration.millis(500));
            pause.setOnFinished(e -> fadeOutAndLoadGame());
            pause.play();
        });

        String titleText = "Nhà nguyện các trắng";
        String panel1Text = "Nơi tiếng chuông yên nghỉ ngàn năm , ConMel gặp tu sĩ mấtức tin -LongDe\nNgai chỉ điểm cho ConMel về tung tích cháu gái AnhChuc..."; // Dòng 1 (bên trái trên)
        String panel2Text = "...Trong gạch đổ vỡ, ConMel theo lời LongDe tìm cách mở ra Trục Thăng Thiên"; // Dòng 2 (bên phải dưới)

        Runnable onT2Finished = () -> {
            PauseTransition finalPause = new PauseTransition(Duration.millis(3000));
            finalPause.setOnFinished(e -> fadeOutAndLoadGame());
            finalPause.play();
        };

        Runnable onT1Finished = () -> {
            startTypewriter(storyText3, panel2Text, onT2Finished, 50);
        };

        // Bắt đầu chạy tiêu đề trước
        startTypewriter(storyText1, titleText, () -> {
            PauseTransition pauseAfterTitle = new PauseTransition(Duration.millis(1500));
            pauseAfterTitle.setOnFinished(e -> startTypewriter(storyText2, panel1Text, onT1Finished, 60));
            pauseAfterTitle.play();
        }, 80);
    }

    /**
     * HÀM NÀY ĐÃ SỬA LẠI LOGIC ÂM THANH
     */
    private void startTypewriter(Text textNode, String fullText, Runnable onFinished, int delayMillis) {
        if (isSkipped) {
            textNode.setText(fullText);
            if (onFinished != null) onFinished.run();
            return;
        }

        final AtomicInteger charIndex = new AtomicInteger(0);
        textNode.setText("");

        currentTimeline = new Timeline(new KeyFrame(Duration.millis(delayMillis), e -> {
            if (isSkipped) {
                currentTimeline.stop();
                return;
            }

            int index = charIndex.getAndIncrement();
            if (index < fullText.length()) {
                textNode.setText(textNode.getText() + fullText.charAt(index));
                // (KHÔNG CẦN PHÁT ÂM THANH Ở ĐÂY NỮA)
            } else {
                currentTimeline.stop();

                // --- DỪNG ÂM THANH KHI CHẠY XONG ---
                SoundManager.stopTypingLoop();

                if (onFinished != null) {
                    onFinished.run();
                }
            }
        }));

        currentTimeline.setCycleCount(fullText.length() + 1);

        // --- BẮT ĐẦU ÂM THANH TRƯỚC KHI CHẠY ---
        SoundManager.startTypingLoop();
        currentTimeline.play();
    }

    private void fadeOutAndLoadGame() {
        if (isSkipped) {
            loadGameSceneAndMusic();
            return;
        }

        FadeTransition fadeImg = new FadeTransition(Duration.millis(1000), storyImageView);
        fadeImg.setToValue(0);

        FadeTransition fadeT1 = new FadeTransition(Duration.millis(1000), storyText1);
        fadeT1.setToValue(0);

        FadeTransition fadeT2 = new FadeTransition(Duration.millis(1000), storyText2);
        fadeT2.setToValue(0);

        FadeTransition fadeT3 = new FadeTransition(Duration.millis(1000), storyText3);
        fadeT3.setToValue(0);

        ParallelTransition fadeOut = new ParallelTransition(fadeImg, fadeT1, fadeT2, fadeT3);
        fadeOut.setOnFinished(e -> {
            storyImageView.setVisible(false);
            storyText1.setVisible(false);
            storyText2.setVisible(false);
            storyText3.setVisible(false);
            loadGameSceneAndMusic();
        });
        fadeOut.play();
    }

    private void loadGameSceneAndMusic() {
        SoundManager.playMusic(SoundManager.Music.BACKGROUND_GAME);
        loadGameScene();
    }


    private void loadGameScene() {
        try {
            Stage stage = (Stage) storyImageView.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/arkanoid/main-view.fxml"));
            Region gameRoot = loader.load();
            StackPane rootPane = new StackPane();
            rootPane.getChildren().add(gameRoot);
            rootPane.setStyle("-fx-background-color: black;");
            rootPane.setAlignment(Pos.CENTER);
            gameRoot.setMaxSize(MainApp.DESIGN_WIDTH, MainApp.DESIGN_HEIGHT);
            Scene gameScene = stage.getScene();
            gameScene.setRoot(rootPane);
            MainApp.scaleToFit(gameRoot, gameScene);
            stage.setTitle("Arkanoid Game");
            Node canvas = gameRoot.lookup("#gameCanvas");
            if (canvas != null) {
                canvas.requestFocus();
            } else {
                gameRoot.requestFocus();
            }
        } catch (IOException ex) {
            System.err.println("Lỗi: Không thể tải file main-view.fxml.");
            ex.printStackTrace();
        } catch (Exception ex) {
            System.err.println("Lỗi không xác định khi tải main-view.fxml.");
            ex.printStackTrace();
        }
    }

    private void handleSettings() {
        System.out.println("Nút Settings đã được nhấn!");
        showPanel(settingsPane);
    }

    private void handleRanking() {
        System.out.println("Nút Ranking đã được nhấn!");
        try {
            showPanel(rankingImageView);
            Image rankingBg = rankingImageView.getImage();
            if (rankingBg == null) {
                rankingBg = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/ranking.jpg"));
                if (rankingBg != null) rankingImageView.setImage(rankingBg);
                else {
                    System.err.println("Lỗi: Không tìm thấy ảnh nền ranking.jpg");
                    showPanel(null);
                    return;
                }
            }
            List<ScoreEntry> topScores = highScores.getScores();
            rank1Text.setText(getRankText(topScores, 0));
            rank2Text.setText(getRankText(topScores, 1));
            rank3Text.setText(getRankText(topScores, 2));

        } catch (Exception e) {
            System.err.println("Lỗi khi tải ảnh hoặc hiển thị BXH:");
            e.printStackTrace();
            showPanel(null);
        }
    }

    private String getRankText(List<ScoreEntry> scores, int index) {
        if (scores != null && index >= 0 && index < scores.size()) {
            ScoreEntry entry = scores.get(index);
            if (entry != null) {
                return (index + 1) + ". " + entry.getPlayerName() + ": " + entry.getScore();
            } else {
                return (index + 1) + ". Error";
            }
        } else {
            return (index + 1) + ". N/A";
        }
    }

    private void handleGuide() {
        System.out.println("Nút Guide đã được nhấn!");
        try {
            Image guideImg = guideImageView.getImage();
            if(guideImg == null){
                guideImg = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/guideMenu.png"));
                if(guideImg != null) guideImageView.setImage(guideImg);
                else {
                    System.err.println("Lỗi: Không tìm thấy ảnh guideMenu.png");
                    showPanel(null);
                    return;
                }
            }
            showPanel(guideImageView);

        } catch (Exception e) {
            System.err.println("Lỗi khi tải ảnh hướng dẫn:");
            e.printStackTrace();
            showPanel(null);
        }
    }

    private void handleBackFromGuide() {
        showPanel(null);
    }

    private void handleBackFromRanking() {
        showPanel(null);
    }

    private void handleBackFromSettings() {
        showPanel(null);
    }

    private void showPanel(Node panelToShow) {
        boolean showGuide = (panelToShow == guideImageView);
        boolean showRanking = (panelToShow == rankingImageView);
        boolean showSettings = (panelToShow == settingsPane);
        boolean showMainMenu = (panelToShow == null);

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
        ScaleTransition pressTransition = new ScaleTransition(Duration.millis(100), image);
        pressTransition.setToX(0.9);
        pressTransition.setToY(0.9);
        ScaleTransition releaseTransition = new ScaleTransition(Duration.millis(100), image);
        releaseTransition.setToX(1.0);
        releaseTransition.setToY(1.0);
        button.setOnMousePressed(event -> pressTransition.playFromStart());
        button.setOnMouseReleased(event -> releaseTransition.playFromStart());
    }

    private void addJiggleAnimation(Button button, ImageView image) {
        if (button == null || image == null) return;
        ScaleTransition pressScale = new ScaleTransition(Duration.millis(100), image);
        pressScale.setToX(0.9);
        pressScale.setToY(0.9);
        RotateTransition pressRotate = new RotateTransition(Duration.millis(100), image);
        pressRotate.setToAngle(-5);
        ParallelTransition pressTransition = new ParallelTransition(pressScale, pressRotate);
        ScaleTransition releaseScale = new ScaleTransition(Duration.millis(100), image);
        releaseScale.setToX(1.0);
        releaseScale.setToY(1.0);
        RotateTransition releaseRotate = new RotateTransition(Duration.millis(100), image);
        releaseRotate.setToAngle(0);
        ParallelTransition releaseTransition = new ParallelTransition(releaseScale, releaseRotate);
        button.setOnMousePressed(event -> pressTransition.playFromStart());
        button.setOnMouseReleased(event -> releaseTransition.playFromStart());
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
}