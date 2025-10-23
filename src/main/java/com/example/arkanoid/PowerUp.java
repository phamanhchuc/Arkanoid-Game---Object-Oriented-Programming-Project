package com.example.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class PowerUp extends MovableObject {
    public enum PowerUpType {
        LIFE
    }

    private PowerUpType type;
    private double fallSpeed = 200;
    private boolean collected = false; // Đánh dấu nếu đã được thu thập

    public PowerUp(double x, double y, double width, double height, PowerUpType type) {
        super(x, y, width, height);
        this.type = type;
        this.dy = fallSpeed; // Bắt đầu rơi xuống

        // Tải hình ảnh dựa trên loại PowerUp
        try {
            String imagePath = "";
            switch (type) {
                case LIFE:
                    imagePath = "/com/example/arkanoid/images/1life.png";
                    break;
                // Thêm các case khác nếu có thêm loại PowerUp
            }
            if (!imagePath.isEmpty()) {
                image = new Image(getClass().getResourceAsStream(imagePath));
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
    }

    @Override
    public void render(GraphicsContext gc) {
        if (!collected) {
            if (image != null) {
                gc.drawImage(image, x, y, width, height);
            } else {
                // Dự phòng: Vẽ hình vuông màu nếu không tải được ảnh
                gc.setFill(Color.GREEN);
                gc.fillRect(x, y, width, height);
            }
        }
    }
}
