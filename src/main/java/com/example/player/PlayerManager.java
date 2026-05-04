package com.example.player;

import javafx.util.Duration;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

// Class to ensure that the whole application is working with a single player while running
public class PlayerManager {
    private static final Player player = new Player();
    private static boolean musicPlaying = false;

    public static void play(Path musicPath) {
        player.play(musicPath);
        musicPlaying = true;
    }

    public static void loop() {
        if (!player.isLooping() && musicPlaying) {
            System.out.println("Looping...");
            player.loop();
            player.setLoopState(true);
            System.out.println("Looped.");
            System.out.print("#> ");
        } else if (player.isLooping()) {
            System.out.println("The music has already been looped.");
            System.out.print("#> ");
        } else {
            System.out.println("No music to loop.");
            System.out.print("#> ");
        }
    }

    public static void skip() {
        if (musicPlaying) {
            System.out.println("Skipping...");
            musicPlaying = false;
            player.setLoopState(false);
            Path nextMusic = player.getQueue().getNextMusic();
            play(nextMusic);
        } else {
            System.out.println("No music to skip.");
        }
    }

    public static void previous() {
        if (musicPlaying) {
            System.out.println("Playing previous...");
            musicPlaying = false;
            player.setLoopState(false);
            Path prevMusic = player.getQueue().getPrevMusic();
            play(prevMusic);
        } else {
            System.out.println("No previous music to go back.");
        }
    }

    public static void pause() {
        if (musicPlaying) {
            player.pause();
            musicPlaying = false;
        } else {
            System.out.println("No music to pause.");
        }
    }

    public static void resume() {
        player.resume();
        musicPlaying = true;
    }

    public static void forward(String option) {
        // d+ means one or more digits, i.e 9, 12, 12332 or whatever.
        boolean isNumber = option.matches("\\d+");

        // NumberFormatException is caught to ensure that an input which exceeds int capacity is handled gracefully
        try {
            if (musicPlaying && isNumber) {
                String[] tokens = option.split("-");
                int howMuch = Integer.parseInt(tokens[0]);
                player.forward(howMuch);
            } else if (!isNumber) {
                System.out.println("Invalid argument, please enter the amount in seconds.");
            } else {
                System.out.println("No music to forward.");
            }
        } catch (NumberFormatException e) {
            System.out.println("The suggested forward exceeds the forwarding capacity.");
        }
    }

    public static void rewind(String option) {
        // d+ means one or more digits, i.e 9, 12, 12332 or whatever.
        boolean isNumber = option.matches("\\d+");

        // NumberFormatException is caught to ensure that an input which exceeds int capacity is handled gracefully
        try {
            if (musicPlaying && isNumber) {
                String[] tokens = option.split("-");
                int howMuch = Integer.parseInt(tokens[0]);
                player.rewind(howMuch);
            } else if (!isNumber) {
                System.out.println("Invalid argument, please enter the amount in seconds.");
            } else {
                System.out.println("No music to rewind.");
            }
        } catch (NumberFormatException e) {
            System.out.println("The suggested rewind exceeds the rewinding capacity.");
        }
    }

    public static void showPlaylist() {
        player.activatePlaylistSelectionMode();
    }

    public static Queue getQueue() {
        return player.getQueue();
    }

    public static Player getPlayer() {
        return player;
    }

    public static Path getCurrentMusic() {
       return player.getCurrentMusic();
    }

    public static Duration getTotalDuration() {
        return player.getTotalDuration();
    }

    public static Duration getCurrentTime() {
        return player.getCurrentTime();
    }
}
