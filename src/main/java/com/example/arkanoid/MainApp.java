package com.example.arkanoid;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer;
import javafx.scene.paint.Color;

import java.util.HashSet;
import java.util.Set;

public class MainApp extends Application {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;

    private GameManager gm;
    private Set<KeyCode> keys = new HashSet<>();

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gm = new GameManager(WIDTH, HEIGHT);

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root);
        scene.setOnKeyPressed(e -> keys.add(e.getCode()));
        scene.setOnKeyReleased(e -> keys.remove(e.getCode()));

        stage.setScene(scene);
        stage.setTitle("Arkanoid - JavaFX (Demo)");
        stage.show();

        AnimationTimer timer = new AnimationTimer() {
            private long last = 0;
            @Override
            public void handle(long now) {
                if (last == 0) last = now;
                double delta = (now - last) / 1_000_000_000.0;
                last = now;

                // handle input
                if (keys.contains(KeyCode.LEFT)) gm.getPaddle().moveLeft();
                if (keys.contains(KeyCode.RIGHT)) gm.getPaddle().moveRight();
                if (keys.contains(KeyCode.SPACE) && !gm.isRunning()) gm.startGame();

                gm.update(delta);
                // clear
                gc.setFill(Color.BLUE);
                gc.fillRect(0,0, WIDTH, HEIGHT);

                gm.render(gc);
            }
        };
        timer.start();
    }

    public static void main(String[] args) { launch(); }
}
