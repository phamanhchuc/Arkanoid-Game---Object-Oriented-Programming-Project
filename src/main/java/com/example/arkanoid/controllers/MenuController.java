package com.example.arkanoid.controllers;

import com.example.arkanoid.HighScores;
import com.example.arkanoid.MainApp;
import com.example.arkanoid.ScoreEntry;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region; // <-- THÊM IMPORT NÀY
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;

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

    private HighScores highScores;

    @FXML
    private void initialize() {
        buttonStart.setOnAction(e -> handleStartGame());
        buttonSetting.setOnAction(e -> handleSettings());
        buttonRanking.setOnAction(e -> handleRanking());
        buttonGuide.setOnAction(e -> handleGuide());
        backButtonGuide.setOnAction(e -> handleBackFromGuide());
        backButtonRanking.setOnAction(e -> handleBackFromRanking());
        addScaleAnimation(buttonStart, startImage);
        addJiggleAnimation(buttonSetting, settingImage);
        addJiggleAnimation(buttonRanking, rankingImage);
        addJiggleAnimation(buttonGuide, guideImageBtn);
        addClickAnimation(backButtonGuide);
        addClickAnimation(backButtonRanking);
        highScores = new HighScores();
    }

    // Đây là hàm gây ra lỗi của bạn
    private void handleStartGame() {
        try {
            Stage stage = (Stage) buttonStart.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/arkanoid/main-view.fxml"));

            // --- SỬA LỖI Ở ĐÂY: Đổi 'Parent' thành 'Region' ---
            Region gameRoot = loader.load(); // <-- Lỗi ở dòng này

            StackPane rootPane = new StackPane();
            rootPane.getChildren().add(gameRoot);
            rootPane.setStyle("-fx-background-color: black;");
            rootPane.setAlignment(Pos.CENTER);

            // Dòng này giờ sẽ hoạt động
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
    }

    private void handleRanking() {
        System.out.println("Nút Ranking đã được nhấn!");
        try {
            Image rankingBg = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/ranking.jpg"));
            if (rankingBg != null) {
                rankingImageView.setImage(rankingBg);
                List<ScoreEntry> topScores = highScores.getScores();
                rank1Text.setText(getRankText(topScores, 0));
                rank2Text.setText(getRankText(topScores, 1));
                rank3Text.setText(getRankText(topScores, 2));
                setRankingVisible(true);
            } else {
                System.err.println("Lỗi: Không tìm thấy ảnh ranking.jpg");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tải ảnh hoặc hiển thị BXH:");
            e.printStackTrace();
        }
    }

    private String getRankText(List<ScoreEntry> scores, int index) {
        if (index < scores.size()) {
            ScoreEntry entry = scores.get(index);
            return (index + 1) + ". " + entry.getPlayerName() + ": " + entry.getScore();
        } else {
            return (index + 1) + ". N/A";
        }
    }

    private void handleGuide() {
        System.out.println("Nút Guide đã được nhấn!");
        try {
            Image guideImg = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/guideMenu.png"));
            if (guideImg != null) {
                guideImageView.setImage(guideImg);
                setGuideVisible(true);
            } else {
                System.err.println("Lỗi: Không tìm thấy ảnh guideMenu.png");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tải ảnh hướng dẫn:");
            e.printStackTrace();
        }
    }

    private void handleBackFromGuide() {
        setGuideVisible(false);
    }

    private void handleBackFromRanking() {
        setRankingVisible(false);
    }

    private void setGuideVisible(boolean isVisible) {
        guideImageView.setVisible(isVisible);
        backButtonGuide.setVisible(isVisible);
        setMainMenuVisible(!isVisible);
    }

    private void setRankingVisible(boolean isVisible) {
        rankingImageView.setVisible(isVisible);
        rank1Text.setVisible(isVisible);
        rank2Text.setVisible(isVisible);
        rank3Text.setVisible(isVisible);
        backButtonRanking.setVisible(isVisible);
        setMainMenuVisible(!isVisible);
    }

    private void setMainMenuVisible(boolean isVisible) {
        mainBackground.setVisible(isVisible);
        buttonStart.setVisible(isVisible);
        buttonSetting.setVisible(isVisible);
        buttonRanking.setVisible(isVisible);
        buttonGuide.setVisible(isVisible);
        startImage.setVisible(isVisible);
        settingImage.setVisible(isVisible);
        rankingImage.setVisible(isVisible);
        guideImageBtn.setVisible(isVisible);
    }

    private void addScaleAnimation(Button button, ImageView image) {
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