package com.example.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

// --- NEW IMPORTS ---
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
// --- END NEW IMPORTS ---

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

    private boolean mouseControlled = false;

    // Current level name (can be changed later)
    private String currentLevel = "level1.txt";

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
        createBricks(currentLevel); // <-- Pass level name
        powerUps.clear();
        particles.clear();
        trailSpawnTimer = 0;
        score = 0;
        lives = 3;
        running = false;
        gameOver = false;
        mouseControlled = false;
    }

    // --- UPDATED createBricks METHOD ---
    private void createBricks(String levelFileName) {
        bricks.clear();
        String path = "/com/example/arkanoid/levels/" + levelFileName;
        List<String[]> mapData = new ArrayList<>();
        int cols = 0;

        // --- Read the map file first ---
        try (InputStream is = getClass().getResourceAsStream(path);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            if (is == null) {
                System.err.println("Lỗi nghiêm trọng: Không tìm thấy file map: " + path);
                return; // Stop if file not found
            }

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim(); // Remove leading/trailing spaces
                if (!line.isEmpty()) {
                    String[] numbers = line.split("\\s+"); // Split by one or more spaces
                    mapData.add(numbers);
                    if (cols == 0) { // Determine number of columns from the first non-empty line
                        cols = numbers.length;
                    } else if (numbers.length != cols) {
                        System.err.println("Cảnh báo: Hàng trong map file có số cột không đồng đều!");
                    }
                }
            }
        } catch (IOException | NullPointerException e) {
            System.err.println("Lỗi khi đọc file map: " + path);
            e.printStackTrace();
            return; // Stop if reading failed
        }

        if (mapData.isEmpty() || cols == 0) {
            System.err.println("Lỗi: Map file rỗng hoặc không hợp lệ: " + path);
            return;
        }

        // --- Now create bricks based on mapData ---
        int rows = mapData.size();
        double horizontalPadding = 50.0; // Total padding left and right
        double verticalPaddingTop = 60.0; // Space from top
        double brickSpacingX = 0.0; // Space between bricks horizontally
        double brickSpacingY = 0.0; // Space between bricks vertically

        // Calculate brick width based on available space and number of columns
        double brickW = (playAreaWidth - horizontalPadding * 2 - (cols - 1) * brickSpacingX) / cols;
        double brickH = 22; // Keep height fixed for now

        for (int r = 0; r < rows; r++) {
            String[] numbers = mapData.get(r);
            for (int c = 0; c < numbers.length; c++) { // Use numbers.length for safety
                int hitCount = 0;
                try {
                    hitCount = Integer.parseInt(numbers[c]);
                } catch (NumberFormatException e) {
                    System.err.println("Cảnh báo: Ký tự không hợp lệ trong map file tại hàng " + (r + 1) + ", cột " + (c + 1));
                    continue; // Skip invalid entry
                }

                if (hitCount > 0) { // Only create brick if hitCount is 1, 2, or 3 etc.
                    double x = playAreaOffsetX + horizontalPadding + c * (brickW + brickSpacingX);
                    double y = verticalPaddingTop + r * (brickH + brickSpacingY);
                    bricks.add(new Brick(x, y, brickW, brickH, hitCount)); // Use hitCount from file
                }
            }
        }
        System.out.println("Đã tải thành công map: " + levelFileName + " (" + rows + "x" + cols + ")");
    }
    // --- END UPDATED METHOD ---


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
            powerUps.clear();
            SoundManager.playSound(SoundManager.Sound.MISSED_BALL);

            if (lives <= 0) {
                gameOver = true;
                saveCurrentScore();
                SoundManager.playSound(SoundManager.Sound.GAME_OVER);
            } else {
                ball.stickTo(paddle);
            }
        }

        if (checkCollisionCircleRect(ball, paddle) && ball.getY() + ball.getHeight() <= paddle.getY() + 30) {
            ball.bounceOffPaddle(paddle);
            SoundManager.playSound(SoundManager.Sound.HIT_PADDLE);
        }

        boolean allBricksDestroyed = true;
        Iterator<Brick> brickIterator = bricks.iterator();
        while(brickIterator.hasNext()) {
            Brick b = brickIterator.next();
            if (b.isDestroyed()) {
                continue;
            }
            allBricksDestroyed = false;
            if (checkCollisionCircleRect(ball, b)) {
                resolveBallBrickCollision(ball, b);
                SoundManager.playSound(SoundManager.Sound.HIT_BRICK);

                if (b.takeHit()) {
                    score += 100;

                    // --- PHẦN LOGIC RƠI POWERUP ĐÃ SỬA ---
                    // (Sửa lại logic của bạn, < 0.9 và < 0.1 là sai)
                    // Logic đúng (ví dụ: 10% LIFE, 10% LOSE_LIFE):

                    double dropChance = random.nextDouble();
                    double powerUpWidth = 50;
                    double powerUpHeight = 70;

                    if (dropChance < 0.4) { // 10% cơ hội rơi LIFE (từ 0.0 -> 0.099)
                        PowerUp newPowerUp = new PowerUp(
                                b.getX() + (b.getWidth() - powerUpWidth) / 2,
                                b.getY() + (b.getHeight() - powerUpHeight) / 2,
                                powerUpWidth,
                                powerUpHeight,
                                PowerUp.PowerUpType.LIFE
                        );
                        powerUps.add(newPowerUp);
                    } else if (dropChance < 0.7) { // 10% cơ hội khác rơi LOSE_LIFE (từ 0.1 -> 0.199)
                        PowerUp newPowerUp = new PowerUp(
                                b.getX() + (b.getWidth() - powerUpWidth) / 2,
                                b.getY() + (b.getHeight() - powerUpHeight) / 2,
                                powerUpWidth,
                                powerUpHeight,
                                PowerUp.PowerUpType.LOSE_LIFE
                        );
                        powerUps.add(newPowerUp);
                    }
                    // --- HẾT PHẦN SỬA ---

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
            SoundManager.playSound(SoundManager.Sound.LEVEL_COMPLETED);
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
                powerUpIterator.remove();
            } else if (pu.getY() > screenHeight) {
                pu.setCollected(true);
                powerUpIterator.remove();
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
            SoundManager.playSound(SoundManager.Sound.COLLECT_POWERUP);
        }
        else if (pu.getType() == PowerUp.PowerUpType.LOSE_LIFE) {
            lives--;
            System.out.println("Bạn bị mất 1 mạng! Tổng mạng: " + lives);
            // Kiểm tra ngay lập tức xem có thua không
            if (lives <= 0) {
                gameOver = true;
                running = false; // Dừng game
                saveCurrentScore(); // Lưu điểm
            }
        }
    }

    private void resolveBallBrickCollision(Ball ball, Brick brick) {
        // ... (Collision resolution logic - unchanged) ...
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
            // Adjust position slightly to prevent sticking
            if (dx_centers > 0) {
                ball.setX(brick.getX() + brick.getWidth() + 0.1);
            } else {
                ball.setX(brick.getX() - ball.getWidth() - 0.1);
            }
        } else {
            ball.setDy(-ball.getDy());
            // Adjust position slightly to prevent sticking
            if (dy_centers > 0) {
                ball.setY(brick.getY() + brick.getHeight() + 0.1);
            } else {
                ball.setY(brick.getY() - ball.getHeight() - 0.1);
            }
        }
    }

    private boolean checkCollisionCircleRect(Ball ball, GameObject rect) {
        // ... (Collision check logic - unchanged) ...
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
        // ... (Render logic - mostly unchanged) ...
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

        // Render bricks that haven't been destroyed
        for (Brick b : bricks) {
            if (!b.isDestroyed()) { // Only render active bricks
                b.render(gc);
            }
        }
        for (PowerUp pu : powerUps) {
            // PowerUps list only contains active ones now
            pu.render(gc);
        }
        paddle.render(gc);
        for (Particle p : particles) {
            p.render(gc);
        }
        ball.render(gc);

        if (gameOver) {
            // ... (Game Over rendering - unchanged) ...
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
