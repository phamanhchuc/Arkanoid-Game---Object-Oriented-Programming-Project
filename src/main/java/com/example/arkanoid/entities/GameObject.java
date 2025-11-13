package com.example.arkanoid.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public abstract class GameObject {
    protected double x, y, width, height;
    protected Image image; // Biến image đã được chuyển lên lớp cha

    public GameObject(double x, double y, double w, double h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    // Các phương thức Setter được thêm vào để xử lý va chạm
    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public abstract void update(double dt);

    public abstract void render(GraphicsContext gc);
}

