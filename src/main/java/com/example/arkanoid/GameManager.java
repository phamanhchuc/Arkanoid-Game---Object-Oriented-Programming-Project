package com.example.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GameManager {
    // --- Biến mới để định nghĩa khu vực chơi ---
    private final int playAreaWidth = 800; // Chiều rộng cố định của khu vực chơi
    private double playAreaOffsetX; // Khoảng lề bên trái

    private int screenWidth, screenHeight;
    private Paddle paddle;
    private Ball ball;
    private List<Brick> bricks = new ArrayList<>();
    private int score = 0;
    private int lives = 3;
    private boolean running = false;
    private String playerName;
    private Image backgroundImage;
    private boolean gameOver = false;

    public GameManager(int screenWidth, int screenHeight, String playerName) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.playerName = playerName != null ? playerName : "Player 1";

        // Tính toán khoảng lề để căn giữa khu vực chơi
        this.playAreaOffsetX = (screenWidth - playAreaWidth) / 2.0;

        try {
            backgroundImage = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/background_1.png"));
        } catch (Exception e) {
            System.err.println("Lỗi: Không thể tải ảnh nền.");
            e.printStackTrace();
        }

        initGame();
    }

    public void initGame() {
        // --- Cập nhật vị trí Paddle để nó nằm trong khu vực chơi ---
        paddle = new Paddle(
                playAreaOffsetX + playAreaWidth / 2.0 - 60,
                screenHeight - 40,
                120,
                20,
                screenWidth // Vẫn truyền screenWidth để paddle không đi ra ngoài màn hình
        );
        // Cần truyền cả offset để paddle biết giới hạn di chuyển
        paddle.setPlayArea(playAreaOffsetX, playAreaWidth);


        int ballRadius = 12;
        ball = new Ball(
                paddle.getX() + paddle.getWidth() / 2 - ballRadius,
                paddle.getY() - (ballRadius * 2),
                ballRadius,
                screenWidth, // Truyền kích thước màn hình
                screenHeight
        );
        // Báo cho bóng biết giới hạn của khu vực chơi
        ball.setPlayArea(playAreaOffsetX, playAreaWidth);


        ball.stickTo(paddle);
        createBricks();
        score = 0;
        lives = 3;
        running = false;
        gameOver = false;
    }

    private void createBricks() {
        bricks.clear();
        int rows = 5, cols = 10;
        // Chiều rộng gạch được tính dựa trên playAreaWidth
        double brickW = (playAreaWidth - 100.0) / cols;
        double brickH = 22;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                // Vị trí X của gạch được tính thêm khoảng lề
                double x = playAreaOffsetX + 30 + c * (brickW + 5);
                double y = 60 + r * (brickH + 6);
                bricks.add(new Brick(x, y, brickW, brickH, 2));
            }
        }
    }

    public void startGame() {
        if (!running && !gameOver) {
            running = true;
            ball.launch();
        }
    }

    public boolean isRunning() {
        return running;
    }

    public Paddle getPaddle() {
        return paddle;
    }

    public void processInput(Set<KeyCode> keys) {
        if (gameOver) {
            if (keys.contains(KeyCode.SPACE) || keys.contains(KeyCode.R)) {
                initGame();
            }
            return;
        }

        if (keys.contains(KeyCode.SPACE)) {
            startGame();
        }

        if (keys.contains(KeyCode.LEFT)) {
            paddle.moveLeft();
        } else if (keys.contains(KeyCode.RIGHT)) {
            paddle.moveRight();
        } else {
            paddle.stop();
        }
    }

    public void update(double dt) {
        if (gameOver) {
            return;
        }

        if (!running) {
            paddle.update(dt);
            ball.stickTo(paddle);
            return;
        }
        paddle.update(dt);
        ball.update(dt);

        if (ball.isOutOfBounds()) {
            lives--;
            running = false;
            if (lives <= 0) {
                gameOver = true;
            } else {
                ball.stickTo(paddle);
            }
        }

        if (checkCollisionCircleRect(ball, paddle) && ball.getY() + ball.getHeight() <= paddle.getY() + 30) {
            ball.bounceOffPaddle(paddle);
        }

        boolean allBricksDestroyed = true;
        for (Brick b : bricks) {
            if (b.isDestroyed()) {
                continue;
            }
            allBricksDestroyed = false;
            if (checkCollisionCircleRect(ball, b)) {
                resolveBallBrickCollision(ball, b);
                if (b.takeHit()) {
                    score += 100;
                } else {
                    score += 50;
                }
                break;
            }
        }

        if (allBricksDestroyed) {
            System.out.println("YOU WIN!");
            running = false;
        }
    }

    private void resolveBallBrickCollision(Ball ball, Brick brick) {
        double ballCenterX = ball.getX() + ball.getWidth() / 2;
        double ballCenterY = ball.getY() + ball.getHeight() / 2;
        double brickCenterX = brick.getX() + brick.getWidth() / 2;
        double brickCenterY = brick.getY() + brick.getHeight() / 2;

        double dx_centers = ballCenterX - brickCenterX;
        double dy_centers = ballCenterY - brickCenterY;

        double combinedHalfWidth = (ball.getWidth() + brick.getWidth()) / 2;
        double combinedHalfHeight = (ball.getHeight() + brick.getHeight()) / 2;

        if (Math.abs(dx_centers / combinedHalfWidth) > Math.abs(dy_centers / combinedHalfHeight)) {
            ball.setDx(-ball.getDx());
            if (dx_centers > 0) {
                ball.setX(brick.getX() + brick.getWidth());
            } else {
                ball.setX(brick.getX() - ball.getWidth());
            }
        } else {
            ball.setDy(-ball.getDy());
            if (dy_centers > 0) {
                ball.setY(brick.getY() + brick.getHeight());
            } else {
                ball.setY(brick.getY() - ball.getHeight());
            }
        }
    }

    private boolean checkCollisionCircleRect(Ball ball, GameObject rect) {
        double cx = ball.getX() + ball.getWidth() / 2;
        double cy = ball.getY() + ball.getHeight() / 2;
        double radius = ball.getWidth() / 2;

        double closestX = Math.max(rect.getX(), Math.min(cx, rect.getX() + rect.getWidth()));
        double closestY = Math.max(rect.getY(), Math.min(cy, rect.getY() + rect.getHeight()));

        double dx = cx - closestX;
        double dy = cy - closestY;

        return (dx * dx + dy * dy) <= (radius * radius);
    }

    public void render(GraphicsContext gc) {
        // --- LOGIC VẼ NỀN ĐÃ ĐƯỢC CẬP NHẬT ---
        // 1. Vẽ ảnh nền cho toàn bộ màn hình (sẽ là nền cho 2 cánh)
        if (backgroundImage != null) {
            gc.drawImage(backgroundImage, 0, 0, screenWidth, screenHeight);
        } else {
            // Dự phòng: Vẽ nền đen cho toàn bộ màn hình nếu không có ảnh
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, screenWidth, screenHeight);
        }

        // 2. Vẽ nền đen cho khu vực chơi, đè lên ảnh nền
        gc.setFill(Color.BLACK);
        gc.fillRect(playAreaOffsetX, 0, playAreaWidth, screenHeight);

        // 3. Vẽ thông tin người chơi (căn giữa theo khu vực chơi)
        gc.setFill(Color.WHITE);
        gc.setFont(new Font("Arial", 16));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("Player: " + playerName, screenWidth / 2.0 - 150, 25);
        gc.fillText("Score: " + score, screenWidth / 2.0, 25);
        gc.fillText("Lives: " + lives, screenWidth / 2.0 + 150, 25);
        gc.setTextAlign(TextAlignment.LEFT);


        // Vẽ các đối tượng game
        paddle.render(gc);
        ball.render(gc);
        for (Brick b : bricks) {
            b.render(gc);
        }

        if (gameOver) {
            gc.setFill(Color.rgb(0, 0, 0, 0.7));
            gc.fillRect(0, 0, screenWidth, screenHeight);

            gc.setFill(Color.RED);
            gc.setFont(new Font("Arial", 80));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("GAME OVER", screenWidth / 2.0, screenHeight / 2.0);

            gc.setFill(Color.WHITE);
            gc.setFont(new Font("Arial", 24));
            gc.fillText("Press R or SPACE to Restart", screenWidth / 2.0, screenHeight / 2.0 + 50);

            gc.setTextAlign(TextAlignment.LEFT);
        }
    }
}

