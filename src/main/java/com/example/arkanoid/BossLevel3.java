// Trong thư mục com/example/arkanoid
// Tạo file BossLevel3.java

package com.example.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import java.util.Objects;
import java.util.Random;


public class BossLevel3 extends MovableObject {

    // --- ENUM cho các Trạng thái của Boss ---
    public enum BossState {
        IDLE_PHASE1,     // Trạng thái nằm yên ban đầu (trước khi tim vỡ)
        SPAWNING_PHASE1, // Trạng thái nhả vật thể (trước khi tim vỡ)
        IDLE_PHASE2,     // Trạng thái nằm yên mới (sau khi tim vỡ, có thể nhận sát thương)
        SPAWNING_PHASE2  // Trạng thái nhả vật thể mới (sau khi tim vỡ, có thể nhận sát thương)
    }

    private BossState currentState = BossState.IDLE_PHASE1; // Trạng thái ban đầu

    // --- Hình ảnh cho từng trạng thái và phase ---
    private Image idlePhase1SpriteSheet;
    private Image spawningPhase1SpriteSheet;
    private Image idlePhase2SpriteSheet;
    private Image spawningPhase2SpriteSheet;
    private Image leftArmImage;
    private Image rightArmImage;

    // --- TÙY CHỈNH VỊ TRÍ VÀ KÍCH THƯỚC CÁNH TAY TẠI ĐÂY ---
    // (Dựa theo thông số bạn cung cấp lần trước)
    private final double DEST_ARM_WIDTH = 200;
    private final double DEST_ARM_HEIGHT = 250;
    private final double LEFT_ARM_OFFSET_X = -200;
    private final double LEFT_ARM_OFFSET_Y = -25;
    private final double RIGHT_ARM_OFFSET_X = 0;
    private final double RIGHT_ARM_OFFSET_Y = -25;

    // --- TÙY CHỈNH ĐIỂM "CUỐI CÁNH TAY" (ĐỂ BẮN LASER) ---
    private final double LEFT_HAND_PIVOT_OFFSET_X = DEST_ARM_WIDTH / 2 + 50; // (Giữ tùy chỉnh của bạn)
    private final double LEFT_HAND_PIVOT_OFFSET_Y = DEST_ARM_HEIGHT - 50;   // (Giữ tùy chỉnh của bạn)

    // (Tính từ góc trên-trái của ẢNH CÁNH TAY PHẢI)
    private final double RIGHT_HAND_PIVOT_OFFSET_X = DEST_ARM_WIDTH / 2 - 50; // (Ví dụ: Quay về mặc định)
    private final double RIGHT_HAND_PIVOT_OFFSET_Y = DEST_ARM_HEIGHT - 50;
    // -------------------------------------------------------------

    // Animation 5 frame của Boss/Tay
    private final int numFrames = 5;
    private int currentFrame = 0;
    private double frameTime = 0;
    private final double frameDuration = 0.1;

    private int hp;
    private final int maxHp;

    private double spawnItemTimer = 0;
    private double spawnItemInterval = 5 + new Random().nextDouble() * 3;

    // Bộ đếm thời gian cho chu kỳ tấn công
    private double attackTimer = 0.0;
    private final double ATTACK_CYCLE_PHASE1 = 10.0;
    private final double ATTACK_CYCLE_PHASE2 = 5.0;
    private boolean itemDropped = false;
    private final int ITEM_DROP_FRAME = 2;

    // --- Trái tim là một Brick (điểm yếu) ---
    private HeartBrick heartBrick;
    private boolean heartDestroyed = false;

    // (fixedX, fixedY) là vị trí neo (anchor)
    private final double fixedX, fixedY;

    private Random random = new Random();

    // --- Biến di chuyển "Lơ lửng" (Hover) ---
    // (Dựa theo thông số bạn cung cấp lần trước)
    private double hoverTimer = 0.0;
    private final double HOVER_AMPLITUDE_X = 150.0;
    private final double HOVER_AMPLITUDE_Y = 10.0;
    private final double HOVER_SPEED_X = 0.8;
    private final double HOVER_SPEED_Y = 1.0;

