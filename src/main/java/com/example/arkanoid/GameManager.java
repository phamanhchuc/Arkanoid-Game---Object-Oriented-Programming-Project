

import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameManager {
    private int width, height;
    private Paddle paddle;
    private Ball ball;
    private List<Brick> bricks = new ArrayList<>();
    private int score = 0;
    private int lives = 3;
    private boolean running = false;

    public GameManager(int width, int height){
        this.width = width; this.height = height;
        initGame();
    }

    public void initGame(){
        paddle = new Paddle(width/2 - 60, height - 40, 120, 20, width);
        ball = new Ball(paddle.getX() + paddle.getWidth()/2 - 8, paddle.getY() - 16, 8, width, height);
        ball.stickTo(paddle);
        createBricks();
        score = 0; lives = 3; running = false;
    }

    private void createBricks(){
        bricks.clear();
        int rows = 5, cols = 10;
        double brickW = (width - 100.0) / cols;
        double brickH = 22;
        for (int r=0;r<rows;r++){
            for (int c=0;c<cols;c++){
                double x = 50 + c * (brickW + 5);
                double y = 60 + r * (brickH + 6);
                bricks.add(new Brick(x,y,brickW,brickH, (r%2==0)?1:2));
            }
        }
    }

    public void startGame(){
        if (!running){
            running = true;
            ball.launch();
        }
    }

    public boolean isRunning(){ return running; }
    public Paddle getPaddle(){ return paddle; }

    public void update(double dt){
        if (!running) {
            // keep paddle and ball synced if stuck
            paddle.update(dt);
            ball.stickTo(paddle);
            return;
        }
        paddle.update(dt);
        ball.update(dt);

        // ball out of bounds -> lose life
        if (ball.isOutOfBounds()){
            lives--;
            running = false;
            if (lives <= 0){
                // game over: reset
                initGame();
                return;
            } else {
                ball.stickTo(paddle);
            }
        }

        // collision: ball <-> paddle
        if (checkCollision(ball, paddle) && ball.getY() + ball.getHeight() <= paddle.getY() + 30){
            ball.bounceOffPaddle(paddle);
        }

        // collision: ball <-> bricks
        for (Iterator<Brick> it = bricks.iterator(); it.hasNext();){
            Brick b = it.next();
            if (b.isDestroyed()) continue;
            if (checkCollision(ball, b)){
                boolean destroyed = b.takeHit();
                // simple reflect Y
                ball.dy = -ball.dy;
                if (destroyed) {
                    score += 100;
                } else {
                    score += 50;
                }
            }
        }

        // optional: remove destroyed bricks (render will skip them)
    }

    private boolean checkCollision(GameObject a, GameObject b){
        return a.getX() < b.getX() + b.getWidth() &&
                a.getX() + a.getWidth() > b.getX() &&
                a.getY() < b.getY() + b.getHeight() &&
                a.getY() + a.getHeight() > b.getY();
    }

    public void render(GraphicsContext gc){
        // draw HUD
        gc.setFill(javafx.scene.paint.Color.WHITE);
        gc.fillText("Score: " + score + "  Lives: " + lives, 10, 20);

        paddle.render(gc);
        ball.render(gc);
        for (Brick b : bricks) b.render(gc);
    }
}
