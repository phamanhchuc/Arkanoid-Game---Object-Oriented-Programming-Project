package com.example.arkanoid.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Paddle extends MovableObject {
    private double speed = 500;
    private double sceneWidth;
    private double playAreaOffsetX;
    private double playAreaWidth;

    private Image originalImage;
    private Image crossBowImage;

    private double originalWidth;
    private double originalHeight;
    private double originalY;

    private final double CROSS_BOW_WIDTH = 120.0;
    private final double CROSS_BOW_HEIGHT = 70.0;
    private final double CROSS_BOW_Y_OFFSET = -40.0;

    // --- THÊM: Biến xử lý làm chậm ---
    private boolean isSlowed = false;
    private double slowTimer = 0.0;
    // --------------------------------

    public void setPlayArea(double offsetX, double width) {
        this.playAreaOffsetX = offsetX;
        this.playAreaWidth = width;
    }

    public Paddle(double x, double y, double w, double h, double sceneWidth) {
        super(x, y, w, h);
        this.originalWidth = w;
        this.originalHeight = h;
        this.originalY = y;
        this.sceneWidth = sceneWidth;
        try {
            originalImage = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/Paddle1.png"));
            this.image = originalImage;

            // Load ảnh nỏ (nếu có)
            try {
                crossBowImage = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/cbpaddle.png"));
            } catch (Exception e) { /* Bỏ qua */ }

        } catch (Exception e) {
            System.err.println("Lỗi: Không thể tải ảnh cho Paddle.");
        }
    }

    public void setCrossBowActive(boolean active) {
        double oldWidth = this.width;
        if (active && crossBowImage != null) {
            this.image = crossBowImage;
            this.width = CROSS_BOW_WIDTH;
            this.height = CROSS_BOW_HEIGHT;
            this.y = originalY + CROSS_BOW_Y_OFFSET;
        } else {
            this.image = originalImage;
            this.width = originalWidth;
            this.height = originalHeight;
            this.y = originalY;
        }
        double widthDifference = this.width - oldWidth;
        this.x = this.x - (widthDifference / 2.0);
        clampPosition();
    }

    // --- THÊM: Hàm áp dụng hiệu ứng làm chậm ---
    public void applySlow(double duration) {
        this.isSlowed = true;
        this.slowTimer = duration;
    }
    // ------------------------------------------

    public void moveLeft() {
        // Nếu bị làm chậm, tốc độ giảm một nửa
        double currentSpeed = isSlowed ? speed * 0.5 : speed;
        dx = -currentSpeed;
    }

    public void moveRight() {
        double currentSpeed = isSlowed ? speed * 0.5 : speed;
        dx = currentSpeed;
    }

    public void stop() {
        dx = 0;
    }

    public void moveTo(double targetX) {
        this.x = targetX - (this.width / 2);
        clampPosition();
        stop();
    }

    private void clampPosition() {
        if (x < playAreaOffsetX) x = playAreaOffsetX;
        if (x + width > playAreaOffsetX + playAreaWidth) x = playAreaOffsetX + playAreaWidth - width;
    }

    @Override
    public void update(double dt) {
        // --- THÊM: Cập nhật timer làm chậm ---
        if (isSlowed) {
            slowTimer -= dt;
            if (slowTimer <= 0) {
                isSlowed = false;
                System.out.println("Paddle đã hết bị làm chậm!");
            }
        }
        // -------------------------------------

        if (dx != 0) {
            move(dt);
            clampPosition();
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (image != null && !image.isError()) {
            gc.drawImage(image, x, y, width, height);

            // (Tùy chọn) Vẽ hiệu ứng màu xanh khi bị chậm
            if (isSlowed) {
                gc.setGlobalAlpha(0.3);
                gc.setFill(javafx.scene.paint.Color.CYAN);
                gc.fillRect(x, y, width, height);
                gc.setGlobalAlpha(1.0);
            }
        } else {
            gc.setFill(javafx.scene.paint.Color.LIGHTGRAY);
            gc.fillRect(x, y, width, height);
        }
    }
}