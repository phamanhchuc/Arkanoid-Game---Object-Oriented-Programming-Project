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
import java.util.Random;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

public class GameManager {

    // (Các biến paddle, balls, bricks... không đổi)
    private final int playAreaWidth = 800;
    private double playAreaOffsetX;
    private int screenWidth, screenHeight;
    private Paddle paddle;
    private List<Ball> balls = new ArrayList<>();
    private List<Brick> bricks = new ArrayList<>();
    private List<MovingBrickRow> movingBrickRows = new ArrayList<>();
    private List<RotatingBrickGroup> rotatingBrickGroups = new ArrayList<>();
    private List<PowerUp> powerUps = new ArrayList<>();
    private List<Particle> particles = new ArrayList<>();
    private List<Projectile> projectiles = new ArrayList<>();

    // --- THÊM BIẾN BOSS ---
    private Boss boss; // Thêm 1 con boss

    private double medicineSpawnTimer = 0.0;
    private double medicineSpawnInterval = 1.0; // Thả lần đầu sau 15s
    private final double MEDICINE_WIDTH = 30.0;  // Chỉnh kích thước viên thuốc
    private final double MEDICINE_HEIGHT = 20.0; // Chỉnh kích thước viên thuốc
    private final double BALL_STATE_DURATION = 10.0; // Hiệu ứng kéo dài 10 giây

    private Random random = new Random();
    private double trailSpawnTimer = 0;
    private static final double TRAIL_SPAWN_INTERVAL = 0.03;
    private int score = 0;
    private int lives = 3;
    private boolean running = false;
    private String playerName;

    private List<Image> backgroundImages = new ArrayList<>();
    private List<Image> borderImage = new ArrayList<>();
    private List<Image> leftSideImage = new ArrayList<>();
    private List<Image> rightSideImage = new ArrayList<>();
    private List<Image> scoreBoard = new ArrayList<>();
    private List<Image> angle1 = new ArrayList<>();
    private List<Image> angle2 = new ArrayList<>();

    private Image currentBackgroundImage;
    private Image currentBorderImage;
    private Image currentLeftSideImage;
    private Image currentRightSideImage;
    private Image currentScoreBoardImage;
    private Image currentAngle1Image;
    private Image currentAngle2Image;


    private boolean gameOver = false;
    private boolean levelWon = false;
    private boolean gameWon = false;

    private HighScores highScores;
    private boolean mouseControlled = false;
    private boolean crossBowActive = false;
    private double crossBowTimer = 0.0;
    private final double CROSS_BOW_DURATION = 8.0;
    private double arrowSpawnTimer = 0.0;
    private final double ARROW_SPAWN_INTERVAL = 0.7;
    private final double ARROW_WIDTH = 10;
    private final double ARROW_HEIGHT = 90;

    private int currentLevelIndex = 0;
    private List<String> levelFiles = List.of("level1.txt", "level2.txt", "level3.txt");

    private Map<Integer, BrickFactory> brickFactories;
    private LevelBuilder levelBuilder;

    // --- Constructor (Không đổi) ---
    public GameManager(int screenWidth, int screenHeight, String playerName) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.playerName = playerName != null ? playerName : "Player 1";
        this.playAreaOffsetX = (screenWidth - playAreaWidth) / 2.0;

        try {
            backgroundImages.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/background_1.png")));
            backgroundImages.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/background_2.png")));
            backgroundImages.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/background_3.png")));

            // --- Viền ngoài ---
            borderImage.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/play_Border1.png")));
            borderImage.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/play_Border2.png")));
            borderImage.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/play_Border31.png")));

            // --- Viền trái ---
            leftSideImage.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/BorderSide1.jpg")));
            leftSideImage.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/BorderSide2.jpg")));
            leftSideImage.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/BorderSide3.jpg")));

            // --- Viền phải ---
            rightSideImage.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/BorderSide1.jpg")));
            rightSideImage.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/BorderSide2.jpg")));
            rightSideImage.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/BorderSide3.jpg")));

            // --- Bảng điểm ---
            scoreBoard.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/Score_Board1.png")));
            scoreBoard.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/Score_Board2.png")));
            scoreBoard.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/Score_Board3.png")));
            //--- Phu kien game map ---
            angle1.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/angle1_1.png")));
            angle1.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/angle1_2.png")));
            angle1.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/angle1_31.png")));

            angle2.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/angle2_1.png")));
            angle2.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/angle2_2.png")));
            angle2.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/angle2_3.png")));


        } catch (Exception e) {
            System.err.println("Lỗi: Không thể tải ảnh nền hoặc viền.");
            e.printStackTrace();
        }

