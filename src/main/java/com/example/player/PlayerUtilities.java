package com.example.player;

import com.example.utilities.Color;
import org.jline.consoleui.prompt.builder.ListPromptBuilder;
import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/* The following class will contain all utilities related to the player */
public class PlayerUtilities {
    public static void clearTerminal(Terminal terminal) {
        terminal.writer().print("\033[3J");  // clear scrollback
        terminal.puts(InfoCmp.Capability.clear_screen);
        terminal.puts(InfoCmp.Capability.cursor_home);
        terminal.flush();
    }

    public static void printPlaylist(List<Path> playlist, ListPromptBuilder listBuilder, Terminal terminal) {
        if (playlist.isEmpty()) {
            System.out.println("The playlist is empty.");
            Player.setPlaylistSelectionMode(false);
            return;
        }

        if (playlist.size() < 10) {
            PlaylistPagination.setEnd(playlist.size());
        }

        listBuilder.newItem("PREV").text(" << Previous Page").add();
        listBuilder.newItem("NEXT").text(" >> Next Page").add();
        listBuilder.newItem("QUIT").text("[X] Exit selection mode").add();

        for (int i = PlaylistPagination.getStart(); i < PlaylistPagination.getEnd(); i++) {
            String[] tokens = playlist.get(i).toString().split("MpMusic\\\\");
            String musicName = tokens[1];
            listBuilder.newItem(String.valueOf(i)).text(musicName).add();
        }
    }

    public static void playPlaylistMusic(String selectedMusic, Terminal terminal) {
        if (!selectedMusic.isEmpty()) {
            String[] tokens = selectedMusic.split("MpMusic\\\\");
            String musicName = tokens[1];
            System.out.println("Trimmed the music name to: " + musicName);
            Path filePath = Paths.get(System.getProperty("user.home"), "MpMusic", musicName);
            if (!Files.exists(filePath)) {
                terminal.writer().println("Could not find the given music file.");
                return;
            }
            terminal.writer().println("Playing: " + musicName + "...");
            PlayerManager.play(filePath);
            Player.setPlaylistSelectionMode(false);
        }
    }
}
