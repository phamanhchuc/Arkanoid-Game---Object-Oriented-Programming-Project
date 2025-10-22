package com.example.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Brick extends GameObject {
    private int hits;
    private boolean destroyed = false;

    // --- PHẦN MỚI: Mảng chứa 3 trạng thái ảnh ---
    // Index 2: còn 3 máu, Index 1: còn 2 máu, Index 0: còn 1 máu
    private static final Image[] brickImages = new Image[3];

    static {
        try {
            // Tải ảnh cho các trạng thái của gạch 3-hit
            brickImages[2] = new Image(Brick.class.getResourceAsStream("/com/example/arkanoid/images/brick_state_3.png"));
            brickImages[1] = new Image(Brick.class.getResourceAsStream("/com/example/arkanoid/images/brick_state_2.png"));
            brickImages[0] = new Image(Brick.class.getResourceAsStream("/com/example/arkanoid/images/brick_state_1.png"));
        } catch (Exception e) {
            System.err.println("Lỗi nghiêm trọng: Không thể tải được ảnh cho Brick.");
            e.printStackTrace();
        }
    }

    public Brick(double x, double y, double w, double h, int hits) {
        super(x, y, w, h);
        this.hits = hits;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public boolean takeHit() {
        hits--;
        if (hits <= 0) {
            destroyed = true;
        }
        return destroyed;
    }

    @Override
    public void update(double dt) { /* no-op for static brick */ }

    @Override
    public void render(GraphicsContext gc) {
        if (destroyed) {
            return;
        }

        Image imageToDraw = null;

        // --- Logic vẽ được cập nhật cho 3 trạng thái ---
        if (hits == 3) {
            imageToDraw = brickImages[2]; // Nguyên vẹn
        } else if (hits == 2) {
            imageToDraw = brickImages[1]; // Chạm 1 lần
        } else if (hits == 1) {
            imageToDraw = brickImages[0]; // Chạm 2 lần
        }

        if (imageToDraw != null) {
            gc.drawImage(imageToDraw, x, y, width, height);
        } else {
            // Dự phòng
            gc.setFill(Color.DARKORANGE);
            gc.fillRect(x, y, width, height);
        }
    }
}

