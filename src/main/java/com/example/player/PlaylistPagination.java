package com.example.player;

import org.jline.terminal.Terminal;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PlaylistPagination {
    private static int start = 0;
    private static int end = 10;
    private static int moveRange = 10;
    private static int pageNumber = 1;
    private static int playlistSize = PlayerManager.getQueue().getPlaylist().size();
    private static boolean shouldAdjustMoveRange = false;

    public static String moveBehind(List<Path> playlist, AtomicInteger selectedRow, Terminal terminal) {
        if (start - moveRange >= 0 && end - moveRange >= 0) {
            start -= moveRange;
            end -= moveRange;
            selectedRow.set(start + 1); // Mark the top of the current page as the selected row
            pageNumber--;
            PlayerUtilities.clearTerminal(terminal);
            // This if statement is necessary to reset moveRange back to 10 in case end + moveRange exceeded
            // the playlist size.
            // moveRange is reset here because this will allow moveBehind to first move back exactly as much as
            // moveForward went ahead first and then start moving behind by the default move range again.
            // For example, if end = 70, moveRange = 10, and playlistSize = 75
            // end + moveRange = 80, and it would've exceeded the playlist and moveForward()
            // would calculate the difference as 5 and would've done end + 5 which matches the playlistSize (75),
            // in this case, moveBehind would first move back by 5
            // and then this if statement would reset moveRange back to 10 which would avoid moving incorrectly
            if (shouldAdjustMoveRange) {
                moveRange = 10;
            }
            return PlayerUtilities.printPlaylist(playlist, selectedRow, terminal);
        }
        return "";
    }

    public static String moveForward(List<Path> playlist, AtomicInteger selectedRow, Terminal terminal) {
        // If moving forward would cause going out of bounds,
        // readjust the move range to cover the remaining songs on the last page
        // This will avoid both out of bounds and missing out last few songs
        if (end + moveRange > playlistSize && end != playlistSize) {
            // Calculate the difference between the end of the music and end + moveRange
            // This will determine how much should moveRange be adjusted
            int difference = 0;
            for (int i = end; i < playlistSize; i++) {
                difference++;
            }

            // Readjust the moveRange
            moveRange = difference;
        }

        if (start + moveRange < playlistSize && end + moveRange <= playlistSize) {
            start += moveRange;
            end += moveRange;
            selectedRow.set(start + 1);
            pageNumber++;
            PlayerUtilities.clearTerminal(terminal);
            // After moving forward, use shouldAdjustMoveRange to notify moveBehind to act accordingly
            shouldAdjustMoveRange = true;
            return PlayerUtilities.printPlaylist(playlist, selectedRow, terminal);
        }
        return "";
    }

    public static int getStart() {
        return start;
    }

    public static int getMoveRange() {
        return moveRange;
    }

    public static int getPageNumber() {
        return pageNumber;
    }

    public static int getEnd() {
        return end;
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
