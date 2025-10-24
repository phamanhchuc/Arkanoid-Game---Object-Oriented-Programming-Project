package com.example.arkanoid;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lớp quản lý danh sách điểm cao.
 */
public class HighScores {

    private static final String HIGH_SCORES_FILE = "highscores.dat"; // Tên file lưu điểm
    private static final int MAX_SCORES = 5; // Số lượng điểm tối đa cần lưu

    private List<ScoreEntry> scores;

    public HighScores() {
        scores = loadScores(); // Tải điểm khi khởi tạo
    }

    // Phương thức tải danh sách điểm từ file
    @SuppressWarnings("unchecked") // Bỏ qua cảnh báo type cast
    private List<ScoreEntry> loadScores() {
        List<ScoreEntry> loadedScores = new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(HIGH_SCORES_FILE))) {
            Object obj = ois.readObject();
            if (obj instanceof List) {
                loadedScores = (List<ScoreEntry>) obj;
            }
        } catch (FileNotFoundException e) {
            System.out.println("File điểm cao chưa tồn tại, sẽ tạo mới.");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Lỗi khi đọc file điểm cao: " + e.getMessage());
            e.printStackTrace();
        }
        // Đảm bảo danh sách luôn được sắp xếp sau khi tải
        Collections.sort(loadedScores);
        return loadedScores;
    }

    // Phương thức lưu danh sách điểm vào file
    private void saveScores() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(HIGH_SCORES_FILE))) {
            oos.writeObject(scores);
        } catch (IOException e) {
            System.err.println("Lỗi khi lưu file điểm cao: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Thêm một điểm mới vào danh sách, giữ danh sách được sắp xếp và giới hạn ở MAX_SCORES.
     * @param playerName Tên người chơi
     * @param score Điểm số
     * @return true nếu điểm được thêm vào top 5, false nếu không.
     */
    public boolean addScore(String playerName, int score) {
        // Chỉ thêm nếu điểm đủ cao hoặc danh sách chưa đầy
        if (scores.size() < MAX_SCORES || score > scores.get(scores.size() - 1).getScore()) {
            scores.add(new ScoreEntry(playerName, score));
            Collections.sort(scores); // Sắp xếp lại

            // Nếu vượt quá giới hạn, xóa điểm thấp nhất
            if (scores.size() > MAX_SCORES) {
                scores.remove(scores.size() - 1);
            }
            saveScores(); // Lưu lại file sau khi thay đổi
            return true;
        }
        return false;
    }

    /**
     * Lấy danh sách top điểm cao (đã được sắp xếp).
     * @return Danh sách ScoreEntry.
     */
    public List<ScoreEntry> getScores() {
        // Trả về bản sao để tránh bị sửa đổi từ bên ngoài
        return new ArrayList<>(scores);
    }
}

