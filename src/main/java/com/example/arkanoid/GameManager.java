package com.example.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GameManager {
    private final int playAreaWidth = 800;
    private double playAreaOffsetX;

    private int screenWidth, screenHeight;
    private Paddle paddle;
    private Ball ball;
    private List<Brick> bricks = new ArrayList<>();
    // --- Danh sách mới cho các dây leo ---
    private List<BorderElement> borders = new ArrayList<>();
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
        paddle = new Paddle(
                playAreaOffsetX + playAreaWidth / 2.0 - 60,
                screenHeight - 40,
                120,
                20,
                screenWidth
        );
        paddle.setPlayArea(playAreaOffsetX, playAreaWidth);


        int ballRadius = 12;
        ball = new Ball(
                paddle.getX() + paddle.getWidth() / 2 - ballRadius,
                paddle.getY() - (ballRadius * 2),
                ballRadius,
                screenWidth,
                screenHeight
        );
        ball.setPlayArea(playAreaOffsetX, playAreaWidth);


        ball.stickTo(paddle);
        createBricks();
        // --- Gọi phương thức tạo dây leo ---
        createBorders();
        score = 0;
        lives = 3;
        running = false;
        gameOver = false;
    }

    private void createBricks() {
        bricks.clear();
        int rows = 5, cols = 10;
        double brickW = (playAreaWidth - 100.0) / cols;
        double brickH = 22;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double x = playAreaOffsetX + 30 + c * (brickW + 5);
                double y = 60 + r * (brickH + 6);
                bricks.add(new Brick(x, y, brickW, brickH, 3));
            }
        }
    }

    // --- Phương thức mới để tạo dây leo ---
    private void createBorders() {
        borders.clear();
        double ivyWidth = BorderElement.BORDER_WIDTH;
        double ivyHeight = BorderElement.BORDER_HEIGHT;

        // Nếu ảnh chưa được tải hoặc chiều cao = 0, không làm gì cả
        if (ivyHeight <= 0) {
            System.err.println("Không thể tạo viền do ảnh ivy chưa được tải.");
            return;
        }

        // Tọa độ X cho viền trái và phải
        // --- THAY ĐỔI VỊ TRÍ TỌA ĐỘ ---
        double x_left = playAreaOffsetX - ivyWidth + 75; // Đặt ở rìa ngoài bên trái
        double x_right = playAreaOffsetX + playAreaWidth - 75; // Đặt ở rìa ngoài bên phải

        // Dùng vòng lặp để "xếp" các dây leo từ trên xuống dưới
        for (double y = 0; y < screenHeight; y += ivyHeight) {
            borders.add(new BorderElement(x_left, y, ivyWidth, ivyHeight));
            borders.add(new BorderElement(x_right, y, ivyWidth, ivyHeight));
        }
    }

    // ... (Các phương thức khác giữ nguyên) ...
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

        // Cập nhật các đối tượng trang trí (nếu cần)
        // for (BorderElement b : borders) {
        //     b.update(dt);
        // }

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
                    score += 25;
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
        // 1. Vẽ ảnh nền (hai bên cánh)
        if (backgroundImage != null) {
            gc.drawImage(backgroundImage, 0, 0, screenWidth, screenHeight);
        } else {
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, screenWidth, screenHeight);
        }

        // 2. Vẽ nền mờ cho khu vực chơi (đè lên nền pixel art)
        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.fillRect(playAreaOffsetX, 0, playAreaWidth, screenHeight);

        // --- 3. Vẽ các dây leo ---
        for (BorderElement b : borders) {
            b.render(gc);
        }

        // 4. Vẽ thông tin HUD
        gc.setFill(Color.WHITE);
        gc.setFont(new Font("Arial", 16));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("Player: " + playerName, screenWidth / 2.0 - 150, 25);
        gc.fillText("Score: " + score, screenWidth / 2.0, 25);
        gc.fillText("Lives: " + lives, screenWidth / 2.0 + 150, 25);
        gc.setTextAlign(TextAlignment.LEFT);

        // 5. Vẽ các đối tượng game
        paddle.render(gc);
        ball.render(gc);
        for (Brick b : bricks) {
            b.render(gc);
        }

        // 6. Vẽ màn hình Game Over (nếu có)
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

