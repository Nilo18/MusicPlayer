package com.example.searcher;

import com.example.AppConfig;
import com.example.downloader.Downloader;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import java.io.IOException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Searcher {
/* Enters a mode where user can choose between the searched results using arrows */
public static void activateSearchSelectionMode(List<SearchResult> results) {
    AtomicBoolean isSelecting = new AtomicBoolean(true);
    AtomicInteger selectedRow = new AtomicInteger(1);
    String selectedMusic = "";
    try {
        Terminal terminal = TerminalBuilder.builder().system(true).build();
        terminal.enterRawMode();
        terminal.flush();
        terminal.writer().print("\033[6n");
        terminal.writer().flush();
        selectedMusic = SearcherUtilities.printResults(results, selectedRow, terminal);
        terminal.flush();
        while (isSelecting.get()) {
            int ch = terminal.reader().read();

            switch(ch) {
                // 65 stands for arrow up
                case 65 -> {
                    selectedMusic = SearcherUtilities.moveUpOnSearchResults(results, selectedRow, terminal);
                    terminal.flush();
                }
                // 66 stands for arrow down
                case 66 -> {
                    selectedMusic = SearcherUtilities.moveDownOnSearchResults(results, selectedRow, terminal);
                    terminal.flush();
                }
                // 13 stands for Enter
                case 13 -> {
                    String finalSelectedMusic = selectedMusic;
                    AtomicReference<String> url = new AtomicReference<>("");
                    // Construct downloader API url by extracting the video from the selected music
                    Optional<SearchResult> found = results.stream()
                            .filter(result -> result.getSnippet().getTitle().equals(finalSelectedMusic))
                            .findFirst();
                    found.ifPresent(match -> {
                       String videoId = match.getId().getVideoId();
                       url.set("https://youtube-mp36.p.rapidapi.com/dl?id=" + videoId);
                    });
                    SearcherUtilities.playResultsSelectedMusic(selectedMusic, String.valueOf(url.get()), terminal, isSelecting);
                }
                // 133 stands for q
                case 113 -> {
                    System.out.println("Pressing q");
                    System.out.println("\nExited the selection mode.");
                    isSelecting.set(false);
                }
                case -1 -> {
                    isSelecting.set(false);
                }
            }
        }
      terminal.close();
       } catch (IOException err) {
           System.out.println("Couldn't initialize the terminal.");
       } catch (Exception err) {
           System.out.println("Unexpected error has occurred while trying to show results: " + err);
           err.printStackTrace();
       }
    }

    public static void searchByKeyword(String keyword, Long amount) {
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
            search.setMaxResults(amount); // L is used here because the method expects long

            // This will send the request to the YouTube's servers and wait for the response
            SearchListResponse response = search.execute();
            // This will turn all the returned video items into a java list
            List<SearchResult> results = response.getItems();

            if (results != null && !results.isEmpty()) {
                SearchResult video = results.get(0); // only item
                String title = video.getSnippet().getTitle();

                String url = "https://youtube-mp36.p.rapidapi.com/dl?id=" + video.getId().getVideoId();
                if (amount > 1L) {
                    activateSearchSelectionMode(results);
                    return;
                }
                Downloader.downloadVideo(title, url);
            } else {
                System.out.println("No such video was found.");
            }
        }
        // If there's no internet, look for the music locally
        catch (UnknownHostException err) {
            SearcherUtilities.handleNoInternet(keyword);
        } catch (IOException err) {
            System.out.println("Couldn't receive response from the search: " + err);

        } catch (Exception err) {
            System.err.println("An error occurred during download, please try again.");
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
        try {
            HttpClient client = HttpClient.newHttpClient();
            // Make a request to the given YouTube music video page
            HttpRequest req = HttpRequest.newBuilder().
                    uri(URI.create(youtubeURL)).
                    header("User-Agent", "Mozilla/5.0").
                    build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() == 200) {
                // If the page was found parse the HTML and extract the title
                String videoHTML = res.body();
                String title = extractTitle(videoHTML);

                // Make a request to the downloader API to download the video
                Downloader.downloadVideo(title, url);
            }
        } catch (IOException err) {
            System.out.println("Couldn't receive response from the search: " + err);
        } catch (InterruptedException err) {
            System.out.println("The search was interrupted: " + err);
        } catch (IllegalArgumentException err) {
            System.out.println("Invalid URL argument: " + err);
        }
    }

    public static void search(String option, Long amount) {
        String[] token = option.split("\"");
        String musicName = token[0];
        // If the user entering a URL extract the id of the video from it using Jsoup
        // Else search by the name using YouTube API
        if (musicName.contains("https://www.youtube.com/watch?v=")) {
            System.out.println("Searching " + musicName + "...");
            String[] splitToken = musicName.split("v=");
            String queryParams = splitToken[1];
            int delimiterIndex = queryParams.indexOf("&");
            String id = queryParams.substring(0, delimiterIndex);
            searchByURL(id);
        } else if (musicName.contains("https://") && !musicName.contains("www.youtube.com/watch?v=")) {
            System.out.println("Only valid youtube video URLs are allowed.");
        } else {
            System.out.println("Searching " + musicName + "...");
            searchByKeyword(musicName, amount);
        }
    }
}
