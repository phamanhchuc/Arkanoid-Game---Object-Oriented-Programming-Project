package com.example.arkanoid;

/**
 * Lớp tĩnh đơn giản để giữ dữ liệu được chia sẻ giữa các màn hình (Scene).
 * Dùng để lưu trữ dữ liệu chung, chẳng hạn như tên người chơi.
 */
public class GameData {
    // Biến static có thể được truy cập từ bất kỳ đâu trong chương trình
    // mà không cần tạo đối tượng của lớp GameData.
    public static String playerName;
}

