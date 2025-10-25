package com.example.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Brick extends GameObject {
    private int hits;
    private boolean destroyed = false;
    private boolean indestructible = false; // <-- Biến mới

    // Mảng chứa 3 trạng thái ảnh cho gạch thường
    private static final Image[] brickImages = new Image[3];
    // Ảnh cho gạch không thể phá hủy
    private static Image indestructibleImage; // <-- Biến ảnh mới

    // Khối static để tải tất cả ảnh một lần
    static {
        try {
            // Tải ảnh cho gạch 1, 2, 3 hit
            brickImages[2] = new Image(Brick.class.getResourceAsStream("/com/example/arkanoid/images/brick_state_3.png"));
            brickImages[1] = new Image(Brick.class.getResourceAsStream("/com/example/arkanoid/images/brick_state_2.png"));
            brickImages[0] = new Image(Brick.class.getResourceAsStream("/com/example/arkanoid/images/brick_state_1.png"));

            // Tải ảnh cho gạch không thể phá hủy
            indestructibleImage = new Image(Brick.class.getResourceAsStream("/com/example/arkanoid/images/brickNotdestroy.png")); // <-- Tải ảnh mới

            // Kiểm tra xem có ảnh nào bị thiếu không
            if (brickImages[0] == null || brickImages[1] == null || brickImages[2] == null) {
                System.err.println("Lỗi nghiêm trọng: Thiếu ảnh trạng thái cho gạch thường.");
            }
            if (indestructibleImage == null) {
                System.err.println("Lỗi nghiêm trọng: Không thể tải ảnh cho gạch không thể phá hủy (brickNotdestroy.png).");
            }

        } catch (Exception e) {
            System.err.println("Lỗi nghiêm trọng: Không thể tải được ảnh cho Brick.");
            e.printStackTrace();
        }
    }

    // Constructor được cập nhật
    public Brick(double x, double y, double w, double h, int hitsOrType) {
        super(x, y, w, h);
        if (hitsOrType == 4) { // Nếu là loại 4
            this.indestructible = true; // Đánh dấu là không thể phá hủy
            this.hits = 1; // Đặt hits > 0 để không bị coi là đã phá hủy ban đầu
        } else {
            this.hits = hitsOrType; // Gán số hit như bình thường (1, 2, 3)
            this.indestructible = false;
        }
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    // Thêm hàm getter này
    public boolean isIndestructible() {
        return indestructible;
    }


    // Cập nhật takeHit()
    public boolean takeHit() {
        if (indestructible) {
            return false; // Không thể phá hủy, luôn trả về false
        }
        // Logic cũ cho gạch thường
        hits--;
        if (hits <= 0) {
            destroyed = true;
        }
        return destroyed;
    }

    @Override
    public void update(double dt) { /* Gạch tĩnh không cần update */ }

    // Cập nhật render()
    @Override
    public void render(GraphicsContext gc) {
        if (destroyed) {
            return; // Gạch đã phá hủy thì không vẽ
        }

        Image imageToDraw = null;

        if (indestructible) {
            // Nếu là gạch không thể phá hủy, dùng ảnh riêng
            imageToDraw = indestructibleImage;
        } else {
            // Logic cũ cho gạch 1, 2, 3 hit
            if (hits >= 3) { // Dùng >= 3 để an toàn
                imageToDraw = brickImages[2];
            } else if (hits == 2) {
                imageToDraw = brickImages[1];
            } else if (hits == 1) {
                imageToDraw = brickImages[0];
            }
        }

        // Vẽ ảnh đã chọn (hoặc vẽ màu dự phòng nếu ảnh lỗi)
        if (imageToDraw != null && !imageToDraw.isError()) {
            gc.drawImage(imageToDraw, x, y, width, height);
        } else {
            // Vẽ màu dự phòng khác nhau cho dễ phân biệt
            gc.setFill(indestructible ? Color.DARKGRAY : Color.DARKORANGE);
            gc.fillRect(x, y, width, height);
            if (imageToDraw != null && imageToDraw.getException() != null) {
                System.err.println("Lỗi render ảnh Brick: " + imageToDraw.getException().getMessage());
            } else if (imageToDraw == null && indestructible) {
                System.err.println("Lỗi render: indestructibleImage chưa được tải.");
            } else if (imageToDraw == null && !indestructible) {
                System.err.println("Lỗi render: brickImages chưa được tải đúng.");
            }
        }
    }
}