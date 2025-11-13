package com.example.arkanoid.data;

import java.io.Serializable;

/**
 * Lớp đại diện cho một mục điểm trong bảng xếp hạng.
 * Phải implements Serializable để có thể lưu xuống file.
 */
public class ScoreEntry implements Serializable, Comparable<ScoreEntry> {
    private static final long serialVersionUID = 1L; // Cần cho Serializable

    private String playerName;
    private int score;

    public ScoreEntry(String playerName, int score) {
        this.playerName = playerName;
        this.score = score;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getScore() {
        return score;
    }

    // Ghi đè phương thức compareTo để sắp xếp điểm từ cao xuống thấp
    @Override
    public int compareTo(ScoreEntry other) {
        // Trả về số âm nếu điểm hiện tại cao hơn, số dương nếu thấp hơn
        return Integer.compare(other.score, this.score);
    }

    // Ghi đè toString để hiển thị đẹp hơn
    @Override
    public String toString() {
        return playerName + ": " + score;
    }
}
