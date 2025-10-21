package com.example.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Brick extends GameObject {
    private int hits;
    private boolean destroyed = false;

    // --- PHẦN MỚI: Tải các ảnh một lần duy nhất cho gạch 2-hit ---
    private static final Image[] brickImages = new Image[2];

    // Khối static này sẽ chạy một lần duy nhất khi lớp Brick được sử dụng lần đầu
    static {
        try {
            // Tải ảnh cho gạch 2-hit
            // Trạng thái khi còn 2 máu (nguyên vẹn)
            brickImages[1] = new Image(Brick.class.getResourceAsStream("/com/example/arkanoid/images/brick1.1.png"));
            // Trạng thái khi còn 1 máu (chạm lần 1)
            brickImages[0] = new Image(Brick.class.getResourceAsStream("/com/example/arkanoid/images/brick1.1_cham 1.png"));
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

        // --- Logic vẽ được đơn giản hóa ---
        if (hits == 2) {
            imageToDraw = brickImages[1]; // Ảnh nguyên vẹn
        } else if (hits == 1) {
            imageToDraw = brickImages[0]; // Ảnh chạm 1 lần
        }

        // Vẽ ảnh đã chọn
        if (imageToDraw != null) {
            gc.drawImage(imageToDraw, x, y, width, height);
        } else {
            // Dự phòng: Vẽ màu nếu không tải được ảnh
            gc.setFill(Color.DARKORANGE);
            gc.fillRect(x, y, width, height);
            gc.setStroke(Color.BLACK);
            gc.strokeRect(x, y, width, height);
        }
    }
}

