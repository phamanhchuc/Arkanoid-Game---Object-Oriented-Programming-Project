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
                    double speed = Math.sqrt(prevDx * prevDx + prevDy * prevDy);
                    if (speed > 0) {
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
                        } else {
                            manager.addScore(50); // Ghi điểm 2 hit (25 + 25)
                        }
                        break;

                    default:
                    case NORMAL:
                        SoundManager.playSound(SoundManager.Sound.HIT_BRICK);

                        // 🟢 CẬP NHẬT LOGIC PHÁ HỦY GẠCH
                        if (brick.takeHit()) {

                            // 🟢 THÊM KIỂM TRA CHO HEARTBRICK
                            if (brick instanceof HeartBrick) {
                                // Nếu là HeartBrick:
                                // 1. Logic setHeartDestroyed(true) đã nằm trong HeartBrick.takeHit()
                                // 2. Chỉ cần thêm điểm và KHÔNG nhả PowerUp
                                manager.addScore(500); // Thêm điểm cho HeartBrick
                            } else {
                                // Nếu là gạch thường/đặc biệt
                                manager.addScore(100);
                                manager.spawnPowerUpFromBrick(brick);
                            }
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
        } else if (object instanceof BossLevel2) { // <--- THÊM KHỐI NÀY
            BossLevel2 boss = (BossLevel2) object;

            // Xử lý vật lý va chạm: Đẩy bóng ra và đảo hướng.
            // Logic này tương tự như va chạm với Brick hoặc Paddle (đảo hướng)

            // 1. Tính toán vị trí tâm bóng và góc va chạm
            double ballCenterX = ball.getX() + ball.getWidth() / 2;
            double ballCenterY = ball.getY() + ball.getHeight() / 2;

            // 2. Xác định cạnh va chạm để đảo dx hoặc dy

            // Tìm điểm gần nhất trên Boss tới tâm bóng
            double closestX = Math.max(boss.getX(), Math.min(ballCenterX, boss.getX() + boss.getWidth()));
            double closestY = Math.max(boss.getY(), Math.min(ballCenterY, boss.getY() + boss.getHeight()));

            double distX = ballCenterX - closestX;
            double distY = ballCenterY - closestY;

            // Chỉ nảy nếu khoảng cách thực sự nhỏ hơn bán kính (va chạm chắc chắn)
            if ((distX * distX) + (distY * distY) < (ball.getWidth() / 2 * ball.getWidth() / 2)) {

                // Xử lý bật ra (đảo chiều vận tốc)
                // Nếu va chạm ngang trội hơn (cạnh trái/phải)
                if (Math.abs(distX) > Math.abs(distY)) {
                    ball.setDx(-ball.getDx());
                } else {
                    // Nếu va chạm dọc trội hơn (cạnh trên/dưới)
                    ball.setDy(-ball.getDy());
                }

                // Tùy chọn: Đẩy bóng ra khỏi Boss để tránh bị kẹt (giống logic Brick)
                double distance = Math.sqrt((distX * distX) + (distY * distY));
                double overlap = (ball.getWidth() / 2) - distance;

                if (distance > 0) {
                    double pushX = (distX / distance) * overlap;
                    double pushY = (distY / distance) * overlap;
                    ball.setX(ball.getX() + pushX);
                    ball.setY(ball.getY() + pushY);
                }

                // Chỉ xử lý nếu Boss còn sống (để tránh lỗi)
                if (boss.getHp() > 0) {
                    int damage = 1;
                    // Tăng sát thương nếu bóng đang ở trạng thái LỬA
                    if (ball.getState() == Ball.BallState.FIRE) {
                        damage = 2;
                    }

                    if (!boss.takeDamage(damage)) {
                        // Boss bị tiêu diệt! (Logic này phải gọi qua GameManager)
                        manager.addScore(5000);
                        manager.setLevelWon(true);
                        manager.setRunning(false);

                        // Cần một cơ chế để GameManager biết phải xóa Boss (vì Boss bị hủy trong Strategy)
                        // Cách đơn giản nhất: Boss tự báo cho GameManager để xóa nó,
                        // nhưng vì bạn đã có GameManagerHolder, chúng ta sẽ dùng nó.

                        // Sử dụng GameManagerHolder để gọi hàm xử lý Boss chết
                        if (BossLevel2.GameManagerHolder.INSTANCE != null) {
                            // GIẢ ĐỊNH: Chúng ta sẽ tạo một hàm mới trong GameManager để xử lý việc này.
                            BossLevel2.GameManagerHolder.INSTANCE.handleBossDefeated(boss);
                        }
                    }
                }

                SoundManager.playSound(SoundManager.Sound.HIT_WALL); // Dùng âm thanh nảy tường

                // LƯU Ý: Không gây sát thương Boss ở đây!
                // Logic boss.takeDamage() đã được xử lý trong GameManager.update()
            }
        } else if (object instanceof BossLevel3) { // <--- THÊM KHỐI NÀY
            BossLevel3 boss = (BossLevel3) object;

            // Xử lý vật lý va chạm: Đẩy bóng ra và đảo hướng.
            double ballCenterX = ball.getX() + ball.getWidth() / 2;
            double ballCenterY = ball.getY() + ball.getHeight() / 2;

            double closestX = Math.max(boss.getX(), Math.min(ballCenterX, boss.getX() + boss.getWidth()));
            double closestY = Math.max(boss.getY(), Math.min(ballCenterY, boss.getY() + boss.getHeight()));

            double distX = ballCenterX - closestX;
            double distY = ballCenterY - closestY;

            if ((distX * distX) + (distY * distY) < (ball.getWidth() / 2 * ball.getWidth() / 2)) {

                if (Math.abs(distX) > Math.abs(distY)) {
                    ball.setDx(-ball.getDx());
                } else {
                    ball.setDy(-ball.getDy());
                }

                double distance = Math.sqrt((distX * distX) + (distY * distY));
                double overlap = (ball.getWidth() / 2) - distance;

                if (distance > 0) {
                    double pushX = (distX / distance) * overlap;
                    double pushY = (distY / distance) * overlap;
                    ball.setX(ball.getX() + pushX);
                    ball.setY(ball.getY() + pushY);
                }

                SoundManager.playSound(SoundManager.Sound.HIT_WALL); // Âm thanh va chạm

                // =======================================================
                // ===== LOGIC TRỪ HP VÀ XỬ LÝ BOSS BỊ PHÁ HỦY (CHỈ KHI TIM VỠ) =====
                // =======================================================
                if (boss.getHp() > 0 && boss.isHeartDestroyed()) { // <-- CHỈ TRỪ HP KHI TRÁI TIM BỊ PHÁ HỦY
                    int damage = 1;
                    if (ball.getState() == Ball.BallState.FIRE) {
                        damage = 2;
                    }

                    if (!boss.takeDamage(damage)) {
                        manager.addScore(10000); // Thưởng lớn khi hạ Boss cuối
                        manager.setLevelWon(true);
                        manager.setRunning(false);

                        if (BossLevel3.GameManagerHolder.INSTANCE != null) {
                            // GIẢ ĐỊNH: Bạn có thể tái sử dụng handleBossDefeated
                            // Hoặc tạo hàm mới: manager.handleBoss3Defeated(boss);
                            // Để đơn giản, ta sẽ chỉ xóa tham chiếu boss từ GameManager
                            manager.setCurrentBoss(null);
                        }
                    }
                }
            }
        }
    }
}