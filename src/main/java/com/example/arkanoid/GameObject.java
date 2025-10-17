

public abstract class GameObject {
    protected double x, y, width, height;
    public GameObject(double x, double y, double w, double h){
        this.x = x; this.y = y; this.width = w; this.height = h;
    }
    public double getX(){ return x; }
    public double getY(){ return y; }
    public double getWidth(){ return width; }
    public double getHeight(){ return height; }

    public abstract void update(double dt);
    public abstract void render(javafx.scene.canvas.GraphicsContext gc);
}
