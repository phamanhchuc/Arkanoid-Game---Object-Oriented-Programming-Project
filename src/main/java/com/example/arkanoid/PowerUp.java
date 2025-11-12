package com.example.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import java.util.Objects;

public class PowerUp extends MovableObject {
    public enum PowerUpType {
        LIFE,
        LOSE_LIFE,
        CROSS_BOW,
        MULTI_BALL,
        PIERCING_SHOT,
        MEDICINE,
        STUN_PADDLE // <-- Loại mới: Làm Paddle dừng lại
    }

    private PowerUpType type;
    private double fallSpeed = 200;
    private boolean collected = false;

    // ----- Biến Animation -----
    private boolean isAnimated = false;
    private int numFrames = 1;
    private double frameDurationSeconds = 0.15;
    private int currentFrame = 0;
    private double animationTime = 0;
    private int frameWidth = 0;
    private int frameHeight = 0;
    private int animationCols = 1;
    private int animationRows = 1;
    // -------------------------

    /**
     * Constructor đã được sửa đổi để TỰ ĐỘNG đặt kích thước và tải ảnh
     * dựa trên loại (type) của PowerUp.
     */
    public PowerUp(double x, double y, double width, double height, PowerUpType type) {
        super(x, y, width, height); // Gọi constructor cha

        this.type = type;
        this.dy = fallSpeed;

        double newWidth = this.width;
        double newHeight = this.height;

        try {
            String imagePath = "";

            // --- CÀI ĐẶT THÔNG SỐ ANIMATION & KÍCH THƯỚC ---
            isAnimated = false;
            animationRows = 1;
            animationCols = 1;
            numFrames = 1;

            switch (type) {
                case LIFE:
                    imagePath = "/com/example/arkanoid/images/1life.png";
                    newWidth = 35;
                    newHeight = 55;
                    break;

                case CROSS_BOW:
                    imagePath = "/com/example/arkanoid/images/cross_bow_pickup.png";
                    newWidth = 35;
                    newHeight = 35;
                    break;

                case MULTI_BALL:
                    imagePath = "/com/example/arkanoid/images/multi_ball_pickup.png";
                    newWidth = 35;
                    newHeight = 35;
                    break;

                case PIERCING_SHOT:
                    imagePath = "/com/example/arkanoid/images/meomeobullet_pickup.png";
                    newWidth = 35;
                    newHeight = 35;
                    break;

                case MEDICINE:
                    imagePath = "/com/example/arkanoid/images/medicineItem.png";
                    newWidth = 40;
                    newHeight = 30;
                    isAnimated = true;
                    animationRows = 1;
                    animationCols = 8;
                    numFrames = 8;
                    frameDurationSeconds = 0.15;
                    break;

                case LOSE_LIFE:
                    imagePath = "/com/example/arkanoid/images/fball.png";
                    newWidth = 35;
                    newHeight = 60;
                    isAnimated = true;
                    animationRows = 4;
                    animationCols = 5;
                    numFrames = 20;
                    frameDurationSeconds = 0.05;
                    break;

                case STUN_PADDLE:
                    imagePath = "/com/example/arkanoid/images/stun_power.png";
                    newWidth = 60; // Kích thước vật phẩm Stun
                    newHeight = 100;
                    isAnimated = true;
                    animationRows = 4;
                    animationCols = 5;
                    numFrames = 20;
                    frameDurationSeconds = 0.05;
                    break;
            }
            // --- KẾT THÚC CÀI ĐẶT ---


            // Ghi đè kích thước mới
            this.width = newWidth;
            this.height = newHeight;


            if (!imagePath.isEmpty()) {
                // Sử dụng Objects.requireNonNull để đảm bảo tệp tồn tại và tránh lỗi NullPointerException
                image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));

                if (image.isError()) {
                    System.err.println("Lỗi: Không thể tải ảnh cho PowerUp: " + imagePath);
                    if(image.getException() != null) image.getException().printStackTrace();
                    image = null;
                } else {
                    // Logic tính toán Frame
                    if (isAnimated) {
                        frameWidth = (int) (image.getWidth() / animationCols);
                        frameHeight = (int) (image.getHeight() / animationRows);
                    }
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
        move(dt);

        // Cập nhật animation
        if (isAnimated) {
            animationTime += dt;
            if (animationTime >= frameDurationSeconds) {
                animationTime -= frameDurationSeconds;
                currentFrame = (currentFrame + 1) % numFrames; // Quay vòng frame
            }
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (!collected) {
            if (image != null && !image.isError()) {

                // LOGIC RENDER
                if (isAnimated) {
                    int col = currentFrame % animationCols;
                    int row = currentFrame / animationCols;

                    double sx = col * frameWidth;
                    double sy = row * frameHeight;
                    double sw = frameWidth;
                    double sh = frameHeight;

                    gc.drawImage(image, sx, sy, sw, sh, x, y, width, height);
                } else {
                    gc.drawImage(image, x, y, width, height);
                }
            } else {
                // Vẽ màu dự phòng
                gc.setFill(Color.web("#CCCCCC")); // Mặc định màu xám
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
                else if (type == PowerUpType.STUN_PADDLE) {
                    gc.setFill(Color.YELLOW);
                }
                gc.fillRect(x, y, width, height);
            }
        }
    }
}