package com.example.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class GameManager {

    private final int playAreaWidth = 800;
    private double playAreaOffsetX;
    private int screenWidth, screenHeight;
    private Paddle paddle;
    private Ball ball;
    private List<Brick> bricks = new ArrayList<>();
    private List<PowerUp> powerUps = new ArrayList<>();
    private List<Particle> particles = new ArrayList<>();
    private Random random = new Random();

    private double trailSpawnTimer = 0;
    private static final double TRAIL_SPAWN_INTERVAL = 0.015;

    private int score = 0;
    private int lives = 3;
    private boolean running = false;
    private String playerName;
    private Image backgroundImage;
    private boolean gameOver = false;
    private HighScores highScores;

    // Cờ để quản lý input chuột
    private boolean mouseControlled = false;

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
        highScores = new HighScores();
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
        powerUps.clear();
        particles.clear();
        trailSpawnTimer = 0;
        score = 0;
        lives = 3;
        running = false;
        gameOver = false;
        mouseControlled = false; // Reset cờ chuột
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

    public void startGame() {
        if (!running && !gameOver) {
            running = true;
            ball.launch();
        }
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public Paddle getPaddle() {
        return paddle;
    }

    public void setMouseControl(boolean controlled) {
        this.mouseControlled = controlled;
    }

    public void processMouseMovement(double mouseX) {
        if (paddle != null) {
            paddle.moveTo(mouseX);
        }
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

        if (mouseControlled) {
            paddle.stop();
            return;
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

        trailSpawnTimer += dt;
        if (trailSpawnTimer >= TRAIL_SPAWN_INTERVAL) {
            spawnBallTrailParticle();
            trailSpawnTimer -= TRAIL_SPAWN_INTERVAL;
        }

        if (ball.isOutOfBounds()) {
            lives--;
            running = false;
            SoundManager.playSound(SoundManager.Sound.MISSED_BALL); // <-- ÂM THANH

            if (lives <= 0) {
                gameOver = true;
                saveCurrentScore();
                SoundManager.playSound(SoundManager.Sound.GAME_OVER); // <-- ÂM THANH
            } else {
                ball.stickTo(paddle);
            }
        }

        if (checkCollisionCircleRect(ball, paddle) && ball.getY() + ball.getHeight() <= paddle.getY() + 30) {
            ball.bounceOffPaddle(paddle);
            SoundManager.playSound(SoundManager.Sound.HIT_PADDLE); // <-- ÂM THANH
        }

        boolean allBricksDestroyed = true;
        for (Brick b : bricks) {
            if (b.isDestroyed()) {
                continue;
            }
            allBricksDestroyed = false;
            if (checkCollisionCircleRect(ball, b)) {
                resolveBallBrickCollision(ball, b);

                // (Chỉ phát âm thanh 1 lần khi gạch bị va chạm,
                // không quan tâm nó vỡ hay chưa)
                SoundManager.playSound(SoundManager.Sound.HIT_BRICK); // <-- ÂM THANH

                if (b.takeHit()) {
                    score += 100;
                    if (random.nextDouble() < 0.2) {
                        double powerUpWidth = 50;
                        double powerUpHeight = 70;
                        PowerUp newPowerUp = new PowerUp(
                                b.getX() + (b.getWidth() - powerUpWidth) / 2,
                                b.getY() + (b.getHeight() - powerUpHeight) / 2,
                                powerUpWidth,
                                powerUpHeight,
                                PowerUp.PowerUpType.LIFE
                        );
                        powerUps.add(newPowerUp);
                    }
                } else {
                    score += 25;
                }
                break;
            }
        }

        if (allBricksDestroyed) {
            System.out.println("YOU WIN!");
            running = false;
            saveCurrentScore();
            SoundManager.playSound(SoundManager.Sound.LEVEL_COMPLETED); // <-- ÂM THANH
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

        Iterator<Particle> particleIterator = particles.iterator();
        while (particleIterator.hasNext()) {
            Particle p = particleIterator.next();
            p.update(dt);
            if (p.isExpired()) {
                particleIterator.remove();
            }
        }
    }

    private void spawnBallTrailParticle() {
        double particleX = ball.getX() + ball.getWidth() / 2;
        double particleY = ball.getY() + ball.getHeight() / 2;
        double particleSize = ball.getWidth() * 0.8;
        Color trailColor = Color.rgb(255, 0, 255, 0.8);
        double lifespan = 0.3;
        particles.add(new Particle(particleX, particleY, particleSize, particleSize, trailColor, lifespan));
    }

    private void saveCurrentScore() {
        if (playerName != null && score > 0) {
            boolean isNewHighScore = highScores.addScore(playerName, score);
            if (isNewHighScore) {
                System.out.println("Điểm mới đã được lưu vào top 3!");
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
        if (pu.getType() == PowerUp.PowerUpType.LIFE) {
            lives++;
            System.out.println("Bạn nhận được thêm 1 mạng! Tổng mạng: " + lives);
            SoundManager.playSound(SoundManager.Sound.COLLECT_POWERUP); // <-- ÂM THANH
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
        if (backgroundImage != null) {
            gc.drawImage(backgroundImage, 0, 0, screenWidth, screenHeight);
        } else {
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, screenWidth, screenHeight);
        }

        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.fillRect(playAreaOffsetX, 0, playAreaWidth, screenHeight);

        gc.setFill(Color.WHITE);
        gc.setFont(new Font("Arial", 16));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("Player: " + playerName, screenWidth - 100, 25);
        gc.fillText("Score: " + score, screenWidth - 100, 50);
        gc.fillText("Lives: " + lives, screenWidth - 100, 75);
        gc.setTextAlign(TextAlignment.LEFT);

        for (Brick b : bricks) {
            b.render(gc);
        }
        for (PowerUp pu : powerUps) {
            pu.render(gc);
        }
        paddle.render(gc);
        for (Particle p : particles) {
            p.render(gc);
        }
        ball.render(gc);

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