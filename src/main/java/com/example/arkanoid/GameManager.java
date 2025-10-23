package com.example.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Random;

public class GameManager {
    private final int playAreaWidth = 800;
    private double playAreaOffsetX;

    private int screenWidth, screenHeight;
    private Paddle paddle;
    private Ball ball;
    private List<Brick> bricks = new ArrayList<>();
    private List<BorderElement> borders = new ArrayList<>();
    private List<PowerUp> powerUps = new ArrayList<>();
    private int score = 0;
    private int lives = 3;
    private boolean running = false;
    private String playerName;

    private Image backgroundImage;
    private boolean gameOver = false;
    private Random random = new Random();

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
        createBorders();
        powerUps.clear();
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
                bricks.add(new Brick(x, y, brickW, brickH, 2));
            }
        }
    }

    private void createBorders() {
        borders.clear();
        double ivyWidth = BorderElement.BORDER_WIDTH;
        double ivyHeight = BorderElement.BORDER_HEIGHT;

        if (ivyHeight <= 0) {
            System.err.println("Không thể tạo viền do ảnh ivy chưa được tải hoặc kích thước không hợp lệ.");
            return;
        }

        double x_left = playAreaOffsetX - ivyWidth + 75;
        double x_right = playAreaOffsetX + playAreaWidth - 75;

        for (double y = 0; y < screenHeight; y += ivyHeight) {
            borders.add(new BorderElement(x_left, y, ivyWidth, ivyHeight));
            borders.add(new BorderElement(x_right, y, ivyWidth, ivyHeight));
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
        Iterator<Brick> brickIterator = bricks.iterator();
        while (brickIterator.hasNext()) {
            Brick b = brickIterator.next();
            if (b.isDestroyed()) {
                continue;
            }
            allBricksDestroyed = false;
            if (checkCollisionCircleRect(ball, b)) {
                resolveBallBrickCollision(ball, b);
                if (b.takeHit()) {
                    score += 100;
                    // THAY ĐỔI: Luôn tạo PowerUp loại LIFE
                    if (random.nextDouble() < 0.2) { // 20% cơ hội rơi vật phẩm
                        double powerUpWidth = 50;
                        double powerUpHeight = 70;
                        PowerUp newPowerUp = new PowerUp(
                                b.getX() + (b.getWidth() - powerUpWidth) / 2,
                                b.getY() + (b.getHeight() - powerUpHeight) / 2,
                                powerUpWidth,
                                powerUpHeight,
                                PowerUp.PowerUpType.LIFE // CHỈ ĐỊNH RÕ LOẠI LIFE
                        );
                        powerUps.add(newPowerUp);
                    }
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

        Iterator<PowerUp> powerUpIterator = powerUps.iterator();
        while (powerUpIterator.hasNext()) {
            PowerUp pu = powerUpIterator.next();
            if (pu.isCollected()) {
                powerUpIterator.remove();
                continue;
            }
            pu.update(dt);

            if (checkCollisionRectRect(pu, paddle)) {
                applyPowerUpEffect(pu);
                pu.setCollected(true);
            }

            if (pu.getY() > screenHeight) {
                pu.setCollected(true);
            }
        }
    }

    private boolean checkCollisionRectRect(GameObject r1, GameObject r2) {
        return r1.getX() < r2.getX() + r2.getWidth() &&
                r1.getX() + r1.getWidth() > r2.getX() &&
                r1.getY() < r2.getY() + r2.getHeight() &&
                r1.getY() + r1.getHeight() > r2.getY();
    }

    private void applyPowerUpEffect(PowerUp pu) {
        // THAY ĐỔI: Chỉ xử lý PowerUp loại LIFE
        if (pu.getType() == PowerUp.PowerUpType.LIFE) {
            lives++;
            System.out.println("Bạn nhận được thêm 1 mạng! Tổng mạng: " + lives);
        }
        // Các loại khác sẽ không làm gì nếu không được định nghĩa ở đây
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
        if (backgroundImage != null) {
            gc.drawImage(backgroundImage, 0, 0, screenWidth, screenHeight);
        } else {
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, screenWidth, screenHeight);
        }

        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.fillRect(playAreaOffsetX, 0, playAreaWidth, screenHeight);

        for (BorderElement b : borders) {
            b.render(gc);
        }

        gc.setFill(Color.WHITE);
        gc.setFont(new Font("Arial", 16));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("Player: " + playerName, screenWidth / 2.0 - 150, 25);
        gc.fillText("Score: " + score, screenWidth / 2.0, 25);
        gc.fillText("Lives: " + lives, screenWidth / 2.0 + 150, 25);
        gc.setTextAlign(TextAlignment.LEFT);

        paddle.render(gc);
        ball.render(gc);
        for (Brick b : bricks) {
            b.render(gc);
        }

        for (PowerUp pu : powerUps) {
            pu.render(gc);
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
