package com.example.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.geometry.Point2D;
import java.util.Objects;
import java.util.Random;

public class BossLevel3 extends MovableObject {

    public enum BossState {
        IDLE_PHASE1, SPAWNING_PHASE1, IDLE_PHASE2, SPAWNING_PHASE2
    }

    private BossState currentState = BossState.IDLE_PHASE1;

    // Hình ảnh
    private Image idlePhase1SpriteSheet, spawningPhase1SpriteSheet;
    private Image idlePhase2SpriteSheet, spawningPhase2SpriteSheet;
    private Image leftArmImage, rightArmImage, laserSpriteSheet;

    // Thông số tay
    private final double DEST_ARM_WIDTH = 200;
    private final double DEST_ARM_HEIGHT = 250;
    private final double LEFT_ARM_OFFSET_X = -200, LEFT_ARM_OFFSET_Y = -25;
    private final double RIGHT_ARM_OFFSET_X = 0, RIGHT_ARM_OFFSET_Y = -25;
    private final double LEFT_HAND_PIVOT_OFFSET_X = DEST_ARM_WIDTH / 2 + 50;
    private final double LEFT_HAND_PIVOT_OFFSET_Y = DEST_ARM_HEIGHT - 50;
    private final double RIGHT_HAND_PIVOT_OFFSET_X = DEST_ARM_WIDTH / 2 - 50;
    private final double RIGHT_HAND_PIVOT_OFFSET_Y = DEST_ARM_HEIGHT - 50;

    // Animation
    private final int numFrames = 5;
    private int currentFrame = 0;
    private double frameTime = 0;
    private final double frameDuration = 0.1;

    // Game Logic
    private int hp, maxHp;
    private double attackTimer = 0.0;
    private final double ATTACK_CYCLE_PHASE1 = 10.0;
    private final double ATTACK_CYCLE_PHASE2 = 5.0;
    private boolean itemDropped = false;
    private final int ITEM_DROP_FRAME = 2;

    private HeartBrick heartBrick;
    private boolean heartDestroyed = false;
    private final double fixedX, fixedY;
    private Random random = new Random();

    // Hover
    private double hoverTimer = 0.0;
    private final double HOVER_AMPLITUDE_X = 150.0;
    private final double HOVER_SPEED_X = 0.8;

    // Laser
    private boolean isFiringLaser = false;
    private final double LASER_INTERVAL = 7.0;
    private double laserIntervalTimer = LASER_INTERVAL;
    private final int LASER_NUM_FRAMES = 12;
    private int laserCurrentFrame = 0;
    private double laserFrameTime = 0.0;
    private final double LASER_FRAME_DURATION = 0.5;
    private double laserAngle = 0.0;
    private final double LASER_START_ANGLE = 60.0;
    private final double LASER_END_ANGLE = 120.0;
    private final double LASER_SWEEP_DURATION = LASER_NUM_FRAMES * LASER_FRAME_DURATION;
    private double laserSweepTimer = 0.0;
    private boolean laserFromLeftHand = false;
    private double laserPivotX = 0.0, laserPivotY = 0.0;
    private final double LASER_LENGTH = 1000.0;
    private final double LASER_THICKNESS = 50.0;
    private final double LASER_PIVOT_IN_IMAGE_X = 0.0;

    public BossLevel3(double x, double y, double width, double height, int hp, double playAreaX, double playAreaWidth) {
        super(x, y, width, height);
        this.fixedX = x;
        this.fixedY = y;
        this.hp = hp;
        this.maxHp = hp;

        // Reset các timer quan trọng để tránh lỗi "có sẵn stun"
        this.attackTimer = 0.0;
        this.itemDropped = false;
        this.currentState = BossState.IDLE_PHASE1;

        try {
            idlePhase1SpriteSheet = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/arkanoid/images/Normal.png")));
            spawningPhase1SpriteSheet = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/arkanoid/images/Attack.png")));
            idlePhase2SpriteSheet = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/arkanoid/images/Crazy.png")));
            spawningPhase2SpriteSheet = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/arkanoid/images/Attack.png")));
            leftArmImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/arkanoid/images/left.png")));
            rightArmImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/arkanoid/images/right.png")));
            laserSpriteSheet = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/laser.png"));
        } catch (Exception e) {
            System.err.println("BossLevel3: Load ảnh lỗi/thiếu.");
        }

        double heartRelativeX = (width / 2) - (40.0 / 2);
        double heartRelativeY = height - 30.0 + 200;
        this.heartBrick = new HeartBrick(x + heartRelativeX, y + heartRelativeY, 100, 100, 15);
    }

