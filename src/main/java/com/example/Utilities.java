package com.example;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

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

}
