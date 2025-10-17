package com.example;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.gson.*;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Searcher {
    public static void searchByKeyword(String keyword) {
        // JsonFactory is an interface which defines how a json parser should behave
        // GsonFactory.getDefaultInstance() sets up the parser with the default settings
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        // setApplicationName() will notify google which app is making a request to their API
        // YouTubeRequestInitializer injects the API key to every request
        // The first parameter pass on http transport to actually make requests
        // The second one passes a parser to parse data
        // The third one is a shorthand for HttpRequestInitializer, which initializes http requests
        YouTube youtube = new YouTube.Builder(
                new NetHttpTransport(),
                jsonFactory,
                request -> {}
        ).setApplicationName("youtube-search-demo").
                setYouTubeRequestInitializer(new YouTubeRequestInitializer(AppConfig.getDotenvValue("YOUTUBE_API_KEY"))).build();
//            YouTube.Search.List search;

        try {
            // list() specifies which data we want to receive, snippet contains the title, thumbnail, etc.
            YouTube.Search.List search = youtube.search().list("id, snippet");
            search.setQ(keyword); // Set the query to the suggested song name
            search.setType("video"); // Set the type of the request to video
            search.setMaxResults(1L); // L is used here because the method expects long

            // This will send the request to the YouTube's servers and wait for the response
            SearchListResponse response = search.execute();
            // This will turn all the returned video items into a java list
            List<SearchResult> results = response.getItems();

            if (results != null && !results.isEmpty()) {
                SearchResult video = results.get(0); // only item
                String title = video.getSnippet().getTitle();
//                System.out.println("Downloading " + title + "...");

                String url = "https://youtube-mp36.p.rapidapi.com/dl?id=" + video.getId().getVideoId();
                // *********************************************
                Downloader.downloadVideo(title, url);

                System.out.println("Now playing:");
                System.out.println("\nTitle: " + video.getSnippet().getTitle());
                System.out.println("URL: https://www.youtube.com/watch?v=" + video.getId().getVideoId());
            } else {
                System.out.println("No such video was found.");
            }
        } catch (IOException err) {
            System.out.println("Couldn't receive response from the search: " + err);
            return;
        } catch (Exception e) {
            System.err.println("An error occurred during download: " + e);
            e.printStackTrace();
        }
    }

    // Private method for extracting the title to name the mp3 video
    // Used by searchByURL
    private static String extractTitle(String htmlContent) {
        try {
            Document doc = Jsoup.parse(htmlContent);
            String title = doc.title();

            // Clean the title as in Method (removing " - YouTube"
            if (title.endsWith(" - YouTube")) {
                title = title.substring(0, title.lastIndexOf(" - YouTube")).trim();
            }

            return title;
        } catch (Exception err) {
            return "untitled_video";
        }
    }

    public static void searchByURL(String id) {
        String url = "https://youtube-mp36.p.rapidapi.com/dl?id=" + id;
        String youtubeURL = "https://www.youtube.com/watch?v=" + id;
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpRequest req = HttpRequest.newBuilder().
                    uri(URI.create(youtubeURL)).
                    header("User-Agent", "Mozilla/5.0").
                    build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() == 200) {
                String videoHTML = res.body();
                String title = extractTitle(videoHTML);

                Downloader.downloadVideo(title, url);
            }
        } catch (IOException err) {
            System.out.println("Couldn't receive response from the search: " + err);
            return;
        } catch (InterruptedException err) {
            System.out.println("The search was interrupted: " + err);
            return;
        }
    }

    public static void search(String token) {
        System.out.println("Downloading " + token + "...");
        // If the user entering a URL extract the id of the video from it using Jsoup
        // Else search by the name using YouTube API
        if (token.contains("https://www.youtube.com/watch?v=")) {
            String[] splitToken = token.split("v=");
            String queryParams = splitToken[1];
            int delimiterIndex = queryParams.indexOf("&");
            String id = queryParams.substring(0, delimiterIndex);
            searchByURL(id);
        } else {
            searchByKeyword(token);
        }
    }
}
