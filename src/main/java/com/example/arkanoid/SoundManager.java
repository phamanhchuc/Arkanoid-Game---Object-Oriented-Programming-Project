package com.example.arkanoid;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration; // <-- THÊM IMPORT NÀY

import java.util.HashMap;
import java.util.Map;

public class SoundManager {

    // Enum cho hiệu ứng âm thanh ngắn
    public enum Sound {
        HIT_BRICK,
        HIT_PADDLE,
        HIT_WALL,
        COLLECT_POWERUP,
        LEVEL_COMPLETED,
        MISSED_BALL,
        GAME_OVER
    }

    // Enum MỚI cho nhạc nền (có thể thêm các bản nhạc khác sau)
    public enum Music {
        BACKGROUND_GAME // Tương ứng với man1.mp3
    }

    private static Map<Sound, Media> sounds = new HashMap<>();
    private static Map<Music, Media> musicTracks = new HashMap<>(); // <-- Map MỚI cho nhạc

    // MediaPlayer riêng cho nhạc nền (để có thể dừng/lặp)
    private static MediaPlayer backgroundMusicPlayer; // <-- Biến MỚI

    static {
        try {
            // Tải hiệu ứng âm thanh
            sounds.put(Sound.HIT_BRICK, loadMedia("ball_hits_bricks.mp3"));
            sounds.put(Sound.HIT_PADDLE, loadMedia("ball_hits_paddle.mp3"));
            sounds.put(Sound.HIT_WALL, loadMedia("ball_hits_wall.mp3"));
            sounds.put(Sound.COLLECT_POWERUP, loadMedia("Game_coin_collect.mp3"));
            sounds.put(Sound.LEVEL_COMPLETED, loadMedia("level-completed.mp3"));
            sounds.put(Sound.MISSED_BALL, loadMedia("missed_ball.mp3"));
            sounds.put(Sound.GAME_OVER, loadMedia("game_over.mp3"));

            // Tải nhạc nền
            musicTracks.put(Music.BACKGROUND_GAME, loadMedia("man1.mp3")); // <-- Tải man1.mp3

            System.out.println("SoundManager: Đã tải " + sounds.size() + " SFX và " + musicTracks.size() + " bản nhạc.");

        } catch (Exception e) {
            System.err.println("SoundManager: Lỗi nghiêm trọng khi tải file âm thanh/nhạc!");
            e.printStackTrace();
        }
    }

    /**
     * Phương thức nội bộ để tải file media (âm thanh hoặc nhạc)
     */
    private static Media loadMedia(String fileName) {
        String path = "/com/example/arkanoid/sounds/" + fileName;
        try {
            String mediaUrl = SoundManager.class.getResource(path).toExternalForm();
            return new Media(mediaUrl);
        } catch (NullPointerException e) {
            System.err.println("Lỗi: Không tìm thấy file media: " + path);
            return null;
        } catch (Exception e) {
            System.err.println("Lỗi không xác định khi tải media: " + path);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Phát hiệu ứng âm thanh ngắn (SFX)
     */
    public static void playSound(Sound sound) {
        Media media = sounds.get(sound);
        if (media == null) {
            System.err.println("SoundManager: Bỏ qua SFX " + sound + " (chưa được tải).");
            return;
        }
        try {
            // Tạo MediaPlayer mới mỗi lần để SFX có thể chồng lên nhau
            MediaPlayer sfxPlayer = new MediaPlayer(media);
            sfxPlayer.play();
        } catch (Exception e) {
            System.err.println("Lỗi khi phát SFX: " + sound);
            e.printStackTrace();
        }
    }

    /**
     * PHƯƠNG THỨC MỚI: Bắt đầu phát nhạc nền (và lặp lại)
     */
    public static void playMusic(Music music) {
        // Dừng nhạc cũ nếu đang phát
        stopMusic();

        Media media = musicTracks.get(music);
        if (media == null) {
            System.err.println("SoundManager: Bỏ qua nhạc " + music + " (chưa được tải).");
            return;
        }
        try {
            backgroundMusicPlayer = new MediaPlayer(media);
            // Đặt chế độ lặp vô hạn
            backgroundMusicPlayer.setOnEndOfMedia(() -> {
                backgroundMusicPlayer.seek(Duration.ZERO);
                backgroundMusicPlayer.play();
            });
            backgroundMusicPlayer.play();
            System.out.println("SoundManager: Bắt đầu phát nhạc " + music);
        } catch (Exception e) {
            System.err.println("Lỗi khi phát nhạc: " + music);
            e.printStackTrace();
            backgroundMusicPlayer = null; // Đặt lại nếu có lỗi
        }
    }

    /**
     * PHƯƠNG THỨC MỚI: Dừng nhạc nền đang phát
     */
    public static void stopMusic() {
        if (backgroundMusicPlayer != null) {
            try {
                backgroundMusicPlayer.stop();
                backgroundMusicPlayer = null; // Giải phóng tài nguyên
                System.out.println("SoundManager: Đã dừng nhạc nền.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}