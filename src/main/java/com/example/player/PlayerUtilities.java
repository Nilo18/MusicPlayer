package com.example.player;

import com.example.utilities.Color;
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

    public static String printPlaylist(List<Path> playlist, AtomicInteger selectedRow, Terminal terminal) {
        if (playlist.isEmpty()) {
            System.out.println("The playlist is empty.");
            Player.setPlaylistSelectionMode(false);
            return "";
        }

        if (playlist.size() < 10) {
            PlaylistPagination.setEnd(playlist.size());
        }

        String selectedMusic = "";
        System.out.println("Press ARROW UP or ARROW DOWN to navigate over the list.");
        System.out.println("Press ARROW RIGHT or ARROW LEFT to change pages.");
        System.out.println("Press ENTER to select and play the desired music and q to exit.");
        System.out.println("Viewing page " + PlaylistPagination.getPageNumber());

        System.out.println("Size of the playlist is: " + PlayerManager.getQueue().getPlaylist().size());
        for (int i = PlaylistPagination.getStart(); i < PlaylistPagination.getEnd(); i++) {
//            System.out.println("The i is: " + i);
            String[] tokens = playlist.get(i).toString().split("MpMusic\\\\");
            String musicName = tokens[1];
            String prefix = "> ";
            if (i + 1 == selectedRow.get()) {
                terminal.writer().println(Color.BLUE + prefix + musicName + Color.RESET);
                selectedMusic = musicName;
            } else {
                terminal.writer().println(prefix + musicName);
            }
        }
        return selectedMusic;
    }

    // AtomicInteger is used to make sure that this method modifies the original selectedRow variable
    public static String moveUpOnPlaylist(List<Path> playlist, AtomicInteger selectedRow, Terminal terminal) {
        if (selectedRow.get() > PlaylistPagination.getStart() + 1) selectedRow.decrementAndGet();
        clearTerminal(terminal);
        return printPlaylist(playlist, selectedRow, terminal);
    }



    public static String moveDownOnPlaylist(List<Path> playlist, AtomicInteger selectedRow, Terminal terminal) {
        if (selectedRow.get() <= PlaylistPagination.getEnd() - 1) selectedRow.incrementAndGet();
        clearTerminal(terminal);
        return printPlaylist(playlist, selectedRow, terminal);
    }


    public static void playPlaylistMusic(String selectedMusic, Terminal terminal) {
        if (!selectedMusic.isEmpty()) {
            String trimmedSelectedMusic = selectedMusic.replace("> ", "");
            Path filePath = Paths.get(System.getProperty("user.home"), "MpMusic", trimmedSelectedMusic);
            if (!Files.exists(filePath)) {
                terminal.writer().println("Could not find the given music file.");
                return;
            }
            terminal.writer().println("Playing: " + trimmedSelectedMusic + "...");
            PlayerManager.play(filePath);
            Player.setPlaylistSelectionMode(false);
        }
    }
}
