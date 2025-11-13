package com.example.arkanoid.entities;

public abstract class MovableObject extends GameObject {
    protected double dx = 0, dy = 0;
    protected double playAreaX = 0;
    protected double playAreaWidth = 0;

    public void setPlayArea(double playAreaX, double playAreaWidth) {
        this.playAreaX = playAreaX;
        this.playAreaWidth = playAreaWidth;
    }

    public MovableObject(double x, double y, double w, double h) {
        super(x, y, w, h);
    }

    public void move(double dt) {
        x += dx * dt;
        y += dy * dt;
    }

    public void setDx(double dx) {
        this.dx = dx;
    }

    public void setDy(double dy) {
        this.dy = dy;
    }

    public double getDx() {
        return dx;
    }

    public double getDy() {
        return dy;
    }
}