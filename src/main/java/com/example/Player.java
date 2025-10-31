package com.example;

import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.nio.file.Path;
import java.util.Arrays;

public class Player {
    private MediaPlayer player;
    private Queue queue = new Queue();
    private boolean isLooped = false;

    public void play(Path musicPath) {
        if (player != null) {
            player.dispose();
        }

        // Add new music to queue,
        // this ensures that the program doesn't have to be restarted
        // in order for the new music to be included in the queue
//        queue.add(musicPath);
//        System.out.println("Media URI: " + musicPath.toUri().toString());
        System.out.println();
        Media media = new Media(musicPath.toUri().toString());
        player = new MediaPlayer(media);
//        System.out.println("I'm running, here's the player object: " + player);
//        System.out.println("Here's the provided path as well: " + musicPath);

        // Add listener to the end of the music so it plays the next one in the queue
        if (!isLooped) {
            player.setOnEndOfMedia(this::playNext);
        } else {
            System.out.println("The current music is looped so I won't play the next one.");
            return;
        }
        player.setOnReady(() -> {
            player.seek(Duration.ZERO); // ensure playback starts from beginning
            player.play();
        });
        player.setOnError(() -> {
            System.out.println("Media error: " + player.getError());
            if (player.getError() != null) player.getError().printStackTrace();
        });
    }

    public void playNext() {
        Path nextMusic = queue.getNextMusic();
        if (nextMusic != null) {
            play(nextMusic);
        } else {
            System.out.println("Playlist finished.");
        }
    }

    public void loop() {
        if (player != null) {
            isLooped = true;
            player.setOnEndOfMedia(() -> {
                player.seek(Duration.ZERO);
                player.play();
            });
        }
        player.setCycleCount(MediaPlayer.INDEFINITE);
        this.isLooped = true;
    }

    public void pause() {
        // Ensure that the player is properly initialized
        if (player == null) {
            System.out.println("No music to pause.");
            return;
        }
        // Only pause if there is a music playing, this condition is handled in PlayerManager as well
        // But PlayerManager is basically a wrapper of a single Player object and handles the condition
        // only for that specific Player object, here we handle the case for all Player objects to ensure
        // That none of them throw unexpected exceptions
        MediaPlayer.Status status = player.getStatus();
        if (status == MediaPlayer.Status.PLAYING) {
            player.pause();
            System.out.println("Music paused.");
        }
    }

    public void resume() {
        // If the player hasn't been initialized, exit prematurely to prevent nullptr errors
        if (player == null) {
            System.out.println("No music to resume.");
            return;
        }
        MediaPlayer.Status status = player.getStatus();
        // If the music isn't already playing, play it again
        if (status != MediaPlayer.Status.PLAYING) {
            player.play();
            System.out.println("Music resumed.");
        }
    }

    public Queue getQueue() {
        return queue;
    }

    public void setLoopState(boolean val) {
        this.isLooped = val;
    }

    public boolean isLooping() {
        return isLooped;
    }
}
