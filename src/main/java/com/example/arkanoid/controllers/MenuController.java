package com.example.arkanoid.controllers;

import com.example.arkanoid.HighScores;
import com.example.arkanoid.ScoreEntry;
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
import javafx.scene.text.Text; // Thêm lại import Text
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class MenuController {

    @FXML private AnchorPane mainPane;
    @FXML private Button buttonStart;
    @FXML private Button buttonSetting;
    @FXML private Button buttonRanking;
    @FXML private Button buttonGuide;

    @FXML private ImageView guideImageView;
    @FXML private Button backButtonGuide;

    @FXML private ImageView startImage;
    @FXML private ImageView settingImage;
    @FXML private ImageView rankingImage; // Ảnh nút Ranking
    @FXML private ImageView guideImageBtn;
    @FXML private ImageView mainBackground;

    // --- Thêm lại FXML cho các thành phần Ranking inline ---
    @FXML private ImageView rankingImageView; // Ảnh nền BXH
    @FXML private Text rank1Text;
    @FXML private Text rank2Text;
    @FXML private Text rank3Text;
    @FXML private Button backButtonRanking;

    private HighScores highScores;


    @FXML
    private void initialize() {
        buttonStart.setOnAction(e -> handleStartGame());
        buttonSetting.setOnAction(e -> handleSettings());
        buttonRanking.setOnAction(e -> handleRanking());
        buttonGuide.setOnAction(e -> handleGuide());
        backButtonGuide.setOnAction(e -> handleBackFromGuide());
        // --- Thêm lại sự kiện nút back Ranking ---
        backButtonRanking.setOnAction(e -> handleBackFromRanking());

        highScores = new HighScores();
    }

    private void handleStartGame() {
        try {
            Stage stage = (Stage) buttonStart.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/arkanoid/main-view.fxml"));
            Parent gameRoot = loader.load();
            Rectangle2D screenBounds = Screen.getPrimary().getBounds();
            Scene gameScene = new Scene(gameRoot, screenBounds.getWidth(), screenBounds.getHeight());
            stage.setScene(gameScene);
            stage.setTitle("Arkanoid Game");
            stage.setFullScreen(true);

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

    // --- QUAY LẠI LOGIC HIỂN THỊ RANKING INLINE ---
    private void handleRanking() {
        System.out.println("Nút Ranking đã được nhấn!");
        try {
            // Tải ảnh nền BXH
            Image rankingBg = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/ranking.jpg"));
            if (rankingBg != null) {
                rankingImageView.setImage(rankingBg);

                // Lấy top 3 điểm
                List<ScoreEntry> topScores = highScores.getScores();

                // Cập nhật Text (tối đa 3 người)
                rank1Text.setText(getRankText(topScores, 0));
                rank2Text.setText(getRankText(topScores, 1));
                rank3Text.setText(getRankText(topScores, 2));

                // Hiện BXH, ẩn menu
                setRankingVisible(true);
            } else {
                System.err.println("Lỗi: Không tìm thấy ảnh ranking.jpg");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tải ảnh hoặc hiển thị BXH:");
            e.printStackTrace();
        }
    }

    // --- Thêm lại phương thức getRankText ---
    private String getRankText(List<ScoreEntry> scores, int index) {
        if (index < scores.size()) {
            ScoreEntry entry = scores.get(index);
            // Trả về chuỗi định dạng, ví dụ: "1. PlayerName: 12345"
            // (Số thứ tự 1, 2, 3 tương ứng với các Text rank1, rank2, rank3)
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

    // --- Thêm lại phương thức quay lại từ Ranking ---
    private void handleBackFromRanking() {
        setRankingVisible(false);
    }

    private void setGuideVisible(boolean isVisible) {
        guideImageView.setVisible(isVisible);
        backButtonGuide.setVisible(isVisible);
        setMainMenuVisible(!isVisible);
    }

    // --- Thêm lại phương thức ẩn/hiện Ranking ---
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
        rankingImage.setVisible(isVisible); // Ảnh nút Ranking
        guideImageBtn.setVisible(isVisible);
    }
}

