package com.example.searcher;

import com.example.AppConfig;
import com.example.downloader.Downloader;
import com.example.utilities.Utilities;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import com.google.api.services.youtube.model.SearchListResponse;
//import com.google.api.services.youtube.model.SearchResult;
//import com.example.searcher.SearchResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
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
                            .filter(result -> result.getTitle().equals(finalSelectedMusic))
                            .findFirst();
                    found.ifPresent(match -> {
                       String videoId = match.getId();
                       url.set(videoId);
                    });
                    SearcherUtilities.playResultsSelectedMusic(
                            selectedMusic, String.valueOf(url.get()), terminal, isSelecting
                    );
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
       }
    }

    public static void searchByKeyword(String keyword, Long amount) {
            String ytDlp = Utilities.getBinaryPath("yt-dlp");
            ProcessBuilder pb = new ProcessBuilder(
                    ytDlp, "ytsearch" + amount + ":" + keyword,
                    "--skip-download", "--print", "%(id)s | %(title)s", "--no-playlist",
                    "--quiet"
            );
            List<SearchResult> results = new ArrayList<>();
            try {
                Process process = pb.start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(" \\| ");
                        if (parts.length >= 2) {
                            results.add(new SearchResult(parts[0], parts[1]));
                        }
                    }
                }

                process.waitFor();

                if (!results.isEmpty()) {
                    if (amount > 1) {
                        // Pass the list to your existing selection logic
                        activateSearchSelectionMode(results);
                    } else {
                        // Immediate mode for "amount = 1"
                        SearchResult video = results.get(0);
                        String title = video.getTitle();
                        String videoId = video.getId();

                        // Note: You can now bypass the RapidAPI and use yt-dlp directly for the download!
                        Downloader.downloadVideoYtDlp(title, videoId);
                    }
                } else {
                    System.out.println("No such video was found.");
                }
            } catch (IOException e) {
                System.out.println("Couldn't start search process." + e);
            } catch (InterruptedException e) {
                System.out.println("Search process was interrupted: " + e);
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
        String youtubeURL = "https://www.youtube.com/watch?v=" + id;
        String ytDlp = Utilities.getBinaryPath("yt-dlp");
        ProcessBuilder pb = new ProcessBuilder(
                ytDlp, youtubeURL,
                "--skip-download", "--print", "%(id)s | %(title)s", "--no-playlist",
                "--quiet"
        );
        try {
            Process process = pb.start();

            SearchResult video = getSearchResult(process);
            int exitCode = process.waitFor();

            if (video != null && exitCode == 0) {
                // Pass title and ID directly to your downloader
                Downloader.downloadVideoYtDlp(video.getTitle(), video.getId());
            } else {
                System.out.println("No such video was found or the process failed.");
            }
        } catch (IOException e) {
            System.out.println("Couldn't start search process." + e);
        } catch (InterruptedException e) {
            System.out.println("Search process was interrupted." + e);
        }
    }

    private static SearchResult getSearchResult(Process process)  {
        SearchResult video = null;

        // Use try-with-resources to automatically close the reader
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" \\| ");
                if (parts.length >= 2) {
                    video = new SearchResult(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            System.out.println("Couldn't read video metadata: " + e);
        }
        return video;
    }

    public static void search(String option, Long amount) {
        String[] token = option.split("\"");
        String musicName = token[0];
        // If the user entering a URL extract the id of the video from it using Jsoup
        // Else search by the name using YouTube API
        // * For multiple query params
        if (musicName.contains("https://www.youtube.com/watch?v=") && musicName.contains("&")) {
            System.out.println("Searching " + musicName + "...");
            String[] splitToken = musicName.split("v=");
            String queryParams = splitToken[1];
            int delimiterIndex = queryParams.indexOf("&");
            String id = queryParams.substring(0, delimiterIndex);
            searchByURL(id);
        }
        // * For a single query param (id)
        else if (musicName.contains("https://www.youtube.com/watch?v=") && !musicName.contains("&")) {
            String[] splitToken = musicName.split("v=");
            String queryParams = splitToken[1];
            searchByURL(queryParams);
        } else if (musicName.contains("https://") && !musicName.contains("www.youtube.com/watch?v=")) {
            System.out.println("Only valid youtube video URLs are allowed.");
        } else {
            System.out.println("Searching " + musicName + "...");
            searchByKeyword(musicName, amount);
        }
    }
}
