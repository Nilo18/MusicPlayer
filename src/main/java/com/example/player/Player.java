package com.example.player;

import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.util.List;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Player {
    private MediaPlayer player;
    private Queue queue = new Queue();
    private boolean isLooped = false;
    // Made this static so PlayerUtilities.printPlaylist() doesn't have to take it as the 4th parameter
    // PlayerUtilities.printPlaylist() needs access to it in order to reset the selection mode
    // in case the playlist is empty
    private static AtomicBoolean isSelecting = new AtomicBoolean(false);

    public void play(Path musicPath) {
        if (player != null) {
            player.dispose();
        }

        // Add new music to queue,
        // this ensures that the program doesn't have to be restarted
        // in order for the new music to be included in the queue
        System.out.println();
        Media media = new Media(musicPath.toUri().toString());
        player = new MediaPlayer(media);

        isLooped = false;
        // Add listener to the end of the music so it plays the next one in the queue
        player.setOnEndOfMedia(this::playNext);

        player.setOnReady(() -> {
            player.seek(Duration.ZERO); // ensure playback starts from beginning
            player.play();
        });
        player.setOnError(() -> {
            MediaException ex = player.getError();
            switch (ex.getType()) {
                case MEDIA_UNSUPPORTED -> System.out.println("The music contains unsupported file format.");
                case MEDIA_INACCESSIBLE -> System.out.println("Couldn't open the downloaded music file.");
                case MEDIA_UNAVAILABLE -> System.out.println("Media is currently unavailable.");
                default -> System.out.println("Unknown error has occurred: " + ex.getType());
            }
        });
    }

    public void playNext() {
        Path nextMusic = queue.getNextMusic();
        if (nextMusic != null) {
            play(nextMusic);
        } else {
            System.out.println("Playlist finished.");
        }
    }

    public void loop() {
        if (player != null) {
            isLooped = true;
            player.setOnEndOfMedia(() -> {
                player.seek(Duration.ZERO);
                player.play();
            });
        }
        player.setCycleCount(MediaPlayer.INDEFINITE);
        this.isLooped = true;
    }

    public void pause() {
        // Ensure that the player is properly initialized
        if (player == null) {
            System.out.println("No music to pause.");
            return;
        }
        // Only pause if there is a music playing, this condition is handled in PlayerManager as well
        // But PlayerManager is basically a wrapper of a single Player object and handles the condition
        // only for that specific Player object, here we handle the case for all Player objects to ensure
        // That none of them throw unexpected exceptions
        MediaPlayer.Status status = player.getStatus();
        if (status == MediaPlayer.Status.PLAYING) {
            player.pause();
            System.out.println("Music paused.");
        }
    }

    public void resume() {
        // If the player hasn't been initialized, exit prematurely to prevent nullptr errors
        if (player == null) {
            System.out.println("No music to resume.");
            return;
        }

        MediaPlayer.Status status = player.getStatus();
        // If the music isn't already playing, play it again
        if (status != MediaPlayer.Status.PLAYING) {
            player.play();
            System.out.println("Music resumed.");
        }
    }

    public void forward(int howMuch) {
        if (player == null) {
            System.out.println("No music to forward.");
            return;
        }

        Duration currentTime = player.getCurrentTime();
        Duration newTime = currentTime.add(Duration.seconds(howMuch));
        Duration totalTime = player.getTotalDuration();
        if (!isLooped) {
            player.seek(newTime);
        } else {
            if (newTime.greaterThanOrEqualTo(totalTime)) {
                newTime = Duration.ZERO;
            }
            player.seek(newTime);
        }
    }

    public void rewind(int howMuch) {
        if (player == null) {
            System.out.println("No music to rewind.");
            return;
        }

        Duration currentTime = player.getCurrentTime();
        Duration newTime = currentTime.subtract(Duration.seconds(howMuch));

        if (newTime.lessThan(Duration.ZERO)) {
            newTime = Duration.ZERO;
        }

        player.seek(newTime);
    }

    public Queue getQueue() {
        return queue;
    }

    public void setLoopState(boolean val) {
        this.isLooped = val;
    }

    public boolean isLooping() {
        return isLooped;
    }

    /* Enters a mode where user can choose between the playlist using arrows */
    public void activatePlaylistSelectionMode() {
        List<Path> playlist = queue.getPlaylist();
        AtomicInteger selectedRow = new AtomicInteger(1);
        String selectedMusic = "";
        try {
            Terminal terminal = TerminalBuilder.builder().system(true).build();
            terminal.enterRawMode();
            terminal.flush();
            terminal.writer().flush();
            /* It is important to make this true before the initial printing
             * because printPlaylist will set isSelecting to false if the music directory is empty
             * and if isSelecting was set to true again after that, it would unintentionally enter
             * the selection mode again. */
            isSelecting.set(true);
            selectedMusic = PlayerUtilities.printPlaylist(playlist, selectedRow, terminal);
            terminal.flush();
            while (isSelecting.get()) {
                int ch = terminal.reader().read();

                switch(ch) {
                    // 65 stands for arrow up
                    case 65 -> {
                        selectedMusic = PlayerUtilities.moveUpOnPlaylist(playlist, selectedRow, terminal);
                        terminal.flush();
                    }
                    // 66 stands for arrow down
                    case 66 -> {
                        selectedMusic = PlayerUtilities.moveDownOnPlaylist(playlist, selectedRow, terminal);
                        terminal.flush();
                    }

                    // 67 stands for arrow right
                    case 67 -> {
                        selectedMusic = PlaylistPagination.moveForward(playlist, selectedRow, terminal);
                        terminal.flush();
                    }

                    // 68 stands for arrow left
                    case 68 -> {
                        selectedMusic = PlaylistPagination.moveBehind(playlist, selectedRow, terminal);
                        terminal.flush();
                    }

                    // 13 stands for Enter
                    case 13 -> {
                        if (!selectedMusic.isEmpty()) {
                            PlayerUtilities.playPlaylistMusic(selectedMusic, terminal);
                            PlaylistPagination.setStart(0);
                            PlaylistPagination.setEnd(10);
                            PlaylistPagination.setMoveRange(10);
                            PlaylistPagination.setPageNumber(1);
                            System.out.println("\nExited the selection mode.");
                        } else {
                            System.out.println("Missing selected music to play from the playlist.");
                        }
                    }
                    // 133 stands for q
                    case 113 -> {
                        System.out.println("\nExited the selection mode.");
                        PlaylistPagination.setStart(0);
                        PlaylistPagination.setEnd(10);
                        PlaylistPagination.setMoveRange(10);
                        PlaylistPagination.setPageNumber(1);
//                        System.out.println("Reset the page number.");
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
            System.out.println("Unexpected error has occurred while trying to show the playlist: " + err);
        }
    }

    public static boolean getPlaylistSelectionState() {
        return isSelecting.get();
    }

    public static void setPlaylistSelectionMode(boolean val) {
        isSelecting.set(val);
    }
}
