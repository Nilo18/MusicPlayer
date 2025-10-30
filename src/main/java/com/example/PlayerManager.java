package com.example;

import java.nio.file.Path;

// Class to ensure that the whole application is working with a single player while running
public class PlayerManager {
    private static final Player player = new Player();
//    private static boolean isLooped = false;
    private static boolean musicPlaying = false;

    public static void play(Path musicPath) {
        player.play(musicPath);
        musicPlaying = true;
    }

    public static void loop() {
        if (!player.isLooping() && musicPlaying) {
            System.out.println("Looping...");
            player.loop();
//            isLooped = true;
            player.setLoopState(true);
            System.out.println("Looped.");
        } else if (player.isLooping()) {
            System.out.println("The music has already been looped.");
        } else {
            System.out.println("No music to loop.");
        }
    }

    public static void skip() {
        if (musicPlaying) {
            System.out.println("Skipping...");
            musicPlaying = false;
//            isLooped = false;
            player.setLoopState(false);
            Path nextMusic = player.getQueue().getNextMusic();
            play(nextMusic);
        } else {
            System.out.println("No music to skip.");
        }
    }
}
