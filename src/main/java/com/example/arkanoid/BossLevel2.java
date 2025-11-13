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

    // --- Timer cho Medicine (Hồi máu) ---
    private double medicineTimer = 0;
    private double medicineInterval = 15 + random.nextDouble() * 5;

    // --- THÊM: Timer cho Fball (Cầu lửa mất mạng) ---
    private double fballTimer = 0;
    private double fballInterval = 4.0; // Thả mỗi 4 giây (có thể chỉnh sửa)
    // ------------------------------------------------

    private final double MEDICINE_WIDTH = 30;
    private final double MEDICINE_HEIGHT = 20;

    public BossLevel2(double x, double y, double width, double height, int hp, double playAreaX, double playAreaWidth) {
        super(x, y, width, height);
        this.hp = hp;
        this.maxHp = hp;
        this.playAreaX = playAreaX;
        this.playAreaWidth = playAreaWidth;

        try {
            imageLeft = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/arkanoid/images/Boss2_left.png")));
            imageRight = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/arkanoid/images/Boss2_right.png")));
        } catch (Exception e) {
            System.err.println("Lỗi tải ảnh Boss 2");
        }

        this.dx = speed;
    }

    public int getHp() { return hp; }

    public boolean takeDamage(int dmg) {
        hp -= dmg;
        return hp > 0;
    }

    @Override
    public void update(double dt) {
        // 1. Di chuyển
        if (x <= playAreaX) {
            facingLeft = false;
            x = playAreaX;
        } else if (x + width >= playAreaX + playAreaWidth) {
            facingLeft = true;
            x = playAreaX + playAreaWidth - width;
        }
        x += (facingLeft ? -dx : dx) * dt;

        // 2. Animation
        frameTime += dt;
        if (frameTime >= frameDuration) {
            frameTime -= frameDuration;
            currentFrame = (currentFrame + 1) % numFrames;
        }

        // 3. Thả Medicine
        medicineTimer += dt;
        if (medicineTimer >= medicineInterval) {
            spawnMedicine();
            medicineTimer = 0;
            medicineInterval = 15 + random.nextDouble() * 5;
        }

        // --- THÊM: Logic thả Fball (LOSE_LIFE) ---
        fballTimer += dt;
        if (fballTimer >= fballInterval) {
            spawnFball();
            fballTimer = 0;
            // Random thời gian thả tiếp theo từ 3 đến 6 giây
            fballInterval = 3.0 + random.nextDouble() * 3.0;
        }
        // -----------------------------------------
    }

    @Override
    public void render(GraphicsContext gc) {
        Image img = facingLeft ? imageLeft : imageRight;
        if (img != null) {
            double frameWidth = img.getWidth() / numFrames;
            gc.drawImage(img, frameWidth * currentFrame, 0, frameWidth, img.getHeight(), x, y, width, height);
        }

        // Render HP Bar
        if (hp > 0) {
            double barX = x;
            double barY = y - 25;
            gc.setFill(javafx.scene.paint.Color.rgb(0, 0, 0, 0.7));
            gc.fillRect(barX, barY, width, 10);
            double hpRatio = (double) hp / maxHp;
            gc.setFill(hpRatio > 0.5 ? javafx.scene.paint.Color.LIMEGREEN : (hpRatio > 0.2 ? javafx.scene.paint.Color.YELLOW : javafx.scene.paint.Color.RED));
            gc.fillRect(barX, barY, width * hpRatio, 10);
        }
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

    private void spawnMedicine() {
        if (GameManagerHolder.INSTANCE != null) {
            GameManagerHolder.INSTANCE.spawnMedicine(x + width / 2 - MEDICINE_WIDTH / 2, y + height);
        }
    }

    // --- THÊM: Hàm thả Fball ---
    private void spawnFball() {
        if (GameManagerHolder.INSTANCE != null) {
            // Tạo PowerUp loại LOSE_LIFE (hình fball.png)
            // Kích thước fball khoảng 35x60 (theo constructor PowerUp)
            PowerUp fball = new PowerUp(x + width / 2 - 17, y + height, 35, 60, PowerUp.PowerUpType.LOSE_LIFE);
            GameManagerHolder.INSTANCE.addPowerUp(fball);
        }
    }
    // ---------------------------

    public static class GameManagerHolder {
        public static GameManager INSTANCE;
    }
}