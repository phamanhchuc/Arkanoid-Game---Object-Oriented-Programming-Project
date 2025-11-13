package com.example.arkanoid;

import java.util.Random;

// Dựa theo mẫu PowerUp Factory
public class PowerUpFactory {

    private static final Random random = new Random();

    /**
     * Tạo một PowerUp ngẫu nhiên dựa trên tỷ lệ bạn đã định nghĩa.
     */
    public static PowerUp createRandomPowerUp(double x, double y, double width, double height) {
        double dropChance = random.nextDouble();

        // Đây là logic từ hàm trySpawnPowerUp của bạn
        if (dropChance < 0.04) { // life
            return new PowerUp(x, y, width, height, PowerUp.PowerUpType.LIFE);
        } else if (dropChance < 0.5) { // lose life
            return new PowerUp(x, y, width, height, PowerUp.PowerUpType.LOSE_LIFE);
        } else if (dropChance < 0.65) { // cross bow
            return new PowerUp(x, y, width, height, PowerUp.PowerUpType.CROSS_BOW);
        } else if (dropChance < 0.8) { // multi ball
            return new PowerUp(x, y, width, height, PowerUp.PowerUpType.MULTI_BALL);
        } else if (dropChance < 0.97) { // meo meo
            return new PowerUp(x, y, width, height, PowerUp.PowerUpType.PIERCING_SHOT);
        }

        return null; // Không rơi gì cả
    }
}