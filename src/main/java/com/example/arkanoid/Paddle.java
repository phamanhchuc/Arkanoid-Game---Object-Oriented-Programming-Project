
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Paddle extends MovableObject {
    private double speed = 500; // pixels/sec
    private double sceneWidth;

    public Paddle(double x, double y, double w, double h, double sceneWidth){
        super(x,y,w,h); this.sceneWidth = sceneWidth;
    }
    public void moveLeft(){ dx = -speed; }
    public void moveRight(){ dx = speed; }
    public void stop(){ dx = 0; }

    @Override
    public void update(double dt){
        move(dt);
        // clamp
        if (x < 0) x = 0;
        if (x + width > sceneWidth) x = sceneWidth - width;
        // friction: stop horizontal movement when keys not pressed
        dx = 0;
    }

    @Override
    public void render(GraphicsContext gc){
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(x, y, width, height);
    }
}
