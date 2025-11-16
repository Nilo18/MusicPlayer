package com.example.downloader;

import com.example.AppConfig;
import com.example.player.PlayerManager;
import com.example.utilities.Utilities;
import com.google.gson.*;
import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Downloader {
//    private static final Player player = new Player();
    // title is used to name the mp3 file after downloading
    // url is used to create a valid URI string
    public static void downloadVideo(String title, String url) {
        System.out.println("Downloading " + title + "...");
        HttpClient client = HttpClient.newHttpClient();
        String illegalCharactersRegex = "[\\\\/:*?\"<>|]";
        // Replace all occurrences of these illegal characters with an underscore (_)
        String safeTitle = title.replaceAll(illegalCharactersRegex, "_");

        Path filePath = Paths.get(System.getProperty("user.home"), "MpMusic", safeTitle + ".mp3");
        // If the suggested music is already downloaded, play it directly and exit without making any HTTP reqs
        if (Files.exists(filePath)) {
            System.out.println("The suggested music has already been downloaded. Opening it locally...");
            Utilities.printCurrentMusic(title, url);
            Platform.runLater(() -> PlayerManager.play(filePath));
            return;
        }
        // If the music isn't downloaded, make a request to the YouTube to mp3 API to download it
        try {
            HttpRequest req = HttpRequest.newBuilder().
                    uri(URI.create(url)).
                    header("x-rapidapi-key", AppConfig.getDotenvValue("DOWNLOADER_API_KEY")).
                    header("x-rapidapi-host", AppConfig.getDotenvValue("DOWNLOADER_API_HOST")).
                    method("GET", HttpRequest.BodyPublishers.noBody()).
                    build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
//            System.out.println("Received the response: " + res.body());
            JsonElement rootElem = JsonParser.parseString(res.body());
            if (!rootElem.isJsonObject()) {
                System.out.println("Unexpected response format: not a JSON object");
                return;
            }
            JsonObject rootObject = rootElem.getAsJsonObject();
            if (!rootObject.has("link")) {
                System.out.println("No 'link' field in response JSON");
                return;
            }

            String downloadLink = rootObject.get("link").getAsString();
            Path mpDir = Paths.get(System.getProperty("user.home"), "MpMusic");
            Utilities.createDirectory(mpDir);

            HttpRequest downloadReq = HttpRequest.newBuilder().
                    uri(URI.create(downloadLink)).
                    GET().
                    build();
            HttpResponse<Path> downloadRes = client.send(downloadReq, HttpResponse.BodyHandlers.ofFile(filePath));
            if (downloadRes.statusCode() == 200) {
                System.out.println("Download was successful.");
                Utilities.printCurrentMusic(title, url);
            } else {
                System.out.println("Failed to download.");
                return;
            }
            // Convert the file path to a File object so Desktop class can open it
            File fileToOpen = filePath.toFile();
            // Set javafx on its own thread
            Platform.runLater(() -> PlayerManager.play(filePath));
        } catch (IOException e) {
            // Handle network errors (e.g., connection refused, timeout)
            System.err.println("IO exception: " + e.getMessage());
        } catch (InterruptedException e) {
            // Handle if the thread was interrupted while waiting
            System.err.println("API request interrupted: " + e.getMessage());
            Thread.currentThread().interrupt(); // Re-interrupt the thread
        } catch (JsonSyntaxException | NullPointerException | IllegalStateException e) {
            // Handle errors where the JSON response structure is unexpected or invalid
            System.err.println("JSON Parsing Error: The API response format was invalid or unexpected keys were missing. " + e.getMessage());
            // You can print the body here for debugging: System.out.println("Response Body: " + res.body());
        } catch (Exception err) {
            System.out.println("Unknown error has occurred: " + err);
        }
    }
}
