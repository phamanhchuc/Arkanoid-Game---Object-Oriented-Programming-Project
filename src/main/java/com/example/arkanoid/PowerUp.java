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
        PIERCING_SHOT
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
                    break;
                case LOSE_LIFE:
                    imagePath = "/com/example/arkanoid/images/lose_life.png";
                    break;
                case CROSS_BOW:
                    imagePath = "/com/example/arkanoid/images/cross_bow_pickup.png";
                    break;
                case MULTI_BALL:
                    imagePath = "/com/example/arkanoid/images/multi_ball_pickup.png";
                    break;
                case PIERCING_SHOT:
                    imagePath = "/com/example/arkanoid/images/meomeobullet_pickup.png";
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
                // Vẽ màu dự phòng cho đúng loại
                if (type == PowerUpType.LIFE) {
                    gc.setFill(Color.GREEN);
                } else if (type == PowerUpType.LOSE_LIFE) {
                    gc.setFill(Color.RED);
                }
                else if (type == PowerUpType.CROSS_BOW) {
                    gc.setFill(Color.BLUE); // Màu dự phòng cho Crossbow
                }
                else if (type == PowerUpType.MULTI_BALL) {
                    gc.setFill(Color.ORANGE); // Màu dự phòng cho Multi-Ball
                }
                else if (type == PowerUpType.PIERCING_SHOT) {
                    gc.setFill(Color.PURPLE); // Màu dự phòng cho đạn xuyên thấu
                }
                gc.fillRect(x, y, width, height);

            }
        }
    }
}
