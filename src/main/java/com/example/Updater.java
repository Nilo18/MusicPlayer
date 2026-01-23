package com.example;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Properties;

public class Updater {
    private static String currentVersion;
//    private static Properties props = new Properties();
    private static String versionUrl = "https://raw.githubusercontent.com/Nilo18/MusicPlayer/1d0973e/version.json";

    static {
        String version = "unknown"; // Default value of version will be unknown

        try (InputStream str = Updater.class.getResourceAsStream("/version.properties")) {
            if (str != null) {
                Properties props = new Properties();
                props.load(str);
                version = props.getProperty("version", version);
            }
        } catch (IOException err) {
            System.out.println("Couldn't load version" + err);
            err.printStackTrace();
        }

        currentVersion = version;
    }

    public static void checkForUpdates() {
        try  {
            System.out.println("Checking for updates...");
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder(URI.create(versionUrl)).build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
//            JSONObject obj = new JSONObject(res.body());
            JsonElement rootElem = JsonParser.parseString(res.body());

            if (!rootElem.isJsonObject()) {
                System.out.println("Unexpected response format: not a valid JSON object.");
                return;
            }

            JsonObject rootObject = rootElem.getAsJsonObject();

            if (!rootObject.has("version")) {
                System.out.println("No 'version' field in response JSON");
                return;
            }

            if (!rootObject.has("url")) {
                System.out.println("No 'url' field in response JSON");
                return;
            }

            String latestVersion = rootObject.get("version").getAsString();
            System.out.println("Latest version: " + latestVersion);
            String downloadUrl = rootObject.get("url").getAsString();
            System.out.println("The downloadUrl: " + downloadUrl);

            if (!currentVersion.equals(latestVersion)) {
                System.out.println("New version available.");
                System.out.println("Downloading...");
//                downloadUpdate(downloadUrl);
//                restart();
            } else {
                System.out.println("The system is up to date.");
            }
        } catch (UncheckedIOException | IOException  | InterruptedException err) {
            System.out.println("Couldn't connect to the server: " + err);
        }
    }

}
