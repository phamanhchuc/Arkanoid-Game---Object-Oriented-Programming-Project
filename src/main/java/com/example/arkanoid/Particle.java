package com.example.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Lớp Particle được nâng cấp để hỗ trợ hiệu ứng "đuôi" (trail).
 * Có thể tùy chỉnh kích thước, vòng đời, và tự động mờ dần/co lại.
 */
public class Particle extends MovableObject {
    private double lifespan; // Vòng đời (giây)
    private double timeAlive = 0;
    private Color color;
    private double startWidth, startHeight; // Lưu kích thước ban đầu

    /**
     * Constructor gốc cho các hạt nhỏ (ví dụ: vụn vỡ)
     */
    public Particle(double x, double y, double dx, double dy, Color color) {
        super(x, y, 2, 2); // Kích thước nhỏ 2x2
        this.dx = dx;
        this.dy = dy;
        this.color = color;
        this.lifespan = 0.5; // Vòng đời mặc định ngắn
        this.startWidth = 2;
        this.startHeight = 2;
    }

    /**
     * Constructor MỚI: Dùng cho hiệu ứng đuôi (trail)
     * Cho phép cài đặt kích thước, màu, và vòng đời. Hạt sẽ đứng yên.
     */
    public Particle(double x, double y, double w, double h, Color color, double lifespan) {
        // Đặt hạt tại tâm (x, y)
        super(x - w / 2, y - h / 2, w, h);
        this.dx = 0; // Không di chuyển
        this.dy = 0; // Không di chuyển
        this.color = color;
        this.lifespan = lifespan;
        this.startWidth = w;
        this.startHeight = h;
    }


    @Override
    public void update(double dt) {
        move(dt); // Cập nhật (nếu có dx, dy)
        timeAlive += dt;

        // --- LOGIC MỚI: Co lại khi già đi ---
        // Tính tỷ lệ sống (từ 0.0 đến 1.0)
        double lifeRatio = timeAlive / lifespan;
        if (lifeRatio > 1) lifeRatio = 1;

        // Kích thước mới = kích thước gốc * (1.0 - tỷ lệ sống)
        this.width = startWidth * (1.0 - lifeRatio);
        this.height = startHeight * (1.0 - lifeRatio);

        // Căn lại tâm của hạt khi nó co lại
        // (Vị trí x, y cũ + 1/2 kích thước cũ) - 1/2 kích thước mới
        this.x = (this.x + this.startWidth / 2) - this.width / 2;
        this.y = (this.y + this.startHeight / 2) - this.height / 2;

        // Cập nhật lại kích thước gốc (vì move() đã thay đổi x, y)
        this.startWidth = this.width;
        this.startHeight = this.height;
    }

    public boolean isExpired() {
        return timeAlive > lifespan;
    }

    @Override
    public void render(GraphicsContext gc) {
        // --- LOGIC MỚI: Mờ dần (Fade out) ---
        // Tính độ mờ (alpha)
        // Bắt đầu từ 1.0 (rõ) và giảm về 0.0 (trong suốt)
        double alpha = 1.0 - (timeAlive / lifespan);
        if (alpha < 0) alpha = 0;

        // Tạo màu mới với độ mờ đã tính
        Color fadedColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);

        gc.setFill(fadedColor);
        // Dùng fillOval (hình tròn) để hiệu ứng mềm mại hơn
        gc.fillOval(x, y, width, height);
    }
}