package com.example.arkanoid;

// Dựa theo mẫu Concrete Factory
public class IndestructibleBrickFactory extends BrickFactory {

    @Override
    public Brick createBrick(double x, double y, double width, double height) {
        // Loại 4 trong code Brick.java của bạn là gạch không thể phá hủy
        return new Brick(x, y, width, height, 4);
    }
}