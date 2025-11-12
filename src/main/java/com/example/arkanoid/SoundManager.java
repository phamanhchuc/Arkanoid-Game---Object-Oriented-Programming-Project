package com.example.arkanoid;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

public class SoundManager {

    // --- SFX ---
    public enum Sound {
        HIT_BRICK, HIT_PADDLE, HIT_WALL, COLLECT_POWERUP,
        LEVEL_COMPLETED, MISSED_BALL, GAME_OVER,
        TYPING,
        LASER_BEAM
    }

    // --- MUSIC TỪNG LEVEL ---
    public enum Music {
        LEVEL1,
        LEVEL2,
        LEVEL3
    }

    private static Map<Sound, Media> sounds = new HashMap<>();
    private static Map<Music, Media> musicTracks = new HashMap<>();

    private static MediaPlayer backgroundMusicPlayer;
    private static MediaPlayer typingSoundPlayer;

    private static double masterVolume = 1.0;

    // ---------------------------------------------------------
    // LOAD MEDIA
    // ---------------------------------------------------------
    static {
        try {
            // --- Load SFX ---
            sounds.put(Sound.HIT_BRICK, loadMedia("ball_hits_bricks.mp3"));
            sounds.put(Sound.HIT_PADDLE, loadMedia("ball_hits_paddle.mp3"));
            sounds.put(Sound.HIT_WALL, loadMedia("ball_hits_wall.mp3"));
            sounds.put(Sound.COLLECT_POWERUP, loadMedia("Game_coin_collect.mp3"));
            sounds.put(Sound.LEVEL_COMPLETED, loadMedia("level-completed.mp3"));
            sounds.put(Sound.MISSED_BALL, loadMedia("missed_ball.mp3"));
            sounds.put(Sound.GAME_OVER, loadMedia("game_over.mp3"));
            sounds.put(Sound.TYPING, loadMedia("typing_sound.mp3"));
            sounds.put(Sound.LASER_BEAM, loadMedia("laser_beam.mp3"));


            // --- Load music từng level ---
            musicTracks.put(Music.LEVEL1, loadMedia("man1.mp3"));
            musicTracks.put(Music.LEVEL2, loadMedia("man2.mp3"));
            musicTracks.put(Music.LEVEL3, loadMedia("man3.mp3"));

            prepareTypingPlayer();

            System.out.println("SoundManager: Loaded " + sounds.size() +
                    " SFX, " + musicTracks.size() + " music tracks.");

        } catch (Exception e) {
            System.err.println("SoundManager: Error loading sounds!");
            e.printStackTrace();
        }
    }

    private static Media loadMedia(String fileName) {
        String path = "/com/example/arkanoid/sounds/" + fileName;
        try {
            String mediaUrl = SoundManager.class.getResource(path).toExternalForm();
            return new Media(mediaUrl);
        } catch (Exception e) {
            System.err.println("Error loading media: " + path);
            return null;
        }
    }

    // ---------------------------------------------------------
    // PLAY SFX
    // ---------------------------------------------------------
    public static void playSound(Sound sound) {
        if (sound == Sound.TYPING) {
            startTypingLoop();
            return;
        }

        Media media = sounds.get(sound);
        if (media == null) return;

        try {
            MediaPlayer sfxPlayer = new MediaPlayer(media);
            sfxPlayer.setVolume(masterVolume);

            // Tự giải phóng sau khi chơi xong
            sfxPlayer.setOnEndOfMedia(sfxPlayer::dispose);

            sfxPlayer.play();
        } catch (Exception e) {
            System.err.println("Error playing SFX: " + sound);
            e.printStackTrace();
        }
    }

    // ---------------------------------------------------------
    // TYPING LOOP
    // ---------------------------------------------------------
    private static void prepareTypingPlayer() {
        Media media = sounds.get(Sound.TYPING);
        if (media != null) {
            try {
                typingSoundPlayer = new MediaPlayer(media);
                typingSoundPlayer.setVolume(masterVolume);
                typingSoundPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            } catch (Exception e) {
                System.err.println("Error preparing typing sound");
                typingSoundPlayer = null;
            }
        }
    }

    public static void startTypingLoop() {
        if (typingSoundPlayer != null) {
            try {
                typingSoundPlayer.play();
            } catch (Exception ignored) {}
        }
    }

    public static void stopTypingLoop() {
        if (typingSoundPlayer != null) {
            try {
                typingSoundPlayer.stop();
            } catch (Exception ignored) {}
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
            backgroundMusicPlayer = new MediaPlayer(media);
            backgroundMusicPlayer.setVolume(masterVolume);

            // LOOP vô hạn
            backgroundMusicPlayer.setOnEndOfMedia(() -> {
                backgroundMusicPlayer.seek(Duration.ZERO);
                backgroundMusicPlayer.play();
            });

            backgroundMusicPlayer.play();
            System.out.println("Playing music: " + music);

        } catch (Exception e) {
            System.err.println("Error playing music: " + music);
            e.printStackTrace();
        }
    }

    public static void stopMusic() {
        if (backgroundMusicPlayer != null) {
            try {
                backgroundMusicPlayer.stop();
                backgroundMusicPlayer.dispose();
            } catch (Exception ignored) {}

            backgroundMusicPlayer = null;
            System.out.println("Stopped background music.");
        }
    }

    // ---------------------------------------------------------
    // VOLUME CONTROL
    // ---------------------------------------------------------
    public static void setMasterVolume(double volume) {
        masterVolume = Math.max(0.0, Math.min(1.0, volume));

        if (backgroundMusicPlayer != null) backgroundMusicPlayer.setVolume(masterVolume);
        if (typingSoundPlayer != null) typingSoundPlayer.setVolume(masterVolume);

        System.out.println("Master volume set to " + masterVolume);
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
    }
}
