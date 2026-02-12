package com.example.searcher;

import com.example.utilities.Color;
import com.example.downloader.Downloader;
import com.example.player.PlayerManager;
import com.google.api.services.youtube.model.SearchResult;
import javafx.application.Platform;
import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SearcherUtilities {
    public static String printResults(List<SearchResult> results, AtomicInteger selectedRow, Terminal terminal) {
        String selectedResult = "";
        for (int i = 0; i < results.size(); i++) {
            String prefix = "> ";
            String musicName = results.get(i).getSnippet().getTitle();
            if (i + 1 == selectedRow.get()) {
                terminal.writer().println(Color.BLUE + prefix + musicName + Color.RESET);
                selectedResult = musicName;
            } else {
                terminal.writer().println(prefix + musicName);
            }
        }
        return selectedResult;
    }

    public static String moveUpOnSearchResults(List<SearchResult> results, AtomicInteger selectedRow, Terminal terminal) {
        terminal.writer().println("Given value of selected row: " + selectedRow.get());
        terminal.flush();
        if (selectedRow.get() > 1) selectedRow.decrementAndGet();
        terminal.writer().println("New value of selected row: " + selectedRow.get());
        terminal.flush();
        terminal.writer().print("\033[3J");  // clear scrollback
        terminal.puts(InfoCmp.Capability.clear_screen);
        terminal.puts(InfoCmp.Capability.cursor_home);
        terminal.flush();
        return printResults(results, selectedRow, terminal);
    }

    public static String moveDownOnSearchResults(List<SearchResult> results, AtomicInteger selectedRow, Terminal terminal) {
        System.out.println("Given value of selected row: " + selectedRow.get());
        if (selectedRow.get() < results.size()) selectedRow.incrementAndGet();
        System.out.println("New value of selected row: " + selectedRow.get());
        terminal.writer().print("\033[3J");  // clear scrollback
        terminal.puts(InfoCmp.Capability.clear_screen);
        terminal.puts(InfoCmp.Capability.cursor_home);
        terminal.flush();
        return printResults(results, selectedRow, terminal);
    }

    public static void playResultsSelectedMusic(String selectedMusic, String url, Terminal terminal, AtomicBoolean isSelecting) {
        if (!selectedMusic.isEmpty()) {
            String trimmedSelectedMusic = selectedMusic.replace("> ", "");
            Downloader.downloadVideoYtDlp(trimmedSelectedMusic, url);
            isSelecting.set(false);
        }
    }

    public static void handleNoInternet(String keyword) {
        System.out.println("Failed to search due to lack of internet connection.");
        System.out.println("Searching for the music locally...");
        String musicFileName = keyword.endsWith(".mp3") ? keyword : keyword + ".mp3";
        Path potentialPath = Paths.get(System.getProperty("user.home"), "MpMusic", musicFileName);
        List<Path> playlist = PlayerManager.getQueue().getPlaylist();
        boolean musicWasFoundLocally = false;
        for (Path musicPath : playlist) {
                /*
                 Extract the actual music name from the path
                 We could also compare the path directly using musicPath.toString()
                 But it wouldn't bring any benefit because the name of the file is what matters.
                */
            String[] tokens = musicPath.toString().split("MpMusic\\\\");
            String musicName = tokens[1];
            if (musicName.toLowerCase().contains(keyword.toLowerCase())) {
                // If any of the titles match the keyword, play the path and exit the loop
                // This will prevent play() method being called too many times.
                System.out.println("Music found locally, playing...");
                Platform.runLater(() -> PlayerManager.play(musicPath));
                musicWasFoundLocally = true;
                break;
            }
        }
        if (!musicWasFoundLocally) {
            System.out.println("Couldn't find the music locally either.");
        }
    }
}
