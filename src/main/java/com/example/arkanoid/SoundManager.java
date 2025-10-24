package com.example.arkanoid;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.util.HashMap;
import java.util.Map;

/**
 * Lớp tĩnh để quản lý và phát âm thanh.
 * Tải trước tất cả âm thanh một lần để tối ưu hiệu suất.
 */
public class SoundManager {

    // Enum để định danh các âm thanh
    public enum Sound {
        HIT_BRICK,
        HIT_PADDLE,
        HIT_WALL,
        COLLECT_POWERUP, // Tương ứng với Game_coin_collect
        LEVEL_COMPLETED,
        MISSED_BALL,
        GAME_OVER
    }

    // Map để lưu các đối tượng Media đã tải
    private static Map<Sound, Media> sounds = new HashMap<>();

    // Biến static này sẽ được thực thi 1 LẦN khi lớp được tải
    static {
        try {
            // Tải tất cả các file âm thanh dựa trên tên file bạn cung cấp
            sounds.put(Sound.HIT_BRICK, loadSound("ball_hits_bricks.mp3"));
            sounds.put(Sound.HIT_PADDLE, loadSound("ball_hits_paddle.mp3"));
            sounds.put(Sound.HIT_WALL, loadSound("ball_hits_wall.mp3"));
            sounds.put(Sound.COLLECT_POWERUP, loadSound("Game_coin_collect.mp3"));
            sounds.put(Sound.LEVEL_COMPLETED, loadSound("level-completed.mp3"));
            sounds.put(Sound.MISSED_BALL, loadSound("missed_ball.mp3"));
            sounds.put(Sound.GAME_OVER, loadSound("game_over.mp3"));

            System.out.println("SoundManager: Đã tải thành công " + sounds.size() + " file âm thanh.");

        } catch (Exception e) {
            System.err.println("SoundManager: Lỗi nghiêm trọng khi tải file âm thanh!");
            e.printStackTrace();
        }
    }

    /**
     * Phương thức nội bộ để tải file âm thanh từ thư mục resources/sounds
     */
    private static Media loadSound(String fileName) {
        String path = "/com/example/arkanoid/sounds/" + fileName;
        try {
            String soundUrl = SoundManager.class.getResource(path).toExternalForm();
            return new Media(soundUrl);
        } catch (NullPointerException e) {
            System.err.println("Lỗi: Không tìm thấy file âm thanh: " + path);
            // Trả về null để chương trình không bị crash
            return null;
        }
    }

    /**
     * Phương thức công khai để phát âm thanh
     */
    public static void playSound(Sound sound) {
        Media media = sounds.get(sound);

        // Nếu âm thanh không tồn tại (do tải lỗi) thì không làm gì cả
        if (media == null) {
            System.err.println("SoundManager: Bỏ qua phát âm thanh " + sound + " (chưa được tải).");
            return;
        }

        try {
            // Tạo một MediaPlayer MỚI mỗi lần phát
            // Điều này cho phép nhiều âm thanh (như tiếng nảy) phát chồng lên nhau
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}