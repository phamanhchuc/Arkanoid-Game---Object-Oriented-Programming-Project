package com.example.arkanoid;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;
// --- XÓA IMPORT THREAD POOL ---
// (Không cần ExecutorService nữa)
// --- KẾT THÚC XÓA ---

public class SoundManager {
    public enum Sound {
        HIT_BRICK, HIT_PADDLE, HIT_WALL, COLLECT_POWERUP,
        LEVEL_COMPLETED, MISSED_BALL, GAME_OVER,
        TYPING // (Giữ lại từ lần sửa trước)
    }
    public enum Music { BACKGROUND_GAME }

    private static Map<Sound, Media> sounds = new HashMap<>();
    private static Map<Music, Media> musicTracks = new HashMap<>();
    private static MediaPlayer backgroundMusicPlayer;

    private static double masterVolume = 1.0;
    private static MediaPlayer typingSoundPlayer;

    // --- XÓA EXECUTORSERVICE (THREAD POOL) ---
    // (Không cần nữa)
    // --- KẾT THÚC XÓA ---

    static {
        try {
            sounds.put(Sound.HIT_BRICK, loadMedia("ball_hits_bricks.mp3"));
            sounds.put(Sound.HIT_PADDLE, loadMedia("ball_hits_paddle.mp3"));
            sounds.put(Sound.HIT_WALL, loadMedia("ball_hits_wall.mp3"));
            sounds.put(Sound.COLLECT_POWERUP, loadMedia("Game_coin_collect.mp3"));
            sounds.put(Sound.LEVEL_COMPLETED, loadMedia("level-completed.mp3"));
            sounds.put(Sound.MISSED_BALL, loadMedia("missed_ball.mp3"));
            sounds.put(Sound.GAME_OVER, loadMedia("game_over.mp3"));
            sounds.put(Sound.TYPING, loadMedia("typing_sound.mp3"));
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

    // --- HÀM NÀY ĐÃ ĐƯỢC SỬA ĐỂ BỎ THREAD POOL ---
    public static void playSound(Sound sound) {
        if (sound == Sound.TYPING) {
            // (Logic cho typing giữ nguyên)
            startTypingLoop();
            return;
        }

        Media media = sounds.get(sound);
        if (media == null) return;

        // --- SỬA LỖI: CHẠY TRỰC TIẾP ---
        // Vì hàm này được gọi từ AnimationTimer (luồng JFX) nên nó an toàn
        try {
            MediaPlayer sfxPlayer = new MediaPlayer(media);
            sfxPlayer.setVolume(masterVolume);

            // Quan trọng: Tự động giải phóng tài nguyên khi chơi xong
            sfxPlayer.setOnEndOfMedia(() -> sfxPlayer.dispose());

            sfxPlayer.play();
        } catch (Exception e) {
            System.err.println("Lỗi khi phát SFX: " + sound);
            e.printStackTrace();
        }
        // --- KẾT THÚC SỬA ---
    }

    // (Hàm prepareTypingPlayer và các hàm typing/music khác giữ nguyên)
    private static void prepareTypingPlayer() {
        Media media = sounds.get(Sound.TYPING);
        if (media != null) {
            try {
                typingSoundPlayer = new MediaPlayer(media);
                typingSoundPlayer.setVolume(masterVolume);
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
    public static void startTypingLoop() {
        if (typingSoundPlayer != null) {
            try {
                typingSoundPlayer.play();
            } catch (Exception e) { /* Bỏ qua */ }
        }
    }
    public static void stopTypingLoop() {
        if (typingSoundPlayer != null) {
            try {
                typingSoundPlayer.stop();
            } catch (Exception e) { /* Bỏ qua */ }
        }
    }
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

    // --- XÓA HÀM SHUTDOWN (VÌ KHÔNG CÒN THREAD POOL) ---
    public static void shutdown() {
        System.out.println("Đang tắt Âm thanh...");
        stopMusic();
        stopTypingLoop();
        // (Không cần audioPool.shutdown() nữa)
    }
}