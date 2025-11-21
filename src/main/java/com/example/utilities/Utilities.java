package com.example.utilities;

import javafx.application.Platform;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

/* The following class stores general utility methods */
public class Utilities {
    public static boolean openingSupported() {
        return Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);
    }

    public static void openFile(File fileToOpen) {
        if (openingSupported()) {
            try {
                Desktop.getDesktop().open(fileToOpen);
            } catch (IOException e) {
                System.out.println("Couldn't open the file.");
            }
        } else {
            System.err.println("Desktop operations (opening browser) are not supported on this system.");
        }
    }

    public static void printCurrentMusic(String videoTitle, String videoURL) {
        System.out.println("Now playing:");
        System.out.println("\nTitle: " + videoTitle);
        String[] splitToken = videoURL.split("id=");
        String id = splitToken[1];
        System.out.println("URL: https://www.youtube.com/watch?v=" + id);
    }

    public static void createDirectory(Path dir) {
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                System.out.println("Couldn't create directory.");
            }
        }
    }

    public static void exit(AtomicBoolean isRunning) {
        System.out.println("Exiting MusicPlayer CLI...");
        isRunning.set(false);
        Platform.exit(); // Exit javafx thread
        System.exit(0); // Exit the JVM completely
    }

//    public static void playMusic(String option) {
//        String[] token = option.split("\"");
//        String musicName = token[0];
//        Searcher.search(musicName, 1L);
//    }

    public static void showAllCommands() {
        System.out.println();
        System.out.println("mp play -" + "NAME OF THE SONG/YOUTUBE URL" + "--- Play the desired music, the music will be downloaded (if it isn't already installed) and played locally. Shortcut: mp p -" + "NAME OF THE SONG/YOUTUBE URL");
        System.out.println("mp loop --- Loop the music which is currently playing. Shortcut: mp l");
        System.out.println("mp skip --- Skip the music which is playing currently. Shortcut: mp s");
        System.out.println("mp exit --- Exit the CLI. Shortcut: mp e");
        System.out.println("mp pause --- Pause the music which is currently playing. Shortcut: mp pa.");
        System.out.println("mp resume --- Resume the music which is currently playing. Shortcut: mp re");
        System.out.println("mp forward -AMOUNT IN SECONDS --- Forward the music by given seconds. Shortcut: mp f -AMOUNT IN SECONDS.");
        System.out.println("mp rewind -AMOUNT IN SECONDS --- Rewind the music by given seconds. Shortcut: mp r -AMOUNT IN SECONDS.");
        System.out.println("mp list --- Show the playlist of all downloaded music stored in MpMusic folder. Shortcut: mp li.");
        System.out.println();
    }

}
