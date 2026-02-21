package com.example.player;

import org.jline.consoleui.prompt.builder.ListPromptBuilder;
import org.jline.terminal.Terminal;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PlaylistPagination {
    private static int start = 0;
    private static int end = 25;
    private static int moveRange = 25;
    private static int pageNumber = 1;
    public static final int pageSize = 25;
    private static int playlistSize = PlayerManager.getQueue().getPlaylist().size();
    private static boolean shouldAdjustMoveRange = false;

    public static void moveBehind(List<Path> playlist, Terminal terminal, ListPromptBuilder listBuilder) {
        if (start - pageSize >= 0) {
            start -= pageSize;
        }
    }

    public static void moveForward(List<Path> playlist, Terminal terminal, ListPromptBuilder listBuilder) {
        if (start + pageSize < playlistSize) {
            start += pageSize;
        }
    }

    public static int getStart() {
        return start;
    }

    public static int getMoveRange() {
        return moveRange;
    }

    public static int getPageNumber() {
        return (start / pageSize) + 1;
    }

    public static int getEnd() {
        return Math.min(start + pageSize, playlistSize);
    }

    public static void setStart(int val) {
        start = val;
    }

    public static void setEnd(int val) {
        end = val;
    }

    public static void setMoveRange(int val) {
        moveRange = val;
    }

    public static void setPageNumber(int val) {
        pageNumber = val;
    }
}
