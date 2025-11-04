package com.example.arkanoid;

// Dựa theo mẫu Abstract Factory
public abstract class BrickFactory {

    // Thêm các tham số cần thiết
    public abstract Brick createBrick(double x, double y, double width, double height);
}