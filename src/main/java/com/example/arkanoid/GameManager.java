package com.example.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.*;

public class GameManager {

    // --- CẤU HÌNH ---
    private final int playAreaWidth = 800;
    private double playAreaOffsetX;
    private int screenWidth, screenHeight;

    // --- GAME OBJECTS ---
    private Paddle paddle;
    private List<Ball> balls = new ArrayList<>();
    private List<Brick> bricks = new ArrayList<>();
    private List<MovingBrickRow> movingBrickRows = new ArrayList<>();
    private List<RotatingBrickGroup> rotatingBrickGroups = new ArrayList<>();
    private List<PowerUp> powerUps = new ArrayList<>();
    private List<Particle> particles = new ArrayList<>();
    private List<Projectile> projectiles = new ArrayList<>();
    private MovableObject currentBoss;

    // --- LOGIC GAME ---
    private int score = 0;
    private int lives = 3;
    private boolean running = false;
    private boolean gameOver = false;
    private boolean levelWon = false;
    private boolean gameWon = false;
    private int currentLevelIndex = 0;
    private String playerName;

    // --- LOGIC CROSSBOW ---
    private boolean crossBowActive = false;
    private double crossBowTimer = 0.0;
    private double arrowSpawnTimer = 0.0;
    private final double CROSS_BOW_DURATION = 10.0;
    private final double ARROW_SPAWN_INTERVAL = 0.5;

    // --- RESOURCES ---
    private List<Image> backgroundImages = new ArrayList<>();
    private List<Image> borderImage = new ArrayList<>();
    private List<Image> leftSideImage = new ArrayList<>();
    private List<Image> rightSideImage = new ArrayList<>();
    private List<Image> scoreBoard = new ArrayList<>();
    private Image currentBackgroundImage, currentBorderImage, currentLeftSideImage, currentRightSideImage, currentScoreBoardImage;

    // --- THÊM: ẢNH GAME OVER ---
    private Image gameOverBgImage;
    private Image gameOverTitleImage;
    private Image gameOverTextImage;

