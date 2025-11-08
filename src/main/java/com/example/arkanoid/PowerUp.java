package com.example.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class PowerUp extends MovableObject {
    public enum PowerUpType {
        LIFE,
        LOSE_LIFE,
        CROSS_BOW,
        MULTI_BALL,
        PIERCING_SHOT,
        MEDICINE // <-- Loại mới của bạn
    }

    private PowerUpType type;
    private double fallSpeed = 200;
    private boolean collected = false;

    // --- THÊM: Biến Animation (Giống Ball/Boss) ---
    private boolean isAnimated = false;
    private int numFrames = 1;
    private double frameDurationSeconds = 0.15; // Tốc độ animation (ví dụ)
    private int currentFrame = 0;
    private double animationTime = 0;
    private int frameWidth = 0;
    private int frameHeight = 0;
    // --- KẾT THÚC THÊM ---

    public PowerUp(double x, double y, double width, double height, PowerUpType type) {
        super(x, y, width, height);
        this.type = type;
        this.dy = fallSpeed;

        try {
            String imagePath = "";
            switch (type) {
                case LIFE:
                    imagePath = "/com/example/arkanoid/images/1life.png";
                    isAnimated = false;
                    break;
                case LOSE_LIFE:
                    imagePath = "/com/example/arkanoid/images/lose_life.png";
                    isAnimated = false;
                    break;
                case CROSS_BOW:
                    imagePath = "/com/example/arkanoid/images/cross_bow_pickup.png";
                    isAnimated = false;
                    break;
                case MULTI_BALL:
                    imagePath = "/com/example/arkanoid/images/multi_ball_pickup.png";
                    isAnimated = false;
                    break;
                case PIERCING_SHOT:
                    imagePath = "/com/example/arkanoid/images/meomeobullet_pickup.png";
                    isAnimated = false;
                    break;

                // --- SỬA CASE MEDICINE ---
                case MEDICINE:
                    imagePath = "/com/example/arkanoid/images/medicineItem.png";
                    isAnimated = true;
                    numFrames = 8; // <-- Có 8 frame trong ảnh
                    frameDurationSeconds = 0.15; // <-- Chỉnh tốc độ ở đây
                    break;
                // --- KẾT THÚC SỬA ---
            }

            if (!imagePath.isEmpty()) {
                image = new Image(getClass().getResourceAsStream(imagePath));
                if (image == null || image.isError()) {
                    System.err.println("Lỗi: Không thể tải ảnh cho PowerUp: " + imagePath);
                    if(image != null && image.getException() != null) image.getException().printStackTrace();
                    image = null;
                } else {
                    // --- THÊM TÍNH TOÁN FRAME ---
                    if (isAnimated) {
                        frameWidth = (int) (image.getWidth() / numFrames);
                        frameHeight = (int) image.getHeight();
                    }
                    // --- KẾT THÚC THÊM ---
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi: Không thể tải ảnh cho PowerUp loại " + type.name());
            e.printStackTrace();
        }
    }

    public PowerUpType getType() {
        return type;
    }

    public boolean isCollected() {
        return collected;
    }

    public void setCollected(boolean collected) {
        this.collected = collected;
    }

    @Override
    public void update(double dt) {
        move(dt); // Cập nhật vị trí (rơi xuống)

        // --- THÊM: Cập nhật animation ---
        if (isAnimated) {
            animationTime += dt;
            if (animationTime >= frameDurationSeconds) {
                animationTime -= frameDurationSeconds;
                currentFrame++;
                if (currentFrame >= numFrames) {
                    currentFrame = 0;
                }
            }
        }
        // --- KẾT THÚC THÊM ---
    }

    @Override
    public void render(GraphicsContext gc) {
        if (!collected) {
            if (image != null && !image.isError()) {

                // --- SỬA LOGIC RENDER ---
                if (isAnimated) {
                    // Vẽ frame animation
                    double sx = currentFrame * frameWidth; // Source X
                    double sy = 0;                         // Source Y
                    double sw = frameWidth;                // Source Width
                    double sh = frameHeight;               // Source Height
                    gc.drawImage(image, sx, sy, sw, sh, x, y, width, height);
                } else {
                    // Vẽ ảnh tĩnh
                    gc.drawImage(image, x, y, width, height);
                }
                // --- KẾT THÚC SỬA ---

            } else {
                // (Khối vẽ màu dự phòng giữ nguyên)
                if (type == PowerUpType.LIFE) {
                    gc.setFill(Color.GREEN);
                } else if (type == PowerUpType.LOSE_LIFE) {
                    gc.setFill(Color.RED);
                }
                else if (type == PowerUpType.CROSS_BOW) {
                    gc.setFill(Color.BLUE);
                }
                else if (type == PowerUpType.MULTI_BALL) {
                    gc.setFill(Color.ORANGE);
                }
                else if (type == PowerUpType.PIERCING_SHOT) {
                    gc.setFill(Color.PURPLE);
                }
                else if (type == PowerUpType.MEDICINE) {
                    gc.setFill(Color.LIGHTPINK);
                }
                gc.fillRect(x, y, width, height);
            }
        }
    }
}