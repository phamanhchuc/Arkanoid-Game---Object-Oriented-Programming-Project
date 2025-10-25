package com.example.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class PowerUp extends MovableObject {
    public enum PowerUpType {
        LIFE,
        LOSE_LIFE
    }

    private PowerUpType type;
    private double fallSpeed = 200;
    private boolean collected = false;

    public PowerUp(double x, double y, double width, double height, PowerUpType type) {
        super(x, y, width, height);
        this.type = type;
        this.dy = fallSpeed;

        try {
            String imagePath = "";
            switch (type) {
                case LIFE:
                    imagePath = "/com/example/arkanoid/images/1life.png";
                    break; // <-- LỖI Ở ĐÂY: Bạn đã thiếu 'break;' này
                case LOSE_LIFE:
                    imagePath = "/com/example/arkanoid/images/lose_life.png";
                    break;
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
                // --- PHẦN SỬA LỖI NHỎ ---
                // Vẽ màu dự phòng cho đúng loại
                if (type == PowerUpType.LIFE) {
                    gc.setFill(Color.GREEN);
                } else if (type == PowerUpType.LOSE_LIFE) {
                    gc.setFill(Color.RED);
                }
                gc.fillRect(x, y, width, height);
                // --- KẾT THÚC PHẦN SỬA ---
            }
        }
    }
}
