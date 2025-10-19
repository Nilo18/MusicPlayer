package com.example;

import com.google.gson.*;

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
    // title is used to name the mp3 file after downloading
    // url is used to create a valid URI string
    public static void downloadVideo(String title, String url) {
        HttpClient client = HttpClient.newHttpClient();
        System.out.println("Downloading " + title + "...");
        try {
            HttpRequest req = HttpRequest.newBuilder().
                    uri(URI.create(url)).
                    header("x-rapidapi-key", AppConfig.getDotenvValue("DOWNLOADER_API_KEY")).
                    header("x-rapidapi-host", AppConfig.getDotenvValue("DOWNLOADER_API_HOST")).
                    method("GET", HttpRequest.BodyPublishers.noBody()).
                    build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

            Gson gson = new Gson();
            JsonElement rootElem = JsonParser.parseString(res.body());
            JsonObject rootObject = rootElem.getAsJsonObject();

            String illegalCharactersRegex = "[\\\\/:*?\"<>|]";

            // Replace all occurrences of these illegal characters with an underscore (_)
            String safeTitle = title.replaceAll(illegalCharactersRegex, "_");

            String downloadLink = rootObject.get("link").getAsString();
            Path mpDir = Paths.get(System.getProperty("user.home"), "MpMusic");
            Utilities.createDirectory(mpDir);
            Path filePath = Paths.get(System.getProperty("user.home"), "MpMusic", safeTitle + ".mp3");
            if (Files.exists(filePath)) {
                System.out.println("The suggested music has already been downloaded.");
                System.out.println("Opening it locally...");
                Utilities.printCurrentMusic(title, url);
                Utilities.openFile(filePath.toFile());
                return;
            }

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
            Utilities.openFile(fileToOpen);
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
        }
    }
}
