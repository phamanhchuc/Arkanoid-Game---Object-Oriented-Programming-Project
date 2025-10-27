package com.example.arkanoid.controllers;

import com.example.arkanoid.HighScores;
import com.example.arkanoid.MainApp;
import com.example.arkanoid.ScoreEntry;
import com.example.arkanoid.SoundManager;
import javafx.animation.Interpolator;
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
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;

public class MenuController {

    // --- Biến FXML ---
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

    private HighScores highScores;

    // --- KHỞI TẠO ---
    @FXML
    private void initialize() {
        // Gán sự kiện OnAction
        buttonStart.setOnAction(e -> handleStartGame());
        buttonSetting.setOnAction(e -> handleSettings());
        buttonRanking.setOnAction(e -> handleRanking());
        buttonGuide.setOnAction(e -> handleGuide());
        backButtonGuide.setOnAction(e -> handleBackFromGuide());
        backButtonRanking.setOnAction(e -> handleBackFromRanking());
        backButtonSettings.setOnAction(e -> handleBackFromSettings());

        // Áp dụng hiệu ứng animation
        addScaleAnimation(buttonStart, startImage);
        addJiggleAnimation(buttonSetting, settingImage);
        addJiggleAnimation(buttonRanking, rankingImage);
        addJiggleAnimation(buttonGuide, guideImageBtn);
        addClickAnimation(backButtonGuide);
        addClickAnimation(backButtonRanking);
        addClickAnimation(backButtonSettings);

        highScores = new HighScores();

        // Thiết lập Slider âm lượng
        if (volumeSlider != null) { // Thêm kiểm tra null
            volumeSlider.setValue(SoundManager.getMasterVolume());
            volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
                SoundManager.setMasterVolume(newValue.doubleValue());
            });
        }
    }

    // --- CÁC HÀM XỬ LÝ SỰ KIỆN ---

    private void handleStartGame() { /* ... Giữ nguyên như cũ ... */
        SoundManager.playMusic(SoundManager.Music.BACKGROUND_GAME);
        Node menuContent = mainPane;
        if (menuContent == null) {
            System.err.println("Không thể tìm thấy nội dung menu để tạo animation.");
            loadGameScene();
            return;
        }
        RotateTransition rotate = new RotateTransition(Duration.millis(800), menuContent);
        rotate.setByAngle(360 * 2);
        rotate.setInterpolator(Interpolator.EASE_IN);
        ScaleTransition scale = new ScaleTransition(Duration.millis(800), menuContent);
        scale.setToX(0);
        scale.setToY(0);
        scale.setInterpolator(Interpolator.EASE_IN);
        ParallelTransition swirl = new ParallelTransition(rotate, scale);
        swirl.setOnFinished(event -> loadGameScene());
        swirl.play();
    }

    private void loadGameScene() { /* ... Giữ nguyên như cũ ... */
        try {
            Stage stage = (Stage) mainPane.getScene().getWindow();
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
        showPanel(settingsPane); // Hiện panel settings
    }

    private void handleRanking() {
        System.out.println("Nút Ranking đã được nhấn!");
        try {
            // Xác định Node chứa toàn bộ phần Ranking
            // (Trong FXML của bạn, rankingImageView và các Text, Button nằm trực tiếp
            // trong AnchorPane gốc, nên chúng ta cần ẩn/hiện từng cái hoặc
            // nhóm chúng vào một Pane riêng. Cách dễ hơn là kiểm soát qua showPanel)
            showPanel(rankingImageView); // Dùng rankingImageView làm đại diện

            // Tải ảnh nền nếu cần
            Image rankingBg = rankingImageView.getImage();
            if (rankingBg == null) {
                rankingBg = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/ranking.jpg"));
                if (rankingBg != null) rankingImageView.setImage(rankingBg);
                else {
                    System.err.println("Lỗi: Không tìm thấy ảnh nền ranking.jpg");
                    showPanel(null); // Quay lại menu nếu không tải được ảnh
                    return;
                }
            }
            // Hiển thị điểm
            List<ScoreEntry> topScores = highScores.getScores();
            rank1Text.setText(getRankText(topScores, 0));
            rank2Text.setText(getRankText(topScores, 1));
            rank3Text.setText(getRankText(topScores, 2));

        } catch (Exception e) {
            System.err.println("Lỗi khi tải ảnh hoặc hiển thị BXH:");
            e.printStackTrace();
            showPanel(null); // Quay lại menu nếu có lỗi
        }
    }

    private String getRankText(List<ScoreEntry> scores, int index) { /* ... Giữ nguyên như cũ ... */
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
            // Chỉ hiển thị guide nếu ảnh nền guide tồn tại
            Image guideImg = guideImageView.getImage();
            if(guideImg == null){
                guideImg = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/guideMenu.png"));
                if(guideImg != null) guideImageView.setImage(guideImg);
                else {
                    System.err.println("Lỗi: Không tìm thấy ảnh guideMenu.png");
                    showPanel(null); // Quay lại menu nếu không tải được ảnh
                    return;
                }
            }
            showPanel(guideImageView); // Dùng guideImageView làm đại diện

        } catch (Exception e) {
            System.err.println("Lỗi khi tải ảnh hướng dẫn:");
            e.printStackTrace();
            showPanel(null); // Quay lại menu nếu có lỗi
        }
    }

    private void handleBackFromGuide() {
        showPanel(null); // Quay lại menu chính
    }

    private void handleBackFromRanking() {
        showPanel(null); // Quay lại menu chính
    }

    private void handleBackFromSettings() {
        showPanel(null); // Quay lại menu chính
    }

    // --- HÀM ẨN/HIỆN ĐÃ ĐƯỢC SỬA LỖI ---
    /**
     * Hàm MỚI để quản lý việc hiển thị các panel phụ (Guide, Ranking, Settings)
     * @param panelToShow Panel đại diện cần hiển thị (ImageView hoặc VBox), hoặc null để hiển thị menu chính
     */
    private void showPanel(Node panelToShow) {
        // --- LOGIC ẨN/HIỆN CHO TỪNG PANEL ---
        boolean showGuide = (panelToShow == guideImageView);
        boolean showRanking = (panelToShow == rankingImageView);
        boolean showSettings = (panelToShow == settingsPane);
        boolean showMainMenu = (panelToShow == null);

        // Ẩn/Hiện panel Guide và nút back của nó
        if (guideImageView != null) guideImageView.setVisible(showGuide);
        if (backButtonGuide != null) backButtonGuide.setVisible(showGuide);

        // Ẩn/Hiện panel Ranking và các thành phần con
        if (rankingImageView != null) rankingImageView.setVisible(showRanking);
        if (rank1Text != null) rank1Text.setVisible(showRanking);
        if (rank2Text != null) rank2Text.setVisible(showRanking);
        if (rank3Text != null) rank3Text.setVisible(showRanking);
        if (backButtonRanking != null) backButtonRanking.setVisible(showRanking);

        // Ẩn/Hiện panel Settings
        if (settingsPane != null) settingsPane.setVisible(showSettings);

        // Ẩn/Hiện các thành phần của menu chính
        setMainMenuVisible(showMainMenu);
    }

    /**
     * Hàm này chỉ ẩn/hiện các nút/ảnh của menu chính
     * SỬA LỖI: Không ẩn mainBackground ở đây nữa
     */
    private void setMainMenuVisible(boolean isVisible) {
        // if (mainBackground != null) mainBackground.setVisible(isVisible); // <-- KHÔNG ẨN NỀN NỮA

        // Chỉ ẩn/hiện các nút và ảnh của nút
        if (buttonStart != null) buttonStart.setVisible(isVisible);
        if (buttonSetting != null) buttonSetting.setVisible(isVisible);
        if (buttonRanking != null) buttonRanking.setVisible(isVisible);
        if (buttonGuide != null) buttonGuide.setVisible(isVisible);
        if (startImage != null) startImage.setVisible(isVisible);
        if (settingImage != null) settingImage.setVisible(isVisible);
        if (rankingImage != null) rankingImage.setVisible(isVisible);
        if (guideImageBtn != null) guideImageBtn.setVisible(isVisible);
    }


    // --- CÁC HÀM HỖ TRỢ ANIMATION (Giữ nguyên) ---
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