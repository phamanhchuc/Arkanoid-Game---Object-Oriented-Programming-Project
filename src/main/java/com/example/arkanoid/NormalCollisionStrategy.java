package com.example.arkanoid;

/**
 * Chiến lược va chạm bình thường: Bóng nảy ra.
 *
 */
public class NormalCollisionStrategy implements CollisionStrategy {

    @Override
    public void handleCollision(Ball ball, GameObject object, GameManager manager) {

        if (object instanceof Brick) {
            Brick brick = (Brick) object;

            // --- BƯỚC 1: XỬ LÝ VẬT LÝ ---
            // Đây là code từ hàm resolveBallBrickCollisionImproved cũ của bạn
            double ballRadius = ball.getWidth() / 2;
            double ballCenterX = ball.getX() + ballRadius;
            double ballCenterY = ball.getY() + ballRadius;

            double closestX = Math.max(brick.getX(), Math.min(ballCenterX, brick.getX() + brick.getWidth()));
            double closestY = Math.max(brick.getY(), Math.min(ballCenterY, brick.getY() + brick.getHeight()));

            double distX = ballCenterX - closestX;
            double distY = ballCenterY - closestY;
            double distanceSquared = (distX * distX) + (distY * distY);

            if (distanceSquared < (ballRadius * ballRadius)) {
                double distance = Math.sqrt(distanceSquared);
                double overlap = (distance > 0) ? ballRadius - distance : ballRadius;

                // Đẩy bóng ra
                if (distance > 0) {
                    double pushX = (distX / distance) * overlap;
                    double pushY = (distY / distance) * overlap;
                    ball.setX(ball.getX() + pushX);
                    ball.setY(ball.getY() + pushY);
                } else {
                    double prevDx = ball.getDx();
                    double prevDy = ball.getDy();
                    double speed = Math.sqrt(prevDx*prevDx + prevDy*prevDy);
                    if(speed > 0) {
                        ball.setX(ball.getX() - (prevDx / speed) * overlap * 0.1);
                        ball.setY(ball.getY() - (prevDy / speed) * overlap * 0.1);
                    } else {
                        ball.setY(ball.getY() - overlap);
                    }
                }

                // Đảo ngược vận tốc
                if (Math.abs(distX) > Math.abs(distY)) {
                    ball.setDx(-ball.getDx());
                } else {
                    ball.setDy(-ball.getDy());
                }

                // --- BƯỚC 2: XỬ LÝ LOGIC GAME ---
                // (Logic này được chuyển từ GameManager.update() vào đây)
                SoundManager.playSound(SoundManager.Sound.HIT_BRICK);
                if (!brick.isIndestructible() && brick.takeHit()) {
                    manager.addScore(100);
                    manager.spawnPowerUpFromBrick(brick); // Gọi hàm mới trong GameManager
                } else if (!brick.isIndestructible()) {
                    manager.addScore(25);
                }
            }

        } else if (object instanceof Paddle) {
            // Xử lý va chạm với Paddle
            Paddle paddle = (Paddle) object;
            ball.bounceOffPaddle(paddle);
            SoundManager.playSound(SoundManager.Sound.HIT_PADDLE);
        }
    }
}