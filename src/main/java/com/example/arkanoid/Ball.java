
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Ball extends MovableObject {
    private double speed = 350;
    private double radius;
    private int sceneW, sceneH;
    private boolean stuck = true; // initially stuck on paddle

    public Ball(double x, double y, double r, int sceneW, int sceneH){
        super(x - r, y - r, r*2, r*2);
        this.radius = r;
        this.sceneW = sceneW;
        this.sceneH = sceneH;
        dx = 0; dy = 0;
    }

    public void launch(){
        if (stuck){
            stuck = false;
            dx = speed * Math.cos(Math.toRadians(60));
            dy = -speed * Math.sin(Math.toRadians(60));
        }
    }
    public void stickTo(Paddle p){
        stuck = true;
        x = p.getX() + p.getWidth()/2 - radius;
        y = p.getY() - radius*2 - 1;
        dx = 0; dy = 0;
    }

    @Override
    public void update(double dt){
        if (stuck) return;
        move(dt);
        // wall collisions
        if (x <= 0) { x = 0; dx = -dx; }
        if (x + width >= sceneW) { x = sceneW - width; dx = -dx; }
        if (y <= 0) { y = 0; dy = -dy; }
        // if fall below bottom, mark off-screen (GameManager handles lives)
    }

    public boolean isOutOfBounds(){ return y > sceneH; }

    public void bounceOffPaddle(Paddle p){
        // simple bounce: reflect Y and add some X depending on hit position
        double paddleCenter = p.getX() + p.getWidth()/2;
        double ballCenter = x + radius;
        double rel = (ballCenter - paddleCenter) / (p.getWidth()/2); // -1..1
        double angle = rel * Math.toRadians(75); // max angle
        double sp = Math.sqrt(dx*dx + dy*dy);
        sp = Math.max(sp, speed);
        dx = sp * Math.sin(angle);
        dy = -Math.abs(sp * Math.cos(angle));
    }

    @Override
    public void render(GraphicsContext gc){
        gc.setFill(Color.WHITE);
        gc.fillOval(x, y, width, height);
    }
}
