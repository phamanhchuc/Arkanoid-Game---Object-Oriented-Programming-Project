package com.example.arkanoid;

/**
 * Interface cho Mẫu thiết kế Strategy (Chiến lược)
 * Định nghĩa cách một quả bóng sẽ xử lý va chạm.
 *
 */
public interface CollisionStrategy {

    /**
     * Xử lý logic vật lý VÀ logic game khi va chạm.
     * @param ball Quả bóng gây ra va chạm
     * @param object Vật thể bị va chạm (Brick hoặc Paddle)
     * @param manager Quản lý game (để cập nhật điểm/power-up)
     */
    void handleCollision(Ball ball, GameObject object, GameManager manager);
}