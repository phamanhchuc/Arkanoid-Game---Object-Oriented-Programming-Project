package com.example.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
// Bỏ import ImagePattern vì không dùng
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class GameManager {

    // --- Biến thành viên ---
    private final int playAreaWidth = 800;
    private double playAreaOffsetX;
    private int screenWidth, screenHeight;
    private Paddle paddle;
    private Ball ball;
    private List<Brick> bricks = new ArrayList<>();
    private List<PowerUp> powerUps = new ArrayList<>();
    private List<Particle> particles = new ArrayList<>();
    private List<Projectile> projectiles = new ArrayList<>();
    private Random random = new Random();
    private double trailSpawnTimer = 0;
    private static final double TRAIL_SPAWN_INTERVAL = 0.005; // Giảm nhiều
    private int score = 0;
    private int lives = 3;
    private boolean running = false;
    private String playerName;
    private Image backgroundImage;
    private boolean gameOver = false;
    private HighScores highScores;
    private boolean mouseControlled = false;
    private boolean crossBowActive = false;
    private double crossBowTimer = 0.0;
    private final double CROSS_BOW_DURATION = 8.0;
    private double arrowSpawnTimer = 0.0;
    private final double ARROW_SPAWN_INTERVAL = 0.7;
    private final double ARROW_WIDTH = 10;
    private final double ARROW_HEIGHT = 90;
    private String currentLevel = "level1.txt";

    // --- Constructor ---
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

    // --- Khởi tạo Game ---
    public void initGame() {
        paddle = new Paddle(
                playAreaOffsetX + playAreaWidth / 2.0 - 60,
                screenHeight - 40, // Y gốc
                120, // Width gốc
                20,  // Height gốc
                screenWidth
        );
        paddle.setPlayArea(playAreaOffsetX, playAreaWidth);

        int ballRadius = 6; // Giảm kích thước bóng
        ball = new Ball(
                paddle.getX() + paddle.getWidth() / 2 - ballRadius,
                paddle.getY() - (ballRadius * 2),
                ballRadius,
                screenWidth,
                screenHeight
        );
        ball.setPlayArea(playAreaOffsetX, playAreaWidth);

        ball.stickTo(paddle);
        createBricks(currentLevel);
        powerUps.clear();
        particles.clear();
        projectiles.clear();
        crossBowActive = false;
        crossBowTimer = 0.0;
        arrowSpawnTimer = 0.0;
        paddle.setCrossBowActive(false);
        trailSpawnTimer = 0;
        score = 0;
        lives = 3;
        running = false;
        gameOver = false;
        mouseControlled = false;
    }

    // --- Tạo Gạch từ File Level ---
    private void createBricks(String levelFileName) {
        bricks.clear();
        String path = "/com/example/arkanoid/levels/" + levelFileName;
        List<String[]> mapData = new ArrayList<>();
        int cols = 0;

        try (InputStream is = getClass().getResourceAsStream(path);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            if (is == null) {
                System.err.println("Lỗi nghiêm trọng: Không tìm thấy file map: " + path);
                return;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    String[] numbers = line.split("\\s+");
                    mapData.add(numbers);
                    if (cols == 0) {
                        cols = numbers.length;
                    } else if (numbers.length != cols) {
                        System.err.println("Cảnh báo: Hàng trong map file có số cột không đồng đều!");
                    }
                }
            }
        } catch (IOException | NullPointerException e) {
            System.err.println("Lỗi khi đọc file map: " + path);
            e.printStackTrace();
            return;
        }

        if (mapData.isEmpty() || cols == 0) {
            System.err.println("Lỗi: Map file rỗng hoặc không hợp lệ: " + path);
            return;
        }

        int rows = mapData.size();
        double horizontalPadding = 50.0;
        double verticalPaddingTop = 60.0;
        double brickSpacingX = 0.0; // Sát nhau
        double brickSpacingY = 0.0; // Sát nhau

        double brickW = (playAreaWidth - horizontalPadding * 2 - (cols - 1) * brickSpacingX) / cols;
        double brickH = 22;

        for (int r = 0; r < rows; r++) {
            String[] numbers = mapData.get(r);
            for (int c = 0; c < numbers.length; c++) {
                int hitCount = 0;
                try {
                    hitCount = Integer.parseInt(numbers[c]);
                } catch (NumberFormatException e) {
                    System.err.println("Cảnh báo: Ký tự không hợp lệ trong map file tại hàng " + (r + 1) + ", cột " + (c + 1));
                    continue;
                }

                if (hitCount > 0) {
                    double x = playAreaOffsetX + horizontalPadding + c * (brickW + brickSpacingX);
                    double y = verticalPaddingTop + r * (brickH + brickSpacingY);
                    bricks.add(new Brick(x, y, brickW, brickH, hitCount));
                }
            }
        }
        System.out.println("Đã tải thành công map: " + levelFileName + " (" + rows + "x" + cols + ")");
    }

    // --- Các hàm Getter/Setter và Xử lý Input ---
    public void startGame() {
        if (!running && !gameOver) {
            running = true;
            ball.launch();
        }
    }
    public boolean isRunning() { return running; }
    public boolean isGameOver() { return gameOver; }
    public Paddle getPaddle() { return paddle; }
    public void setMouseControl(boolean controlled) { this.mouseControlled = controlled; }
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

    // --- Vòng lặp Update chính ---
    public void update(double dt) {
        // Nếu game over hoặc chưa chạy (và đang không pause), chỉ update paddle và giữ bóng
        if (gameOver || !running) {
            paddle.update(dt); // Cho phép paddle di chuyển cả khi chưa bắt đầu
            ball.stickTo(paddle); // Giữ bóng dính vào paddle
            return;
        }
        // Nếu game đang chạy (running = true)
        paddle.update(dt);
        ball.update(dt);

        // Update Crossbow
        if (crossBowActive) {
            crossBowTimer -= dt;
            arrowSpawnTimer += dt;
            if (arrowSpawnTimer >= ARROW_SPAWN_INTERVAL) { spawnArrow(); arrowSpawnTimer -= ARROW_SPAWN_INTERVAL; }
            if (crossBowTimer <= 0) { crossBowActive = false; paddle.setCrossBowActive(false); System.out.println("Nỏ hết hạn");}
        }
        // Update Trail
        trailSpawnTimer += dt;
        if (trailSpawnTimer >= TRAIL_SPAWN_INTERVAL) { spawnBallTrailParticle(); trailSpawnTimer -= TRAIL_SPAWN_INTERVAL; }
        // Check Ball Out
        if (ball.isOutOfBounds()) { lives--; running = false; powerUps.clear(); SoundManager.playSound(SoundManager.Sound.MISSED_BALL); if (crossBowActive) { crossBowActive = false; paddle.setCrossBowActive(false); } if (lives <= 0) { gameOver = true; saveCurrentScore(); SoundManager.playSound(SoundManager.Sound.GAME_OVER); } else { ball.stickTo(paddle); } }
        // Ball-Paddle Collision
        if (checkCollisionCircleRect(ball, paddle) && ball.getY() + ball.getHeight() <= paddle.getY() + ball.getDy()*dt + 5) { // Thêm ball.getDy()*dt + 5 để tránh kẹt
            ball.bounceOffPaddle(paddle); SoundManager.playSound(SoundManager.Sound.HIT_PADDLE);
        }

        // Ball-Brick Collision
        boolean allBricksDestroyed = true;
        Iterator<Brick> brickIterator = bricks.iterator();
        while(brickIterator.hasNext()) {
            Brick b = brickIterator.next();
            if (b.isDestroyed()) continue;
            allBricksDestroyed = false;
            if (checkCollisionCircleRect(ball, b)) {
                resolveBallBrickCollisionImproved(ball, b); // Gọi hàm xử lý va chạm mới
                SoundManager.playSound(SoundManager.Sound.HIT_BRICK);
                if (!b.isIndestructible() && b.takeHit()) { score += 100; trySpawnPowerUp(b); }
                else if (!b.isIndestructible()) { score += 25; }
                break;
            }
        }
        // Check Win
        if (allBricksDestroyed) { System.out.println("YOU WIN!"); running = false; saveCurrentScore(); SoundManager.playSound(SoundManager.Sound.LEVEL_COMPLETED); if (crossBowActive) { crossBowActive = false; paddle.setCrossBowActive(false); } }
        // Update Powerups
        Iterator<PowerUp> powerUpIterator = powerUps.iterator(); while (powerUpIterator.hasNext()) { PowerUp pu = powerUpIterator.next(); if (pu.isCollected()) { powerUpIterator.remove(); continue; } pu.update(dt); if (checkCollisionRectRect(pu, paddle)) { applyPowerUpEffect(pu); pu.setCollected(true); powerUpIterator.remove(); } else if (pu.getY() > screenHeight) { pu.setCollected(true); powerUpIterator.remove(); } }
        // Update Projectiles
        Iterator<Projectile> projectileIterator = projectiles.iterator(); while (projectileIterator.hasNext()) { Projectile p = projectileIterator.next(); p.update(dt); if (p.getY() + p.getHeight() < 0) p.setDestroyed(true); if (!p.isDestroyed()) { for (Brick b : bricks) { if (!b.isDestroyed() && !b.isIndestructible() && checkCollisionRectRect(p, b)) { // Đạn không phá hủy gạch bất tử
            if (b.takeHit()) { score += 100; trySpawnPowerUp(b); } else { score += 25; } p.setDestroyed(true); SoundManager.playSound(SoundManager.Sound.HIT_BRICK); break; } } } if (p.isDestroyed()) projectileIterator.remove(); }
        // Update Particles
        Iterator<Particle> particleIterator = particles.iterator(); while (particleIterator.hasNext()) { Particle p = particleIterator.next(); p.update(dt); if (p.isExpired()) particleIterator.remove(); }
    }


    // --- Các hàm tiện ích ---
    private void spawnArrow() {
        double arrowX = paddle.getX() + paddle.getWidth() / 2 - ARROW_WIDTH / 2;
        double arrowY = paddle.getY() - ARROW_HEIGHT;
        projectiles.add(new Projectile(arrowX, arrowY, ARROW_WIDTH, ARROW_HEIGHT));
    }

    private void trySpawnPowerUp(Brick b) {
        double dropChance = random.nextDouble();
        double powerUpWidth = 50;
        double powerUpHeight = 70;
        if (dropChance < 0.1) { // 10% LIFE
            powerUps.add(new PowerUp(b.getX() + (b.getWidth() - powerUpWidth) / 2, b.getY() + (b.getHeight() - powerUpHeight) / 2, powerUpWidth, powerUpHeight, PowerUp.PowerUpType.LIFE));
        } else if (dropChance < 0.6) { // 50% LOSE_LIFE
            powerUps.add(new PowerUp(b.getX() + (b.getWidth() - powerUpWidth) / 2, b.getY() + (b.getHeight() - powerUpHeight) / 2, powerUpWidth, powerUpHeight, PowerUp.PowerUpType.LOSE_LIFE));
        } else if (dropChance < 0.7) { // 10% CROSS_BOW
            powerUps.add(new PowerUp(b.getX() + (b.getWidth() - powerUpWidth) / 2, b.getY() + (b.getHeight() - powerUpHeight) / 2, powerUpWidth, powerUpHeight, PowerUp.PowerUpType.CROSS_BOW));
        }
    }

    private void spawnBallTrailParticle() {
        double particleX = ball.getX() + ball.getWidth() / 2;
        double particleY = ball.getY() + ball.getHeight() / 2;
        double particleSize = ball.getWidth() * 1.0; // Tăng kích thước hạt
        Color trailColor = Color.rgb(255, 0, 255, 0.8);
        double lifespan = 0.6; // Tăng thời gian tồn tại
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
            lives++; System.out.println("Bạn nhận được thêm 1 mạng! Tổng mạng: " + lives); SoundManager.playSound(SoundManager.Sound.COLLECT_POWERUP);
        } else if (pu.getType() == PowerUp.PowerUpType.LOSE_LIFE) {
            lives--; System.out.println("Bạn bị mất 1 mạng! Tổng mạng: " + lives); SoundManager.playSound(SoundManager.Sound.MISSED_BALL); // Ví dụ: dùng âm thanh mất bóng
            if (lives <= 0) { gameOver = true; running = false; saveCurrentScore(); SoundManager.playSound(SoundManager.Sound.GAME_OVER); if (crossBowActive) { crossBowActive = false; paddle.setCrossBowActive(false); } }
        } else if (pu.getType() == PowerUp.PowerUpType.CROSS_BOW) {
            if (!crossBowActive) SoundManager.playSound(SoundManager.Sound.COLLECT_POWERUP);
            crossBowActive = true; crossBowTimer = CROSS_BOW_DURATION; paddle.setCrossBowActive(true); System.out.println("Nỏ đã được kích hoạt! " + CROSS_BOW_DURATION + " giây."); arrowSpawnTimer = 0.0;
        }
    }

    // --- HÀM XỬ LÝ VA CHẠM MỚI ---
    private void resolveBallBrickCollisionImproved(Ball ball, Brick brick) {
        // Xử lý riêng cho gạch bất tử: Chỉ đảo chiều, không đẩy
        if (brick.isIndestructible()) {
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
                ball.setX(ball.getX() + Math.signum(ball.getDx()) * 0.1); // Đẩy nhẹ
            } else {
                ball.setDy(-ball.getDy());
                ball.setY(ball.getY() + Math.signum(ball.getDy()) * 0.1); // Đẩy nhẹ
            }
            return; // Không làm gì thêm
        }

        // Logic cho gạch thường: Đẩy ra khỏi vùng chồng lấn
        double ballRadius = ball.getWidth() / 2;
        double ballCenterX = ball.getX() + ballRadius;
        double ballCenterY = ball.getY() + ballRadius;

        double closestX = Math.max(brick.getX(), Math.min(ballCenterX, brick.getX() + brick.getWidth()));
        double closestY = Math.max(brick.getY(), Math.min(ballCenterY, brick.getY() + brick.getHeight()));

        double distX = ballCenterX - closestX;
        double distY = ballCenterY - closestY;
        double distanceSquared = (distX * distX) + (distY * distY);

        if (distanceSquared < (ballRadius * ballRadius)) {
            double distance = Math.sqrt(distanceSquared);
            // Cần kiểm tra distance > 0 để tránh chia cho 0
            double overlap = (distance > 0) ? ballRadius - distance : ballRadius;

            // Đẩy bóng ra
            if (distance > 0) {
                double pushX = (distX / distance) * overlap;
                double pushY = (distY / distance) * overlap;
                ball.setX(ball.getX() + pushX);
                ball.setY(ball.getY() + pushY);
            } else { // Xử lý trường hợp distance = 0 (tâm bóng trùng điểm gần nhất)
                // Đẩy bóng ra theo hướng ngược lại vận tốc trước đó (nếu có)
                // Hoặc đẩy lên trên nếu bóng đứng yên
                double prevDx = ball.getDx();
                double prevDy = ball.getDy();
                double speed = Math.sqrt(prevDx*prevDx + prevDy*prevDy);
                if(speed > 0) {
                    ball.setX(ball.getX() - (prevDx / speed) * overlap * 0.1); // Đẩy nhẹ ngược hướng cũ
                    ball.setY(ball.getY() - (prevDy / speed) * overlap * 0.1);
                } else {
                    ball.setY(ball.getY() - overlap); // Đẩy lên nếu đứng yên
                }
            }

            // Đảo ngược vận tốc dựa trên hướng va chạm chính
            if (Math.abs(distX) > Math.abs(distY)) { // Va chạm ngang
                ball.setDx(-ball.getDx());
            } else { // Va chạm dọc
                ball.setDy(-ball.getDy());
            }
        }
    }
    // --- KẾT THÚC HÀM MỚI ---

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

    // --- Hàm Render ---
    public void render(GraphicsContext gc) {
        if (backgroundImage != null) { gc.drawImage(backgroundImage, 0, 0, screenWidth, screenHeight); }
        else { gc.setFill(Color.BLACK); gc.fillRect(0, 0, screenWidth, screenHeight); }

        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.fillRect(playAreaOffsetX, 0, playAreaWidth, screenHeight);

        // Hiển thị UI
        gc.setFill(Color.WHITE); gc.setFont(new Font("Arial", 16)); gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("Player: " + playerName, screenWidth - 100, 25);
        gc.fillText("Score: " + score, screenWidth - 100, 50);
        gc.fillText("Lives: " + lives, screenWidth - 100, 75);
        if (crossBowActive) { gc.setFill(Color.YELLOW); gc.fillText(String.format("Nỏ: %.1f s", crossBowTimer), screenWidth - 100, 100); }
        gc.setTextAlign(TextAlignment.LEFT);

        // Vẽ các đối tượng game
        for (Brick b : bricks) { if (!b.isDestroyed()) { b.render(gc); } }
        for (PowerUp pu : powerUps) { pu.render(gc); }
        for (Projectile p : projectiles) { p.render(gc); }
        paddle.render(gc);
        for (Particle p : particles) { p.render(gc); }
        ball.render(gc);

        // Vẽ màn hình Game Over
        if (gameOver) {
            gc.setFill(Color.rgb(0, 0, 0, 0.7)); gc.fillRect(0, 0, screenWidth, screenHeight);
            gc.setFill(Color.RED); gc.setFont(new Font("Arial", 80)); gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("GAME OVER", screenWidth / 2.0, screenHeight / 2.0);
            gc.setFill(Color.WHITE); gc.setFont(new Font("Arial", 24));
            gc.fillText("Press R or SPACE to Restart", screenWidth / 2.0, screenHeight / 2.0 + 50);
            gc.setTextAlign(TextAlignment.LEFT);
        }
    }

    // --- PHƯƠNG THỨC MỚI CHO PAUSE/RESUME ---
    /**
     * Tạm dừng logic bên trong GameManager.
     */
    public void pauseGame() {
        if (!gameOver) {
            // Chỉ thay đổi trạng thái running nếu game đang thực sự chạy
            if (this.running) {
                this.running = false; // Đặt running thành false để dừng update logic
            }
        }
    }

    /**
     * Tiếp tục logic bên trong GameManager.
     */
    public void resumeGame() {
        if (!gameOver) {
            // Quan trọng: KHÔNG đặt running thành true ở đây.
            // Nếu bóng đang dính vào paddle, người chơi cần nhấn Space/Click để launch.
            // Nếu bóng đang bay, trạng thái running sẽ được khôi phục khi logic update chạy lại.
            // Chỉ cần đảm bảo AnimationTimer (trong MainController) chạy lại là đủ.
            System.out.println("Game Resumed (logic internal). Cần Space/Click để launch bóng nếu đang dính.");
        }
    }
    // --- KẾT THÚC ---
}