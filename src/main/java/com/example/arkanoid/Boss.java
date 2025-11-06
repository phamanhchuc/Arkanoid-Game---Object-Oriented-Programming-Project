package com.example.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.Objects;

/**
 * Lớp Boss, di chuyển qua lại ở đầu màn hình.
 * Kế thừa MovableObject để có thể di chuyển.
 */
public class Boss extends MovableObject {

    // --- Cấu hình Animation ---
    private Image imageLeft;  // Spritesheet bên trái
    private Image imageRight; // Spritesheet bên phải
    private Image currentImage; // Ảnh đang được dùng để vẽ

    private static final int NUM_FRAMES = 4; // Cả 2 file đều có 4 frame
    private static final double FRAME_DURATION_SECONDS = 0.15; // Tốc độ animation (0.15s/frame)
    private int currentFrame = 0;
    private double animationTime = 0;
    private int frameWidth;
    private int frameHeight;

    // --- Cấu hình Di chuyển ---
    private double speed = 150; // Tốc độ bay (pixel/giây)
    private double playAreaOffsetX;
    private double playAreaWidth;

    public Boss(double x, double y, double w, double h) {
        super(x, y, w, h);

        try {
            // Tải 2 ảnh spritesheet
            imageLeft = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/arkanoid/images/boss1_left.png")));
            imageRight = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/arkanoid/images/boss1_right.png")));

            if (imageRight == null || imageLeft == null) {
                System.err.println("LỖI NGHIÊM TRỌNG: Không tìm thấy file ảnh boss1_left.png hoặc boss1_right.png");
            } else {
                // Lấy kích thước 1 frame (giả sử 2 ảnh có kích thước frame như nhau)
                this.frameWidth = (int) (imageRight.getWidth() / NUM_FRAMES);
                this.frameHeight = (int) imageRight.getHeight();
            }
        } catch (Exception e) {
            System.err.println("Lỗi: Không thể tải ảnh cho Boss spritesheet.");
            e.printStackTrace();
        }

        // --- Cài đặt ban đầu ---
        this.dx = speed; // Bắt đầu bằng cách bay sang phải
            this.currentImage = imageRight; // Dùng ảnh bên phải
    }

    /**
     * Gán khu vực hoạt động cho Boss (để nó biết khi nào cần quay đầu).
     */
    public void setPlayArea(double offsetX, double width) {
        this.playAreaOffsetX = offsetX;
        this.playAreaWidth = width;
    }

    @Override
    public void update(double dt) {
        // 1. Di chuyển (hàm move từ lớp cha MovableObject)
        move(dt);

        // 2. Cập nhật animation (logic giống Ball.java)
        animationTime += dt;
        if (animationTime >= FRAME_DURATION_SECONDS) {
            animationTime -= FRAME_DURATION_SECONDS;
            currentFrame++;
            if (currentFrame >= NUM_FRAMES) {
                currentFrame = 0;
            }
        }

        // 3. Xử lý logic quay đầu
        // Nếu chạm biên trái
        if (dx < 0 && x <= playAreaOffsetX) {
            x = playAreaOffsetX; // Đặt lại vị trí
            dx = speed; // Đổi hướng sang phải
            currentImage = imageRight; // Đổi ảnh
            currentFrame = 0; // Reset animation
        }
        // Nếu chạm biên phải
        else if (dx > 0 && x + width >= playAreaOffsetX + playAreaWidth) {
            x = playAreaOffsetX + playAreaWidth - width; // Đặt lại vị trí
            dx = -speed; // Đổi hướng sang trái
            currentImage = imageLeft; // Đổi ảnh
            currentFrame = 0; // Reset animation
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (currentImage != null && !currentImage.isError()) {
            // Tính toán vị trí của frame hiện tại trong spritesheet
            double sx = currentFrame * frameWidth; // Source X
            double sy = 0;                          // Source Y
            double sw = frameWidth;                 // Source Width
            double sh = frameHeight;                // Source Height

            // Vẽ frame đó lên canvas
            gc.drawImage(currentImage, sx, sy, sw, sh, x, y, width, height);
        } else {
            // Vẽ dự phòng nếu ảnh lỗi
            gc.setFill(Color.PURPLE);
            gc.fillRect(x, y, width, height);
        }
    }
}