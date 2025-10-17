

public abstract class MovableObject extends GameObject {
    protected double dx = 0, dy = 0;
    public MovableObject(double x, double y, double w, double h){
        super(x,y,w,h);
    }
    public void move(double dt){
        x += dx * dt;
        y += dy * dt;
    }
}
