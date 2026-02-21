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
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.jline.consoleui.prompt.ConsolePrompt;
import org.jline.consoleui.prompt.PromptResultItemIF;
import org.jline.consoleui.prompt.builder.ListPromptBuilder;
import org.jline.consoleui.prompt.builder.PromptBuilder;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Searcher {
/* Enters a mode where user can choose between the searched results using arrows */
public static void activateSearchSelectionMode(List<SearchResult> results) {
    AtomicBoolean isSelecting = new AtomicBoolean(true);
    Terminal terminal = null;
    Attributes originalAttributes = null;
    try {
        terminal = TerminalBuilder.builder().system(true).build();
        originalAttributes = terminal.getAttributes();
        terminal.flush();
        terminal.puts(InfoCmp.Capability.cursor_invisible);
        ConsolePrompt prompt = new ConsolePrompt(terminal);
        PromptBuilder builder = prompt.getPromptBuilder();
        ListPromptBuilder listBuilder = builder.createListPrompt().
                name("search").
                message("Choose one of the 5 results:");

        for (int i = 0; i < results.size(); i++) {
            listBuilder.newItem(String.valueOf(i)).text(results.get(i).getTitle()).add();
        }
        listBuilder.newItem("QUIT").text("[X] Exit selection mode").add();
        listBuilder.addPrompt();

        Map<String, PromptResultItemIF> res = prompt.prompt(builder.build());

        PromptResultItemIF selectedItem = res.get("search");

        if (selectedItem != null) {
            String selectId = selectedItem.getResult();

            if (!selectId.equals("QUIT")) {
                int index = Integer.parseInt(selectId);
                SearcherUtilities.playResultsSelectedMusic(
                        results.get(index).getTitle(),
                        results.get(index).getId(),
                        terminal, isSelecting
                );
                terminal.puts(InfoCmp.Capability.cursor_normal);
            } else {
                terminal.setAttributes(originalAttributes);
                terminal.puts(InfoCmp.Capability.cursor_normal);
                terminal.flush();
                terminal.close();
                return;
            }
        }
      terminal.close();
       } catch (IOException err) {
           System.out.println("Couldn't initialize the terminal.");
       } catch (Exception err) {
           System.out.println("Unexpected error has occurred while trying to show results: " + err);
       }
    finally {
            if (terminal != null && originalAttributes != null) {
                terminal.setAttributes(originalAttributes);
                terminal.puts(InfoCmp.Capability.cursor_normal);
                terminal.flush();
            }
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
