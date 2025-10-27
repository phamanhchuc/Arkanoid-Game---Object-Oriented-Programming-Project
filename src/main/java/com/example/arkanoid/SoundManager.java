package com.example.arkanoid;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

public class SoundManager {

    public enum Sound {
        HIT_BRICK, HIT_PADDLE, HIT_WALL, COLLECT_POWERUP,
        LEVEL_COMPLETED, MISSED_BALL, GAME_OVER
    }
    public enum Music { BACKGROUND_GAME }

    private static Map<Sound, Media> sounds = new HashMap<>();
    private static Map<Music, Media> musicTracks = new HashMap<>();
    private static MediaPlayer backgroundMusicPlayer;

    // --- BIẾN MỚI ĐỂ LƯU ÂM LƯỢNG CHUNG ---
    // Giá trị từ 0.0 (tắt tiếng) đến 1.0 (to nhất)
    private static double masterVolume = 1.0;

    static {
        try {
            sounds.put(Sound.HIT_BRICK, loadMedia("ball_hits_bricks.mp3"));
            sounds.put(Sound.HIT_PADDLE, loadMedia("ball_hits_paddle.mp3"));
            sounds.put(Sound.HIT_WALL, loadMedia("ball_hits_wall.mp3"));
            sounds.put(Sound.COLLECT_POWERUP, loadMedia("Game_coin_collect.mp3"));
            sounds.put(Sound.LEVEL_COMPLETED, loadMedia("level-completed.mp3"));
            sounds.put(Sound.MISSED_BALL, loadMedia("missed_ball.mp3"));
            sounds.put(Sound.GAME_OVER, loadMedia("game_over.mp3"));
            musicTracks.put(Music.BACKGROUND_GAME, loadMedia("man1.mp3"));
            System.out.println("SoundManager: Đã tải " + sounds.size() + " SFX và " + musicTracks.size() + " bản nhạc.");
        } catch (Exception e) {
            System.err.println("SoundManager: Lỗi nghiêm trọng khi tải file âm thanh/nhạc!");
            e.printStackTrace();
        }
    }

    private static Media loadMedia(String fileName) {
        String path = "/com/example/arkanoid/sounds/" + fileName;
        try {
            String mediaUrl = SoundManager.class.getResource(path).toExternalForm();
            return new Media(mediaUrl);
        } catch (Exception e) { // Bắt Exception chung cho an toàn
            System.err.println("Lỗi khi tải media: " + path);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Cập nhật để áp dụng masterVolume
     */
    public static void playSound(Sound sound) {
        Media media = sounds.get(sound);
        if (media == null) return;
        try {
            MediaPlayer sfxPlayer = new MediaPlayer(media);
            sfxPlayer.setVolume(masterVolume); // <-- Áp dụng âm lượng
            sfxPlayer.play();
        } catch (Exception e) {
            System.err.println("Lỗi khi phát SFX: " + sound); e.printStackTrace();
        }
    }

    /**
     * Cập nhật để áp dụng masterVolume
     */
    public static void playMusic(Music music) {
        stopMusic();
        Media media = musicTracks.get(music);
        if (media == null) return;
        try {
            backgroundMusicPlayer = new MediaPlayer(media);
            backgroundMusicPlayer.setVolume(masterVolume); // <-- Áp dụng âm lượng
            backgroundMusicPlayer.setOnEndOfMedia(() -> {
                backgroundMusicPlayer.seek(Duration.ZERO);
                backgroundMusicPlayer.play();
            });
            backgroundMusicPlayer.play();
            System.out.println("SoundManager: Bắt đầu phát nhạc " + music);
        } catch (Exception e) {
            System.err.println("Lỗi khi phát nhạc: " + music); e.printStackTrace();
            backgroundMusicPlayer = null;
        }
    }

    public static void stopMusic() {
        if (backgroundMusicPlayer != null) {
            try {
                backgroundMusicPlayer.stop(); backgroundMusicPlayer = null;
                System.out.println("SoundManager: Đã dừng nhạc nền.");
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    // --- PHƯƠNG THỨC MỚI ĐỂ SET ÂM LƯỢNG ---
    /**
     * Đặt âm lượng chung cho cả SFX và Music.
     * @param volume Giá trị từ 0.0 đến 1.0
     */
    public static void setMasterVolume(double volume) {
        // Giới hạn giá trị volume trong khoảng 0.0 - 1.0
        masterVolume = Math.max(0.0, Math.min(1.0, volume));
        System.out.println("SoundManager: Đặt âm lượng thành " + masterVolume);

        // Cập nhật âm lượng cho nhạc nền đang phát (nếu có)
        if (backgroundMusicPlayer != null) {
            try {
                backgroundMusicPlayer.setVolume(masterVolume);
            } catch (Exception e) {
                e.printStackTrace(); // Có thể xảy ra lỗi nếu player đang ở trạng thái không hợp lệ
            }
        }
    }

    // --- PHƯƠNG THỨC MỚI ĐỂ LẤY ÂM LƯỢNG HIỆN TẠI ---
    /**
     * Lấy giá trị âm lượng chung hiện tại.
     * @return Âm lượng (0.0 đến 1.0)
     */
    public static double getMasterVolume() {
        return masterVolume;
    }
}