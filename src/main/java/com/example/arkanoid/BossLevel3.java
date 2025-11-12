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

    private final int numFrames = 5; // Tất cả các trạng thái đều có 5 frame
    private int currentFrame = 0;
    private double frameTime = 0;
    private final double frameDuration = 0.1; // Tốc độ animation

    private int hp; // Máu của Boss (chỉ trừ khi tim vỡ)
    private final int maxHp;

    private double spawnItemTimer = 0;
    private double spawnItemInterval = 5 + new Random().nextDouble() * 3; // Nhả vật thể mỗi 5-8 giây

    // Trong BossLevel3.java

    // Bộ đếm thời gian cho chu kỳ tấn công
    private double attackTimer = 0.0;
    // Chu kỳ reset mặc định (10 giây)
    private final double ATTACK_CYCLE_PHASE1 = 10.0;
    // Chu kỳ cho Phase 2 (Crazy mode)
    private final double ATTACK_CYCLE_PHASE2 = 5.0;
    // Cờ báo hiệu đã thả vật phẩm trong chu kỳ này chưa
    private boolean itemDropped = false;

    // Hằng số cho số frame Attack (Nếu 5 frame, dùng 4 vì index bắt đầu từ 0)
    private final int ITEM_DROP_FRAME = 2; // Nếu có 5 frame, frame cuối là index 4

    // --- Trái tim là một Brick (điểm yếu) ---
    private HeartBrick heartBrick;
    private boolean heartDestroyed = false; // Cờ báo tim đã bị vỡ

    // --- Kích thước và vị trí của Boss (cố định) ---
    private final double fixedX, fixedY;

    // --- Random cho việc nhả vật thể ---
    private Random random = new Random();

    public BossLevel3(double x, double y, double width, double height, int hp, double playAreaX, double playAreaWidth) {
        super(x, y, width, height);
        this.fixedX = x;
        this.fixedY = y;
        this.hp = hp;
        this.maxHp = hp;

        // Tải các sprite sheets
        try {
            idlePhase1SpriteSheet = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/arkanoid/images/Normal.png")));
            spawningPhase1SpriteSheet = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/arkanoid/images/Attack.png")));
            idlePhase2SpriteSheet = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/arkanoid/images/Crazy.png")));
            spawningPhase2SpriteSheet = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/arkanoid/images/Attack.png")));
        } catch (Exception e) {
            System.err.println("Lỗi: Không thể tải ảnh cho BossLevel3.");
            e.printStackTrace();
        }

        // Khởi tạo HeartBrick - Vị trí của trái tim (ví dụ: ở giữa Boss, hơi thấp xuống)
        // Kích thước của Brick trái tim
        double heartWidth = 40;
        double heartHeight = 30;
        double heartX = x + width / 2 - heartWidth / 2;
        double heartY = y + height - heartHeight + 200; // Đặt thấp hơn Boss một chút
        int heartHp = 1;
        this.heartBrick = new HeartBrick(heartX, heartY, 100, 100, heartHp);
    }

    public int getHp() { return hp; }

    /**
     * Boss nhận sát thương (chỉ khi trái tim đã vỡ).
     * @param dmg Lượng sát thương nhận vào.
     * @return true nếu Boss vẫn sống, false nếu Boss bị phá hủy.
     */
    public boolean takeDamage(int dmg) {
        if (!heartDestroyed) return true; // Không mất máu nếu tim chưa vỡ

        hp -= dmg;
        if (hp <= 0) {
            System.out.println("Boss Level 3 đã bị tiêu diệt!");
            return false; // Boss bị phá hủy
        }
        return true; // Boss vẫn sống
    }

    /**
     * Dùng để Boss biết khi nào HeartBrick bị phá hủy.
     */
    public void setHeartDestroyed(boolean destroyed) {
        this.heartDestroyed = destroyed;
        if (destroyed) {
            System.out.println("Trái tim của Boss Level 3 đã bị phá hủy!");
            // Chuyển sang trạng thái Phase 2 (có thể nhận sát thương)
            if (currentState == BossState.IDLE_PHASE1) {
                currentState = BossState.IDLE_PHASE2;
            } else if (currentState == BossState.SPAWNING_PHASE1) {
                currentState = BossState.SPAWNING_PHASE2;
            }
        }
    }

    public HeartBrick getHeartBrick() {
        return heartBrick;
    }

    /**
     * Kiểm tra xem điểm yếu (trái tim) đã bị phá hủy chưa.
     */
    public boolean isHeartDestroyed() {
        return heartDestroyed;
    }

    @Override

    public void update(double dt) {
        attackTimer += dt;
        boolean isPhase1 = (hp > maxHp / 2);
        double currentAttackCycle = isPhase1 ? ATTACK_CYCLE_PHASE1 : ATTACK_CYCLE_PHASE2;

        // --- 1️⃣ Bắt đầu Attack ---
        if (attackTimer >= currentAttackCycle &&
                (currentState == BossState.IDLE_PHASE1 || currentState == BossState.IDLE_PHASE2)) {

            attackTimer = 0.0;
            currentFrame = 0;
            currentState = isPhase1 ? BossState.SPAWNING_PHASE1 : BossState.SPAWNING_PHASE2;
            itemDropped = false;
            frameTime = -frameDuration / 2; // 🔥 Delay nửa frame trước khi đếm frame
            return; // 🔥 Ngăn không xử lý logic frame ngay trong frame này
        }

        // --- 2️⃣ Nếu đang tấn công ---
        if (currentState == BossState.SPAWNING_PHASE1 || currentState == BossState.SPAWNING_PHASE2) {
            frameTime += dt;
            if (frameTime >= frameDuration) {
                frameTime = 0.0;
                currentFrame++;

                if (currentFrame == ITEM_DROP_FRAME && !itemDropped) {
                    System.out.println("Boss Level 3 drop item tại frame " + currentFrame);
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

        // --- 3️⃣ Nếu đang IDLE ---
        else {
            frameTime += dt;
            if (frameTime >= frameDuration) {
                frameTime = 0.0;
                currentFrame = (currentFrame + 1) % numFrames;
            }
        }

        // --- 4️⃣ Cập nhật HeartBrick ---
        if (!heartBrick.isDestroyed()) {
            heartBrick.update(dt);
        }
    }
    @Override
    public void render(GraphicsContext gc) {
        Image currentSpriteSheet = null;
        switch (currentState) {
            case IDLE_PHASE1:
                currentSpriteSheet = idlePhase1SpriteSheet;
                break;
            case SPAWNING_PHASE1:
                currentSpriteSheet = spawningPhase1SpriteSheet;
                break;
            case IDLE_PHASE2:
                currentSpriteSheet = idlePhase2SpriteSheet;
                break;
            case SPAWNING_PHASE2:
                currentSpriteSheet = spawningPhase2SpriteSheet;
                break;
        }

        if (currentSpriteSheet != null) {
            double frameWidth = currentSpriteSheet.getWidth() / numFrames;
            if (frameWidth > 0) { // <-- THÊM KIỂM TRA NÀY
                gc.drawImage(currentSpriteSheet, frameWidth * currentFrame, 0, frameWidth, currentSpriteSheet.getHeight(),
                        x, y, width, height);
            } else {
                // Vẽ hình chữ nhật tạm thời để debug
                gc.setFill(javafx.scene.paint.Color.PURPLE);
                gc.fillRect(x, y, width, height);
            }
        }

        // Vẽ HeartBrick (nếu nó chưa bị phá hủy)
        if (!heartBrick.isDestroyed()) {
            heartBrick.render(gc);
        }

        // Vẽ thanh máu (HP Bar) - CHỈ KHI TRÁI TIM BỊ VỠ
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

            if (hpRatio > 0.6) {
                gc.setFill(javafx.scene.paint.Color.LIMEGREEN);
            } else if (hpRatio > 0.3) {
                gc.setFill(javafx.scene.paint.Color.YELLOW);
            } else {
                gc.setFill(javafx.scene.paint.Color.RED);
            }
            gc.fillRect(barX, barY, currentHpWidth, BAR_HEIGHT);
        }
    }

    /**
     * Hàm này sẽ được gọi khi Boss cần nhả vật thể.
     * Bạn có thể nhả PowerUp, Projectile hoặc Boss minion ở đây.
     */
    private void spawnItem() {
        // Ví dụ: Nhả một PowerUp MEDICINE (hoặc một loại khác)
        // Vị trí: Từ tâm của Boss, rơi xuống.
        if (GameManagerHolder.INSTANCE != null) {
            double itemWidth = 30;
            double itemHeight = 20;
            double itemX = x + width / 2 - itemWidth / 2;
            double itemY = y + height - itemHeight; // Nhả từ dưới Boss

            // Bạn có thể nhả MEDICINE, LOSE_LIFE, hoặc một loại Projectile của Boss
            // Đây là ví dụ nhả MEDICINE, bạn có thể thay đổi
            PowerUp powerUp = new PowerUp(itemX, itemY, itemWidth, itemHeight, PowerUp.PowerUpType.MEDICINE);
            GameManagerHolder.INSTANCE.addPowerUp(powerUp); // Cần thêm hàm addPowerUp vào GameManager
        }
    }

    /** Cách giữ tham chiếu tới GameManager (giống BossLevel2) */
    public static class GameManagerHolder {
        public static GameManager INSTANCE;
    }
}


