    private HighScores highScores;
    private boolean mouseControlled = false;
    private Map<Integer, BrickFactory> brickFactories;
    private LevelBuilder levelBuilder;
    private List<String> levelFiles = List.of("level1.txt", "level2.txt", "level3.txt");
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
            backgroundImages.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/background_1.png")));
            backgroundImages.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/background_2.png")));
            backgroundImages.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/background_3.png")));
            borderImage.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/play_Border1.png")));
            borderImage.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/play_Border2.png")));
            borderImage.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/play_Border31.png")));
            leftSideImage.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/BorderSide1.jpg")));
            leftSideImage.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/BorderSide2.jpg")));
            leftSideImage.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/BorderSide3.jpg")));
            rightSideImage.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/BorderSide1.jpg")));
            rightSideImage.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/BorderSide2.jpg")));
            rightSideImage.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/BorderSide3.jpg")));
            scoreBoard.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/Score_Board1.png")));
            scoreBoard.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/Score_Board2.png")));
            scoreBoard.add(new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/Score_Board3.png")));

            // --- THÊM: TẢI ẢNH GAME OVER ---
            try {
                gameOverBgImage = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/game_over_screen.png"));
                gameOverTitleImage = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/game_over.png"));
                gameOverTextImage = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/game_over_text.png"));
            } catch (Exception e) {
                System.err.println("Lỗi tải ảnh Game Over (Kiểm tra lại tên file!)");
            }

        } catch (Exception e) { System.err.println("Lỗi tải ảnh GameManager"); }
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
        score = 0; lives = 3; currentLevelIndex = 0; gameWon = false;
        loadLevelData();
        SoundManager.playMusic(SoundManager.Music.LEVEL1);
    }

    private void loadLevelData() {
        int bgIndex = Math.min(currentLevelIndex, backgroundImages.size() - 1);
        currentBackgroundImage = backgroundImages.get(bgIndex);
        currentBorderImage = borderImage.get(Math.min(currentLevelIndex, borderImage.size()-1));
        currentLeftSideImage = leftSideImage.get(Math.min(currentLevelIndex, leftSideImage.size()-1));
        currentRightSideImage = rightSideImage.get(Math.min(currentLevelIndex, rightSideImage.size()-1));
        currentScoreBoardImage = scoreBoard.get(Math.min(currentLevelIndex, scoreBoard.size()-1));

        if (currentLevelIndex < levelFiles.size()) {
            levelBuilder.buildLevelBricks(levelFiles.get(currentLevelIndex), bricks, movingBrickRows, rotatingBrickGroups);
        }

        paddle = new Paddle(playAreaOffsetX + playAreaWidth / 2.0 - 60, screenHeight - 40, 120, 20, screenWidth);
        paddle.setPlayArea(playAreaOffsetX, playAreaWidth);

        balls.clear(); powerUps.clear(); particles.clear(); projectiles.clear();
        currentBoss = null;

        crossBowActive = false;
        crossBowTimer = 0;

        resetBall();
        setupBossForLevel();
        running = false; gameOver = false; levelWon = false; mouseControlled = false;
    }

    private void setupBossForLevel() {
        BossLevel2.GameManagerHolder.INSTANCE = this;
        BossLevel3.GameManagerHolder.INSTANCE = this;

        if (currentLevelIndex == 0) {
            currentBoss = new Boss(playAreaOffsetX, 20, 80, 60);
            currentBoss.setPlayArea(playAreaOffsetX, playAreaWidth);
        } else if (currentLevelIndex == 1) {
            currentBoss = new BossLevel2(playAreaOffsetX, 20, 100, 70, 50, playAreaOffsetX, playAreaWidth);
        } else if (currentLevelIndex == 2) {
            double boss3Width = 280;
            double boss3Height = 250;
            double boss3X = playAreaOffsetX + playAreaWidth / 2 - boss3Width / 2;
            currentBoss = new BossLevel3(boss3X, -20, boss3Width, boss3Height, 1000, playAreaOffsetX, playAreaWidth);
            if (currentBoss instanceof BossLevel3) bricks.add(((BossLevel3)currentBoss).getHeartBrick());
        }
    }

    private void resetBall() {
        int r = 6;
        Ball newBall = new Ball(paddle.getX() + paddle.getWidth()/2 - r, paddle.getY() - r*2, r, screenWidth, screenHeight);
        newBall.setPlayArea(playAreaOffsetX, playAreaWidth);
        newBall.stickTo(paddle);
        balls.add(newBall);
    }

    public void update(double dt) {
        if (currentBoss != null) currentBoss.update(dt);

        if (currentBoss instanceof BossLevel3) {
            BossLevel3 b3 = (BossLevel3) currentBoss;
            if (b3.checkLaserCollision(paddle)) paddle.applySlow(0.5);
        }

        if (gameOver || !running) {
            paddle.update(dt);
            for (Ball b : balls) b.stickTo(paddle);
            return;
        }

        paddle.update(dt);
        for (Ball b : balls) b.update(dt);
        for (MovingBrickRow row : movingBrickRows) row.update(dt);
        for (RotatingBrickGroup group : rotatingBrickGroups) group.update(dt);

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
                System.out.println("Hết giờ Nỏ thần!");
            }
        }

        updatePowerUps(dt);
        updateParticles(dt);
        updateProjectiles(dt);
        handleBallOutOfBounds();
        handleCollisions(dt);
        checkWinCondition();
    }

    private void spawnArrow() {
        double arrowX = paddle.getX() + paddle.getWidth() / 2;
        double arrowY = paddle.getY();
        projectiles.add(new Projectile(arrowX, arrowY, 15, 40, false));
    }

    private void handleCollisions(double dt) {
        for (Ball b : balls) {
            if (checkCollisionCircleRect(b, paddle) && b.getY() + b.getHeight() <= paddle.getY() + b.getDy() * dt + 5) {
                b.handleCollision(paddle, this);
            }
        }

        if (currentBoss instanceof BossLevel2) {
            BossLevel2 bossL2 = (BossLevel2) currentBoss;
            for (Ball b : balls) {
                if (bossL2.checkCollisionWithBall(b)) {
                    b.handleCollision(bossL2, this);
                    int damage = (b.getState() == Ball.BallState.FIRE) ? 2 : 1;
                    if (!bossL2.takeDamage(damage)) handleBossDefeated(bossL2);
                }
            }
        }

        Iterator<Brick> brickIterator = bricks.iterator();
        while (brickIterator.hasNext()) {
            Brick b = brickIterator.next();
            if (b.isDestroyed()) continue;
            for (Ball ball : balls) {
                if (checkCollisionCircleRect(ball, b)) {
                    ball.handleCollision(b, this);
                    break;
                }
            }
        }
    }

    public void handleBossDefeated(BossLevel2 boss) {
        if (currentBoss == boss) {
            currentBoss = null;
            addScore(5000);
        }
    }

    private void checkWinCondition() {
        if (levelWon) return;
        boolean allBricksDestroyed = true;
        for (Brick b : bricks) {
            if (!b.isDestroyed() && !b.isIndestructible()) { allBricksDestroyed = false; break; }
        }

        boolean conditionMet = false;
        if (currentLevelIndex == 1) {
            if (allBricksDestroyed && currentBoss == null) conditionMet = true;
        } else if (currentLevelIndex == 2) {
            boolean bossDead = (currentBoss == null) || ((BossLevel3)currentBoss).getHp() <= 0;
            if (allBricksDestroyed && bossDead) conditionMet = true;
        } else {
            if (allBricksDestroyed) conditionMet = true;
        }

        if (conditionMet) {
            levelWon = true; running = false;
            saveCurrentScore();
            SoundManager.playSound(SoundManager.Sound.LEVEL_COMPLETED);
            paddle.setCrossBowActive(false);
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
        if (balls.isEmpty()) handleLoseLife();
    }

    private void handleLoseLife() {
        lives--;
        if (lives <= 0) {
            gameOver = true; running = false;
            saveCurrentScore();
            SoundManager.playSound(SoundManager.Sound.GAME_OVER);
        } else {
            resetCurrentLevel();
        }
    }

    private void updatePowerUps(double dt) {
        for (int i = powerUps.size() - 1; i >= 0; i--) {
            if (i >= powerUps.size()) break;
            PowerUp pu = powerUps.get(i);
            if (pu.isCollected()) { powerUps.remove(i); continue; }
            pu.update(dt);

            if (checkCollisionRectRect(pu, paddle)) {
                applyPowerUpEffect(pu);
                pu.setCollected(true);
                if (i < powerUps.size()) powerUps.remove(i);
            } else if (pu.getY() > screenHeight) {
                pu.setCollected(true);
                if (i < powerUps.size()) powerUps.remove(i);
            }
        }
    }

    private void applyPowerUpEffect(PowerUp pu) {
        switch (pu.getType()) {
            case LIFE:
                lives++; SoundManager.playSound(SoundManager.Sound.COLLECT_POWERUP);
                break;
            case LOSE_LIFE:
                System.out.println("Trúng đạn lửa! Mất 1 mạng.");
                handleLoseLife();
                break;
            case CROSS_BOW:
                if (!crossBowActive) SoundManager.playSound(SoundManager.Sound.COLLECT_POWERUP);
                crossBowActive = true;
                crossBowTimer = CROSS_BOW_DURATION;
                arrowSpawnTimer = 0.0;
                paddle.setCrossBowActive(true);
                break;
            case MULTI_BALL:
                spawnMultiBall(); SoundManager.playSound(SoundManager.Sound.COLLECT_POWERUP);
                break;
            case PIERCING_SHOT:
                spawnPiercingShot(); SoundManager.playSound(SoundManager.Sound.COLLECT_POWERUP);
                break;
            case MEDICINE:
                if (random.nextBoolean()) applyBallState(Ball.BallState.ICE);
                else applyBallState(Ball.BallState.FIRE);
                SoundManager.playSound(SoundManager.Sound.COLLECT_POWERUP);
                break;
            case STUN_PADDLE:
                System.out.println("Trúng đạn Boss! Mất 1 mạng.");
                handleLoseLife();
                break;
        }
    }

    private void spawnMultiBall() {
        if (!balls.isEmpty()) {
            Ball s = balls.get(0);
            for (int i = 0; i < 2; i++) {
                Ball nb = new Ball(s.getX(), s.getY(), s.getWidth()/2, screenWidth, screenHeight);
                nb.setPlayArea(playAreaOffsetX, playAreaWidth);
                nb.launchAtAngle(random.nextDouble() * 180);
                balls.add(nb);
            }
        }
    }

    private void spawnPiercingShot() {
        projectiles.add(new Projectile(paddle.getX()+paddle.getWidth()/2, paddle.getY(), 10, 50, true));
    }

    private void applyBallState(Ball.BallState state) {
        for (Ball b : balls) b.setBallState(state, 10.0);
    }

    public void resetCurrentLevel() {
        running = false; balls.clear(); powerUps.clear(); projectiles.clear();
        paddle.setCrossBowActive(false); crossBowActive = false;
        resetBall();
    }

    public void nextLevel() {
        currentLevelIndex++;
        if (currentLevelIndex >= levelFiles.size()) gameWon = true;
        else loadLevelData();
    }

    private boolean checkCollisionCircleRect(Ball ball, GameObject rect) {
        double cx = ball.getX() + ball.getWidth()/2;
        double cy = ball.getY() + ball.getHeight()/2;
        double r = ball.getWidth()/2;
        double clX = Math.max(rect.getX(), Math.min(cx, rect.getX()+rect.getWidth()));
        double clY = Math.max(rect.getY(), Math.min(cy, rect.getY()+rect.getHeight()));
        double dx = cx - clX; double dy = cy - clY;
        return (dx*dx + dy*dy) <= (r*r);
    }

    private boolean checkCollisionRectRect(GameObject r1, GameObject r2) {
        return r1.getX() < r2.getX() + r2.getWidth() && r1.getX() + r1.getWidth() > r2.getX() &&
                r1.getY() < r2.getY() + r2.getHeight() && r1.getY() + r1.getHeight() > r2.getY();
    }

    public void addPowerUp(PowerUp pu) { powerUps.add(pu); }
    public void spawnPowerUpFromBrick(Brick b) {
        PowerUp pu = PowerUpFactory.createRandomPowerUp(b.getX(), b.getY(), 50, 50);
        if (pu != null) powerUps.add(pu);
    }
    public void spawnMedicine(double x, double y) {
        powerUps.add(new PowerUp(x, y, 30, 20, PowerUp.PowerUpType.MEDICINE));
    }

    public void render(GraphicsContext gc) {
        // 1. Background Game (Vẽ nền game đang chơi)
        if (currentBackgroundImage != null) {
            gc.drawImage(currentBackgroundImage, 0, 0, screenWidth, screenHeight);
        } else {
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, screenWidth, screenHeight);
        }

        // 2. Vẽ Boss (nếu có)
        if (currentBoss != null) currentBoss.render(gc);

        // 3. Vẽ các đối tượng game
        for (Brick b : bricks) b.render(gc);
        for (PowerUp p : powerUps) p.render(gc);
        for (Projectile p : projectiles) p.render(gc);
        for (Particle p : particles) p.render(gc);
        paddle.render(gc);
        for (Ball b : balls) b.render(gc);

        // 4. Vẽ Viền & UI (Bảng điểm, khung...)
        if (currentLeftSideImage != null) gc.drawImage(currentLeftSideImage, playAreaOffsetX - currentLeftSideImage.getWidth() - 10, 0, currentLeftSideImage.getWidth(), screenHeight);
        if (currentRightSideImage != null) gc.drawImage(currentRightSideImage, playAreaOffsetX + playAreaWidth + 10, 0, currentRightSideImage.getWidth(), screenHeight);
        if (currentBorderImage != null) gc.drawImage(currentBorderImage, 0, 0, screenWidth, screenHeight);
        if (currentScoreBoardImage != null) gc.drawImage(currentScoreBoardImage, 870, 60, 500, 840);

        // Thông tin điểm số
        gc.setFill(Color.WHITE);
        gc.setFont(new Font("Arial Bold", 16));
        gc.fillText("Player: " + playerName, screenWidth - 185, 190);
        gc.fillText("Score: " + score, screenWidth - 185, 230);
        gc.fillText("Lives: " + lives, screenWidth - 185, 270);

        if (crossBowActive) {
            gc.setFill(Color.YELLOW);
            gc.fillText(String.format("Nỏ: %.1fs", crossBowTimer), screenWidth - 185, 310);
        }

        // ============================================================
        // 5. MÀN HÌNH GAME OVER (ĐÃ CĂN CHỈNH THEO ẢNH MẪU)
        // ============================================================
        if (gameOver) {
            // --- LỚP 1: NỀN (Hình người trùm mũ bên phải) ---
            if (gameOverBgImage != null && !gameOverBgImage.isError()) {
                // Vẽ full màn hình để lấp hết background game cũ
                gc.drawImage(gameOverBgImage, 0, 0, screenWidth, screenHeight);
            } else {
                // Dự phòng màu đen nếu chưa load được ảnh
                gc.setFill(Color.rgb(0, 0, 0, 1.0));
                gc.fillRect(0, 0, screenWidth, screenHeight);
            }

            // --- LỚP 2: TEXT TIẾNG VIỆT (Ở TRÊN CÙNG) ---
            // "MỌI THỨ DỪNG LẠI..."
            if (gameOverTextImage != null && !gameOverTextImage.isError()) {
                double imgW = gameOverTextImage.getWidth();
                double imgH = gameOverTextImage.getHeight();

                // Tăng kích thước lên cho rõ (Rộng khoảng 900px)
                double targetWidth = 900;
                double targetHeight = imgH * (targetWidth / imgW); // Giữ tỉ lệ ảnh

                // Vị trí: Căn giữa chiều ngang, cách đỉnh màn hình 150px
                double x = (screenWidth - targetWidth) / 2;
                double y = 150;

                gc.drawImage(gameOverTextImage, x, y, targetWidth, targetHeight);
            }

            // --- LỚP 3: TIÊU ĐỀ "GAME OVER" (Ở GIỮA) ---
            if (gameOverTitleImage != null && !gameOverTitleImage.isError()) {
                double imgW = gameOverTitleImage.getWidth();
                double imgH = gameOverTitleImage.getHeight();

                // Kích thước chữ GAME OVER (Rộng khoảng 600px)
                double targetWidth = 600;
                double targetHeight = imgH * (targetWidth / imgW);

                // Vị trí: Căn giữa chiều ngang, Căn giữa chiều dọc (hơi lệch lên chút)
                double x = (screenWidth - targetWidth) / 2;
                double y = (screenHeight - targetHeight) / 2 + 50; // +50 để không đè lên text trên

                gc.drawImage(gameOverTitleImage, x, y, targetWidth, targetHeight);
            } else {
                // Fallback text nếu ảnh lỗi
                gc.setFill(Color.RED);
                gc.setFont(new Font("Arial Black", 80));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText("GAME OVER", screenWidth / 2.0, screenHeight / 2.0 + 50);
            }

            // --- LỚP 4: HƯỚNG DẪN (Ở DƯỚI CÙNG) ---
            // "NHẤN R HOẶC SPACE ĐỂ BẮT ĐẦU LẠI"
            gc.setFill(Color.WHITE); // Màu trắng (hoặc dùng Color.web("#ffe0b2") cho hơi vàng)
            // Sử dụng Font to, đậm
            gc.setFont(Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 30));
            gc.setTextAlign(TextAlignment.CENTER);

            // Vị trí: Gần đáy màn hình (cách đáy 150px)
            gc.fillText("NHẤN R HOẶC SPACE ĐỂ BẮT ĐẦU LẠI", screenWidth / 2.0 - 50, screenHeight - 150);

            // Reset căn lề để không ảnh hưởng frame sau
            gc.setTextAlign(TextAlignment.LEFT);
        }
    }

    private void updateProjectiles(double dt) {
        Iterator<Projectile> it = projectiles.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            p.update(dt);
            if (p.getY() < 0 || p.isDestroyed()) { it.remove(); continue; }

            if (currentBoss instanceof BossLevel2) {
                if (checkCollisionRectRect(p, currentBoss)) {
                    p.setDestroyed(true);
                    int dmg = p.isPiercing() ? 5 : 1;
                    if (!((BossLevel2)currentBoss).takeDamage(dmg)) handleBossDefeated((BossLevel2)currentBoss);
                }
            }

            for (Brick b : bricks) {
                if (b.isDestroyed()) continue;
                if (p.isPiercing() && p.hasHitBrick(b)) continue;
                if (checkCollisionRectRect(p, b)) {
                    if (p.isPiercing()) { b.takeHit(); p.addHitBrick(b); }
                    else { b.takeHit(); p.setDestroyed(true); }
                    addScore(50); SoundManager.playSound(SoundManager.Sound.HIT_BRICK);
                    if (!p.isPiercing()) break;
                }
            }
        }
    }

    private void updateParticles(double dt) {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next(); p.update(dt);
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
    public void resumeGame() {
        if (!gameOver) {
            boolean anyMoving = false;
            for(Ball b : balls) if (!b.isStuck()) { anyMoving = true; break; }
            running = anyMoving;
        }
    }

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