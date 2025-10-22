package com.example.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image; // Thêm import

public class Ball extends MovableObject {
    private double speed = 550;
    private double radius;
    private int sceneW, sceneH;
    private boolean stuck = true;
    private double playAreaOffsetX;
    private double playAreaWidth;

    // Thêm phương thức này vào lớp Ball
    public void setPlayArea(double offsetX, double width) {
        this.playAreaOffsetX = offsetX;
        this.playAreaWidth = width;
    }

    public Ball(double x, double y, double r, int sceneW, int sceneH) {
        super(x - r, y - r, r * 2, r * 2);
        this.radius = r;
        this.sceneW = sceneW;
        this.sceneH = sceneH;
        dx = 0;
        dy = 0;
        // Tải hình ảnh của quả bóng
        try {
            // Đảm bảo đường dẫn này chính xác!
            image = new Image(getClass().getResourceAsStream("/com/example/arkanoid/images/Ball1.png"));
        } catch (Exception e) {
            System.err.println("Lỗi: Không thể tải ảnh cho Ball.");
            e.printStackTrace();
        }
    }

    // ... (các phương thức khác giữ nguyên)

    @Override
    public void render(GraphicsContext gc) {
        // Vẽ hình ảnh thay vì hình tròn
        if (image != null) {
            gc.drawImage(image, x, y, width, height);
        } else {
            // Dự phòng: Vẽ hình tròn màu trắng nếu không tải được ảnh
            gc.setFill(javafx.scene.paint.Color.WHITE);
            gc.fillOval(x, y, width, height);
        }
    }

    // ... (phần còn lại của lớp giữ nguyên)
    public void launch(){
        if (stuck){
            stuck = false;
            dx = speed * Math.cos(Math.toRadians(60));
            dy = -speed * Math.sin(Math.toRadians(60));
        }
    }
    public void stickTo(Paddle p){
        stuck = true;
        x = p.getX() + p.getWidth()/2 - radius;
        y = p.getY() - radius*2 - 1;
        dx = 0; dy = 0;
    }

    @Override
    public void update(double dt) {
        if (stuck) return;
        move(dt);
        // Va chạm tường trong khu vực chơi
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

    public void bounceOffPaddle(Paddle p){
        double paddleCenter = p.getX() + p.getWidth()/2;
        double ballCenter = x + radius;
        double rel = (ballCenter - paddleCenter) / (p.getWidth()/2); // -1..1
        double angle = rel * Math.toRadians(75); // max angle
        double sp = Math.sqrt(dx*dx + dy*dy);
        sp = Math.max(sp, speed);
        dx = sp * Math.sin(angle);
        dy = -Math.abs(sp * Math.cos(angle));
    }
}