    // =========================================================
    // 🟢 1. BIẾN CHO SKILL LASER (12 FRAME) 🟢
    // =========================================================
    private Image laserSpriteSheet; // (Đây PHẢI là sprite sheet 12 frame)
    private boolean isFiringLaser = false;

    // Timer 7 giây
    private final double LASER_INTERVAL = 7.0;
    private double laserIntervalTimer = LASER_INTERVAL;

    // Animation (12 frame)
    private final int LASER_NUM_FRAMES = 12; // 🟢 12 frame
    private int laserCurrentFrame = 0;
    private double laserFrameTime = 0.0;
    private final double LASER_FRAME_DURATION = 0.5; // Tốc độ anim/xoay

    // Sweep (Xoay)
    private double laserAngle = 0.0;
    private final double LASER_START_ANGLE = 60.0;
    private final double LASER_END_ANGLE = 120.0;
    private final double LASER_SWEEP_DURATION = LASER_NUM_FRAMES * LASER_FRAME_DURATION;
    private double laserSweepTimer = 0.0;

    // Pivot (Điểm xoay)
    private boolean laserFromLeftHand = false;
    private double laserPivotX = 0.0;
    private double laserPivotY = 0.0;

    // (Tùy chỉnh độ dài, độ dày và điểm gốc xoay CỦA ẢNH LASER)
    private final double LASER_LENGTH = 1000.0;
    private final double LASER_THICKNESS = 50.0;
    private final double LASER_PIVOT_IN_IMAGE_X = 0.0;
    // =========================================================


    public BossLevel3(double x, double y, double width, double height, int hp, double playAreaX, double playAreaWidth) {
        super(x, y, width, height);
        this.fixedX = x;
        this.fixedY = y;
        this.hp = hp;
        this.maxHp = hp;

        try {
            // Tải ảnh Boss
            idlePhase1SpriteSheet = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/arkanoid/images/Normal.png")));
            spawningPhase1SpriteSheet = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/arkanoid/images/Attack.png")));
            idlePhase2SpriteSheet = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/arkanoid/images/Crazy.png")));
            spawningPhase2SpriteSheet = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/arkanoid/images/Attack.png")));
            // Tải ảnh Tay
            leftArmImage = new Image(Objects.requireNonNull(
                    getClass().getResourceAsStream("/com/example/arkanoid/images/left.png")
            ));
            rightArmImage = new Image(Objects.requireNonNull(
                    getClass().getResourceAsStream("/com/example/arkanoid/images/right.png")
            ));

            // 🟢 2. TẢI ẢNH LASER 🟢
            // (Hãy chắc chắn file này tên là laser.png (hoặc .jpg nếu bạn sửa tên)
            // (LƯU Ý: Nếu dùng .jpg, bạn phải đổi tên file trong code)
            laserSpriteSheet = new Image(Objects.requireNonNull(
                    getClass().getResourceAsStream("/com/example/arkanoid/images/laser.png")
            ));
            // ---------------------

        } catch (Exception e) {
            System.err.println("Lỗi: Không thể tải ảnh cho BossLevel3 hoặc Laser.");
            e.printStackTrace();
        }

        // Khởi tạo HeartBrick (Vị trí cố định)
        double heartWidth = 40;
        double heartHeight = 30;
        double heartRelativeX = (width / 2) - (heartWidth / 2);
        double heartRelativeY = height - heartHeight + 200;
        double heartRenderWidth = 100;
        double heartRenderHeight = 100;
        int heartHp = 1;

        this.heartBrick = new HeartBrick(
                x + heartRelativeX,
                y + heartRelativeY,
                heartRenderWidth,
                heartRenderHeight,
                heartHp
        );
    }

