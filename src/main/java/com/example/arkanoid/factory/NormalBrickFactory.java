package com.example.arkanoid.factory;

import com.example.arkanoid.entities.Brick;

// Dựa theo mẫu Concrete Factory
public class NormalBrickFactory extends BrickFactory {
    private int hits;

    public NormalBrickFactory(int hits) {
        this.hits = hits;
    }

    @Override
    public Brick createBrick(double x, double y, double width, double height) {
        return new Brick(x, y, width, height, this.hits);
    }
}