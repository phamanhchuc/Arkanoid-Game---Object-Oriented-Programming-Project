package com.example.arkanoid.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Particle extends MovableObject {
    private double lifespan;
    private double timeAlive = 0;
    private Color color;
    private double startWidth, startHeight;

    // Constructor cho hạt nhỏ
    public Particle(double x, double y, double dx, double dy, Color color) {
        super(x, y, 2, 2);
        this.dx = dx;
        this.dy = dy;
        this.color = color;
        this.lifespan = 0.5;
        this.startWidth = 2;
        this.startHeight = 2;
    }

    // Constructor cho hiệu ứng đuôi (Trail)
    public Particle(double x, double y, double w, double h, Color color, double lifespan) {
        super(x - w / 2, y - h / 2, w, h);
        this.dx = 0;
        this.dy = 0;
        this.color = color;
        this.lifespan = lifespan;
        this.startWidth = w;
        this.startHeight = h;
    }

    @Override
    public void update(double dt) {
        move(dt);
        timeAlive += dt;

        // Co lại khi già đi
        double lifeRatio = timeAlive / lifespan;
        if (lifeRatio > 1) lifeRatio = 1;

        this.width = startWidth * (1.0 - lifeRatio);
        this.height = startHeight * (1.0 - lifeRatio);

        // Căn lại tâm
        this.x = (this.x + this.startWidth / 2) - this.width / 2;
        this.y = (this.y + this.startHeight / 2) - this.height / 2;

        // Cập nhật kích thước gốc tạm thời cho frame sau
        this.startWidth = this.width;
        this.startHeight = this.height;
    }

    public boolean isExpired() {
        return timeAlive > lifespan;
    }

    @Override
    public void render(GraphicsContext gc) {
        // --- TỐI ƯU HÓA RENDER ---

        // Tính độ mờ (opacity) từ 1.0 -> 0.0
        double opacity = 1.0 - (timeAlive / lifespan);
        if (opacity < 0) opacity = 0;
        if (opacity > 1) opacity = 1;

        // Thay vì dùng gc.setGlobalAlpha (tốn kém), ta tạo màu mới có alpha tương ứng
        // Color.deriveColor(hueShift, saturationFactor, brightnessFactor, opacityFactor)
        // Dùng opacity trực tiếp làm opacityFactor nếu dùng Color.color,
        // nhưng ở đây ta dùng deriveColor để giữ nguyên màu gốc và chỉ chỉnh Alpha.

        // Cách tối ưu nhất:
        Color renderColor = new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                opacity // Gán alpha trực tiếp
        );

        gc.setFill(renderColor);
        gc.fillOval(x, y, width, height);
    }
}