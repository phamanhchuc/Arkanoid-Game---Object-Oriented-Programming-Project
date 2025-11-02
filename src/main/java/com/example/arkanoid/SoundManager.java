package com.example.arkanoid;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    public enum Sound {
        HIT_BRICK, HIT_PADDLE, HIT_WALL, COLLECT_POWERUP,
        LEVEL_COMPLETED, MISSED_BALL, GAME_OVER,
        TYPING
    }
    public enum Music { BACKGROUND_GAME }

    private static Map<Sound, Media> sounds = new HashMap<>();
    private static Map<Music, Media> musicTracks = new HashMap<>();
    private static MediaPlayer backgroundMusicPlayer;

    private static double masterVolume = 1.0;

    private static MediaPlayer typingSoundPlayer;

    static {
        try {
            sounds.put(Sound.HIT_BRICK, loadMedia("ball_hits_bricks.mp3"));
            sounds.put(Sound.HIT_PADDLE, loadMedia("ball_hits_paddle.mp3"));
            sounds.put(Sound.HIT_WALL, loadMedia("ball_hits_wall.mp3"));
            sounds.put(Sound.COLLECT_POWERUP, loadMedia("Game_coin_collect.mp3"));
            sounds.put(Sound.LEVEL_COMPLETED, loadMedia("level-completed.mp3"));
            sounds.put(Sound.MISSED_BALL, loadMedia("missed_ball.mp3"));
            sounds.put(Sound.GAME_OVER, loadMedia("game_over.mp3"));

            sounds.put(Sound.TYPING, loadMedia("typing_sound.mp3")); // Tên file của bạn

            musicTracks.put(Music.BACKGROUND_GAME, loadMedia("man1.mp3"));

            prepareTypingPlayer();

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
        } catch (Exception e) {
            System.err.println("Lỗi khi tải media: " + path);
            return null;
        }
    }

    public static void playSound(Sound sound) {
        // --- SỬA LOGIC GỌI ---
        if (sound == Sound.TYPING) {
            // Không làm gì ở đây, dùng hàm start/stop
            return;
        }
        // --- KẾT THÚC SỬA ---

        Media media = sounds.get(sound);
        if (media == null) return;
        try {
            MediaPlayer sfxPlayer = new MediaPlayer(media);
            sfxPlayer.setVolume(masterVolume);
            sfxPlayer.play();
        } catch (Exception e) {
            System.err.println("Lỗi khi phát SFX: " + sound); e.printStackTrace();
        }
    }

    // --- HÀM NÀY ĐÃ SỬA ---
    /**
     * Tải trước MediaPlayer và cài đặt lặp lại (Loop).
     */
    private static void prepareTypingPlayer() {
        Media media = sounds.get(Sound.TYPING);

        if (media != null) {
            try {
                typingSoundPlayer = new MediaPlayer(media);
                typingSoundPlayer.setVolume(masterVolume);
                // --- THÊM DÒNG NÀY ĐỂ LẶP LẠI ---
                typingSoundPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            } catch (Exception e) {
                System.err.println("Lỗi khi chuẩn bị typingSoundPlayer");
                typingSoundPlayer = null;
            }
        } else {
            System.err.println("SoundManager: Không tìm thấy file 'typing_sound.mp3', tiếng gõ phím sẽ bị tắt.");
            typingSoundPlayer = null;
        }
    }

    // --- ĐỔI TÊN HÀM NÀY ---
    /**
     * Bắt đầu phát âm thanh gõ phím lặp lại.
     */
    public static void startTypingLoop() {
        if (typingSoundPlayer != null) {
            try {
                typingSoundPlayer.play(); // Sẽ tự động lặp
            } catch (Exception e) {
                // (Bỏ qua lỗi nếu có)
            }
        }
    }

    // --- THÊM HÀM MỚI NÀY ---
    /**
     * Dừng âm thanh gõ phím lặp lại.
     */
    public static void stopTypingLoop() {
        if (typingSoundPlayer != null) {
            try {
                typingSoundPlayer.stop(); // Dừng và tua về 0
            } catch (Exception e) {
                // (Bỏ qua lỗi nếu có)
            }
        }
    }
    // --- KẾT THÚC THÊM/SỬA ---

    public static void playMusic(Music music) {
        stopMusic();
        Media media = musicTracks.get(music);
        if (media == null) return;
        try {
            backgroundMusicPlayer = new MediaPlayer(media);
            backgroundMusicPlayer.setVolume(masterVolume);
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

    public static void setMasterVolume(double volume) {
        masterVolume = Math.max(0.0, Math.min(1.0, volume));
        System.out.println("SoundManager: Đặt âm lượng thành " + masterVolume);

        if (backgroundMusicPlayer != null) {
            try {
                backgroundMusicPlayer.setVolume(masterVolume);
            } catch (Exception e) { e.printStackTrace(); }
        }

        if (typingSoundPlayer != null) {
            try {
                typingSoundPlayer.setVolume(masterVolume);
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    public static double getMasterVolume() {
        return masterVolume;
    }
}