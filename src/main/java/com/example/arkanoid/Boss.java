package com.example.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import java.util.Objects;
import java.util.Random;

public class Boss extends MovableObject {

    // Animation
    private Image imageLeft;
    private Image imageRight;
    private Image currentImage;
    private static final int NUM_FRAMES = 4;
    private static final double FRAME_DURATION_SECONDS = 0.15;
    private int currentFrame = 0;
    private double animationTime = 0;
    private int frameWidth;
    private int frameHeight;

    // Di chuyển
    private double speed = 150;
    private double playAreaOffsetX;
    private double playAreaWidth;

    // --- THÊM: Logic thả thuốc ---
    private Random random = new Random();
    private double medicineTimer = 0;
    private double medicineInterval = 10 + random.nextDouble() * 5; // Thả mỗi 10-15 giây
    // -----------------------------

    public Boss(double x, double y, double w, double h) {
        super(x, y, w, h);

        try {
            imageLeft = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/arkanoid/images/boss1_left.png")));
            imageRight = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/arkanoid/images/boss1_right.png")));
            this.frameWidth = (int) (imageRight.getWidth() / NUM_FRAMES);
            this.frameHeight = (int) imageRight.getHeight();
        } catch (Exception e) {
            System.err.println("Lỗi: Không thể tải ảnh cho Boss spritesheet.");
        }

        this.dx = speed;
        this.currentImage = imageRight;
    }

    public void setPlayArea(double offsetX, double width) {
        this.playAreaOffsetX = offsetX;
        this.playAreaWidth = width;
    }

    @Override
    public void update(double dt) {
        move(dt);

        // Animation
        animationTime += dt;
        if (animationTime >= FRAME_DURATION_SECONDS) {
            animationTime -= FRAME_DURATION_SECONDS;
            currentFrame = (currentFrame + 1) % NUM_FRAMES;
        }

        // Di chuyển qua lại
        if (dx < 0 && x <= playAreaOffsetX) {
            x = playAreaOffsetX; dx = speed;
            currentImage = imageRight; currentFrame = 0;
        } else if (dx > 0 && x + width >= playAreaOffsetX + playAreaWidth) {
            x = playAreaOffsetX + playAreaWidth - width; dx = -speed;
            currentImage = imageLeft; currentFrame = 0;
        }

        // --- THÊM: Logic thả thuốc ---
        medicineTimer += dt;
        if (medicineTimer >= medicineInterval) {
            spawnMedicine();
            medicineTimer = 0;
            medicineInterval = 10 + random.nextDouble() * 5;
        }
        // -----------------------------
    }

    // --- THÊM: Hàm sinh ra thuốc ---
    private void spawnMedicine() {
        // Boss.java chưa có GameManagerHolder như Boss 2/3,
        // ta dùng cách truy cập tĩnh hoặc truyền vào (nhưng ở đây dùng tạm cách PowerUp rơi tự do)
        // Để chuẩn nhất, bạn nên thêm GameManagerHolder cho Boss.java giống BossLevel2
        if (BossLevel2.GameManagerHolder.INSTANCE != null) {
            BossLevel2.GameManagerHolder.INSTANCE.spawnMedicine(x + width / 2 - 15, y + height);
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (currentImage != null) {
            double sx = currentFrame * frameWidth;
            gc.drawImage(currentImage, sx, 0, frameWidth, frameHeight, x, y, width, height);
        } else {
            gc.setFill(Color.PURPLE);
            gc.fillRect(x, y, width, height);
        }
    }
}