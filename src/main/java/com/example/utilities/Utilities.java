package com.example.utilities;

import com.example.App;
import com.example.player.PlayerManager;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import javafx.application.Platform;
import javafx.util.Duration;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
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
        String id = splitToken[0];
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
        System.out.println("Exiting JXPlayer CLI...");
        isRunning.set(false);
        Platform.exit(); // Exit javafx thread
//        PlayerManager.getPlayer().close();
        System.exit(0); // Exit the JVM completely
    }

    public static void showAllCommands() {
        System.out.println();
        System.out.println("play -" + "NAME OF THE SONG/YOUTUBE URL" + "--- Play the desired music, the music will be downloaded (if it isn't already installed) and played locally. Shortcut: p -" + "NAME OF THE SONG/YOUTUBE URL");
        System.out.println("loop --- Loop the music which is currently playing. Shortcut: l");
        System.out.println("skip --- Skip the music which is playing currently. Shortcut: s");
        System.out.println("previous --- Play the previous music which stands before the current music in the playlist. Shortcut: prev");
        System.out.println("exit --- Exit the CLI. Shortcut: e");
        System.out.println("pause --- Pause the music which is currently playing. Shortcut: pa.");
        System.out.println("resume --- Resume the music which is currently playing. Shortcut: re");
        System.out.println("forward -AMOUNT IN SECONDS --- Forward the music by given seconds. Shortcut: f -AMOUNT IN SECONDS.");
        System.out.println("rewind -AMOUNT IN SECONDS --- Rewind the music by given seconds. Shortcut: r -AMOUNT IN SECONDS.");
        System.out.println("list --- Show the playlist of all downloaded music stored in MpMusic folder. Shortcut: li. NOTE: After downloading new music, the program must be restarted for the changes to affect the music directory");
        System.out.println("duration --- Show the duration of the music which is currently playing. Shortcut: dur");
        System.out.println("remaining --- Show how much time current music has left until it finishes. Shortcut: rem");
        System.out.println();
    }

    public static String getBinaryPath(String binaryName) {
        try {
            String jarPath = App.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();
            File jarFile = new File(jarPath);
            File appRoot = jarFile.getParentFile();

            String os = System.getProperty("os.name").toLowerCase();
            String subFolder;
            if (os.contains("win")) {
                subFolder = "windows";
                binaryName += ".exe";
            } else if (os.contains("mac")) {
                subFolder = "macos";
            } else {
                subFolder = "linux";
            }

            File binDir = new File(appRoot, "bin");
//            System.out.println("Binary directory is: " + binDir.getAbsolutePath());
            File platformFolder = new File(binDir, subFolder);
            File binaryFile = new File(platformFolder, binaryName);
//            System.out.println("Found the binary file at: " + binaryFile.getAbsolutePath());

            if (binaryFile.exists()) {
                return binaryFile.getAbsolutePath();
            }

            return binaryName;
        } catch (Exception e) {
            return binaryName;
        }
    }

    public static void showDuration() {
        Path currentMusic = PlayerManager.getCurrentMusic();

        if (currentMusic == null) {
            System.out.println("No music playing.");
            return;
        }

        try {
            Mp3File music = new Mp3File(currentMusic);
            if (music.hasId3v2Tag()) {
                long lengthInSeconds = music.getLengthInSeconds();
                long minutes = lengthInSeconds / 60;
                long seconds = lengthInSeconds % 60;
                System.out.printf("Duration: %d:%02d%n", minutes, seconds);
            }
        } catch (IOException e) {
            System.out.println("Couldn't load music file: " + e);
        } catch (UnsupportedTagException e) {
            System.out.println("Unsupported tag detected: " + e);
        } catch (InvalidDataException e) {
            System.out.println("Invalid data detected: " + e);
        }
    }

    public static void showRemainingTime() {
        Duration total = PlayerManager.getTotalDuration();
        Duration current = PlayerManager.getCurrentTime();

        if (total == null) {
            System.out.println("No music playing.");
            return;
        }

        if (current == null) {
            System.out.println("No music playing.");
            return;
        }

        Duration remainder = total.subtract(current);
        int minutes = (int) remainder.toMinutes();
        int seconds = (int) remainder.toSeconds() % 60;
        System.out.println("Remaining time: " +  minutes + "m" + " " + seconds + "s");
    }
}
