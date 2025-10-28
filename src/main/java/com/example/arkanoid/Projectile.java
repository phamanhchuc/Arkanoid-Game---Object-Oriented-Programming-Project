package com.example.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Projectile extends MovableObject {

    private static final double PROJECTILE_SPEED = -600; // Tốc độ bay (âm là đi lên)
    private boolean destroyed = false;

    public Projectile(double x, double y, double width, double height) {
        super(x, y, width, height);
        this.dy = PROJECTILE_SPEED; // Thiết lập hướng di chuyển

        try {
            // Tải ảnh mũi tên
            image = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/arrow.png"));
            if (image == null || image.isError()) {
                System.err.println("LỖI: Không tìm thấy file ảnh arrow.png");
            }
        } catch (Exception e) {
            System.err.println("Lỗi: Không thể tải ảnh cho Projectile.");
            e.printStackTrace();
        }
    }

    @Override
    public void update(double dt) {
        move(dt);
    }

    @Override
    public void render(GraphicsContext gc) {
        if (image != null && !image.isError()) {
            gc.drawImage(image, x, y, width, height);
        } else {
            // Dự phòng: Vẽ hình chữ nhật nếu không tải được ảnh
            gc.setFill(Color.YELLOW);
            gc.fillRect(x, y, width, height);
        }
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }
}
