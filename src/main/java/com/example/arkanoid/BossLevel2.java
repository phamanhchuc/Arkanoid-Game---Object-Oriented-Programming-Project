package com.example.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import java.util.Objects;
import java.util.Random;

public class BossLevel2 extends MovableObject {

    private Image imageLeft;
    private Image imageRight;

    private int numFrames = 5;
    private int currentFrame = 0;
    private double frameTime = 0;
    private double frameDuration = 0.15;

    private boolean facingLeft = true;

    private int hp;
    private final int maxHp;

    private double playAreaX;
    private double playAreaWidth;

    private double speed = 150; // pixel/giây
    private Random random = new Random();
    private double medicineTimer = 0;
    private double medicineInterval = 15 + random.nextDouble() * 5; // 15-20s

    private final double MEDICINE_WIDTH = 30;
    private final double MEDICINE_HEIGHT = 20;

    public BossLevel2(double x, double y, double width, double height, int hp, double playAreaX, double playAreaWidth) {
        super(x, y, width, height);
        this.hp = hp;
        this.maxHp = hp;
        this.playAreaX = playAreaX;
        this.playAreaWidth = playAreaWidth;

        imageLeft = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/arkanoid/images/Boss2_left.png")));
        imageRight = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/arkanoid/images/Boss2_right.png")));

        this.dx = speed; // bắt đầu bay sang phải
    }

    public int getHp() { return hp; }

    public boolean takeDamage(int dmg) {
        hp -= dmg;
        return hp > 0;
    }

    @Override
    public void update(double dt) {
        // Di chuyển trái–phải
        if (x <= playAreaX) {
            facingLeft = false;
            x = playAreaX;
        } else if (x + width >= playAreaX + playAreaWidth) {
            facingLeft = true;
            x = playAreaX + playAreaWidth - width;
        }

        x += (facingLeft ? -dx : dx) * dt;

        // Animation
        frameTime += dt;
        if (frameTime >= frameDuration) {
            frameTime -= frameDuration;
            currentFrame = (currentFrame + 1) % numFrames;
        }

        // Thả medicine nếu boss còn sống
        medicineTimer += dt;
        if (medicineTimer >= medicineInterval) {
            spawnMedicine();
            medicineTimer = 0;
            medicineInterval = 15 + random.nextDouble() * 5;
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        Image img = facingLeft ? imageLeft : imageRight;
        if (img != null) {
            double frameWidth = img.getWidth() / numFrames;
            gc.drawImage(img, frameWidth * currentFrame, 0, frameWidth, img.getHeight(),
                    x, y, width, height);
        }
        // ==========================================================
        // ===== LOGIC RENDER THANH MÁU (HP BAR) =====
        // ==========================================================
        if (hp > 0) {
            final double BAR_WIDTH = width; // Chiều rộng bằng Boss
            final double BAR_HEIGHT = 10;
            final double BAR_Y_OFFSET = 15; // Cách Boss 15 pixel lên trên

            double barX = x;
            double barY = y - BAR_HEIGHT - BAR_Y_OFFSET;

            // 1. Vẽ nền thanh máu (màu đen)
            gc.setFill(javafx.scene.paint.Color.rgb(0, 0, 0, 0.7)); // Đen mờ
            gc.fillRect(barX, barY, BAR_WIDTH, BAR_HEIGHT);

            // 2. Tính toán máu hiện tại
            double hpRatio = (double) hp / maxHp;
            double currentHpWidth = BAR_WIDTH * hpRatio;

            // 3. Chọn màu (Xanh lá -> Vàng -> Đỏ)
            if (hpRatio > 0.6) {
                gc.setFill(javafx.scene.paint.Color.LIMEGREEN); // Xanh lá đậm
            } else if (hpRatio > 0.3) {
                gc.setFill(javafx.scene.paint.Color.YELLOW);
            } else {
                gc.setFill(javafx.scene.paint.Color.RED);
            }

            // 4. Vẽ thanh máu hiện tại
            gc.fillRect(barX, barY, currentHpWidth, BAR_HEIGHT);
        }
        // ==========================================================
        // ===== KẾT THÚC LOGIC RENDER THANH MÁU =====
        // ==========================================================
    }

    public boolean checkCollisionWithBall(Ball ball) {
        double cx = ball.getX() + ball.getWidth() / 2;
        double cy = ball.getY() + ball.getHeight() / 2;
        double radius = ball.getWidth() / 2;

        double closestX = Math.max(x, Math.min(cx, x + width));
        double closestY = Math.max(y, Math.min(cy, y + height));

        double dx = cx - closestX;
        double dy = cy - closestY;

        return (dx * dx + dy * dy) <= (radius * radius);
    }

    /**
     * Gọi để tạo medicine rơi xuống dưới boss.
     * Cần gọi phương thức này của GameManager.
     */
    private void spawnMedicine() {
        // GameManager phải add powerUp vào list powerUps
        if (GameManagerHolder.INSTANCE != null) {
            double mx = x + width / 2 - MEDICINE_WIDTH / 2;
            double my = y + height;
            GameManagerHolder.INSTANCE.spawnMedicine(mx, my);
        }
    }

    /** Cách giữ tham chiếu tới GameManager */
    public static class GameManagerHolder {
        public static GameManager INSTANCE;
    }
}