        highScores = new HighScores();
        initializeFactories();
        this.levelBuilder = new LevelBuilder(playAreaWidth, playAreaOffsetX, brickFactories);
        initGame();
    }

    // (Hàm initializeFactories không đổi)
    private void initializeFactories() {
        brickFactories = new HashMap<>();
        brickFactories.put(1, new NormalBrickFactory(1));
        brickFactories.put(2, new NormalBrickFactory(2));
        brickFactories.put(3, new NormalBrickFactory(3));
        brickFactories.put(4, new IndestructibleBrickFactory());
        brickFactories.put(5, new NormalBrickFactory(5));
    }


    public void initGame() {
        score = 0;
        lives = 3;
        currentLevelIndex = 0;
        gameWon = false;
        loadLevelData();
    }

    // --- HÀM NÀY ĐÃ SỬA ---
    private void loadLevelData() {
        String levelFile = levelFiles.get(currentLevelIndex);
        int bgIndex = Math.min(currentLevelIndex, backgroundImages.size() - 1);
        currentBackgroundImage = backgroundImages.get(bgIndex);

        int borderIndex = Math.min(currentLevelIndex, borderImage.size() - 1);
        currentBorderImage = borderImage.get(borderIndex);

        int leftIndex = Math.min(currentLevelIndex, leftSideImage.size() - 1);
        currentLeftSideImage = leftSideImage.get(leftIndex);

        int rightIndex = Math.min(currentLevelIndex, rightSideImage.size() - 1);
        currentRightSideImage = rightSideImage.get(rightIndex);

        int scoreIndex = Math.min(currentLevelIndex, scoreBoard.size() - 1);
        currentScoreBoardImage = scoreBoard.get(scoreIndex);

        int angle1Index = Math.min(currentLevelIndex, angle1.size() - 1);
        currentAngle1Image = angle1.get(angle1Index);

        int angle2Index = Math.min(currentLevelIndex, angle2.size() - 1);
        currentAngle2Image = angle2.get(angle2Index);

        if (!levelBuilder.buildLevelBricks(levelFile, bricks, movingBrickRows, rotatingBrickGroups)) {
            System.err.println("Không thể tải cấp độ: " + levelFile);
            // Xử lý lỗi, có thể về menu chính hoặc thoát game
        }
        paddle = new Paddle(
                playAreaOffsetX + playAreaWidth / 2.0 - 60,
                screenHeight - 40, 120, 20, screenWidth
        );
        paddle.setPlayArea(playAreaOffsetX, playAreaWidth);

        balls.clear();
        powerUps.clear();
        particles.clear();
        projectiles.clear();
        boss = null; // Xóa boss cũ (nếu có)
        medicineSpawnTimer = 0.0;
        int ballRadius = 6;
        Ball newBall = new Ball(
                paddle.getX() + paddle.getWidth() / 2 - ballRadius,
                paddle.getY() - (ballRadius * 2),
                ballRadius, screenWidth, screenHeight
        );
        newBall.setPlayArea(playAreaOffsetX, playAreaWidth);
        newBall.stickTo(paddle);
        balls.add(newBall);

        // --- THÊM KHỞI TẠO BOSS ---
        // Chỉ tạo boss nếu là Màn 1 (index = 0)
        if (currentLevelIndex == 0) {
            // Tạo boss ở tọa độ Y=20 (trên cùng), kích thước 80x60 (bạn có thể đổi)
            boss = new Boss(playAreaOffsetX, 20, 80, 60);
            boss.setPlayArea(playAreaOffsetX, playAreaWidth);
        }
        // --- KẾT THÚC THÊM ---

        crossBowActive = false;
        crossBowTimer = 0.0;
        arrowSpawnTimer = 0.0;
        paddle.setCrossBowActive(false);
        trailSpawnTimer = 0;
        running = false;
        gameOver = false;
        levelWon = false;
        mouseControlled = false;

        // ===============================================
        // ===== BẮT ĐẦU SỬA: CHỈ PHÁT NHẠC MÀN 1 =====
        // ===============================================
        // PHÁT NHẠC THEO LEVEL
        if (currentLevelIndex == 0) {
            SoundManager.playMusic(SoundManager.Music.LEVEL1);
        }
        /* (XÓA BỎ else if cho Màn 2 và Màn 3 khỏi đây)
        else if (currentLevelIndex == 1) {
            SoundManager.playMusic(SoundManager.Music.LEVEL2);
        } else if (currentLevelIndex == 2) {
            SoundManager.playMusic(SoundManager.Music.LEVEL3);
        }
        */
        // ===============================================
        // ===== KẾT THÚC SỬA                             =====
        // ===============================================
    }

    // (Các hàm resetCurrentLevel, nextLevel, createBricks... không đổi)
    private void resetCurrentLevel() {
        running = false;
        powerUps.clear();
        projectiles.clear();
        crossBowActive = false;
        paddle.setCrossBowActive(false);
        balls.clear();
        int ballRadius = 6;
        Ball newBall = new Ball(
                paddle.getX() + paddle.getWidth() / 2 - ballRadius,
                paddle.getY() - (ballRadius * 2),
                ballRadius, screenWidth, screenHeight
        );
        newBall.setPlayArea(playAreaOffsetX, playAreaWidth);
        newBall.stickTo(paddle);
        balls.add(newBall);
        applyBallState(Ball.BallState.NORMAL);
    }

    public void nextLevel() {
        currentLevelIndex++;
        if (currentLevelIndex >= levelFiles.size()) {
            System.out.println("Đã thắng TOÀN BỘ game!");
            gameWon = true;
        } else {
            loadLevelData();
        }
    }

    public boolean hasWonLevel() {
        return levelWon;
    }

    public boolean hasWonGame() {
        return gameWon;
    }

    public int getCurrentLevelIndex() {
        return currentLevelIndex;
    }

    public void startGame() {
        if (!running && !gameOver) {
            running = true;
            for (Ball b : balls) {
                b.launch();
            }
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

    // --- HÀM UPDATE (ĐÃ SỬA) ---
    public void update(double dt) {
        // --- THÊM UPDATE BOSS ---
        // (Boss di chuyển ngay cả khi game chưa bắt đầu)
        if (boss != null) {
            boss.update(dt);
        }
        if (boss != null && running) {
            medicineSpawnTimer += dt;
            if (medicineSpawnTimer >= medicineSpawnInterval) {
                // Thả thuốc ở giữa, bên dưới boss
                spawnMedicine(boss.getX() + boss.getWidth() / 2 - (MEDICINE_WIDTH / 2),
                        boss.getY() + boss.getHeight());

                medicineSpawnTimer = 0.0; // Reset timer
                // Lần thả tiếp theo ngẫu nhiên từ 15-20s
                medicineSpawnInterval = 15.0 + random.nextDouble() * 5.0;
            }
        }
        // --- KẾT THÚC THÊM ---

        if (gameOver || !running) {
            paddle.update(dt);
            for (Ball b : balls) {
                b.stickTo(paddle);
            }
            return;
        }

        paddle.update(dt);
        for (Ball b : balls) {
            b.update(dt);
        }
        for (MovingBrickRow row : movingBrickRows) {
            row.update(dt);
        }
        for (RotatingBrickGroup group : rotatingBrickGroups) {
            group.update(dt);
        }

        // (Code crossbow, trail, ball-out-of-bounds... không đổi)
        if (crossBowActive) {
            crossBowTimer -= dt;
            arrowSpawnTimer += dt;
            if (arrowSpawnTimer >= ARROW_SPAWN_INTERVAL) {
                spawnArrow();
                arrowSpawnTimer -= ARROW_SPAWN_INTERVAL;
            }
            if (crossBowTimer <= 0) {
                crossBowActive = false;
                paddle.setCrossBowActive(false);
                System.out.println("Nỏ hết hạn");
            }
        }
        trailSpawnTimer += dt;
        if (trailSpawnTimer >= TRAIL_SPAWN_INTERVAL) {
            for (Ball b : balls) {
                spawnBallTrailParticle(b);
            }
            trailSpawnTimer -= TRAIL_SPAWN_INTERVAL;
        }
        Iterator<Ball> ballIterator = balls.iterator();
        while (ballIterator.hasNext()) {
            Ball b = ballIterator.next();
            if (b.isOutOfBounds()) {
                ballIterator.remove();
                if (balls.isEmpty()) {
                    SoundManager.playSound(SoundManager.Sound.MISSED_BALL);
                }
            }
        }
        if (balls.isEmpty()) {
            lives--;
            if (lives <= 0) {
                gameOver = true;
                saveCurrentScore();
                SoundManager.playSound(SoundManager.Sound.GAME_OVER);
            } else {
                resetCurrentLevel();
            }
        }
        for (Ball b : balls) {
            if (checkCollisionCircleRect(b, paddle) && b.getY() + b.getHeight() <= paddle.getY() + b.getDy() * dt + 5) {
                b.handleCollision(paddle, this);
            }
        }
        boolean allBricksDestroyed = true;
        Iterator<Brick> brickIterator = bricks.iterator();
        while (brickIterator.hasNext()) {
            Brick b = brickIterator.next();
            if (b.isDestroyed()) continue;
            if (!b.isIndestructible()) {
                allBricksDestroyed = false;
            }
            for (Ball ball : balls) {
                if (checkCollisionCircleRect(ball, b)) {
                    ball.handleCollision(b, this);
                    break;
                }
            }
        }
        if (allBricksDestroyed && !levelWon) {
            System.out.println("YOU WIN!");
            running = false;
            levelWon = true;
            saveCurrentScore();
            SoundManager.playSound(SoundManager.Sound.LEVEL_COMPLETED);
            if (crossBowActive) {
                crossBowActive = false;
                paddle.setCrossBowActive(false);
            }
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
        Iterator<Projectile> projectileIterator = projectiles.iterator();
        while (projectileIterator.hasNext()) {
            Projectile p = projectileIterator.next();
            p.update(dt);
            if (p.getY() + p.getHeight() < 0) {
                p.setDestroyed(true);
            }
            if (!p.isDestroyed()) {
                for (Brick b : bricks) {
                    if (b.isDestroyed() || b.isIndestructible()) continue;
                    if (p.isPiercing() && p.hasHitBrick(b)) {
                        continue;
                    }
                    if (checkCollisionRectRect(p, b)) {
                        if (p.isPiercing()) {
                            boolean destroyed = b.takeHit();
                            if (destroyed) {
                                score += 100;
                                trySpawnPowerUp(b);
                            } else {
                                score += 25;
                            }
                            SoundManager.playSound(SoundManager.Sound.HIT_BRICK);
                            p.addHitBrick(b);
                            continue;
                        } else {
                            if (b.takeHit()) {
                                score += 100;
                                trySpawnPowerUp(b);
                            } else {
                                score += 25;
                            }
                            p.setDestroyed(true);
                            SoundManager.playSound(SoundManager.Sound.HIT_BRICK);
                            break;
                        }
                    }
                }
            }
            if (p.isDestroyed()) {
                projectileIterator.remove();
            }
        }
        Iterator<Particle> particleIterator = particles.iterator();
        while (particleIterator.hasNext()) {
            Particle p = particleIterator.next();
            p.update(dt);
            if (p.isExpired()) particleIterator.remove();
        }
    }

    // (Các hàm tiện ích không đổi)
    public void addScore(int points) {
        this.score += points;
    }

    public void spawnPowerUpFromBrick(Brick b) {
        double powerUpWidth = 50;
        double powerUpHeight = 70;
        double x = b.getX() + (b.getWidth() - powerUpWidth) / 2;
        double y = b.getY() + (b.getHeight() - powerUpHeight) / 2;
        PowerUp powerUp = PowerUpFactory.createRandomPowerUp(x, y, powerUpWidth, powerUpHeight);
        if (powerUp != null) {
            powerUps.add(powerUp);
        }
    }

    private void trySpawnPowerUp(Brick b) {
        spawnPowerUpFromBrick(b);
    }

    private void spawnArrow() {
        double arrowX = paddle.getX() + paddle.getWidth() / 2;
        double arrowY = paddle.getY();
        projectiles.add(new Projectile(arrowX, arrowY, ARROW_WIDTH, ARROW_HEIGHT, false));
    }

    private void spawnBallTrailParticle(Ball b) {
        double particleX = b.getX() + b.getWidth() / 2;
        double particleY = b.getY() + b.getHeight() / 2;
        double particleSize = b.getWidth() * 1.0;
        Color trailColor;
        switch (b.getState()) {
            case ICE:
                trailColor = Color.rgb(0, 255, 255, 0.8); // Màu Xanh Băng
                break;
            case FIRE:
                trailColor = Color.rgb(255, 100, 0, 0.8); // Màu Lửa
                break;
            default:
            case NORMAL:
                trailColor = Color.rgb(255, 0, 255, 0.8); // Màu gốc
                break;
        }
        double lifespan = 0.6;
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
        return r1.getX() < r2.getX() + r2.getWidth() && r1.getX() + r1.getWidth() > r2.getX() && r1.getY() < r2.getY() + r2.getHeight() && r1.getY() + r1.getHeight() > r2.getY();
    }

    private void applyPowerUpEffect(PowerUp pu) {
        if (pu.getType() == PowerUp.PowerUpType.LIFE) {
            lives++;
            System.out.println("Bạn nhận được thêm 1 mạng! Tổng mạng: " + lives);
            SoundManager.playSound(SoundManager.Sound.COLLECT_POWERUP);
        } else if (pu.getType() == PowerUp.PowerUpType.LOSE_LIFE) {
            lives--;
            System.out.println("Bạn bị mất 1 mạng! Tổng mạng: " + lives);
            SoundManager.playSound(SoundManager.Sound.MISSED_BALL);
            if (lives <= 0) {
                gameOver = true;
                running = false;
                saveCurrentScore();
                SoundManager.playSound(SoundManager.Sound.GAME_OVER);
                if (crossBowActive) {
                    crossBowActive = false;
                    paddle.setCrossBowActive(false);
                }
            }
        } else if (pu.getType() == PowerUp.PowerUpType.CROSS_BOW) {
            if (!crossBowActive) SoundManager.playSound(SoundManager.Sound.COLLECT_POWERUP);
            crossBowActive = true;
            crossBowTimer = CROSS_BOW_DURATION;
            paddle.setCrossBowActive(true);
            System.out.println("Nỏ đã được kích hoạt! " + CROSS_BOW_DURATION + " giây.");
            arrowSpawnTimer = 0.0;
        } else if (pu.getType() == PowerUp.PowerUpType.MULTI_BALL) {
            if (!balls.isEmpty()) {
                SoundManager.playSound(SoundManager.Sound.COLLECT_POWERUP);
                System.out.println("Multi-Ball kích hoạt!");
                Ball sourceBall = balls.get(0);
                double radius = sourceBall.getWidth() / 2.0;
                double sourceCenterX = sourceBall.getX() + radius;
                double sourceCenterY = sourceBall.getY() + radius;
                List<Ball> newBallsList = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    Ball newBall = new Ball(sourceCenterX, sourceCenterY, radius, screenWidth, screenHeight);
                    newBall.setPlayArea(playAreaOffsetX, playAreaWidth);
                    double angleDegrees = 60.0 + (random.nextDouble() * 60.0);
                    newBall.launchAtAngle(angleDegrees);
                    newBallsList.add(newBall);
                }
                balls.addAll(newBallsList);
            }
        } else if (pu.getType() == PowerUp.PowerUpType.PIERCING_SHOT) {
            System.out.println("Kích hoạt Meomeo Bullet!");
            SoundManager.playSound(SoundManager.Sound.COLLECT_POWERUP);
            double bulletX = paddle.getX() + paddle.getWidth() / 2;
            double bulletY = paddle.getY();
            double placeholderWidth = 10;
            double placeholderHeight = 50;
            projectiles.add(new Projectile(bulletX, bulletY, placeholderWidth, placeholderHeight, true));
        } else if (pu.getType() == PowerUp.PowerUpType.MEDICINE) {
            SoundManager.playSound(SoundManager.Sound.COLLECT_POWERUP);

            if (random.nextDouble() < 0.5) {
                // Trường hợp 1: BĂNG
                System.out.println("Kích hoạt trạng thái BĂNG!");
                applyBallState(Ball.BallState.ICE);
            } else {
                // Trường hợp 2: LỬA
                System.out.println("Kích hoạt trạng thái LỬA!");
                applyBallState(Ball.BallState.FIRE);
            }
        }
    }

    private void applyBallState(Ball.BallState state) {
        for (Ball b : balls) {
            b.setBallState(state, BALL_STATE_DURATION);
        }
    }

    // --- THÊM HÀM MỚI 2 ---

    /**
     * Tạo ra PowerUp "Thuốc" tại một vị trí.
     */
    public void spawnMedicine(double x, double y) {
        PowerUp powerUp = new PowerUp(x, y, MEDICINE_WIDTH, MEDICINE_HEIGHT, PowerUp.PowerUpType.MEDICINE);
        powerUps.add(powerUp);
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

    // --- HÀM RENDER (ĐÃ SỬA) ---
    public void render(GraphicsContext gc) {
        if (currentBackgroundImage != null) {
            gc.drawImage(currentBackgroundImage, 0, 0, screenWidth, screenHeight);
        } else {
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, screenWidth, screenHeight);
        }
        // --- THÊM RENDER BOSS ---
        // (Vẽ boss *sau* nền đen, nhưng *trước* gạch)
        if (boss != null) {
            boss.render(gc);
        }
        // --- KẾT THÚC THÊM ---

        // (Code vẽ UI, gạch, bóng... không đổi)
        gc.setFill(Color.WHITE);
        gc.setFont(new Font("Arial", 16));
        gc.setTextAlign(TextAlignment.CENTER);
        if (crossBowActive) {
            gc.setFill(Color.YELLOW);
            gc.fillText(String.format("Nỏ: %.1f s", crossBowTimer), screenWidth - 100, 100);
        }
        gc.setTextAlign(TextAlignment.LEFT);
        for (Brick b : bricks) {
            if (!b.isDestroyed()) {
                b.render(gc);
            }
        }
        for (PowerUp pu : powerUps) {
            pu.render(gc);
        }
        for (Projectile p : projectiles) {
            p.render(gc);
        }
        paddle.render(gc);
        for (Particle p : particles) {
            p.render(gc);
        }
        for (Ball b : balls) {
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
        if (currentLeftSideImage != null && !currentLeftSideImage.isError()) {
            gc.drawImage(currentLeftSideImage, playAreaOffsetX - currentLeftSideImage.getWidth() - 10, 0, currentLeftSideImage.getWidth(), screenHeight);
        }

        if (currentRightSideImage != null && !currentRightSideImage.isError()) {
            gc.drawImage(currentRightSideImage, playAreaOffsetX + playAreaWidth + 10, 0, currentRightSideImage.getWidth(), screenHeight);
        }

        if (currentBorderImage != null && !currentBorderImage.isError()) {
            gc.drawImage(currentBorderImage, 0, 0, screenWidth, screenHeight);
        }

        if (currentScoreBoardImage != null && !currentScoreBoardImage.isError()) {
            gc.drawImage(currentScoreBoardImage, 870, 60, 500, 840);
        }
        if (currentAngle1Image != null) {
            gc.drawImage(currentAngle1Image, -50, 400, 460, 840);
        }
        if (currentAngle2Image != null) {
            gc.drawImage(currentAngle2Image, 0, 0, 250, 240);
        }


//  Reset lại màu chữ và font sau khi vẽ particle

        gc.setFill(Color.WHITE);              // ✅ Đặt lại màu trắng hoặc màu bạn muốn
        gc.setFont(new Font("Arial Bold", 16));
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);

// Vẽ text len bang diem nhe hehehehehehe
        gc.strokeText("Player: " + playerName, screenWidth - 185, 190);
        gc.fillText("Player: " + playerName, screenWidth - 185, 190);

        gc.strokeText("Score: " + score, screenWidth - 185, 230);
        gc.fillText("Score: " + score, screenWidth - 185, 230);

        gc.strokeText("Lives: " + lives, screenWidth - 185, 270);
        gc.fillText("Lives: " + lives, screenWidth - 185, 270);

    }

    public void pauseGame() {
        if (!gameOver) {
        }
    }

    public void resumeGame() {
        if (!gameOver) {
            System.out.println("Game Resumed (logic internal).");
        }
    }
}