    public int getHp() { return hp; }
    public boolean takeDamage(int dmg) {
        if (!heartDestroyed) return true;
        hp -= dmg;
        return hp > 0;
    }
    public void setHeartDestroyed(boolean destroyed) {
        this.heartDestroyed = destroyed;
        if (destroyed) {
            if (currentState == BossState.IDLE_PHASE1) currentState = BossState.IDLE_PHASE2;
            else if (currentState == BossState.SPAWNING_PHASE1) currentState = BossState.SPAWNING_PHASE2;
        }
    }
    public HeartBrick getHeartBrick() { return heartBrick; }
    public boolean isHeartDestroyed() { return heartDestroyed; }
    public boolean isFiringLaser() { return isFiringLaser; }

    @Override
    public void update(double dt) {
        // 1. Logic tấn công thả item
        attackTimer += dt;
        boolean isPhase1 = (hp > maxHp / 2);
        double currentAttackCycle = isPhase1 ? ATTACK_CYCLE_PHASE1 : ATTACK_CYCLE_PHASE2;

        // Chuyển sang trạng thái Spawning
        if (attackTimer >= currentAttackCycle && (currentState == BossState.IDLE_PHASE1 || currentState == BossState.IDLE_PHASE2)) {
            attackTimer = 0.0;
            currentFrame = 0;
            currentState = isPhase1 ? BossState.SPAWNING_PHASE1 : BossState.SPAWNING_PHASE2;
            itemDropped = false;
            frameTime = -frameDuration / 2;
        }

        if (currentState == BossState.SPAWNING_PHASE1 || currentState == BossState.SPAWNING_PHASE2) {
            frameTime += dt;
            if (frameTime >= frameDuration) {
                frameTime = 0.0;
                currentFrame++;
                // CHỈ THẢ ITEM KHI ĐÚNG FRAME VÀ CHƯA THẢ
                if (currentFrame == ITEM_DROP_FRAME && !itemDropped) {
                    spawnItem();
                    itemDropped = true;
                }
                if (currentFrame >= numFrames) {
                    currentFrame = 0;
                    currentState = isPhase1 ? BossState.IDLE_PHASE1 : BossState.IDLE_PHASE2;
                }
            }
        } else { // IDLE
            frameTime += dt;
            if (frameTime >= frameDuration) {
                frameTime = 0.0;
                currentFrame = (currentFrame + 1) % numFrames;
            }
        }

        // 2. Di chuyển
        hoverTimer += dt;
        this.x = this.fixedX + Math.sin(hoverTimer * HOVER_SPEED_X) * HOVER_AMPLITUDE_X;
        this.y = this.fixedY + Math.cos(hoverTimer * 1.0) * 10.0;

        if (!heartBrick.isDestroyed()) heartBrick.update(dt);

        // 3. Laser Logic
        updateLaserPivot();
        if (isFiringLaser) {
            laserFrameTime += dt;
            if (laserFrameTime >= LASER_FRAME_DURATION) {
                laserFrameTime -= LASER_FRAME_DURATION;
                laserCurrentFrame++;
            }
            laserSweepTimer += dt;
            double sweepT = Math.min(1.0, laserSweepTimer / LASER_SWEEP_DURATION);
            laserAngle = LASER_START_ANGLE + (LASER_END_ANGLE - LASER_START_ANGLE) * sweepT;

            if (laserCurrentFrame >= LASER_NUM_FRAMES || laserSweepTimer >= LASER_SWEEP_DURATION) {
                isFiringLaser = false;
            }
        } else {
            laserIntervalTimer -= dt;
            if (laserIntervalTimer <= 0) startLaserAttack();
        }
    }

    // --- THÊM: Hàm kiểm tra va chạm Laser với Paddle ---
    public boolean checkLaserCollision(GameObject obj) {
        if (!isFiringLaser) return false;

        // Chuyển đổi tọa độ Paddle sang không gian cục bộ của Laser để kiểm tra dễ hơn
        // 1. Xoay ngược điểm Paddle theo tâm Laser
        // 2. Kiểm tra xem điểm đó có nằm trong hình chữ nhật Laser không

        // Kiểm tra 4 góc của Paddle (đơn giản hóa: kiểm tra tâm hoặc bounding box)
        // Ở đây dùng cách kiểm tra ngược đơn giản:

        double angleRad = Math.toRadians(laserAngle);
        double cosA = Math.cos(-angleRad);
        double sinA = Math.sin(-angleRad);

        // Tọa độ Paddle relative với Pivot
        double dx = obj.getX() + obj.getWidth()/2 - laserPivotX;
        double dy = obj.getY() + obj.getHeight()/2 - laserPivotY;

        // Xoay tọa độ
        double localX = dx * cosA - dy * sinA;
        double localY = dx * sinA + dy * cosA;

        // Kiểm tra va chạm trong không gian local của Laser
        // Laser vẽ từ (-PivotX, -Thickness/2) đến (Length-PivotX, Thickness/2)
        double minX = -LASER_PIVOT_IN_IMAGE_X;
        double maxX = minX + LASER_LENGTH;
        double minY = -LASER_THICKNESS / 2.0;
        double maxY = LASER_THICKNESS / 2.0;

        // Mở rộng vùng va chạm một chút cho dễ trúng
        return (localX >= minX && localX <= maxX && localY >= minY && localY <= maxY);
    }

