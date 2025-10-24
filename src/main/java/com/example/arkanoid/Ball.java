package com.example.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Ball extends MovableObject {
    private double speed = 550;
    private double radius; // Bán kính va chạm (sẽ là 12)
    private int sceneW, sceneH;
    private boolean stuck = true;
    private double playAreaOffsetX;
    private double playAreaWidth;

    // --- BIẾN CHO ANIMATION ---
    private static final int NUM_FRAMES = 5;
    private static final double FRAME_DURATION_SECONDS = 0.08;
    private int currentFrame = 0;
    private double animationTime = 0;

    // Kích thước của 1 frame trong ảnh (dùng để cắt ảnh)
    private int frameWidth;
    private int frameHeight;

    public void setPlayArea(double offsetX, double width) {
        this.playAreaOffsetX = offsetX;
        this.playAreaWidth = width;
    }

    // --- HÀM KHỞI TẠO ĐÃ ĐƯỢC CHỈNH SỬA ---
    public Ball(double x, double y, double r, int sceneW, int sceneH) {
        // 1. Ưu tiên kích thước 'r' (radius=12) từ GameManager
        // 'r * 2' sẽ là width và height (ví dụ: 24x24)
        super(x - r, y - r, r * 2, r * 2);

        this.sceneW = sceneW;
        this.sceneH = sceneH;
        dx = 0;
        dy = 0;

        // 2. Đặt bán kính va chạm bằng 'r'
        this.radius = r;

        // Tải hình ảnh của quả bóng (spritesheet)
        try {
            String imagePath = "/com/example/arkanoid/images/ball_spritesheet.png";
            image = new Image(getClass().getResourceAsStream(imagePath));

            if (image == null) {
                System.err.println("LỖI NGHIÊM TRỌNG: Không tìm thấy file ảnh: " + imagePath);
            } else {
                // 3. Tính kích thước frame NGUỒN (để cắt ảnh)
                // Chúng ta sẽ KHÔNG dùng kích thước này cho va chạm
                this.frameWidth = (int) (image.getWidth() / NUM_FRAMES);
                this.frameHeight = (int) image.getHeight();
            }

        } catch (Exception e) {
            System.err.println("Lỗi: Không thể tải ảnh cho Ball spritesheet.");
            e.printStackTrace();
        }
    }

    /**
     * Phương thức render sẽ tự động co giãn ảnh frame to (sw, sh)
     * vào đúng kích thước nhỏ (dw, dh) đã định nghĩa (vd: 24x24)
     */
    @Override
    public void render(GraphicsContext gc) {
        if (image != null && !image.isError()) {
            // Kích thước NGUỒN (cắt từ spritesheet)
            double sx = currentFrame * frameWidth;
            double sy = 0;
            double sw = frameWidth;                // Rộng (to)
            double sh = frameHeight;               // Cao (to)

            // Vị trí ĐÍCH (vẽ lên canvas)
            double dx = x;
            double dy = y;
            double dw = width;  // Rộng (nhỏ - vd: 24)
            double dh = height; // Cao (nhỏ - vd: 24)

            // Vẽ frame (sw, sh) vào khu vực (dw, dh) -> ảnh tự co lại
            gc.drawImage(image, sx, sy, sw, sh, dx, dy, dw, dh);

        } else {
            gc.setFill(javafx.scene.paint.Color.WHITE);
            gc.fillOval(x, y, width, height);
        }
    }

    public void launch(){
        if (stuck){
            stuck = false;
            dx = speed * Math.cos(Math.toRadians(60));
            dy = -speed * Math.sin(Math.toRadians(60));
        }
    }

    /**
     * Logic dính vào paddle (giờ đã đúng vì dùng this.radius và this.height)
     */
    public void stickTo(Paddle p){
        stuck = true;
        x = p.getX() + p.getWidth()/2 - this.radius;
        y = p.getY() - this.height - 1;
        dx = 0; dy = 0;

        currentFrame = 0;
        animationTime = 0;
    }

    @Override
    public void update(double dt) {
        if (stuck) return;

        move(dt); // Cập nhật vị trí x, y

        // --- CẬP NHẬT ANIMATION ---
        animationTime += dt;
        if (animationTime >= FRAME_DURATION_SECONDS) {
            animationTime -= FRAME_DURATION_SECONDS;
            currentFrame++;

            if (currentFrame >= NUM_FRAMES) {
                currentFrame = 0;
            }
        }
        // --- KẾT THÚC CẬP NHẬT ANIMATION ---

        // Xử lý va chạm tường
        if (x <= playAreaOffsetX) {
            x = playAreaOffsetX;
            dx = -dx;
        }
        if (x + width >= playAreaOffsetX + playAreaWidth) {
            x = playAreaOffsetX + playAreaWidth - width;
            dx = -dx;
        }
        if (y <= 0) {
            y = 0;
            dy = -dy;
        }
    }

    public boolean isOutOfBounds(){ return y > sceneH; }

    // Logic nảy bóng (đã đúng vì dùng this.radius)
    public void bounceOffPaddle(Paddle p){
        double paddleCenter = p.getX() + p.getWidth()/2;
        double ballCenter = x + radius;

        double rel = (ballCenter - paddleCenter) / (p.getWidth()/2);
        rel = Math.max(-1, Math.min(1, rel));

        double angle = rel * Math.toRadians(75);

        double sp = Math.sqrt(dx*dx + dy*dy);
        sp = Math.max(sp, speed);

        dx = sp * Math.sin(angle);
        dy = -Math.abs(sp * Math.cos(angle));
    }
}