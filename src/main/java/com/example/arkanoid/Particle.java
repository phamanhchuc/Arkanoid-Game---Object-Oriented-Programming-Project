package com.example.arkanoid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Particle extends MovableObject {
    private double lifespan = 0.5; // seconds
    private double timeAlive = 0;
    private Color color;

    public Particle(double x, double y, double dx, double dy, Color color) {
        super(x, y, 2, 2); // Small size
        this.dx = dx;
        this.dy = dy;
        this.color = color;
    }

    @Override
    public void update(double dt) {
        move(dt);
        timeAlive += dt;
    }

    public boolean isExpired() {
        return timeAlive > lifespan;
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.setFill(color);
        gc.fillRect(x, y, width, height);
    }
}