    private void startLaserAttack() {
        isFiringLaser = true;
        laserIntervalTimer = LASER_INTERVAL;
        laserCurrentFrame = 0;
        laserFrameTime = 0.0;
        laserSweepTimer = 0.0;
        laserAngle = LASER_START_ANGLE;
        laserFromLeftHand = random.nextBoolean();
        SoundManager.playSound(SoundManager.Sound.LASER_BEAM);
    }

    private void updateLaserPivot() {
        if (laserFromLeftHand) {
            laserPivotX = (this.x + LEFT_ARM_OFFSET_X) + LEFT_HAND_PIVOT_OFFSET_X;
            laserPivotY = (this.y + LEFT_ARM_OFFSET_Y) + LEFT_HAND_PIVOT_OFFSET_Y;
        } else {
            laserPivotX = ((this.x + this.width) + RIGHT_ARM_OFFSET_X) + RIGHT_HAND_PIVOT_OFFSET_X;
            laserPivotY = (this.y + RIGHT_ARM_OFFSET_Y) + RIGHT_HAND_PIVOT_OFFSET_Y;
        }
    }

    private void spawnItem() {
        if (GameManagerHolder.INSTANCE != null) {
            // Tạo vật phẩm STUN
            PowerUp powerUp = new PowerUp(x + width / 2, y + height, 50, 70, PowerUp.PowerUpType.STUN_PADDLE);
            GameManagerHolder.INSTANCE.addPowerUp(powerUp);
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        Image currentSpriteSheet = null;
        switch (currentState) {
            case IDLE_PHASE1: currentSpriteSheet = idlePhase1SpriteSheet; break;
            case SPAWNING_PHASE1: currentSpriteSheet = spawningPhase1SpriteSheet; break;
            case IDLE_PHASE2: currentSpriteSheet = idlePhase2SpriteSheet; break;
            case SPAWNING_PHASE2: currentSpriteSheet = spawningPhase2SpriteSheet; break;
        }

        // Render tay
        if (leftArmImage != null) drawFrame(gc, leftArmImage, x + LEFT_ARM_OFFSET_X, y + LEFT_ARM_OFFSET_Y, DEST_ARM_WIDTH, DEST_ARM_HEIGHT);
        if (rightArmImage != null) drawFrame(gc, rightArmImage, (x + width) + RIGHT_ARM_OFFSET_X, y + RIGHT_ARM_OFFSET_Y, DEST_ARM_WIDTH, DEST_ARM_HEIGHT);

        // Render thân
        if (currentSpriteSheet != null) drawFrame(gc, currentSpriteSheet, x, y, width, height);
        else { gc.setFill(Color.PURPLE); gc.fillRect(x, y, width, height); }

        // Render Laser
        if (isFiringLaser) {
            gc.save();
            gc.translate(laserPivotX, laserPivotY);
            gc.rotate(laserAngle);
            if (laserSpriteSheet != null) {
                double frameH = laserSpriteSheet.getHeight() / LASER_NUM_FRAMES;
                gc.drawImage(laserSpriteSheet, 0, laserCurrentFrame * frameH, laserSpriteSheet.getWidth(), frameH,
                        -LASER_PIVOT_IN_IMAGE_X, -LASER_THICKNESS / 2.0, LASER_LENGTH, LASER_THICKNESS);
            } else {
                gc.setFill(Color.RED); gc.fillRect(0, -LASER_THICKNESS/2, LASER_LENGTH, LASER_THICKNESS);
            }
            gc.restore();
        }

        if (!heartBrick.isDestroyed()) heartBrick.render(gc);

        // HP bar
        if (heartDestroyed && hp > 0) {
            double r = (double) hp / maxHp;
            gc.setFill(Color.BLACK); gc.fillRect(x, y-40, width, 10);
            gc.setFill(r > 0.5 ? Color.LIME : Color.RED); gc.fillRect(x, y-40, width*r, 10);
        }
    }

    private void drawFrame(GraphicsContext gc, Image img, double x, double y, double w, double h) {
        double fw = img.getWidth() / numFrames;
        gc.drawImage(img, currentFrame * fw, 0, fw, img.getHeight(), x, y, w, h);
    }

    public static class GameManagerHolder { public static GameManager INSTANCE; }
}