package com.example.arkanoid;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region; // <-- THÊM IMPORT NÀY
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static final double DESIGN_WIDTH = 1200.0;
    public static final double DESIGN_HEIGHT = 955.5;

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/arkanoid/login.fxml"));

        // --- SỬA LỖI Ở ĐÂY: Đổi 'Parent' thành 'Region' ---
        Region contentRoot = loader.load();

        StackPane rootPane = new StackPane();
        rootPane.getChildren().add(contentRoot);
        rootPane.setStyle("-fx-background-color: black;");
        rootPane.setAlignment(Pos.CENTER);

        // Dòng này giờ sẽ hoạt động
        contentRoot.setMaxSize(DESIGN_WIDTH, DESIGN_HEIGHT);

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double screenWidth = screenBounds.getWidth();
        double screenHeight = screenBounds.getHeight();

        Scene scene = new Scene(rootPane, screenWidth, screenHeight);

        scaleToFit(contentRoot, scene);

        stage.setTitle("Arkanoid");
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.show();
    }

    // Hàm scaleToFit vẫn nhận 'Parent', điều này là đúng
    public static void scaleToFit(Parent content, Scene scene) {
        var scaleXBinding = scene.widthProperty().divide(DESIGN_WIDTH);
        var scaleYBinding = scene.heightProperty().divide(DESIGN_HEIGHT);
        var minScaleBinding = Bindings.min(scaleXBinding, scaleYBinding);

        content.scaleXProperty().bind(minScaleBinding);
        content.scaleYProperty().bind(minScaleBinding);
    }

    public static void main(String[] args) {
        launch(args);
    }
}