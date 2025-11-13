package com.example.arkanoid.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.Objects;

public class HeartBrick extends Brick {

    private Image heartSpriteSheet;
    private final int numFrames = 5; // Dùng 5 frame để hiển thị 5 mức HP
    private int currentFrame = 0;
    private double frameTime = 0;
    private final double frameDuration = 0.2; // <-- THỜI GIAN CHUYỂN FRAME (0.4 giây)

    private double frameWidth = 0;

    public HeartBrick(double x, double y, double width, double height, int initialHp) {
        // Dùng typeCode 9 (ví dụ) hoặc 3 (như cũ)
        super(x, y, width, height, 9);
        this.indestructible = true;
        // 🟢 GHI ĐÈ HITS & MAXHITS BẰNG HP
        this.hits = initialHp;
        this.maxHits = initialHp;
        this.scoreValue = 0;
        try {
            // 🟢 THÊM LẠI LOGIC TẢI ẢNH
            heartSpriteSheet = new Image(Objects.requireNonNull(
                    getClass().getResourceAsStream("/com/example/arkanoid/images/HeartBrick.png")
            ));
            this.currentFrame = 0;

            // 🟢 THÊM: Tính toán chiều rộng của mỗi frame
            if (heartSpriteSheet != null && heartSpriteSheet.getWidth() > 0) {
                this.frameWidth = heartSpriteSheet.getWidth() / numFrames;
            }

        } catch (Exception e) {
            System.err.println("Lỗi: Không thể tải ảnh cho HeartBrick spritesheet.");
            e.printStackTrace();
        }
    }

    @Override
    public boolean takeHit() {
        if (hits > 0) {
            hits--;

            if (hits <= 0) {
                this.destroyed = true;
                // --- THÔNG BÁO CHO BOSS LEVEL 3 RẰNG TRÁI TIM ĐÃ BỊ PHÁ HỦY ---
                if (BossLevel3.GameManagerHolder.INSTANCE != null &&
                        BossLevel3.GameManagerHolder.INSTANCE.getCurrentBoss() instanceof BossLevel3) {

                    ((BossLevel3) BossLevel3.GameManagerHolder.INSTANCE.getCurrentBoss()).setHeartDestroyed(true);
                }
                return true; // Bị phá hủy
            }
        }
        return false; // Chưa bị phá hủy
    }

    @Override
    public void update(double dt) {
        if (this.isDestroyed()) {
            return;
        }

        // 🟢 PHỤC HỒI LOGIC CHUYỂN FRAME SAU MỖI frameDuration (0.4s)
        frameTime += dt;
        if (frameTime >= frameDuration) {
            // Chuyển sang frame tiếp theo (vòng lặp 0 -> 4 -> 0)
            currentFrame = (currentFrame + 1) % numFrames;
            frameTime -= frameDuration;
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (this.isDestroyed()) {
            return;
        }

        if (heartSpriteSheet != null && !heartSpriteSheet.isError() && frameWidth > 0) { // Thêm kiểm tra frameWidth
            // Dùng currentFrame đã được tính trong update()
            double sx = currentFrame * frameWidth;
            double sy = 0;
            double sw = frameWidth;
            double sh = heartSpriteSheet.getHeight();

            gc.drawImage(heartSpriteSheet, sx, sy, sw, sh, x, y, width, height);
        } else {
            gc.setFill(javafx.scene.paint.Color.RED);
            gc.fillRect(x, y, width, height);
        }
        // ===============================================
        // 🟢 2. RENDER THANH MÁU (HP Bar)
        // ===============================================

        // Kích thước và vị trí thanh máu
        double hpBarHeight = 5;
        double hpBarPadding = 2;
        double hpBarY = y - hpBarHeight - hpBarPadding; //Thanh máu nằm trên
        double hpBarWidth = width;

        // Tính toán HP còn lại
        double healthRatio = (double) hits / maxHits;
        double currentHpWidth = hpBarWidth * healthRatio;

        // 2a. Vẽ nền (HP tối đa - Màu xám hoặc đen)
        gc.setFill(javafx.scene.paint.Color.GRAY);
        gc.fillRect(x, hpBarY, hpBarWidth, hpBarHeight);

        // 2b. Vẽ HP hiện tại (Màu xanh lá hoặc màu khác)
        gc.setFill(javafx.scene.paint.Color.LIMEGREEN); // Xanh lá cây
        if (healthRatio < 0.3) {
            gc.setFill(javafx.scene.paint.Color.ORANGERED); // Đổi màu khi máu thấp
        }

        gc.fillRect(x, hpBarY, currentHpWidth, hpBarHeight);

        // Tùy chọn: Vẽ viền cho thanh máu
        gc.setStroke(javafx.scene.paint.Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(x, hpBarY, hpBarWidth, hpBarHeight);

        // ===============================================
    }
}