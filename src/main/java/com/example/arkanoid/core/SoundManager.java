package com.example.arkanoid.core;

import javafx.scene.media.AudioClip; // <-- Thay đổi quan trọng
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.HashMap;
import java.util.Map;

public class SoundManager {

    // --- SFX ---
    public enum Sound {
        HIT_BRICK, HIT_PADDLE, HIT_WALL, COLLECT_POWERUP,
        LEVEL_COMPLETED, MISSED_BALL, GAME_OVER,
        TYPING,
        LASER_BEAM,
        ROAR
    }

    // --- MUSIC TỪNG LEVEL ---
    public enum Music {
        LEVEL1,
        LEVEL2,
        LEVEL3
    }

    // Tối ưu: Dùng AudioClip cho SFX (Load vào RAM, phát ngay lập tức)
    private static Map<Sound, AudioClip> soundClips = new HashMap<>();
    // Dùng Media cho Music (Stream từ đĩa, tốt cho file dài)
    private static Map<Music, Media> musicTracks = new HashMap<>();

    private static MediaPlayer backgroundMusicPlayer;
    private static MediaPlayer typingSoundPlayer; // Typing loop vẫn dùng MediaPlayer để dễ loop

    private static double masterVolume = 1.0;

    // ---------------------------------------------------------
    // LOAD MEDIA
    // ---------------------------------------------------------
    static {
        try {
            // --- Load SFX (Dùng AudioClip) ---
            loadSoundClip(Sound.HIT_BRICK, "ball_hits_bricks.mp3");
            loadSoundClip(Sound.HIT_PADDLE, "ball_hits_paddle.mp3");
            loadSoundClip(Sound.HIT_WALL, "ball_hits_wall.mp3");
            loadSoundClip(Sound.COLLECT_POWERUP, "Game_coin_collect.mp3");
            loadSoundClip(Sound.LEVEL_COMPLETED, "level-completed.mp3");
            loadSoundClip(Sound.MISSED_BALL, "missed_ball.mp3");
            loadSoundClip(Sound.GAME_OVER, "game_over.mp3");
            loadSoundClip(Sound.LASER_BEAM, "laser_beam.mp3");
            loadSoundClip(Sound.ROAR, "roar.mp3");



            // Riêng TYPING dùng MediaPlayer để loop dễ hơn
            loadTypingMedia("typing_sound.mp3");

            // --- Load music từng level (Dùng Media) ---
            loadMusicTrack(Music.LEVEL1, "man1.mp3");
            loadMusicTrack(Music.LEVEL2, "man2.mp3");
            loadMusicTrack(Music.LEVEL3, "man3.mp3");

            System.out.println("SoundManager: Loaded resources.");

        } catch (Exception e) {
            System.err.println("SoundManager: Error loading sounds!");
            e.printStackTrace();
        }
    }

    private static void loadSoundClip(Sound sound, String fileName) {
        try {
            String path = "/com/example/arkanoid/sounds/" + fileName;
            String url = SoundManager.class.getResource(path).toExternalForm();
            soundClips.put(sound, new AudioClip(url));
        } catch (Exception e) {
            System.err.println("Error loading AudioClip: " + fileName);
        }
    }

    private static void loadMusicTrack(Music music, String fileName) {
        try {
            String path = "/com/example/arkanoid/sounds/" + fileName;
            String url = SoundManager.class.getResource(path).toExternalForm();
            musicTracks.put(music, new Media(url));
        } catch (Exception e) {
            System.err.println("Error loading Music: " + fileName);
        }
    }

    private static void loadTypingMedia(String fileName) {
        try {
            String path = "/com/example/arkanoid/sounds/" + fileName;
            Media media = new Media(SoundManager.class.getResource(path).toExternalForm());
            typingSoundPlayer = new MediaPlayer(media);
            typingSoundPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        } catch (Exception e) {
            System.err.println("Error loading Typing sound");
        }
    }

    // ---------------------------------------------------------
    // PLAY SFX (Đã tối ưu)
    // ---------------------------------------------------------
    public static void playSound(Sound sound) {
        if (sound == Sound.TYPING) {
            startTypingLoop();
            return;
        }

        AudioClip clip = soundClips.get(sound);
        if (clip != null) {
            // AudioClip hỗ trợ phát chồng (polyphony) tự động và volume riêng
            clip.play(masterVolume);
        }
    }

    // ---------------------------------------------------------
    // TYPING LOOP
    // ---------------------------------------------------------
    public static void startTypingLoop() {
        if (typingSoundPlayer != null && typingSoundPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
            typingSoundPlayer.setVolume(masterVolume);
            typingSoundPlayer.play();
        }
    }

    public static void stopTypingLoop() {
        if (typingSoundPlayer != null) {
            typingSoundPlayer.stop();
        }
    }

    // ---------------------------------------------------------
    // MUSIC
    // ---------------------------------------------------------
    public static void playMusic(Music music) {
        stopMusic();

        Media media = musicTracks.get(music);
        if (media == null) {
            System.err.println("Music not found: " + music);
            return;
        }

        try {
            backgroundMusicPlayer = new MediaPlayer(media); // đa luồng
            backgroundMusicPlayer.setVolume(masterVolume);
            backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Loop vô hạn
            backgroundMusicPlayer.play();
            System.out.println("Playing music: " + music);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stopMusic() {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.stop();
            backgroundMusicPlayer.dispose(); // Giải phóng tài nguyên
            backgroundMusicPlayer = null;
        }
    }

    // ---------------------------------------------------------
    // VOLUME CONTROL
    // ---------------------------------------------------------
    public static void setMasterVolume(double volume) {
        masterVolume = Math.max(0.0, Math.min(1.0, volume));
        if (backgroundMusicPlayer != null) backgroundMusicPlayer.setVolume(masterVolume);
        if (typingSoundPlayer != null) typingSoundPlayer.setVolume(masterVolume);
    }

    public static double getMasterVolume() {
        return masterVolume;
    }

    // ---------------------------------------------------------
    // SHUTDOWN
    // ---------------------------------------------------------
    public static void shutdown() {
        stopMusic();
        stopTypingLoop();
        if (typingSoundPlayer != null) typingSoundPlayer.dispose();
    }
}