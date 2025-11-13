package com.example.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.*;

public class GameManager {

    // --- CẤU HÌNH GAME ---
    private final int playAreaWidth = 800;
    private double playAreaOffsetX;
    private int screenWidth, screenHeight;

    // --- CÁC ĐỐI TƯỢNG GAME ---
    private Paddle paddle;
    private List<Ball> balls = new ArrayList<>();
    private List<Brick> bricks = new ArrayList<>();
    private List<MovingBrickRow> movingBrickRows = new ArrayList<>();
    private List<RotatingBrickGroup> rotatingBrickGroups = new ArrayList<>();
    private List<PowerUp> powerUps = new ArrayList<>();
    private List<Particle> particles = new ArrayList<>();
    private List<Projectile> projectiles = new ArrayList<>();

    // BOSS
    private MovableObject currentBoss;

    // --- TRẠNG THÁI GAME ---
    private int score = 0;
    private int lives = 3;
    private boolean running = false;
    private boolean gameOver = false;
    private boolean levelWon = false;
    private boolean gameWon = false;
    private int currentLevelIndex = 0;
    private String playerName;

    // --- TÀI NGUYÊN ẢNH ---
    private List<Image> backgroundImages = new ArrayList<>();
    private List<Image> borderImage = new ArrayList<>();
    private List<Image> leftSideImage = new ArrayList<>();
    private List<Image> rightSideImage = new ArrayList<>();
    private List<Image> scoreBoard = new ArrayList<>();

    private Image currentBackgroundImage;
    private Image currentBorderImage;
    private Image currentLeftSideImage;
    private Image currentRightSideImage;
    private Image currentScoreBoardImage;

    // --- HỆ THỐNG ---
    private HighScores highScores;
    private boolean mouseControlled = false;
    private Map<Integer, BrickFactory> brickFactories;
    private LevelBuilder levelBuilder;
    private List<String> levelFiles = List.of("level1.txt", "level2.txt", "level3.txt");

    // --- LOGIC BỔ TRỢ ---
    private Random random = new Random();
    private double trailSpawnTimer = 0;

    public GameManager(int screenWidth, int screenHeight, String playerName) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.playerName = playerName != null ? playerName : "Player 1";
        this.playAreaOffsetX = (screenWidth - playAreaWidth) / 2.0;

        loadResources();

        highScores = new HighScores();
        initializeFactories();
        this.levelBuilder = new LevelBuilder(playAreaWidth, playAreaOffsetX, brickFactories);

