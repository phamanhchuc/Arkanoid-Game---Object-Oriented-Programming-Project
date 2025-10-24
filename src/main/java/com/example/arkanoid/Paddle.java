package com.example.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Paddle extends MovableObject {
    private double speed = 500;
    private double sceneWidth;
    private double playAreaOffsetX;
    private double playAreaWidth;

    public void setPlayArea(double offsetX, double width) {
        this.playAreaOffsetX = offsetX;
        this.playAreaWidth = width;
    }

    public Paddle(double x, double y, double w, double h, double sceneWidth) {
        super(x, y, w, h);
        this.sceneWidth = sceneWidth;
        try {
            // --- LỖI ĐÃ ĐƯỢC SỬA Ở DÒNG NÀY ---
            // Đường dẫn cũ (SAI): "/com.example.arkanoid/images/Paddle1.png"
            image = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/Paddle1.png"));
            // --- KẾT THÚC SỬA LỖI ---

            if (image == null) {
                System.err.println("LỖI NGHIÊM TRỌNG: Không tìm thấy file ảnh Paddle1.png");
            }
        } catch (Exception e) {
            System.err.println("Lỗi: Không thể tải ảnh cho Paddle.");
            e.printStackTrace();
        }
    }

    public void moveLeft(){ dx = -speed; }
    public void moveRight(){ dx = speed; }
    public void stop(){ dx = 0; }

    /**
     * Di chuyển tâm của thanh đỡ đến tọa độ X của chuột.
     */
    public void moveTo(double targetX) {
        // Đặt vị trí x sao cho tâm của paddle ở targetX
        this.x = targetX - (this.width / 2);

        // Ngay lập tức kiểm tra và áp dụng giới hạn
        if (x < playAreaOffsetX) {
            x = playAreaOffsetX;
        }
        if (x + width > playAreaOffsetX + playAreaWidth) {
            x = playAreaOffsetX + playAreaWidth - width;
        }

        // Dừng di chuyển bằng phím (dx) để tránh xung đột
        stop();
    }

    @Override
    public void update(double dt) {
        // Chỉ di chuyển bằng 'dx' (bàn phím) nếu nó được thiết lập
        if (dx != 0) {
            move(dt);
            // Giữ paddle trong khu vực chơi (cho bàn phím)
            if (x < playAreaOffsetX) {
                x = playAreaOffsetX;
            }
            if (x + width > playAreaOffsetX + playAreaWidth) {
                x = playAreaOffsetX + playAreaWidth - width;
            }
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (image != null && !image.isError()) {
            gc.drawImage(image, x, y, width, height);
        } else {
            // Dự phòng: Vẽ hình chữ nhật nếu không tải được ảnh
            gc.setFill(javafx.scene.paint.Color.LIGHTGRAY);
            gc.fillRect(x, y, width, height);
        }
    }
}