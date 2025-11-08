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

    // ----- Biến Animation (Logic Grid DUY NHẤT) -----
    private boolean isAnimated = false;
    private int numFrames = 1;
    private double frameDurationSeconds = 0.15;
    private int currentFrame = 0;
    private double animationTime = 0;
    private int frameWidth = 0;
    private int frameHeight = 0;
    private int animationCols = 1;
    private int animationRows = 1;
    // ----------------------------------------------------

    /**
     * Constructor đã được sửa đổi để TỰ ĐỘNG đặt kích thước (tỉ lệ)
     * dựa trên loại (type) của PowerUp.
     */
    public PowerUp(double x, double y, double width, double height, PowerUpType type) {
        // --- SỬA LỖI Java 24: Gọi super() NGAY LẬP TỨC ---
        // Gọi với kích thước ban đầu (ví dụ: 50x70 từ Factory)
        super(x, y, width, height);

        this.type = type;
        this.dy = fallSpeed;

        // Biến tạm để lưu kích thước MỚI (theo ý bạn)
        double newWidth = this.width; // Bắt đầu bằng kích thước đã super()
        double newHeight = this.height; // Bắt đầu bằng kích thước đã super()

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
                    // --- KÍCH THƯỚC MỚI CỦA BẠN ---
                    newWidth = 35;
                    newHeight = 55;
                    break;

                case CROSS_BOW:
                    imagePath = "/com/example/arkanoid/images/cross_bow_pickup.png";
                    // --- KÍCH THƯỚC MỚI CỦA BẠN ---
                    newWidth = 35;
                    newHeight = 35;
                    break;

                case MULTI_BALL:
                    imagePath = "/com/example/arkanoid/images/multi_ball_pickup.png";
                    // --- KÍCH THƯỚC MỚI CỦA BẠN ---
                    newWidth = 35;
                    newHeight = 35;
                    break;

                case PIERCING_SHOT:
                    imagePath = "/com/example/arkanoid/images/meomeobullet_pickup.png";
                    // --- KÍCH THƯỚC MỚI CỦA BẠN ---
                    newWidth = 35;
                    newHeight = 35;
                    break;

                // --- SỬA CASE MEDICINE (Grid 1x8) ---
                case MEDICINE:
                    imagePath = "/com/example/arkanoid/images/medicineItem.png";
                    // --- KÍCH THƯỚC MỚI CỦA BẠN ---
                    newWidth = 40;
                    newHeight = 30;

                    isAnimated = true;
                    animationRows = 1;
                    animationCols = 8;
                    numFrames = 8;
                    frameDurationSeconds = 0.15;
                    break;

                // --- SỬA CASE LOSE_LIFE (Grid 12x5) ---
                case LOSE_LIFE:
                    imagePath = "/com/example/arkanoid/images/fball.png";
                    // --- KÍCH THƯỚC MỚI CỦA BẠN ---
                    newWidth = 35;
                    newHeight = 60;

                    isAnimated = true;
                    animationRows = 4; // (Bạn ghi 12x5 nhưng code là 4x5, tôi giữ 4x5)
                    animationCols = 5;
                    numFrames = 20;
                    frameDurationSeconds = 0.05;
                    break;
            }
            // --- KẾT THÚC CÀI ĐẶT ---


            // --- GHI ĐÈ kích thước của đối tượng (width, height là từ MovableObject) ---
            this.width = newWidth;
            this.height = newHeight;


            if (!imagePath.isEmpty()) {
                image = new Image(getClass().getResourceAsStream(imagePath));
                if (image == null || image.isError()) {
                    System.err.println("Lỗi: Không thể tải ảnh cho PowerUp: " + imagePath);
                    if(image != null && image.getException() != null) image.getException().printStackTrace();
                    image = null;
                } else {
                    // --- Logic tính toán Frame (Đã đơn giản hóa) ---
                    if (isAnimated) {
                        frameWidth = (int) (image.getWidth() / animationCols);
                        frameHeight = (int) (image.getHeight() / animationRows);
                    }
                    // --- KẾT THÚC ---
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

        // --- Cập nhật animation (Dùng chung cho tất cả) ---
        if (isAnimated) {
            animationTime += dt;
            if (animationTime >= frameDurationSeconds) {
                animationTime -= frameDurationSeconds;
                currentFrame = (currentFrame + 1) % numFrames; // Quay vòng frame
            }
        }
        // --- KẾT THÚC ---
    }

    @Override
    public void render(GraphicsContext gc) {
        if (!collected) {
            if (image != null && !image.isError()) {

                // --- LOGIC RENDER (Đã đơn giản hóa) ---
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
                // --- KẾT THÚC ---

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