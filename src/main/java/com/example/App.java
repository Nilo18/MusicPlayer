package com.example;
import com.google.gson.Gson;
import com.github.kiulian.downloader.YoutubeDownloader;
import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import java.io.IOException;
import java.util.List;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

public class App {
    public static void main( String[] args ) {
//        String[] allowedCommands = {""};
        // The allowed commands should be:
        // mp play -"name", mp loop, mp skip, mp exit
        // 1. Analyze the user input for commands
        System.out.println("******************");
        System.out.println("MusicPlayer CLI");
        System.out.println("******************");
        System.out.println();
        System.out.println("Choose a command: ");
        System.out.println("""
         1) mp search -"NAME OF THE SONG" --- Search the desired music.
         2) mp download -NUMBER OF THE RESULT --- Download one music. (MUST USE THE SEARCH COMMAND BEFOREHAND)
         3) mp play -"NAME OF THE SONG" --- Play the desired music locally. (MUSIC MUST BE DOWLOADED BEFOREHAND)
         4) mp loop --- Loop the music which is currently playing.
         5) mp skip --- Skip the music which is playing currently.
         6) mp exit --- Exit the CLI
         ************************************************************** \n
         """);
        final String API_KEY = "AIzaSyCuc-P9rBKk-dTm6-Q7IqfmlSVnJF0-b_s";
        Scanner sc = new Scanner(System.in);
        boolean isRunning = true;
        boolean isPromptingDownload = false;

        // 2. Search YouTube by the given keyword if the command is mp play

        while (isRunning) {
            String option = sc.nextLine();

            if (option.contains("mp search -")) {
                String[] token = option.split("\"");
                String keyword = token[1];
                System.out.println("Processing...");
                // JsonFactory is an interface which defines how a json parser should behave
                // GsonFactory.getDefaultInstance() sets up the parser with the default settings
                JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
                // setApplicationName() will notify google which app is making a request to their API
                // YouTubeRequestInitializer injects the API key to every request
                // The first parameter pass an http transport to actually make requests
                // The second one passes a parser to parse data
                // The third one is a shorthand for HttpRequestInitializer, which initializes http requests
                YouTube youtube = new YouTube.Builder(
                        new NetHttpTransport(),
                        jsonFactory,
                        request -> {}
                ).setApplicationName("youtube-search-demo").
                        setYouTubeRequestInitializer(new YouTubeRequestInitializer(API_KEY)).build();
//            YouTube.Search.List search;

                try {
                    // list() specifies which data we want to receive, snippet contains the title, thumbnail, etc.
                    YouTube.Search.List search = youtube.search().list("id, snippet");
                    search.setQ(keyword); // Set the query to the suggested song name
                    search.setType("video"); // Set the type of the request to video
                    search.setMaxResults(5L); // L is used here because the method expects long

                    // This will send the request to the YouTube's servers and wait for the response
                    SearchListResponse response = search.execute();
                    // This will turn all the returned video items into a java list
                    List<SearchResult> results = response.getItems();

                    System.out.println("The first five results are: ");
                    for (SearchResult result : results) {
                        System.out.println("\nTitle: " + result.getSnippet().getTitle());
                        System.out.println("URL: https://www.youtube.com/watch?v=" + result.getId().getVideoId());
                    }
                } catch (IOException err) {
                    System.out.println("Couldn't receive response from the search: " + err);
                    return;
                }

                System.out.println("Type mp download -NUMBER OF THE RESULT to download the desired music, "
                        + "mp download all to download all of the five results.");
                isPromptingDownload = true;
//                sc.nextLine();
            } else if (option.contains("mp download -") && isPromptingDownload) {
                String[] selectedOption = option.split("-");
                System.out.println("Downloading option " + selectedOption[1] + "...");
            } else if (option.contains("mp download all") && isPromptingDownload) {
                System.out.println("Downloading all...");
            } else if (option.contains("mp exit")) {
                System.out.println("Exiting MusicPlayer...");
                isRunning = false;
            } else {
                System.out.println("Unrecognized command.");
            }
        }


        // 3. Parse the first 5-10 results of the search using Gson
        // 4. Ask the user to choose between one of them by typing numbers 1-10
        // 5. Download the selected option using java-youtube-downloader, it should be stored in MpMusic folder
        // (If the suggested video is already downloaded, play it locally instead)
        // 6. Allow other commands like md pause, md loop, etc.
        Gson gson = new Gson();
    }
}
