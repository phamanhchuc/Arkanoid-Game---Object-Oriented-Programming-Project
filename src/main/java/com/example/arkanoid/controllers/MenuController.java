package com.example.arkanoid.controllers;

import com.example.arkanoid.HighScores;
import com.example.arkanoid.ScoreEntry;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Screen;
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
        // Gán sự kiện setOnAction (như cũ)
        buttonStart.setOnAction(e -> handleStartGame());
        buttonSetting.setOnAction(e -> handleSettings());
        buttonRanking.setOnAction(e -> handleRanking());
        buttonGuide.setOnAction(e -> handleGuide());
        backButtonGuide.setOnAction(e -> handleBackFromGuide());
        backButtonRanking.setOnAction(e -> handleBackFromRanking());

        // --- CODE CẬP NHẬT ---

        // 1. Áp dụng hiệu ứng THU PHÓNG (không xoay) cho nút Start
        addScaleAnimation(buttonStart, startImage);

        // 2. Áp dụng hiệu ứng JIGGLE (lắc lư) cho các nút menu khác
        addJiggleAnimation(buttonSetting, settingImage);
        addJiggleAnimation(buttonRanking, rankingImage);
        addJiggleAnimation(buttonGuide, guideImageBtn);

        // 3. Áp dụng hiệu ứng CLICK (thu nhỏ nút) cho các nút "Back"
        addClickAnimation(backButtonGuide);
        addClickAnimation(backButtonRanking);
        // --- KẾT THÚC CODE CẬP NHẬT ---

        highScores = new HighScores();
    }

    /**
     * HÀM MỚI: Chỉ thu nhỏ/phóng to HÌNH ẢNH (không xoay)
     * Giống như hiệu ứng Login.
     */
    private void addScaleAnimation(Button button, ImageView image) {
        // Tạo hiệu ứng khi nhấn xuống (thu nhỏ còn 90%)
        ScaleTransition pressTransition = new ScaleTransition(Duration.millis(100), image);
        pressTransition.setToX(0.9);
        pressTransition.setToY(0.9);

        // Tạo hiệu ứng khi thả ra (trở về 100%)
        ScaleTransition releaseTransition = new ScaleTransition(Duration.millis(100), image);
        releaseTransition.setToX(1.0);
        releaseTransition.setToY(1.0);

        // Áp dụng sự kiện (lên NÚT)
        button.setOnMousePressed(event -> pressTransition.playFromStart());
        button.setOnMouseReleased(event -> releaseTransition.playFromStart());
    }

    /**
     * HÀM CŨ: (Giữ lại) Thêm hiệu ứng JIGGLE (lắc + thu nhỏ)
     */
    private void addJiggleAnimation(Button button, ImageView image) {
        // Thu nhỏ + Xoay khi nhấn
        ScaleTransition pressScale = new ScaleTransition(Duration.millis(100), image);
        pressScale.setToX(0.9);
        pressScale.setToY(0.9);
        RotateTransition pressRotate = new RotateTransition(Duration.millis(100), image);
        pressRotate.setToAngle(-5);
        ParallelTransition pressTransition = new ParallelTransition(pressScale, pressRotate);

        // Trở về bình thường khi thả
        ScaleTransition releaseScale = new ScaleTransition(Duration.millis(100), image);
        releaseScale.setToX(1.0);
        releaseScale.setToY(1.0);
        RotateTransition releaseRotate = new RotateTransition(Duration.millis(100), image);
        releaseRotate.setToAngle(0);
        ParallelTransition releaseTransition = new ParallelTransition(releaseScale, releaseRotate);

        button.setOnMousePressed(event -> pressTransition.playFromStart());
        button.setOnMouseReleased(event -> releaseTransition.playFromStart());
    }

    /**
     * HÀM CŨ: (Giữ lại) Thêm hiệu ứng nhấn và thả cho một nút (dùng cho nút Back)
     */
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

    //
    // --- (Toàn bộ các hàm handle... và set... của bạn giữ nguyên, không thay đổi gì) ---
    //
    private void handleStartGame() {
        try {
            Stage stage = (Stage) buttonStart.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/arkanoid/main-view.fxml"));
            Parent gameRoot = loader.load();
            Scene gameScene = new Scene(gameRoot, 1200, 955.5);
            stage.setScene(gameScene);
            stage.setTitle("Arkanoid Game");

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
}