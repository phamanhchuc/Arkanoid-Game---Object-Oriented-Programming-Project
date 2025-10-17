package com.example.arkanoid;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Brick extends GameObject {
    private int hits;
    private boolean destroyed = false;

    public Brick(double x, double y, double w, double h, int hits){
        super(x,y,w,h);
        this.hits = hits;
    }

    public boolean isDestroyed(){ return destroyed; }
    public boolean takeHit(){
        hits--;
        if (hits <= 0) destroyed = true;
        return destroyed;
    }

    @Override
    public void update(double dt){ /* no-op for static brick */ }

    @Override
    public void render(GraphicsContext gc){
        if (destroyed) return;
        if (hits == 1) gc.setFill(Color.DARKRED);
        else gc.setFill(Color.DARKORANGE);
        gc.fillRect(x, y, width, height);
        gc.setStroke(Color.BLACK);
        gc.strokeRect(x, y, width, height);
    }
}