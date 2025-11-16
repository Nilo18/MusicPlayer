package com.example;

import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/* The following class will contain all utilities related to the player */
public class PlayerUtilities {
    public static String printPlaylist(List<Path> playlist, AtomicInteger selectedRow, Terminal terminal) {
        String selectedMusic = "";
        for (int i = 0; i < playlist.size(); i++) {
            String[] tokens = playlist.get(i).toString().split("MpMusic\\\\");
            String musicName = tokens[1];
            String prefix = "> ";
            if (i + 1 == selectedRow.get()) {
                System.out.println("Painting selectedMusic blue...");
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
        System.out.println("The selectedRow: " + selectedRow);
        if (selectedRow.get() > 1) selectedRow.decrementAndGet();
        terminal.writer().print("\033[3J");  // clear scrollback
        terminal.puts(InfoCmp.Capability.clear_screen);
        terminal.puts(InfoCmp.Capability.cursor_home);
        terminal.flush();
        return printPlaylist(playlist, selectedRow, terminal);
//        terminal.flush();
        // Adjust cursor row because Capability.cursor_address is 0 based
//                        int cursorRow = selectedRow - 1;
//                        int cursorCol = 0;
//                        terminal.puts(Capability.cursor_address, cursorRow, cursorCol);
//                        terminal.flush();
    }


    public static String moveDownOnPlaylist(List<Path> playlist, AtomicInteger selectedRow, Terminal terminal) {
        if (selectedRow.get() < playlist.size()) selectedRow.incrementAndGet();
        terminal.writer().print("\033[3J");  // clear scrollback
        terminal.puts(InfoCmp.Capability.clear_screen);
        terminal.puts(InfoCmp.Capability.cursor_home);
        terminal.flush();
        return printPlaylist(playlist, selectedRow, terminal);
//        terminal.flush();
        // Adjust cursor row because Capability.cursor_address is 0 based
//                        int cursorRow = selectedRow - 1;
//                        int cursorCol = 0;
//                        terminal.puts(Capability.cursor_address, cursorRow, cursorCol);
//                        terminal.flush();
    }

    public static void playPlaylistMusic(String selectedMusic, Terminal terminal, AtomicBoolean isSelecting) {
        if (!selectedMusic.isEmpty()) {
            String trimmedSelectedMusic = selectedMusic.replace("> ", "");
            terminal.writer().println("Attempting to play: " + trimmedSelectedMusic);
            Path filePath = Paths.get(System.getProperty("user.home"), "MpMusic", trimmedSelectedMusic);
            PlayerManager.play(filePath);
            if (!Files.exists(filePath)) {
                terminal.writer().println("Could not find the given music file.");
            }
            isSelecting.set(false);
        }
    }
}
