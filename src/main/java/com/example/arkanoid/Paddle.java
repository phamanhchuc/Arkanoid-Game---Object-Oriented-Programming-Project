package com.example.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image; // Thêm import

public class Paddle extends MovableObject {
    private double speed = 500;
    private double sceneWidth;
    private double playAreaOffsetX;
    private double playAreaWidth;

    // Thêm phương thức này vào lớp Paddle
    public void setPlayArea(double offsetX, double width) {
        this.playAreaOffsetX = offsetX;
        this.playAreaWidth = width;
    }

    public Paddle(double x, double y, double w, double h, double sceneWidth) {
        super(x, y, w, h);
        this.sceneWidth = sceneWidth;
        // Tải hình ảnh của thanh đỡ
        try {
            // Đảm bảo đường dẫn này chính xác!
            image = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/Paddle1.png"));
        } catch (Exception e) {
            System.err.println("Lỗi: Không thể tải ảnh cho Paddle.");
            e.printStackTrace();
        }
    }

    public void moveLeft(){ dx = -speed; }
    public void moveRight(){ dx = speed; }
    public void stop(){ dx = 0; }

    @Override
    public void update(double dt) {
        move(dt);
        // Giữ paddle trong khu vực chơi
        if (x < playAreaOffsetX) {
            x = playAreaOffsetX;
        }
        if (x + width > playAreaOffsetX + playAreaWidth) {
            x = playAreaOffsetX + playAreaWidth - width;
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        // Vẽ hình ảnh thay vì hình chữ nhật
        if (image != null) {
            gc.drawImage(image, x, y, width, height);
        } else {
            // Dự phòng: Vẽ hình chữ nhật nếu không tải được ảnh
            gc.setFill(javafx.scene.paint.Color.LIGHTGRAY);
            gc.fillRect(x, y, width, height);
        }
    }
}

