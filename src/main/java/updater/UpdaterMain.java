package updater;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;

public class UpdaterMain {
    private static void downloadUpdate(String url) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder(URI.create(url)).build();
            HttpResponse<Path> res = client.send(req, HttpResponse.BodyHandlers.ofFile(Path.of("JXPlayer.zip")));

            restart();
        } catch (UncheckedIOException | IOException | InterruptedException err) {
            System.out.println("Couldn't connect to the server while downloading: " + err);
        }
    }

    private static void restart() {
        System.out.println("Restarting...");
        try {
            Runtime.getRuntime().exec("cmd c/ start mpp.bat");
        } catch (IOException err) {
            System.out.println("Couldn't open new command prompt: " + err);
            err.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("No argument provided.");
            return;
        }

        String url = args[0];
        if (!url.contains("https://raw.githubusercontent.com/Nilo18/MusicPlayer/")) {
            System.out.println("Wrong URL.");
            return;
        }

        downloadUpdate(url);

        System.out.println("Hello, world!");
    }

}
