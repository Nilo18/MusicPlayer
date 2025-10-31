package com.example;

import javafx.application.Platform;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

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
                return;
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
                System.out.println("Couldn't create MpMusic directory.");
                return;
            }
        }
    }

    public static void exit(AtomicBoolean isRunning) {
        System.out.println("Exiting MusicPlayer CLI...");
        isRunning.set(false);
        Platform.exit(); // Exit javafx thread
        System.exit(0); // Exit the JVM completely
    }

    public static void playMusic(String option) {
        String[] token = option.split("\"");
//        System.out.println("Token is: " + Arrays.toString(token));
        String musicName = token[0];
//        System.out.println("The music name is: " + musicName);
        Searcher.search(musicName);
    }

    public static void loopMusic() {
        Platform.runLater(() -> PlayerManager.loop());
    }

}
