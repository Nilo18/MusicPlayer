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
        player.setOnEndOfMedia(this::playNext);
        player.setOnReady(() -> {
            player.seek(Duration.ZERO); // ensure playback starts from beginning
            player.play();
        });
        player.setOnError(() -> {
            System.out.println("Media error: " + player.getError());
            if (player.getError() != null) player.getError().printStackTrace();
        });

//        Platform.runLater(() -> {
//            player.play();
//        });
    }

    private void playNext() {
        Path nextMusic = queue.getNextMusic();
        if (nextMusic != null) {
            play(nextMusic);
        } else {
            System.out.println("Playlist finished.");
        }
    }
}
