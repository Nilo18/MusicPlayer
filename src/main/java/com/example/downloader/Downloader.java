package com.example.downloader;

import com.example.AppConfig;
import com.example.player.PlayerManager;
import com.example.utilities.Utilities;
import com.google.gson.*;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class Downloader {
    private static boolean ytDlpUpdated = false;

    public static void downloadVideoYtDlp(String title, String url) {
        System.out.println("Downloading " + title + "...");

        Path mpDir = Paths.get(System.getProperty("user.home"), "MpMusic");

        if (!Files.exists(mpDir)) {
            System.out.println("Failed to detect MpMusic directory to store the music, creating it...");
            Utilities.createDirectory(mpDir);
        }

        String illegalCharactersRegex = "[\\\\/:*?\"<>|]";
        String safeTitle = title.replaceAll(illegalCharactersRegex, " ");

        Path filePath = Paths.get(System.getProperty("user.home"), "MpMusic", safeTitle + ".mp3");
        if (Files.exists(filePath)) {
            System.out.println("The suggested music has already been downloaded. Opening it locally...");
            Utilities.printCurrentMusic(title, url);
            Platform.runLater(() -> PlayerManager.play(filePath));
            return;
        }

        String userHome = System.getProperty("user.home");
        String outputPath = userHome + "/MpMusic/" + safeTitle + ".%(ext)s";

        String ytDlp = Utilities.getBinaryPath("yt-dlp");
        String ffmpeg = Utilities.getBinaryPath("ffmpeg");

        String[] splitToken = url.split("id=");
        String id = splitToken[0];
        String ytUrl = "https://www.youtube.com/watch?v=" + id;
//        System.out.println("ytUrl is: " + ytUrl);

        if (!ytDlpUpdated) {
            System.out.println("Checking for yt-dlp updates...");

            try {
                ProcessBuilder updatePb = new ProcessBuilder(ytDlp, "-U");
                Process updateProcess = updatePb.inheritIO().start();
                updateProcess.waitFor();
                ytDlpUpdated = true;
            } catch (IOException e) {
                System.out.println("Couldn't start yt-dlp update process." + e);
            } catch (InterruptedException e) {
                System.out.println("Interruption during yt-dlp update. " + e);
            } catch (Exception e) {
                System.out.println("Unknown exception has occurred while trying to update yt-dlp: " + e);
            }
        }

        ProcessBuilder pb = new ProcessBuilder(
                ytDlp, "--ffmpeg-location", ffmpeg,
                "--extract-audio",
                "--audio-format", "mp3",
                "--embed-thumbnail",        // adds album art
                "--embed-metadata",         // embeds title, uploader, etc.
                "--parse-metadata", "%(uploader)s:%(meta_artist)s",  // maps uploader → artist tag
                "--output", outputPath,
                ytUrl
        );

        try {
            Process process = pb.inheritIO().start();
            int exitCode = process.waitFor(); // Wait for it to finish

            if (exitCode == 0) {
                System.out.println("Download complete!");
                Utilities.printCurrentMusic(safeTitle, url);
                PlayerManager.play(filePath);
            } else {
                System.out.println("Download failed with exit code: " + exitCode);
            }
        } catch (IOException e) {
            System.out.println("Couldn't start download process." + e);
        } catch (InterruptedException e) {
            System.out.println("Interruption during download. " + e);
        } catch (Exception e) {
            System.out.println("Unknown exception has occurred: " + e);
        }
    }
}
