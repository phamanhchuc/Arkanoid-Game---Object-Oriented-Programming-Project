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
            Ball.BallState ballState = ball.getState(); // <-- Lấy trạng thái của bóng

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

                // --- BƯỚC 2: XỬ LÝ LOGIC GAME (ĐÃ SỬA) ---

                // Gạch bất tử luôn nảy bóng (không phân biệt trạng thái)
                if (brick.isIndestructible()) {
                    SoundManager.playSound(SoundManager.Sound.HIT_WALL); // Tạm dùng âm thanh nảy tường
                    return; // Không làm gì thêm
                }

                // Xử lý logic dựa trên trạng thái của bóng
                switch (ballState) {
                    case ICE:
                        // Trạng thái BĂNG: Hồi máu cho gạch
                        if (brick.healHit()) {
                            // Hồi máu thành công (gạch chưa đầy máu)
                            manager.addScore(10); // Thưởng 10 điểm
                        }
                        // Tạm dùng âm thanh này cho "hồi máu"
                        SoundManager.playSound(SoundManager.Sound.COLLECT_POWERUP);
                        break;

                    case FIRE:
                        // Trạng thái LỬA: Gây 2 sát thương
                        SoundManager.playSound(SoundManager.Sound.HIT_BRICK);
                        boolean destroyed = brick.takeHit(); // Hit 1

                        if (!destroyed) {
                            // Nếu chưa bị phá hủy, gây thêm Hit 2
                            destroyed = brick.takeHit();
                        }

                        if (destroyed) {
                            manager.addScore(100); // Ghi điểm phá hủy
                            manager.spawnPowerUpFromBrick(brick);
                        } else {
                            manager.addScore(50); // Ghi điểm 2 hit (25 + 25)
                        }
                        break;

                    default:
                    case NORMAL:
                        // Trạng thái BÌNH THƯỜNG (Đây là code cũ của bạn)
                        SoundManager.playSound(SoundManager.Sound.HIT_BRICK);
                        if (brick.takeHit()) {
                            manager.addScore(100);
                            manager.spawnPowerUpFromBrick(brick);
                        } else {
                            manager.addScore(25);
                        }
                        break;
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