        initGame();
    }

    private void loadResources() {
        try {
            // Backgrounds
            backgroundImages.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/background_1.png")));
            backgroundImages.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/background_2.png")));
            backgroundImages.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/background_3.png")));

            // Borders
            borderImage.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/play_Border1.png")));
            borderImage.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/play_Border2.png")));
            borderImage.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/play_Border31.png")));

            // Side Borders
            leftSideImage.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/BorderSide1.jpg")));
            leftSideImage.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/BorderSide2.jpg")));
            leftSideImage.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/BorderSide3.jpg")));

            rightSideImage.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/BorderSide1.jpg")));
            rightSideImage.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/BorderSide2.jpg")));
            rightSideImage.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/BorderSide3.jpg")));

            // Scoreboard
            scoreBoard.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/Score_Board1.png")));
            scoreBoard.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/Score_Board2.png")));
            scoreBoard.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/Score_Board3.png")));

        } catch (Exception e) {
            System.err.println("GameManager: Lỗi tải ảnh nền/viền.");
        }
    }

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

        // Phát nhạc màn 1
        SoundManager.playMusic(SoundManager.Music.LEVEL1);
    }

    private void loadLevelData() {
        // 1. Cập nhật ảnh nền theo Level
        int bgIndex = Math.min(currentLevelIndex, backgroundImages.size() - 1);
        currentBackgroundImage = backgroundImages.get(bgIndex);
        currentBorderImage = borderImage.get(Math.min(currentLevelIndex, borderImage.size()-1));
        currentLeftSideImage = leftSideImage.get(Math.min(currentLevelIndex, leftSideImage.size()-1));
        currentRightSideImage = rightSideImage.get(Math.min(currentLevelIndex, rightSideImage.size()-1));
        currentScoreBoardImage = scoreBoard.get(Math.min(currentLevelIndex, scoreBoard.size()-1));

        // 2. Load Map
        if (currentLevelIndex < levelFiles.size()) {
            String levelFile = levelFiles.get(currentLevelIndex);
            if (!levelBuilder.buildLevelBricks(levelFile, bricks, movingBrickRows, rotatingBrickGroups)) {
                System.err.println("Không thể tải cấp độ: " + levelFile);
            }
        }

        // 3. Reset Đối tượng
        paddle = new Paddle(playAreaOffsetX + playAreaWidth / 2.0 - 60, screenHeight - 40, 120, 20, screenWidth);
        paddle.setPlayArea(playAreaOffsetX, playAreaWidth);

        balls.clear();
        powerUps.clear();
        particles.clear();
        projectiles.clear();
        currentBoss = null;

        resetBall();
        setupBossForLevel();

        running = false;
        gameOver = false;
        levelWon = false;
        mouseControlled = false;
    }

    private void setupBossForLevel() {
        if (currentLevelIndex == 0) {
            // Boss Màn 1 (Cũ)
            currentBoss = new Boss(playAreaOffsetX, 20, 80, 60);
            currentBoss.setPlayArea(playAreaOffsetX, playAreaWidth);
        } else if (currentLevelIndex == 1) {
            // Boss Màn 2
            currentBoss = new BossLevel2(playAreaOffsetX, 20, 100, 70, 50, playAreaOffsetX, playAreaWidth);
            BossLevel2.GameManagerHolder.INSTANCE = this;
        } else if (currentLevelIndex == 2) {
            // Boss Màn 3 (Rồng)
            double boss3Width = 280;
            double boss3Height = 250;
            double boss3X = playAreaOffsetX + playAreaWidth / 2 - boss3Width / 2;
            currentBoss = new BossLevel3(boss3X, -20, boss3Width, boss3Height, 1000, playAreaOffsetX, playAreaWidth);
            BossLevel3.GameManagerHolder.INSTANCE = this;

            // Thêm tim rồng vào danh sách gạch để bóng va chạm
            if (currentBoss instanceof BossLevel3) {
                bricks.add(((BossLevel3)currentBoss).getHeartBrick());
            }
        }
    }

    private void resetBall() {
        int ballRadius = 6;
        Ball newBall = new Ball(paddle.getX() + paddle.getWidth() / 2 - ballRadius, paddle.getY() - (ballRadius * 2), ballRadius, screenWidth, screenHeight);
        newBall.setPlayArea(playAreaOffsetX, playAreaWidth);
        newBall.stickTo(paddle);
        balls.add(newBall);
    }

    // ==================================================================
    //  CORE UPDATE LOOP (LOGIC CHÍNH CỦA GAME)
    // ==================================================================
    public void update(double dt) {
        // 1. Update Boss
        if (currentBoss != null) {
            currentBoss.update(dt);
        }

        // MÀN 3: Kiểm tra va chạm Laser Boss với Paddle -> Làm chậm
        if (currentBoss instanceof BossLevel3) {
            BossLevel3 b3 = (BossLevel3) currentBoss;
            if (b3.checkLaserCollision(paddle)) {
                paddle.applySlow(0.5); // Giảm tốc trong 0.5s
            }
        }

        if (gameOver || !running) {
            paddle.update(dt);
            for (Ball b : balls) b.stickTo(paddle);
            return;
        }

        // 2. Update các đối tượng
        paddle.update(dt);
        for (Ball b : balls) b.update(dt);
        for (MovingBrickRow row : movingBrickRows) row.update(dt);
        for (RotatingBrickGroup group : rotatingBrickGroups) group.update(dt);

        // 3. Update PowerUps & Particles & Projectiles
        updatePowerUps(dt);
        updateParticles(dt);
        updateProjectiles(dt);

        // 4. Xử lý Bóng rơi
        handleBallOutOfBounds();

        // 5. Xử lý Va chạm
        handleCollisions(dt);

        // 6. KIỂM TRA ĐIỀU KIỆN THẮNG
        checkWinCondition();
    }

    private void handleCollisions(double dt) {
        // Ball vs Paddle
        for (Ball b : balls) {
            if (checkCollisionCircleRect(b, paddle) && b.getY() + b.getHeight() <= paddle.getY() + b.getDy() * dt + 5) {
                b.handleCollision(paddle, this);
            }
        }

        // Ball vs Boss 2
        if (currentBoss instanceof BossLevel2) {
            BossLevel2 bossL2 = (BossLevel2) currentBoss;
            for (Ball b : balls) {
                if (bossL2.checkCollisionWithBall(b)) {
                    b.handleCollision(bossL2, this);
                    int damage = (b.getState() == Ball.BallState.FIRE) ? 2 : 1;

                    if (!bossL2.takeDamage(damage)) {
                        // Boss chết -> Gọi hàm xử lý
                        handleBossDefeated(bossL2);
                        // KHÔNG set levelWon = true ở đây cho màn 2
                    }
                }
            }
        }

        // Ball vs Bricks
        Iterator<Brick> brickIterator = bricks.iterator();
        while (brickIterator.hasNext()) {
            Brick b = brickIterator.next();
            if (b.isDestroyed()) continue;

            for (Ball ball : balls) {
                if (checkCollisionCircleRect(ball, b)) {
                    ball.handleCollision(b, this); // Logic trừ máu gạch/cộng điểm nằm trong Strategy
                    break;
                }
            }
        }
    }

    // --- HÀM MỚI: Xử lý khi Boss bị tiêu diệt ---
    // Đây là hàm bạn bị thiếu gây lỗi Cannot find symbol
    public void handleBossDefeated(BossLevel2 boss) {
        if (currentBoss == boss) {
            currentBoss = null; // Xóa boss khỏi màn chơi
            addScore(5000);
            System.out.println("Boss defeated via callback.");
        }
    }

    private void checkWinCondition() {
        if (levelWon) return;

        // Kiểm tra gạch
        boolean allBricksDestroyed = true;
        for (Brick b : bricks) {
            if (!b.isDestroyed() && !b.isIndestructible()) {
                allBricksDestroyed = false;
                break;
            }
        }

        boolean conditionMet = false;

        if (currentLevelIndex == 1) {
            // MÀN 2: Thắng khi (Hết gạch) VÀ (Boss đã chết)
            if (allBricksDestroyed && currentBoss == null) {
                conditionMet = true;
            }
        } else if (currentLevelIndex == 2) {
            // MÀN 3: Thắng khi Boss chết
            if (currentBoss == null || ((BossLevel3)currentBoss).getHp() <= 0) {
                // Logic thắng màn 3 thường xử lý trực tiếp khi trừ HP
            }
        } else {
            // MÀN 1: Chỉ cần hết gạch
            if (allBricksDestroyed) {
                conditionMet = true;
            }
        }

        if (conditionMet) {
            levelWon = true;
            running = false;
            saveCurrentScore();
            SoundManager.playSound(SoundManager.Sound.LEVEL_COMPLETED);
            paddle.setCrossBowActive(false);
            System.out.println("LEVEL CLEARED!");
        }
    }

    private void handleBallOutOfBounds() {
        Iterator<Ball> ballIterator = balls.iterator();
        while (ballIterator.hasNext()) {
            Ball b = ballIterator.next();
            if (b.isOutOfBounds()) {
                ballIterator.remove();
                if (balls.isEmpty()) SoundManager.playSound(SoundManager.Sound.MISSED_BALL);
            }
        }
        if (balls.isEmpty()) {
            handleLoseLife();
        }
    }

    private void handleLoseLife() {
        lives--;
        if (lives <= 0) {
            gameOver = true;
            running = false;
            saveCurrentScore();
            SoundManager.playSound(SoundManager.Sound.GAME_OVER);
        } else {
            resetCurrentLevel();
        }
    }

    private void updatePowerUps(double dt) {
        Iterator<PowerUp> it = powerUps.iterator();
        while (it.hasNext()) {
            PowerUp pu = it.next();
            if (pu.isCollected()) { it.remove(); continue; }
            pu.update(dt);

            if (checkCollisionRectRect(pu, paddle)) {
                applyPowerUpEffect(pu);
                pu.setCollected(true);
                it.remove();
            } else if (pu.getY() > screenHeight) {
                pu.setCollected(true);
                it.remove();
            }
        }
    }

    private void applyPowerUpEffect(PowerUp pu) {
        switch (pu.getType()) {
            case LIFE:
                lives++;
                SoundManager.playSound(SoundManager.Sound.COLLECT_POWERUP);
                break;
            case LOSE_LIFE:
                handleLoseLife();
                break;
            case CROSS_BOW:
                paddle.setCrossBowActive(true);
                SoundManager.playSound(SoundManager.Sound.COLLECT_POWERUP);
                break;
            case MULTI_BALL:
                spawnMultiBall();
                SoundManager.playSound(SoundManager.Sound.COLLECT_POWERUP);
                break;
            case PIERCING_SHOT:
                spawnPiercingShot();
                SoundManager.playSound(SoundManager.Sound.COLLECT_POWERUP);
                break;
            case MEDICINE:
                if (random.nextBoolean()) applyBallState(Ball.BallState.ICE);
                else applyBallState(Ball.BallState.FIRE);
                SoundManager.playSound(SoundManager.Sound.COLLECT_POWERUP);
                break;
            case STUN_PADDLE:
                System.out.println("Dính đạn Boss -> Mất mạng!");
                handleLoseLife();
                break;
        }
    }

    private void spawnMultiBall() {
        if (!balls.isEmpty()) {
            Ball source = balls.get(0);
            for (int i = 0; i < 2; i++) {
                Ball nb = new Ball(source.getX(), source.getY(), source.getWidth()/2, screenWidth, screenHeight);
                nb.setPlayArea(playAreaOffsetX, playAreaWidth);
                nb.launchAtAngle(random.nextDouble() * 180);
                balls.add(nb);
            }
        }
    }

    private void spawnPiercingShot() {
        double bx = paddle.getX() + paddle.getWidth()/2;
        projectiles.add(new Projectile(bx, paddle.getY(), 10, 50, true));
    }

    private void applyBallState(Ball.BallState state) {
        for (Ball b : balls) b.setBallState(state, 10.0);
    }

    // --- HELPER & GETTERS ---
    public void resetCurrentLevel() {
        running = false;
        balls.clear();
        powerUps.clear();
        projectiles.clear();
        paddle.setCrossBowActive(false);
        resetBall();
    }

    public void nextLevel() {
        currentLevelIndex++;
        if (currentLevelIndex >= levelFiles.size()) {
            gameWon = true;
        } else {
            loadLevelData();
        }
    }

    private boolean checkCollisionCircleRect(Ball ball, GameObject rect) {
        double cx = ball.getX() + ball.getWidth()/2;
        double cy = ball.getY() + ball.getHeight()/2;
        double r = ball.getWidth()/2;
        double clX = Math.max(rect.getX(), Math.min(cx, rect.getX()+rect.getWidth()));
        double clY = Math.max(rect.getY(), Math.min(cy, rect.getY()+rect.getHeight()));
        double dx = cx - clX;
        double dy = cy - clY;
        return (dx*dx + dy*dy) <= (r*r);
    }

    private boolean checkCollisionRectRect(GameObject r1, GameObject r2) {
        return r1.getX() < r2.getX() + r2.getWidth() &&
                r1.getX() + r1.getWidth() > r2.getX() &&
                r1.getY() < r2.getY() + r2.getHeight() &&
                r1.getY() + r1.getHeight() > r2.getY();
    }

    // Spawn logic
    public void addPowerUp(PowerUp pu) { powerUps.add(pu); }
    public void spawnPowerUpFromBrick(Brick b) {
        PowerUp pu = PowerUpFactory.createRandomPowerUp(b.getX(), b.getY(), 50, 50);
        if (pu != null) powerUps.add(pu);
    }
    public void spawnMedicine(double x, double y) {
        powerUps.add(new PowerUp(x, y, 30, 20, PowerUp.PowerUpType.MEDICINE));
    }

    // Render
    public void render(GraphicsContext gc) {
        // 1. Background
        if (currentBackgroundImage != null) gc.drawImage(currentBackgroundImage, 0, 0, screenWidth, screenHeight);
        else { gc.setFill(Color.BLACK); gc.fillRect(0, 0, screenWidth, screenHeight); }

        // 2. Boss
        if (currentBoss != null) currentBoss.render(gc);

        // 3. Objects
        for (Brick b : bricks) b.render(gc);
        for (PowerUp p : powerUps) p.render(gc);
        for (Projectile p : projectiles) p.render(gc);
        for (Particle p : particles) p.render(gc);
        paddle.render(gc);
        for (Ball b : balls) b.render(gc);

        // 4. Borders & UI
        if (currentLeftSideImage != null) gc.drawImage(currentLeftSideImage, playAreaOffsetX - currentLeftSideImage.getWidth() - 10, 0, currentLeftSideImage.getWidth(), screenHeight);
        if (currentRightSideImage != null) gc.drawImage(currentRightSideImage, playAreaOffsetX + playAreaWidth + 10, 0, currentRightSideImage.getWidth(), screenHeight);
        if (currentBorderImage != null) gc.drawImage(currentBorderImage, 0, 0, screenWidth, screenHeight);
        if (currentScoreBoardImage != null) gc.drawImage(currentScoreBoardImage, 870, 60, 500, 840);

        // 5. Text Info
        gc.setFill(Color.WHITE);
        gc.setFont(new Font("Arial Bold", 16));
        gc.fillText("Player: " + playerName, screenWidth - 185, 190);
        gc.fillText("Score: " + score, screenWidth - 185, 230);
        gc.fillText("Lives: " + lives, screenWidth - 185, 270);

        if (gameOver) {
            gc.setFill(Color.rgb(0, 0, 0, 0.7));
            gc.fillRect(0, 0, screenWidth, screenHeight);
            gc.setFill(Color.RED); gc.setFont(new Font("Arial", 80)); gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("GAME OVER", screenWidth/2.0, screenHeight/2.0);
            gc.setFill(Color.WHITE); gc.setFont(new Font("Arial", 24));
            gc.fillText("Press R to Restart", screenWidth/2.0, screenHeight/2.0 + 50);
            gc.setTextAlign(TextAlignment.LEFT);
        }
    }

    // Update phụ (Projectile, Particle)
    private void updateProjectiles(double dt) {
        Iterator<Projectile> it = projectiles.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            p.update(dt);
            if (p.getY() < 0 || p.isDestroyed()) { it.remove(); continue; }

            // Boss 2 Damage
            if (currentBoss instanceof BossLevel2) {
                if (checkCollisionRectRect(p, currentBoss)) {
                    p.setDestroyed(true);
                    int dmg = p.isPiercing() ? 5 : 1;
                    if (!((BossLevel2)currentBoss).takeDamage(dmg)) {
                        handleBossDefeated((BossLevel2)currentBoss);
                    }
                }
            }

            // Brick Damage
            for (Brick b : bricks) {
                if (b.isDestroyed()) continue;
                if (p.isPiercing() && p.hasHitBrick(b)) continue;

                if (checkCollisionRectRect(p, b)) {
                    if (p.isPiercing()) {
                        b.takeHit(); p.addHitBrick(b);
                    } else {
                        b.takeHit(); p.setDestroyed(true);
                    }
                    addScore(50);
                    SoundManager.playSound(SoundManager.Sound.HIT_BRICK);
                    if (!p.isPiercing()) break;
                }
            }
        }
    }

    private void updateParticles(double dt) {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.update(dt);
            if (p.isExpired()) it.remove();
        }

        trailSpawnTimer += dt;
        if (trailSpawnTimer > 0.03) {
            for (Ball b : balls) {
                particles.add(new Particle(b.getX()+b.getWidth()/2, b.getY()+b.getHeight()/2, b.getWidth(), b.getHeight(),
                        b.getState() == Ball.BallState.ICE ? Color.CYAN : (b.getState() == Ball.BallState.FIRE ? Color.ORANGE : Color.MAGENTA), 0.5));
            }
            trailSpawnTimer = 0;
        }
    }

    // --- Getters/Setters ---
    public void addScore(int points) { this.score += points; }
    public MovableObject getCurrentBoss() { return currentBoss; }
    public void setCurrentBoss(MovableObject boss) { this.currentBoss = boss; }
    public void setLevelWon(boolean won) { this.levelWon = won; }
    public boolean hasWonLevel() { return levelWon; }
    public boolean hasWonGame() { return gameWon; }
    public int getCurrentLevelIndex() { return currentLevelIndex; }
    public boolean isRunning() { return running; }
    public boolean isGameOver() { return gameOver; }
    public void setRunning(boolean running) { this.running = running; }
    public Paddle getPaddle() { return paddle; }
    public void setMouseControl(boolean b) { this.mouseControlled = b; }
    public void processMouseMovement(double x) { if (paddle != null) paddle.moveTo(x); }
    public void startGame() { if (!running && !gameOver) { running = true; for(Ball b : balls) b.launch(); } }
    public void pauseGame() { running = false; }
    public void resumeGame() { if (!gameOver) running = true; }

    public void processInput(Set<KeyCode> keys) {
        if (gameOver && (keys.contains(KeyCode.SPACE) || keys.contains(KeyCode.R))) initGame();
        if (keys.contains(KeyCode.SPACE)) startGame();
        if (mouseControlled) { paddle.stop(); return; }
        if (keys.contains(KeyCode.LEFT)) paddle.moveLeft();
        else if (keys.contains(KeyCode.RIGHT)) paddle.moveRight();
        else paddle.stop();
    }

    private void saveCurrentScore() {
        if (playerName != null && score > 0) highScores.addScore(playerName, score);
    }
}