    // (Các hàm getHp, takeDamage, setHeartDestroyed, getHeartBrick, isHeartDestroyed ... KHÔNG ĐỔI)
    public int getHp() { return hp; }
    public boolean takeDamage(int dmg) {
        if (!heartDestroyed) return true;
        hp -= dmg;
        if (hp <= 0) {
            System.out.println("Boss Level 3 đã bị tiêu diệt!");
            return false;
        }
        return true;
    }
    public void setHeartDestroyed(boolean destroyed) {
        this.heartDestroyed = destroyed;
        if (destroyed) {
            System.out.println("Trái tim của Boss Level 3 đã bị phá hủy!");
            if (currentState == BossState.IDLE_PHASE1) {
                currentState = BossState.IDLE_PHASE2;
            } else if (currentState == BossState.SPAWNING_PHASE1) {
                currentState = BossState.SPAWNING_PHASE2;
            }
        }
    }
    public HeartBrick getHeartBrick() { return heartBrick; }
    public boolean isHeartDestroyed() { return heartDestroyed; }


    // =========================================================
    // 🟢 3. HÀM UPDATE() (ĐÃ CHÍNH XÁC) 🟢
    // =========================================================
    @Override
    public void update(double dt) {

        // --- A. Cập nhật FSM (Tấn công thường / Idle) ---
        attackTimer += dt;
        boolean isPhase1 = (hp > maxHp / 2);
        double currentAttackCycle = isPhase1 ? ATTACK_CYCLE_PHASE1 : ATTACK_CYCLE_PHASE2;

        if (attackTimer >= currentAttackCycle &&
                (currentState == BossState.IDLE_PHASE1 || currentState == BossState.IDLE_PHASE2)) {
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
                if (currentFrame == ITEM_DROP_FRAME && !itemDropped) {
                    GameManagerHolder.INSTANCE.addPowerUp(
                            new PowerUp(x + width / 2, y + height, 50, 70, PowerUp.PowerUpType.STUN_PADDLE)
                    );
                    itemDropped = true;
                }
                if (currentFrame >= numFrames) {
                    currentFrame = 0;
                    currentState = isPhase1 ? BossState.IDLE_PHASE1 : BossState.IDLE_PHASE2;
                }
            }
        }
        else {
            frameTime += dt;
            if (frameTime >= frameDuration) {
                frameTime = 0.0;
                currentFrame = (currentFrame + 1) % numFrames;
            }
        }

        // --- B. Cập nhật Di chuyển "Lơ lửng" (Hover) CỦA BOSS ---
        hoverTimer += dt;
        double hoverOffsetX = Math.sin(hoverTimer * HOVER_SPEED_X) * HOVER_AMPLITUDE_X;
        double hoverOffsetY = Math.cos(hoverTimer * HOVER_SPEED_Y) * HOVER_AMPLITUDE_Y;
        this.x = this.fixedX + hoverOffsetX;
        this.y = this.fixedY + hoverOffsetY;

        // --- C. Cập nhật Trái tim (Đứng yên) ---
        if (!heartBrick.isDestroyed()) {
            heartBrick.update(dt);
        }

        // --- D. CẬP NHẬT LOGIC SKILL LASER (12 FRAME) ---

        // Cập nhật điểm Pivot (cuối cánh tay) MỖI FRAME
        updateLaserPivot();

