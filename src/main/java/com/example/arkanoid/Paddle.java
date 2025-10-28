package com.example.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Paddle extends MovableObject {
    private double speed = 500;
    private double sceneWidth;
    private double playAreaOffsetX;
    private double playAreaWidth;

    private Image originalImage;
    private Image crossBowImage;

    // --- Biến lưu trạng thái gốc ---
    private double originalWidth;
    private double originalHeight;
    private double originalY; // <-- THÊM MỚI: Lưu Y gốc

    // --- Cấu hình cho Paddle Nỏ ---

    // 1. Kích thước
    private final double CROSS_BOW_WIDTH = 120.0;
    private final double CROSS_BOW_HEIGHT = 70.0;
    //  Độ cao crossbow

    private final double CROSS_BOW_Y_OFFSET = -40.0; //  Dịch n pixels



    public void setPlayArea(double offsetX, double width) {
        this.playAreaOffsetX = offsetX;
        this.playAreaWidth = width;
    }

    public Paddle(double x, double y, double w, double h, double sceneWidth) {
        super(x, y, w, h);

        // --- SỬA ĐỔI: Lưu trạng thái gốc ---
        this.originalWidth = w;
        this.originalHeight = h;
        this.originalY = y;

        this.sceneWidth = sceneWidth;
        try {
            originalImage = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/Paddle1.png"));
            this.image = originalImage;

            if (originalImage == null || originalImage.isError()) {
                System.err.println("LỖI NGHIÊM TRỌNG: Không tìm thấy file ảnh Paddle1.png");
            }

            try {
                crossBowImage = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/cbpaddle.png"));
                if (crossBowImage == null || crossBowImage.isError()) {
                    System.err.println("LỖI: Không tìm thấy file ảnh cbpaddle.png");
                    crossBowImage = null;
                }
            } catch (Exception e) {
                System.err.println("Lỗi: Không thể tải ảnh cho CrossBow Paddle.");
                e.printStackTrace();
            }

        } catch (Exception e) {
            System.err.println("Lỗi: Không thể tải ảnh cho Paddle.");
            e.printStackTrace();
        }
    }

    /**
     * Kích hoạt hoặc hủy kích hoạt trạng thái nỏ.
     * Cập nhật ảnh, kích thước VÀ VỊ TRÍ Y.
     */
    public void setCrossBowActive(boolean active) {
        double oldWidth = this.width; // Lưu lại chiều rộng cũ

        if (active && crossBowImage != null) {
            this.image = crossBowImage;
            // Áp dụng kích thước MỚI
            this.width = CROSS_BOW_WIDTH;
            this.height = CROSS_BOW_HEIGHT;

            this.y = originalY + CROSS_BOW_Y_OFFSET;

        } else {
            this.image = originalImage;
            // Trả về kích thước GỐC
            this.width = originalWidth;
            this.height = originalHeight;

            this.y = originalY;

        }

        // --- Logic giữ paddle ở giữa (sau khi đổi size) ---
        double widthDifference = this.width - oldWidth;
        this.x = this.x - (widthDifference / 2.0);

        // Kiểm tra lại biên ngay lập tức
        if (x < playAreaOffsetX) {
            x = playAreaOffsetX;
        }
        if (x + width > playAreaOffsetX + playAreaWidth) {
            x = playAreaOffsetX + playAreaWidth - width;
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
        // Vị trí Y (this.y) giờ đã được cố định bởi setCrossBowActive,
        // nên không cần cập nhật Y trong hàm move(dt) nữa.
    }

    @Override
    public void render(GraphicsContext gc) {
        // Phương thức render này đã đúng, nó sẽ vẽ ảnh
        // với 'this.width', 'this.height' và 'this.y' đã được cập nhật
        if (image != null && !image.isError()) {
            gc.drawImage(image, x, y, width, height);
        } else {
            // Dự phòng: Vẽ hình chữ nhật nếu không tải được ảnh
            gc.setFill(javafx.scene.paint.Color.LIGHTGRAY);
            gc.fillRect(x, y, width, height);
        }
    }
}
