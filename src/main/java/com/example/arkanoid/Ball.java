package com.example.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Ball extends MovableObject {
    private double speed = 350;
    private double radius;
    private int sceneW, sceneH;
    private boolean stuck = true;
    private double playAreaOffsetX;
    private double playAreaWidth;


    private static final int NUM_FRAMES = 5;
    private static final double FRAME_DURATION_SECONDS = 0.03; // Tốc độ nhanh
    private int currentFrame = 0;
    private double animationTime = 0;

    private int frameWidth;
    private int frameHeight;

    public void setPlayArea(double offsetX, double width) {
        this.playAreaOffsetX = offsetX;
        this.playAreaWidth = width;
    }

    public Ball(double x, double y, double r, int sceneW, int sceneH) {
        super(x - r, y - r, r * 2, r * 2);
        this.sceneW = sceneW;
        this.sceneH = sceneH;
        dx = 0;
        dy = 0;
        this.radius = r;

        try {
            String imagePath = "/com/example/arkanoid/images/ball_spritesheet.png";
            image = new Image(getClass().getResourceAsStream(imagePath));
            if (image == null) {
                System.err.println("LỖI NGHIÊM TRỌNG: Không tìm thấy file ảnh: " + imagePath);
            } else {
                this.frameWidth = (int) (image.getWidth() / NUM_FRAMES);
                this.frameHeight = (int) image.getHeight();
            }
        } catch (Exception e) {
            System.err.println("Lỗi: Không thể tải ảnh cho Ball spritesheet.");
            e.printStackTrace();
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (image != null && !image.isError()) {
            double sx = currentFrame * frameWidth;
            double sy = 0;
            double sw = frameWidth;
            double sh = frameHeight;
            double dx = x;
            double dy = y;
            double dw = width;
            double dh = height;
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

        move(dt);

        animationTime += dt;
        if (animationTime >= FRAME_DURATION_SECONDS) {
            animationTime -= FRAME_DURATION_SECONDS;
            currentFrame++;
            if (currentFrame >= NUM_FRAMES) {
                currentFrame = 0;
            }
        }

        // --- CẬP NHẬT ÂM THANH TƯỜNG ---
        if (x <= playAreaOffsetX) {
            x = playAreaOffsetX;
            dx = -dx;
            SoundManager.playSound(SoundManager.Sound.HIT_WALL); // <-- ÂM THANH
        }
        if (x + width >= playAreaOffsetX + playAreaWidth) {
            x = playAreaOffsetX + playAreaWidth - width;
            dx = -dx;
            SoundManager.playSound(SoundManager.Sound.HIT_WALL); // <-- ÂM THANH
        }
        if (y <= 0) {
            y = 0;
            dy = -dy;
            SoundManager.playSound(SoundManager.Sound.HIT_WALL); // <-- ÂM THANH
        }
        // --- KẾT THÚC CẬP NHẬT ---
    }

    public boolean isOutOfBounds(){ return y > sceneH; }

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