        // Logic Bắn (Firing)
        if (isFiringLaser) {
            // 1. Cập nhật Animation Sprite Sheet (12 frame)
            laserFrameTime += dt;
            if (laserFrameTime >= LASER_FRAME_DURATION) {
                laserFrameTime -= LASER_FRAME_DURATION;
                laserCurrentFrame++;
            }

            // 2. Cập nhật Góc xoay (Sweep)
            laserSweepTimer += dt;
            double sweepT = Math.min(1.0, laserSweepTimer / LASER_SWEEP_DURATION);
            laserAngle = LASER_START_ANGLE + (LASER_END_ANGLE - LASER_START_ANGLE) * sweepT;

            // 3. Kiểm tra kết thúc (Dựa cả vào Frame và Thời gian)
            if (laserCurrentFrame >= LASER_NUM_FRAMES || laserSweepTimer >= LASER_SWEEP_DURATION) {
                isFiringLaser = false;
            }
        }
        // Logic Chờ (Cooldown)
        else {
            laserIntervalTimer -= dt;
            if (laserIntervalTimer <= 0) {
                startLaserAttack();
            }
        }
    }

    /**
     * HÀM MỚI: Kích hoạt skill laser (Dùng 12 frame)
     */
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

    /**
     * HÀM MỚI: Cập nhật vị trí điểm xoay của laser (cuối cánh tay)
     * (Hàm này không đổi)
     */
    private void updateLaserPivot() {
        if (laserFromLeftHand) {
            double leftArmBaseX = this.x + LEFT_ARM_OFFSET_X;
            double leftArmBaseY = this.y + LEFT_ARM_OFFSET_Y;
            laserPivotX = leftArmBaseX + LEFT_HAND_PIVOT_OFFSET_X;
            laserPivotY = leftArmBaseY + LEFT_HAND_PIVOT_OFFSET_Y;
        } else {
            double rightArmBaseX = (this.x + this.width) + RIGHT_ARM_OFFSET_X;
            double rightArmBaseY = this.y + RIGHT_ARM_OFFSET_Y;
            laserPivotX = rightArmBaseX + RIGHT_HAND_PIVOT_OFFSET_X;
            laserPivotY = rightArmBaseY + RIGHT_HAND_PIVOT_OFFSET_Y;
        }
    }


    // =========================================================
    // 🟢 4. HÀM RENDER() (ĐÃ SỬA ĐỂ ĐỌC SPRITE SHEET DỌC) 🟢
    // =========================================================
    @Override
    public void render(GraphicsContext gc) {

        Image currentSpriteSheet = null;
        switch (currentState) {
            case IDLE_PHASE1: currentSpriteSheet = idlePhase1SpriteSheet; break;
            case SPAWNING_PHASE1: currentSpriteSheet = spawningPhase1SpriteSheet; break;
            case IDLE_PHASE2: currentSpriteSheet = idlePhase2SpriteSheet; break;
            case SPAWNING_PHASE2: currentSpriteSheet = spawningPhase2SpriteSheet; break;
        }

        // 1. VẼ CÁNH TAY TRÁI
        if (leftArmImage != null && !leftArmImage.isError()) {
            double armFrameWidth = leftArmImage.getWidth() / numFrames;
            double armFrameHeight = leftArmImage.getHeight();
            if (armFrameWidth > 0) {
                double sx = currentFrame * armFrameWidth;
                double sy = 0;
                double destX = this.x + LEFT_ARM_OFFSET_X;
                double destY = this.y + LEFT_ARM_OFFSET_Y;
                gc.drawImage(leftArmImage, sx, sy, armFrameWidth, armFrameHeight,
                        destX, destY, DEST_ARM_WIDTH, DEST_ARM_HEIGHT);
            }
        }

        // 2. VẼ CÁNH TAY PHẢI
        if (rightArmImage != null && !rightArmImage.isError()) {
            double armFrameWidth = rightArmImage.getWidth() / numFrames;
            double armFrameHeight = rightArmImage.getHeight();
            if (armFrameWidth > 0) {
                double sx = currentFrame * armFrameWidth;
                double sy = 0;
                double destX = (this.x + this.width) + RIGHT_ARM_OFFSET_X;
                double destY = this.y + RIGHT_ARM_OFFSET_Y;
                gc.drawImage(rightArmImage, sx, sy, armFrameWidth, armFrameHeight,
                        destX, destY, DEST_ARM_WIDTH, DEST_ARM_HEIGHT);
            }
        }

        // 3. VẼ THÂN BOSS
        if (currentSpriteSheet != null) {
            double frameWidth = currentSpriteSheet.getWidth() / numFrames;
            if (frameWidth > 0) {
                gc.drawImage(currentSpriteSheet, frameWidth * currentFrame, 0, frameWidth, currentSpriteSheet.getHeight(),
                        x, y, width, height);
            } else {
                gc.setFill(javafx.scene.paint.Color.PURPLE);
                gc.fillRect(x, y, width, height);
            }
        }

        // =======================================================
        // 🟢 4. VẼ SKILL LASER (SỬA LOGIC ĐỌC FRAME DỌC) 🟢
        // =======================================================
        if (isFiringLaser && laserSpriteSheet != null && !laserSpriteSheet.isError()) {

            // 🟢 1. Lấy frame animation (12 frame) DỌC
            double frameW = laserSpriteSheet.getWidth(); // Lấy TOÀN BỘ chiều rộng
            double frameH = laserSpriteSheet.getHeight() / LASER_NUM_FRAMES; // 🟢 Chiều cao = Tổng / 12
            double sx = 0; // 🟢 Source X luôn là 0
            double sy = laserCurrentFrame * frameH; // 🟢 Source Y = frame * chiều cao frame

            // 2. Tính toán điểm gốc xoay BÊN TRONG ảnh laser
            double pivotInImageX = LASER_PIVOT_IN_IMAGE_X;
            double pivotInImageY = LASER_THICKNESS / 2.0;

            // 3. Thực hiện xoay và vẽ
            gc.save();
            gc.translate(laserPivotX, laserPivotY);
            gc.rotate(laserAngle);

            gc.drawImage(laserSpriteSheet,
                    sx, sy, frameW, frameH,         // 🟢 Source (Đọc frame DỌC)
                    -pivotInImageX, -pivotInImageY, // Destination (Dịch lùi theo tâm mới)
                    LASER_LENGTH, LASER_THICKNESS   // Destination Size (Vẽ theo độ dài/dày)
            );

            gc.restore();
        }
        // =======================================================

        // 5. VẼ TRÁI TIM (HeartBrick)
        if (!heartBrick.isDestroyed()) {
            heartBrick.render(gc);
        }

        // 6. VẼ THANH MÁU (HP Bar)
        if (heartDestroyed && hp > 0) {
            final double BAR_WIDTH = width;
            final double BAR_HEIGHT = 10;
            final double BAR_Y_OFFSET = 15;
            double barX = x;
            double barY = y - BAR_HEIGHT - BAR_Y_OFFSET;

            gc.setFill(javafx.scene.paint.Color.rgb(0, 0, 0, 0.7));
            gc.fillRect(barX, barY, BAR_WIDTH, BAR_HEIGHT);
            double hpRatio = (double) hp / maxHp;
            double currentHpWidth = BAR_WIDTH * hpRatio;
            if (hpRatio > 0.6) gc.setFill(javafx.scene.paint.Color.LIMEGREEN);
            else if (hpRatio > 0.3) gc.setFill(javafx.scene.paint.Color.YELLOW);
            else gc.setFill(javafx.scene.paint.Color.RED);
            gc.fillRect(barX, barY, currentHpWidth, BAR_HEIGHT);
        }
    }

    /**
     * Hàm này sẽ được gọi khi Boss cần nhả vật thể (Tấn công thường).
     */
    private void spawnItem() {
        if (GameManagerHolder.INSTANCE != null) {
            double itemWidth = 30;
            double itemHeight = 20;
            double itemX = x + width / 2 - itemWidth / 2;
            double itemY = y + height - itemHeight;
            PowerUp powerUp = new PowerUp(itemX, itemY, itemWidth, itemHeight, PowerUp.PowerUpType.MEDICINE);
            GameManagerHolder.INSTANCE.addPowerUp(powerUp);
        }
    }

    /** Cách giữ tham chiếu tới GameManager */
    public static class GameManagerHolder {
        public static GameManager INSTANCE;
    }
}